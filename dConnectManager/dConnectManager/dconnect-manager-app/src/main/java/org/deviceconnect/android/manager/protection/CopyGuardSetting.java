/*
 CopyGuardSetting.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.protection;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.util.NotificationUtils;

/**
 * コピーガード設定の基底クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class CopyGuardSetting {

    public interface EventListener {
        void onSettingChange(CopyGuardSetting setting, boolean isEnabled);
    }

    protected EventListener mEventListener;

    public abstract boolean isAvailable();

    public abstract boolean isEnabled();

    public abstract void enable();

    public abstract void disable();

    public void setEventListener(final EventListener listener, final Handler handler) {
        mEventListener = new EventListenerHolder(listener, handler);
    }

    protected void startActivity(final Context context,
                                 final Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NotificationUtils.createNotificationChannel(context);
            NotificationUtils.notify(context, hashCode(), 0, intent,
                    context.getString(R.string.copy_guard_notification_activity_warnning));
        } else {
            context.startActivity(intent);
        }
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
        public void onSettingChange(final CopyGuardSetting setting, final boolean isEnabled) {
            mHandler.post(() -> mEventListener.onSettingChange(setting, isEnabled));
        }
    }
}
