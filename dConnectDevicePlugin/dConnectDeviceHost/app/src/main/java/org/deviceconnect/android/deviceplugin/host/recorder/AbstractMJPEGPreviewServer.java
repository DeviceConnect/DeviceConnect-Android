package org.deviceconnect.android.deviceplugin.host.recorder;

import android.util.Log;
import android.util.Size;

import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.net.Socket;

import javax.net.ssl.SSLContext;

public abstract class AbstractMJPEGPreviewServer extends AbstractPreviewServer {
    /**
     * Motion JPEG のマイムタイプを定義します.
     */
    public static final String MIME_TYPE = "video/x-mjpeg";

    /**
     * サーバー名を定義します.
     */
    private static final String SERVER_NAME = "Android Host Camera2 MJPEG Server";

    /**
     * MotionJPEG 配信サーバ.
     */
    private MJPEGServer mMJPEGServer;

    public AbstractMJPEGPreviewServer(HostMediaRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    // PreviewServer

    @Override
    public String getUri() {
        return mMJPEGServer == null ? null : mMJPEGServer.getUri();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public boolean isRunning() {
        return mMJPEGServer != null;
    }

    @Override
    public void start(final OnStartCallback callback) {
        if (mMJPEGServer == null) {
            SSLContext sslContext = getSSLContext();
            if (useSSLContext() && sslContext == null) {
                callback.onFailed(new RuntimeException("Failed to create a SSLContext."));
                return;
            }

            mMJPEGServer = new MJPEGServer();
            mMJPEGServer.setServerName(SERVER_NAME);
            mMJPEGServer.setServerPort(getEncoderSettings().getPort());
            mMJPEGServer.setCallback(mCallback);
            if (useSSLContext()) {
                mMJPEGServer.setSSLContext(sslContext);
            }
            try {
                mMJPEGServer.start();
            } catch (Exception e) {
                callback.onFailed(e);
                return;
            }
        }
        callback.onSuccess();
    }

    @Override
    public void stop() {
        if (mMJPEGServer != null) {
            mMJPEGServer.stop();
            mMJPEGServer = null;
        }
    }

    @Override
    public long getBPS() {
        return mMJPEGServer != null ? mMJPEGServer.getBPS() : 0;
    }

    @Override
    public void onConfigChange() {
        setEncoderQuality();
        restartEncoder();
    }

    /**
     * エンコーダの設定を行います.
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
     * エンコーダを再スタートさせて、設定を反映します.
     */
    private void restartEncoder() {
        if (mMJPEGServer != null) {
            new Thread(() -> {
                if (mMJPEGServer != null) {
                    mMJPEGServer.restartEncoder();
                }
            }).start();
        }
    }

    /**
     * MJPEG の設定を行います.
     *
     * @param quality 設定を行う MJPEGQuality
     */
    private void setMJPEGQuality(MJPEGQuality quality) {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.EncoderSettings settings = getEncoderSettings();

        EGLSurfaceDrawingThread d = recorder.getSurfaceDrawingThread();
        Size previewSize = settings.getPreviewSize();
        if (previewSize == null) {
            previewSize = settings.getPreviewSize();
        }
        int w = d.isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
        int h = d.isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
        quality.setWidth(w);
        quality.setHeight(h);
        quality.setQuality(settings.getPreviewQuality());
        quality.setFrameRate(settings.getPreviewMaxFrameRate());
        quality.setDrawingRange(settings.getCropRect());
    }

    /**
     * MJPEG 用のエンコーダを作成します.
     *
     * @return MJPEG 用のエンコーダ
     */
    protected abstract MJPEGEncoder createSurfaceMJPEGEncoder();

    /**
     * MJPEGServer からのイベントを受け取るためのコールバック.
     */
    private final MJPEGServer.Callback mCallback = new MJPEGServer.Callback() {
        @Override
        public boolean onAccept(Socket socket) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#onAccept: ");
                Log.d(TAG, "  socket: " + socket);
            }
            // 特に制限を付けないので、常に true を返却
            return true;
        }

        @Override
        public void onClosed(Socket socket) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#onClosed: ");
                Log.d(TAG, "  socket: " + socket);
            }
        }

        @Override
        public MJPEGEncoder createMJPEGEncoder() {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#createMJPEGEncoder: ");
            }

            MJPEGEncoder encoder = createSurfaceMJPEGEncoder();
            setMJPEGQuality(encoder.getMJPEGQuality());
            return encoder;
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#releaseMJPEGEncoder: ");
            }
        }
    };
}
