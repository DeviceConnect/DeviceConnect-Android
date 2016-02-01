/*
 TestSettingsProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SettingsProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;

/**
 * JUnit用テストデバイスプラグイン、Settingsプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestSettingsProfile extends SettingsProfile {
    /**
     * レベル.
     */
    public static final double LEVEL = 0.5;

    /**
     * 日時.
     */
    public static final String DATE = "2014-01-01T01:01:01+09:00";
    
    /**
     * サービスIDをチェックする.
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkserviceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyserviceId(final Intent response) {
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

    @Override
    protected boolean onGetSoundVolume(final Intent request, final Intent response, final String serviceId,
            final VolumeKind kind) {
        
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (kind == null || kind == VolumeKind.UNKNOWN) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setVolumeLevel(response, LEVEL);
        }
        
        return true;
    }

    @Override
    protected boolean onGetDate(final Intent request, final Intent response, final String serviceId) {
        
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setDate(response, DATE);
        }
        
        return true;
    }

    @Override
    protected boolean onGetDisplayLight(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setLightLevel(response, LEVEL);
        }
        
        return true;
    }

    @Override
    protected boolean onGetDisplaySleep(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setTime(response, 1);
        }
        return true;
    }

    @Override
    protected boolean onPutSoundVolume(final Intent request, final Intent response, final String serviceId,
            final VolumeKind kind, final Double level) {
        
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (kind == null || kind == VolumeKind.UNKNOWN
                || level == null || level < MIN_LEVEL || level > MAX_LEVEL) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutDate(final Intent request, final Intent response,
          final String serviceId, final String date) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (date == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onPutDisplayLight(final Intent request, final Intent response,
                                            final String serviceId, final Double level) {
        
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (level == null || level < MIN_LEVEL || level > MAX_LEVEL) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutDisplaySleep(final Intent request, final Intent response,
                                                final String serviceId, final Integer time) {
        
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (time == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }
}
