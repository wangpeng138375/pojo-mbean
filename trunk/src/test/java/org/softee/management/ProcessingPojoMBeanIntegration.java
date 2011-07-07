package org.softee.management;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.softee.time.StopWatch;


public class ProcessingPojoMBeanIntegration {
    private static final int AVERAGE_PROCESSING_TIME_MILLIS = 10000;
    private int count = 0;
    private ProcessingPojoMBean monitor;
    
    
    @Before
    public void before() throws Exception {
        monitor = new ProcessingPojoMBean(this, "UnitTest");
        monitor.start();
    }
    
    @After
    public void after() throws Exception {
        monitor.stop();
    }
    
    @Test
    public void testMonitorFor10Minutes() throws Exception {
        runFor(10, TimeUnit.MINUTES);
    }
    
    public void runFor(int value, TimeUnit tu) throws InterruptedException {
        System.out.println("MBean test running for " + value + " " + tu);

        StopWatch runtime = new StopWatch();
        StopWatch stopWatch = new StopWatch();
        
        while (runtime.elapsed(tu) < value) {
            int delayMillis = (int) (Math.random() * 2 * AVERAGE_PROCESSING_TIME_MILLIS);
            System.out.println("Processing message #" + count);
            stopWatch.start();
            monitor.notifyInput();
            System.out.println("notifyReceived duration " + stopWatch.elapsed(TimeUnit.MICROSECONDS) + "µs");
            if (count % 10 == 0) {
                monitor.notifyFailed(throwable());
            } else {
                Thread.sleep(delayMillis);
                monitor.notifyOutput();
            }
            count++;
        }
    }
    
    private Throwable throwable() {
        Exception e = new Exception("Failed message #" + count);
        return new DummyException("Dummy exception with cause", e);
        
    }
}
