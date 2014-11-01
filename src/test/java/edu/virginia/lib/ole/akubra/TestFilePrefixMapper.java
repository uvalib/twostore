
package edu.virginia.lib.ole.akubra;

import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;

import org.junit.Test;
import org.slf4j.Logger;

public class TestFilePrefixMapper extends AbstractIdMapperTestFrame<FilePrefixMapper> {

    private final Logger log = getLogger(TestFilePrefixMapper.class);

    private final FilePrefixMapper mapper = new FilePrefixMapper();

    private final String prefix = mapper.getInternalPrefix("");

    @Override
    @Test
    public void testGetInternalPrefix() {
        assertEquals("file:", prefix);
    }

    @Test
    public void testGetInternalId() {
        final URI dummyURI = create("http://www.example.com");
        log.debug("dummyURI: {}", dummyURI);
        log.debug("mapper.getInternalId(dummyURI): {}", mapper.getInternalId(dummyURI));
        log.debug("prefix + dummyURI.toString(): {}", prefix + dummyURI);
        assertEquals(prefix + dummyURI, mapper.getInternalId(dummyURI).toString());
    }

    @Test
    public void testGetExternalId() {
        final URI dummyURI = create("file://www.example.com");
        log.debug("dummyURI: {}", dummyURI);
        assertEquals(dummyURI.toString().substring(prefix.length()), mapper.getExternalId(dummyURI).toString());
        log.debug("mapper.getExternalId(dummyURI): {}", mapper.getExternalId(dummyURI));
        log.debug("http://www.example.com - prefix: {}", dummyURI.toString().substring(prefix.length()));
    }

    @Override
    protected FilePrefixMapper getTestMapper() {
        return mapper;
    }

}
