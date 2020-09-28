/*
 MidiMessageService.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Handler;
import android.os.Looper;
import androidx.preference.PreferenceManager;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static org.deviceconnect.android.deviceplugin.midi.MidiDeviceManager.OnDeviceDiscoveryListener;

/**
 * MIDI プラグイン本体.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiMessageService extends DConnectMessageService {

    private static final int DEMO_NOTIFICATION_ID = 1;

    private static final boolean LOCAL_OAUTH_DEFAULT = true;

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
        public void onDiscovery(final BluetoothDevice[] devices) {}

        @Override
        public void onDiscovery(final MidiDeviceInfo[] devices) {}

        @Override
        public void onConnected(final MidiDevice midiDevice) {
            MidiDeviceInfo deviceInfo = midiDevice.getInfo();
            if (deviceInfo == null) {
                mLogger.warning("No MIDI device info.");
                return;
            }
            String serviceId = DConnectMidiDeviceService.createServiceId(deviceInfo);
            DConnectMidiDeviceService service = (DConnectMidiDeviceService) getServiceProvider().getService(serviceId);
            if (service == null) {
                service = DConnectMidiDeviceService.getInstance(deviceInfo);
                if (service != null) {
                    service.setMidiDevice(midiDevice);
                    getServiceProvider().addService(service);
                }
            } else {
                service.setMidiDevice(midiDevice);
            }
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {}

        @Override
        public void onDisconnected(final MidiDeviceInfo deviceInfo) {
            String serviceId = DConnectMidiDeviceService.createServiceId(deviceInfo);
            if (serviceId != null) {
                DConnectService service = getServiceProvider().getService(serviceId);
                if (service instanceof DConnectMidiDeviceService) {
                    ((DConnectMidiDeviceService) service).destroy();
                }
            }
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = (shredPref, key) -> {
        if (key.equals(getString(R.string.settings_pref_key_local_oauth))) {
            setUseLocalOAuthFromPreference(shredPref);
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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setUseLocalOAuthFromPreference(pref);
        pref.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    private void setUseLocalOAuthFromPreference(final SharedPreferences pref) {
        boolean isOn = pref.getBoolean(getString(R.string.settings_pref_key_local_oauth), LOCAL_OAUTH_DEFAULT);
        mLogger.info("Changed Local OAuth setting: isOn = " + isOn);
        setUseLocalOAuth(isOn);
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