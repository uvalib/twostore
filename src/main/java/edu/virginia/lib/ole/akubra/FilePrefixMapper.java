
package edu.virginia.lib.ole.akubra;

import static java.net.URI.create;

import java.net.URI;

/**
 * @author ajs6f
 * @version 1.0
 * @see org.akubraproject.map.IdMapper
 */
public class FilePrefixMapper extends AbstractIdMapper {

    private final static String prefix = "file:";

    /**
     * @see org.akubraproject.map.IdMapper#getExternalId(java.net.URI)
     * @see edu.virginia.lib.ole.akubra.AbstractIdMapper.getExternalId(java.net.URI)
     * @see com.google.common.base.Converter#doBackward(java.lang.Object)
     */
    @Override
    public URI doBackward(final URI internalId) {
        return create(internalId.toString().substring(getInternalPrefix("").length()));
    }

    /**
     * @see org.akubraproject.map.IdMapper#getInternalId(java.net.URI)
     * @see edu.virginia.lib.ole.akubra.AbstractIdMapper.getInternalId(java.net.URI)
     * @see com.google.common.base.Converter#doForward(java.lang.Object)
     */
    @Override
    public URI doForward(final URI externalId) {
        return create(getInternalPrefix("") + externalId);
    }

    /**
     * @param externalPrefix ignored
     * @return {@link String} The URI prefix in use by this mapper, normally "file:"
     * @see org.akubraproject.map.IdMapper#getInternalPrefix(java.lang.String)
     */
    @Override
    public String getInternalPrefix(final String externalPrefix) {
        return prefix;
    }

}
