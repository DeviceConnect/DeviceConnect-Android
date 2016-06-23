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
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectRequestParamSpec;
import org.deviceconnect.android.profile.spec.IntegerRequestParamSpec;
import org.deviceconnect.android.profile.spec.NumberRequestParamSpec;
import org.deviceconnect.android.profile.spec.StringRequestParamSpec;
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
public class ServiceInformationProfile extends DConnectProfile implements ServiceInformationProfileConstants {

    /**
     * 設定画面起動用IntentのパラメータオブジェクトのExtraキー.
     */
    public static final String SETTING_PAGE_PARAMS = "org.deviceconnect.profile.system.setting_params";

    /**
     * Service Information API.
     */
    private final DConnectApi mServiceInformationApi = new DConnectApi() {

        @Override
        public DConnectApiSpec.Method getMethod() {
            return DConnectApiSpec.Method.GET;
        }

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

            // supports
            List<DConnectProfile> profileList = getService().getProfileList();
            List<DConnectApiSpec> supports = new ArrayList<DConnectApiSpec>();
            String[] profileNames = new String[profileList.size()];
            int i = 0;
            for (DConnectProfile profile : profileList) {
                profileNames[i++] = profile.getProfileName();
                for (DConnectApi api : profile.getApiList()) {
                    DConnectApiSpec spec = api.getApiSpec();
                    if (spec != null) {
                        supports.add(spec);
                    }
                }
            }
            setSupports(response, profileNames);
            setSupportApis(response, supports);

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

    public static void setSupportApis(final Intent response, final List<DConnectApiSpec> apiSpecs) {
        List<Bundle> supports = new ArrayList<Bundle>();
        for (DConnectApiSpec spec : apiSpecs) {
            Bundle support = new Bundle();
            setSupportApi(support, spec);
            supports.add(support);
        }
        response.putExtra(PARAM_SUPPORT_APIS, supports.toArray(new Bundle[supports.size()]));
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

    public static void setSupportApiParams(final Bundle api, final DConnectRequestParamSpec[] paramSpecs) {
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
        param.putBoolean(PARAM_MANDATORY, paramSpec.isMandatory());

        if (paramSpec instanceof IntegerRequestParamSpec) {
            IntegerRequestParamSpec intParamSpec = (IntegerRequestParamSpec) paramSpec;
            if (intParamSpec.getEnumList() != null) {
                ArrayList<Bundle> enums = new ArrayList<Bundle>();
                for (DConnectRequestParamSpec.Enum<Long> e : intParamSpec.getEnumList()) {
                    Bundle b = new Bundle();
                    b.putString(PARAM_NAME, e.getName());
                    b.putLong(PARAM_VALUE, e.getValue());
                    enums.add(b);
                }
                param.putParcelableArrayList(PARAM_ENUM, enums);
            }
            if (intParamSpec.getMaxValue() != null) {
                param.putLong(PARAM_MAX_VALUE, intParamSpec.getMaxValue());
            }
            if (intParamSpec.getMinValue() != null) {
                param.putLong(PARAM_MIN_VALUE, intParamSpec.getMinValue());
            }
            if (intParamSpec.getExclusiveMaxValue() != null) {
                param.putLong(PARAM_EXCLUSIVE_MAX_VALUE, intParamSpec.getExclusiveMaxValue());
            }
            if (intParamSpec.getExclusiveMinValue() != null) {
                param.putLong(PARAM_EXCLUSIVE_MIN_VALUE, intParamSpec.getExclusiveMinValue());
            }
        } else if (paramSpec instanceof NumberRequestParamSpec) {
            NumberRequestParamSpec numParamSpec = (NumberRequestParamSpec) paramSpec;
            if (numParamSpec.getMaxValue() != null) {
                param.putDouble(PARAM_MAX_VALUE, numParamSpec.getMaxValue());
            }
            if (numParamSpec.getMinValue() != null) {
                param.putDouble(PARAM_MIN_VALUE, numParamSpec.getMinValue());
            }
            if (numParamSpec.getExclusiveMaxValue() != null) {
                param.putDouble(PARAM_EXCLUSIVE_MAX_VALUE, numParamSpec.getExclusiveMaxValue());
            }
            if (numParamSpec.getExclusiveMinValue() != null) {
                param.putDouble(PARAM_EXCLUSIVE_MIN_VALUE, numParamSpec.getExclusiveMinValue());
            }
        } else if (paramSpec instanceof StringRequestParamSpec) {
            StringRequestParamSpec strParamSpec = (StringRequestParamSpec) paramSpec;
            if (strParamSpec.getEnumList() != null) {
                ArrayList<Bundle> enums = new ArrayList<Bundle>();
                for (DConnectRequestParamSpec.Enum<String> e : strParamSpec.getEnumList()) {
                    Bundle b = new Bundle();
                    b.putString(PARAM_NAME, e.getName());
                    b.putString(PARAM_VALUE, e.getValue());
                    enums.add(b);
                }
                param.putParcelableArrayList(PARAM_ENUM, enums);
            }
            if (strParamSpec.getMaxLength() != null) {
                param.putLong(PARAM_MAX_LENGTH, strParamSpec.getMaxLength());
            }
            if (strParamSpec.getMinLength() != null) {
                param.putLong(PARAM_MIN_LENGTH, strParamSpec.getMinLength());
            }
        }
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
