package org.softee.management;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
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

import org.softee.management.annotation.MBean;
import org.softee.management.annotation.Operation;
import org.softee.management.annotation.Property;

/**
 *  Helper class for implementing commonly monitored metrics in a message processing system.
 *  This class may be extended, and new metrics (attributes and operations) may be added by applying annotations.
 *
 *  <pre>
 *  private int number;
 *
 *  @Description("An operation that adds to number")
 *  public void addToNumber(@Parameter(name="add", value="An integer value to be added") int add){
 *      this.number :+ number;
 *  }
 *
 *  @Description("Number attribute")
 *  public int getNumber() {
 *      return number;
 *  }
 *
 *  // Description annotation not required on setter, since getter is annotated
 *  public void setNumber(int number) {
 *      this.number = number;
 *  }
 *  </pre>
 */
@MBean("Generic MBean for monitoring input/output processing")
public class ProcessingPojoMBean {
    private static final long NONE = Long.MIN_VALUE;
    private final DatatypeFactory dtf;
    private final String instanceName;
    
    private AtomicLong inputCount;
    private AtomicLong inputLatestLatest;

    private AtomicLong outputCount;
    private AtomicLong outputLatest;
    
    private AtomicLong durationLastMillis;
    private AtomicLong durationTotalMillis;
    private AtomicLong durationMaxMillis;
    private AtomicLong durationMinMillis;

    private AtomicLong failedCount;
    private AtomicLong failedLatest;
    private Throwable failedLatestCause;

    private AtomicLong started;

    private final MBeanServer mBeanServer;
    private ObjectName mBeanObjectName;


    /**
     * Create a monitor
     * @param monitored the class that is monitored, used for identifying the MBean in the JConsole or JMX Console
     * @param instanceName instanceName, used for identifying the MBean in the JConsole or JMX Console
     * @throws MalformedObjectNameException 
     * @throws RuntimeException if the monitor could not be created
     */
    public ProcessingPojoMBean(Object monitored, String instanceName) throws MalformedObjectNameException {
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

    private ObjectName createObjectName(Object instance, String instanceName) throws MalformedObjectNameException {
        return new ObjectName(getClass().getPackage() + ":name=" + instanceName + ",type=" + instance.getClass().getName());
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
     * Notify that a message has been input, and processing will begin
     */
    public synchronized void notifyInput() {
        inputCount.incrementAndGet();
        inputLatestLatest.set(now());
    }

    /**
     * Notify that a message has been successfully processed (output), and automatically set the processing duration
     * to the duration since the most recent call to {@link #notifyInput()}.
     * The duration will be invalid this MBean is notified from multiple threads. 
     */
    public synchronized void notifyOutput() {
        long durationMillis = (inputLatestLatest != null) ? now() - inputLatestLatest.get() : 0;
        notifyOutput(durationMillis);
    }

    /**
     * Notify that a message has been successfully processed (output)
     * @param durationMillis The duration of the processing in milliseconds
     */
    public synchronized void notifyOutput(long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Negative duration: " + durationMillis);
        }
        outputLatest.set(now());
        outputCount.incrementAndGet();
        durationLastMillis.set(durationMillis);
        durationTotalMillis.addAndGet(durationMillis);

        Long minMillis = getDurationMinMillis();
        if ((minMillis == null) || (minMillis != null && durationMillis < minMillis)) {
            durationMinMillis.set(durationMillis);
        }

        Long maxMillis = getDurationMaxMillis();
        if ((maxMillis == null) || (durationMillis > maxMillis)) {
            durationMaxMillis.set(durationMillis);
        }
    }

    /**
     * Notify that the processing of a message has failed - no cause
     */
    public synchronized void notifyFailed() {
        notifyFailed(null);
    }

    /**
     * Notify that the processing of a message has failed
     * @param cause The cause of the failure, or null if no cause is available
     */
    public synchronized void notifyFailed(Throwable cause) {
        failedCount.incrementAndGet();
        failedLatest.set(now());
        failedLatestCause = cause;
    }

    @Property("The time when the monitor was started")
    public String getStarted() {
        return dateString(noneAsNull(started));
    }

    // Used for debugging @Description("Unregister and then register the MXBean monitor")
    public void reregisterMonitor() throws ManagementException {
        unregister();
        register();
    }

    @Operation("Reset the monitor statistics")
    public synchronized void reset() {
        started = none();
        inputLatestLatest = none();
        outputLatest = none();
        failedLatest = none();
        inputCount = zero();
        outputCount = zero();
        failedCount = zero();
        durationLastMillis = none();
        durationTotalMillis = zero();
        durationMinMillis = none();
        durationMaxMillis = none();
    }

    @Property("Name of this instance")
    public String getName() {
        return instanceName;
    }
    
    @Property("Number of messages received")
    public long getInputCount() {
        return inputCount.get();
    }

    @Property("Time of last received message")
    public String getInputLatestLatest() {
        return dateString(noneAsNull(inputLatestLatest));
    }

    @Property("Time since latest received message (seconds)")
    public Long getReceivedLatestAgeSeconds() {
        return age(noneAsNull(inputLatestLatest), SECONDS);
    }


    @Property("Number of processed messages")
    public long getOutputCount() {
        return outputCount.get();
    }

    @Property("Time of the latest processed message")
    public String getProcessedLatest() {
        return dateString(noneAsNull(outputLatest));
   }

    @Property("Time since latest processed message (seconds)")
    public Long getProcessedLatestAgeSeconds() {
        return age(noneAsNull(outputLatest), SECONDS);
    }

    @Property("Processing time of the latest message (ms)")
    public Long getProcessedDurationLatestMillis() {
        return noneAsNull(durationLastMillis);
    }

    @Property("Total processing time of all messages (ms)")
    public long getDurationTotalMillis() {
        return durationTotalMillis.get();
    }

    @Property("Average processing time (ms)")
    public Long getProcessedDurationAverageMillis() {
        long processedDurationTotalMillis = getDurationTotalMillis();
        long processedCount = getOutputCount();
        return (processedCount != 0) ? processedDurationTotalMillis/processedCount : null;
    }

    @Property("Min processing time (ms)")
    public Long getDurationMinMillis() {
        return noneAsNull(durationMinMillis);
    }

    @Property("Max processing time (ms)")
    public Long getDurationMaxMillis() {
        return noneAsNull(durationMaxMillis);
    }

    @Property("Number of processes that failed")
    public long getFailedCount() {
        return failedCount.get();
    }

    @Property("Time of the latest failed message processing")
    public String getFailedLatest() {
        return dateString(noneAsNull(failedLatest));
    }

    @Property("Time since latest failed message processing (seconds)")
    public Long getFailedLatestAgeSeconds() {
        return age(noneAsNull(failedLatest), SECONDS);
    }

    @Property("The failure reason of the latest failed message processing")
    public String getFailedLatestReason() {
        if (failedLatestCause == null) {
            return null;
        }
        return failedLatestCause.toString();
    }
    
    /**
     * TODO Neither a multiline string, nor a String array seems to be displayed in a proper way.
     * If required, look into presenting in a composite or tabular form (see {@link javax.management.MXBean})
     * 
     * @Description("The failure stacktrace of the latest failed message processing")
     */
    public String[] getFailedLatestStacktrace() {
        if (failedLatestCause == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        failedLatestCause.printStackTrace(pw);
        pw.close();
        ArrayList<String> lines = new ArrayList<String>(1000);
        BufferedReader reader = new BufferedReader(new StringReader(sw.toString()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            // Impossible
        }
        return lines.toArray(new String[lines.size()]);
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
            mBeanServer.registerMBean(new IntrospectedMBean(this), mBeanObjectName);
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
