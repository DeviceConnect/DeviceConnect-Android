package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSettingData;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;
import java.net.Socket;

@TargetApi(21)
class ScreenCastMJPEGPreviewServer extends AbstractPreviewServer {
    /**
     * MJPEG のマイムタイプを定義します.
     */
    static final String MIME_TYPE = "video/x-mjpeg";

    /**
     * Android 端末の画面をキャストするクラス.
     */
    private final ScreenCastManager mScreenCastMgr;

    /**
     * MJPEG を配信するサーバ.
     */
    private MJPEGServer mMJPEGServer;

    ScreenCastMJPEGPreviewServer(Context context,
                                 AbstractPreviewServerProvider serverProvider,
                                 ScreenCastManager screenCastMgr) {
        super(context, serverProvider);
        mScreenCastMgr = screenCastMgr;
        setPort(11000);
    }

    @Override
    public int getQuality() {
        return RecorderSettingData.getInstance(getContext()).readPreviewQuality(getServerProvider().getId());
    }

    @Override
    public void setQuality(int quality) {
        RecorderSettingData.getInstance(getContext()).storePreviewQuality(getServerProvider().getId(), quality);
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        if (mMJPEGServer == null) {
            mMJPEGServer = new MJPEGServer();
            mMJPEGServer.setServerName("HostDevicePlugin Server");
            mMJPEGServer.setServerPort(getPort());
            mMJPEGServer.setCallback(mCallback);
            try {
                mMJPEGServer.start();
            } catch (IOException e) {
                callback.onFail();
                return;
            }
        }
        callback.onStart(mMJPEGServer.getUri());
    }

    @Override
    public void stopWebServer() {
        if (mMJPEGServer != null) {
            mMJPEGServer.stop();
            mMJPEGServer = null;
        }
        unregisterConfigChangeReceiver();
    }

    @Override
    public void onConfigChange() {
        if (mMJPEGServer != null) {
            new Thread(() -> {
                if (mMJPEGServer != null) {
                    mMJPEGServer.restartEncoder();
                }
            }).start();
        }
    }

    private final MJPEGServer.Callback mCallback = new MJPEGServer.Callback() {
        @Override
        public boolean onAccept(Socket socket) {
            return true;
        }

        @Override
        public void onClosed(Socket socket) {
        }

        @Override
        public MJPEGEncoder createMJPEGEncoder() {
            registerConfigChangeReceiver();

            HostDeviceRecorder.PictureSize size = getRotatedPreviewSize();

            ScreenCastMJPEGEncoder encoder = new ScreenCastMJPEGEncoder(mScreenCastMgr);
            MJPEGQuality quality = encoder.getMJPEGQuality();
            quality.setWidth(size.getWidth());
            quality.setHeight(size.getHeight());
            quality.setQuality(getQuality());
            quality.setFrameRate((int) getServerProvider().getMaxFrameRate());
            return encoder;
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
            unregisterConfigChangeReceiver();
        }
    };
}
