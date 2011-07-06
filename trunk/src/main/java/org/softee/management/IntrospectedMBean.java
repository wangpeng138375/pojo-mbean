package org.softee.management;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

public class IntrospectedMBean implements DynamicMBean {

    /** Constructs a Dynamic MBean by introspecting {@code clazz}
     * @param clazz
     */
    public IntrospectedMBean(Object annotatedMBean) {

    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
        // TODO Auto-generated method stub

    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        // TODO Auto-generated method stub
        return null;
    }

}
