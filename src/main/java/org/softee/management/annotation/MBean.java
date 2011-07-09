package org.softee.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author morten.hattesen@gmail.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MBean {
    /**
     * @return the textual description of the MBean to be displayed by the management agent
     */
    String value() default "";

    /**
     * @return the domain part of the ObjectName for MBean server registration
     */
    String domain() default "";

    /**
     * @return the type part of the ObjectName for MBean server registration
     */
    String type() default "";

    /**
     * @return the type part of the ObjectName for MBean server registration
     */
    String name() default "";
}
