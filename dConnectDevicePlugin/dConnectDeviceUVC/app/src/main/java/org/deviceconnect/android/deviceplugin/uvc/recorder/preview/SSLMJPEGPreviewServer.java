package org.deviceconnect.android.deviceplugin.uvc.recorder.preview;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;

import javax.net.ssl.SSLContext;

public class SSLMJPEGPreviewServer extends  MJPEGPreviewServer implements PreviewServer {

    private static final String SERVER_NAME = "UVC Plugin SSL MotionJPEG Server";

    private MJPEGServer mServer;
    /**
     * SSL Context.
     */
    private SSLContext mSSLContext;

    public SSLMJPEGPreviewServer(final SSLContext sslContext, final UVCDeviceManager mgr, final UVCDevice device, final int port) {
        super(mgr, device, port);
        mSSLContext = sslContext;
    }

    @Override
    public String getUrl() {
        return mServer.getUri();
    }

    @Override
    public boolean isStarted() {
        return mServer != null;
    }

    @Override
    public void start(final OnWebServerStartCallback callback) {
        if (mServer == null) {
            mServer = new MJPEGServer();
            if (mSSLContext != null) {
                mServer.setSSLContext(mSSLContext);
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

}
