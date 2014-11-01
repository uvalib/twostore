
package edu.virginia.lib.ole.akubra;

import static java.net.URI.create;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

public abstract class AbstractIdMapperTestFrame<MapperType extends AbstractIdMapper> {

    protected abstract MapperType getTestMapper();

    @Test(expected = NullPointerException.class)
    public void badExternalId() {
        getTestMapper().getExternalId(null);
    }

    @Test(expected = NullPointerException.class)
    public void badInternalId() {
        getTestMapper().getInternalId(null);
    }

    @Test
    public void testGetInternalPrefix() {
        assertEquals(null, getTestMapper().getInternalPrefix(""));
    }

    @Test
    public void testInverseOfInverseIsIdentityForward() {
        assertEquals(getTestMapper(), getTestMapper().reverse().reverse());
        final URI testExternalUri = create(randomUUID().toString());
        assertEquals(testExternalUri, getTestMapper().reverse().convert(getTestMapper().convert(testExternalUri)));
    }

    @Test
    public void testInverseOfInverseIsIdentityBackward() {
        final URI testInternalUri = createTestInternalUri();
        assertEquals(testInternalUri, getTestMapper().convert(getTestMapper().reverse().convert(testInternalUri)));
    }

    protected abstract URI createTestInternalUri();
}
