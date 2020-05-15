package org.deviceconnect.android.manager.protection;

import android.os.Handler;

/**
 * コピーガード設定の基底クラス.
 */
public abstract class CopyProtectionSetting {

    public interface EventListener {
        void onSettingChange(CopyProtectionSetting setting, boolean isEnabled);
    }

    protected EventListener mEventListener;

    public abstract boolean isAvailable();

    public abstract boolean isEnabled();

    public abstract void enable();

    public abstract void disable();

    public void setEventListener(final EventListener listener, final Handler handler) {
        mEventListener = new EventListenerHolder(listener, handler);
    }

    protected void notifyOnSettingChange(final boolean isEnabled) {
        if (mEventListener != null) {
            mEventListener.onSettingChange(this, isEnabled);
        }
    }

    private static class EventListenerHolder implements EventListener {
        private final EventListener mEventListener;
        private final Handler mHandler;

        EventListenerHolder(final EventListener eventListener,
                            final Handler handler) {
            mEventListener = eventListener;
            mHandler = handler;
        }

        @Override
        public void onSettingChange(final CopyProtectionSetting setting, final boolean isEnabled) {
            mHandler.post(() -> mEventListener.onSettingChange(setting, isEnabled));
        }
    }
}
