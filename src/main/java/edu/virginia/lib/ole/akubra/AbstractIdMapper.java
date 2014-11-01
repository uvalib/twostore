/**
 * 
 */

package edu.virginia.lib.ole.akubra;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import org.akubraproject.map.IdMapper;

import com.google.common.base.Converter;

/**
 * @author ajs6f
 */
public abstract class AbstractIdMapper extends Converter<URI, URI> implements IdMapper {

    /*
     * (non-Javadoc)
     * @see org.akubraproject.map.IdMapper#getExternalId(java.net.URI)
     */
    @Override
    public URI getExternalId(final URI internalId) throws NullPointerException {
        checkNotNull(internalId, "External ID may not be null!");
        return reverse().convert(internalId);
    }

    /*
     * (non-Javadoc)
     * @see org.akubraproject.map.IdMapper#getInternalId(java.net.URI)
     */
    @Override
    public URI getInternalId(final URI externalId) {
        checkNotNull(externalId, "Internal ID may not be null!");
        return convert(externalId);
    }

    /*
     * (non-Javadoc)
     * @see org.akubraproject.map.IdMapper#getInternalPrefix(java.lang.String)
     */
    @Override
    public String getInternalPrefix(final String externalPrefix) {
        return null;
    }
}
