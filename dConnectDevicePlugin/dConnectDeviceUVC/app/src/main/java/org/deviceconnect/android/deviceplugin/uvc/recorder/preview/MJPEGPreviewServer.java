package org.deviceconnect.android.deviceplugin.uvc.recorder.preview;

import com.serenegiant.usb.UVCCamera;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLContext;

public class MJPEGPreviewServer implements PreviewServer {

    private static final String SERVER_NAME = "UVC Plugin MotionJPEG Server";

    private final UVCDeviceManager mDeviceMgr;
    private final UVCDevice mDevice;
    private MJPEGServer mServer;
    protected int mPort;
    /**
     * SSLContext を使用するかどうかのフラグ.
     */
    private boolean mUsesSSLContext;
    /**
     * SSLContext のインスタンス.
     */
    private SSLContext mSSLContext;
    public MJPEGPreviewServer(final boolean isSSL, final UVCDeviceManager mgr, final UVCDevice device, final int port) {
        mDeviceMgr = mgr;
        mDevice = device;
        mPort = port;
        mUsesSSLContext = isSSL;
    }

    @Override
    public String getUrl() {
        return mServer == null ? null : mServer.getUri();
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
    public boolean usesSSLContext() {
        return mUsesSSLContext;
    }

    @Override
    public void setSSLContext(SSLContext sslContext) {
        mSSLContext = sslContext;
    }

    @Override
    public SSLContext getSSLContext() {
        return mSSLContext;
    }

    @Override
    public void start(final OnWebServerStartCallback callback) {
        if (mServer == null) {
            SSLContext sslContext = getSSLContext();
            if (usesSSLContext() && sslContext == null) {
                callback.onFail();
                return;
            }
            mServer = new MJPEGServer();
            if (sslContext != null) {
                mServer.setSSLContext(sslContext);
            }
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
