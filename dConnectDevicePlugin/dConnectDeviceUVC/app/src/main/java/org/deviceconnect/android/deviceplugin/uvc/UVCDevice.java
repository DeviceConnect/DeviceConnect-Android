package org.deviceconnect.android.deviceplugin.uvc;


import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.IPreviewFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.List;

public class UVCDevice {

    private final UsbDevice mDevice;

    private final USBMonitor.UsbControlBlock mCtrlBlock;

    private final UVCCamera mCamera;

    private PreviewListener mPreviewListener;

    UVCDevice(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
        mDevice = device;
        mCtrlBlock = ctrlBlock;
        mCamera = new UVCCamera();
    }

    public String getName() {
        return mDevice.getDeviceName();
    }

    boolean isSameDevice(final UsbDevice usbDevice) {
        return usbDevice.getDeviceName().equals(mDevice.getDeviceName());
    }

    void open() {
        mCamera.open(mCtrlBlock);

        List<Size> previewSizeList = mCamera.getSupportedSizeList();
        if (previewSizeList.size() > 0) {
            Size size = previewSizeList.get(0);
            mCamera.setPreviewSize(size.width, size.height, UVCCamera.FRAME_FORMAT_MJPEG);
        }
        mCamera.setPreviewFrameCallback(new IPreviewFrameCallback() {
            @Override
            public void onFrame(final byte[] frame) {
                if (mPreviewListener != null) {
                    mPreviewListener.onFrame(frame);
                }
            }
        }, UVCCamera.PIXEL_FORMAT_RAW);
    }

    void close() {
        mCamera.close();
    }

    void setPreviewListener(final PreviewListener listener) {
        mPreviewListener = listener;
    }

    void startPreview() {
        mCamera.startPreview();
    }

    void stopPreview() {
        mCamera.stopPreview();
    }

    public interface PreviewListener {

        void onFrame(byte[] frame);

    }

}
