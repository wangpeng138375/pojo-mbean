package org.softee.management.samples;

import java.util.concurrent.atomic.AtomicInteger;

import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.annotation.Parameter;
import org.softee.management.helper.MBeanRegistration;

/**
 * pojo-mbean sample application.<p>
 *
 * The application runs until manually stopped.<p>
 *
 * {@code @ManagedAttribute} methods will be called from multiple threads (application as well as MBean server)
 * and appropriate thread safety precautions must therefore be taken.<br>
 * These may include:
 * <ul>
 * <li>Make the methods synchronized</li>
 * <li>Include synchronized blocks around critical sections of the method</li>
 * <li>Make the fields volatile</li>
 * <li>Use concurrent types, and use them in a thread safe manner (used in this example)</li>
 * </ul>
 *
 *  For more information about concurrency and thread safety, I highly recommend the books
 *  "Effective Java", and "Concurrency in Practice".
 *
 * @author morten.hattesen@gmail.com
 */
@MBean(objectName = "org.softee:type=Demo,name=CountingApplication")
@Description("This Java application shows how to expose a Read/Write int property and two methods as an MBean")
public class CountingApplication {
    /**
     * The counter that is made visible to both application and Management Agent (JConsole / JMX console)
     */
    private final AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        new CountingApplication().run();
    }

    private void run() throws Exception {
        /* in a "real" application, the registration instance would probably be
         * saved in a field for subsequent unregistration */
        new MBeanRegistration(this).register();
        for (;;) {
            System.out.println("counter = " + getCounter());
            incrementCounter(1);
            Thread.sleep(1000); // one second
        }
    }

    @ManagedAttribute @Description("A counter variable")
    public int getCounter() {
        return counter.get();
    }

    @ManagedAttribute
    public void setCounter(int counter) {
        this.counter.set(counter);
    }

    @ManagedOperation
    @Description("Increments the counter by the requested amount and shows the resulting value")
    public int incrementCounter(
            @Parameter("amount") @Description("The amount to increment the counter with") int delta) {
        return counter.addAndGet(delta);
    }

    @ManagedOperation @Description("Resets the counter")
    public void reset() {
        counter.set(0);
    }
}
