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
    protected static final long NONE = Long.MIN_VALUE;
    private final String name;
    private final DatatypeFactory dtf;
    protected final MBeanServer mBeanServer;
    protected final ObjectName mBeanObjectName;
    private AtomicLong started;

    /**
     * @param type the type (class) of the MBean. The parent package is used as the domain of the MBean
     * @param name the name of the MBean
     * @throws MalformedObjectNameException
     */
    public AbstractPojoMBean(Class<?> type, String name) throws MalformedObjectNameException {
        this(type.getPackage(), type, name);
    }
    
    /**
     * 
     * @param domain the domain of the MBean
     * @param type the type (class) of the MBean.
     * @param name the name of the MBean
     * @throws MalformedObjectNameException
     */
    public AbstractPojoMBean(Package domain, Class<?> type, String name) throws MalformedObjectNameException {
        if (domain == null) {
            throw new NullPointerException("monitoredPackage");
        }
        if (type == null) {
            throw new NullPointerException("monitoredClass");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;
        mBeanObjectName = createObjectName(domain.getName(), type, name);
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        
        // XmlGregorianCalendar suport init
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        
        // Reset all stats
        reset();
    }
    
    @Operation(value = "Reset the MBean", impact = Operation.Impact.ACTION)
    public void reset() {
        // Nothing to reset - shouldn't reset the start time
    }

    @Property("The time when the monitor was started")
    public String getStarted() {
        return dateString(noneAsNull(started));
    }
    
    public String getName() {
        return name;
    }
    /**
     * Constructs an objectName of the format:
     * {@code <domain>:name=<name>,type=<type>}
     * @param domain is the domain that is used for categorizing MBeans in a view
     * @param type is the value bound to the "type" property of the MBean
     * @param name is the value bound to the "name" property of the MBean
     * @return an objectName constructed as above
     * @throws MalformedObjectNameException
     */
    protected ObjectName createObjectName(String domain, Class<?> type, String name) throws MalformedObjectNameException {
        return new ObjectName(domain + ":name=" + name + ",type=" + type.getName());
    }

    /**
     * Start monitoring and register the MBean with the MBean server
     * @throws ManagementException 
     */
    public void start() throws ManagementException {
        started = new AtomicLong(now());
        register();
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
