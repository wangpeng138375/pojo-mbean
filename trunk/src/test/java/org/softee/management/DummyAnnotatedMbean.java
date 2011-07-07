package org.softee.management;

import org.softee.management.annotation.MBean;
import org.softee.management.annotation.Operation;
import org.softee.management.annotation.Parameter;
import org.softee.management.annotation.Property;

@MBean("DummyAnnotatedMbean")
public class DummyAnnotatedMbean {
    int integer;
    final String string = "A string";
    String operationArgument;

    @Property("string")
    public String getString() {
        return string;
    }

    @Operation("voidOneArgOperation")
    public void voidOneArgOperation(@Parameter(name = "argument", description = "An argument") String argument) {
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
