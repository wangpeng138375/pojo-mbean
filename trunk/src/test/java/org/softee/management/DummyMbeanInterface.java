package org.softee.management;



public interface DummyMbeanInterface {
    String getString();

    String getLorem();

    void voidOneArgOperation(String argument);

    void setInteger(int integer);

    /**
     * This is NOT a valid MBean method
     * @param value
     */
    void setString(String value);

    /**
     * This is NOT a valid MBean method
     * @return
     */
    String getNotAttribute();

}