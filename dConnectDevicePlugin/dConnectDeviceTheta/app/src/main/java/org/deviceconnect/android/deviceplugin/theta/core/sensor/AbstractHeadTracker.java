package org.deviceconnect.android.deviceplugin.theta.core.sensor;


import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractHeadTracker implements HeadTracker {

    private final List<HeadTrackingListener> mListeners = new ArrayList<HeadTrackingListener>();

    @Override
    public void registerTrackingListener(final HeadTrackingListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    @Override
    public void unregisterTrackingListener(final HeadTrackingListener listener) {
        synchronized (mListeners) {
            for (Iterator<HeadTrackingListener> it = mListeners.iterator(); it.hasNext(); ) {
                if (it.next() == listener) {
                    it.remove();
                }
            }
        }
    }

    protected void notifyHeadRotation(final Quaternion rotation) {
        synchronized (mListeners) {
            for (HeadTrackingListener listener : mListeners) {
                listener.onHeadRotated(rotation);
            }
        }
    }

    public int getListenerCount() {
        return mListeners.size();
    }

}
