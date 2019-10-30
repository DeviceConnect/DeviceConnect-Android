/*
 FaBoDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fabo.core.BuildConfig;
import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoGPIOProfile;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoSystemProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.FaBoService;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualService;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualServiceFactory;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.VirtualServiceDBHelper;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.spec.DConnectServiceSpec;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.service.DConnectService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class FaBoDeviceService extends DConnectMessageService {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Tag.
     */
    private static final String TAG = "FaBo";

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("fabo.dplugin");

    /**
     * 仮想サービスを管理するクラス.
     */
    private VirtualServiceDBHelper mDBHelper;

    /**
     * FaBoデバイスを操作するためのクラス.
     */
    private FaBoDeviceControl mFaBoDeviceControl;

    /**
     * 監視用Thread.
     */
    private WatchFirmataThread mWatchFirmataThread;

    @Override
    public void onCreate() {
        super.onCreate();

        // FaBoを直接操作するためのサービス
        getServiceProvider().addService(new FaBoService());

        // 仮想サービスの初期化
        initVirtualService();

        // FaBoデバイス操作クラスの初期化
        initFaBoDeviceControl();
    }

    @Override
    public void onDestroy() {
        endWatchFirmata();

        if (mFaBoDeviceControl != null) {
            mFaBoDeviceControl.destroy();
            mFaBoDeviceControl = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // ManagerのEvent送信経路切断通知受信時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (origin != null) {
            EventManager.INSTANCE.removeEvents(origin);
            List<Event> events = EventManager.INSTANCE.getEventList(FaBoGPIOProfile.PROFILE_NAME,
                    FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);
            for (Event event : events) {
                if (event.getOrigin().equals(origin)) {
                    String serviceId = event.getServiceId();
                    Iterator serviceIds = mServiceIdStore.iterator();
                    while (serviceIds.hasNext()) {
                        String tmpServiceId = (String) serviceIds.next();
                        if (tmpServiceId.equals(serviceId)) {
                            serviceIds.remove();
                        }
                    }
                }
            }
        } else {
            resetPluginResource();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new FaBoSystemProfile();
    }

    /**
     * FaBoを操作するクラスを取得します.
     * @return FaBoDeviceControlのインスタンス
     */
    protected abstract FaBoDeviceControl createFaBoDeviceControl();

    /**
     * FaBoを操作するためのクラスを取得します.
     * @return FaBoを操作するクラス
     */
    public FaBoDeviceControl getFaBoDeviceControl() {
        return mFaBoDeviceControl;
    }

    /**
     * FaBoDeviceControlを初期化します.
     */
    private void initFaBoDeviceControl() {
        mFaBoDeviceControl = createFaBoDeviceControl();
        mFaBoDeviceControl.setOnFaBoDeviceControlListener(new FaBoDeviceControl.OnFaBoDeviceControlListener() {
            @Override
            public void onConnected() {
                startWatchFirmata();
                setOnline(true);
                sendResultToActivity(FaBoConst.SUCCESS_CONNECT_FIRMATA);
            }

            @Override
            public void onDisconnected() {
                endWatchFirmata();
                setOnline(false);
            }

            @Override
            public void onFailedConnected() {
                setOnline(false);
                sendResultToActivity(FaBoConst.FAILED_CONNECT_ARDUINO);
            }
        });
        mFaBoDeviceControl.initialize();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        EventManager.INSTANCE.removeAll();

        if (mFaBoDeviceControl != null) {
            mFaBoDeviceControl.destroy();
        }
        initFaBoDeviceControl();
    }

    /**
     * テスト用仮想データ.
     */
    private void createTestData() {
        if (mDBHelper.getServiceDataList().isEmpty()) {
            ServiceData serviceData = new ServiceData();
            serviceData.setName("RobotCar(Mouse)");
            serviceData.setServiceId("mouse_service_id");

            ProfileData profileData = new ProfileData();
            profileData.setServiceId("mouse_service_id");
            profileData.setType(ProfileData.Type.I2C_MOUSE_DRIVE_CONTROLLER);
            serviceData.addProfileData(profileData);

            mDBHelper.addServiceData(serviceData);
        }
    }

    /**
     * 仮想サービスのデータをDBに追加します.
     * @param serviceData 追加する仮想サービスのデータ
     * @return 追加したVirtualService
     */
    public VirtualService addServiceData(final ServiceData serviceData) {
        String serviceId = mDBHelper.createServiceId();
        serviceData.setServiceId(serviceId);
        boolean result = mDBHelper.addServiceData(serviceData) >= 0;
        if (result) {
            VirtualService service = VirtualServiceFactory.createService(serviceData);
            service.setOnline(FaBoConst.STATUS_FABO_RUNNING == mFaBoDeviceControl.getStatus());
            getServiceProvider().addService(service);
            return service;
        }
        return null;
    }

    /**
     * 仮想サービスのデータを更新します.
     * @param serviceData 更新する仮想サービスのデータ
     * @return 更新したVirtualService
     */
    public VirtualService updateServiceData(final ServiceData serviceData) {
        boolean result = mDBHelper.updateServiceData(serviceData) >= 0;
        if (result) {
            DConnectService service = getServiceProvider().getService(serviceData.getServiceId());
            if (service != null && service instanceof VirtualService) {
                service.setName(serviceData.getName());
                ((VirtualService)service).setServiceData(serviceData);

                // プロファイルを一度全て削除してから、プロファイルを追加します
                for (DConnectProfile p : service.getProfileList()) {
                    if (canRemoveProfile(p.getProfileName())) {
                        service.removeProfile(p);
                    }
                }

                for (ProfileData p : serviceData.getProfileDataList()) {
                    DConnectProfile profile = VirtualServiceFactory.createProfile(p);
                    if (profile != null) {
                        service.addProfile(profile);
                        profile.setContext(this);
                    }
                }
                return ((VirtualService) service);
            }
        }
        return null;
    }

    /**
     * 仮想サービスのデータを削除します.
     * @param serviceData 削除する仮想サービスのデータ
     */
    public void removeServiceData(final ServiceData serviceData) {
        getServiceProvider().removeService(serviceData.getServiceId());
        mDBHelper.removeServiceData(serviceData);
    }

    /**
     * 指定されたvidに対応する仮想サービスデータを取得します.
     * <p>
     * 指定されたvidに対応する仮想サービスが存在しない場合にはnullを返却します.
     * </p>
     * @param vid 仮想サービスデータのID
     * @return ServiceDataのインスタンス
     */
    public ServiceData getServiceData(final String vid) {
        return mDBHelper.getServiceData(vid);
    }

    /**
     * 削除できるプロファイルか確認を行う.
     * @param profileName 削除できるかを確認するプロファイル名
     * @return 削除できる場合はtrue、それ以外はfalse
     */
    private boolean canRemoveProfile(final String profileName) {
        return !"serviceInformation".equalsIgnoreCase(profileName);
    }

    /**
     * 仮想サービスの初期化を行います.
     */
    private void initVirtualService() {
        if (DEBUG) {
            Log.i(TAG, "------------------------------------");
            Log.i(TAG, "Create virtual service list.");
            Log.i(TAG, "------------------------------------");
        }

        mDBHelper = new VirtualServiceDBHelper(getApplicationContext());
        createTestData();

        List<ServiceData> serviceDataList = mDBHelper.getServiceDataList();
        for (ServiceData serviceData : serviceDataList) {
            DConnectService service = VirtualServiceFactory.createService(serviceData);
            getServiceProvider().addService(service);
        }
    }

    /**
     * DConnectServiceのOnline状況を設定します.
     *
     * @param online オンライン状態
     */
    private void setOnline(final boolean online) {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            service.setOnline(online);
        }
    }

    /**
     * Activityにメッセージを返信する.
     *
     * @param resultId 結果のID.
     */
    private void sendResultToActivity(final int resultId) {
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT);
        intent.putExtra("resultId", resultId);
        sendBroadcast(intent);
    }

    /**
     * 値監視用のThread.
     */
    private void startWatchFirmata() {
        if (mWatchFirmataThread == null) {
            mWatchFirmataThread = new WatchFirmataThread();
            mWatchFirmataThread.start();
        }
    }

    /**
     * Threadを停止.
     */
    private void endWatchFirmata() {
        if (mWatchFirmataThread != null) {
            mWatchFirmataThread.stopWatchFirmata();
            mWatchFirmataThread = null;
        }
    }

    /**
     * ServiceIDを保持する.
     */
    private List<String> mServiceIdStore = new ArrayList<>();

    /**
     * onChangeイベントの登録.
     *
     * @param serviceId 現在接続中のデバイスプラグインのServiceId.
     */
    public void registerOnChange(final String serviceId) {
        mServiceIdStore.add(serviceId);
    }

    /**
     * onChangeイベントの削除.
     */
    public void unregisterOnChange(final String serviceId) {
        Iterator serviceIds = mServiceIdStore.iterator();
        while (serviceIds.hasNext()) {
            String tmpServiceId = (String) serviceIds.next();
            if (tmpServiceId.equals(serviceId)) {
                serviceIds.remove();
            }
        }
    }

    /**
     * 監視用スレッド.
     */
    private class WatchFirmataThread extends Thread {
        /**
         * 監視スレッド停止フラグ.
         */
        private boolean mStopFlag;

        @Override
        public void run() {
            if (DEBUG) {
                Log.i(TAG, "---------------------------------");
                Log.i(TAG, "Start watch a fragment_fabo_firmata.");
                Log.i(TAG, "---------------------------------");
            }

            while (!mStopFlag) {
                for (int s = 0; s < mServiceIdStore.size(); s++) {
                    String serviceId = mServiceIdStore.get(s);
                    List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                            FaBoGPIOProfile.PROFILE_NAME, null, FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);

                    for (Event event : events) {
                        Bundle pins = new Bundle();
                        for (FaBoShield.Pin pin : FaBoShield.Pin.values()) {
                            if (getFaBoDeviceControl().isPinSupported(pin)) {
                                switch (pin.getMode()) {
                                    case GPIO_IN:
                                        pins.putInt(pin.getPinNames()[1], getFaBoDeviceControl().getDigital(pin).getValue());
                                        break;
                                    case ANALOG:
                                        pins.putInt(pin.getPinNames()[1], getFaBoDeviceControl().getAnalog(pin));
                                        break;
                                }
                            }
                        }

                        if (pins.size() > 0) {
                            // Eventに値をおくる.
                            Intent intent = EventManager.createEventMessage(event);
                            intent.putExtra("pins", pins);
                            sendEvent(intent, event.getAccessToken());
                        }
                    }
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (DEBUG) {
                Log.i(TAG, "---------------------------------");
                Log.i(TAG, "Stop watch a fragment_fabo_firmata.");
                Log.i(TAG, "---------------------------------");
            }
        }

        /**
         * 監視用スレッドを停止します.
         */
        void stopWatchFirmata() {
            mStopFlag = true;
            interrupt();
        }
    }
}
