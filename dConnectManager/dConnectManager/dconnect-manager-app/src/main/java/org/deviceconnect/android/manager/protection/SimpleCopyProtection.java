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
 */
public class SimpleCopyProtection extends CopyProtectionSetting {

    private static final String TAG = "SimpleCopyProtection";

    private final List<CopyProtectionSetting> mCopyProtectionSettingList = new ArrayList<>();

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
    };

    private final Handler mHandler;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     * @param
     */
    public SimpleCopyProtection(final Context context, final int appIconId) {
        HandlerThread handlerThread = new HandlerThread("SimpleCopyProtectionThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        addSetting(new ScreenRecordingProtectionOverlay(context, appIconId));
        addSetting(new DeveloperToolGuard(context));

        // コピー防止機能の通知チャンネル作成
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = context.getString(R.string.copy_protection_notification_channel_id);
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    context.getString(R.string.copy_protection_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.copy_protection_notification_channel_description));
            notificationManager.createNotificationChannel(channel);
        }

        mLastEnabled = isEnabled();
    }

    private void addSetting(final CopyProtectionSetting setting) {
        setting.setEventListener(mEventListener, mHandler);
        mCopyProtectionSettingList.add(setting);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        for (CopyProtectionSetting setting : mCopyProtectionSettingList) {
            if (!setting.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void enable() {
        for (CopyProtectionSetting setting : mCopyProtectionSettingList) {
            setting.enable();
        }
    }

    @Override
    public void disable() {
        for (CopyProtectionSetting setting : mCopyProtectionSettingList) {
            setting.disable();
        }
    }
}
