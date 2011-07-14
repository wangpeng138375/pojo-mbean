package org.softee.management.samples;

import org.softee.management.helper.MBeanRegistration;

/**
 * pojo-mbean sample application, showing the use of auto-annotated attributes and operations<p>
 *
 * The application runs until manually stopped.<p>
 *
 * @author morten.hattesen@gmail.com
 */
public class AutomaticApplication {
    AutomaticMBean mbean = new AutomaticMBean();

    public static void main(String[] args) throws Exception {
        new AutomaticApplication().run();
    }

    public void run() throws Exception {
        /* in a "real" application, the registration instance would probably be
         * saved in a field for subsequent unregistration */
        System.out.println("Running " + getClass());
        new MBeanRegistration(mbean).register();
        for (;;) {
            System.out.print('.');
            Thread.sleep(1000);
        }
    }
}
