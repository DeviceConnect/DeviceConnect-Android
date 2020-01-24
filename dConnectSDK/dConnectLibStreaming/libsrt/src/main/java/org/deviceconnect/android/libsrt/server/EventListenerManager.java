package org.deviceconnect.android.libsrt.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class EventListenerManager<T> {

    final List<T> mEventListenerList = new ArrayList<>();

    void addListener(final T listener) {
        synchronized (mEventListenerList) {
            mEventListenerList.add(listener);
        }
    }

    void removeListener(final T listener) {
        synchronized (mEventListenerList) {
            for (Iterator<T> it = mEventListenerList.iterator(); it.hasNext(); ) {
                T cache = it.next();
                if (cache.equals(listener)) {
                    it.remove();
                    return;
                }
            }
        }
    }

    void listeners(final ListenerListCallback<T> callback) {
        synchronized (mEventListenerList) {
            for (T listener : mEventListenerList) {
                callback.listener(listener);
            }
        }
    }

    interface ListenerListCallback<T> {

        void listener(T listener);
    }
}
