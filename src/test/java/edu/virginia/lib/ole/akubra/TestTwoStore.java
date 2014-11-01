
package edu.virginia.lib.ole.akubra;

import static com.google.common.base.Strings.repeat;
import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.mem.MemBlobStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class TestTwoStore {

    private static final String testURIinfix = "test:";

    private static final Logger log = getLogger(TestTwoStore.class);

    private BlobStore right, left;

    private final IdMapper bitstringmapper = new BitStringMapper();

    @Before
    public void setUp() {
        left = new MemBlobStore();
        right = new MemBlobStore();
    }

    @Test
    public void testInstantiateTwoStore() {
        final TwoStore teststore = new TwoStore(create("teststore"), left, right);
        assertEquals("teststore", teststore.getId().toString());
        assertEquals(right, teststore.getRight());
        assertEquals(left, teststore.getLeft());
    }

    @Test
    public void testOpenConnection() throws IOException {
        final TwoStore teststore = new TwoStore(create("teststore"), left, right);
        try (final TwoStoreConnection testconnection = teststore.openConnection(null, null)) {
            assertEquals(teststore, testconnection.getBlobStore());
        }
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

        try (final TwoStoreConnection testconnection = teststore.openConnection(null, null)) {
            final Blob testblob = testconnection.getBlob(testencuri, null);
            log.trace("Test blob created with internal URI: {}", testblob.getId());
            try (final OutputStream out = testblob.openOutputStream(0, true)) {
                copy(testdata, out);
            }
            log.trace("Stored '{}' in that Blob.", teststring);
        }
        try (final TwoStoreConnection newtestconnection = teststore.openConnection(null, null)) {
            final Blob newtestblob = newtestconnection.getBlob(testencuri, null);
            log.trace("New test blob retrieved with internal URI: {}", newtestblob.getId());
            try (final InputStream inputStream = newtestblob.openInputStream()) {
                final String retrievedvalue = new String(toByteArray(inputStream));
                log.trace("Retrieved value: {}", retrievedvalue);
                assertEquals(teststring, retrievedvalue);
            }
        }
    }
}
