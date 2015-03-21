/*
 PebbleDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble;

import java.util.Set;

import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleBatteryProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleCanvasProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleKeyEventProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleNotificationProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleServceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleSettingProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleSystemProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleVibrationProfile;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

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

        // add supported profiles
        addProfile(new PebbleNotificationProfile());
        addProfile(new PebbleDeviceOrientationProfile(this));
        addProfile(new PebbleVibrationProfile());
        addProfile(new PebbleBatteryProfile(this));
        addProfile(new PebbleSettingProfile());
        addProfile(new PebbleCanvasProfile());
        addProfile(new PebbleKeyEventProfile(this));
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

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new PebbleServceDiscoveryProfile(this, this);
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
                String deviceAddress = device.getAddress();
                // URIに使えるように、Macアドレスの":"を取り除いて小文字に変換する 
                String serviceId = deviceAddress.replace(":", "").toLowerCase();
                if (deviceName.indexOf("Pebble") != -1) {
                    return PebbleServceDiscoveryProfile.SERVICE_ID + serviceId;
                }
            }
        }
        return null;
    }
}
