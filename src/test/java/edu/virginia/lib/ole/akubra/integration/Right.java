
package edu.virginia.lib.ole.akubra.integration;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

/**
 * The right-hand side of a test TwoStore.
 * 
 * @author ajs6f
 *
 */
@Qualifier
@Retention(RUNTIME)
public @interface Right {
    // EMPTY
}
