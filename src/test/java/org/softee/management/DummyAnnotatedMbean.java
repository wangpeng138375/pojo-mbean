package org.softee.management;

import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.annotation.Parameter;

@MBean() @Description("DummyAnnotatedMbean")
public class DummyAnnotatedMbean {
    int integer;
    String string = "A string";
    String operationArgument;

    @ManagedAttribute @Description("string that can only be read")
    public String getString() {
        return string;
    }

    // this should be inaccessible
    public void setString(String value) {
        string = value;
    }

    @ManagedAttribute @Description("A Lorem")
    public String getLorem() {
        return "Here is a Lorem";
    }


    @ManagedOperation @Description("voidOneArgOperation")
    public void voidOneArgOperation(@Parameter("argument") @Description("An argument") String argument) {
        operationArgument = argument;
    }

    @ManagedAttribute @Description("integer")
    public void setInteger(int integer) {
        this.integer = integer;
    }

    public void notOperation() {

    }

    public String getNotAttribute() {
        return null;
    }
}
