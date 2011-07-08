package org.softee.management;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MalformedObjectNameException;

import org.softee.management.annotation.MBean;
import org.softee.management.annotation.Operation;
import org.softee.management.annotation.Property;
import org.softee.management.exception.ManagementException;

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
 *
 * @author morten.hattesen@gmail.com
 */
@MBean("Generic MBean for monitoring input/output processing")
public class ProcessingPojoMBean extends AbstractPojoMBean {

    private AtomicLong inputCount;
    private AtomicLong inputLatest;

    private AtomicLong outputCount;
    private AtomicLong outputLatest;

    private AtomicLong durationLastMillis;
    private AtomicLong durationTotalMillis;
    private AtomicLong durationMaxMillis;
    private AtomicLong durationMinMillis;

    private AtomicLong failedCount;
    private AtomicLong failedLatest;
    private Throwable failedLatestCause;

    public ProcessingPojoMBean(Class<?> type, String name) throws MalformedObjectNameException {
        super(type, name);
    }

    public ProcessingPojoMBean(Package domain, Class<?> type, String name) throws MalformedObjectNameException {
        super(domain, type, name);
    }

    /**
     * Notify that a message has been input, and processing will begin
     */
    public synchronized void notifyInput() {
        inputCount.incrementAndGet();
        inputLatest.set(now());
    }

    /**
     * Notify that a message has been successfully processed (output), and automatically set the processing duration
     * to the duration since the most recent call to {@link #notifyInput()}.
     * The duration will be invalid this MBean is notified from multiple threads.
     */
    public synchronized void notifyOutput() {
        long latest = inputLatest.get();
        if (latest == NONE) {
            /* This can only be caused by...
             * 1. notifyStop() without preceding notifyStart()
             * 2. a call to reset() since last notifyStart()
             */
            notifyOutput(0);
        } else {
            notifyOutput(now() - latest);
        }
    }

    /**
     * Notify that a message has been successfully processed (output)
     * @param durationMillis The duration of the processing in milliseconds
     */
    public synchronized void notifyOutput(long durationMillis) {
        durationMillis = Math.max(0, durationMillis);

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

    // Used for debugging @Operation("Unregister and then register the MXBean monitor")
    public void reregisterMonitor() throws ManagementException {
        unregister();
        register();
    }

    @Override
    @Operation(value = "Reset the accumulated statistics", impact = Operation.Impact.ACTION)
    public synchronized void reset() {
        super.reset();
        inputLatest = none();
        outputLatest = none();
        failedLatest = none();
        inputCount = zero();
        outputCount = zero();
        failedCount = zero();
        durationLastMillis = none();
        durationMinMillis = none();
        durationMaxMillis = none();
        durationTotalMillis = zero();
    }


    @Property("Number of messages received")
    public long getInputCount() {
        return inputCount.get();
    }

    @Property("Time of last received message")
    public String getInputLatest() {
        return dateString(noneAsNull(inputLatest));
    }

    @Property("Time since latest received message (seconds)")
    public Long getInputLatestAgeSeconds() {
        return age(noneAsNull(inputLatest), SECONDS);
    }


    @Property("Number of processed messages")
    public long getOutputCount() {
        return outputCount.get();
    }

    @Property("Time of the latest processed message")
    public String getOutputLatest() {
        return dateString(noneAsNull(outputLatest));
   }

    @Property("Time since latest processed message (seconds)")
    public Long getOutputLatestAgeSeconds() {
        return age(noneAsNull(outputLatest), SECONDS);
    }

    @Property("Processing time of the latest message (ms)")
    public Long getDurationLatestMillis() {
        return noneAsNull(durationLastMillis);
    }

    @Property("Total processing time of all messages (ms)")
    public long getDurationTotalMillis() {
        return durationTotalMillis.get();
    }

    @Property("Average processing time (ms)")
    public Long getDurationAverageMillis() {
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
     * TODO Presentation of multi-line content isn't elegant. But in the JConsole, by double-clocking on a String array,
     * the elements will be presented one-per-line.<p>
     * If required, look into presenting in a composite or tabular form (see {@link javax.management.MXBean}
     */
    @Property("The failure stacktrace of the latest failed message processing (one line per element)")
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
}
