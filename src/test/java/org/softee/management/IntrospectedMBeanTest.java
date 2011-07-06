package org.softee.management;

import static org.junit.Assert.fail;

import javax.management.DynamicMBean;

import org.junit.Before;
import org.junit.Test;

public class IntrospectedMBeanTest {
    private AnnotatedMBean annotatedMBean;
    private DynamicMBean introspectedMBean;

    @Before
    public void before() {
        annotatedMBean = new AnnotatedMBean(this, "UnitTest");
        introspectedMBean = new IntrospectedMBean(annotatedMBean);
    }

    @Test
    public void testAnnotatedMBean() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetAttribute() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetAttributes() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetMBeanInfo() {
        fail("Not yet implemented");
    }

    @Test
    public void testInvoke() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetAttribute() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetAttributes() {
        fail("Not yet implemented");
    }

}
