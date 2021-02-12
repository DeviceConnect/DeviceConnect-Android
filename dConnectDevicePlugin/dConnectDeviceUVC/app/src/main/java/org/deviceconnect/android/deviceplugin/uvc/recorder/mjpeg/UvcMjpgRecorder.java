package org.deviceconnect.android.deviceplugin.uvc.recorder.mjpeg;

import android.content.Context;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.PreviewServerProvider;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UvcMjpgRecorder extends AbstractMediaRecorder {

    private static final String RECORDER_ID = "1";
    private static final String RECORDER_NAME = "uvc-mjpeg";
    private static final String RECORDER_MIME_TYPE_MJPEG = "video/x-mjpeg";

    private final UVCCamera mUVCCamera;
    private final UvcSettings mSettings;

    public UvcMjpgRecorder(Context context, UVCCamera camera) {
        super(context);
        mUVCCamera = camera;
        mSettings = new UvcSettings(context, this);

        initSupportedSettings();
    }

    public UVCCamera getUVCCamera() {
        return mUVCCamera;
    }

    /**
     * レコーダの設定を初期化します.
     */
    private void initSupportedSettings() {
        if (!mSettings.isInitialized()) {
            mSettings.setPictureSize(new Size(640, 480));
            mSettings.setPreviewSize(new Size(640, 480));
            mSettings.setPreviewBitRate(2 * 1024 * 1024);
            mSettings.setPreviewMaxFrameRate(30);
            mSettings.setPreviewKeyFrameInterval(1);
            mSettings.setPreviewQuality(80);

            mSettings.setPreviewAudioSource(null);
            mSettings.setPreviewAudioBitRate(64 * 1024);
            mSettings.setPreviewSampleRate(16000);
            mSettings.setPreviewChannel(1);
            mSettings.setUseAEC(true);

            mSettings.setMjpegPort(11000);
            mSettings.setMjpegSSLPort(11100);
            mSettings.setRtspPort(12000);
            mSettings.setSrtPort(13000);

            mSettings.finishInitialization();
        }
    }

    @Override
    public synchronized void clean() {
        super.clean();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getId() {
        return RECORDER_ID;
    }

    @Override
    public String getName() {
        return RECORDER_NAME;
    }

    @Override
    public String getMimeType() {
        return RECORDER_MIME_TYPE_MJPEG;
    }

    @Override
    public MediaRecorder.Settings getSettings() {
        return mSettings;
    }

    @Override
    public PreviewServerProvider getServerProvider() {
        return null;
    }

    @Override
    public BroadcasterProvider getBroadcasterProvider() {
        return null;
    }

    @Override
    public EGLSurfaceDrawingThread getSurfaceDrawingThread() {
        return null;
    }

    @Override
    public void requestPermission(MediaRecorder.PermissionCallback callback) {
    }

    public class UvcSettings extends MediaRecorder.Settings {
        UvcSettings(Context context, MediaRecorder recorder) {
            super(context, recorder);
        }

        @Override
        public List<Size> getSupportedPictureSizes() {
            List<Size> sizes = new ArrayList<>();
            try {
                List<Parameter> parameters = mUVCCamera.getParameter();
                for (Parameter p : parameters) {
                    if (!p.hasExtH264()) {
                        sizes.add(new Size(p.getWidth(), p.getHeight()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sizes;
        }

        @Override
        public List<Size> getSupportedPreviewSizes() {
            List<Size> sizes = new ArrayList<>();
            try {
                List<Parameter> parameters = mUVCCamera.getParameter();
                for (Parameter p : parameters) {
                    if (!p.hasExtH264()) {
                        sizes.add(new Size(p.getWidth(), p.getHeight()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sizes;
        }

        /**
         * 設定されているパラメータに一致するパラメータを取得します.
         *
         * @return パラメータ
         * @throws IOException カメラ情報の取得に失敗した場合に例外が発生
         */
        public Parameter getParameter() throws IOException {
            Parameter parameter = null;
            Size previewSize = getPreviewSize();
            List<Parameter> parameters = mUVCCamera.getParameter();
            for (Parameter p : parameters) {
                if (!p.hasExtH264()) {
                    if (p.getWidth() == previewSize.getWidth() && p.getHeight() == previewSize.getHeight()) {
                        parameter = p;
                    }
                }
            }
            return parameter;
        }
    }
}