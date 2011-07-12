package org.softee.util;

public class Objects {

    private Objects() {
    }

    public static <T> T firstNotNull(T... all) {
        for (T element : all) {
            if (element != null) {
                return element;
            }
        }
        throw new IllegalArgumentException("All null arguments");
    }

    public static <T> T notNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }
}
