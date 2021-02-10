/*
 HostPhoneProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.telephony.TelephonyManager;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.phone.HostPhoneManager;
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

import java.util.List;
import java.util.logging.Logger;

/**
 * Phoneプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostPhoneProfile extends PhoneProfile {

    /** Notification Id */
    private static final int NOTIFICATION_ID = 3537;

    /**
     * 現在の通話状態.
     */
    private HostPhoneManager.PhoneState mCurrentPhoneState;

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("host.dplugin");

    private boolean doPrivileged(final Runnable action, final Intent response) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mHostPhoneManager.requestPermissions(new HostPhoneManager.PermissionCallback() {
                @Override
                public void onAllowed() {
                    action.run();
                    sendResponse(response);
                }
                @Override
                public void onDisallowed() {
                    MessageUtils.setIllegalServerStateError(response, "CALL_PHONE permission not granted.");
                    sendResponse(response);

                }
            });
            return false;
        } else {
            action.run();
            return true;
        }
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
                return doPrivileged(() -> onPostCallInternal(request, response, phoneNumber), response);
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
                            if (!mHostPhoneManager.setPhoneMode(mode)) {
                                MessageUtils.setInvalidRequestParameterError(response, "mode is invalid.");
                            } else {
                                setResult(response, DConnectMessage.RESULT_OK);
                            }
                        } else {
                            MessageUtils.setIllegalServerStateError(response, "PHOME_MODE setting permisson not granted");
                        }
                        sendResponse(response);
                    }
                });
                return false;
            } else {
                if (!mHostPhoneManager.setPhoneMode(mode)) {
                    MessageUtils.setInvalidRequestParameterError(response, "mode is invalid.");
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        }
    };

    private void requestNotificationPolicyPermission(final ResultReceiver resultReceiver) {
        Intent intent = new Intent(android.provider.Settings
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

    private final DConnectApi mGetPhoneStateApi = new GetApi() {
        @Override
        public String getAttribute() {
            return "callState";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setResult(response, IntentDConnectMessage.RESULT_OK);
            HostPhoneManager.PhoneState currentState = mCurrentPhoneState;
            response.putExtra("state", currentState.getName());
            response.putExtra("phoneNumber", currentState.getPhoneNumber());
            return true;
        }
    };

    private final DConnectApi mPutOnPhoneStateChangeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return "onPhoneStateChange";
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

    private final DConnectApi mDeleteOnPhoneStateChangeApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return "onPhoneStateChange";
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
                mHostPhoneManager.acceptRingingCall();
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
                mHostPhoneManager.endCall();
                setResult(response, IntentDConnectMessage.RESULT_OK);
            }, response);
        }
    };

    private HostPhoneManager mHostPhoneManager;

    /**
     * コンストラクタ.
     */
    public HostPhoneProfile(final HostPhoneManager hostPhoneManager) {
        mHostPhoneManager = hostPhoneManager;
        mHostPhoneManager.setPhoneEventListener(new HostPhoneManager.PhoneEventListener() {
            @Override
            public void onNewOutGoingCall(String phoneNumber) {
                mLogger.info("onNewOutGoingCall");
                HostPhoneManager.PhoneState nextState = HostPhoneManager.PhoneState.DIALING;
                nextState(nextState, phoneNumber);
                sendOnConnectEvent(phoneNumber);
            }

            @Override
            public void onPhoneStateChanged(String state, String phoneNumber) {
                mLogger.info("onPhoneStateChanged: state=" + state + " phoneNumber=" + phoneNumber);

                HostPhoneManager.PhoneState currentState = mCurrentPhoneState;
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                    nextState(HostPhoneManager.PhoneState.STANDBY);
                } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && phoneNumber != null) {
                    if (currentState == HostPhoneManager.PhoneState.RINGING ||
                            currentState == HostPhoneManager.PhoneState.DIALING) {
                        nextState(HostPhoneManager.PhoneState.ACTIVE, phoneNumber);
                    }
                } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && phoneNumber != null) {
                    nextState(HostPhoneManager.PhoneState.RINGING, phoneNumber);
                }
            }
        });
        mCurrentPhoneState = mHostPhoneManager.detectCurrentPhoneState();

        addApi(mPostCallApi);
        addApi(mPutSetApi);
        addApi(mPutOnConnectApi);
        addApi(mDeleteOnConnectApi);

        addApi(mGetPhoneStateApi);
        addApi(mPutOnPhoneStateChangeApi);
        addApi(mDeleteOnPhoneStateChangeApi);

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
            if (!mHostPhoneManager.checkPhoneNumber(phoneNumber)) {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
                return;
            }

            Uri uri = Uri.parse("tel:" + phoneNumber);
            if (uri != null) {
                mHostPhoneManager.call(uri);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
            }
        } catch (Throwable throwable) {
            MessageUtils.setUnknownError(response, "Failed to make a phone call.");
        }
    }

    private void nextState(final HostPhoneManager.PhoneState nextState, final String phoneNumber) {
        mLogger.info("nextState: " + nextState.getName() + " - " + phoneNumber);
        nextState.setPhoneNumber(phoneNumber);
        mCurrentPhoneState = nextState;
        sendOnPhoneStateChangeEvent(nextState, phoneNumber);
    }

    private void nextState(final HostPhoneManager.PhoneState nextState) {
        nextState(nextState, "");
    }

    private void sendOnPhoneStateChangeEvent(final HostPhoneManager.PhoneState state, final String phoneNumber) {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                HostPhoneProfile.PROFILE_NAME, null, "onPhoneStateChange");

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent intent = EventManager.createEventMessage(event);
            HostPhoneProfile.setAttribute(intent, "onPhoneStateChange");
            intent.putExtra("state", state.getName());
            intent.putExtra("phoneNumber", phoneNumber);
            sendEvent(intent, event.getAccessToken());
        }
    }

    private void sendOnConnectEvent(String phoneNumber) {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                HostPhoneProfile.PROFILE_NAME, null, HostPhoneProfile.ATTRIBUTE_ON_CONNECT);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostPhoneProfile.setAttribute(mIntent, HostPhoneProfile.ATTRIBUTE_ON_CONNECT);
            Bundle phoneStatus = new Bundle();
            HostPhoneProfile.setPhoneNumber(phoneStatus, phoneNumber);
            HostPhoneProfile.setState(phoneStatus, PhoneProfileConstants.CallState.START);
            HostPhoneProfile.setPhoneStatus(mIntent, phoneStatus);
            sendEvent(mIntent, event.getAccessToken());
        }
    }
}
