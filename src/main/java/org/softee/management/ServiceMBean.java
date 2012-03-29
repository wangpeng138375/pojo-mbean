package org.softee.management;

import static org.softee.management.ServiceMBean.State.NEW;
import static org.softee.management.ServiceMBean.State.PAUSED;
import static org.softee.management.ServiceMBean.State.RUNNING;
import static org.softee.management.ServiceMBean.State.STARTING;
import static org.softee.management.ServiceMBean.State.STOPPED;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;


public class ServiceMBean {
    public static enum State {
        NEW {
            @Override
            protected Set<State> getAllowed() {
                return EnumSet.of(STARTING, RUNNING, STOPPED, FAILED, TERMINATED);
            }
        },
        STARTING {
            @Override
            protected Set<State> getAllowed() {
                return EnumSet.of(RUNNING, STOPPED, FAILED, TERMINATED);
            }
        },
        RUNNING {
            @Override
            protected Set<State> getAllowed() {
                return EnumSet.of(PAUSED, STOPPED, FAILED, TERMINATED);
            }
        },
        PAUSED {
            @Override
            protected Set<State> getAllowed() {
                return EnumSet.of(RUNNING, STOPPED, FAILED, TERMINATED);
            }
        },
        STOPPED {
            @Override
            protected Set<State> getAllowed() {
                return EnumSet.of(RUNNING, FAILED, TERMINATED);
            }
        },
        FAILED {
            @Override
            protected Set<State> getAllowed() {
                return EnumSet.of(TERMINATED);
            }
        }, TERMINATED {
            @Override
            protected Set<State> getAllowed() {
                return EnumSet.noneOf(State.class);
            }
        };

        protected abstract Set<State> getAllowed();

        public State next(State next) {
            Set<State> allowed = getAllowed();
            if (!allowed.contains(next)) {
                throw new IllegalArgumentException("Unable to change state from " + this
                        + " to " + next + " (allowed: " + allowed + ")");
            }
            return next;
        }
    }

    private final AtomicReference<State> state = new AtomicReference<State>(NEW);

    @ManagedAttribute
    public String getState() {
        return state.toString();
    }

    /**
     * Transition NEW -> STARTING
     */
    @ManagedOperation
    public void start() {
        if(!state.compareAndSet(NEW, STARTING)) {
            throw new IllegalStateException("Unable to start service with state " + state);
        }
        doStart();
    }

    /**
     * Override to implement actions to be taken to start a service.
     */
    protected void doStart() {
        notifyStarted();
    }

    /**
     * Transition STARTING -> RUNNING
     */
    protected void notifyStarted() {
        //TODO
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
