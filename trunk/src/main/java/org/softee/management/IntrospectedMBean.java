package org.softee.management;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import javax.management.ReflectionException;

import org.softee.management.annotation.Description;

public class IntrospectedMBean implements DynamicMBean {
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
    public IntrospectedMBean(Object mbean) throws javax.management.IntrospectionException, IntrospectionException {
        this.mbean = mbean;
        this.mbeanType = mbean.getClass();
        if (!mbeanType.isAnnotationPresent(Description.class)) {
            throw new IllegalArgumentException(
                    String.format("mbean type %s is not annotated with %s", mbean.getClass(), Description.class));
        }
        this.beanInfo = Introspector.getBeanInfo(mbeanType);
        this.propertyDescriptors = propertyDescriptors();
        this.operationMethods = operationMethods();
        mbeanInfo = mbeanInfo();
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
    private MBeanInfo mbeanInfo() throws javax.management.IntrospectionException, IntrospectionException {
        final String description = description();
        final MBeanAttributeInfo[] attributeInfo = attributeInfo();
        final MBeanConstructorInfo[] constructorInfo = constructorInfo();
        final MBeanOperationInfo[] operationInfo = operationInfo();
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
     * FIXME: Allow multiple matches for each name (overloaded)
     * The methods that constitute the operations made available.
     * @return
     */
    private Map<String, Method> operationMethods() {
        Set<Method> propertyMethods = propertyMethods();
        Map<String, Method> operationMethods = new HashMap<String, Method>();
        for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors()) {
            Method method = descriptor.getMethod();
            Description description = getAnnotation(Description.class, method);
            if (description != null && !propertyMethods.contains(method)) {
                // This method is Description annotated and not a property/attribute method
                operationMethods.put(method.getName(), method);
            }
        }
        return operationMethods;
    }

    private MBeanOperationInfo[] operationInfo() {
        MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[operationMethods.size()];
        int i = 0;
        for (Method method : operationMethods.values()) {
            Description description = getAnnotation(Description.class, method);
            operationInfos[i++] = new MBeanOperationInfo(description.value(), method);
        }
        return operationInfos;
    }

    private Set<Method> propertyMethods() {
        Set<Method> methods = new HashSet<Method>();
        for (PropertyDescriptor property : propertyDescriptors.values()) {
            addNotNull(methods, property.getReadMethod());
            addNotNull(methods, property.getWriteMethod());
        }
        return methods;
    }

    private <E> void addNotNull(Collection<E> elements, E element) {
        if (element != null) {
            elements.add(element);
        }
    }

    private MBeanConstructorInfo[] constructorInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return all properties where getter or setter is annotated with {@link Description}
     */
    private Map<String, PropertyDescriptor> propertyDescriptors() {
        Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor property: beanInfo.getPropertyDescriptors()) {
            Description description = getAnnotation(Description.class, property.getReadMethod(), property.getWriteMethod());
            if (description != null) {
                properties.put(property.getName(), property);
            }
        }
        return properties;
    }

    private MBeanAttributeInfo[] attributeInfo() throws IntrospectionException, javax.management.IntrospectionException {
        MBeanAttributeInfo[] infos = new MBeanAttributeInfo[propertyDescriptors.size()];
        int i = 0;
        for (PropertyDescriptor property: propertyDescriptors.values()) {
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
            Description description = getAnnotation(Description.class, readMethod, writeMethod);
            MBeanAttributeInfo info = new MBeanAttributeInfo(
                    property.getName(),
                    description.value(),
                    readMethod,
                    writeMethod);
            infos[i++] = info;
        }
        return infos;
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


    private String description() {
        Description description = mbean.getClass().getAnnotation(Description.class);
        return (description != null) ? description.value() : null;
    }
}
