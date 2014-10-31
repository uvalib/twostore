package edu.virginia.lib.ole.akubra;

import static com.google.common.base.Strings.repeat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class TestTwoStore {
	
	private static final String testURIinfix = "uva-lib:";
	private static final String teststring = "If they can get you asking the wrong questions, they don't have to worry about answers.";
	
	private static final Logger log = getLogger(TestTwoStore.class);

	private BlobStore right,left;
	private TwoStore teststore;
	private final IdMapper bitstringmapper = new BitStringMapper();
	private final IdMapper filemapper = new FilePrefixMapper();
	
	@BeforeClass
	public void setUp() throws URISyntaxException {
		final File rightfile = Files.createTempDir();
		final BlobStore rightfilestore = new FSBlobStore(new URI("rightfile"), rightfile);
		log.debug("Created temporary right-hand BlobStore in: " + rightfile.getAbsolutePath());
		final File leftfile = Files.createTempDir();
		final BlobStore leftfilestore = new FSBlobStore(new URI("leftfile"), leftfile);
		log.debug("Created temporary left-hand BlobStore in: " + leftfile.getAbsolutePath());
		right = new IdMappingBlobStore(new URI("right"), rightfilestore,filemapper);
		log.debug("Created new file-mapped BlobStore to the right.");
		left = new IdMappingBlobStore(new URI("left"), leftfilestore,filemapper);
		log.debug("Created new file-mapped BlobStore to the left.");
	}
	
	@Test(groups = { "init" })
	public void testInstantiateTwoStore() throws URISyntaxException {
		teststore = new TwoStore(new URI("teststore"),left,right);
		assertEquals(teststore.getId().toString(),"teststore");
		assertEquals(teststore.getRight().getId().toString(),"right");
		assertEquals(teststore.getLeft().getId().toString(),"left");
	}
	
	@Test(dependsOnGroups = { "init" })
	public void testOpenConnection() throws UnsupportedOperationException, IOException {
		final BlobStoreConnection testconnection = teststore.openConnection(null, null);
		assertEquals(testconnection.getBlobStore(),teststore);
	}
	
	@Test(dependsOnGroups = { "init" })
	public void testStoreBlobs() throws UnsupportedOperationException, IOException, URISyntaxException {
		String testURI,testdata;
		for (int i = 0 ; i < 101; i++) {
			testURI = ((i % 2 == 0) ? "A" : "B") + testURIinfix + i;
			testdata = i + repeat(testURIinfix, i) + i;
			storeOneBlob(testURI, testdata);	
		}
	}
	
	private void storeOneBlob(final String testuristring, final String teststring) throws UnsupportedOperationException, IOException, URISyntaxException {	
		final URI testuri = new URI(testuristring);
		log.debug("Test URI: " + testuri.toString());
		final URI testencuri = bitstringmapper.getInternalId(testuri);
		log.debug("Test encoded URI: "+ testencuri);
		final InputStream testdata = new ByteArrayInputStream(teststring.getBytes("UTF-8"));
		final BlobStoreConnection testconnection = teststore.openConnection(null, null);
		final Blob testblob = testconnection.getBlob(testencuri, null);
		log.debug("Test blob created with internal URI: " + testblob.getId().toString());
        try (final OutputStream out = testblob.openOutputStream(0, true)) {
            IOUtils.copy(testdata, out);
        }
		log.debug("Stored '" + teststring + "' in that Blob.");
		testconnection.close();
		log.debug("Closed test connection.");
		final BlobStoreConnection newtestconnection = teststore.openConnection(null, null);
		log.debug("Opened new test connection.");
		final Blob newtestblob = newtestconnection.getBlob(testencuri, null);
		log.debug("New test blob retrieved with internal URI: " + newtestblob.getId().toString());
		final String retrievedvalue = IOUtils.toString(newtestblob.openInputStream(), "UTF-8");
		log.debug("Retrieved value: " + retrievedvalue);
		assertEquals(teststring,retrievedvalue);
	}
	
}
