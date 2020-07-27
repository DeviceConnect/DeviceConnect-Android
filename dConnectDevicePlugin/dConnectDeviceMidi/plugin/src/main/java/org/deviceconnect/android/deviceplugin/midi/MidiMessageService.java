package org.deviceconnect.android.deviceplugin.midi;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Handler;
import android.os.Looper;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static org.deviceconnect.android.deviceplugin.midi.MidiDeviceManager.OnDeviceDiscoveryListener;

public class MidiMessageService extends DConnectMessageService {

    private final int DEMO_NOTIFICATION_ID = 1;

    private final Logger mLogger = Logger.getLogger("midi-plugin");

    private MidiDeviceManager mMidiDeviceManager;

    private DemoInstaller mDemoInstaller;

    private DemoInstaller.Notification mDemoNotification;

    private final BroadcastReceiver mDemoNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            mDemoNotification.cancel(context);
            if (DemoInstaller.Notification.ACTON_UPDATE_DEMO.equals(action)) {
                updateDemoPage(context);
            }
        }
    };

    private final OnDeviceDiscoveryListener mDeviceDiscoveryListener = new OnDeviceDiscoveryListener() {
        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {

        }

        @Override
        public void onConnected(final MidiDevice midiDevice) {
            MidiDeviceInfo deviceInfo = midiDevice.getInfo();
            if (deviceInfo == null) {
                mLogger.warning("No MIDI device info.");
                return;
            }
            for (MidiDeviceInfo.PortInfo port : deviceInfo.getPorts()) {
                String serviceId;
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                    serviceId = DConnectMidiInputService.createServiceId(deviceInfo, port);
                    DConnectService service = getServiceProvider().getService(serviceId);
                    if (service != null) {
                        service.setOnline(true);
                    } else {
                        service = DConnectMidiInputService.createService(midiDevice, port);
                        if (service != null) {
                            getServiceProvider().addService(service);
                        }
                    }
                } else if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    serviceId = DConnectMidiOutputService.createServiceId(deviceInfo, port);
                    DConnectService service = getServiceProvider().getService(serviceId);
                    if (service != null) {
                        service.setOnline(true);
                    } else {
                        service = DConnectMidiOutputService.createService(midiDevice, port);
                        if (service != null) {
                            getServiceProvider().addService(service);
                        }
                    }
                }
            }
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {

        }

        @Override
        public void onDisconnected(final MidiDeviceInfo deviceInfo) {
            for (MidiDeviceInfo.PortInfo port : deviceInfo.getPorts()) {
                String serviceId = null;
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                    serviceId = DConnectMidiInputService.createServiceId(deviceInfo, port);
                } else if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    serviceId = DConnectMidiOutputService.createServiceId(deviceInfo, port);
                }
                if (serviceId != null) {
                    DConnectService service = getServiceProvider().getService(serviceId);
                    if (service instanceof DConnectMidiService) {
                        ((DConnectMidiService) service).destroy();
                        service.setOnline(false);
                    }
                }
            }
        }
    };

    public MidiDeviceManager getMidiDeviceManager() {
        return mMidiDeviceManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMidiDeviceManager = new MidiDeviceManager(getApplicationContext());
        mMidiDeviceManager.addOnDeviceDiscoveryListener(mDeviceDiscoveryListener);
        mMidiDeviceManager.start();

        mDemoInstaller = new MidiDemoInstaller(getApplicationContext());
        mDemoNotification = new DemoInstaller.Notification(
                DEMO_NOTIFICATION_ID,
                getString(R.string.app_name),
                R.drawable.ic_launcher,
                "org.deviceconnect.android.deviceplugin.midi.channel.demo",
                "MIDI Plug-in, Demo",
                "MIDI Plug-in, Demo"
        );
        registerDemoNotification();
        updateDemoPageIfNeeded();
    }

    private void registerDemoNotification() {
        IntentFilter filter  = new IntentFilter();
        filter.addAction(DemoInstaller.Notification.ACTON_CONFIRM_NEW_DEMO);
        filter.addAction(DemoInstaller.Notification.ACTON_UPDATE_DEMO);
        registerReceiver(mDemoNotificationReceiver, filter);
    }

    private void updateDemoPageIfNeeded() {
        if (mDemoInstaller.isUpdateNeeded()) {
            updateDemoPage(getApplicationContext());
        }
    }

    private void updateDemoPage(final Context context) {
        mDemoInstaller.update(new DemoInstaller.UpdateCallback() {
            @Override
            public void onBeforeUpdate(final File demoDir) {
                // 自動更新を実行する直前
            }

            @Override
            public void onAfterUpdate(final File demoDir) {
                // 自動更新に成功した直後
                mDemoNotification.showUpdateSuccess(context);
            }

            @Override
            public void onFileError(final IOException e) {
                // 自動更新時にファイルアクセスエラーが発生した場合
                mDemoNotification.showUpdateError(context);
            }

            @Override
            public void onUnexpectedError(final Throwable e) {
                // 自動更新時に不明なエラーが発生した場合
                mDemoNotification.showUpdateError(context);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mDemoNotificationReceiver);
        mMidiDeviceManager.removeOnDeviceDiscoveryListener(mDeviceDiscoveryListener);
        mMidiDeviceManager.stop();
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new MidiSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        // TODO Device Connect Managerアンインストール時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerTerminated() {
        // TODO Device Connect Manager停止時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // TODO アプリとのWebSocket接続が切断された時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onDevicePluginReset() {
        // TODO Device Connect Managerの設定画面上で「プラグイン再起動」を要求された場合の処理. 実装は任意.
    }
}