package org.softee.util;

public class Objects {

    private Objects() {
    }

    /**
     * @param <E> The type of elements
     * @param all any number of elements
     * @return the first element of {@code all} that is not null
     * @throws NullPointerException if no element is not-null
     */
    public static <E> E firstNotNull(E... all) {
        for (E element : all) {
            if (element != null) {
                return element;
            }
        }
        throw new NullPointerException("All null arguments");
    }

    /**
     * An assertion method that makes null validation more fluent
     * @param <E> The type of elements
     * @param obj an Object
     * @return {@code obj}
     * @throws NullPointerException if {@code obj} is null
     */
    public static <E> E notNull(E obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }
}
