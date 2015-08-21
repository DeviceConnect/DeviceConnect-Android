/*
 HostPhoneProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.activity.PermissionRequestActivity;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.PhoneProfile;
import org.deviceconnect.message.DConnectMessage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

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

    @Override
    protected boolean onPostCall(final Intent request, final Intent response, final String serviceId,
            final String phoneNumber) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            if (phoneNumber != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String[] requiredPermissions = new String[] { Manifest.permission.CALL_PHONE };
                    final List<String> missingPermissions = new ArrayList<>();
                    for (String requiredPermission : requiredPermissions) {
                        if (getContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                            missingPermissions.add(requiredPermission);
                        }
                    }
                    if (missingPermissions.size() != 0) {
                        PermissionRequestActivity.requestPermissions(getContext(),
                                missingPermissions.toArray(new String[missingPermissions.size()]),
                                new ResultReceiver(new Handler(Looper.getMainLooper())) {
                                    @Override
                                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                                        String[] permissions = resultData
                                                .getStringArray(PermissionRequestActivity.EXTRA_PERMISSIONS);
                                        int[] grantResults = resultData
                                                .getIntArray(PermissionRequestActivity.EXTRA_GRANT_RESULTS);

                                        if (permissions == null || permissions.length != missingPermissions.size()
                                                || grantResults == null
                                                || grantResults.length != missingPermissions.size()) {
                                            MessageUtils.setIllegalServerStateError(response,
                                                    "CALL_PHONE permission not granted.");
                                            getContext().sendBroadcast(response);
                                            return;
                                        }

                                        int count = missingPermissions.size();
                                        for (String requiredPermission : missingPermissions) {
                                            for (int i = 0; i < permissions.length; ++i) {
                                                if (permissions[i].equals(requiredPermission)) {
                                                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                                        --count;
                                                    } else {
                                                        MessageUtils.setIllegalServerStateError(response,
                                                                "CALL_PHONE permission not granted.");
                                                        getContext().sendBroadcast(response);
                                                        return;
                                                    }
                                                }
                                            }
                                        }

                                        if (count == 0) {
                                            onPostCallInternal(request, response, phoneNumber);
                                            getContext().sendBroadcast(response);
                                        } else {
                                            MessageUtils.setIllegalServerStateError(response,
                                                    "CALL_PHONE permission not granted.");
                                            getContext().sendBroadcast(response);
                                            return;
                                        }
                                    }
                                });
                        return false;
                    }
                }
                onPostCallInternal(request, response, phoneNumber);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
            }
        }
        return true;
    }

    private void onPostCallInternal(final Intent request, final Intent response, final String phoneNumber) {
        try {
            if (!checkPhoneNumber(phoneNumber)) {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
                return;
            }

            Uri uri = Uri.parse("tel:" + phoneNumber);
            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_CALL, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "phoneNumber is invalid.");
            }
        } catch (Throwable throwable) {
            MessageUtils.setUnknownError(response, "Failed to make a phone call.");
        }
    }

    @Override
    protected boolean onPutSet(final Intent request, final Intent response, final String serviceId,
            final PhoneMode mode) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
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
        return true;
    }

    @Override
    protected boolean onPutOnConnect(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                ((HostDeviceService) getContext()).setServiceId(serviceId);
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnConnect(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
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
        }

        return true;
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = HostServiceDiscoveryProfile.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);

        return match.find();
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    /**
     * セッションキーが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response, "SessionKey not found");
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
}
