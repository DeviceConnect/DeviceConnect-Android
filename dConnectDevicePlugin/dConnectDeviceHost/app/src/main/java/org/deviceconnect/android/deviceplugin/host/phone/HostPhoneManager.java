package org.deviceconnect.android.deviceplugin.host.phone;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.profile.PhoneProfileConstants;

import java.util.ArrayList;
import java.util.List;

public class HostPhoneManager {

    private static final String[] PERMISSIONS;

    static {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.READ_CALL_LOG);
        }
        PERMISSIONS = permissions.toArray(new String[permissions.size()]);
    }

    /** Notification Id */
    private final int NOTIFICATION_ID = 3537;

    /**
     * 電話番号のサイズ.
     */
    private static final int MAX_PHONE_NUMBER_SIZE = 13;

    private DevicePluginContext mPluginContext;
    private TelephonyManager mTelephonyManager;

    /**
     * 現在の通話状態.
     */
    private PhoneState mCurrentPhoneState;

    private PhoneEventListener mPhoneEventListener;

    public HostPhoneManager(DevicePluginContext pluginContext) {
        mPluginContext = pluginContext;
        mTelephonyManager = (TelephonyManager) mPluginContext.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        init();
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        mPluginContext.getContext().registerReceiver(mHostConnectionReceiver, filter);
    }

    public void destroy() {
        try {
            mPluginContext.getContext().unregisterReceiver(mHostConnectionReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    public void setPhoneEventListener(PhoneEventListener listener) {
        mPhoneEventListener = listener;
    }

    /**
     * パーミッションの取得を要求します.
     *
     * @param callback パーミッションの要求結果を通知するコールバック
     */
    public void requestPermissions(PermissionCallback callback) {
        PermissionUtility.requestPermissions(mPluginContext.getContext(), new Handler(Looper.getMainLooper()),
                PERMISSIONS,
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        if (callback != null) {
                            callback.onAllowed();
                        }
                    }

                    @Override
                    public void onFail(@NonNull String deniedPermission) {
                        if (callback != null) {
                            callback.onDisallowed();
                        }
                    }
                });
    }

    /**
     * 電話番号のフォーマットチェックを行う.
     *
     * @param phoneNumber 電話番号
     * @return 電話番号の場合はtrue、それ以外はfalse
     */
    public boolean checkPhoneNumber(final String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        if (phoneNumber.length() > MAX_PHONE_NUMBER_SIZE) {
            return false;
        }
        String pattern = "[0-9+]+";
        return phoneNumber.matches(pattern);
    }

    /**
     * 指定された URI に電話をかけます.
     *
     * @param uri 電話をかけ先
     */
    public void call(final Uri uri) {
        final Context context = mPluginContext.getContext();

        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.startActivity(intent);
        } else {
            NotificationUtils.createNotificationChannel(context);
            NotificationUtils.notify(context, NOTIFICATION_ID, 0, intent,
                    context.getString(R.string.host_notification_phone_warnning));
        }
    }

    @SuppressWarnings("MissingPermission")
    @TargetApi(26)
    public void acceptRingingCall() {
        TelecomManager telecomMgr = (TelecomManager) mPluginContext.getContext()
                .getSystemService(Context.TELECOM_SERVICE);
        if (telecomMgr != null) {
            telecomMgr.acceptRingingCall();
        }
    }

    @SuppressWarnings("MissingPermission")
    @TargetApi(28)
    public void endCall() {
        TelecomManager telecomMgr = (TelecomManager) mPluginContext.getContext()
                .getSystemService(Context.TELECOM_SERVICE);
        if (telecomMgr != null) {
            telecomMgr.endCall();
        }
    }

    public boolean setPhoneMode(PhoneProfileConstants.PhoneMode mode) {
        AudioManager audioManager = (AudioManager) mPluginContext.getContext()
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return false;
        } else if (mode.equals(PhoneProfileConstants.PhoneMode.SILENT)) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else if (mode.equals(PhoneProfileConstants.PhoneMode.SOUND)) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        } else if (mode.equals(PhoneProfileConstants.PhoneMode.MANNER)) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        } else {
            return false;
        }
        return true;
    }

    public PhoneState detectCurrentPhoneState() {
        int state = mTelephonyManager.getCallState();
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                return PhoneState.STANDBY;
            case TelephonyManager.CALL_STATE_RINGING:
                return PhoneState.RINGING;
            case TelephonyManager.CALL_STATE_OFFHOOK:
            default:
                return PhoneState.UNKNOWN;
        }
    }

    private void postOnNewOutGoingCall(final Intent intent) {
        if (mPhoneEventListener != null) {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            mPhoneEventListener.onNewOutGoingCall(phoneNumber);
        }
    }

    private void postOnPhoneStateChanged(final Intent intent) {
        if (mPhoneEventListener != null) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            mPhoneEventListener.onPhoneStateChanged(state, phoneNumber);
        }
    }

    /**
     * Wi-Fi などのネットワーク情報を受信するブロードキャストレシーバー.
     */
    private final BroadcastReceiver mHostConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
                postOnNewOutGoingCall(intent);
            } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                postOnPhoneStateChanged(intent);
            }
        }
    };

    public interface PhoneEventListener {
        void onNewOutGoingCall(String phoneNumber);
        void onPhoneStateChanged(String state, String phoneNumber);
    }

    /**
     * パーミッション結果通知用コールバック.
     */
    public interface PermissionCallback {
        /**
         * 許可された場合に呼び出されます.
         */
        void onAllowed();

        /**
         * 拒否された場合に呼び出されます.
         */
        void onDisallowed();
    }

    public enum PhoneState {
        STANDBY("standby"),
        RINGING("ringing"),
        DIALING("dialing"),
        ACTIVE("active"),
        ON_HOLD("on-hold"),
        UNKNOWN("unknown");

        private final String mName;
        private String mPhoneNumber;

        PhoneState(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public String getPhoneNumber() {
            return mPhoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            mPhoneNumber = phoneNumber;
        }
    }
}
