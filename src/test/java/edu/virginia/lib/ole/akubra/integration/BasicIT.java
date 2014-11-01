
package edu.virginia.lib.ole.akubra.integration;

import static com.google.common.collect.Iterators.size;
import static com.google.common.io.ByteSource.wrap;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.net.URI.create;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.virginia.lib.ole.akubra.TwoStore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-xml/basic-operation.xml")
public class BasicIT {

    private static final Logger log = getLogger(BasicIT.class);
    
    private static final int NUMTESTBLOBS = 1000;

    @Inject
    private TwoStore testTwoStore;

    @Inject
    @MappedStore
    private BlobStore parentStore;

    @Inject
    @Right
    private BlobStore right;

    @Inject
    @Left
    private BlobStore left;

    private static Map<URI, String> testData = new HashMap<>();

    private static boolean testDataLoaded = false;

    static {
        log.debug("Generating test data...");
        for (int i = 0; i < NUMTESTBLOBS; i++) {
            final URI blobId = create(randomUUID().toString());
            final String testDataValue = randomUUID().toString();
            testData.put(blobId, testDataValue);
        }
        log.debug("Generated test data.");
    }


    @Before
    public void setUp() throws IOException {
        if (!testDataLoaded) {
            log.debug("Loading test data...");
            BlobStoreConnection testConn = null;
            try {
                testConn = parentStore.openConnection(null, null);
                for (final Map.Entry<URI, String> data : testData.entrySet()) {
                    final Blob blob = testConn.getBlob(data.getKey(), null);
                    try (final OutputStream outputStream = blob.openOutputStream(-1, true)) {
                        wrap(data.getValue().getBytes()).copyTo(outputStream);
                    }
                }
            } finally {
                testConn.close();
            }
            testDataLoaded = true;
            log.debug("Loaded test data.");
        }
    }

    @Test
    public void testBasicStructure() {
        assertEquals(right, testTwoStore.getRight());
        assertEquals(left, testTwoStore.getLeft());
    }

    @Test
    public void testBasicOperation() throws IOException {
        BlobStoreConnection testConn = null;
        try {
            testConn = parentStore.openConnection(null, null);
            final Iterator<URI> blobs = testConn.listBlobIds(null);
            while (blobs.hasNext()) {
                final URI blobId = blobs.next();
                final Blob blob = testConn.getBlob(blobId, null);
                try (final InputStream inputStream = blob.openInputStream()) {
                    final String data = new String(toByteArray(inputStream));
                    assertEquals(testData.get(blobId), data);
                }
            }
        } finally {
            testConn.close();
        }
    }

    @Test
    public void testBalance() throws IOException {
        int numInLeft = 0, numInRight = 0;

        BlobStoreConnection testConn = null;
        try {
            testConn = left.openConnection(null, null);
            numInLeft = size(testConn.listBlobIds(null));
        } finally {
            testConn.close();
        }
        try {
            testConn = right.openConnection(null, null);
            numInRight = size(testConn.listBlobIds(null));
        } finally {
            testConn.close();
        }
        log.debug("Got {} blobs in the left side and {} in the right side.", numInLeft, numInRight);
        assertFalse("No blobs went to the left!", numInLeft < 1);
        assertFalse("No blobs went to the right!", numInRight < 1);
    }
}
