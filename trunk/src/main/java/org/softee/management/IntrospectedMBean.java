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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    public AttributeList getAttributes(String[] attributes) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        // TODO Auto-generated method stub
        return null;
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
        final MBeanConstructorInfo[] constructorInfo = createConstructorInfo();
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


    private MBeanOperationInfo[] createOperationInfo() {
        Set<Method> propertyMethods = propertyMethods();
        List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
        for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors()) {
            Method method = descriptor.getMethod();
            Description description = getAnnotation(Description.class, method);
            if (description != null && !propertyMethods.contains(method)) {
                // This method is Description annotated and not a property/attribute method
                operations.add(new MBeanOperationInfo(description.value(), method));
            }
        }
        return operations.toArray(new MBeanOperationInfo[operations.size()]);
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

    private MBeanConstructorInfo[] createConstructorInfo() {
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
