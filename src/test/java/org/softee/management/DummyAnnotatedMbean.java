package org.softee.management;

import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.annotation.Parameter;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedAttribute.Access;

@MBean("DummyAnnotatedMbean")
public class DummyAnnotatedMbean {
    @ManagedAttribute(value = "integer", access=Access.WRITE)
    int integer;
    String string = "A string";
    String operationArgument;

    @ManagedAttribute(access = ManagedAttribute.Access.READ_WRITE)
    String ipsum;

    @ManagedAttribute("string that can only be read")
    public String getString() {
        return string;
    }

    // this should be inaccessible
    public void setString(String value) {
        string = value;
    }

    @ManagedAttribute(value="A Lorem", access=ManagedAttribute.Access.READ)
    public String getLorem() {
        return "Here is a Lorem";
    }


    @ManagedOperation("voidOneArgOperation")
    public void voidOneArgOperation(@Parameter(value = "An argument", name = "argument") String argument) {
        operationArgument = argument;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public void notOperation() {

    }

    public String getNotAttribute() {
        return null;
    }
}
