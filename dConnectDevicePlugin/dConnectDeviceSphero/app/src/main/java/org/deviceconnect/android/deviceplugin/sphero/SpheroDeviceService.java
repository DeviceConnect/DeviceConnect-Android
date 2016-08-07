/*
 SpheroDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.orbotix.ConvenienceRobot;
import com.orbotix.common.Robot;

import org.deviceconnect.android.deviceplugin.sphero.SpheroManager.DeviceDiscoveryListener;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.data.SpheroParcelable;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroSystemProfile;
import org.deviceconnect.android.deviceplugin.sphero.service.SpheroService;
import org.deviceconnect.android.deviceplugin.sphero.setting.SettingActivity;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spheroデバイスプラグイン.
 * @author NTT DOCOMO, INC.
 */
public class SpheroDeviceService extends DConnectMessageService implements DeviceDiscoveryListener {

    /** TAG. */
    private static final String TAG = SpheroDeviceService.class.getSimpleName();
    /** Action NameSpace. */
    private static final String ACTION_NAMESPACE = SpheroDeviceService.class.getPackage().getName() + ".action";

    /**
     * 検知開始アクション.
     */
    public static final String ACTION_START_DISCOVERY = ACTION_NAMESPACE + ".START_DISCOVERY";

    /**
     * 検知終了アクション.
     */
    public static final String ACTION_STOP_DISCOVERY = ACTION_NAMESPACE + ".STOP_DISCOVERY";

    /**
     * 接続アクション.
     */
    public static final String ACTION_CONNECT = ACTION_NAMESPACE + ".CONNECT";

    /**
     * 接続解除アクション.
     */
    public static final String ACTION_DISCONNECT = ACTION_NAMESPACE + ".DISCONNECT";

    /**
     * 接続済みデバイス取得アクション.
     */
    public static final String ACTION_GET_CONNECTED = ACTION_NAMESPACE + ".GET_CONNECTED";
    /**
     * 見つかっているが、接続されていないデバイス取得アクション.
     */
    public static final String ACTION_GET_FOUND = ACTION_NAMESPACE + ".GET_FOUND";
    /**
     * デバイス削除アクション.
     */
    public static final String ACTION_DELETE_DEVICE = ACTION_NAMESPACE + ".DELETE_DEVICE";

    /**
     * Extraキー : {@value} .
     */
    public static final String EXTRA_ID = "id";

    /** 
     * レシーバー.
     */
    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());
        SpheroManager.INSTANCE.setService(this);
        
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECT);
        filter.addAction(ACTION_DISCONNECT);
        filter.addAction(ACTION_GET_CONNECTED);
        filter.addAction(ACTION_START_DISCOVERY);
        filter.addAction(ACTION_STOP_DISCOVERY);
        filter.addAction(ACTION_GET_FOUND);
        filter.addAction(ACTION_DELETE_DEVICE);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                onStartCommand(intent, 0, 0);
            }
        };
        lbm.registerReceiver(mReceiver, filter);
        addProfile(new SpheroServiceDiscoveryProfile(getServiceProvider()));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (action.equals(ACTION_START_DISCOVERY)) {
                SpheroManager.INSTANCE.setDiscoveryListener(this);
                SpheroManager.INSTANCE.startDiscovery(this);
            } else if (action.equals(ACTION_STOP_DISCOVERY)) {
                SpheroManager.INSTANCE.setDiscoveryListener(null);
                SpheroManager.INSTANCE.stopDiscovery();
            } else if (action.equals(ACTION_CONNECT)) {
                final String id = intent.getStringExtra(EXTRA_ID);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (SpheroManager.INSTANCE.connect(id)) {
                            if (BuildConfig.DEBUG) {
                                Log.d("TEST", "************ connected **********");
                            }
                            DeviceInfo info = SpheroManager.INSTANCE.getDevice(id);
                            ConvenienceRobot device;
                            if (info == null) {
                                device = null;
                            } else {
                                device = info.getDevice();
                            }
                            if (device != null) {
                                DConnectService service = new SpheroService(info);
                                getServiceProvider().addService(service);

                                sendDevice(SettingActivity.ACTION_CONNECTED, device, SpheroParcelable.SpheroState.Connected);
                            } else {
                                sendDevice(SettingActivity.ACTION_CONNECTED, null, SpheroParcelable.SpheroState.Error);
                            }
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.d("TEST", "************ failed to connect **********");
                            }
                            sendDevice(SettingActivity.ACTION_CONNECTED, null, SpheroParcelable.SpheroState.Error);
                        }
                    }
                }).start();
            } else if (action.equals(ACTION_DISCONNECT)) {
                String id = intent.getStringExtra(EXTRA_ID);
                DeviceInfo info = SpheroManager.INSTANCE.getDevice(id);
                DConnectService service = getServiceProvider().getService(id);
                if (service != null) {
                    service.setOnline(false);
                }

                SpheroManager.INSTANCE.disconnect(id);
                if (info != null) {
                    sendDevice(SettingActivity.ACTION_DISCONNECTED, info.getDevice(), SpheroParcelable.SpheroState.Disconnected);
                }
            } else if (action.equals(ACTION_GET_CONNECTED)) {
                Collection<DeviceInfo> devices = SpheroManager.INSTANCE.getConnectedDevices();
                Intent res = new Intent();
                res.setAction(SettingActivity.ACTION_ADD_CONNECTED_DEVICE);
                ArrayList<SpheroParcelable> devs = new ArrayList<SpheroParcelable>();
                for (DeviceInfo info : devices) {
                    DConnectService service = getServiceProvider().getService(info.getDevice().getRobot().getIdentifier());
                    if (service != null) {
                        service.setOnline(true);
                    }
                    devs.add(new SpheroParcelable(info.getDevice().getRobot().getIdentifier(),
                            info.getDevice().getRobot().getName(),
                            SpheroParcelable.SpheroState.Remember));
                }
                res.putParcelableArrayListExtra(SettingActivity.EXTRA_DEVICES, devs);
                LocalBroadcastManager.getInstance(this).sendBroadcast(res);
            } else if (action.equals(ACTION_GET_FOUND)) {
                List<Robot> devices = SpheroManager.INSTANCE.getFoundDevices();
                Collection<DeviceInfo> connectedDevices = SpheroManager.INSTANCE.getConnectedDevices();
                Intent res = new Intent();
                res.setAction(SettingActivity.ACTION_ADD_FOUNDED_DEVICE);
                ArrayList<SpheroParcelable> devs = new ArrayList<SpheroParcelable>();
                for (Robot device : devices) {
                    DConnectService service = getServiceProvider().getService(device.getIdentifier());
                    if (service == null) {
                        DeviceInfo info = SpheroManager.INSTANCE.getDevice(device.getIdentifier());
                        if (info != null) {
                            service = new SpheroService(info);
                            getServiceProvider().addService(service);
                        }
                    }
                    for (DeviceInfo dInfo : connectedDevices) {
                        if (dInfo.getDevice().getRobot().getIdentifier().equals(device.getIdentifier())
                                && device.isOnline()) {
                            devs.add(new SpheroParcelable(device.getIdentifier(),
                                    device.getName(),
                                    SpheroParcelable.SpheroState.Connected));
                        } else {
                            devs.add(new SpheroParcelable(device.getIdentifier(),
                                    device.getName(),
                                    SpheroParcelable.SpheroState.Delete));
                        }
                    }
                }
                res.putParcelableArrayListExtra(SettingActivity.EXTRA_DEVICES, devs);
                LocalBroadcastManager.getInstance(this).sendBroadcast(res);
            } else if (action.equals(ACTION_DELETE_DEVICE)) {
                String id = intent.getStringExtra(EXTRA_ID);
                Robot device = SpheroManager.INSTANCE.getNotConnectedDevice(id);

                DConnectService service = getServiceProvider().getService(id);
                if (service != null) {
                    service.setOnline(false);
                    getServiceProvider().removeService(id);
                    sendDevice(SettingActivity.ACTION_DELETED, new ConvenienceRobot(device), SpheroParcelable.SpheroState.Delete);
                }
            } else {
                return super.onStartCommand(intent, flags, startId);
            }
        }

        return START_STICKY;
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SpheroSystemProfile();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(mReceiver);
        SpheroManager.INSTANCE.shutdown();
    }


    @Override
    public void onDeviceFound(ConvenienceRobot sphero) {
        sendDevice(SettingActivity.ACTION_ADD_DEVICE, sphero, SpheroParcelable.SpheroState.Remember);
    }

    @Override
    public void onDeviceLost(ConvenienceRobot sphero) {
        sendDevice(SettingActivity.ACTION_REMOVE_DEVICE, sphero, SpheroParcelable.SpheroState.Delete);
    }

    @Override
    public void onDeviceLostAll() {
        sendDevice(SettingActivity.ACTION_REMOVE_DEVICE_ALL, null, SpheroParcelable.SpheroState.Delete);
    }


    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            Log.d("TEST","Plug-in : onManagerUninstalled");
        }
        EventManager.INSTANCE.removeAll();
        resetSpheroEvents();
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "Plug-in : onManagerTerminated");
        }
        EventManager.INSTANCE.removeAll();
        resetSpheroEvents();
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "Plug-in : onManagerEventTransmitDisconnected");
        }
        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }
        resetSpheroEvents();

    }


    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "Plug-in : onDevicePluginReset");
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
     * デバイスの情報を送る.
     * 
     * @param action アクション
     * @param sphero デバイス情報
     */
    private void sendDevice(final String action, final ConvenienceRobot sphero, final SpheroParcelable.SpheroState state) {
        Intent res = new Intent();
        res.setAction(action);
        SpheroParcelable s = null;

        if (sphero != null) {
            s = new SpheroParcelable(sphero.getRobot().getIdentifier(),
                    sphero.getRobot().getName(),
                    state);
        }
        res.putExtra(SettingActivity.EXTRA_DEVICE, s);
        LocalBroadcastManager.getInstance(this).sendBroadcast(res);
    }
}
