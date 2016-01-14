/*
 UVCDevice.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.IPreviewFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UVCDevice {

    private static final int VS_FORMAT_UNCOMPRESSED = 0x04;

    private static final int VS_FORMAT_MJPEG = 0x06;

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private final UsbDevice mDevice;

    private final USBMonitor.UsbControlBlock mCtrlBlock;

    private UVCCamera mCamera;

    private final String mId;

    private final List<PreviewListener> mPreviewListeners = new ArrayList<PreviewListener>();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private boolean mIsOpen;

    UVCDevice(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
        mDevice = device;
        mId = Integer.toString(device.getDeviceId());
        mCtrlBlock = ctrlBlock;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mDevice.getDeviceName();
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    boolean isSameDevice(final UsbDevice usbDevice) {
        return usbDevice.getDeviceName().equals(mDevice.getDeviceName());
    }

    synchronized boolean open() {
        if (mIsOpen) {
            return false;
        }
        mIsOpen = true;

        mCamera = new UVCCamera();
        mCamera.open(mCtrlBlock);

        List<Size> previewSizeList = mCamera.getSupportedSizeList();
        mLogger.info("Supported preview sizes: " + previewSizeList.size());
        Size size = selectSize(previewSizeList);
        int width;
        int height;
        if (size == null) {
            mLogger.warning("Preview size fof supported format (MJPEG or YUY2) is not found.");
            width = 1280;
            height = 720;
        } else {
            mLogger.info("Selected Preview size: type = " + size.type +  ", width = " + size.width + ", height = " + size.height);
            width = size.width;
            height = size.height;
        }

        mCamera.setPreviewSize(width, height, UVCCamera.FRAME_FORMAT_MJPEG);
        mCamera.setPreviewFrameCallback(new IPreviewFrameCallback() {
            @Override
            public void onFrame(final byte[] frame) {
                notifyPreviewFrame(frame);
            }
        }, UVCCamera.PIXEL_FORMAT_RAW);

        return true;
    }

    private Size selectSize(final List<Size> sizeList) {
        if (sizeList.size() == 0) {
            return null;
        }
        List<Size> mjpegList = new ArrayList<>();
        int i = 0;
        for (Size size : sizeList) {
            mLogger.info("Preview size (" + (i++) + ") : type = " + size.type
                + ", width = " + size.width + ", height = " + size.height);
            if (size.type == VS_FORMAT_MJPEG || size.type == VS_FORMAT_UNCOMPRESSED) {
                mjpegList.add(size);
            }
        }
        if (mjpegList.size() == 0) {
            return null;
        }
        Collections.sort(mjpegList, new Comparator<Size>() {
            @Override
            public int compare(final Size s1, final Size s2) {
                return s2.width * s2.height - s1.width * s1.height;
            }
        });
        return mjpegList.get(0);
    }

    private void notifyPreviewFrame(final byte[] frame) {
        synchronized (mPreviewListeners) {
            for (Iterator<PreviewListener> it = mPreviewListeners.iterator(); it.hasNext(); ) {
                final PreviewListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onFrame(UVCDevice.this, frame);
                    }
                });
            }
        }
    }

    synchronized boolean close() {
        if (!mIsOpen) {
            return false;
        }
        mIsOpen = false;

        mCamera.close();
        mCamera.destroy();

        return true;
    }

    void addPreviewListener(final PreviewListener listener) {
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

    public synchronized boolean startPreview() {
        if (!mIsOpen) {
            return false;
        }
        mCamera.startPreview();
        return true;
    }

    public synchronized boolean stopPreview() {
        if (!mIsOpen) {
            return false;
        }
        mCamera.stopPreview();
        return false;
    }

    interface PreviewListener {

        void onFrame(UVCDevice device, byte[] frame);

    }

}
