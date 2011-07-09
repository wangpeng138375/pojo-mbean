package org.softee.management.helper;

import static java.lang.String.format;
import static org.softee.util.Preconditions.notNull;

import java.util.Hashtable;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.softee.management.annotation.MBean;

public class ObjectNameFactory {

    /**
     * Construct an ObjectName using the object name attributes of the @MBean annotation
     * @throws MalformedObjectNameException if the object name is not wellformed
     */
    public static ObjectName createObjectName(Object mBean) throws MalformedObjectNameException {
        Class<? extends Object> clazz = mBean.getClass();
        MBean annotation = clazz.getAnnotation(MBean.class);
        if (annotation == null) {
            throw new MalformedObjectNameException(format("%s is not annotated with @%s", clazz, MBean.class.getName()));
        }
        String objectName = annotation.objectName();
        if (objectName.isEmpty()) {
            throw new MalformedObjectNameException(format("@%s does not define the 'objectName' attribute", MBean.class.getName()));
        }
        return new ObjectName(objectName);
    }

    public static ObjectName replaceProperty(ObjectName objectName, String name, String value) throws MalformedObjectNameException {
        Hashtable<String, String> table = objectName.getKeyPropertyList();
        table.put(name, value);
        return new ObjectName(objectName.getDomain(), table);
    }

    /**
     * Constructs an objectName of the format:
     * {@code <domain>:name=<name>,type=<type>}
     * @param domain is the domain that is used for categorizing MBeans in a view
     * @param application value bound to the "application" property of the MBean, or null if unspecified
     * @param type is the value bound to the "type" property of the MBean, not null
     * @param name is the value bound to the "name" property of the MBean, not null
     * @return an objectName constructed as above
     * @throws MalformedObjectNameException if one of the object name components is not well formed
     */
    public static ObjectName createObjectName(String domain, String application, String name, String type) throws MalformedObjectNameException {
        StringBuilder sb = new StringBuilder(100);
        sb.append(notNull(domain, "domain"));
        addAttribute(sb, "application", application, false);
        addAttribute(sb, "name", name, true);
        addAttribute(sb, "type", type, true);
        return new ObjectName(sb.toString());
    }

    public static ObjectName createObjectName(String domain, Map<String, String> properties) throws MalformedObjectNameException {
        Hashtable<String, String> table = new Hashtable<String, String>(properties);
        return new ObjectName(domain, table);
    }

    protected static void addAttribute(StringBuilder sb, String name, String value, boolean required) {
        if (required && value == null) {
            throw new NullPointerException(name);
        }
        if (value != null) {
            sb.append(name).append("=").append(value);
        }
    }
}
