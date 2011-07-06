package org.softee.management;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for MBean classes, attributes and operations.<p>
 *
 * If applied to a method, it will describe...
 * <ul>
 * <li>An MBean, if applied to a class</li>
 * <li>An MBean attribute, if applied to a JavaBean method (getXxx(), isXxx() or setXxx()</li>
 * <li>An MBean operation, if applied to a non-JavaBean method</li>
 * </ul>
 *
 * @author morten.hattesen@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Description {
    /**
     * @return the textual description of the entity to be displayed by the management agent
     */
    String value();
}
