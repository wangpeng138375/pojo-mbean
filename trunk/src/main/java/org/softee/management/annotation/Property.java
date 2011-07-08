package org.softee.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.softee.management.annotation.Property.Access;

/**
 * Annotation for an MBean attributes.<p>
 *
 * May be applied to one (and only one) of:
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
    public enum Access {READ, WRITE, READ_WRITE}

    /**
     * @return the textual description of the operation to be displayed by the management agent
     */
    String value() default "";

    /**
     * @return The access level of the Attribute (Property). Default access is {@link Access.READ}, regardless of
     * whether a setter method may exist.
     */
    Access access() default Access.READ;

}
