/**
 * 
 */
package edu.virginia.lib.ole.akubra;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.UnsupportedIdException;
import org.akubraproject.impl.AbstractBlobStoreConnection;
import org.akubraproject.impl.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

/**
 * @author ajs6f
 * @version 1.0 
 */
public class TwoStoreConnection extends AbstractBlobStoreConnection {

	private static final Logger log = LoggerFactory
			.getLogger(TwoStoreConnection.class);

	/**
	 * {@link org.akubraproject.BlobStore} to which this connection belongs
	 */
	private TwoStore myowner;
	
	/**
	 * a {@link org.akubraproject.BlobStoreConnection} for each side of the {@link TwoStore}
	 */
	private BlobStoreConnection right, left;

	/**
	 * @param owner {@link org.akubraproject.BlobStore} to which this connection belongs
	 * @param hints {@link Map} of hints for this connection which will be passed on to each side
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */

	public TwoStoreConnection(BlobStore owner, Map<String, String> hints)
			throws UnsupportedOperationException, IOException {
		super(owner);
		this.myowner = (TwoStore) owner;
		this.left = myowner.getLeft().openConnection(null, hints);
		this.right = myowner.getRight().openConnection(null, hints);
	}

	/**
	 * @param owner {@link org.akubraproject.BlobStore} to which this connection belongs
	 * @param streamManager
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public TwoStoreConnection(BlobStore owner, StreamManager streamManager)
			throws UnsupportedOperationException, IOException {
		super(owner, streamManager);
		this.myowner = (TwoStore) owner;
		this.left = myowner.getLeft().openConnection(null, null);
		this.right = myowner.getRight().openConnection(null, null);
	}

	/** 
	 * @see org.akubraproject.BlobStoreConnection#getBlob(java.net.URI, java.util.Map)
	 */
	public Blob getBlob(URI blobId, Map<String, String> hints)
			throws IOException, UnsupportedIdException,
			UnsupportedOperationException {
		log.debug("Entering TwoStoreConnection.getBlob()");
		String blobIdString = blobId.toString();
		log.debug("Using blobIdString: " +blobIdString);
		String newBlobIdString = blobIdString.substring(Constants.CHUNK+1);
		log.debug("Using newBlobIdString: " +newBlobIdString);
		Short front = Short.parseShort((blobIdString.substring(0, Constants.CHUNK)),
				2);
		log.debug("Using front: " +front);
		URI newBlobId = URI.create(newBlobIdString);

		if (front % 2 == 0) {
			log.debug("Exiting TwoStoreConnection.getBlob() to the left");
			return left.getBlob(newBlobId, hints);
		} else {
			log.debug("Exiting TwoStoreConnection.getBlob() to the right");
			return right.getBlob(newBlobId, hints);
		}

	}

	/**
	 *  @see org.akubraproject.BlobStoreConnection#listBlobIds(java.lang.String)
	 */
	public Iterator<URI> listBlobIds(String filterPrefix) throws IOException {
		Iterator<URI> leftiter = left.listBlobIds(filterPrefix);
		Iterator<URI> rightiter = right.listBlobIds(filterPrefix);
		return Iterators.concat(leftiter, rightiter);
	}

	/**
	 * @see org.akubraproject.BlobStoreConnection#sync()
	 */
	public void sync() throws IOException, UnsupportedOperationException {
		left.sync();
		right.sync();
	}

	@Override
	public void close() {
		left.close();
		right.close();
		closed = true;
	}

}
