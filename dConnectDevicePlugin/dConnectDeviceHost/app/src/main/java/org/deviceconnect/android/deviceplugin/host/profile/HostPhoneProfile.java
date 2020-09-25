/*
 HostPhoneProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;

import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.PhoneProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.PhoneProfileConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Phoneプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostPhoneProfile extends PhoneProfile {

    private static final String[] PERMISSIONS;
    static {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        if (checkMinSdkVersion(Build.VERSION_CODES.O)) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        if (checkMinSdkVersion(Build.VERSION_CODES.P)) {
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

    /**
     * 現在の通話状態.
     */
    private CallState mCurrentCallState;

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("host.dplugin");

    private boolean doPrivileged(final Runnable action, final Intent response) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                    PERMISSIONS,
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            action.run();
                            sendResponse(response);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            MessageUtils.setIllegalServerStateError(response,
                                    "CALL_PHONE permission not granted.");
                            sendResponse(response);
                        }
                    });
            return false;
        }
        action.run();
        return true;
    }

    private final DConnectApi mPostCallApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_CALL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String phoneNumber = getPhoneNumber(request);
            if (phoneNumber != null) {
                return doPrivileged(new Runnable() {
                    @Override
                    public void run() {
                        onPostCallInternal(request, response, phoneNumber);
                    }
                }, response);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
            }
            return true;
        }
    };

    private final DConnectApi mPutSetApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_SET;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final PhoneMode mode = getMode(request);
            final NotificationManager notificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !notificationManager.isNotificationPolicyAccessGranted()) {

                requestNotificationPolicyPermission(new ResultReceiver(new Handler(Looper.getMainLooper())) {
                    @Override
                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                        if (notificationManager.isNotificationPolicyAccessGranted()) {
                            setPhoneMode(response, mode);
                        } else {
                            MessageUtils.setIllegalServerStateError(response,
                                    "PHOME_MODE setting permisson not granted");
                        }
                        sendResponse(response);
                    }
                });
                return false;
            }
            setPhoneMode(response, mode);

            return true;
        }
    };

    private void requestNotificationPolicyPermission(final ResultReceiver resultReceiver) {
        Intent intent = new Intent(
                android.provider.Settings
                        .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

        Intent callIntent = new Intent(getContext(), IntentHandlerActivity.class);
        callIntent.putExtra("EXTRA_INTENT", intent);
        callIntent.putExtra("EXTRA_CALLBACK", resultReceiver);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NotificationUtils.createNotificationChannel(getContext());
            NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, callIntent,
                    getContext().getString(R.string.host_notification_setting_warnning));
        } else {
            getContext().startActivity(callIntent);
        }
    }


    private void setPhoneMode(Intent response, PhoneMode mode) {
        // AudioManager
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (mode.equals(PhoneMode.SILENT)) {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (mode.equals(PhoneMode.SOUND)) {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (mode.equals(PhoneMode.MANNER)) {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (mode.equals(PhoneMode.UNKNOWN)) {
            MessageUtils.setInvalidRequestParameterError(response, "mode is invalid.");
        }
    }

    private final DConnectApi mPutOnConnectApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CONNECT;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                switch (error) {
                    case FAILED:
                        MessageUtils.setUnknownError(response, "Do not unregister event.");
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    case NOT_FOUND:
                        MessageUtils.setUnknownError(response, "Event not found.");
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnConnectApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CONNECT;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                switch (error) {
                    case FAILED:
                        MessageUtils.setUnknownError(response, "Do not unregister event.");
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    case NOT_FOUND:
                        MessageUtils.setUnknownError(response, "Event not found.");
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
            }
            return true;
        }
    };

    private final DConnectApi mGetCallStateApi = new GetApi() {

        @Override
        public String getAttribute() {
            return "callState";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setResult(response, IntentDConnectMessage.RESULT_OK);
            CallState currentState = mCurrentCallState;
            response.putExtra("state", currentState.getName());
            response.putExtra("phoneNumber", currentState.getPhoneNumber());
            return true;
        }
    };

    private final DConnectApi mPutOnCallStateChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return "onCallStateChange";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doPrivileged(() -> {
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (error == EventError.NONE) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    switch (error) {
                        case FAILED:
                            MessageUtils.setUnknownError(response, "Do not unregister event.");
                            break;
                        case INVALID_PARAMETER:
                            MessageUtils.setInvalidRequestParameterError(response);
                            break;
                        case NOT_FOUND:
                            MessageUtils.setUnknownError(response, "Event not found.");
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                }
            }, response);
        }
    };

    private final DConnectApi mDeleteOnCallStateChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return "onCallStateChange";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                switch (error) {
                    case FAILED:
                        MessageUtils.setUnknownError(response, "Do not unregister event.");
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    case NOT_FOUND:
                        MessageUtils.setUnknownError(response, "Event not found.");
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
            }
            return true;
        }
    };

    private final DConnectApi mPostAcceptCallApi = new PostApi() {

        @Override
        public String getAttribute() {
            return "acceptCall";
        }

        @SuppressWarnings("MissingPermission")
        @TargetApi(26)
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doPrivileged(() -> {
                TelecomManager telecomMgr = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
                telecomMgr.acceptRingingCall();
                setResult(response, IntentDConnectMessage.RESULT_OK);
            }, response);
        }
    };

    private final DConnectApi mPostRejectCallApi = new PostApi() {

        @Override
        public String getAttribute() {
            return "rejectCall";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return mPostEndCallApi.onRequest(request, response);
        }
    };

    private final DConnectApi mPostEndCallApi = new PostApi() {

        @Override
        public String getAttribute() {
            return "endCall";
        }

        @SuppressWarnings("MissingPermission")
        @TargetApi(28)
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doPrivileged(() -> {
                TelecomManager telecomMgr = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
                telecomMgr.endCall();
                setResult(response, IntentDConnectMessage.RESULT_OK);
            }, response);
        }
    };

    /**
     * コンストラクタ.
     */
    public HostPhoneProfile(final TelephonyManager telephonyManager) {
        mCurrentCallState = detectCurrentCallState(telephonyManager);

        addApi(mPostCallApi);
        addApi(mPutSetApi);
        addApi(mPutOnConnectApi);
        addApi(mDeleteOnConnectApi);

        addApi(mGetCallStateApi);
        addApi(mPutOnCallStateChangeApi);
        addApi(mDeleteOnCallStateChangeApi);
        if (checkMinSdkVersion(Build.VERSION_CODES.O)) {
            addApi(mPostAcceptCallApi);
        }
        if (checkMinSdkVersion(Build.VERSION_CODES.P)) {
            addApi(mPostRejectCallApi);
            addApi(mPostEndCallApi);
        }
    }

    private static boolean checkMinSdkVersion(final int minLevel) {
        return Build.VERSION.SDK_INT >= minLevel;
    }

    private void onPostCallInternal(final Intent request, final Intent response, final String phoneNumber) {
        try {
            if (!checkPhoneNumber(phoneNumber)) {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
                return;
            }

            Uri uri = Uri.parse("tel:" + phoneNumber);
            if (uri != null) {
                call(uri);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
            }
        } catch (Throwable throwable) {
            MessageUtils.setUnknownError(response, "Failed to make a phone call.");
        }
    }

    /**
     * 電話番号のフォーマットチェックを行う.
     * 
     * @param phoneNumber 電話番号
     * @return 電話番号の場合はtrue、それ以外はfalse
     */
    private boolean checkPhoneNumber(final String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        if (phoneNumber.length() > MAX_PHONE_NUMBER_SIZE) {
            return false;
        }
        String pattern = "[0-9+]+";
        return phoneNumber.matches(pattern);
    }

    private void call(final Uri uri) {
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            this.getContext().startActivity(intent);
        } else {
            NotificationUtils.createNotificationChannel(getContext());
            NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, intent,
                    getContext().getString(R.string.host_notification_phone_warnning));
        }
    }

    public void onNewOutGoingCall(final Intent intent) {
        mLogger.info("onNewOutGoingCall");
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        CallState nextState = CallState.DIALING;
        nextState(nextState, phoneNumber);

        sendOnConnectEvent(intent);
    }

    public void onPhoneStateChanged(final Intent intent) {
        CallState currentState = mCurrentCallState;
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        mLogger.info("onPhoneStateChanged: state=" + state + " phoneNumber=" + phoneNumber);

        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            nextState(CallState.STANDBY);
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && phoneNumber != null) {
            if (currentState == CallState.RINGING || currentState == CallState.DIALING) {
                nextState(CallState.ACTIVE, phoneNumber);
            }
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && phoneNumber != null) {
            nextState(CallState.RINGING, phoneNumber);
        }
    }

    private void nextState(final CallState nextState, final String phoneNumber) {
        mLogger.info("nextState: " + nextState.getName() + " - " + phoneNumber);
        nextState.setPhoneNumber(phoneNumber);
        mCurrentCallState = nextState;
        sendOnCallStateChangeEvent(nextState, phoneNumber);
    }

    private void nextState(final CallState nextState) {
        nextState(nextState, "");
    }

    private void sendOnCallStateChangeEvent(final CallState state, final String phoneNumber) {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID, "phone", null,
                "onCallStateChange");

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent intent = EventManager.createEventMessage(event);
            HostPhoneProfile.setAttribute(intent, "onCallStateChange");
            intent.putExtra("state", state.getName());
            intent.putExtra("phoneNumber", phoneNumber);
            sendEvent(intent, event.getAccessToken());
        }
    }

    private void sendOnConnectEvent(final Intent intent) {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID, HostPhoneProfile.PROFILE_NAME, null,
                HostPhoneProfile.ATTRIBUTE_ON_CONNECT);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostPhoneProfile.setAttribute(mIntent, HostPhoneProfile.ATTRIBUTE_ON_CONNECT);
            Bundle phoneStatus = new Bundle();
            HostPhoneProfile.setPhoneNumber(phoneStatus, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
            HostPhoneProfile.setState(phoneStatus, PhoneProfileConstants.CallState.START);
            HostPhoneProfile.setPhoneStatus(mIntent, phoneStatus);
            sendEvent(mIntent, event.getAccessToken());
        }
    }

    private CallState detectCurrentCallState(final TelephonyManager telephonyManager) {
        int state = telephonyManager.getCallState();
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                return CallState.STANDBY;
            case TelephonyManager.CALL_STATE_RINGING:
                return CallState.RINGING;
            case TelephonyManager.CALL_STATE_OFFHOOK:
            default:
                return CallState.UNKNOWN;
        }
    }

    private enum CallState {

        STANDBY("standby"),
        RINGING("ringing"),
        DIALING("dialing"),
        ACTIVE("active"),
        ON_HOLD("on-hold"),
        UNKNOWN("unknown");

        private final String mName;
        private String mPhoneNumber;

        CallState(final String name) {
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
