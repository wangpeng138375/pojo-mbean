package org.softee.management;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.UndeclaredThrowableException;

import javax.management.DynamicMBean;

import org.junit.Before;
import org.junit.Test;
import org.softee.management.helper.IntrospectedDynamicMBean;
import org.softee.management.helper.DynamicProxyFactory;

public class DynamicProxyfactoryTest {
    private DummyAnnotatedMbean annotatedMBean;
    private DynamicMBean introspectedMBean;
    private DummyMbeanInterface interfaceProxy;
    private DummyAnnotatedMbean classProxy;

    @Before
    public void before() throws Exception, java.beans.IntrospectionException {
        annotatedMBean = new DummyAnnotatedMbean();
        introspectedMBean = new IntrospectedDynamicMBean(annotatedMBean);
        interfaceProxy = DynamicProxyFactory.createDynamicProxy(introspectedMBean, DummyMbeanInterface.class);
    }

    @Test
    public void testInterfaceProxy() {
        // operation
        interfaceProxy.voidOneArgOperation("Hello");
        assertEquals("Hello", annotatedMBean.operationArgument);
        // attribute write
        interfaceProxy.setInteger(123);
        assertEquals(123, annotatedMBean.integer);
        // attribute read
        assertEquals(annotatedMBean.lorem, interfaceProxy.getLorem());
    }

    @Test
    public void testInterfaceProxySetString() {
        // this really ought not to work, but it does, since the getter is valid
        interfaceProxy.setString("Not an MBean annotated method");
    }

    @Test(expected=UndeclaredThrowableException.class)
    public void testInterfaceProxySetNotAttribute() {
        interfaceProxy.getNotAttribute();
    }

}
