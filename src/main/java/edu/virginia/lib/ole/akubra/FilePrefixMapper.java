package edu.virginia.lib.ole.akubra;

import java.net.URI;

import org.akubraproject.map.IdMapper;

/**
 * @author ajs6f
 * @version 1.0
 * @see org.akubraproject.map.IdMapper
 */
public class FilePrefixMapper implements IdMapper {
	
	private final static String prefix = "file:";

	/**
	 * @param internalId internal ID of a blob in the mapped {@link org.akubraproject.BlobStore}
	 * @return {@link URI} external ID for this blob in the mapped BlobStore
	 * @see org.akubraproject.map.IdMapper#getExternalId(java.net.URI)
	 */
	@Override
	public URI getExternalId(URI internalId) throws NullPointerException {
		return URI.create(internalId.toString().substring(getInternalPrefix("").length()));
	}
	
	/**
	 * @param externalId external ID of a blob in the mapped {@link org.akubraproject.BlobStore}
	 * @return {@link URI} internal ID for this blob in the mapped BlobStore
	 * @see org.akubraproject.map.IdMapper#getInternalId(java.net.URI)
	 */
	@Override
	public URI getInternalId(URI externalId) throws NullPointerException {
		return URI.create(getInternalPrefix("") + externalId.toString());
	}
	
	/** 
	 * @param externalPrefix ignored
	 * @return {@link String} The URI prefix in use by this mapper, normally "file:"
	 * @see org.akubraproject.map.IdMapper#getInternalPrefix(java.lang.String)
	 */
	@Override
	public String getInternalPrefix(String externalPrefix)
			throws NullPointerException {
		return prefix;
	}

}
