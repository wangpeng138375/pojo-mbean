package org.softee.management;

import org.softee.management.annotation.MBean;
import org.softee.management.annotation.Operation;
import org.softee.management.annotation.Parameter;
import org.softee.management.annotation.Property;

@MBean("DummyAnnotatedMbean")
public class DummyAnnotatedMbean {
    int integer;
    String string = "A string";
    String operationArgument;

    @Property(access = Property.Access.READ_WRITE)
    String ipsum;

    @Property("string that can only be read")
    public String getString() {
        return string;
    }

    // this should be inaccessible
    public void setString(String value) {
        string = value;
    }

    @Property(value="A Lorem", access=Property.Access.READ)
    public String getLorem() {
        return "Here is a Lorem";
    }


    @Operation("voidOneArgOperation")
    public void voidOneArgOperation(@Parameter(value = "An argument", name = "argument") String argument) {
        operationArgument = argument;
    }

    @Property("integer")
    public void setInteger(int integer) {
        this.integer = integer;
    }

    public void notOperation() {

    }

    public String getNotAttribute() {
        return null;
    }
}
