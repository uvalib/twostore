/**
 * 
 */

package edu.virginia.lib.ole.akubra;

import static com.google.common.collect.Iterators.concat;
import static edu.virginia.lib.ole.akubra.Constants.CHUNK;
import static java.lang.Short.parseShort;
import static java.net.URI.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.impl.AbstractBlobStoreConnection;
import org.akubraproject.impl.StreamManager;
import org.slf4j.Logger;

/**
 * @author ajs6f
 * @version 1.0
 */
public class TwoStoreConnection extends AbstractBlobStoreConnection {

    private static final Logger log = getLogger(TwoStoreConnection.class);

    /**
     * {@link org.akubraproject.BlobStore} to which this connection belongs
     */
    private final TwoStore parent;

    /**
     * a {@link org.akubraproject.BlobStoreConnection} for each side of the {@link TwoStore}
     */
    private final BlobStoreConnection right, left;

    /**
     * @param p {@link org.akubraproject.BlobStore} to which this connection belongs
     * @param hints {@link Map} of hints for this connection which will be passed on to each side
     * @throws IOException
     */

    public TwoStoreConnection(final BlobStore p, final Map<String, String> hints) throws IOException {
        super(p);
        this.parent = (TwoStore) p;
        this.left = parent.getLeft().openConnection(null, hints);
        this.right = parent.getRight().openConnection(null, hints);
    }

    /**
     * @param p {@link org.akubraproject.BlobStore} to which this connection belongs
     * @param streamManager
     * @throws IOException
     */
    public TwoStoreConnection(final BlobStore p, final StreamManager streamManager) throws IOException {
        super(p, streamManager);
        this.parent = (TwoStore) p;
        this.left = parent.getLeft().openConnection(null, null);
        this.right = parent.getRight().openConnection(null, null);
    }

    /**
     * @see org.akubraproject.BlobStoreConnection#getBlob(java.net.URI, java.util.Map)
     */
    @Override
    public Blob getBlob(final URI blobId, final Map<String, String> hints) throws IOException {
        log.trace("Entering TwoStoreConnection.getBlob()");
        final String blobIdString = blobId.toString();
        log.trace("Using blobIdString: " + blobIdString);
        final String newBlobIdString = blobIdString.substring(CHUNK + 1);
        log.trace("Using newBlobIdString: " + newBlobIdString);
        final short front = parseShort(blobIdString.substring(0, CHUNK), 2);
        log.trace("Using front: " + front);
        final URI newBlobId = create(newBlobIdString);

        if (front % 2 == 0) {
            log.trace("Exiting TwoStoreConnection.getBlob() to the left");
            return left.getBlob(newBlobId, hints);
        }
        log.trace("Exiting TwoStoreConnection.getBlob() to the right");
        return right.getBlob(newBlobId, hints);
    }

    /**
     * @see org.akubraproject.BlobStoreConnection#listBlobIds(java.lang.String)
     */
    @Override
    public Iterator<URI> listBlobIds(final String filterPrefix) throws IOException {
        return concat(left.listBlobIds(filterPrefix), right.listBlobIds(filterPrefix));
    }

    /**
     * @see org.akubraproject.BlobStoreConnection#sync()
     */
    @Override
    public void sync() throws IOException {
        left.sync();
        right.sync();
    }

    @Override
    public void close() {
        if (!closed) {
            left.close();
            right.close();
            closed = true;
        }
    }

}
