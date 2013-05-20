package org.softee.management;

import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.annotation.Parameter;

@MBean() @Description("DummyAnnotatedMbean")
public class DummyAnnotatedMbean implements DummyMbeanInterface {
    int integer;
    String string = "A string";
    String operationArgument;
    final String lorem = "Here is a Lorem";
    
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
        return lorem;
    }

    @ManagedAttribute @Description("A Lorem, DummyException")
    public String getLoremThrowException() throws DummyException {
        throw new DummyException("getLoremThrowException");
    }    

    @ManagedOperation @Description("voidOneArgOperation")
    public void voidOneArgOperation(@Parameter("argument") @Description("An argument") String argument) {
        operationArgument = argument;
    }

    @ManagedOperation @Description("voidOneArgOperationException")
    public void voidOneArgOperationException(@Parameter("argument") @Description("An argument") String argument) throws DummyException {
        throw new DummyException("voidOneArgOperationException");
    }

    
    @ManagedAttribute @Description("integer")
    public void setInteger(int integer) {
        this.integer = integer;
    }

    @ManagedAttribute @Description("integerThrowException")
    public void setIntegerThrowException(int integer) throws DummyException {
        throw new DummyException("setIntegerThrowException");
    }

    
    public void notOperation() {

    }

    public String getNotAttribute() {
        return null;
    }
}
