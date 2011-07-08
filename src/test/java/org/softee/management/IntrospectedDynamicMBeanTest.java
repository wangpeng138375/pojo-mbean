package org.softee.management;

import static org.junit.Assert.assertEquals;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
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

    @Test(expected=InvalidAttributeValueException.class)
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
        assertEquals("Number of attributes", 3, attributes.length);
        assertEquals(annotatedMBean.getClass().getSimpleName(), mBeanInfo.getDescription());
        MBeanOperationInfo[] operations = mBeanInfo.getOperations();
        assertEquals("Number of operations", 1, operations.length);
    }

    @Test
    public void testInvoke() throws Exception {
        String arg0 = "Lorem Ipsum";
        introspectedMBean.invoke("voidOneArgOperation", new Object[] {arg0}, new String[] {"java.lang.String"});
        assertEquals(arg0, annotatedMBean.operationArgument);
    }

}
