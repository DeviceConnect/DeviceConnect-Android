/*
 HostPhoneProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.PhoneProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;

/**
 * Phoneプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostPhoneProfile extends PhoneProfile {

    /** Error. */
    private static final int ERROR_VALUE_IS_NULL = 100;

    @Override
    protected boolean onPostCall(final Intent request, final Intent response, final String serviceId,
            final String phoneNumber) {

        mLogger.entering(this.getClass().getName(), "onPostReceive", new Object[] {request, response});

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            if (phoneNumber != null) {
                try {
                    Uri uri = Uri.parse("tel:" + phoneNumber);
                    Intent intent = new Intent(Intent.ACTION_CALL, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.getContext().startActivity(intent);
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (Exception e) {
                    MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "phone app is not exist");
                    setResult(response, DConnectMessage.RESULT_ERROR);
                }

            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "phoneNumber is null");
                setResult(response, DConnectMessage.RESULT_ERROR);
            }

        }

        return true;
    }

    @Override
    protected boolean onPutSet(final Intent request, final Intent response, final String serviceId,
            final PhoneMode mode) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            this.getContext();
            // AudioManager
            AudioManager mAudioManager = (AudioManager) this.getContext().getSystemService(
                    Context.AUDIO_SERVICE);

            if (mode.equals(PhoneMode.SILENT)) {
                // サイレントモード
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (mode.equals(PhoneMode.SOUND)) {
                // 通常
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (mode.equals(PhoneMode.MANNER)) {
                // バイブレーションのみ
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (mode.equals(PhoneMode.UNKNOWN)) {
                setResult(response, DConnectMessage.RESULT_ERROR);
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
            setResult(response, DConnectMessage.RESULT_OK);

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);
            ((HostDeviceService) getContext()).setServiceId(serviceId);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
                return true;
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
            setResult(response, DConnectMessage.RESULT_OK);
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
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
        MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "SessionKey not found");
    }

}
