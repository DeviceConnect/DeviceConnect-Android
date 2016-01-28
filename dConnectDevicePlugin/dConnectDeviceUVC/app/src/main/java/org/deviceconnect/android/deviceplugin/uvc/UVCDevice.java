/*
 UVCDevice.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import android.hardware.usb.UsbDevice;
import android.view.Surface;
import android.view.TextureView;

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

    private static final int VS_FORMAT_MJPEG = 0x06;

    private static final int[] SUPPORTED_PAYLOAD_FORMATS = {
        VS_FORMAT_MJPEG
    };

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private final UsbDevice mDevice;

    private final UVCDeviceManager mDeviceMgr;

    private USBMonitor.UsbControlBlock mCtrlBlock;

    private UVCCamera mCamera;

    private final String mId;

    private final List<PreviewListener> mPreviewListeners = new ArrayList<PreviewListener>();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    final Object mLockPermission = new Object();

    private boolean mIsPermitted;

    private boolean mIsInitialized;

    private boolean mIsOpen;

    private int mPreviewClientNum;

    private PreviewOption mCurrentOption;

    private Long mMinFrameInterval;

    private long mLastFrameTime = -1;

    private PendingPermissionRequest mPermissionRequest;

    UVCDevice(final UsbDevice device, final UVCDeviceManager deviceMgr) {
        mDevice = device;
        mId = Integer.toString(device.getDeviceId());
        mDeviceMgr = deviceMgr;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mDevice.getDeviceName();
    }

    public int getVendorId() {
        return mDevice.getVendorId();
    }

    public int getProductId() {
        return mDevice.getProductId();
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public boolean canPreview() {
        return mCurrentOption != null;
    }

    public boolean hasStartedPreview() {
        return mPreviewClientNum > 0;
    }

    boolean isSameDevice(final UsbDevice usbDevice) {
        return usbDevice.getDeviceName().equals(mDevice.getDeviceName());
    }

    void requestPermission() throws InterruptedException {
        synchronized (mLockPermission) {
            if (mPermissionRequest == null) {
                mPermissionRequest = new PendingPermissionRequest();
            }
            mDeviceMgr.requestPermission(mDevice);
            while (mPermissionRequest != null
                && mPermissionRequest.isWaitingResult()) {
                mLockPermission.wait(100);
            }
            mIsPermitted = mPermissionRequest.isPermitted();
            mPermissionRequest = null;
        }
    }

    void notifyPermission(final USBMonitor.UsbControlBlock ctrlBlock) {
        synchronized (mLockPermission) {
            if (mPermissionRequest != null) {
                mPermissionRequest.setResult(ctrlBlock != null);
                mCtrlBlock = ctrlBlock;
            }
            mLockPermission.notifyAll();
        }
    }

    synchronized boolean initialize() {
        if (mIsInitialized) {
            return true;
        }
        if (!mIsPermitted) {
            try {
                mLogger.info("Requesting permission...");
                requestPermission();
                mLogger.info("Received the response for permission request: result = " + mIsPermitted);
            } catch (InterruptedException e) {
                return false;
            }
        }
        if (!open()) { // Check supported video formats.
            return false;
        }
        mLogger.info("UVC device: name = " + getName() + ", supported format = " + mCamera.getSupportedSize());
        if (!close()) {
            return false;
        }
        mIsInitialized = true;
        return true;
    }

    synchronized boolean open() {
        if (mIsOpen) {
            return false;
        }
        if (!mIsPermitted) {
            return false;
        }

        mCamera = new UVCCamera();
        mCamera.open(mCtrlBlock);
        mIsOpen = true;

        List<Size> previewSizeList = mCamera.getSupportedSizeListAll();
        mLogger.info("Supported preview sizes: " + previewSizeList.size());
        Size size = selectSize(previewSizeList);
        if (size == null) {
            mLogger.warning("Preview size fof supported format (MJPEG or YUY2) is not found.");
            return false;
        }
        mLogger.info("Selected Preview size: type = " + size.type +  ", width = " + size.width + ", height = " + size.height);
        if (mCurrentOption == null) {
            mCurrentOption = new PreviewOption(size.width, size.height);
        }
        final int width = mCurrentOption.getWidth();
        final int height = mCurrentOption.getHeight();
        final int frameFormat = UVCCamera.FRAME_FORMAT_MJPEG;
        final int pixelFormat = UVCCamera.PIXEL_FORMAT_RAW;

        if (!setPreviewSize(width, height)) {
            mIsOpen = false;
            mCurrentOption = null;
            return false;
        }
        mCamera.setPreviewFrameCallback(new IPreviewFrameCallback() {
            @Override
            public void onFrame(final byte[] frame) {
                if (checkFrameInterval()) {
                    return;
                }
                notifyPreviewFrame(frame, frameFormat, width, height);
            }
        }, pixelFormat);

        return true;
    }

    private boolean supportsFormat(final Size previewSize) {
        for (int format : SUPPORTED_PAYLOAD_FORMATS) {
            if (previewSize.type == format) {
                return true;
            }
        }
        return false;
    }

    private Size selectSize(final List<Size> sizeList) {
        if (sizeList.size() == 0) {
            return null;
        }
        for (int format : SUPPORTED_PAYLOAD_FORMATS) {
            Size size = selectSize(sizeList, format);
            if (size != null) {
                return size;
            }
        }
        return null;
    }

    private Size selectSize(final List<Size> sizeList, final int format) {
        List<Size> list = new ArrayList<>();
        int i = 0;
        for (Size size : sizeList) {
            if (size.type == format) {
                list.add(size);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        Collections.sort(list, new Comparator<Size>() {
            @Override
            public int compare(final Size s1, final Size s2) {
                return s1.width * s1.height - s2.width * s2.height;
                //return s2.width * s2.height - s1.width * s1.height;
            }
        });
        return list.get(0);
    }

    private boolean checkFrameInterval() {
        if (mMinFrameInterval == null) {
            mLogger.info("checkFrameInterval: Use default interval.");
            return false;
        }
        long currentFrameTime = System.currentTimeMillis();
        if (mLastFrameTime < 0 || currentFrameTime - mLastFrameTime >= mMinFrameInterval) {
            mLastFrameTime = currentFrameTime;
            return false;
        }
        return true;
    }

    private void notifyPreviewFrame(final byte[] frame, final int frameFormat,
                                    final int width, final int height) {
        synchronized (mPreviewListeners) {
            for (Iterator<PreviewListener> it = mPreviewListeners.iterator(); it.hasNext(); ) {
                final PreviewListener l = it.next();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onFrame(UVCDevice.this, frame, frameFormat, width, height);
                    }
                });
            }
        }
    }

    synchronized boolean close() {
        if (!mIsOpen) {
            return false;
        }
        if (hasStartedPreview()) {
            mPreviewClientNum = 0;
            mCamera.stopPreview();
        }
        mIsOpen = false;

        mCamera.close();
        mCamera.destroy();
        mCamera = null;

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

    public boolean setPreviewSize(final int width, final int height) {
        mCurrentOption = new PreviewOption(width, height);
        try {
            if (mIsOpen) {
                mCamera.setPreviewSize(width, height, UVCCamera.FRAME_FORMAT_MJPEG);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setPreviewFrameRate(final Float maxFrameRate) {
        mMinFrameInterval = maxFrameRate == null ? null : (long) (1000 / maxFrameRate);
    }

    public boolean setNearestPreviewSize(final int requestedWidth,
                                         final int requestedHeight) {
        PreviewOption option = getNearestPreviewSize(requestedWidth, requestedHeight);
        return setPreviewSize(option.getWidth(), option.getHeight());
    }

    public PreviewOption getNearestPreviewSize(final int requestedWidth,
                                               final int requestedHeight) {
        List<PreviewOption> options = getPreviewOptions();
        final float ratio = requestedWidth / requestedHeight;
        final int area = requestedWidth * requestedHeight;
        Collections.sort(options, new Comparator<PreviewOption>() {
            @Override
            public int compare(final PreviewOption op1, final PreviewOption op2) {
                if (op1.getRatio() == op2.getRatio()) {
                    int d1 = Math.abs(area - op1.getWidth() * op2.getHeight());
                    int d2 = Math.abs(area - op2.getWidth() * op2.getHeight());
                    return d1 - d2;
                } else {
                    float d1 = Math.abs(ratio - op1.getRatio());
                    float d2 = Math.abs(ratio - op2.getRatio());
                    return d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
                }
            }
        });
        return options.get(0);
    }

    public void setPreviewDisplay(final TextureView display) {
        Surface surface = new Surface(display.getSurfaceTexture());
        mCamera.setPreviewDisplay(surface);
    }

    public void clearPreviewDisplay() {
        mCamera.setPreviewDisplay((Surface) null);
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
        boolean isFirstActiveClient = (++mPreviewClientNum) == 1;
        if (isFirstActiveClient) {
            mCamera.startPreview();
        }
        return true;
    }

    public synchronized boolean stopPreview() {
        if (!mIsOpen) {
            return false;
        }
        boolean isLastActiveClient = (mPreviewClientNum--) == 1;
        if (isLastActiveClient) {
            mCamera.stopPreview();
        }
        return true;
    }

    public int getPreviewWidth() {
        return mCurrentOption.getWidth();
    }

    public int getPreviewHeight() {
        return mCurrentOption.getHeight();
    }

    public synchronized List<PreviewOption> getPreviewOptions() {
        if (!mIsOpen) {
            return null;
        }
        List<PreviewOption> options = new ArrayList<PreviewOption>();
        List<Size> supportedSizes = mCamera.getSupportedSizeList();
        for (Size size : supportedSizes) {
            if (!supportsFormat(size)) {
                continue;
            }
            options.add(new PreviewOption(size));
        }
        return options;
    }

    private static class PendingPermissionRequest {

        Boolean mIsPermitted;

        boolean isWaitingResult() {
            return mIsPermitted == null;
        }

        void setResult(final boolean isPermitted) {
            mIsPermitted = isPermitted;
        }

        boolean isPermitted() {
            return mIsPermitted;
        }

    }

    interface PreviewListener {

        void onFrame(UVCDevice device, byte[] frame, int frameFormat, int width, int height);

    }

    public static class PreviewOption {

        private final int mWidth;

        private final int mHeight;

        private PreviewOption(final int width, final int height) {
            mWidth = width;
            mHeight = height;
        }

        private PreviewOption(final Size size) {
            this(size.width, size.height);
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public float getRatio() {
            return mWidth / mHeight;
        }
    }

}
