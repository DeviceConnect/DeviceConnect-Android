/*
 SimpleCopyGuard.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.protection;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.deviceconnect.android.manager.R;

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.manager.core.BuildConfig.DEBUG;

/**
 * Android 端末上でシンプルなコピーガード機能を提供するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class SimpleCopyGuard extends CopyGuardSetting {

    private static final String TAG = "SimpleCopyGuard";

    private final List<CopyGuardSetting> mCopyGuardSettingList = new ArrayList<>();

    private final List<CopyGuardSetting> mPendingList = new ArrayList<>();

    private boolean mLastEnabled;

    private final EventListener mEventListener = (setting, isEnabled) -> {
        if (DEBUG) {
            Log.d(TAG, "onSettingChange: setting=" + setting.getClass().getSimpleName() + ", isEnabled=" + isEnabled);
        }

        boolean enabled = isEnabled();
        if (enabled != mLastEnabled) {
            notifyOnSettingChange(enabled);
        }
        mLastEnabled = enabled;

        // 有効にする際は、許可画面が出る場合があるので、順番に1つずつ確実に処理していく
        if (isEnabled) {
            enableNextSetting(setting);
        }
    };

    private final Handler mHandler;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     * @param appIconId アプリアイコンID
     */
    public SimpleCopyGuard(final Context context, final int appIconId) {
        HandlerThread handlerThread = new HandlerThread("SimpleCopyGuardThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        addSetting(new DeveloperToolGuard(context));
        addSetting(new ScreenRecordingGuardOverlay(context, appIconId));

        // コピー防止機能の通知チャンネル作成
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = context.getString(R.string.copy_guard_notification_channel_id);
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    context.getString(R.string.copy_guard_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.copy_guard_notification_channel_description));
            notificationManager.createNotificationChannel(channel);
        }

        mLastEnabled = isEnabled();
    }

    private void addSetting(final CopyGuardSetting setting) {
        setting.setEventListener(mEventListener, mHandler);
        mCopyGuardSettingList.add(setting);
    }

    @Override
    public void reset() {
        for (CopyGuardSetting setting : mCopyGuardSettingList) {
            setting.reset();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        for (CopyGuardSetting setting : mCopyGuardSettingList) {
            if (!setting.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void enable() {
        synchronized (mPendingList) {
            for (CopyGuardSetting setting : mCopyGuardSettingList) {
                if (!setting.isEnabled() && !mPendingList.contains(setting)) {
                    mPendingList.add(setting);
                }
            }
        }
        enableNextSetting(null);
    }

    private void enableNextSetting(final CopyGuardSetting done) {
        synchronized (mPendingList) {
            if (done != null) {
                mPendingList.remove(done);
            }
            if (mPendingList.size() > 0) {
                CopyGuardSetting next = mPendingList.get(0);
                next.enable();
            }
        }
    }

    @Override
    public void disable() {
        for (CopyGuardSetting setting : mCopyGuardSettingList) {
            setting.disable();
        }
    }
}
