package org.softee.util;

public class Preconditions {
    public static <T> T notNull(T obj) {
        return notNull(obj, null);
    }

    public static <T> T notNull(T obj, String msg) {
        if (obj == null) {
            throw (msg == null) ? new NullPointerException() : new NullPointerException(msg);
        }
        return obj;
    }
}
