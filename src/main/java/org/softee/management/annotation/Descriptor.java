package org.softee.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Support for a subset of the fields described by {@link javax.management.Descriptor}<p>
 *
 * Supported fields (and context) in order of importance:
 * <ul>
 * <li>units (ManagedAttribute, ManagedOperation, Parameter)</li>
 * <li>defaultValue (ManagedAttribute, Parameter)</li>
 * <li>minValue (ManagedAttribute, Parameter)</li>
 * <li>maxValue (ManagedAttribute, Parameter)</li>
 * <li>legalValues (ManagedAttribute, ManagedOperation)</li>
 * <li>since (ManagedAttribute, ManagedOperation, Parameter, MBean)</li>
 * <li>metricType (ManagedAttribute, ManagedOperation)</li>
 * <li>enabled (ManagedAttribute, ManagedOperation)</li>
 * <li>interfaceClassName (MBean) - automatically set by the introspection</li>
 * <li>mxbean (MBean) - set to false by the introspection</li>
 * <li>deprecated (MBean, ManagedAttribute, ManagedOperation) - automatically set by the introspection according to
 * the {@link Deprecated} annotation on the class or method in question</li>
 * </ul>
 *
 * @author morten.hattesen@gmail.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Inherited
public @interface Descriptor {
    public enum MetricType {UNKNOWN, COUNTER, GAUGE;
        /**
         * MetricType values are lower case
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    };

    public static final String NONE = "";

    String units() default NONE;

    String defaultValue() default NONE;

    String minValue() default NONE;

    String[] legalValues() default NONE;

    String maxValue() default NONE;

    String since() default NONE;

    MetricType metricType() default MetricType.UNKNOWN;

    boolean enabled() default true;
}
