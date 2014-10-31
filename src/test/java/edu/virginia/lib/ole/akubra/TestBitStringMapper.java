
package edu.virginia.lib.ole.akubra;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.akubraproject.map.IdMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBitStringMapper {

    private final Logger log = LoggerFactory.getLogger(TestBitStringMapper.class);

    private final IdMapper mapper = new BitStringMapper();

    @Test
    public void testGetInternalPrefix() {
        assertEquals(mapper.getInternalPrefix(""), "");
    }

    @Test
    public void testGetInternalId() throws URISyntaxException {
        URI dummyURI = null;
        dummyURI = new URI("http://www.example.com");
        final URI internal = mapper.getInternalId(dummyURI);
        final String testinternal = "10/00/00/10/11/00/01/01/00/00/10/01/10/00/00/http%3A%2F%2Fwww.example.com";
        log.debug("dummyURI: " + dummyURI);
        log.debug("testinternal: " + testinternal);
        log.debug("Internal URI: " + internal.toString());
        assertEquals(internal.toString(), testinternal);
    }

    @Test
    public void testGetExternalId() throws URISyntaxException {
        URI dummyURI = null;
        dummyURI = new URI("10/00/00/10/11/00/01/01/00/00/10/01/10/00/00/http%3A%2F%2Fwww.example.com");
        final URI internal = mapper.getExternalId(dummyURI);
        final String testexternal = "http://www.example.com";
        log.debug("dummyURI: " + dummyURI);
        log.debug("testexternal: " + testexternal);
        log.debug("External URI: " + internal.toString());
        assertEquals(internal.toString(), testexternal);
    }
}
