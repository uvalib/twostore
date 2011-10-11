package edu.virginia.lib.ole.akubra;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Files;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestTwoStore {
	
	private String teststring = "If they can get you asking the wrong questions, they don’t have to worry about answers.";
	
	private Logger log = LoggerFactory.getLogger(TestTwoStore.class);

	private BlobStore right,left;
	private TwoStore teststore;
	private IdMapper bitstringmapper = new BitStringMapper();
	private IdMapper filemapper = new FilePrefixMapper();
	
	@BeforeClass
	public void setUp() throws URISyntaxException, IOException {
		File rightfile = Files.createTempDir();
		BlobStore rightfilestore = new FSBlobStore(new URI("rightfile"), rightfile);
		log.debug("Created temporary right-hand BlobStore in: " + rightfile.getAbsolutePath());
		File leftfile = Files.createTempDir();
		BlobStore leftfilestore = new FSBlobStore(new URI("leftfile"), leftfile);
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
	public void testOpenConnection() throws URISyntaxException, UnsupportedOperationException, IOException {
		BlobStoreConnection testconnection = teststore.openConnection(null, null);
		assertEquals(testconnection.getBlobStore(),teststore);
	}
	
	@Test(dependsOnGroups = { "init" })
	public void testStoreBlob() throws UnsupportedOperationException, IOException, URISyntaxException {
		URI testuri = new URI("testURI");
		log.debug("Test URI: " + testuri.toString());
		URI testencuri = bitstringmapper.getInternalId(testuri);
		log.debug("Test encoded URI: "+ testencuri);
		InputStream testdata = new ByteArrayInputStream(teststring.getBytes("UTF-8"));
		BlobStoreConnection testconnection = teststore.openConnection(null, null);
		Blob testblob = testconnection.getBlob(testencuri, null);
		log.debug("Test blob created with internal URI: " + testblob.getId().toString());
		OutputStream out  = testblob.openOutputStream(0, true);
		IOUtils.copy(testdata, out);
		log.debug("Stored '" + teststring + "' in that Blob.");
		testconnection.close();
		log.debug("Closed test connection.");
		BlobStoreConnection newtestconnection = teststore.openConnection(null, null);
		log.debug("Opened new test connection.");
		Blob newtestblob = newtestconnection.getBlob(testencuri, null);
		log.debug("New test blob retrieved with internal URI: " + newtestblob.getId().toString());
		String retrievedvalue = IOUtils.toString(newtestblob.openInputStream(), "UTF-8");
		log.debug("Retrieved value: " + retrievedvalue);
		assertEquals(teststring,retrievedvalue);
	}
	
}
