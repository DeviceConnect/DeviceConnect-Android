/*
 TestProximityProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * JUnit用テストデバイスプラグイン、Proximityプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestProximityProfile extends ProximityProfile {

    /**
     * 距離.
     */
    public static final double VALUE = 0;

    /**
     * 距離の最小値.
     */
    public static final double MIN = 0;

    /**
     * 距離の最大値.
     */
    public static final double MAX = 0;

    /**
     * 距離の閾値.
     */
    public static final double THRESHOLD = 0;

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
     * セッションキーが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response);
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
    protected boolean onGetOnDeviceProximity(final Intent request, final Intent response,
            final String serviceId) {
        setResult(response, DConnectMessage.RESULT_OK);
        setDeviceProximity(response);
        return true;
    }

    @Override
    protected boolean onPutOnDeviceProximity(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_DEVICE_PROXIMITY);
            setDeviceProximity(message);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDeviceProximity(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnUserProximity(final Intent request, final Intent response,
            final String serviceId) {
        setResult(response, DConnectMessage.RESULT_OK);
        setUserProximity(response);
        return true;
    }

    @Override
    protected boolean onPutOnUserProximity(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            
            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_USER_PROXIMITY);
            setUserProximity(message);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnUserProximity(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    /**
     * メッセージにテスト用データを設定する.
     * @param message メッセージ
     */
    private static void setDeviceProximity(final Intent message) {
        Bundle proximity = new Bundle();
        setValue(proximity, VALUE);
        setMin(proximity, MIN);
        setMax(proximity, MAX);
        setThreshold(proximity, THRESHOLD);
        setProximity(message, proximity);
    }

    /**
     * メッセージにテスト用データを設定する.
     * @param message メッセージ
     */
    private static void setUserProximity(final Intent message) {
        Bundle proximity = new Bundle();
        setNear(proximity, true);
        setProximity(message, proximity);
    }

}
