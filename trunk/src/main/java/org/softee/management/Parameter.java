package org.softee.management;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds description to a parameter of an MBean operation
 * @author morten.hattesen@gmail.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Parameter {
    /**
     *
     * @return name of the parameter
     */
    String name();

    /**
     *
     * @return description of the parameter
     */
    String value();
}
