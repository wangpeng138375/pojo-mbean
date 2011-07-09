package org.softee.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
@Inherited
public @interface ManagedAttribute {
    public enum Access {READ(true, false), WRITE(false, true), READ_WRITE(true, true);
        public final boolean canRead;
        public final boolean canWrite;
        private Access(boolean canRead, boolean canWrite) {
            this.canRead = canRead;
            this.canWrite = canWrite;
        }
    }

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
