/*
 TestSystemProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * JUnit用テストデバイスプラグイン、Systemプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestSystemProfile extends SystemProfile {

    /**
     * バージョン.
     */
    public static final String VERSION = "1.0";
    
    /**
     * デバイスIDをチェックする.
     * @param deviceId デバイスID
     * @return <code>deviceId</code>がテスト用デバイスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkdeviceId(final String deviceId) {
        return TestNetworkServiceDiscoveryProfile.DEVICE_ID.equals(deviceId);
    }

    /**
     * デバイスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptydeviceId(final Intent response) {
        MessageUtils.setEmptyDeviceIdError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundDevice(final Intent response) {
        MessageUtils.setNotFoundDeviceError(response);
    }

    /**
     * コンストラクタ.
     * @param plugin プラグイン
     */
    public TestSystemProfile(final DConnectProfileProvider plugin) {
        super(plugin);
    }

    @Override
    protected boolean onGetDevice(final Intent request, final Intent response, final String deviceId) {
        
        if (deviceId == null) {
            createEmptydeviceId(response);
        } else if (!checkdeviceId(deviceId)) {
            createNotFoundDevice(response);
        } else {
            setVersion(response, VERSION);
            setSupports(response, new String[]{});
            Bundle connect = new Bundle();
            setWifiState(connect, false);
            setBluetoothState(connect, false);
            setNFCState(connect, false);
            setBLEState(connect, false);
            setConnect(response, connect);
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onPutWakeup(final Intent request, final Intent response, final String pluginId) {
        // /system/device/wakeupはテスト用デバイスプラグインでは疎通確認だけを行う.
        // 正常に設定画面が開かれることの確認は、実際のデバイスプラグインのテストで行う.
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onDeleteEvents(final Intent request, final Intent response, final String sessionKey) {
        boolean removed = EventManager.INSTANCE.removeEvents(sessionKey);
        if (removed) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setUnknownError(response, "Failed to remove events.");
        }
        return true;
    }

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return null; // テスト用プラグインでは実装しない
    }
}
