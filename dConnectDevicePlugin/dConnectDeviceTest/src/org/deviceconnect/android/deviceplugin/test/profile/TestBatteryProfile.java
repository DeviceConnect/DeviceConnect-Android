/*
 TestBatteryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * JUnit用テストデバイスプラグイン、Batteryプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class TestBatteryProfile extends BatteryProfile {
    /**
     * バッテリー充電時間を定義する.
     */
    public static final double CHARGING_TIME = 50000;

    /**
     * バッテリー放電時間を定義する.
     */
    public static final double DISCHARGING_TIME = 10000;

    /**
     * バッテリーレベルを定義する.
     */
    public static final double LEVEL = 0.5;

    /**
     * バッテリー充電フラグを定義する.
     */
    public static final boolean CHARGING = false;

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response, "Service ID is empty.");
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response, "Service is not found.");
    }

    @Override
    protected boolean onGetAll(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setCharging(response, CHARGING);
            setChargingTime(response, CHARGING_TIME);
            setDischargingTime(response, DISCHARGING_TIME);
            setLevel(response, LEVEL);
        }
        return true;
    }

    @Override
    protected boolean onGetCharging(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setCharging(response, CHARGING);
        }
        return true;
    }

    @Override
    protected boolean onGetDischargingTime(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setDischargingTime(response, DISCHARGING_TIME);
        }
        return true;
    }

    @Override
    protected boolean onGetChargingTime(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setChargingTime(response, CHARGING_TIME);
        }
        return true;
    }

    @Override
    protected boolean onGetLevel(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setLevel(response, LEVEL);
        }
        return true;
    }

    @Override
    protected boolean onPutOnChargingChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            
            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_CHARGING_CHANGE);
            Bundle charging = new Bundle();
            setCharging(charging, false);
            setBattery(message, charging);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnChargingChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnBatteryChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_BATTERY_CHANGE);
            Bundle battery = new Bundle();
            setChargingTime(battery, CHARGING_TIME);
            setDischargingTime(battery, DISCHARGING_TIME);
            setLevel(battery, LEVEL);
            setBattery(message, battery);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnBatteryChange(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

}
