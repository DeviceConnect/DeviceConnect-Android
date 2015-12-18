/*
 TestVibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;

/**
 * JUnit用テストデバイスプラグイン、Vibrationプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestVibrationProfile extends VibrationProfile {

    /**
     * デバイスをチェックする.
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
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
    private void createNotFoundService(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
    }

    @Override
    protected boolean onPutVibrate(final Intent request, final Intent response, final String serviceId, 
            final long[] pattern) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteVibrate(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }
}
