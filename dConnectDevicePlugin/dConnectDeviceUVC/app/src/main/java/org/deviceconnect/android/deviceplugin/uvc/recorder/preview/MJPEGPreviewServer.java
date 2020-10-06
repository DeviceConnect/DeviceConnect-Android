package org.deviceconnect.android.deviceplugin.uvc.recorder.preview;

import com.serenegiant.usb.UVCCamera;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;
import java.net.Socket;

public class MJPEGPreviewServer implements PreviewServer {

    private static final String SERVER_NAME = "UVC Plugin MotionJPEG Server";

    protected final UVCDeviceManager mDeviceMgr;
    protected final UVCDevice mDevice;
    private MJPEGServer mServer;
    protected int mPort;

    public MJPEGPreviewServer(final UVCDeviceManager mgr, final UVCDevice device, final int port) {
        mDeviceMgr = mgr;
        mDevice = device;
        mPort = port;
    }

    @Override
    public String getUrl() {
        return mServer.getUri();
    }

    @Override
    public String getMimeType() {
        return "video/x-mjpeg";
    }

    @Override
    public boolean isStarted() {
        return mServer != null;
    }

    @Override
    public void start(final OnWebServerStartCallback callback) {
        if (mServer == null) {
            mServer = new MJPEGServer();
            mServer.setServerName(SERVER_NAME);
            mServer.setServerPort(mPort);
            mServer.setCallback(mCallback);
            try {
                mServer.start();
            } catch (IOException e) {
                callback.onFail();
                return;
            }
        }
        callback.onStart(mServer.getUri());
    }

    @Override
    public void stop() {
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
    }

    protected MJPEGServer.Callback mCallback = new MJPEGServer.Callback() {
        @Override
        public boolean onAccept(Socket socket) {
            return true;
        }

        @Override
        public void onClosed(Socket socket) {
        }

        @Override
        public MJPEGEncoder createMJPEGEncoder() {
            return new UVCEncoder();
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
        }
    };

    private class UVCEncoder extends MJPEGEncoder implements UVCDeviceManager.PreviewListener {
        @Override
        public void start() {
            boolean isStarted = mDevice.startPreview();
            if (isStarted) {
                mDeviceMgr.addPreviewListener(this);
            }
        }

        @Override
        public void stop() {
            mDeviceMgr.removePreviewListener(this);
        }

        // UVCDeviceManager.PreviewListener

        @Override
        public void onFrame(UVCDevice device, byte[] frame, int frameFormat, int width, int height) {
            if (frameFormat != UVCCamera.FRAME_FORMAT_MJPEG) {
                return;
            }
            postJPEG(frame);
        }
    }
}
