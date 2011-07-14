package org.softee.management;

import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;


public class ServiceMBean {
    public static enum State {
        NEW, STARTED, RUNNING, PAUSED, STOPPED, FAILED, TERMINATED;
        private State next;

    }
    private State state;

    @ManagedAttribute
    public String getState() {
        return state.toString();
    }

    @ManagedOperation
    public void start() {

    }

    @ManagedOperation
    public void pause() {

    }

    @ManagedOperation
    public void resume() {

    }

    @ManagedOperation
    public void stop() {

    }

    @ManagedOperation
    public void terminate() {

    }
}
