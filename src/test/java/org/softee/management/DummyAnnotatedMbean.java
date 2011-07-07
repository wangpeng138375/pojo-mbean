package org.softee.management;

import javax.management.MXBean;

import org.softee.management.annotation.Description;
import org.softee.management.annotation.Parameter;

@MXBean
@Description("DummyAnnotatedMbean")
public class DummyAnnotatedMbean {
    int integer;
    final String string = "A string";
    String operationArgument;

    @Description("string")
    public String getString() {
        return string;
    }

    @Description("voidOneArgOperation")
    public void voidOneArgOperation(@Parameter(name = "argument", value = "An argument") String argument) {
        operationArgument = argument;
    }

    @Description("integer")
    public void setInteger(int integer) {
        this.integer = integer;
    }

    public void notOperation() {

    }

    public String getNotAttribute() {
        return null;
    }
}
