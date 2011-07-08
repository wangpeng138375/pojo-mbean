package org.softee.management;

import java.util.concurrent.TimeUnit;

import org.softee.time.StopWatch;

public class DemoPojoMBeanMain implements Runnable {
    private static final int DEMO_PROCESSING_TIME_MILLIS = 1000;
    private ProcessingPojoMBean monitor;

    public DemoPojoMBeanMain() {

    }

    @Override
    public void run() {
        try {
            monitor = new ProcessingPojoMBean(getClass(), "Demonstration");
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
        new DemoPojoMBeanMain().run();
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
