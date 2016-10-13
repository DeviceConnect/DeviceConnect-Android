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

import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleSystemProfile;
import org.deviceconnect.android.deviceplugin.pebble.service.PebbleService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.Set;

/**
 * Pebbleデバイスプロバイダ.
 * @author NTT DOCOMO, INC.
 */
public class PebbleDeviceService extends DConnectMessageService {
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
                                service.setOnline(true);
                                getServiceProvider().addService(service);
                                break;
                            }
                        }
                    }
                }
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
                if (deviceName.indexOf("Pebble") != -1) {
                    return PebbleService.createServiceId(device);
                }
            }
        }
        return null;
    }
}
