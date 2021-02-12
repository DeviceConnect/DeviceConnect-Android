package org.deviceconnect.android.deviceplugin.uvc.recorder.h264;

import android.content.Context;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.uvc.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcBroadcasterProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libuvc.FrameType;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UvcH264Recorder extends UvcRecorder {
    private static final String RECORDER_ID = "0";
    private static final String RECORDER_NAME = "uvc-h264";
    private static final String RECORDER_MIME_TYPE_MJPEG = "video/x-mjpeg";

    public UvcH264Recorder(Context context, UVCCamera camera) {
        super(context, camera);
    }

    @Override
    protected UvcSettings createSettings() {
        H264Settings mSettings = new H264Settings(getContext(), this);
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

            mSettings.setMjpegPort(11001);
            mSettings.setMjpegSSLPort(11101);
            mSettings.setRtspPort(12001);
            mSettings.setSrtPort(13001);

            mSettings.finishInitialization();
        }
        return mSettings;
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

    public class H264Settings extends UvcSettings {
        H264Settings(Context context, MediaRecorder recorder) {
            super(context, recorder);
        }

        @Override
        public List<Size> getSupportedPictureSizes() {
            List<Size> sizes = new ArrayList<>();
            try {
                List<Parameter> parameters = getUVCCamera().getParameter();
                for (Parameter p : parameters) {
                    if (p.getFrameType() == FrameType.H264) {
                        sizes.add(new Size(p.getWidth(), p.getHeight()));
                    } else if (p.getFrameType() == FrameType.MJPEG) {
                        if (p.hasExtH264()) {
                            sizes.add(new Size(p.getWidth(), p.getHeight()));
                        }
                    }
                }
            } catch (IOException e) {
                // ignore.
            }
            return sizes;
        }

        @Override
        public List<Size> getSupportedPreviewSizes() {
            List<Size> sizes = new ArrayList<>();
            try {
                List<Parameter> parameters = getUVCCamera().getParameter();
                for (Parameter p : parameters) {
                    if (p.getFrameType() == FrameType.H264) {
                        sizes.add(new Size(p.getWidth(), p.getHeight()));
                    } else if (p.getFrameType() == FrameType.MJPEG) {
                        if (p.hasExtH264()) {
                            sizes.add(new Size(p.getWidth(), p.getHeight()));
                        }
                    }
                }
            } catch (IOException e) {
                // ignore.
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
            List<Parameter> parameters = getUVCCamera().getParameter();
            for (Parameter p : parameters) {
                if (p.getFrameType() == FrameType.H264) {
                    if (p.getWidth() == previewSize.getWidth() && p.getHeight() == previewSize.getHeight()) {
                        parameter = p;
                        break;
                    }
                }
            }

            if (parameter == null) {
                for (Parameter p : parameters) {
                    if (p.getFrameType() == FrameType.MJPEG && p.hasExtH264()) {
                        if (p.getWidth() == previewSize.getWidth() && p.getHeight() == previewSize.getHeight()) {
                            parameter = p;
                            parameter.setUseH264(true);
                            break;
                        }
                    }
                }
            }

            return parameter;
        }
    }
}
