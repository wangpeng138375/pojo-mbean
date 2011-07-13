package org.softee.management;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class DynamicMBeanValidator {
    
    public void validateDynamicMBean(DynamicMBean dbean) {
        MBeanInfo beanInfo = dbean.getMBeanInfo();
        
        // class
        String className = beanInfo.getClassName();
        System.out.println("className = " + className);
        assertNotNull(className);
        
        // description
        String description = beanInfo.getDescription();
        System.out.println("description = " + description);
        assertNotNull(description);
        
        // attributes
        MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
        assertNotNull(attributes);
        // require attributes
        assertTrue(attributes.length > 0);
        for (MBeanAttributeInfo attribute : attributes) {
            System.out.println("attribute = " + attribute);
            // require description
            assertNotNull(attribute.getDescription());
        }
        
        //operations
        MBeanOperationInfo[] operations = beanInfo.getOperations();
        assertNotNull(operations);
        //require operation
        assertTrue(operations.length > 0);
        for (MBeanOperationInfo operation : operations) {
            System.out.println("operation = " + operation);
            //Require description
            assertNotNull(operation.getDescription());
            MBeanParameterInfo[] parameters = operation.getSignature();
            for (MBeanParameterInfo parameter : parameters) {
                System.out.println("parameter = " + parameter);
                // require description of all parameters
                assertNotNull(parameter.getDescription());
                assertNotNull(parameter.getName());
                assertNotNull(parameter.getType());
            }
        }
        
        // constructors
        MBeanConstructorInfo[] constructors = beanInfo.getConstructors();
        System.out.println("constructors = " + Arrays.toString(constructors));
        assertNotNull(constructors);
        
        
        // notifications
        MBeanNotificationInfo[] notifications = beanInfo.getNotifications();
        System.out.println("notifications = " + Arrays.toString(notifications));
        assertNotNull(notifications);
    }
}
