package org.softee.management;

import java.util.concurrent.TimeUnit;

import javax.management.MalformedObjectNameException;

import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.time.StopWatch;

public class DemoMBeanMain implements Runnable {
    private static final int DEMO_PROCESSING_TIME_MILLIS = 1000;
    private MessagingMBean monitor;

    /**
     * Subclass create purely to redefine the objectname and description
     */
    @MBean(objectName = "org.softee:type=Demo,application=ESB,name=MessageMonitor")
    @Description("An MBean created to show the ease of use of the pojo-mbean")
    private static class DemoMessagingMBean extends MessagingMBean {
        public DemoMessagingMBean() throws MalformedObjectNameException {
        }

    }
    private DemoMBeanMain() {

    }

    @Override
    public void run() {
        try {
            monitor = new DemoMessagingMBean();
            start();
            runFor(1, TimeUnit.DAYS, DEMO_PROCESSING_TIME_MILLIS);
            shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        monitor.start();
    }
    public void shutdown() throws Exception {
        monitor.stop();
    }

    public static void main(String[] args) {
        new DemoMBeanMain().run();
    }

    public void runFor(int value, TimeUnit tu, int averageProcessingTimeMillis) {
        System.out.println("MBean demo running for " + value + " " + tu);
        int count = 0;
        StopWatch runtime = new StopWatch();
        try {
            while (runtime.elapsed(tu) < value) {
                count++;
                int delayMillis = (int) (Math.random() * 2 * averageProcessingTimeMillis);
                monitor.notifyInput(); // simulate receiving an event
                Thread.sleep(delayMillis);  // simulate processing the event
                if (count % 10 == 0) {
                    monitor.notifyFailed(dummyThrowable(count));
                    log(count, "failed");
                } else {
                    monitor.notifyOutput();
                    log(count, null);
                }
            }
        } catch (InterruptedException ignore) {
            // empty by design
        }
    }

    private void log(int count, String suffix) {
        String msgSuffix = (suffix == null) ? "" : " (" + suffix + ")";
        String msg = String.format("message #%d%s", count, msgSuffix);
        System.out.println(msg);
    }

    private Throwable dummyThrowable(int count) {
        Exception e = new RuntimeException("Wrapped failure, message #" + count);
        return new Exception("Failed message #" + count, e);

    }

}
