/*
 SystemProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.android.profile.spec.DConnectSpecConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

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

    private static final String KEY_PATHS = "paths";
    private static final String KEY_INFO = "info";
    private static final String KEY_DEFINITIONS = "definitions";
    private static final String KEY_RESPONSES = "responses";
    private static final String KEY_PARAMETERS = "parameters";
    private static final String KEY_X_EVENT = "x-event";
    private static final String KEY_SUMMARY = "summary";
    private static final String KEY_DESCRIPTION = "description";

    /**
     * Service Information API.
     */
    private final DConnectApi mServiceInformationApi = new GetApi() {

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            appendServiceInformation(response);
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

    protected void appendServiceInformation(final Intent response) {
        // connect
        Bundle connect = new Bundle();
        String networkType = getService().getNetworkType();
        boolean isOnline = getService().isOnline();
        switch (ServiceDiscoveryProfileConstants.NetworkType.getInstance(networkType)) {
            case WIFI:
                setWifiState(connect, isOnline);
                break;
            case BLUETOOTH:
                setBluetoothState(connect, isOnline);
                break;
            case BLE:
                setBLEState(connect, isOnline);
                break;
            case NFC:
                setNFCState(connect, isOnline);
                break;
            default:
                break;
        }
        setConnect(response, connect);

        // TODO: getXXXStateメソッドを削除する。

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
     */
    public static void setSupports(final Intent response, final List<String> supports) {
        setSupports(response, supports.toArray(new String[supports.size()]));
    }

    public static void setSupportApis(final Intent response, final List<DConnectProfile> profileList) {
        Bundle supportApisBundle = new Bundle();
        for (final DConnectProfile profile : profileList) {
            DConnectProfileSpec profileSpec = profile.getProfileSpec();
            if (profileSpec != null) {
                Bundle bundle = createSupportApisBundle(profileSpec, profile);

                // 送信しない情報はここで削除.
                reduceInformation(bundle);

                supportApisBundle.putBundle(profile.getProfileName(), bundle);
            }
        }
        response.putExtra(PARAM_SUPPORT_APIS, supportApisBundle);
    }

    private static Bundle createSupportApisBundle(final DConnectProfileSpec profileSpec,
                                                  final DConnectProfile profile) {
        Bundle tmpBundle = new Bundle(profileSpec.toBundle());
        Bundle pathsObj = tmpBundle.getBundle(KEY_PATHS);
        if (pathsObj == null) {
            return tmpBundle;
        }
        List<String> pathNames = new ArrayList<String>(pathsObj.keySet());
        for (String pathName : pathNames) {
            Bundle pathObj = pathsObj.getBundle(pathName);
            if (pathObj == null) {
                continue;
            }
            for (DConnectSpecConstants.Method method : DConnectSpecConstants.Method.values()) {
                String methodName = method.getName().toLowerCase();
                Bundle methodObj = pathObj.getBundle(methodName);
                if (methodObj == null) {
                    continue;
                }
                if (!profile.hasApi(pathName, method)) {
                    pathObj.remove(methodName);
                }
            }
            if (pathObj.size() == 0) {
                pathsObj.remove(pathName);
            }
        }
        return tmpBundle;
    }

    private static void reduceInformation(final Bundle supportApi) {
        Bundle infoObj = supportApi.getBundle(KEY_INFO);
        if (infoObj != null) {
            infoObj.remove(KEY_DESCRIPTION);
        }
        Bundle pathsObj = supportApi.getBundle(KEY_PATHS);
        if (pathsObj != null) {
            List<String> pathNames = new ArrayList<String>(pathsObj.keySet());
            for (String pathName : pathNames) {
                Bundle pathObj = pathsObj.getBundle(pathName);
                if (pathObj == null) {
                    continue;
                }
                for (DConnectSpecConstants.Method method : DConnectSpecConstants.Method.values()) {
                    String methodName = method.getName().toLowerCase();
                    Bundle methodObj = pathObj.getBundle(methodName);
                    if (methodObj == null) {
                        continue;
                    }
                    methodObj.remove(KEY_RESPONSES);
                    methodObj.remove(KEY_X_EVENT);
                    methodObj.remove(KEY_SUMMARY);
                    methodObj.remove(KEY_DESCRIPTION);

                    Bundle[] parameters = (Bundle[]) methodObj.getParcelableArray(KEY_PARAMETERS);
                    if (parameters != null) {
                        for (Bundle parameter : parameters) {
                            parameter.remove(KEY_DESCRIPTION);
                        }
                    }
                }
            }
        }
        supportApi.remove(KEY_DEFINITIONS);
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
