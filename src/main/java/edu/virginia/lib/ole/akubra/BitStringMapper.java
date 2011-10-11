/**
 * 
 */
package edu.virginia.lib.ole.akubra;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.zip.Adler32;

import org.akubraproject.map.IdMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ajs6f
 * @version 1.0
 * @see org.akubraproject.map.IdMapper
 */
public class BitStringMapper implements IdMapper {

	private static final Logger log = LoggerFactory
			.getLogger(BitStringMapper.class);

	/**
	 * @see org.akubraproject.map.IdMapper#getExternalId(java.net.URI)
	 */
	public URI getExternalId(URI internalId) throws NullPointerException {
		String uriandpath = internalId.toString();
		String uri = "";
		try {
			uri = URLDecoder.decode(
					uriandpath.substring(uriandpath.lastIndexOf('/') + 1),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// useless catch block since UTF-8 is spec'd above
		}
		return URI.create(uri);
	}

	/**
	 * @see org.akubraproject.map.IdMapper#getInternalId(java.net.URI)
	 */
	public URI getInternalId(URI externalId) throws NullPointerException {
		log.debug("Entering BitStringMapper.getInternalId()");
		String characters = "";
		try {
			characters = URLEncoder.encode(externalId.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// useless catch block since UTF-8 is spec'd above
		}
		String hashcharacters = Long.toBinaryString(hash(characters));
		log.debug("Using hashcharacters: " + hashcharacters);
		StringBuffer path = new StringBuffer();
		for (int i = 0; i < hashcharacters.length() - Constants.CHUNK; i = i
				+ Constants.CHUNK) {
			String section = hashcharacters.substring(i, i + Constants.CHUNK)
					+ "/";
			log.debug("Appending: '" + section + "' to path");
			path.append(section);
		}
		String uristring = path.toString() + characters;
		log.debug("Exiting BitStringMapper.getInternalId() with: " + uristring);
		return URI.create(String.valueOf(uristring));
	}

	/**
	 * @param externalPrefix ignored
	 * @return always returns empty string, since there is no defined prefix for this mapper
	 * @see org.akubraproject.map.IdMapper#getInternalPrefix(java.lang.String)
	 */
	public String getInternalPrefix(String externalPrefix)
			throws NullPointerException {
		return "";
	}
	
	/**
	 * @param {@link String} characters
	 * @return {@link Long} Adler32 hash of input
	 * @see Adler32
	 */
	private Long hash(String characters) {
		Adler32 hasher = new Adler32();
		hasher.reset();
		hasher.update(characters.getBytes());
		return hasher.getValue();
	}

}
