/*
 UVCDeviceManager.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.core;


import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Build;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;

import org.deviceconnect.android.deviceplugin.uvc.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UVCDeviceManager {

    private static final long INTERVAL_MONITORING = 3000; // 3 seconds

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private final USBMonitor mUSBMonitor;

    private final List<UVCDevice> mAttachedDevices = new ArrayList<UVCDevice>();

    private final List<DeviceListener> mDeviceListeners = new ArrayList<DeviceListener>();

    private final List<ConnectionListener> mConnectionListeners = new ArrayList<ConnectionListener>();

    private final List<DiscoveryListener> mDiscoveryListeners = new ArrayList<DiscoveryListener>();

    private final List<PreviewListener> mPreviewListeners = new ArrayList<PreviewListener>();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final UVCDevice.PreviewListener mPreviewListener = new UVCDevice.PreviewListener() {
        @Override
        public void onFrame(final UVCDevice device, final byte[] frame, final int frameFormat,
                            final int width, final int height) {
            notifyPreviewFrame(device, frame, frameFormat, width, height);
        }
    };

    private boolean mIsStarted;

    private Thread mScanThread;

    private int mScanNum;

    public UVCDeviceManager(final Context context) {
        mUSBMonitor = new USBMonitor(context, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(final UsbDevice usbDevice) {
                if (!supportedAttachEvent()) {
                    return;
                }
                if (usbDevice == null) {
                    return;
                }
                mLogger.info("onAttach: " + usbDevice.getDeviceName());

                UVCDevice device = getDevice(usbDevice);
                if (device == null) {
                    device = new UVCDevice(usbDevice, UVCDeviceManager.this);
                    device.addPreviewListener(mPreviewListener);
                    pushDevice(device);
                    notifyEventOnFound(device);
                }
            }

            @Override
            public void onDettach(final UsbDevice usbDevice) {
                mLogger.info("onDettach: " + usbDevice.getDeviceName());
                pullDevice(usbDevice);
            }

            @Override
            public void onConnect(final UsbDevice usbDevice,
                                  final USBMonitor.UsbControlBlock ctrlBlock,
                                  final boolean createNew) {
                mLogger.info("onConnect: device = " + usbDevice.getDeviceName()
                    + ", ctrlBlock = " + ctrlBlock + ", createNew = " + createNew);

                UVCDevice device = getDevice(usbDevice);
                if (device != null) {
                    device.notifyPermission(ctrlBlock);
                }
            }

            @Override
            public void onDisconnect(final UsbDevice usbDevice,
                                     final USBMonitor.UsbControlBlock ctrlBlock) {
                mLogger.info("onDisconnect: " + usbDevice.getDeviceName());
                final UVCDevice device = getDevice(usbDevice);
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (device != null && device.disconnect()) {
                            notifyEventOnDisconnect(device);
                        }
                    }
                });
            }

            @Override
            public void onCancel(final UsbDevice usbDevice) {
                mLogger.info("onCancel");
                UVCDevice device = getDevice(usbDevice);
                if (device != null) {
                    device.notifyPermission(null);
                }
            }
        });

        List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(context, R.xml.device_filter);
        mUSBMonitor.setDeviceFilter(filters);
    }

    void requestPermission(final UsbDevice usbDevice) {
        mUSBMonitor.requestPermission(usbDevice);
    }

    public boolean connectDevice(final UVCDevice device) {
        mLogger.info("Connecting Device... : " + device.getName());
        if (device.connect()) {
            notifyEventOnConnect(device);
            return true;
        } else {
            notifyEventOnConnectionFailed(device);
            return false;
        }
    }

    public boolean connectDevice(final String id) {
        UVCDevice device = getDevice(id);
        if (device != null) {
            return connectDevice(device);
        } else {
            mLogger.warning("connectDevice: unknown device: " + device.getName());
            return false;
        }
    }

    public void disconnectDevice(final UVCDevice device) {
        if (device.disconnect()) {
            mLogger.info("disconnectDevice: closed " + device.getName());
            notifyEventOnDisconnect(device);
        } else {
            mLogger.info("disconnectDevice: already closed " + device.getName());
        }
    }

    public void disconnectDevice(final String id) {
        UVCDevice device = getDevice(id);
        if (device != null) {
            disconnectDevice(device);
        } else {
            mLogger.warning("disconnectDevice: unknown device: " + device.getName());
        }
    }

    private void pushDevice(final UVCDevice device) {
        synchronized (mAttachedDevices) {
            mAttachedDevices.add(device);
        }
    }

    private UVCDevice pullDevice(final UsbDevice usbDevice) {
        synchronized (mAttachedDevices) {
            for (Iterator<UVCDevice> it = mAttachedDevices.iterator(); it.hasNext(); ) {
                UVCDevice device = it.next();
                if (device.isSameDevice(usbDevice)) {
                    it.remove();
                    return device;
                }
            }
        }
        return null;
    }

    private UVCDevice getDevice(final UsbDevice usbDevice) {
        synchronized (mAttachedDevices) {
            for (Iterator<UVCDevice> it = mAttachedDevices.iterator(); it.hasNext(); ) {
                UVCDevice device = it.next();
                if (device.isSameDevice(usbDevice)) {
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

    private void notifyEventOnFound(final UVCDevice device) {
        synchronized (mDeviceListeners) {
            for (Iterator<DeviceListener> it = mDeviceListeners.iterator(); it.hasNext(); ) {
                final DeviceListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onFound(device);
                    }
                });
            }
        }
    }

    public void addConnectionListener(final ConnectionListener listener) {
        synchronized (mConnectionListeners) {
            mConnectionListeners.add(listener);
        }
    }

    public void removeConnectionListener(final ConnectionListener listener) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                ConnectionListener l = it.next();
                if (l == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    private void notifyEventOnConnect(final UVCDevice device) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                final ConnectionListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onConnect(device);
                    }
                });
            }
        }
    }

    private void notifyEventOnConnectionFailed(final UVCDevice device) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                final ConnectionListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onConnectionFailed(device);
                    }
                });
            }
        }
    }

    private void notifyEventOnDisconnect(final UVCDevice device) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                final ConnectionListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onDisconnect(device);
                    }
                });
            }
        }
    }

    public void addDiscoveryListener(final DiscoveryListener listener) {
        synchronized (mDiscoveryListeners) {
            mDiscoveryListeners.add(listener);
        }
    }

    public void removeDiscoveryListener(final DiscoveryListener listener) {
        synchronized (mDiscoveryListeners) {
            for (Iterator<DiscoveryListener> it = mDiscoveryListeners.iterator(); it.hasNext(); ) {
                DiscoveryListener l = it.next();
                if (l == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    private void notifyEventOnDiscovery(final List<UVCDevice> devices) {
        synchronized (mDiscoveryListeners) {
            for (Iterator<DiscoveryListener> it = mDiscoveryListeners.iterator(); it.hasNext(); ) {
                final DiscoveryListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onDiscovery(devices);
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

    public void removePreviewListener(final PreviewListener listener) {
        synchronized (mPreviewListeners) {
            for (Iterator<PreviewListener> it = mPreviewListeners.iterator(); it.hasNext(); ) {
                PreviewListener l = it.next();
                if (l == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    private void clearPreviewListeners() {
        synchronized (mPreviewListeners) {
            mPreviewListeners.clear();
        }
    }

    private void notifyPreviewFrame(final UVCDevice device, final byte[] frame, final int frameFormat,
                                    final int width, final int height) {
        synchronized (mPreviewListeners) {
            for (Iterator<PreviewListener> it = mPreviewListeners.iterator(); it.hasNext(); ) {
                final PreviewListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onFrame(device, frame, frameFormat, width, height);
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
        if (supportedAttachEvent()) {
            doScanOnce();
        } else {
            startScan();
        }
    }

    public synchronized void startScan() {
        if (mScanThread == null) {
            mScanThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    mLogger.info("Started UVC device monitoring: ");

                    // Find UVC devices connected to Host device already.
                    try {
                        do {
                            doScanOnce();
                            Thread.sleep(INTERVAL_MONITORING);
                        } while (mIsStarted);
                    } catch (InterruptedException e) {
                        // Nothing to do.
                    }
                    mLogger.info("Stopped UVC device monitoring.");
                }
            });
            mScanThread.start();
        }
        ++mScanNum;
    }

    private void doScanOnce() {
        List<UsbDevice> usbDevices = mUSBMonitor.getDeviceList();
        notifyEventOnDiscovery(getDeviceList());

        for (UsbDevice usbDevice : usbDevices) {
            if (getDevice(usbDevice) != null) {
                continue;
            }
            UVCDevice device = new UVCDevice(usbDevice, UVCDeviceManager.this);
            device.addPreviewListener(mPreviewListener);
            pushDevice(device);
            notifyEventOnFound(device);
        }
    }

    public synchronized void stopScan() {
        if (mScanThread != null) {
            --mScanNum;
            if (mScanNum == 0) {
                mScanThread.interrupt();
                mScanThread = null;
            }
        }
    }

    private boolean supportedAttachEvent() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        stopScan();
        mUSBMonitor.unregister();
        mIsStarted = false;
    }

    public List<UVCDevice> getDeviceList() {
        return mAttachedDevices;
    }

    public UVCDevice getDevice(final String id) {
        synchronized (mAttachedDevices) {
            for (UVCDevice device : mAttachedDevices) {
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
        void onFound(UVCDevice device);
    }

    public interface ConnectionListener {
        void onConnect(UVCDevice device);
        void onConnectionFailed(UVCDevice device);
        void onDisconnect(UVCDevice device);
    }

    public interface DiscoveryListener {
        void onDiscovery(List<UVCDevice> devices);
    }

    public interface PreviewListener {
        void onFrame(UVCDevice device, byte[] frame, int frameFormat, int width, int height);
    }
}
