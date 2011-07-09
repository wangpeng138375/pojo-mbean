package org.softee.util;

public class Objects {
    public <T> T firstNotNull(T... all) {
        for (T element : all) {
            if (element != null) {
                return element;
            }
        }
        throw new IllegalArgumentException("All null arguments");
    }
}
