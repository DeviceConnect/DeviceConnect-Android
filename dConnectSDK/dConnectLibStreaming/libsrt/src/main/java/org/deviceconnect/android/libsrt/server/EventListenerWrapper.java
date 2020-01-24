package org.deviceconnect.android.libsrt.server;

import android.os.Handler;

abstract class EventListenerWrapper<T> {

    final T mEventListener;

    final Handler mHandler;

    EventListenerWrapper(final T eventListener,
                         final Handler handler) {
        mEventListener = eventListener;
        mHandler = handler;
    }

    void post(final Runnable r) {
        mHandler.post(r);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (this.mEventListener == o) return true;
        if (!(o instanceof EventListenerWrapper)) return false;

        EventListenerWrapper<?> that = (EventListenerWrapper<?>) o;

        return mEventListener.equals(that.mEventListener);
    }

    @Override
    public int hashCode() {
        return mEventListener.hashCode();
    }
}
