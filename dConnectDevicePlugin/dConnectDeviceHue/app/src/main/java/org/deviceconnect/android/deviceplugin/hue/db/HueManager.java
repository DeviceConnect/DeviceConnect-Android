/*
 HueManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hue.db;

import android.content.Context;
import android.util.Log;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.exception.PHHueInvalidAPIException;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;

import org.deviceconnect.android.deviceplugin.hue.HueConstants;
import org.deviceconnect.android.deviceplugin.hue.BuildConfig;
import org.deviceconnect.android.deviceplugin.hue.service.HueLightService;
import org.deviceconnect.android.deviceplugin.hue.service.HueService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.deviceconnect.android.deviceplugin.hue.HueConstants.OFFLINE_USERNAME;

/**
 * Hueの操作をするクラス.
 * @author NTT DOCOMO, INC.
 */
public enum HueManager {
    /**
     * シングルトンなHueManagerのインスタンス.
     */
    INSTANCE;
    /** Log's tag name. */
    private static final String TAG = "HueManager";
    /**
     * Hue接続状態.
     */
    public enum HueState {
        /** 未認証. */
        INIT,
        /** 未接続. */
        NO_CONNECT,
        /** 認証失敗. */
        AUTHENTICATE_FAILED,
        /** 認証済み. */
        AUTHENTICATE_SUCCESS
    }

    /**
     * Hueと接続する時にすでに接続されているかの確認結果を返す.
     */
    public interface HueConnectionListener {
        /**
         * すでに接続されている.
         */
        void onConnected();

        /**
         * まだ接続されていない.
         */
        void onNotConnected();
    }

    /**
     * Hueのプラグインがサポートするサービス情報が更新されたことを通知する.
     */
    public interface HueServiceListener {
        /**
         * Hueのブリッジ情報が更新された.
         * @param isConnected ブリッジがスマートフォンと繋がっているか
         * @param accessPoint ブリッジの情報
         */
        void onUpdatedHueBridgeService(final boolean isConnected, final PHAccessPoint accessPoint);

        /**
         * Hueのライト情報が更新された.
         * @param isConnected ブリッジがスマートフォンと繋がっているか
         * @param accessPoint ブリッジの情報
         * @param light ライトの情報
         */
        void onUpdatedHueLightService(final boolean isConnected, final PHAccessPoint accessPoint, final PHLight light);
    }

    /**
     * Hueプラグインがサポートするサービスの切断情報を返す.
     */
    public interface HueDisconnectionListener {
        /**
         * 切断されたブリッジ情報.
         * @param ip ブリッジのIPアドレス
         */
        void onDisconnectedBridge(final String ip);

        /**
         * 切断されたライト情報.
         * @param ip ブリッジのIPアドレス
         * @param lightId ライトID
         */
        void onDisconnectedLight(final String ip, final String lightId);
    }
    /**
     * Hue SDK オブジェクト.
     */
    private PHHueSDK mHueSDK;
    /**
     * HueのBridgeデータを管理ヘルパークラス.
     */
    private HueDBHelper mHueDBHelper;
    /**
     * Hueのライトデータを管理ヘルパークラス.
     */
    private HueLightDBHelper mHueLightDBHelper;

    /**
     * HueManagerのインスタンスを生成する.
     */
    private HueManager() {
    }

    /**
     * 初期化を実行する.
     *
     * @param context コンテキストオブジェクト。
     */
    public void init(final Context context) {
        mHueDBHelper = new HueDBHelper(context);
        mHueLightDBHelper = new HueLightDBHelper(context);
        mHueSDK = PHHueSDK.getInstance();
        mHueSDK.setAppName(HueConstants.APNAME);
        mHueSDK.setDeviceName(HueConstants.APNAME);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@@@@ PHHueSDK version: " + mHueSDK.getSDKVersion());
            Log.i(TAG, "@@@@@@ PHHueSDK App Name: " + mHueSDK.getAppName());
            Log.i(TAG, "@@@@@@ PHHueSDK Device Name: " + mHueSDK.getDeviceName());
        }
    }

    /**
     * HueManagerを破棄する.
     */
    public synchronized void destroy() {
        if (mHueSDK == null) {
            return;
        }

        mHueSDK.disableAllHeartbeat();
        mHueSDK.destroySDK();
    }

    /**
     * HueSDKのリスナーを追加する.
     * @param listener PHSDKLisneter
     */
    public synchronized void addSDKListener(final PHSDKListener listener) {
        if (mHueSDK == null) {
            return;
        }

        mHueSDK.getNotificationManager().registerSDKListener(listener);
    }

    /**
     * HueSDKのリスナーを削除する.
     * @param listener PHSDKListener
     */
    public synchronized void removeSDKListener(final PHSDKListener listener) {
        if (mHueSDK == null) {
            return;
        }

        mHueSDK.getNotificationManager().unregisterSDKListener(listener);
    }

    /**
     * Hueアクセスポイントを返す.
     * @return Hueアクセスポイント
     */
    public synchronized List<PHAccessPoint> getAccessPoint() {
        if (mHueSDK == null) {
            return null;
        }

        // アクセスポイントのキャッシュクリア
        mHueSDK.getAccessPointsFound().clear();
        // アクセスポイントリストビューのクリア
        return mHueSDK.getAccessPointsFound();
    }

    /**
     * Hueブリッジを検索する.
     */
    public synchronized void searchHueBridge() {
        if (mHueSDK == null) {
            return;
        }

        // ローカルBridgeのUPNP Searchを開始
        PHBridgeSearchManager sm = (PHBridgeSearchManager) mHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
    }

    /**
     * Hueブリッジの認証を行います.
     * <p>
     * ユーザにHueブリッジのボタンを押下してもらう必要があります。
     * </p>
     * @param accessPoint Hueアクセスポイント
     * @param listener Hueの接続状況を返すリスナー
     */
    public synchronized void startAuthenticate(final PHAccessPoint accessPoint, final HueConnectionListener listener) {
        if (mHueSDK == null) {
            return;
        }

        if (!mHueSDK.isAccessPointConnected(accessPoint)) {
            mHueSDK.connect(accessPoint);
            if (listener != null) {
                listener.onNotConnected();
            }
        } else {
            if (listener != null) {
                listener.onConnected();
            }
        }
    }

    /**
     * Pushlink認証の受付を開始する.
     * @param accessPoint Hueアクセスポイント
     */
    public synchronized void startPushlinkAuthentication(final PHAccessPoint accessPoint) {
        if (mHueSDK == null) {
            return;
        }

        mHueSDK.startPushlinkAuthentication(accessPoint);
    }

    /**
     * Pushlink認証の受付を終了する.
     */
    public synchronized void stopPushlinkAuthentication() {
        if (mHueSDK == null) {
            return;
        }

        mHueSDK.stopPushlinkAuthentication();
    }


    /**
     * SDKのキャッシュ上のライトリストを返す.
     * @return ライトリスト
     */
    public synchronized List<PHLight> getCacheLights() {
        if (mHueSDK == null) {
            return null;
        }

        PHBridge bridge = mHueSDK.getSelectedBridge();
        if (bridge != null) {
            PHBridgeResourcesCache cache = bridge.getResourceCache();
            return cache.getAllLights();
        }
        return null;
    }

    /**
     * SDkのキャッシュの中からライトIDにあったPHLightオブジェクトを返す
     * @param serviceId ライトのServiceId
     * @return PHLightオブジェクト
     */
    public synchronized PHLight getCacheLight(final String serviceId) {
        if (mHueSDK == null) {
            return null;
        }

        PHBridge bridge = mHueSDK.getSelectedBridge();
        if (bridge != null && serviceId.contains(":")) {
            String[] ids = serviceId.split(":");
            String lightId = ids[1];
            List<PHLight> lights = bridge.getResourceCache().getAllLights();
            for (int i = 0; i < lights.size(); i++) {
                PHLight light = lights.get(i);
                if (light.getIdentifier().equals(lightId)) {
                    return light;
                }
            }
        }
        return null;
    }

    /**
     * ブリッジが認識できるライトを検索する.
     * @param listener 検索結果を返すリスナー
     * @throws PHHueInvalidAPIException HueAPIを実行した時に発生したException
     */
    public synchronized void searchLightAutomatic(final PHLightListener listener) throws PHHueInvalidAPIException {
        if (mHueSDK == null) {
            return;
        }

        PHBridge bridge = mHueSDK.getSelectedBridge();
        bridge.findNewLights(listener);
    }

    /**
     * ブリッジに認識させるライトを検索する.
     * @param serialNumber 認識させるライトのシリアルナンバー
     * @param listener 検索結果を返すリスナー
     * @throws PHHueInvalidAPIException HueAPIを実行した時に発生したException
     */
    public synchronized void searchLightManually(final String serialNumber, final PHLightListener listener) throws PHHueInvalidAPIException {
        if (mHueSDK == null) {
            return;
        }

        PHBridge bridge = mHueSDK.getSelectedBridge();
        List<String> serials = new ArrayList<String>();
        serials.add(serialNumber);
        bridge.findNewLightsWithSerials(serials, listener);
    }

    /**
     * アクセスポイントと再接続する.
     * @param listener 再接続処理結果を返すリスナー
     */
    public synchronized void reconnectAccessPoints(final HueServiceListener listener) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,"HueDeviceService#reconnectAccessPoints");
        }

        List<PHAccessPoint> accessPoints = mHueDBHelper.getAccessPoints();
        for (int i = 0; i < accessPoints.size(); i++) {
            PHAccessPoint accessPoint = accessPoints.get(i);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "AccessPoint: " + accessPoint.getMacAddress() + " " + accessPoint.getIpAddress());
            }
            //UserNameがオフラインの時は接続処理を行わない
            if (!accessPoint.getUsername().equals(OFFLINE_USERNAME) && mHueSDK != null && !mHueSDK.isAccessPointConnected(accessPoint)) {
                mHueSDK.connect(accessPoint);
            }
            if (listener != null) {
                if (mHueSDK != null) {
                    listener.onUpdatedHueBridgeService(mHueSDK.isAccessPointConnected(accessPoint), accessPoint);
                } else {
                    listener.onUpdatedHueBridgeService(false, accessPoint);
                }
            }

            List<PHLight> lights;
            PHBridge bridge = findBridge(accessPoint.getIpAddress());
            if (bridge != null) {
                lights = bridge.getResourceCache().getAllLights();
            } else {
                lights = mHueLightDBHelper.getLightsForIp(accessPoint.getIpAddress());
            }
            for (int j = 0; j < lights.size(); j++) {
                PHLight light = lights.get(j);
                if (listener != null) {
                    if (mHueSDK != null) {
                        listener.onUpdatedHueLightService(mHueSDK.isAccessPointConnected(accessPoint), accessPoint, light);
                    } else {
                        listener.onUpdatedHueLightService(false, accessPoint, light);
                    }
                }
            }
        }
        if (mHueSDK != null) {
            PHBridgeSearchManager sm = (PHBridgeSearchManager) mHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            sm.search(true, true);
        }
    }
    /**
     * DBに格納されているアクセスポイント情報とライトを削除します.
     *
     * @param service Hueサービス
     */
    public synchronized void removeHueService(final HueService service) {
        mHueDBHelper.removeAccessPointByIpAddress(service.getId());
        mHueLightDBHelper.removeLightByIpAddress(service.getId());
    }

    /**
     * DBに格納されているライトを削除します.
     *
     * @param service HueLightサービス
     */
    public synchronized void removeHueLightService(final HueLightService service) {
        String serviceId = service.getId();
        if (!serviceId.contains(":")) {
            return;
        }
        // ServiceIdの解析
        String[] ids = serviceId.split(":");
        String ipAddress = ids[0];
        final String lightId = ids[1];

        PHBridge bridge = findBridge(ipAddress);
        if (bridge != null) {
            bridge.deleteLight(lightId, new PHLightListener() {
                @Override
                public void onReceivingLightDetails(PHLight phLight) {

                }

                @Override
                public void onReceivingLights(List<PHBridgeResource> list) {

                }

                @Override
                public void onSearchComplete() {

                }

                @Override
                public void onSuccess() {
                    mHueLightDBHelper.removeLightByLightId(lightId);
                }

                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                }
            });
        }
    }
    /**
     *　指定されたIPのブリッジに紐づいているライトリストを返す.
     *
     * @param ipAddress BridgeのIP
     */
    public synchronized List<PHLight> getLightsForIp(final String ipAddress) {
        return mHueLightDBHelper.getLightsForIp(ipAddress);
    }

    /**
     * 全てのブリッジとの接続を解除する.
     * @param flag true:Hueプラグインが認識しているサービスをオフラインにする
     * @param listener 切断結果を返すリスナー
     */
    public synchronized void disconnectAllBridges(final boolean flag, final HueDisconnectionListener listener) {
        if (mHueSDK == null) {
            return;
        }

        List<PHBridge> bridges = new ArrayList<PHBridge>(mHueSDK.getAllBridges());
        for (int i = 0; i < bridges.size(); i++) {
            PHBridge bridge = bridges.get(i);
            mHueSDK.disableHeartbeat(bridge);
            mHueSDK.disconnect(bridge);

            if (flag) {
                PHBridgeResourcesCache cache = bridge.getResourceCache();
                String ipAddress = cache.getBridgeConfiguration().getIpAddress();
                if (listener != null) {
                    listener.onDisconnectedBridge(ipAddress);
                    List<PHLight> lights = mHueLightDBHelper.getLightsForIp(ipAddress);
                    for (int j = 0; j < lights.size(); j++) {
                        PHLight light = lights.get(j);
                        listener.onDisconnectedLight(ipAddress, light.getIdentifier());
                    }
                }
            }
        }
    }
    /**
     * Hueのブリッジを検索する.
     *
     * @param serviceId Service ID
     * @return Hueのブリッジを管理するオブジェクト
     */
    public synchronized PHBridge findBridge(final String serviceId) {
        if (mHueSDK == null) {
            return null;
        }
        List<PHBridge> bridges = mHueSDK.getAllBridges();
        for (int i = 0; i < bridges.size(); i++) {
            PHBridge bridge = bridges.get(i);
            PHBridgeResourcesCache cache = bridge.getResourceCache();
            String ipAddress = cache.getBridgeConfiguration().getIpAddress();
            if (serviceId.equals(ipAddress)) {
                return bridge;
            }
        }
        return null;
    }

    /**
     * Hueに接続されているライトを検索する.
     * <p>
     * ライトが見つからない場合には<code>null</code>を返却する。
     * </p>
     *
     * @param bridge  Hueのブリッジ
     * @param lightId ライトID
     * @return Hueのブリッジに接続されたライト
     */
    public synchronized PHLight findLight(final PHBridge bridge, final String lightId) {
        Map<String, PHLight> lights = bridge.getResourceCache().getLights();
        if (lights.size() == 0) {
            return null;
        }

        if (lightId == null || lightId.length() == 0) {
            return lights.entrySet().iterator().next().getValue();
        } else {
            return bridge.getResourceCache().getLights().get(lightId);
        }
    }

    /**
     * Hueプラグインが認識しているサービスが保持する情報を更新する.
     * @param accessPoints Hueアクセスポイント
     * @param listener アップデート結果を返すリスナー
     */
    public synchronized void updateAccessPoint(final List<PHAccessPoint> accessPoints, final HueServiceListener listener) {
        if (mHueSDK == null) {
            return;
        }

        if (accessPoints != null && accessPoints.size() > 0) {
            for (int i = 0; i < accessPoints.size(); i++) {
                PHAccessPoint accessPoint = accessPoints.get(i);
                PHAccessPoint ap = mHueDBHelper.getAccessPointByMacAddress(accessPoint.getMacAddress());
                if (ap != null) {
                    accessPoint.setUsername(ap.getUsername());
                }

                boolean isConnected = true;
                if ((accessPoint.getUsername() == null || !accessPoint.getUsername().equals(OFFLINE_USERNAME))
                        && (mHueSDK != null && !mHueSDK.isAccessPointConnected(accessPoint))) {
                    isConnected = false;
                }
                if (listener != null) {
                    if (isConnected) {
                        listener.onUpdatedHueBridgeService(mHueSDK.isAccessPointConnected(accessPoint), accessPoint);
                    } else {
                        listener.onUpdatedHueBridgeService(isConnected, accessPoint);
                    }
                }
                List<PHLight> lights;
                PHBridge bridge = findBridge(accessPoint.getIpAddress());
                if (bridge != null && isConnected) {
                    lights = bridge.getResourceCache().getAllLights();
                } else {
                    lights = mHueLightDBHelper.getLightsForIp(accessPoint.getIpAddress());
                }
                for (int j = 0; j < lights.size(); j++) {
                    PHLight light = lights.get(j);
                    if (listener != null) {
                        if (isConnected) {
                            listener.onUpdatedHueLightService(mHueSDK.isAccessPointConnected(accessPoint), accessPoint, light);
                        } else {
                            listener.onUpdatedHueLightService(false, accessPoint, light);
                        }
                    }
                }
            }
        } else {
            PHBridgeSearchManager sm = (PHBridgeSearchManager) mHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            sm.search(false, false, true);
        }
    }

    /**
     * Hue ブリッジおよびライトの情報を永続化する.
     * @param phBridge PHBridge
     * @param userName ブリッジのユーザ名
     */
    public synchronized void saveBridgeForDB(final PHBridge phBridge, final String userName) {
        if (mHueSDK == null) {
            return;
        }

        String ipAddress = phBridge.getResourceCache().getBridgeConfiguration().getIpAddress();
        String macAddress = phBridge.getResourceCache().getBridgeConfiguration().getMacAddress();

        mHueSDK.setSelectedBridge(phBridge);
        mHueSDK.addBridge(phBridge);
        mHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
        mHueSDK.getLastHeartbeat().put(ipAddress, System.currentTimeMillis());
        // 接続されたアクセスポイント情報をDBに格納
        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setUsername(userName);
        accessPoint.setIpAddress(ipAddress);
        accessPoint.setMacAddress(macAddress);
        if (!mHueDBHelper.hasAccessPoint(accessPoint)) {
            mHueDBHelper.addAccessPoint(accessPoint);
        } else {
            mHueDBHelper.updateAccessPoint(accessPoint);
        }

        // Bridgeに紐づくライトの追加
        List<PHLight> lights;

        PHBridge bridge = findBridge(accessPoint.getIpAddress());
        if (bridge != null) {
            lights = bridge.getResourceCache().getAllLights();
            for (int i = 0; i < lights.size(); i++) {
                PHLight light = lights.get(i);
                if (!mHueLightDBHelper.hasLight(ipAddress, light.getIdentifier())) {
                    mHueLightDBHelper.addLight(ipAddress, light);
                } else {
                    mHueLightDBHelper.updateLight(ipAddress, light);
                }
            }
        }
    }

    /**
     * 切断されたアクセスポイントリストから指定されたブリッジを削除する.
     * @param bridge 接続されたブリッジ
     * @return 接続されたブリッジのIPアドレス
     */
    public synchronized String removeDisconnectedAccessPoint(final PHBridge bridge) {
        if (mHueSDK == null) {
            return null;
        }

        String ipAddress = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
        for (int i = 0; i < mHueSDK.getDisconnectedAccessPoint().size(); i++) {
            if (mHueSDK.getDisconnectedAccessPoint().get(i).getIpAddress().equals(ipAddress)) {
                mHueSDK.getDisconnectedAccessPoint().remove(i);
            }
        }
        return ipAddress;
    }

    /**
     * 切断されたアクセスポイントリストに指定されたブリッジを追加する.
     * @param accessPoint 切断されたアクセスポイント
     * @return アクセスポイントのIPアドレスを返す
     */
    public synchronized String addDisconnectedAccessPoint(final PHAccessPoint accessPoint) {
        if (mHueSDK == null) {
            return null;
        }
        if (!mHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
            mHueSDK.getDisconnectedAccessPoint().add(accessPoint);
        }
        return accessPoint.getIpAddress();
    }

    /**
     * 認証が失敗した場合にブリッジを切断します.
     * @param bridge 切断するブリッジ
     */
    public synchronized void disconnectHueBridge(final PHBridge bridge) {
        if (bridge == null || mHueSDK == null) {
            return;
        }
        if (mHueSDK.isHeartbeatEnabled(bridge)) {
            mHueSDK.disableHeartbeat(bridge);
        }
        mHueSDK.disconnect(bridge);
    }

    /**
     * アクセスポイントとの接続を解除する.
     * @param ipAddress アクセスポイントのIPアドレス
     */
    public synchronized void disconnectAccessPoint(final String ipAddress) {
        if (mHueSDK == null) {
            return;
        }

        List<PHAccessPoint> accessPoints = mHueDBHelper.getAccessPoints();
        for (int i = 0; i < accessPoints.size(); i++) {
            PHAccessPoint p = accessPoints.get(i);
            if (p.getIpAddress().equals(ipAddress)) {
                PHAccessPoint accessPoint = new PHAccessPoint();
                accessPoint.setUsername(OFFLINE_USERNAME);
                accessPoint.setIpAddress(p.getIpAddress());
                accessPoint.setMacAddress(p.getMacAddress());
                mHueDBHelper.updateAccessPoint(accessPoint);
            }
        }
    }

    /**
     * Hueブリッジとの接続を解除する.
     */
    public synchronized void disconnectHueBridge() {
        if (mHueSDK == null) {
            return;
        }

        PHBridge bridge = mHueSDK.getSelectedBridge();
        if (bridge != null) {
            if (mHueSDK.isHeartbeatEnabled(bridge)) {
                mHueSDK.disableHeartbeat(bridge);
            }
            mHueSDK.disconnect(bridge);
            mHueSDK.removeBridge(bridge);
            mHueSDK.getAccessPointsFound().clear();
            mHueSDK = null;
        }
    }

    /**
     * サービスIDで指定されたアクセスポイントを返す.
     * @param serviceId アクセスポイントをさすserviceId
     * @return PHAccessPointオブジェクト
     */
    public synchronized PHAccessPoint getAccessPoint(final String serviceId) {
        List<PHAccessPoint> accesses = mHueDBHelper.getAccessPoints();
        for (int i = 0; i < accesses.size(); i++) {
            PHAccessPoint access = accesses.get(i);
            if (serviceId.contains(access.getIpAddress())) {
                return access;
            }
        }
        return null;
    }

    /**
     * 指定されたアクセスポイントのDB情報を更新する.
     * @param accessPoint アクセスポイント
     */
    public synchronized void updateHueServiceForDB(final PHAccessPoint accessPoint) {
        mHueDBHelper.updateAccessPoint(accessPoint);
    }
}
