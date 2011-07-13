package org.softee.management.samples;

import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.helper.MBeanRegistration;

/**
 * pojo-mbean sample application.<p>
 *
 * The application runs until manually stopped.<p>
 *
 * @author morten.hattesen@gmail.com
 */
@MBean(objectName = "org.softee:type=Demo,name=ElementSortingApplication")
@Description("This Java application shows how override default sorting of operations and attributes in the MBean server client."
        + "\nNote that not all clients respect the sort order.")
public class ElementSortingApplication {
    public static void main(String[] args) throws Exception {
        new ElementSortingApplication().run();
    }

    public Object run() throws Exception {
        /* in a "real" application, the registration instance would probably be
         * saved in a field for subsequent unregistration */
        new MBeanRegistration(this).register();
        for (;;) {
            System.out.println("Running " + getClass());
            Thread.sleep(1000); // one second
        }
    }

    @ManagedAttribute @Description(value="sorted as b", sortAs="b")
    public String getA() {
        return null;
    }

    @ManagedAttribute @Description(value="sorted as C", sortAs="C")
    public String getB() {
        return null;
    }

    @ManagedAttribute @Description(value="sorted as A", sortAs="A")
    public String getC() {
        return null;
    }

    @ManagedOperation
    @Description(value="sorted as 3", sortAs="3")
    public void a() {}

    @ManagedOperation
    @Description(value="sorted as 1", sortAs="1")
    public void b() {}

    @ManagedOperation
    @Description(value="sorted as 2", sortAs="2")
    public void c() {}
}
