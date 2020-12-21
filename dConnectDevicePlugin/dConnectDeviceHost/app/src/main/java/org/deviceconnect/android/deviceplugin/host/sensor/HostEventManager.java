package org.deviceconnect.android.deviceplugin.host.sensor;

import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;

import java.util.List;

public class HostEventManager implements HostKeyEventObserver, HostTouchEventObserver {

    private WeakReferenceList<OnKeyEventListener> mOnKeyEventListeners = new WeakReferenceList<>();
    private WeakReferenceList<OnTouchEventListener> mOnTouchEventListeners = new WeakReferenceList<>();

    public void destroy() {
        mOnKeyEventListeners.clear();
        mOnTouchEventListeners.clear();
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

    // Implements HostKeyEventObserver

    @Override
    public void observeKeyEvent(List<HostKeyEvent> keyEvents) {
    }

    // Implements HostTouchEventObserver

    @Override
    public void observeTouchEvent(List<HostTouchEvent> touchEvents) {
    }

    public interface OnKeyEventListener {
        void onKeyEvent(List<HostKeyEvent> keyEvents);
    }

    public interface OnTouchEventListener {
        void onTouchEvent(List<HostTouchEvent> touchEvents);
    }
}
