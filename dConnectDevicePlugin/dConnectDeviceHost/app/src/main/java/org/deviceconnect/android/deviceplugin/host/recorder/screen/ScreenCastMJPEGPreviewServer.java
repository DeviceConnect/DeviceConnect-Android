package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;
import java.net.Socket;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

    ScreenCastMJPEGPreviewServer(Context context, ScreenCastRecorder recorder, int port) {
        super(context, recorder);
        mScreenCastMgr = recorder.getScreenCastMgr();
        setPort(RecorderSetting.getInstance(getContext()).getPort(recorder.getId(), MIME_TYPE, port));
    }

    @Override
    public String getUri() {
        return mMJPEGServer == null ? null : mMJPEGServer.getUri();
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
        callback.onStart(getUri());
    }

    @Override
    public void stopWebServer() {
        if (mMJPEGServer != null) {
            mMJPEGServer.stop();
            mMJPEGServer = null;
        }
    }

    @Override
    public boolean requestSyncFrame() {
        // 何もしない
        return false;
    }

    @Override
    public void onConfigChange() {
        setEncoderQuality();

        if (mMJPEGServer != null) {
            new Thread(() -> {
                if (mMJPEGServer != null) {
                    mMJPEGServer.restartEncoder();
                }
            }).start();
        }
    }

    /**
     * JPEG のクオリティを取得します.
     *
     * @return JPEG のクオリティ
     */
    private int getJpegQuality() {
        return RecorderSetting.getInstance(getContext()).getJpegQuality(getRecorder().getId(), 40);
    }

    /**
     * エンコーダーの設定を行います.
     */
    private void setEncoderQuality() {
        if (mMJPEGServer != null) {
            MJPEGEncoder encoder = mMJPEGServer.getMJPEGEncoder();
            if (encoder != null) {
                setMJPEGQuality(encoder.getMJPEGQuality());
            }
        }
    }

    /**
     * MJPEG エンコーダの設定を行います.
     *
     * @param quality 設定を反映する MJPEGQuality
     */
    private void setMJPEGQuality(MJPEGQuality quality) {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();

        HostMediaRecorder.PictureSize size = recorder.getPreviewSize();

        quality.setWidth(size.getWidth());
        quality.setHeight(size.getHeight());
        quality.setQuality(getJpegQuality());
        quality.setFrameRate((int) recorder.getMaxFrameRate());
    }

    /**
     * MJPEGServerからのイベントを受け取るためのコールバック.
     */
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
            ScreenCastMJPEGEncoder encoder = new ScreenCastMJPEGEncoder(mScreenCastMgr);
            setMJPEGQuality(encoder.getMJPEGQuality());
            return encoder;
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
        }
    };
}
