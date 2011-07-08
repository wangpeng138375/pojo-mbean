package org.softee.management.helper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
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
import org.softee.management.annotation.Operation;
import org.softee.management.annotation.Operation.Impact;
import org.softee.management.annotation.Parameter;
import org.softee.management.annotation.Property;
/**
 * A DynamicMBean that can introspect an annotated POJO bean and expose it as a DynamicMBean
 *  
 * @author morten.hattesen@gmail.com
 *
 */
public class IntrospectedDynamicMBean implements DynamicMBean {
    private final Object mbean;
    private final Class<?> mbeanType;
    private final MBeanInfo mbeanInfo;
    private final BeanInfo beanInfo;
    private final Map<String, PropertyDescriptor> propertyDescriptors;
    private final Map<String, Method> operationMethods;

    /** Constructs a Dynamic MBean by introspecting an annotated POJO MBean {@code annotatedMBean}
     * @param mbean the POJO MBean that should be exposed as a {@link javax.management.DynamicMBean}
     * @throws IntrospectionException
     * @throws javax.management.IntrospectionException
     */
    public IntrospectedDynamicMBean(Object mbean) throws javax.management.IntrospectionException, IntrospectionException {
        this.mbean = mbean;
        this.mbeanType = mbean.getClass();
        if (!mbeanType.isAnnotationPresent(MBean.class)) {
            throw new IllegalArgumentException(
                    String.format("mbean type %s is not annotated with %s", mbean.getClass(), MBean.class));
        }
        beanInfo = Introspector.getBeanInfo(mbeanType);
        propertyDescriptors = createPropertyDescriptors();
        operationMethods = createOperationMethods();
        mbeanInfo = createMbeanInfo();
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
            throw new AttributeNotFoundException("getter method for attribute " + attribute);
        }
        try {
            return getter.invoke(mbean);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Unable to obtain value of attribute %s in bean %s", attribute, mbean));
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
            throw new AttributeNotFoundException("setter method for attribute " + name);
        }
        try {
            setter.invoke(mbean, value);
        } catch (IllegalArgumentException e) {
            throw new InvalidAttributeValueException(String.format("attribute %s, value = (%s)%s, expected (%s)",
                    name, value.getClass().getName(), value, setter.getParameterTypes()[0].getName()));
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e, String.format("attribute %s, value = (%s)%s", name, value.getClass().getName(), value));
        } catch (InvocationTargetException e) {
            throw new MBeanException(e, String.format("attribute %s, value = (%s)%s", name, value.getClass().getName(), value));
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
        // TODO how come we have to return the values?
        return attributes;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        //TODO check that the right signature is picked
        Method method = operationMethods.get(actionName);
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
     */
    private MBeanInfo createMbeanInfo() throws javax.management.IntrospectionException, IntrospectionException {
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


    private MBeanNotificationInfo[] createNotificationInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * TODO: Allow multiple matches for each (overloaded) method name
     * 
     * @return The methods that constitute the operations
     * @throws IllegalStateException if multiple Operation annotations exist on identically named (overloaded) methods
     */
    private Map<String, Method> createOperationMethods() {
        Map<String, Method> operationMethods = new HashMap<String, Method>();
        for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors()) {
            Method method = descriptor.getMethod();
            Operation annotation = getAnnotation(Operation.class, method);
            if (annotation != null) {
                // This method is an operation
                Method old = operationMethods.put(method.getName(), method);
                if (old != null) {
                    throw new IllegalStateException("Multiple Operation annotations for operation " + method.getName());
                }
            }
        }
        return operationMethods;
    }

    private MBeanOperationInfo[] createOperationInfo() {
        MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[operationMethods.size()];
        int operationIndex = 0;
        // Iterate in method name order
        for (String methodName : sortedKeys(operationMethods)) {
            Method method = operationMethods.get(methodName);
            Operation annotation = getAnnotation(Operation.class, method);
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
                pDesc = annotation.description();
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
            int index,
            Class<A> annotationClass) {
        for (Annotation a : method.getParameterAnnotations()[index]) {
            if (annotationClass.isInstance(a))
                return annotationClass.cast(a);
        }
        return null;
    }

    private MBeanConstructorInfo[] constructorInfo() {
        // TODO Implement
        return null;
    }

    /**
     * @return all properties where getter or setter is annotated with {@link org.softee.management.annotation.Property}
     */
    private Map<String, PropertyDescriptor> createPropertyDescriptors() {
        Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor property: beanInfo.getPropertyDescriptors()) {
            Property attribute = getAnnotation(Property.class, 
                        property.getReadMethod(), property.getWriteMethod());
            if (attribute != null) {
                properties.put(property.getName(), property);
            }
        }
        return properties;
    }

    private MBeanAttributeInfo[] createAttributeInfo() throws IntrospectionException, javax.management.IntrospectionException {
        MBeanAttributeInfo[] infos = new MBeanAttributeInfo[propertyDescriptors.size()];
        int i = 0;
        // we should iterate over properties sorted by name
        for (String propertyName : sortedKeys(propertyDescriptors)) {
            PropertyDescriptor property = propertyDescriptors.get(propertyName);
            Method readMethod = property.getReadMethod();
            if (readMethod != null && readMethod.getParameterTypes().length != 0) {
                throw new IntrospectionException(
                        String.format("Getter method %s of class %s has > 0 parameters (does not follow beanspec)",
                                readMethod.getName(), readMethod.getDeclaringClass()));
            }
            Method writeMethod = property.getWriteMethod();
            if (writeMethod != null && writeMethod.getParameterTypes().length != 1) {
                throw new IntrospectionException(
                        String.format("Setter method %s of class %s has != 1 parameters (does not follow beanspec)",
                                writeMethod.getName(), writeMethod.getDeclaringClass()));
            }
            Property attribute = getAnnotation(Property.class, readMethod, writeMethod);
            MBeanAttributeInfo info = new MBeanAttributeInfo(
                    property.getName(),
                    attribute.value(),
                    readMethod,
                    writeMethod);
            infos[i++] = info;
        }
        return infos;
    }

    private List<String> sortedKeys(Map<String, ?> map) {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        return keys;
    }
    /**
     *
     * @param <T>
     * @param annotationClass Annotation type
     * @param entities A number of {@code Method}'s or {@code null}'s
     * @return The first annotation of type {@code annotationClass} that appears on {@code methods}, or null
     */
    private <T extends Annotation> T getAnnotation(Class<T> annotationClass, AccessibleObject... entities) {
        for (AccessibleObject entity : entities) {
            if (entity != null) {
                T annotation = entity.getAnnotation(annotationClass);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }


}
