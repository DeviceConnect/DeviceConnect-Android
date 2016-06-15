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

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectRequestParamSpec;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Information プロファイル.
 * 
 * <p>
 * サービス情報を提供するAPI.<br>
 * サービス情報を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * System Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * <ul>
 * <li>System API [GET] :
 * {@link ServiceInformationProfile#onGetInformation(Intent, Intent, String)}</li>
 * </ul>
 * 
 * @author NTT DOCOMO, INC.
 */
public abstract class ServiceInformationProfile extends DConnectProfile implements ServiceInformationProfileConstants {

    /**
     * 設定画面起動用IntentのパラメータオブジェクトのExtraキー.
     */
    public static final String SETTING_PAGE_PARAMS = "org.deviceconnect.profile.system.setting_params";

    /**
     * プロファイルプロバイダー.
     */
    private final DConnectProfileProvider mProvider;

    /**
     * Service Information API.
     */
    private final DConnectApi mServiceInformationApi = new DConnectApi() {

        @Override
        public DConnectApiSpec.Method getMethod() {
            return DConnectApiSpec.Method.GET;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response,
                                 final DConnectService service) {
            String serviceId = service.getId();

            // connect
            Bundle connect = new Bundle();
            setWifiState(connect, getWifiState(serviceId));
            setBluetoothState(connect, getBluetoothState(serviceId));
            setNFCState(connect, getNFCState(serviceId));
            setBLEState(connect, getBLEState(serviceId));
            setConnect(response, connect);

            // version
            setVersion(response, getCurrentVersionName());

            // supports
            List<DConnectApiSpec> supports = new ArrayList<DConnectApiSpec>();
            for (DConnectProfile profile : service.getProfileList()) {
                for (DConnectApi api : profile.getApiList()) {
                    DConnectApiSpec spec = api.getApiSpec();
                    if (spec != null) {
                        supports.add(spec);
                    }
                }
            }
            setSupportApis(response, supports);

            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    /**
     * ServiceInformationプロファイルを生成する.
     */
    public ServiceInformationProfile() {
        mProvider = null;
        addApi(mServiceInformationApi);
    }

    /**
     * 指定されたプロファイルプロバイダーをもつServiceInformationプロファイルを生成する.
     *
     * @param provider プロファイルプロバイダー
     * @deprecated
     */
    public ServiceInformationProfile(final DConnectProfileProvider provider) {
        mProvider = provider;
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

    protected DConnectService getEndPoint(final String serviceId) {
        return null;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;
        String serviceId = getServiceID(request);

        if (attribute == null) {
            result = onGetInformation(request, response, serviceId);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    // ------------------------------------
    // GET
    // ------------------------------------

    /**
     * 周辺機器のサービス情報取得リクエストハンドラー.<br>
     * 周辺機器のサービス情報取得を提供し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * このメソッドでは自動的にサービス情報を返信する。返信処理に変更を加えたい場合はオーバーライドし、処理を上書きすること。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetInformation(final Intent request, final Intent response, final String serviceId) {
        // connect
        Bundle connect = new Bundle();
        setWifiState(connect, getWifiState(serviceId));
        setBluetoothState(connect, getBluetoothState(serviceId));
        setNFCState(connect, getNFCState(serviceId));
        setBLEState(connect, getBLEState(serviceId));
        setConnect(response, connect);

        // version
        setVersion(response, getCurrentVersionName());

        // supports
        List<DConnectProfile> profiles = mProvider.getProfileList();
        String[] profileNames = new String[profiles.size()];
        for (int i = 0; i < profileNames.length; i++) {
            profileNames[i] = profiles.get(i).getProfileName();
        }
        setSupports(response, profileNames);

        setResult(response, DConnectMessage.RESULT_OK);

        return true;
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
     * @deprecated
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

    public static void setSupportApis(final Intent response, final List<DConnectApiSpec> apiSpecs) {
        List<Bundle> supports = new ArrayList<Bundle>();
        for (DConnectApiSpec spec : apiSpecs) {
            Bundle support = new Bundle();
            setSupportApi(support, spec);
            supports.add(support);
        }
        response.putExtra(PARAM_SUPPORTS, supports.toArray(new Bundle[supports.size()]));
    }

    public static void setSupportApi(final Bundle api, final DConnectApiSpec spec) {
        setSupportApiName(api, spec.getName());
        setSupportApiMethod(api, spec.getMethod().getName());
        setSupportApiPath(api, spec.getPath());
        setSupportApiParams(api, spec.getRequestParamList());
    }

    public static void setSupportApiName(final Bundle api, final String name) {
        api.putString(PARAM_NAME, name);
    }

    public static void setSupportApiMethod(final Bundle api, final String method) {
        api.putString(PARAM_METHOD, method);
    }

    public static void setSupportApiPath(final Bundle api, final String path) {
        api.putString(PARAM_PATH, path);
    }

    public static void setSupportApiParams(final Bundle api, final List<DConnectRequestParamSpec> paramSpecs) {
        ArrayList<Bundle> params = new ArrayList<Bundle>();
        for (DConnectRequestParamSpec paramSpec : paramSpecs) {
            Bundle param = new Bundle();
            setRequestParam(param, paramSpec);
            params.add(param);
        }
        api.putParcelableArrayList(PARAM_REQUEST_PARAMS, params);
    }

    public static void setRequestParam(final Bundle param, final DConnectRequestParamSpec paramSpec) {
        param.putString(PARAM_NAME, paramSpec.getName());
        param.putString(PARAM_TYPE, paramSpec.getType().getName());
        param.putBoolean(PARAM_REQUIRED, paramSpec.isRequired());
        // TODO 型ごとのパラメータ定義をすべて設定
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
