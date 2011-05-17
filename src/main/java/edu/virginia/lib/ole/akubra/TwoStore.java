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
 *
 */
public class TwoStore extends AbstractBlobStore {

	private BlobStore left, right;
	

	/**
	 * @param id
	 */
	public TwoStore(URI id) {
		super(id);
	}
	
	/**
	 * @param id
	 * @param left
	 * @param right
	 */
	public TwoStore(URI id, BlobStore left, BlobStore right) {
		super(id);
		this.left = left;
		this.right = right;
	}
	
	/**
	 * @param id
	 * @param left
	 * @param right
	 */
	public TwoStore(URI id, BlobStore left, BlobStore right, Integer limit) {
		super(id);
		this.left = left;
		this.right = right;
	}

	/* (non-Javadoc)
	 * @see org.akubraproject.BlobStore#openConnection(javax.transaction.Transaction, java.util.Map)
	 */
	public BlobStoreConnection openConnection(Transaction tx,
			Map<String, String> hints) throws UnsupportedOperationException,
			IOException {
		return new TwoStoreConnection(this, hints);
	}

	public BlobStore getLeft() {
		return left;
	}

	public void setLeft(BlobStore left) {
		this.left = left;
	}

	public BlobStore getRight() {
		return right;
	}

	public void setRight(BlobStore right) {
		this.right = right;
	}


}
