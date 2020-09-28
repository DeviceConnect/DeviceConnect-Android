/*
 MidiDeviceManager.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import org.deviceconnect.android.deviceplugin.midi.ble.BleDeviceDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * MIDI デバイス管理クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiDeviceManager {

    private final Logger mLogger = Logger.getLogger("midi-plugin");

    private final MidiManager mMidiManager;

    private final BleDeviceDetector mDetector;

    private final List<MidiDevice> mConnectedDevices = new ArrayList<>();

    private List<OnDeviceDiscoveryListener> mDeviceDiscoveryListener;

    private Handler mDeviceCallbackHandler;

    private Handler mDeviceConnectionHandler;

    private MidiManager.DeviceCallback mDeviceCallback = new MidiManager.DeviceCallback() {
        @Override
        public void onDeviceAdded(final MidiDeviceInfo device) {
            onAddMidiDevice(device);
        }

        @Override
        public void onDeviceRemoved(final MidiDeviceInfo deviceInfo) {
            synchronized (mConnectedDevices) {
                for (Iterator<MidiDevice> it = mConnectedDevices.iterator(); it.hasNext(); ) {
                    if (it.next().getInfo().getId() == deviceInfo.getId()) {
                        it.remove();
                        break;
                    }
                }
            }

            onRemoveMidiDevice(deviceInfo);
        }

        @Override
        public void onDeviceStatusChanged(final MidiDeviceStatus status) {
            // TODO ポートの開閉状態を反映
        }
    };

    private final BleDeviceDetector.BleDeviceDiscoveryListener mDiscoveryListener = (devices) -> {
        mLogger.fine("BleDeviceDiscoveryListener#onDiscovery: " + devices.size());

        if (mDeviceDiscoveryListener != null) {
            for (OnDeviceDiscoveryListener l : mDeviceDiscoveryListener) {
                l.onDiscovery(devices.toArray(new BluetoothDevice[0]));
            }
        }
    };

    private final MidiManager.OnDeviceOpenedListener mOpenedListener = (midiDevice -> {
        mLogger.info("OnDeviceOpenedListener: midiDevice = " + midiDevice);
        if (midiDevice == null) {
            return;
        }

        synchronized (mConnectedDevices) {
            if (!mConnectedDevices.contains(midiDevice)) {
                mConnectedDevices.add(midiDevice);
            }
        }

        if (mDeviceDiscoveryListener != null) {
            for (OnDeviceDiscoveryListener l : mDeviceDiscoveryListener) {
                l.onConnected(midiDevice);
            }
        }
    });

    public MidiDeviceManager(final Context context) {
        HandlerThread deviceCallbackThread = new HandlerThread("MidiDeviceCallbackThread");
        deviceCallbackThread.start();
        mDeviceCallbackHandler = new Handler(deviceCallbackThread.getLooper());

        HandlerThread deviceConnectionThread = new HandlerThread("MidiDeviceConnectionThread");
        deviceConnectionThread.start();
        mDeviceConnectionHandler = new Handler(deviceConnectionThread.getLooper());

        mMidiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        mMidiManager.registerDeviceCallback(mDeviceCallback, mDeviceCallbackHandler);

        mDeviceDiscoveryListener = new ArrayList<>();

        mDetector = new BleDeviceDetector(context);
        mDetector.setListener(mDiscoveryListener);
    }

    public void start() {
        MidiDeviceInfo[] devices = mMidiManager.getDevices();

        if (mDeviceDiscoveryListener != null) {
            for (OnDeviceDiscoveryListener l : mDeviceDiscoveryListener) {
                l.onDiscovery(devices);
            }
        }

        for (MidiDeviceInfo deviceInfo : devices) {
            mDeviceCallbackHandler.post(() -> onAddMidiDevice(deviceInfo));
        }
    }

    public void stop() {
        mDetector.stopScan();
        mMidiManager.unregisterDeviceCallback(mDeviceCallback);

        mDeviceCallbackHandler.getLooper().quitSafely();
        mDeviceConnectionHandler.getLooper().quitSafely();
    }

    public Set<BluetoothDevice> getBondedDevices() {
        Set<BluetoothDevice> devices = new LinkedHashSet<>();
        synchronized (mConnectedDevices) {
            for (MidiDevice midiDevice : mConnectedDevices) {
                Bundle props = midiDevice.getInfo().getProperties();
                if (props != null) {
                    BluetoothDevice device = props.getParcelable(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE);
                    if (device != null) {
                        devices.add(device);
                    }
                }
            }
        }
        return devices;
    }

    public void addOnDeviceDiscoveryListener(final OnDeviceDiscoveryListener listener) {
        mDeviceDiscoveryListener.add(listener);
    }

    public void removeOnDeviceDiscoveryListener(final OnDeviceDiscoveryListener listener) {
        mDeviceDiscoveryListener.remove(listener);
    }

    public void startScanBle() {
        if (mDetector.isEnabled()) {
            mDetector.startScan();
        }
    }

    public void stopScanBle() {
        if (mDetector.isEnabled()) {
            mDetector.startScan();
        }
    }

    public boolean isEnabledBle() {
        return mDetector != null && mDetector.isEnabled();
    }

    public void connectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            connectBleDevice(blue);
        }
    }

    private void connectBleDevice(final BluetoothDevice bluetoothDevice) {
        mMidiManager.openBluetoothDevice(bluetoothDevice, mOpenedListener, mDeviceConnectionHandler);
    }

    public void disconnectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            disconnectBleDevice(blue);
        }
    }

    private void disconnectBleDevice(final BluetoothDevice bluetoothDevice) {
        synchronized (mConnectedDevices) {
            for (MidiDevice midiDevice : mConnectedDevices) {
                Bundle props = midiDevice.getInfo().getProperties();
                if (props != null) {
                    BluetoothDevice device = props.getParcelable(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE);
                    if (device != null && device.getAddress().equals(bluetoothDevice.getAddress())) {
                        try {
                            midiDevice.close();
                        } catch (IOException e) {
                            mLogger.warning("Failed to close MIDI device (Bluetooth): address = " + bluetoothDevice.getAddress());
                        }
                        break;
                    }
                }
            }
        }
    }

    private void onAddMidiDevice(final MidiDeviceInfo deviceInfo) {
        mMidiManager.openDevice(deviceInfo, mOpenedListener, mDeviceConnectionHandler);
    }

    private void onRemoveMidiDevice(final MidiDeviceInfo deviceInfo) {
        mDeviceConnectionHandler.post(() -> {
            if (mDeviceDiscoveryListener != null) {
                for (OnDeviceDiscoveryListener l : mDeviceDiscoveryListener) {
                    l.onDisconnected(deviceInfo);
                }
            }
        });
    }

    public boolean containsConnectedBleDevice(final String address) {
        synchronized (mConnectedDevices) {
            for (MidiDevice midiDevice : mConnectedDevices) {
                Bundle props = midiDevice.getInfo().getProperties();
                if (props != null) {
                    BluetoothDevice device = props.getParcelable(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE);
                    if (device != null && device.getAddress().equals(address)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public interface OnDeviceDiscoveryListener {
        void onDiscovery(BluetoothDevice[] devices);
        void onDiscovery(MidiDeviceInfo[] devices);
        void onConnected(MidiDevice device);
        void onConnectFailed(BluetoothDevice device);
        void onDisconnected(MidiDeviceInfo device);
    }
}
