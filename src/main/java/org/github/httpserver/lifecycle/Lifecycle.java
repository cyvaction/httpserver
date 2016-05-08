package org.github.httpserver.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.github.httpserver.log.Logger;
import org.github.httpserver.log.LoggerFactory;

public abstract class Lifecycle {
    
	private static final Logger logger = LoggerFactory.getLogger(Lifecycle.class);
	
	private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<LifecycleListener>();
	
    private State state = State.INITED;
    
    protected abstract void doStart();
    
    protected abstract void doStop();
    
    public State getState() {
    	return state;
    }

    public void start() {
    	for (LifecycleListener listener : listeners) {
    		try {
    			listener.onStartBefore();
    		} catch(Exception e) {
    			logger.error(e);
    		}
        }
    	doStart();
    	for (LifecycleListener listener : listeners) {
    		try {
    			listener.onStartAfter();
    		} catch(Exception e) {
    			logger.error(e);
    		}
        }
    	state = State.STARTED;
    }

    public void stop() {
    	for (LifecycleListener listener : listeners) {
    		try {
    			listener.onStopBefore();
    		} catch(Exception e) {
    			logger.error(e);
    		}
        }
    	doStop();
    	for (LifecycleListener listener : listeners) {
    		try {
    			listener.onStopAfter();
    		} catch(Exception e) {
    			logger.error(e);
    		}
        }
    	state = State.STOPPED;
    }
    
    public void addListener(LifecycleListener listener) {
    	listeners.add(listener);
    }
    
    public void removeListener(LifecycleListener listener) {
    	listeners.remove(listener);
    }
    
    public static enum State {
        INITED,
        STARTED,
        STOPPED
    }
    
}

