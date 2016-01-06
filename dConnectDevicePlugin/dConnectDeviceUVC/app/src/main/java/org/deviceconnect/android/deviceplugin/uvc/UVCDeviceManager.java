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

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

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
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mDeviceListeners) {
                    for (Iterator<DeviceListener> it = mDeviceListeners.iterator(); it.hasNext(); ) {
                        DeviceListener l = it.next();
                        l.onOpen(device);
                    }
                }
            }
        });
    }

    private void notifyEventOnClose(final UVCDevice device) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mDeviceListeners) {
                    for (Iterator<DeviceListener> it = mDeviceListeners.iterator(); it.hasNext(); ) {
                        DeviceListener l = it.next();
                        l.onClose(device);
                    }
                }
            }
        });
    }

    public void start() {
        mUSBMonitor.register();
    }

    public void stop() {
        mUSBMonitor.unregister();
    }

    public List<UVCDevice> getDeviceList() {
        return mConnectedDevices;
    }

    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    public interface DeviceListener {

        void onOpen(UVCDevice device);

        void onClose(UVCDevice device);

    }

}
