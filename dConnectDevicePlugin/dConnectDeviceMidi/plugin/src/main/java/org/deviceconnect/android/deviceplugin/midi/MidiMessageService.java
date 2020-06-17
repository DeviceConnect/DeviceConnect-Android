package org.deviceconnect.android.deviceplugin.midi;

import android.content.Context;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.os.HandlerThread;

import org.deviceconnect.android.deviceplugin.midi.profiles.MidiSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;


public class MidiMessageService extends DConnectMessageService {

    private MidiManager mMidiManager;

    private MidiManager.DeviceCallback mDeviceCallback = new MidiManager.DeviceCallback() {
        @Override
        public void onDeviceAdded(final MidiDeviceInfo device) {
            onAddMidiDevice(device);
        }

        @Override
        public void onDeviceRemoved(final MidiDeviceInfo device) {
            onRemoveMidiDevice(device);
        }

        @Override
        public void onDeviceStatusChanged(final MidiDeviceStatus status) {
            // TODO ポートの開閉状態を反映
        }
    };

    private Handler mDeviceCallbackHandler;

    private Handler mDeviceConnectionHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread deviceCallbackThread = new HandlerThread("MidiDeviceCallbackThread");
        deviceCallbackThread.start();
        mDeviceCallbackHandler = new Handler(deviceCallbackThread.getLooper());

        HandlerThread deviceConnectionThread = new HandlerThread("MidiDeviceConnectionThread");
        deviceConnectionThread.start();
        mDeviceConnectionHandler = new Handler(deviceConnectionThread.getLooper());

        mMidiManager = (MidiManager) getSystemService(Context.MIDI_SERVICE);
        if (mMidiManager != null) {
            mMidiManager.registerDeviceCallback(mDeviceCallback, mDeviceCallbackHandler);
            for (MidiDeviceInfo deviceInfo : mMidiManager.getDevices()) {
                mDeviceCallbackHandler.post(() -> onAddMidiDevice(deviceInfo));
            }
        }
    }

    private void onAddMidiDevice(final MidiDeviceInfo deviceInfo) {
        mMidiManager.openDevice(deviceInfo, (midiDevice -> {
            for (MidiDeviceInfo.PortInfo port : deviceInfo.getPorts()) {
                String serviceId;
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                    serviceId = DConnectMidiInputService.createServiceId(deviceInfo, port);
                    DConnectService service = getServiceProvider().getService(serviceId);
                    if (service != null) {
                        service.setOnline(true);
                    } else {
                        service = DConnectMidiInputService.createService(midiDevice, port);
                        getServiceProvider().addService(service);
                    }
                } else if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    serviceId = DConnectMidiOutputService.createServiceId(deviceInfo, port);
                    DConnectService service = getServiceProvider().getService(serviceId);
                    if (service != null) {
                        service.setOnline(true);
                    } else {
                        service = DConnectMidiOutputService.createService(midiDevice, port);
                        getServiceProvider().addService(service);
                    }
                }
            }
        }), mDeviceConnectionHandler);
    }

    private void onRemoveMidiDevice(final MidiDeviceInfo deviceInfo) {
        mDeviceConnectionHandler.post(() -> {
            for (MidiDeviceInfo.PortInfo port : deviceInfo.getPorts()) {
                String serviceId = null;
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                    serviceId = DConnectMidiInputService.createServiceId(deviceInfo, port);
                } else if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    serviceId = DConnectMidiOutputService.createServiceId(deviceInfo, port);
                }
                if (serviceId != null) {
                    DConnectService service = getServiceProvider().getService(serviceId);
                    if (service != null) {
                        service.setOnline(false);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        mMidiManager.unregisterDeviceCallback(mDeviceCallback);

        mDeviceCallbackHandler.getLooper().quitSafely();
        mDeviceConnectionHandler.getLooper().quitSafely();
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