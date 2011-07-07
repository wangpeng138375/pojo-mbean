package org.softee.management;

public class DummyException extends Exception {
    private static final long serialVersionUID = 1L;

    public DummyException() {
    }

    public DummyException(String message) {
        super(message);
    }

    public DummyException(Throwable cause) {
        super(cause);
    }

    public DummyException(String message, Throwable cause) {
        super(message, cause);
    }

}
