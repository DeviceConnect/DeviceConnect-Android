/*
 HostPhoneProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.PhoneProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.PhoneProfileConstants;

/**
 * Phoneプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostPhoneProfile extends PhoneProfile {

    /**
     * 電話番号のサイズ.
     */
    private static final int MAX_PHONE_NUMBER_SIZE = 13;

    private final DConnectApi mPostCallApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_CALL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String phoneNumber = getPhoneNumber(request);
            if (phoneNumber != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                        new String[] { Manifest.permission.CALL_PHONE, Manifest.permission.PROCESS_OUTGOING_CALLS },
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                onPostCallInternal(request, response, phoneNumber);
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
                onPostCallInternal(request, response, phoneNumber);
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

                Intent intent = new Intent(
                        android.provider.Settings
                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                IntentHandlerActivity.startActivityForResult(getContext(), intent,
                        new ResultReceiver(new Handler(Looper.getMainLooper())) {
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

    public HostPhoneProfile() {
        addApi(mPostCallApi);
        addApi(mPutSetApi);
        addApi(mPutOnConnectApi);
        addApi(mDeleteOnConnectApi);
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
        getContext().startActivity(intent);
    }
}
