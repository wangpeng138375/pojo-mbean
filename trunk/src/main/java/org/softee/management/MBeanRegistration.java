package org.softee.management;

import java.lang.management.ManagementFactory;

import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.softee.management.exception.ManagementException;
import org.softee.management.helper.IntrospectedDynamicMBean;

public class MBeanRegistration {
    private final Object mBean;
    private final ObjectName mBeanObjectName;
    private final MBeanServer mBeanServer;

    /**
     *
     * @param mBean an MBean instance in the form of a traditional MBean (implementing a sibling *MBean interface) or an
     * MXBean (implementing an interface annotated with @MXBean), or an instance implementing the DynamicMBean interface.
     * @param mBeanObjectName the object name with which {@code mBean} will be registered
     */
    public MBeanRegistration(Object mBean, ObjectName mBeanObjectName) {
        this.mBean = mBean;
        this.mBeanObjectName = mBeanObjectName;
        mBeanServer = ManagementFactory.getPlatformMBeanServer();

    }

    /**
     * Conventience method
     * @param mBean
     * @param objectName the MBean object name in string form
     * @throws MalformedObjectNameException
     */
    public MBeanRegistration(Object mBean, String mBeanObjectName) throws MalformedObjectNameException {
        this(mBean, new ObjectName(mBeanObjectName));
    }

    public MBeanRegistration(Object mBean, String domain, String application, String name, String type) throws MalformedObjectNameException {
        this(mBean, ObjectNameFactory.createObjectName(domain, application, name, type));
    }

    /**
     * Register the MXBean.
     * If the registration fails, a WARN message is logged
     * @throws java.beans.IntrospectionException
     * @throws IntrospectionException
     * @throws NotCompliantMBeanException
     * @throws MBeanRegistrationException
     * @throws InstanceAlreadyExistsException
     */
    protected void register() throws ManagementException {
        try {
            DynamicMBean dynamicMBean = new IntrospectedDynamicMBean(mBean);
            mBeanServer.registerMBean(dynamicMBean, mBeanObjectName);
        } catch (Exception e) {
            throw new ManagementException(e);
        }
    }

    /**
     * Unregister the MXBean.
     * If the unregistration fails, a WARN message is logged
     * @throws InstanceNotFoundException
     * @throws MBeanRegistrationException
     */
    protected void unregister() throws ManagementException {
        try {
            mBeanServer.unregisterMBean(mBeanObjectName);
        } catch (Exception e) {
           throw new ManagementException(e);
        }
    }

}
