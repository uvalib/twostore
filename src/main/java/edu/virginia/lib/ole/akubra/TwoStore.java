/**
 * 
 */

package edu.virginia.lib.ole.akubra;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.transaction.Transaction;

import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.impl.AbstractBlobStore;

/**
 * @author ajs6f
 * @version 1.0
 * @see org.akubraproject.BlobStore
 */
public class TwoStore extends AbstractBlobStore {

    private final BlobStore left, right;

    /**
     * @param id {@link URI} identifier for this {@link org.akubraproject.BlobStore}
     * @param left {@link URI} for the BlobStore on the left side
     * @param right {@link URI} for the BlobStore on the right side
     */
    public TwoStore(final URI id, final BlobStore left, final BlobStore right) {
        super(id);
        this.left = left;
        this.right = right;
    }

    /**
     * @see org.akubraproject.BlobStore#openConnection(javax.transaction.Transaction, java.util.Map)
     */
    @Override
    public BlobStoreConnection openConnection(final Transaction tx, final Map<String, String> hints)
            throws IOException {
        return new TwoStoreConnection(this, hints);
    }

    /**
     * @return {@link org.akubraproject.BlobStore} from the left side
     */
    public BlobStore getLeft() {
        return left;
    }

    /**
     * @return {@link org.akubraproject.BlobStore} from the right side
     */
    public BlobStore getRight() {
        return right;
    }

}
