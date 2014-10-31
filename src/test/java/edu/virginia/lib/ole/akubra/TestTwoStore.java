
package edu.virginia.lib.ole.akubra;

import static com.google.common.base.Strings.repeat;
import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Files.createTempDir;
import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class TestTwoStore {

    private static final String testURIinfix = "test:";

    private static final Logger log = getLogger(TestTwoStore.class);

    private BlobStore right, left;

    private final IdMapper bitstringmapper = new BitStringMapper();

    private final IdMapper filemapper = new FilePrefixMapper();

    @Before
    public void setUp() {
        final File rightfile = createTempDir();
        final BlobStore rightfilestore = new FSBlobStore(create("rightfile"), rightfile);
        log.debug("Created temporary right-hand BlobStore in: {}", rightfile.getAbsolutePath());
        final File leftfile = createTempDir();
        final BlobStore leftfilestore = new FSBlobStore(create("leftfile"), leftfile);
        log.debug("Created temporary left-hand BlobStore in: {}", leftfile.getAbsolutePath());
        right = new IdMappingBlobStore(create("right"), rightfilestore, filemapper);
        log.debug("Created new file-mapped BlobStore to the right.");
        left = new IdMappingBlobStore(create("left"), leftfilestore, filemapper);
        log.debug("Created new file-mapped BlobStore to the left.");
    }

    @Test
    public void testInstantiateTwoStore() {
        final TwoStore teststore = new TwoStore(create("teststore"), left, right);
        assertEquals("teststore", teststore.getId().toString());
        assertEquals("right", teststore.getRight().getId().toString());
        assertEquals("left", teststore.getLeft().getId().toString());
    }

    @Test
    public void testOpenConnection() throws IOException {
        final TwoStore teststore = new TwoStore(create("teststore"), left, right);
        final BlobStoreConnection testconnection = teststore.openConnection(null, null);
        assertEquals(teststore, testconnection.getBlobStore());
    }

    @Test
    public void testStoreBlobs() throws IOException {
        String testURI, testdata;
        for (int i = 0; i < 101; i++) {
            testURI = ((i % 2 == 0) ? "A" : "B") + testURIinfix + i;
            testdata = i + repeat(testURIinfix, i) + i;
            storeOneBlob(testURI, testdata);
        }
    }

    private void storeOneBlob(final String testuristring, final String teststring) throws IOException {
        final TwoStore teststore = new TwoStore(create("teststore"), left, right);
        final URI testuri = create(testuristring);
        log.debug("Test URI: {}", testuri);
        final URI testencuri = bitstringmapper.getInternalId(testuri);
        log.debug("Test encoded URI: {}", testencuri);
        final InputStream testdata = new ByteArrayInputStream(teststring.getBytes("UTF-8"));
        final BlobStoreConnection testconnection = teststore.openConnection(null, null);
        final Blob testblob = testconnection.getBlob(testencuri, null);
        log.debug("Test blob created with internal URI: {}", testblob.getId());
        try (final OutputStream out = testblob.openOutputStream(0, true)) {
            copy(testdata, out);
        }
        log.debug("Stored '{}' in that Blob.", teststring);
        testconnection.close();
        log.debug("Closed test connection.");
        final BlobStoreConnection newtestconnection = teststore.openConnection(null, null);
        log.debug("Opened new test connection.");
        final Blob newtestblob = newtestconnection.getBlob(testencuri, null);
        log.debug("New test blob retrieved with internal URI: {}", newtestblob.getId());
        final String retrievedvalue = new String(toByteArray(newtestblob.openInputStream()));
        log.debug("Retrieved value: {}", retrievedvalue);
        assertEquals(teststring, retrievedvalue);
    }

}
