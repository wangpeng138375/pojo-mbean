package org.softee.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.management.Attribute;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import org.junit.Before;
import org.junit.Test;

public class IntrospectedMBeanTest {
    private DummyAnnotatedMbean annotatedMBean;
    private DynamicMBean introspectedMBean;

    @Before
    public void before() throws Exception, java.beans.IntrospectionException {
        annotatedMBean = new DummyAnnotatedMbean();
        introspectedMBean = new IntrospectedMBean(annotatedMBean);
    }

    @Test
    public void testAnnotatedMBean() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetAttribute() throws Exception {
        Object attribute = introspectedMBean.getAttribute("string");
        assertEquals(annotatedMBean.string, attribute);
    }

    @Test
    public void testGetAttributes() throws Exception {
        fail("Not yet implemented");
    }

    @Test(expected=InvalidAttributeValueException.class)
    public void testSetAttributeIllegalType() throws Exception {
        Attribute attribute = new Attribute("integer", "42");
        introspectedMBean.setAttribute(attribute);
    }

    @Test
    public void testSetAttribute() throws Exception {
        int value = 42;
        Attribute attribute = new Attribute("integer", new Integer(value));
        introspectedMBean.setAttribute(attribute);
        assertEquals(value, annotatedMBean.integer);
    }

    @Test
    public void testSetAttributes() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetMBeanInfo() {
        MBeanInfo mBeanInfo = introspectedMBean.getMBeanInfo();
        MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
        assertEquals("Number of attributes", 2, attributes.length);
        assertEquals(annotatedMBean.getClass().getSimpleName(), mBeanInfo.getDescription());
        MBeanOperationInfo[] operations = mBeanInfo.getOperations();
        assertEquals("Number of operations", 1, operations.length);
    }

    @Test
    public void testInvoke() {
        fail("Not yet implemented");
    }

}
