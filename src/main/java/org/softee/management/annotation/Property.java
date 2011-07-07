package org.softee.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for MBean attributes.<p>
 * 
 * May be applied to:
 * <ul>
 * <li>An MBean field</li>
 * <li>An MBean attribute getter: getXxx() or isXxx()</li>
 * <li>An MBean attribute setter: setXxx()</li>
 * </ul>
 * @author morten.hattesen@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Property {
    /**
     * @return the textual description of the operation to be displayed by the management agent
     */
    String value();
}
