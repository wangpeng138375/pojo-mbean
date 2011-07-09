package org.softee.management.helper;

import static java.lang.String.format;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.annotation.ManagedOperation.Impact;
import org.softee.management.annotation.Parameter;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedAttribute.Access;
import org.softee.management.exception.ManagementException;
/**
 * A DynamicMBean that can introspect an annotated POJO bean and expose it as a DynamicMBean
 *
 * @author morten.hattesen@gmail.com
 *
 */
public class IntrospectedDynamicMBean implements DynamicMBean {
    private final Object mbean;
    private final Class<?> beanClass;
    private final Map<String, PropertyDescriptor> propertyDescriptors;
    private final Map<String, Method> operationMethods;
    private final MBeanInfo mbeanInfo;

    /** Constructs a Dynamic MBean by introspecting an annotated POJO MBean {@code annotatedMBean}
     * @param mbean the POJO MBean that should be exposed as a {@link javax.management.DynamicMBean}
     * @throws ManagementException if an exception occurs during the introspection of {@code mbean}
     */
    public IntrospectedDynamicMBean(Object mbean) throws ManagementException {
        this.mbean = mbean;
        this.beanClass = mbean.getClass();
        if (!beanClass.isAnnotationPresent(MBean.class)) {
            throw new IllegalArgumentException(
                    format("MBean %s is not annotated with @%s", beanClass, MBean.class.getName()));
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            propertyDescriptors = createPropertyDescriptors(beanInfo);
            operationMethods = createOperationMethods(beanInfo);
            mbeanInfo = createMbeanInfo();
        } catch (IntrospectionException e) {
            throw new ManagementException(e);
        } catch (java.beans.IntrospectionException e) {
            throw new ManagementException(e);
        }
    }


    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        PropertyDescriptor propertyDescriptor = propertyDescriptors.get(attribute);
        if (propertyDescriptor == null) {
            throw new AttributeNotFoundException(attribute);
        }
        Method getter = propertyDescriptor.getReadMethod();
        if (getter == null) {
            throw new AttributeNotFoundException(
                    format("Getter method for attribute %s of %s", attribute, beanClass));
        }
        try {
            return getter.invoke(mbean);
        } catch (Exception e) {
            throw new RuntimeException(
                    format("Unable to obtain value of attribute %s of %s", attribute, beanClass));
        }
    }

    @Override
    public AttributeList getAttributes(String[] attributeNames) {
        AttributeList attributes = new AttributeList(attributeNames.length);
        for (String attributeName : attributeNames) {
            try {
                Attribute attribute = new Attribute(attributeName, getAttribute(attributeName));
                attributes.add(attribute);
            } catch (Exception e) {
                // Must be a mistake that the signature doesn't allow throwing exceptions
                throw new IllegalArgumentException(e);
            }
        }
        return attributes;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
    InvalidAttributeValueException, MBeanException, ReflectionException {
        String name = attribute.getName();
        Object value = attribute.getValue();
        PropertyDescriptor propertyDescriptor = propertyDescriptors.get(name);
        if (propertyDescriptor == null) {
            throw new AttributeNotFoundException(name);
        }
        Method setter = propertyDescriptor.getWriteMethod();
        if (setter == null) {
            throw new AttributeNotFoundException(format("setter method for attribute %s of %s", name, beanClass));
        }
        try {
            setter.invoke(mbean, value);
        } catch (IllegalArgumentException e) {
            throw new InvalidAttributeValueException(String.format("attribute %s, value = (%s)%s, expected (%s)",
                    name, value.getClass().getName(), value, setter.getParameterTypes()[0].getName()));
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e, format("attribute %s of %s, value = (%s)%s",
                    name, beanClass, value.getClass().getName(), value));
        } catch (InvocationTargetException e) {
            throw new MBeanException(e, format("attribute %s of %s, value = (%s)%s",
                    name, beanClass, value.getClass().getName(), value));
        }
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        for (Object object : attributes) {
            Attribute attribute = (Attribute) object;
            try {
                setAttribute(attribute);
            } catch (Exception e) {
                // Must be a mistake that the signature doesn't allow throwing exceptions
                throw new IllegalArgumentException(e);
            }
        }
        // It seems like an API mistake that we have to return the attributes
        return attributes;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        Method method = operationMethods.get(actionName);
        //TODO verify that the right signature is picked
        try {
            return method.invoke(mbean, params);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

    }

    /**
     * @param mbean the annotated POJO MBean
     * @return an MBeanInfo created by introspecting the {@code mbean}
     * @throws IntrospectionException
     * @throws javax.management.IntrospectionException
     * @throws javax.management.IntrospectionException
     * @throws ManagementException
     */
    private MBeanInfo createMbeanInfo() throws IntrospectionException, javax.management.IntrospectionException, ManagementException {
        MBean annotation = mbean.getClass().getAnnotation(MBean.class);
        final String description = annotation.value();
        final MBeanAttributeInfo[] attributeInfo = createAttributeInfo();
        final MBeanConstructorInfo[] constructorInfo = constructorInfo();
        final MBeanOperationInfo[] operationInfo = createOperationInfo();
        final MBeanNotificationInfo[] notificationInfo = createNotificationInfo();
        return new MBeanInfo(
                mbean.getClass().getName(),
                description,
                attributeInfo,
                constructorInfo,
                operationInfo,
                notificationInfo);
    }

    /**
     * TODO should this be implemented?
     * @return null
     */
    private MBeanNotificationInfo[] createNotificationInfo() {
        return null;
    }

    /**
     * TODO: Consider allowing multiple matches for each (overloaded) method name
     *
     * @return The methods that constitute the operations
     * @throws ManagementException if multiple Operation annotations exist on identically named (overloaded) methods
     */
    private Map<String, Method> createOperationMethods(BeanInfo beanInfo) throws ManagementException {
        Map<String, Method> operationMethods = new HashMap<String, Method>();
        for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors()) {
            Method method = descriptor.getMethod();
            ManagedOperation annotation = method.getAnnotation(ManagedOperation.class);
            if (annotation != null) {
                // This method is an operation
                Method old = operationMethods.put(method.getName(), method);
                if (old != null) {
                    throw new ManagementException(format("Multiple Operation annotations for operation %s of %s",
                            method.getName(), beanClass));
                }
            }
        }
        return operationMethods;
    }

    /**
     * @return an MBeanOPerationInfo array that describes the @Operation annotated methods of the operationMethods
     * @throws ManagementException
     */
    private MBeanOperationInfo[] createOperationInfo() throws ManagementException {
        MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[operationMethods.size()];
        int operationIndex = 0;
        // Iterate in method name order
        for (String methodName : sortedKeys(operationMethods)) {
            Method method = operationMethods.get(methodName);
            ManagedOperation annotation = method.getAnnotation(ManagedOperation.class);
            // add description and names to parameters
            MBeanParameterInfo[] signature = createParameterInfo(method);
            // add description and parameter info to operation method
            Impact impact = (annotation.impact() != null) ? annotation.impact() : Impact.UNKNOWN;
            int impactValue = impact.impactValue;
            MBeanOperationInfo opInfo = new MBeanOperationInfo(
                    method.getName(),
                    annotation.value(),
                    signature,
                    method.getReturnType().getName(),
                    impactValue,
                    null);
            operationInfos[operationIndex++] = opInfo;
        }
        return operationInfos;
    }


    protected MBeanParameterInfo[] createParameterInfo(Method method) {
        MBeanParameterInfo[] parameters = new MBeanParameterInfo[method.getParameterTypes().length];
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            final String pType = method.getParameterTypes()[parameterIndex].getName();
            final String pName;
            final String pDesc;
            // locate parameter annotation
            Parameter annotation = getParameterAnnotation(method, parameterIndex, Parameter.class);
            if (annotation != null) {
                // a parameter annotation exists
                pName = annotation.name();
                pDesc = annotation.value();
            } else {
                pName = "p" + (parameterIndex + 1); // 1 .. n
                pDesc = "";
            }
            parameters[parameterIndex] = new MBeanParameterInfo(pName, pType, pDesc);
        }
        return parameters;
    }

    /**
     * Find an annotation for a parameter on a method.
     *
     * @param <A> The annotation.
     * @param method The method.
     * @param index The index (0 .. n-1) of the parameter in the parameters list
     * @param annotationClass The annotation class
     * @return The annotation, or null
     */
    private static <A extends Annotation> A getParameterAnnotation(Method method,
            int index, Class<A> annotationClass) {
        for (Annotation a : method.getParameterAnnotations()[index]) {
            if (annotationClass.isInstance(a))
                return annotationClass.cast(a);
        }
        return null;
    }

    /**
     * TODO should this be implemented?
     * @return null
     */
    private MBeanConstructorInfo[] constructorInfo() {
        return null;
    }

    /**
     * @return all properties where field, getter or setter is annotated with {@link org.softee.management.annotation.ManagedAttribute}
     * @throws ManagementException
     */
    private Map<String, PropertyDescriptor> createPropertyDescriptors(BeanInfo beanInfo) throws ManagementException {
        Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor property: beanInfo.getPropertyDescriptors()) {
            String name = property.getName();
            Field field = getField(beanClass, name);
            ManagedAttribute annotation = getSingleAnnotation(property, ManagedAttribute.class,
                        field, property.getReadMethod(), property.getWriteMethod());
            /* this time around, we'll use the presence of a Property annotation on either field, setter or getter method as a
             * signal to add the property to the Property annotated properties for future quick lookup
             */
            if (annotation != null) {
                properties.put(name, property);
            }
        }
        return properties;
    }

    private MBeanAttributeInfo[] createAttributeInfo() throws ManagementException, IntrospectionException {
        MBeanAttributeInfo[] infos = new MBeanAttributeInfo[propertyDescriptors.size()];
        int i = 0;
        // iterate over properties sorted by name
        for (String propertyName : sortedKeys(propertyDescriptors)) {
            PropertyDescriptor property = propertyDescriptors.get(propertyName);
            String name = property.getName();
            Field field = getField(beanClass, name);
            Method readMethod = property.getReadMethod();
            Method writeMethod = property.getWriteMethod();
            ManagedAttribute annotation = getSingleAnnotation(property, ManagedAttribute.class, field, readMethod, writeMethod);
            Access access = annotation.access();
            if (access.canRead && readMethod == null) {
                boolean isBoolean = field != null && Boolean.TYPE == field.getType();
                String isErText = isBoolean ? " or is" + initialCapital(name) + "()" : "";
                throw new ManagementException(
                        format("%s access specified for property %s of %s, but no get%s()%s method found",
                                access, name, beanClass, initialCapital(name), isErText));
            }
            if (access.canWrite && writeMethod == null) {
                throw new ManagementException(
                        format("%s access specified for property %s of %s, but no set%s() method found",
                                access, name, beanClass, initialCapital(name)));
            }
            // now restrict the methods exposed to the MBean to those allowed by the annotation
            Method declaredReadMethod = access.canRead ? readMethod : null;
            Method declaredWriteMethod = access.canWrite ? writeMethod : null;
            MBeanAttributeInfo info = new MBeanAttributeInfo(
                    property.getName(),
                    annotation.value(),
                    declaredReadMethod,
                    declaredWriteMethod);
            infos[i++] = info;
        }
        return infos;
    }

    /**
     * Locates a field on a class. The Class.getField(name) method does not return non-private fields,
     * so the only way to locate fields is to look for fields up the inheritance hierarchy
     * @param beanClass The class on which to locate the bean
     * @param name the name of the bean
     * @return field of beanClass named name, or null if such a field does not exist
     * @throws ManagementException
     */
    public Field getField(Class<?> beanClass, String name)
            throws ManagementException {
        Class<?> clazz = beanClass;
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (SecurityException e) {
                throw new ManagementException(format("Unable to access field %s of %s", name, clazz));
            } catch (NoSuchFieldException ignore) {
                // ignore
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * @param map
     * @return a list of the keys in map, sorted
     */
    private List<String> sortedKeys(Map<String, ?> map) {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        return keys;
    }

    /**
     *
     * @param <T>
     * @param property The property to which entities belong
     * @param annotationClass Annotation type
     * @param entities A number of {@code Method}'s or {@code null}'s
     * @return The one (and only) annotation of type {@code annotationClass} that appears on {@code methods},
     * or null if none of the entities are annotated with annotationClass
     * @throws ManagementException if more than one of the entities are annotated with annotationClass
     */
    private <T extends Annotation> T getSingleAnnotation(PropertyDescriptor property, Class<T> annotationClass,
            AccessibleObject... entities) throws ManagementException {
        T result = null;
        for (AccessibleObject entity : entities) {
            if (entity != null) {
                T annotation = entity.getAnnotation(annotationClass);
                if (annotation != null) {
                    if (result != null) {
                        throw new ManagementException(
                                String.format("Multiple %s annotations found for property %s of %s",
                                        annotationClass.getName(), property.getName(), beanClass));
                    }
                    result = annotation;
                }
            }
        }
        return result;
    }

    private String initialCapital(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

}
