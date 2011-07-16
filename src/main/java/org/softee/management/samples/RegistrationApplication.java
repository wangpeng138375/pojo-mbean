package org.softee.management.samples;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.management.helper.MBeanRegistration;

/**
 * pojo-mbean sample application showing how MBeanRegistration callbacks are achieved.<p>
 *
 * The application runs until manually stopped.<p>
 *
 * @author morten.hattesen@gmail.com
 */
@MBean(objectName = "org.softee:type=Demo,name=RegistrationApplication")
@Description("This Java application shows how to receive MBeanRegistration events from the MBeanServer")
public class RegistrationApplication implements javax.management.MBeanRegistration {

    public static void main(String[] args) throws Exception {
        new RegistrationApplication().run();
    }

    public void run() throws Exception {
        /* in a "real" application, the registration instance would probably be
         * saved in a field for subsequent unregistration */
        System.out.println("Running " + getClass());
        new MBeanRegistration(this).register();
        for (;;) {
            System.out.print('.');
            Thread.sleep(1000);
        }
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        System.out.println("preRegister(" + server + ", " + name + ")");
        return name;
    }

    public void postRegister(Boolean registrationDone) {
        System.out.println("postRegister(" + registrationDone + ")");
    }

    public void postDeregister() {
        System.out.println("postDeregister()");
    }

    public void preDeregister() throws Exception {
        System.out.println("preDeregister()");
    }

}
