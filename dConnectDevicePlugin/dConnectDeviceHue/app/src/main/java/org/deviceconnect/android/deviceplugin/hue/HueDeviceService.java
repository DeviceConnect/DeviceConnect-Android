/*
HueDeviceService
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.hue;

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

import java.util.List;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class HueDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
        android.os.Debug.waitForDebugger();

        //hue SDKの初期化
        PHHueSDK hueSDK = PHHueSDK.getInstance();
        hueSDK.setAppName(HueConstants.APNAME);
        hueSDK.getNotificationManager().registerSDKListener(mPhListener);

        //前もってキャッシュをupdateしておく
        PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
    }

    @Override
    public void onDestroy() {
        //hue SDKの後始末
        PHHueSDK hueSDK = PHHueSDK.getInstance();
        hueSDK.getNotificationManager().unregisterSDKListener(mPhListener);
        hueSDK.disableAllHeartbeat();

        PHBridge bridge = hueSDK.getSelectedBridge();
        if (bridge != null) {
            hueSDK.disconnect(bridge);
        }
        super.onDestroy();
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HueSystemProfile();
    }

    /**
     * Hueのブリッジとの通信を管理するリスナー.
     */
    private final PHSDKListener mPhListener = new PHSDKListener() {

        @Override
        public void onAccessPointsFound(final List<PHAccessPoint> accessPoints) {
            PHHueSDK hueSDK = PHHueSDK.getInstance();
            if (accessPoints != null && accessPoints.size() > 0) {
                hueSDK.getAccessPointsFound().clear();
                hueSDK.getAccessPointsFound().addAll(accessPoints);

                for (PHAccessPoint accessPoint : accessPoints) {
                    if (getServiceProvider().getService(accessPoint.getIpAddress()) == null) {
                        getServiceProvider().addService(new HueService(accessPoint));
                    }
                }
            } else {
                PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                sm.search(false, false, true);
            }
        }
        
        @Override
        public void onCacheUpdated(final List<Integer> arg0, final PHBridge arg1) {
        }

        @Override
        public void onBridgeConnected(final PHBridge bridge) {
            String ipAddress = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();

            PHHueSDK phHueSDK = PHHueSDK.getInstance();
            phHueSDK.setSelectedBridge(bridge);
            phHueSDK.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);
            phHueSDK.getLastHeartbeat().put(ipAddress, System.currentTimeMillis());

            DConnectService service = getServiceProvider().getService(ipAddress);
            if (service != null) {
                service.setOnline(true);
            }
        }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
            PHHueSDK phHueSDK = PHHueSDK.getInstance();
            phHueSDK.startPushlinkAuthentication(accessPoint);
        }

        @Override
        public void onConnectionResumed(final PHBridge bridge) {
            String ipAddress = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();

            PHHueSDK phHueSDK = PHHueSDK.getInstance();
            for (int i = 0; i < phHueSDK.getDisconnectedAccessPoint().size(); i++) {
                if (phHueSDK.getDisconnectedAccessPoint().get(i).getIpAddress().equals(ipAddress)) {
                    phHueSDK.getDisconnectedAccessPoint().remove(i);
                }
            }

            DConnectService service = getServiceProvider().getService(ipAddress);
            if (service != null) {
                service.setOnline(true);
            }
        }

        @Override
        public void onConnectionLost(final PHAccessPoint accessPoint) {
            PHHueSDK phHueSDK = PHHueSDK.getInstance();
            if (!phHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
                phHueSDK.getDisconnectedAccessPoint().add(accessPoint);
            }

            DConnectService service = getServiceProvider().getService(accessPoint.getIpAddress());
            if (service != null) {
                service.setOnline(false);
            }
        }

        @Override
        public void onError(final int code, final String message) {
        }

        @Override
        public void onParsingErrors(final List<PHHueParsingError> errors) {
        }
    };
}
