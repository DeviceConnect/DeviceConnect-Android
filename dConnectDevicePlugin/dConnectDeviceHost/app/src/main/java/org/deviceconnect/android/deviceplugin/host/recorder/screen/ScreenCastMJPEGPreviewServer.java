package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSettingData;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.net.Socket;


@TargetApi(21)
class ScreenCastMJPEGPreviewServer extends ScreenCastPreviewServer {
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
    }

    @Override
    public int getQuality() {
        return RecorderSettingData.getInstance(mContext).readPreviewQuality(mServerProvider.getId());
    }

    @Override
    public void setQuality(int quality) {
        RecorderSettingData.getInstance(mContext).storePreviewQuality(mServerProvider.getId(), quality);
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
            mMJPEGServer.setServerPort(11000);
            mMJPEGServer.setCallback(mCallback);
            mMJPEGServer.start();
        }
        callback.onStart("http://localhost:11000/mjpeg");
    }

    @Override
    public void stopWebServer() {
        if (mMJPEGServer != null) {
            mMJPEGServer.stop();
            mMJPEGServer = null;
        }
    }

    @Override
    protected void onConfigChange() {
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
            HostDeviceRecorder.PictureSize size = getRotatedPreviewSize();

            ScreenCastMJPEGEncoder encoder = new ScreenCastMJPEGEncoder(mScreenCastMgr);
            MJPEGQuality quality = encoder.getMJPEGQuality();
            quality.setWidth(size.getWidth());
            quality.setHeight(size.getHeight());
            quality.setQuality(getQuality());
            quality.setFrameRate((int) mServerProvider.getMaxFrameRate());
            quality.setRotation(Camera2Wrapper.Rotation.FREE);
            return encoder;
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
        }
    };
}
