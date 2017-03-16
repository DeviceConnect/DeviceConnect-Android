/*
HueDeviceService
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.hue;

import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import org.deviceconnect.android.deviceplugin.hue.profile.HueSystemProfile;
import org.deviceconnect.android.deviceplugin.hue.service.HueService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.json.hue.JSONObject;

import java.util.List;
import java.util.logging.Logger;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class HueDeviceService extends DConnectMessageService {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "hue.dplugin";

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("hue.dplugin");

    @Override
    public void onCreate() {
        super.onCreate();
        initHueSDK();
    }

    @Override
    public void onDestroy() {
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

        // ブリッジの検索
        PHHueSDK hueSDK = PHHueSDK.getInstance();
        PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
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

        // hue SDKの初期化
        PHHueSDK hueSDK = PHHueSDK.getInstance();
        hueSDK.setAppName(HueConstants.APNAME);
        hueSDK.setDeviceName(HueConstants.APNAME);
        hueSDK.getNotificationManager().registerSDKListener(mPhListener);

        if (DEBUG) {
            Log.i(TAG, "@@@@@@ PHHueSDK version: " + hueSDK.getSDKVersion());
            Log.i(TAG, "@@@@@@ PHHueSDK App Name: " + hueSDK.getAppName());
            Log.i(TAG, "@@@@@@ PHHueSDK Device Name: " + hueSDK.getDeviceName());
        }

        // ブリッジの検索
        PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
    }

    /**
     * HueSDKを破棄します.
     */
    private void destroyHueSDK() {
        if (DEBUG) {
            Log.i(TAG, "HueDeviceService#destroyHueSDK");
        }

        // hue SDKの後始末
        PHHueSDK hueSDK = PHHueSDK.getInstance();
        hueSDK.getNotificationManager().unregisterSDKListener(mPhListener);
        hueSDK.disableAllHeartbeat();
        hueSDK.destroySDK();
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

            PHHueSDK hueSDK = PHHueSDK.getInstance();
            if (accessPoints != null && accessPoints.size() > 0) {
                hueSDK.getAccessPointsFound().clear();
                hueSDK.getAccessPointsFound().addAll(accessPoints);

                for (PHAccessPoint accessPoint : accessPoints) {
                    accessPoint.setUsername(HueConstants.USERNAME);

                    HueService service = (HueService) getServiceProvider().getService(accessPoint.getIpAddress());
                    if (service == null) {
                        service = new HueService(accessPoint);
                        getServiceProvider().addService(service);
                    }
                    service.setOnline(hueSDK.isAccessPointConnected(accessPoint));
                }
            } else {
                PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                sm.search(false, false, true);
            }
        }
        
        @Override
        public void onCacheUpdated(final List<Integer> cacheNotificationsList, final PHBridge bridge) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onCacheUpdated: cacheNotificationsList=" + cacheNotificationsList + ", bridge=" + bridge);
            }
        }

        @Override
        public void onBridgeConnected(final PHBridge phBridge, final String s) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onBridgeConnected: bridge=" + phBridge + ", s=" + s);
            }

            String ipAddress = phBridge.getResourceCache().getBridgeConfiguration().getIpAddress();

            PHHueSDK hueSDK = PHHueSDK.getInstance();
            hueSDK.setSelectedBridge(phBridge);
            hueSDK.addBridge(phBridge);
            hueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
            hueSDK.getLastHeartbeat().put(ipAddress, System.currentTimeMillis());

            DConnectService service = getServiceProvider().getService(ipAddress);
            if (service != null) {
                service.setOnline(true);
            }
        }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onAuthenticationRequired: accessPoint=" + accessPoint);
            }
        }

        @Override
        public void onConnectionResumed(final PHBridge bridge) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onConnectionResumed: bridge=" + bridge);
            }

            String ipAddress = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();

            PHHueSDK hueSDK = PHHueSDK.getInstance();
            for (int i = 0; i < hueSDK.getDisconnectedAccessPoint().size(); i++) {
                if (hueSDK.getDisconnectedAccessPoint().get(i).getIpAddress().equals(ipAddress)) {
                    hueSDK.getDisconnectedAccessPoint().remove(i);
                }
            }

            DConnectService service = getServiceProvider().getService(ipAddress);
            if (service != null) {
                service.setOnline(true);
            }
        }

        @Override
        public void onConnectionLost(final PHAccessPoint accessPoint) {
            if (DEBUG) {
                Log.i(TAG, "PHSDKListener:#onConnectionLost: accessPoint=" + accessPoint);
            }

            PHHueSDK hueSDK = PHHueSDK.getInstance();
            if (!hueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
                hueSDK.getDisconnectedAccessPoint().add(accessPoint);
            }

            DConnectService service = getServiceProvider().getService(accessPoint.getIpAddress());
            if (service != null) {
                service.setOnline(false);
            }
        }

        @Override
        public void onError(final int code, final String message) {
            if (DEBUG) {
                Log.e(TAG, "PHSDKListener:#onError: code=" + code + ", message=" + message);
            }
        }

        @Override
        public void onParsingErrors(final List<PHHueParsingError> errors) {
            if (DEBUG) {
                Log.e(TAG, "PHSDKListener:#onParsingErrors");
                for (PHHueParsingError error : errors) {
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
}
