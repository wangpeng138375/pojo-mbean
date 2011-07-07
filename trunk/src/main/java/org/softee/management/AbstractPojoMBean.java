package org.softee.management;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.lang.management.ManagementFactory;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.softee.management.annotation.Operation;
import org.softee.management.annotation.Property;
import org.softee.management.helper.IntrospectedDynamicMBean;

/**
 * An abstract base-class for generating Pojo MBeans that are capable of registering and unregistering themselves
 * 
 * @author morten.hattesen@gmail.com
 *
 */
public abstract class AbstractPojoMBean {
    static final long NONE = Long.MIN_VALUE;
    private final String instanceName;
    private final DatatypeFactory dtf;
    protected AtomicLong started;
    protected final MBeanServer mBeanServer;
    protected ObjectName mBeanObjectName;

    public AbstractPojoMBean(Object monitored, String instanceName) throws MalformedObjectNameException {
        if (instanceName == null) {
            throw new NullPointerException("instanceName");
        }
        this.instanceName = instanceName;
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanObjectName = createObjectName(monitored, instanceName);
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        reset();
    }

    @Operation("Reset the MBean")
    public void reset() {
        // Nothing to reset. Shouldn't reset the start time
    }

    @Property("Name of this instance")
    public String getName() {
        return instanceName;
    }
    
    protected ObjectName createObjectName(Object instance, String instanceName) throws MalformedObjectNameException {
        return new ObjectName(getClass().getPackage().getName() + ":name=" + instanceName + ",type=" + instance.getClass().getName());
    }

    /**
     * Start monitoring and register the MBean with the MBean server
     * @throws ManagementException 
     */
    public void start() throws ManagementException {
        register();
        started.set(now());
    }

    /**
     * Stop monitoring and unregister the MBean with the MBean server
     * @throws ManagementException 
     */
    public void stop() throws ManagementException {
        unregister();
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
            mBeanServer.registerMBean(new IntrospectedDynamicMBean(this), mBeanObjectName);
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

    protected Long noneAsNull(AtomicLong a) {
        long n = a.get();
        return n == NONE ? null : n;
    }

    protected Long age(Long millis, TimeUnit unit) {
        return millis == null ? null : unit.convert(now() - millis, MILLISECONDS);
    }

    /**
     * 
     * @param millis
     * @return millis formatted as an ISO 8601 (XML datetime) string
     */
    protected String dateString(Long millis) {
        if (millis == null) {
            return null;
        }
        return date(millis).toXMLFormat();
    }

    /**
     * XMLGregorianCalendar used because it provides a standard formatting (ISO 8601) and timezone handling
     * @param millis
     * @return an XMLGregorianCalendar in the current timezone.<p>
     * TODO consider forcing TimeZone to UTC
     */
    protected XMLGregorianCalendar date(Long millis) {
        if (millis == null) {
            return null;
        }
        GregorianCalendar gCal = new GregorianCalendar();
        gCal.setTimeInMillis(millis);
        return dtf.newXMLGregorianCalendar(gCal);
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    protected AtomicLong zero() {
        return new AtomicLong();
    }

    protected AtomicLong none() {
        return new AtomicLong(NONE);
    }

}
