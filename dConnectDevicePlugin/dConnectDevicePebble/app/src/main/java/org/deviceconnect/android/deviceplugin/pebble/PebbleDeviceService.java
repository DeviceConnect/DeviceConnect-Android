/*
 PebbleDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleSystemProfile;
import org.deviceconnect.android.deviceplugin.pebble.service.PebbleService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Pebbleデバイスプロバイダ.
 * @author NTT DOCOMO, INC.
 */
public class PebbleDeviceService extends DConnectMessageService {
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("pebble.dplugin");

    /**
     * Pebbleとのインターフェースを管理するクラス.
     */
    private PebbleManager mPebbleManager;

    @Override
    public void onCreate() {
        // super.onCreate() の前に初期化
        mPebbleManager = new PebbleManager(this);

        super.onCreate();

        // initialize of the EventManager
        EventManager.INSTANCE.setController(new MemoryCacheController());

        if (PebbleKit.isWatchConnected(this)) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                for (BluetoothDevice device : adapter.getBondedDevices()) {
                    if (device.getName().contains("Pebble")) {
                        DConnectService service = new PebbleService(device, PebbleDeviceService.this);
                        service.setOnline(true);
                        getServiceProvider().addService(service);
                        break;
                    }
                }
            }
        }
        mPebbleManager.addConnectStatusListener(new PebbleManager.OnConnectionStatusListener() {
            @Override
            public void onConnect(final String macAddress) {
                String serviceId = PebbleService.createServiceId(macAddress);
                DConnectService service = getServiceProvider().getService(serviceId);
                if (service == null) {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    if (adapter != null) {
                        for (BluetoothDevice device : adapter.getBondedDevices()) {
                            if (device.getAddress().equalsIgnoreCase(macAddress)) {
                                service = new PebbleService(device, PebbleDeviceService.this);
                                getServiceProvider().addService(service);
                                break;
                            }
                        }
                    }
                }
                service.setOnline(true);
            }

            @Override
            public void onDisconnect(final String macAddress) {
                String serviceId = PebbleService.createServiceId(macAddress);
                DConnectService service = getServiceProvider().getService(serviceId);
                if (service != null) {
                    service.setOnline(false);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        // Pebbleの後始末を行う
        mPebbleManager.destory();
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
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (origin != null) {
            EventManager.INSTANCE.removeEvents(origin);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        /** 全イベント削除. */
        EventManager.INSTANCE.removeAll();

        /** Pebble バッテリー関連 イベント解放. */
        sendDeleteEventToPebble(PebbleManager.PROFILE_BATTERY,
                PebbleManager.BATTERY_ATTRIBUTE_ON_BATTERY_CHANGE);

        sendDeleteEventToPebble(PebbleManager.PROFILE_BATTERY,
                PebbleManager.BATTERY_ATTRIBUTE_ON_CHARGING_CHANGE);

        /** Pebble DeviceOrientation イベント解放. */
        sendDeleteEventToPebble(PebbleManager.PROFILE_DEVICE_ORIENTATION,
                PebbleManager.DEVICE_ORIENTATION_ATTRIBUTE_ON_DEVICE_ORIENTATION);

        /** Pebble KeyEvent イベント解放. */
        sendDeleteEventToPebble(PebbleManager.PROFILE_KEY_EVENT,
                PebbleManager.KEY_EVENT_ATTRIBUTE_ON_DOWN);

        sendDeleteEventToPebble(PebbleManager.PROFILE_KEY_EVENT,
                PebbleManager.KEY_EVENT_ATTRIBUTE_ON_UP);

        /** System イベント解放. */
        sendDeleteEventToPebble(PebbleManager.PROFILE_SYSTEM,
                PebbleManager.SYSTEM_ATTRIBUTE_EVENTS);
    }

    /**
     * PebbleへEvent停止コマンドを送信する.
     * @param profile プロファイル.
     * @param attribute 属性.
     */
    private void sendDeleteEventToPebble(final int profile, final int attribute) {
        if (mPebbleManager != null) {
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) profile);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) attribute);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
            mPebbleManager.sendCommandToPebble(dic, new PebbleManager.OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    // do nothing.
                }
            });
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new PebbleSystemProfile();
    }

    /**
     * Pebble管理クラスを取得する.
     * 
     * @return Pebble管理クラス
     */
    public PebbleManager getPebbleManager() {
        return mPebbleManager;
    }

    /**
     * 現在接続されているPebbleのサービスIDを取得する.
     * <p>
     * 発見されない場合にはnullを返却する。
     * </p>
     * <p>
     * Pebbleが複数台接続されたときの挙動が不明。<br/>
     * PebbleKitでは、命令を識別して出す機能はない。<br/>
     * 基本は1対1で考える。<br/>
     * </p>
     * @return サービスID
     */
    public String getServiceId() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                String deviceName = device.getName();
                if (deviceName.contains("Pebble")) {
                    return PebbleService.createServiceId(device);
                }
            }
        }
        return null;
    }
}
