/*
 IRKitDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.irkit.IRKitManager.DetectionListener;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualDeviceData;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.network.WiFiUtil;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitLightProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitRmeoteControllerProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitServceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitSystemProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitTVProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IRKitデバイスプラグインサービス.
 * @author NTT DOCOMO, INC.
 */
public class IRKitDeviceService extends DConnectMessageService implements DetectionListener {

    /**
     * IRKitの検知を再スタートさせるためのアクションを定義.
     */
    public static final String ACTION_RESTART_DETECTION_IRKIT = "action.ACTION_RESTART_DETECTION_IRKIT";

    /**
     * 検知したデバイス群.
     */
    private ConcurrentHashMap<String, IRKitDevice> mDevices;

    /**
     * 現在のSSID.
     */
    private String mCurrentSSID;

    /** DB Helper. */
    private IRKitDBHelper mDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = new IRKitDBHelper(getContext());
        EventManager.INSTANCE.setController(new MemoryCacheController());
        mDevices = new ConcurrentHashMap<String, IRKitDevice>();
        IRKitApplication app = (IRKitApplication) getApplication();
        app.setIRKitDevices(mDevices);
        IRKitManager.INSTANCE.init(this);
        IRKitManager.INSTANCE.setDetectionListener(this);
        if (WiFiUtil.isOnWiFi(this)) {
            startDetection();
        }

        // 追加するプロファイル
        addProfile(new IRKitRmeoteControllerProfile());
        // Virtual Device用プロファイル
        addProfile(new IRKitLightProfile());
        addProfile(new IRKitTVProfile());

        mCurrentSSID = WiFiUtil.getCurrentSSID(this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                if (!WiFiUtil.isOnWiFi(this) && IRKitManager.INSTANCE.isDetecting()) {
                    stopDetection();
                } else if (WiFiUtil.isOnWiFi(this) && WiFiUtil.isChangedSSID(this, mCurrentSSID)) {
                    restartDetection();
                }
                return START_STICKY;
            } else if (ACTION_RESTART_DETECTION_IRKIT.equals(action)) {
                if (WiFiUtil.isOnWiFi(this)) {
                    restartDetection();
                } else {
                    stopDetection();
                }
                return START_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDetection();
        // 参照をきっておく
        IRKitManager.INSTANCE.setDetectionListener(null);
        LocalOAuth2Main.destroy();
    }

    /**
     * サービスIDからIRKitのデバイスを取得する.
     * 
     * @param serviceId サービスID
     * @return デバイス
     */
    public IRKitDevice getDevice(final String serviceId) {
        return mDevices.get(serviceId);
    }

    /**
     * Service Discoveryのリクエストを用意する.
     * 
     * @param response レスポンスオブジェクト
     */
    public void prepareServiceDiscoveryResponse(final Intent response) {
        synchronized (mDevices) {

            List<Bundle> services = new ArrayList<Bundle>();
            int index = 0;
            for (IRKitDevice device : mDevices.values()) {
                Bundle service = createService(device, true);
                services.add(service);
                if (BuildConfig.DEBUG) {
                    Log.d("IRKit", "prepareServiceDiscoveryResponse service=" + service);
                }
                List<VirtualDeviceData> virtuals = mDBHelper.getVirtualDevices(null);
                if (virtuals.size() > 0) {
                    for (VirtualDeviceData virtual : virtuals) {

                        if (virtual.getServiceId().indexOf(device.getName()) != -1
                                && isIRExist(virtual.getServiceId())) {
                            Bundle virtualService = new Bundle();
                            ServiceDiscoveryProfile.setId(virtualService, virtual.getServiceId());
                            ServiceDiscoveryProfile.setName(virtualService, virtual.getDeviceName());
                            ServiceDiscoveryProfile.setType(virtualService, NetworkType.WIFI);
                            ServiceDiscoveryProfile.setState(virtualService, true);
                            ServiceDiscoveryProfile.setOnline(virtualService, true);
                            if (virtual.getCategoryName().equals("ライト")) {
                                ArrayList<String> scopes = new ArrayList<String>();
                                for (String profile : IRKitServiceInformationProfile.LIGHT_PROFILES) {
                                    scopes.add(profile);
                                }
                                virtualService.putStringArray(ServiceDiscoveryProfileConstants.PARAM_SCOPES,
                                        scopes.toArray(new String[scopes.size()]));
                            } else {
                                ArrayList<String> scopes = new ArrayList<String>();
                                for (String profile : IRKitServiceInformationProfile.TV_PROFILES) {
                                    scopes.add(profile);
                                }
                                virtualService.putStringArray(ServiceDiscoveryProfileConstants.PARAM_SCOPES,
                                        scopes.toArray(new String[scopes.size()]));
                            }
                            ServiceDiscoveryProfile.setConfig(virtualService, "Virtual Device");
                            services.add(virtualService);
                        }
                    }
                }
            }
            ServiceDiscoveryProfile.setServices(response, services);
            ServiceDiscoveryProfile.setResult(response, DConnectMessage.RESULT_OK);
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new IRKitSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new IRKitServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new IRKitServceDiscoveryProfile(this);
    }

    @Override
    public void onFoundDevice(final IRKitDevice device) {
        sendDeviceDetectionEvent(device, true);
    }

    @Override
    public void onLostDevice(final IRKitDevice device) {
        sendDeviceDetectionEvent(device, false);
    }

    /**
     * 赤外線を送信する.
     * @param serviceId サービスID
     * @param message 赤外線
     * @param response レスポンス
     * @return true:同期 false:非同期
     */
    public boolean sendIR(final String serviceId, final String message,
                          final Intent response) {
        boolean send = true;
        String[] ids = serviceId.split("\\.");
        IRKitDevice device = mDevices.get(ids[0]);
        if (message != null) {
            send = false;
            IRKitManager.INSTANCE.sendMessage(device.getIp(), message, new IRKitManager.PostMessageCallback() {
                @Override
                public void onPostMessage(boolean result) {
                    if (result) {
                        response.putExtra(DConnectMessage.EXTRA_RESULT,  DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setUnknownError(response);
                    }
                    getContext().sendBroadcast(response);
                }
            });
        }
        return send;
    }


    /**
     * デバイスの検知イベントを送信する.
     * 
     * @param device デバイス
     * @param isOnline trueなら発見、falseなら消失を意味する
     */
    private void sendDeviceDetectionEvent(final IRKitDevice device, final boolean isOnline) {

        boolean hit = false;
        synchronized (mDevices) {

            IRKitDevice d = mDevices.get(device.getName());
            if (d != null) {
                hit = true;
                if (!isOnline) {
                    mDevices.remove(device.getName());
                }
            } else if (isOnline) {
                mDevices.put(device.getName(), device);
            }
        }

        IRKitApplication app = (IRKitApplication) getApplication();
        app.setIRKitDevices(mDevices);
        if ((!hit && isOnline) || (hit && !isOnline)) {
            Bundle service = createService(device, isOnline);

            List<Event> events = EventManager.INSTANCE.getEventList(ServiceDiscoveryProfile.PROFILE_NAME,
                    ServiceDiscoveryProfile.ATTRIBUTE_ON_SERVICE_CHANGE);

            for (Event e : events) {
                Intent message = EventManager.createEventMessage(e);
                ServiceDiscoveryProfile.setNetworkService(message, service);
                sendEvent(message, e.getAccessToken());
            }
        }

    }

    /**
     * 一つでも赤外線が登録されているかをチェックする.
     * @param serviceId サービスID
     * @return true:登録されている, false:登録されていない
     */
    private boolean isIRExist(final String serviceId) {
        List<VirtualProfileData> requests = mDBHelper.getVirtualProfiles(serviceId, null);
        for (VirtualProfileData request : requests) {
            if (request.getIr() != null && request.getIr().indexOf("{\"format\":\"raw\",") != -1) {
                return true;
            }
        }
        return false;
    }



    /**
     * IRKitのデバイス情報からServiceを生成する.
     * 
     * @param device デバイス情報
     * @param online オンライン状態
     * @return サービス
     */
    private Bundle createService(final IRKitDevice device, final boolean online) {
        Bundle service = new Bundle();
        ServiceDiscoveryProfile.setId(service, device.getName());
        ServiceDiscoveryProfile.setName(service, device.getName());
        ServiceDiscoveryProfile.setType(service, NetworkType.WIFI);
        ServiceDiscoveryProfile.setState(service, online);
        ServiceDiscoveryProfile.setOnline(service, online);
        ArrayList<String> scopes = new ArrayList<String>();
        for (String profile : IRKitServiceInformationProfile.IRKIT_PROFILES) {
            scopes.add(profile);
        }
        service.putStringArray(ServiceDiscoveryProfileConstants.PARAM_SCOPES,
                scopes.toArray(new String[scopes.size()]));

        return service;
    }

    /**
     * 検知を開始する.
     */
    private void startDetection() {
        mCurrentSSID = WiFiUtil.getCurrentSSID(this);
        IRKitManager.INSTANCE.startDetection(this);
    }

    /**
     * 検知を終了する.
     */
    private void stopDetection() {
        mCurrentSSID = null;
        mDevices.clear();
        IRKitManager.INSTANCE.stopDetection();
    }

    private void restartDetection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopDetection();
                startDetection();
            }
        }).start();
    }
}
