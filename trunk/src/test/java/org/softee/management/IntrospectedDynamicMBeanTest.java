package org.softee.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import org.junit.Before;
import org.junit.Test;
import org.softee.management.helper.IntrospectedDynamicMBean;

public class IntrospectedDynamicMBeanTest {
    private DummyAnnotatedMbean annotatedMBean;
    private DynamicMBean introspectedMBean;
    private DynamicMBeanValidator validator = new DynamicMBeanValidator();

    @Before
    public void before() throws Exception, java.beans.IntrospectionException {
        annotatedMBean = new DummyAnnotatedMbean();
        introspectedMBean = new IntrospectedDynamicMBean(annotatedMBean);
    }

    @Test
    public void testValidate() {
        validator.validateDynamicMBean(introspectedMBean);
    }
    
    @Test
    public void testgetLoremThrowException() throws Exception {
        try {
            introspectedMBean.getAttribute("loremThrowException");
            fail("getAttribute should throw MBeanException");
        } catch (MBeanException e) {
            assertTrue(e.getCause() instanceof DummyException);
        }
    }
    
    @Test
    public void testGetAttribute() throws Exception {
        Object attribute = introspectedMBean.getAttribute("string");
        assertEquals(annotatedMBean.string, attribute);
    }

    @Test
    public void testGetAttributes() throws Exception {
        AttributeList attributes = introspectedMBean.getAttributes(new String[] {"string"});
        Attribute attribute = (Attribute) attributes.get(0);
        assertEquals(annotatedMBean.string, attribute.getValue());
    }

    @Test(expected = InvalidAttributeValueException.class)
    public void testSetAttributeIllegalType() throws Exception {
        Attribute attribute = new Attribute("integer", "42");
        introspectedMBean.setAttribute(attribute);
    }

    @Test
    public void testSetAttribute() throws Exception {
        int answer = 42;
        Attribute attribute = new Attribute("integer", Integer.valueOf(answer));
        introspectedMBean.setAttribute(attribute);
        assertEquals(answer, annotatedMBean.integer);
    }

    @Test
    public void testSetAttributeThrowDummyException() throws Exception {
        int value = 42;
        Attribute attribute = new Attribute("integerThrowException", Integer.valueOf(value));
        try {
            introspectedMBean.setAttribute(attribute);
            fail("setAttribute should throw MBeanException");
        } catch (MBeanException e) {
            assertTrue(e.getCause() instanceof DummyException);
        }
    }

    
    @Test
    public void testSetAttributes() {
        int answer = 42;
        AttributeList attributes = new AttributeList();
        attributes.add(new Attribute("integer", Integer.valueOf(answer)));
        introspectedMBean.setAttributes(attributes);
        assertEquals(answer, annotatedMBean.integer);
    }

    @Test
    public void testGetMBeanInfo() {
        MBeanInfo mBeanInfo = introspectedMBean.getMBeanInfo();
        MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
        assertEquals("Number of attributes", 5, attributes.length);
        assertEquals(annotatedMBean.getClass().getSimpleName(), mBeanInfo.getDescription());
        MBeanOperationInfo[] operations = mBeanInfo.getOperations();
        assertEquals("Number of operations", 2, operations.length);
    }

    @Test
    public void testInvoke() throws Exception {
        String arg0 = "Lorem Ipsum";
        introspectedMBean.invoke("voidOneArgOperation", new Object[] {arg0}, new String[] {"java.lang.String"});
        assertEquals(arg0, annotatedMBean.operationArgument);
    }

    @Test
    public void testInvokeThrowsDummyException() throws Exception{
        String arg0 = "Lorem Ipsum";
        try {
            introspectedMBean.invoke("voidOneArgOperationException", new Object[] {arg0}, new String[] {"java.lang.String"});
            fail("invoke should throw MBeanException");
        } catch (MBeanException e) {
            assertTrue(e.getCause() instanceof DummyException);
        }
    }
}
