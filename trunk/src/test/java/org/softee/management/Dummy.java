package org.softee.management;

import java.util.Collection;
import java.util.EnumSet;

public class Dummy {

    public static <E extends Enum<E>> EnumSet<E> copyOf(Class<E> clazz, Collection<E> c) {
        return (c == null || c.isEmpty()) ? EnumSet.noneOf(clazz) : EnumSet.copyOf(c);
    }
}
