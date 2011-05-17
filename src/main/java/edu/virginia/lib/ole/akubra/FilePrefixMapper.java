package edu.virginia.lib.ole.akubra;

import java.net.URI;

import org.akubraproject.map.IdMapper;

public class FilePrefixMapper implements IdMapper {
	
	private final static String prefix = "file:";

	public URI getExternalId(URI internalId) throws NullPointerException {
		return URI.create(internalId.toString().substring(getInternalPrefix("").length()));
	}

	public URI getInternalId(URI externalId) throws NullPointerException {
		return URI.create(getInternalPrefix("") + externalId.toString());
	}

	public String getInternalPrefix(String externalPrefix)
			throws NullPointerException {
		return prefix;
	}

}
