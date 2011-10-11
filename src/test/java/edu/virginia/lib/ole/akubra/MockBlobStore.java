package edu.virginia.lib.ole.akubra;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.transaction.Transaction;

import org.akubraproject.BlobStoreConnection;
import org.akubraproject.impl.AbstractBlobStore;

public class MockBlobStore extends AbstractBlobStore {

	public MockBlobStore(URI id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BlobStoreConnection openConnection(Transaction tx,
			Map<String, String> hints) throws UnsupportedOperationException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
