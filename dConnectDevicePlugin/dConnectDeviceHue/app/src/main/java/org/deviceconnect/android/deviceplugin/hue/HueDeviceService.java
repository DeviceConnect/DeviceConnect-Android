/*
HueDeviceService
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.hue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;

import org.deviceconnect.android.deviceplugin.hue.BuildConfig;
import org.deviceconnect.android.deviceplugin.hue.db.HueManager;
import org.deviceconnect.android.deviceplugin.hue.profile.HueSystemProfile;
import org.deviceconnect.android.deviceplugin.hue.service.HueLightService;
import org.deviceconnect.android.deviceplugin.hue.service.HueService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.json.hue.JSONObject;

import java.util.List;
import java.util.logging.Logger;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class HueDeviceService extends DConnectMessageService {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグタグ.
     */
    private static final String TAG = "hue.dplugin";

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("hue.dplugin");

    @Override
    public void onCreate() {
        super.onCreate();
        initHueSDK();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityReceiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mConnectivityReceiver);
        destroyHueSDK();
        super.onDestroy();
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }

        reconnectAccessPoints();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HueSystemProfile();
    }

    /**
     * HueSDKを初期化します.
     */
    private void initHueSDK() {
        if (DEBUG) {
            Log.i(TAG, "HueDeviceService#initHueSDK");
        }

        HueManager.INSTANCE.init(getApplicationContext());
        HueManager.INSTANCE.addSDKListener(mPhListener);
        reconnectAccessPoints();
    }

    /**
     * HueSDKを破棄します.
     */
    private void destroyHueSDK() {
        if (DEBUG) {
            Log.i(TAG, "HueDeviceService#destroyHueSDK");
        }

        // hue SDKの後始末
        HueManager.INSTANCE.removeSDKListener(mPhListener);
        HueManager.INSTANCE.destroy();
    }

    /**
     * Hueサービスを追加します.
     *
     * @param isConnected アクセスポイントと接続しているかどうか
     * @param accessPoint アクセスポイント
     */
    private void addHueService(final boolean isConnected, final PHAccessPoint accessPoint) {
        HueService service = (HueService) getServiceProvider().getService(accessPoint.getIpAddress());
        if (service == null) {
            service = new HueService(accessPoint);
            getServiceProvider().addService(service);
        }
        service.setOnline(isConnected);
    }
    /**
     * ライトサービスを追加します.
     *
     * @param isConnected アクセスポイントと接続しているかどうか
     * @param accessPoint アクセスポイント
     * @param light ライト
     */
    private void addHueLightService(final boolean isConnected, final PHAccessPoint accessPoint, final PHLight light) {
        HueLightService service = (HueLightService) getServiceProvider().getService(accessPoint.getIpAddress() + ":" + light.getIdentifier());
        if (service == null) {
            service = new HueLightService(accessPoint.getIpAddress(), light);
            getServiceProvider().addService(service);
        }
        service.setOnline(isConnected);
    }
    /**
     * 登録されているアクセスポイントに再接続を行います.
     */
    private void reconnectAccessPoints() {
        HueManager.INSTANCE.reconnectAccessPoints(new HueManager.HueServiceListener() {
            @Override
            public void onUpdatedHueBridgeService(boolean isConnected, PHAccessPoint accessPoint) {
                addHueService(isConnected, accessPoint);
            }

            @Override
            public void onUpdatedHueLightService(boolean isConnected, PHAccessPoint accessPoint, PHLight light) {
                addHueLightService(isConnected, accessPoint, light);
            }
        });
    }


    /**
     * 接続されている全てのブリッジを切断します。
     *
     * @param flag serviceのonline状態を変更フラグ(trueの時は変更する)
     */
    private void disconnectAllBridges(final boolean flag) {
        HueManager.INSTANCE.disconnectAllBridges(flag, new HueManager.HueDisconnectionListener() {
            @Override
            public void onDisconnectedBridge(String ip) {
                DConnectService service = getServiceProvider().getService(ip);
                if (service != null) {
                    service.setOnline(false);
                }

            }

            @Override
            public void onDisconnectedLight(String ip, String lightId) {
                DConnectService service = getServiceProvider().getService(ip + ":" + lightId);
                if (service != null) {
                    service.setOnline(false);
                }
            }
        });
    }

    /**
     * Hueのブリッジとの通信を管理するリスナー.
     */
    private final PHSDKListener mPhListener = new PHSDKListener() {

        @Override
        public void onAccessPointsFound(final List<PHAccessPoint> accessPoints) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onAccessPointsFound: accessPoint" +
                        "=" + accessPoints);
            }
            HueManager.INSTANCE.updateAccessPoint(accessPoints, new HueManager.HueServiceListener() {
                @Override
                public void onUpdatedHueBridgeService(boolean isConnected, PHAccessPoint accessPoint) {
                    addHueService(isConnected, accessPoint);
                }

                @Override
                public void onUpdatedHueLightService(boolean isConnected, PHAccessPoint accessPoint, PHLight light) {
                    addHueLightService(isConnected, accessPoint, light);
                }
            });
        }

        @Override
        public void onCacheUpdated(final List<Integer> cacheNotificationsList, final PHBridge bridge) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onCacheUpdated: cacheNotificationsList=" + cacheNotificationsList + ", bridge=" + bridge);
            }
        }

        @Override
        public void onBridgeConnected(final PHBridge phBridge, final String userName) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onBridgeConnected: bridge=" + phBridge + ", userName=" + userName);
            }
            HueManager.INSTANCE.saveBridgeForDB(phBridge, userName);
            updateHueBridge(true, phBridge);
         }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onAuthenticationRequired: accessPoint=" + accessPoint);
            }
        }

        @Override
        public void onConnectionResumed(final PHBridge phBridge) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onConnectionResumed: bridge=" + phBridge);
            }

            HueManager.INSTANCE.removeDisconnectedAccessPoint(phBridge);
            updateHueBridge(true, phBridge);
        }

        @Override
        public void onConnectionLost(final PHAccessPoint accessPoint) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onConnectionLost: accessPoint=" + accessPoint);
            }

            updateHueBridge(false, accessPoint);
        }

        @Override
        public void onError(final int code, final String message) {
            if (DEBUG) {
                Log.e(TAG, "PHSDKListener:#onError: code=" + code + ", message=" + message);
            }

            if (code == PHHueError.AUTHENTICATION_FAILED) {
                disconnectAllBridges(false);
                reconnectAccessPoints();
            }
        }

        @Override
        public void onParsingErrors(final List<PHHueParsingError> errors) {
            if (DEBUG) {
                Log.e(TAG, "PHSDKListener:#onParsingErrors");
                for (int i = 0; i < errors.size(); i++) {
                    PHHueParsingError error = errors.get(i);
                    Log.e(TAG, "--");
                    Log.e(TAG, "code: " + error.getCode() + ", " + error.getMessage());
                    JSONObject obj = error.getJSONContext();
                    if (obj != null) {
                        Log.e(TAG, "" + obj.toString(4));
                    }
                }
                Log.e(TAG, "==");
            }
        }
    };

    /**
     * Hueプラグインが管理するサービスのステータスを更新する.
      * @param isConnected true:オンライン false:オフライン
     * @param phBridge PHBridge
     */
    private void updateHueBridge(final boolean isConnected, final PHBridge phBridge) {
        String ipAddress = phBridge.getResourceCache().getBridgeConfiguration().getIpAddress();
        DConnectService service = getServiceProvider().getService(ipAddress);
        if (service != null) {
            service.setOnline(isConnected);
        }
        List<PHLight> lights = phBridge.getResourceCache().getAllLights();
        for (int i = 0; i < lights.size(); i++) {
            HueLightService light = (HueLightService) getServiceProvider().getService(ipAddress + ":" + lights.get(i).getIdentifier());
            if (light == null) {
                light = new HueLightService(ipAddress, lights.get(i));
                getServiceProvider().addService(light);
            }
            light.setOnline(isConnected);
        }
    }
    /**
     * Hueプラグインが管理するサービスのステータスを更新する.
     * @param isConnected true:オンライン false:オフライン
     * @param accessPoint PHAccessPoint
     */
    private void updateHueBridge(final boolean isConnected, final PHAccessPoint accessPoint) {
        String ipAddress = HueManager.INSTANCE.addDisconnectedAccessPoint(accessPoint);
        DConnectService service = getServiceProvider().getService(ipAddress);
        if (service != null) {
            service.setOnline(isConnected);
        }
        List<PHLight> lights = HueManager.INSTANCE.getLightsForIp(ipAddress);
        for (int i = 0; i < lights.size(); i++) {
            HueLightService light = (HueLightService) getServiceProvider().getService(ipAddress + ":" + lights.get(i).getIdentifier());
            if (light == null) {
                light = new HueLightService(ipAddress, lights.get(i));
                getServiceProvider().addService(light);
            }
            light.setOnline(isConnected);
        }
    }
    /**
     * ネットワーク状況が変わった通知を受けるレシーバー.
     */
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = conn.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    reconnectAccessPoints();
                } else {
                    disconnectAllBridges(true);
                }
            }
        }
    };
}
