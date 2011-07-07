package org.softee.management;

import java.util.Date;

import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.MXBean;

import org.softee.management.annotation.Description;

@MXBean
@Description("A Basic Annotated MBean implementation")
public class AnnotatedMBean {
    private final Object monitored;
    private final String instanceName;
    private final Date created;

    public AnnotatedMBean(Object monitored, String instanceName) {
        this.monitored = monitored;
        this.instanceName = instanceName;
        created = new Date();
    }

    @Description("The time at which the MBean was created")
    public Date getCreated() {
        return created;
    }

    protected void registerMBean() throws IntrospectionException, java.beans.IntrospectionException {
        DynamicMBean mbean = new IntrospectedMBean(monitored);
    }
}
