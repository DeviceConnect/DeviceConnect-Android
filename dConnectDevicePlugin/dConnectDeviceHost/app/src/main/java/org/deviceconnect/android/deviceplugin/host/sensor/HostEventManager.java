package org.deviceconnect.android.deviceplugin.host.sensor;

import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;

import java.util.HashMap;
import java.util.Map;

public class HostEventManager implements HostKeyEventObserver, HostTouchEventObserver {
    private static final long CACHE_RETENTION_TIME = 10000;

    private final Map<String, HostTouchEvent> mTouchEventCache = new HashMap<>();
    private final Map<String, HostKeyEvent> mKeyEventCache = new HashMap<>();

    private final WeakReferenceList<OnKeyEventListener> mOnKeyEventListeners = new WeakReferenceList<>();
    private final WeakReferenceList<OnTouchEventListener> mOnTouchEventListeners = new WeakReferenceList<>();

    public void destroy() {
        mOnKeyEventListeners.clear();
        mOnTouchEventListeners.clear();
        mTouchEventCache.clear();
        mKeyEventCache.clear();
    }

    public void addOnKeyEventListener(OnKeyEventListener listener) {
        mOnKeyEventListeners.add(listener);
    }

    public void removeOnKeyEventListener(OnKeyEventListener listener) {
        mOnKeyEventListeners.remove(listener);
    }

    public void addOnTouchEventListener(OnTouchEventListener listener) {
        mOnTouchEventListeners.add(listener);
    }

    public void removeOnTouchEventListener(OnTouchEventListener listener) {
        mOnTouchEventListeners.remove(listener);
    }

    public HostTouchEvent getTouchCache(final String state) {
        long currentTime = System.currentTimeMillis();
        HostTouchEvent event = mTouchEventCache.get(state);
        if (event != null && currentTime - event.getTimestamp() <= CACHE_RETENTION_TIME) {
            return event;
        }
        return null;
    }

    private void setTouchCache(String state, HostTouchEvent touchData) {
        mTouchEventCache.put(state, touchData);
    }

    public HostKeyEvent getKeyEventCache(String state) {
        long currentTime = System.currentTimeMillis();
        HostKeyEvent event = mKeyEventCache.get(state);
        if (event != null && currentTime - event.getTimestamp() <= CACHE_RETENTION_TIME) {
            return event;
        }
        return null;
    }

    private void setKeyEventCache(String state, HostKeyEvent keyEvent) {
        mKeyEventCache.put(state, keyEvent);
    }

    // Implements HostKeyEventObserver

    @Override
    public void observeKeyEvent(HostKeyEvent keyEvent) {
        setKeyEventCache(keyEvent.getState(), keyEvent);
        setKeyEventCache(HostKeyEvent.STATE_KEY_CHANGE, keyEvent);
        for (OnKeyEventListener l : mOnKeyEventListeners) {
            l.onKeyEvent(keyEvent);
        }
    }

    // Implements HostTouchEventObserver

    @Override
    public void observeTouchEvent(HostTouchEvent touchEvent) {
        setTouchCache(touchEvent.getState(), touchEvent);
        setTouchCache(HostTouchEvent.STATE_TOUCH_CHANGE, touchEvent);
        for (OnTouchEventListener l : mOnTouchEventListeners) {
            l.onTouchEvent(touchEvent);
        }
    }

    public interface OnKeyEventListener {
        void onKeyEvent(HostKeyEvent keyEvent);
    }

    public interface OnTouchEventListener {
        void onTouchEvent(HostTouchEvent touchEvent);
    }
}
