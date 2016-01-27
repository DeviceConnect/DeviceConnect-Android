/*
 UVCDeviceManager.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Build;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;

import java.lang.ref.WeakReference;
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

    private final List<PreviewListener> mPreviewListeners = new ArrayList<PreviewListener>();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final UVCDevice.PreviewListener mPreviewListener = new UVCDevice.PreviewListener() {
        @Override
        public void onFrame(final UVCDevice device, final byte[] frame, final int frameFormat,
                            final int width, final int height) {
            notifyPreviewFrame(device, frame, frameFormat, width, height);
        }
    };

    private final WeakReference<Context> mWeakContext;

    private boolean mIsStarted;

    private final Thread mMonitorThread = new Thread(new Runnable() {
        @Override
        public void run() {
            mLogger.info("Started UVC device monitoring: ");

            Context context = mWeakContext.get();
            if (context == null) {
                return;
            }

            // Find UVC devices connected to Host device already.
            try {
                do {
                    List<UsbDevice> usbDevices = mUSBMonitor.getDeviceList();
                    for (UsbDevice usbDevice : usbDevices) {
                        if (getDevice(usbDevice) != null) {
                            continue;
                        }
                        UVCDevice device = new UVCDevice(usbDevice, UVCDeviceManager.this);
                        device.addPreviewListener(mPreviewListener);
                        pushDevice(device);
                        notifyEventOnAttach(device);
                    }

                    Thread.sleep(INTERVAL_MONITORING);
                } while (mIsStarted && !supportedAttachEvent());
            } catch (InterruptedException e) {
                // Nothing to do.
            }

            mLogger.info("Stopped UVC device monitoring.");
        }
    });

    public UVCDeviceManager(final Context context) {
        mWeakContext = new WeakReference<Context>(context);
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
                    notifyEventOnAttach(device);
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
                UVCDevice device = getDevice(usbDevice);
                if (device != null && device.close()) {
                    notifyEventOnClose(device);
                }
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

    public boolean openDevice(final UVCDevice device) {
        mLogger.info("Opening Device... : " + device.getName());
        if (device.open()) {
            notifyEventOnOpen(device);
            return true;
        } else {
            return false;
        }
    }

    public void closeDevice(final UVCDevice device) {
        if (device.close()) {
            mLogger.info("closeDevice: closed " + device.getName());
            notifyEventOnClose(device);
        } else {
            mLogger.info("closeDevice: already closed " + device.getName());
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

    private void notifyEventOnAttach(final UVCDevice device) {
        synchronized (mDeviceListeners) {
            for (Iterator<DeviceListener> it = mDeviceListeners.iterator(); it.hasNext(); ) {
                final DeviceListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onAttach(device);
                    }
                });
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
        mMonitorThread.start();
    }

    private boolean supportedAttachEvent() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        mMonitorThread.interrupt();
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

        void onAttach(UVCDevice device);

        void onOpen(UVCDevice device);

        void onClose(UVCDevice device);

    }

    public interface PreviewListener {

        void onFrame(UVCDevice device, byte[] frame, int frameFormat, int width, int height);

    }
}
