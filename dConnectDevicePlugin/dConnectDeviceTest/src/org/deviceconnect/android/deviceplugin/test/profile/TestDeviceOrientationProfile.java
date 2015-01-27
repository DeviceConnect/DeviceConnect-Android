/*
 TestDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * JUnit用テストデバイスプラグイン、Connectプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class TestDeviceOrientationProfile extends DeviceOrientationProfile {

    /**
     * サービスIDをチェックする.
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return TestNetworkServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundDevice(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
    }

    @Override
    protected boolean onPutOnDeviceOrientation(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent intent = MessageUtils.createEventIntent();
            setSessionKey(intent, sessionKey);
            setDeviceID(intent, serviceId);
            setProfile(intent, getProfileName());
            setAttribute(intent, ATTRIBUTE_ON_DEVICE_ORIENTATION);
            
            Bundle orientation = new Bundle();
            Bundle a1 = new Bundle();
            setX(a1, 0.0);
            setY(a1, 0.0);
            setZ(a1, 0.0);
            Bundle a2 = new Bundle();
            setX(a2, 0.0);
            setY(a2, 0.0);
            setZ(a2, 0.0);
            Bundle r = new Bundle();
            setAlpha(r, 0.0);
            setBeta(r, 0.0);
            setGamma(r, 0.0);
            
            setAcceleration(orientation, a1);
            setAccelerationIncludingGravity(orientation, a2);
            setRotationRate(orientation, r);
            setInterval(orientation, 0);
            setOrientation(intent, orientation);
            Util.sendBroadcast(getContext(), intent);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDeviceOrientation(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

}
