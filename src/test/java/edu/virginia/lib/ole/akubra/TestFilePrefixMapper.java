package edu.virginia.lib.ole.akubra;

import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.akubraproject.map.IdMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TestFilePrefixMapper {

	private Logger log = LoggerFactory.getLogger(TestFilePrefixMapper.class);
	
	private final IdMapper mapper = new FilePrefixMapper();
	private final String prefix = mapper.getInternalPrefix("");
	
	@Test(groups = { "init" })
	public void testGetInternalPrefix() {
		assertEquals(prefix,"file:");
	}
	
	@Test(dependsOnGroups = { "init" })
	public void testGetInternalId() throws URISyntaxException {
		URI dummyURI = null;
		dummyURI = new URI("http://www.example.com");
		log.debug("dummyURI: " + dummyURI);
		log.debug("mapper.getInternalId(dummyURI): " + mapper.getInternalId(dummyURI));
		log.debug("prefix + dummyURI.toString(): " + prefix + dummyURI.toString());
		assertEquals(mapper.getInternalId(dummyURI).toString(), prefix + dummyURI.toString());	
	}
	
	@Test(dependsOnGroups = { "init" })
	public void testGetExternalId() throws URISyntaxException {
		URI dummyURI = null;
		dummyURI = new URI("file://www.example.com");	
		log.debug("dummyURI: " + dummyURI);
		assertEquals(mapper.getExternalId(dummyURI).toString(), dummyURI.toString().substring(prefix.length()));
		log.debug("mapper.getExternalId(dummyURI): " + mapper.getExternalId(dummyURI));
		log.debug("http://www.example.com - prefix: " + dummyURI.toString().substring(prefix.length()));
	}
	
}
