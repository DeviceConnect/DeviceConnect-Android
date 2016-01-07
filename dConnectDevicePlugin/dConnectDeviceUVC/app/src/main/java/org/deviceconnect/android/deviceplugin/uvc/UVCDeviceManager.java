/*
 UVCDeviceManager.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.USBMonitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UVCDeviceManager {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private final USBMonitor mUSBMonitor;

    private final List<UVCDevice> mConnectedDevices = new ArrayList<UVCDevice>();

    private final List<DeviceListener> mDeviceListeners = new ArrayList<DeviceListener>();

    private final List<PreviewListener> mPreviewListeners = new ArrayList<PreviewListener>();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final UVCDevice.PreviewListener mPreviewListener = new UVCDevice.PreviewListener() {
        @Override
        public void onFrame(final UVCDevice device, final byte[] frame) {
            notifyPreviewFrame(device, frame);
        }
    };

    private boolean mIsStarted;

    public UVCDeviceManager(final Context context) {
        mUSBMonitor = new USBMonitor(context, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(final UsbDevice usbDevice) {
                mLogger.info("onAttach: " + usbDevice.getDeviceName());
            }

            @Override
            public void onDettach(final UsbDevice usbDevice) {
                mLogger.info("onDettach: " + usbDevice.getDeviceName());
            }

            @Override
            public void onConnect(final UsbDevice usbDevice,
                                  final USBMonitor.UsbControlBlock ctrlBlock,
                                  final boolean createNew) {
                mLogger.info("onConnect: " + usbDevice.getDeviceName());
                if (createNew) {
                    UVCDevice device = new UVCDevice(usbDevice, ctrlBlock);
                    pushDevice(device);
                    device.addPreviewListener(mPreviewListener);
                    device.open();
                    notifyEventOnOpen(device);
                }
            }

            @Override
            public void onDisconnect(final UsbDevice usbDevice,
                                     final USBMonitor.UsbControlBlock ctrlBlock) {
                mLogger.info("onDisconnect: " + usbDevice.getDeviceName());
                UVCDevice device = pullDevice(usbDevice);
                device.close();
                notifyEventOnClose(device);
            }

            @Override
            public void onCancel() {
                mLogger.info("onCancel");
            }
        });
    }

    private void pushDevice(final UVCDevice device) {
        synchronized (mConnectedDevices) {
            mConnectedDevices.add(device);
        }
    }

    private UVCDevice pullDevice(final UsbDevice usbDevice) {
        synchronized (mConnectedDevices) {
            for (Iterator<UVCDevice> it = mConnectedDevices.iterator(); it.hasNext(); ) {
                UVCDevice device = it.next();
                if (device.isSameDevice(usbDevice)) {
                    it.remove();
                    return device;
                }
            }
        }
        return null;
    }

    public void addDeviceListener(final DeviceListener listener) {
        synchronized (mDeviceListeners) {
            mDeviceListeners.add(listener);
        }
    }

    public void removeDeviceListener(final DeviceListener listener) {
        synchronized (mDeviceListeners) {
            for (Iterator<DeviceListener> it = mDeviceListeners.iterator(); it.hasNext(); ) {
                DeviceListener l = it.next();
                if (l == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    private void notifyEventOnOpen(final UVCDevice device) {
        synchronized (mDeviceListeners) {
            for (Iterator<DeviceListener> it = mDeviceListeners.iterator(); it.hasNext(); ) {
                final DeviceListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onOpen(device);
                    }
                });
            }
        }
    }

    private void notifyEventOnClose(final UVCDevice device) {
        synchronized (mDeviceListeners) {
            for (Iterator<DeviceListener> it = mDeviceListeners.iterator(); it.hasNext(); ) {
                final DeviceListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onClose(device);
                    }
                });
            }
        }
    }

    public void addPreviewListener(final PreviewListener listener) {
        synchronized (mPreviewListeners) {
            for (Iterator<PreviewListener> it = mPreviewListeners.iterator(); it.hasNext(); ) {
                if (it.next() == listener) {
                    return;
                }
            }
            mPreviewListeners.add(listener);
        }
    }

    private void clearPreviewListeners() {
        synchronized (mPreviewListeners) {
            mPreviewListeners.clear();
        }
    }

    private void notifyPreviewFrame(final UVCDevice device, final byte[] frame) {
        synchronized (mPreviewListeners) {
            for (Iterator<PreviewListener> it = mPreviewListeners.iterator(); it.hasNext(); ) {
                final PreviewListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onFrame(device, frame);
                    }
                });
            }
        }
    }

    public synchronized void start() {
        if (mIsStarted) {
            return;
        }
        mIsStarted = true;
        mUSBMonitor.register();
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        mUSBMonitor.unregister();
        mIsStarted = false;
    }

    public List<UVCDevice> getDeviceList() {
        return mConnectedDevices;
    }

    public UVCDevice getDevice(final String id) {
        synchronized (mConnectedDevices) {
            for (UVCDevice device : mConnectedDevices) {
                if (device.getId().equals(id)) {
                    return device;
                }
            }
        }
        return null;
    }

    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    public interface DeviceListener {

        void onOpen(UVCDevice device);

        void onClose(UVCDevice device);

    }

    public interface PreviewListener {

        void onFrame(UVCDevice device, byte[] frame);

    }
}
