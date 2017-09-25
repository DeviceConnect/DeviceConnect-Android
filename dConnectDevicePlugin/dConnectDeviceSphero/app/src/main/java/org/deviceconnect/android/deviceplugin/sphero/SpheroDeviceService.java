/*
 SpheroDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.orbotix.ConvenienceRobot;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.sphero.SpheroManager.DeviceDiscoveryListener;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.data.SpheroParcelable;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroSystemProfile;
import org.deviceconnect.android.deviceplugin.sphero.service.SpheroService;
import org.deviceconnect.android.deviceplugin.sphero.setting.SettingActivity;
import org.deviceconnect.android.deviceplugin.sphero.util.BleUtils;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Spheroデバイスプラグイン.
 * @author NTT DOCOMO, INC.
 */
public class SpheroDeviceService extends DConnectMessageService implements DeviceDiscoveryListener,
        DConnectServiceListener  {

    /** TAG. */
    private static final String TAG = SpheroDeviceService.class.getSimpleName();

    /** 
     * レシーバー.
     */
    private BroadcastReceiver mReceiver;
    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                final String name = device.getName();
                PermissionUtility.requestPermissions(SpheroDeviceService.this, mHandler,
                        BleUtils.BLE_PERMISSIONS,
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                if (state == BluetoothDevice.BOND_BONDED && name != null && name.contains("Sphero")) {
                                    // Spheroとのペアリングに成功後、Spheroを使用できる状態にするための処理を開始する

                                    SpheroManager.INSTANCE.setDiscoveryListener(SpheroDeviceService.this);
                                    SpheroManager.INSTANCE.startDiscovery(SpheroDeviceService.this);
                                } else {
                                    SpheroManager.INSTANCE.stopDiscovery();
                                    DConnectService service = getServiceProvider().getService(device.getAddress());
                                    if (service != null) {
                                        service.setOnline(false);
                                    }
                                }
                            }

                            @NonNull
                            @Override
                            public void onFail(final String deniedPermission) {
                            }
                        });
                return;
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                Collection<DeviceInfo> devices = SpheroManager.INSTANCE.getConnectedDevices();
                for (DeviceInfo info : devices) {
                   final DConnectService service = getServiceProvider().getService(info.getDevice().getRobot().getIdentifier());
                    if (service != null) {
                        if (state == BluetoothAdapter.STATE_ON) {
                            PermissionUtility.requestPermissions(SpheroDeviceService.this, mHandler,
                                    BleUtils.BLE_PERMISSIONS,
                                    new PermissionUtility.PermissionRequestCallback() {
                                        @Override
                                        public void onSuccess() {
                                            SpheroManager.INSTANCE.stopDiscovery();
                                            SpheroManager.INSTANCE.setDiscoveryListener(SpheroDeviceService.this);
                                            SpheroManager.INSTANCE.startDiscovery(SpheroDeviceService.this);
                                        }

                                        @NonNull
                                        @Override
                                        public void onFail(final String deniedPermission) {
                                        }
                                    });
                        } else if (state == BluetoothAdapter.STATE_OFF) {
                            service.setOnline(false);
                        }
                    }
                }
            }
        }
    };

    /**
     * Instance of handler.
     */
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());
        SpheroManager.INSTANCE.setService(this);
        registerBluetoothFilter();
        addProfile(new SpheroServiceDiscoveryProfile(getServiceProvider()));
        getServiceProvider().addServiceListener(this);
        //既にSpheroとペアリングされている場合は接続処理を行う。
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName() != null && device.getName().contains("Sphero")) {
                PermissionUtility.requestPermissions(SpheroDeviceService.this, mHandler,
                        BleUtils.BLE_PERMISSIONS,
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                SpheroManager.INSTANCE.setDiscoveryListener(SpheroDeviceService.this);
                                SpheroManager.INSTANCE.startDiscovery(SpheroDeviceService.this);
                            }

                            @NonNull
                            @Override
                            public void onFail(final String deniedPermission) {
                            }
                        });
                break;
            }
        }
    }

    /**
     * Spheroのサービスをオフラインにする.
     * @param id ServiceId
     */
    private void disconnectingSpheroService(String id) {
        final DConnectService service = getServiceProvider().getService(id);
        if (service != null) {
            if (service.isOnline()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Disconnect to " + service.getName(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            service.setOnline(false);
        }
    }

    /**
     * Spheroと接続処理を行う.
     * DConnectServiceの生成および状態の変化を行う。
     * @param id SpheroのID
     */
    private void connectingSpheroService(final String id) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (SpheroManager.INSTANCE.connect(id)) {

                    final DeviceInfo info = SpheroManager.INSTANCE.getDevice(id);
                    ConvenienceRobot device = null;
                    if (info != null) {
                        device = info.getDevice();
                    }
                    if (device != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "************ connected **********");
                        }
                        DConnectService service = getServiceProvider().getService(info.getDevice().getRobot().getIdentifier());
                        if (service == null) {
                            service = new SpheroService(info);
                            getServiceProvider().addService(service);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Connect to " + info.getDevice().getRobot().getName(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        service.setOnline(true);
                        SpheroManager.INSTANCE.stopDiscovery();
                    }
                }
            }
        }).start();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SpheroSystemProfile();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBluetoothFilter();
        getServiceProvider().removeServiceListener(this);
        SpheroManager.INSTANCE.shutdown();
    }


    @Override
    public void onDeviceFound(ConvenienceRobot sphero) {
        connectingSpheroService(sphero.getRobot().getIdentifier());
    }

    @Override
    public void onDeviceLost(ConvenienceRobot sphero) {
       disconnectingSpheroService(sphero.getRobot().getIdentifier());
    }

    @Override
    public void onDeviceLostAll() {
    }


    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            Log.d(TAG,"Plug-in : onManagerUninstalled");
        }
        EventManager.INSTANCE.removeAll();
        resetSpheroEvents();
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Plug-in : onManagerTerminated");
        }
        EventManager.INSTANCE.removeAll();
        resetSpheroEvents();
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Plug-in : onManagerEventTransmitDisconnected");
        }
        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }
        resetSpheroEvents();

    }

    @Override
    public void onServiceAdded(DConnectService service) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onServiceAdded: " + service.getName());
        }
    }

    @Override
    public void onServiceRemoved(final DConnectService service) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onServiceRemoved: " + service.getName());
        }
        SpheroManager.INSTANCE.disconnect(service.getId());
        SpheroManager.INSTANCE.removeNotConnectedDevice(service.getId());
        EventManager.INSTANCE.removeAll();
        resetSpheroEvents();
    }

    @Override
    public void onStatusChange(DConnectService service) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStatusChange: " + service.getName());
        }

    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Plug-in : onDevicePluginReset");
        }
        EventManager.INSTANCE.removeAll();
        resetSpheroEvents();
    }

    /**
     * Spheroが現在実行中のイベントを停止する.
     */
    private void resetSpheroEvents() {
        Collection<DeviceInfo> devices = SpheroManager.INSTANCE.getConnectedDevices();
        for (DeviceInfo info : devices) {
            if (!SpheroManager.INSTANCE.hasSensorEvent(info)) {
                SpheroManager.INSTANCE.stopSensor(info);
            }
            List<Event> events = EventManager.INSTANCE.getEventList(
                    info.getDevice().getRobot().getIdentifier(), SpheroProfile.PROFILE_NAME,
                    SpheroProfile.INTER_COLLISION, SpheroProfile.ATTR_ON_COLLISION);

            if (events.size() == 0) {
                SpheroManager.INSTANCE.stopCollision(info);
            }
        }
    }

    /**
     * Bluetoothイベントの登録.
     */
    private void registerBluetoothFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mSensorReceiver, filter, null, mHandler);
    }
    /**
     * Bluetoothイベントの解除.
     */
    private void unregisterBluetoothFilter() {
        unregisterReceiver(mSensorReceiver);
    }

}
