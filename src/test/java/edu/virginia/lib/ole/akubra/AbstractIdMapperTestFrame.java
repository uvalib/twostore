
package edu.virginia.lib.ole.akubra;

import static org.junit.Assert.assertEquals;

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

}
