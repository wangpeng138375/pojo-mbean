package org.softee.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for MBean operation methods.<p>
 *
 * @author morten.hattesen@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface ManagedOperation {
    /**
     * The impact of this operation
     */
    public enum Impact {INFO(0), ACTION(1), ACTION_INFO(2), UNKNOWN(3);
        public final int impactValue;
        private Impact(int impactValue) {
            this.impactValue = impactValue;
        }
    }

    /**
     * @return the textual description of the attribute to be displayed by the management agent
     */
    String value() default "";

    /**
     * @resturn The impact of this operation
     */
    Impact impact() default Impact.UNKNOWN;
}