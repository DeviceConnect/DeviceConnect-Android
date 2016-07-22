/*
 SystemProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.spec.DConnectApiSpecFilter;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

import java.util.List;

/**
 * Service Information プロファイル.
 * 
 * <p>
 * サービス情報を提供するAPI.
 * </p>
 * 
 * @author NTT DOCOMO, INC.
 */
public class ServiceInformationProfile extends DConnectProfile implements ServiceInformationProfileConstants {

    /**
     * 設定画面起動用IntentのパラメータオブジェクトのExtraキー.
     */
    public static final String SETTING_PAGE_PARAMS = "org.deviceconnect.profile.system.setting_params";

    /**
     * Service Information API.
     */
    private final DConnectApi mServiceInformationApi = new GetApi() {

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getService().getId();

            // connect
            Bundle connect = new Bundle();
            setWifiState(connect, getWifiState(serviceId));
            setBluetoothState(connect, getBluetoothState(serviceId));
            setNFCState(connect, getNFCState(serviceId));
            setBLEState(connect, getBLEState(serviceId));
            setConnect(response, connect);

            // version
            setVersion(response, getCurrentVersionName());

            // supports, supportApis
            List<DConnectProfile> profileList = getService().getProfileList();
            String[] profileNames = new String[profileList.size()];
            int i = 0;
            for (DConnectProfile profile : profileList) {
                profileNames[i++] = profile.getProfileName();
            }
            setSupports(response, profileNames);
            setSupportApis(response, profileList);

            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    /**
     * ServiceInformationプロファイルを生成する.
     */
    public ServiceInformationProfile() {
        addApi(mServiceInformationApi);
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    /**
     * AndroidManifest.xmlのversionNameを取得する.
     * 
     * @return バージョン名
     */
    private String getCurrentVersionName() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return "Unknown";
        }
    }

    /**
     * WiFiの接続状態を取得する.
     * 
     * @param serviceId サービスID
     * @return WiFiの接続状態
     * @see ConnectState
     */
    protected ConnectState getWifiState(final String serviceId) {
        return ConnectState.NONE;
    }

    /**
     * Bluetoothの接続状態を取得する.
     * 
     * @param serviceId サービスID
     * @return Bluetoothの接続状態
     * @see ConnectState
     */
    protected ConnectState getBluetoothState(final String serviceId) {
        return ConnectState.NONE;
    }

    /**
     * NFCの接続状態を取得する.
     * 
     * @param serviceId サービスID
     * @return NFCの接続状態
     * @see ConnectState
     */
    protected ConnectState getNFCState(final String serviceId) {
        return ConnectState.NONE;
    }

    /**
     * BLEの接続状態を取得する.
     * 
     * @param serviceId サービスID
     * @return BLEの接続状態
     * @see ConnectState
     */
    protected ConnectState getBLEState(final String serviceId) {
        return ConnectState.NONE;
    }

    // ------------------------------------
    // レスポンスセッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにバージョンを格納する.
     * 
     * @param response レスポンスパラメータ
     * @param version バージョン
     */
    public static void setVersion(final Intent response, final String version) {
        response.putExtra(PARAM_VERSION, version);
    }

    /**
     * レスポンスにサポートしているI/Fの一覧を格納する.
     * 
     * @param response レスポンスパラメータ
     * @param supports サポートしているI/F一覧
     * @see #setSupportApis(Intent, List)
     */
    public static void setSupports(final Intent response, final String[] supports) {
        response.putExtra(PARAM_SUPPORTS, supports);
    }

    /**
     * レスポンスにサポートしているI/Fの一覧を格納する.
     * 
     * @param response レスポンスパラメータ
     * @param supports サポートしているI/F一覧
     * @deprecated
     * @see #setSupportApis(Intent, List)
     */
    public static void setSupports(final Intent response, final List<String> supports) {
        setSupports(response, supports.toArray(new String[supports.size()]));
    }

    public static void setSupportApis(final Intent response, final List<DConnectProfile> profileList) {
        Bundle supportApisBundle = new Bundle();
        for (final DConnectProfile profile : profileList) {
            DConnectProfileSpec profileSpec = profile.getProfileSpec();
            if (profileSpec != null) {
                Bundle bundle = profileSpec.toBundle(new DConnectApiSpecFilter() {
                    @Override
                    public boolean filter(final String path, final Method method) {
                        return profile.hasApi(path, method);
                    }
                });
                supportApisBundle.putBundle(profile.getProfileName(), bundle);
            }
        }
        response.putExtra(PARAM_SUPPORT_APIS, supportApisBundle);
    }

    /**
     * レスポンスにデバイスの接続状態を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param connect デバイスの接続状態
     */
    public static void setConnect(final Intent response, final Bundle connect) {
        response.putExtra(PARAM_CONNECT, connect);
    }

    /**
     * 指定されたパラメータに接続状態を設定する.
     * 
     * @param b バンドルオブジェクト
     * @param key パラメータキー
     * @param state 状態
     */
    private static void setConnectionState(final Bundle b, final String key, final ConnectState state) {
        // 非対応のものは省略
        switch (state) {
        case ON:
            b.putBoolean(key, true);
            break;
        case OFF:
            b.putBoolean(key, false);
            break;
        default:
            break;
        }
    }

    /**
     * デバイスプラグインの接続状態にWiFiの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param connecting true:ON、false:OFF
     */
    public static void setWifiState(final Bundle connect, final boolean connecting) {
        connect.putBoolean(PARAM_WIFI, connecting);
    }

    /**
     * デバイスプラグインの接続状態にWiFiの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param state 接続状態
     */
    public static void setWifiState(final Bundle connect, final ConnectState state) {
        setConnectionState(connect, PARAM_WIFI, state);
    }

    /**
     * デバイスプラグインの接続状態にBluetoothの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param connecting true:ON、false:OFF
     */
    public static void setBluetoothState(final Bundle connect, final boolean connecting) {
        connect.putBoolean(PARAM_BLUETOOTH, connecting);
    }

    /**
     * デバイスプラグインの接続状態にBluetoothの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param state 接続状態
     */
    public static void setBluetoothState(final Bundle connect, final ConnectState state) {
        setConnectionState(connect, PARAM_BLUETOOTH, state);
    }

    /**
     * デバイスプラグインの接続状態にNFCの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param connecting true:ON、false:OFF
     */
    public static void setNFCState(final Bundle connect, final boolean connecting) {
        connect.putBoolean(PARAM_NFC, connecting);
    }

    /**
     * デバイスプラグインの接続状態にNFCの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param state 接続状態
     */
    public static void setNFCState(final Bundle connect, final ConnectState state) {
        setConnectionState(connect, PARAM_NFC, state);
    }

    /**
     * デバイスプラグインの接続状態にBLEの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param connecting true:ON、false:OFF
     */
    public static void setBLEState(final Bundle connect, final boolean connecting) {
        connect.putBoolean(PARAM_BLE, connecting);
    }

    /**
     * デバイスプラグインの接続状態にBLEの接続状態を設定する.
     * 
     * @param connect デバイスプラグインの接続状態パラメータ
     * @param state 接続状態
     */
    public static void setBLEState(final Bundle connect, final ConnectState state) {
        setConnectionState(connect, PARAM_BLE, state);
    }

}
