package org.deviceconnect.android.deviceplugin.uvc.profile;


import com.serenegiant.usb.UVCCamera;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.utils.MixedReplaceMediaServer;

import java.util.logging.Logger;

class MJPEGPreviewServer implements PreviewServer,
        UVCDeviceManager.PreviewListener,
        MixedReplaceMediaServer.Callback {

    private final MixedReplaceMediaServer mServer;
    private final UVCDeviceManager mDeviceMgr;
    private final UVCDevice mDevice;
    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    MJPEGPreviewServer(final UVCDeviceManager mgr,
                       final UVCDevice device) {
        MixedReplaceMediaServer server = new MixedReplaceMediaServer();
        server.setServerName("UVC Video Server");
        server.setContentType("image/jpg");
        server.setCallback(this);
        mServer = server;

        mDeviceMgr = mgr;
        mDevice = device;
    }

    @Override
    public String getUrl() {
        return mServer.getUrl();
    }

    @Override
    public String getMimeType() {
        return "video/x-mjpeg";
    }

    @Override
    public void start(final OnWebServerStartCallback callback) {
        mServer.start();
        callback.onStart(mServer.getUrl());
    }

    @Override
    public void stop() {
        mServer.stop();
        mDeviceMgr.removePreviewListener(this);
    }

    @Override
    public boolean onAccept() {
        boolean isStarted = mDevice.startPreview();;
        if (isStarted) {
            mDeviceMgr.addPreviewListener(this);
        }
        return isStarted;
    }

    @Override
    public void onFrame(final UVCDevice device, final byte[] frame, final int frameFormat, final int width, final int height) {
        if (frameFormat != UVCCamera.FRAME_FORMAT_MJPEG) {
            mLogger.warning("onFrame: unsupported frame format: " + frameFormat);
            return;
        }

        mServer.offerMedia(frame);
    }
}
