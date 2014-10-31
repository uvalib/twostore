/**
 * 
 */

package edu.virginia.lib.ole.akubra;

import static com.google.common.base.Charsets.UTF_8;
import static edu.virginia.lib.ole.akubra.Constants.CHUNK;
import static java.lang.Long.toBinaryString;
import static java.net.URI.create;
import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.zip.Adler32;

import org.akubraproject.map.IdMapper;
import org.slf4j.Logger;

/**
 * @author ajs6f
 * @version 1.0
 * @see org.akubraproject.map.IdMapper
 */
public class BitStringMapper implements IdMapper {

    private static final Logger log = getLogger(BitStringMapper.class);

    /**
     * @see org.akubraproject.map.IdMapper#getExternalId(java.net.URI)
     */
    @Override
    public URI getExternalId(final URI internalId) {
        final String uriandpath = internalId.toString();
        try {
            final String uri = decode(uriandpath.substring(uriandpath.lastIndexOf('/') + 1), UTF_8.toString());
            return create(uri);
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

    }

    /**
     * @see org.akubraproject.map.IdMapper#getInternalId(java.net.URI)
     */
    @Override
    public URI getInternalId(final URI externalId) {
        log.trace("Entering BitStringMapper.getInternalId()");
        String characters = "";
        try {
            characters = encode(externalId.toString(), UTF_8.toString());
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        final String hashcharacters = toBinaryString(hash(characters));
        log.trace("Using hashcharacters: " + hashcharacters);
        final StringBuffer path = new StringBuffer();
        for (int i = 0; i < hashcharacters.length() - CHUNK; i = i + CHUNK) {
            final String section = hashcharacters.substring(i, i + CHUNK) + "/";
            log.trace("Appending: '" + section + "' to path");
            path.append(section);
        }
        final String uristring = path.toString() + characters;
        log.trace("Exiting BitStringMapper.getInternalId() with: {}", uristring);
        return create(uristring);
    }

    /**
     * @param externalPrefix ignored
     * @return always returns empty string, since there is no defined prefix for this mapper
     * @see org.akubraproject.map.IdMapper#getInternalPrefix(java.lang.String)
     */
    @Override
    public String getInternalPrefix(final String externalPrefix) {
        return "";
    }

    /**
     * @param {@link String} characters
     * @return {@link Long} Adler32 hash of input
     * @see java.util.zip.Adler32.Adler32()
     */
    private static long hash(final String characters) {
        final Adler32 hasher = new Adler32();
        hasher.reset();
        hasher.update(characters.getBytes());
        return hasher.getValue();
    }

}
