package org.softee.management;

import static org.softee.management.ServiceMBean.State.NEW;
import static org.softee.management.ServiceMBean.State.PAUSED;
import static org.softee.management.ServiceMBean.State.RUNNING;
import static org.softee.management.ServiceMBean.State.STARTING;
import static org.softee.management.ServiceMBean.State.STOPPED;

import java.util.concurrent.atomic.AtomicReference;

import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;


public class ServiceMBean {
    public static enum State {
        NEW, STARTING, RUNNING, PAUSED, STOPPED, FAILED, TERMINATED;
    }

    private final AtomicReference<State> state = new AtomicReference<State>(NEW);

    @ManagedAttribute
    public String getState() {
        return state.toString();
    }

    @ManagedOperation
    public void start() {
        if(!state.compareAndSet(NEW, STARTING)) {
            throw new IllegalStateException("Unable to start service with state " + state);
        }
    }

    @ManagedOperation
    public void run() {
        // state checking not atomic
        switch (state.get()) {
        case STARTING:
        case PAUSED:
        case STOPPED:
        case FAILED:
            state.set(RUNNING);
            break;
        default:
            throw new IllegalStateException("Unable to run service with state " + state);
        }
    }

    @ManagedOperation
    public void pause() {
        if(!state.compareAndSet(RUNNING, PAUSED)) {
            throw new IllegalStateException("Unable to pause service with state " + state);
        }
    }

    @ManagedOperation
    public void resume() {
        if(!state.compareAndSet(PAUSED, RUNNING)) {
            throw new IllegalStateException("Unable to resume service with state " + state);
        }
    }

    @ManagedOperation
    public void stop() {
        // non atomic state change
        if(!(state.compareAndSet(PAUSED, STOPPED) || state.compareAndSet(RUNNING, STOPPED))) {
            throw new IllegalStateException("Unable to resume service with state " + state);
        }
    }

    @ManagedOperation
    public void terminate() {
        if (state.get() == State.TERMINATED) {
            throw new IllegalStateException("Unable to terminate service with state " + state);
        }
        state.set(State.TERMINATED);
    }
}
