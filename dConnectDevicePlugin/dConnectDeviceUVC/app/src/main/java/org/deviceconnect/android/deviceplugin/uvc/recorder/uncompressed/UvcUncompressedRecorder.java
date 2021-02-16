package org.deviceconnect.android.deviceplugin.uvc.recorder.uncompressed;

import android.content.Context;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.libuvc.FrameType;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UvcUncompressedRecorder extends UvcRecorder {
    private static final String RECORDER_ID = "2";
    private static final String RECORDER_NAME = "uncompressed";
    private static final String RECORDER_MIME_TYPE_MJPEG = "video/x-mjpeg";

    public UvcUncompressedRecorder(Context context, UVCCamera camera) {
        super(context, camera);
    }

    @Override
    protected UvcSettings createSettings() {
        UncompressedSettings settings = new UncompressedSettings(getContext());
        if (!settings.isInitialized()) {
            List<Size> supportPictureSizes = settings.getSupportedPictureSizes();
            List<Size> supportPreviewSizes = settings.getSupportedPreviewSizes();

            settings.setPictureSize(supportPictureSizes.get(0));
            settings.setPreviewSize(supportPreviewSizes.get(0));

            settings.setPreviewBitRate(2 * 1024 * 1024);
            settings.setPreviewMaxFrameRate(30);
            settings.setPreviewKeyFrameInterval(1);
            settings.setPreviewQuality(80);

            settings.setPreviewAudioSource(null);
            settings.setPreviewAudioBitRate(64 * 1024);
            settings.setPreviewSampleRate(16000);
            settings.setPreviewChannel(1);
            settings.setUseAEC(true);

            settings.setMjpegPort(11002);
            settings.setMjpegSSLPort(11102);
            settings.setRtspPort(12002);
            settings.setSrtPort(13002);

            settings.finishInitialization();
        }
        return settings;
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

    public class UncompressedSettings extends UvcSettings {
        UncompressedSettings(Context context) {
            super(context);
        }

        @Override
        public List<Size> getSupportedPictureSizes() {
            List<Size> sizes = new ArrayList<>();
            try {
                List<Parameter> parameters = getUVCCamera().getParameter();
                for (Parameter p : parameters) {
                    if (p.getFrameType() == FrameType.UNCOMPRESSED) {
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
                List<Parameter> parameters = getUVCCamera().getParameter();
                for (Parameter p : parameters) {
                    if (p.getFrameType() == FrameType.UNCOMPRESSED) {
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
            List<Parameter> parameters = getUVCCamera().getParameter();
            for (Parameter p : parameters) {
                if (p.getFrameType() == FrameType.UNCOMPRESSED) {
                    if (p.getWidth() == previewSize.getWidth() && p.getHeight() == previewSize.getHeight()) {
                        parameter = p;
                        parameter.setUseH264(false);
                    }
                }
            }
            return parameter;
        }
    }
}
