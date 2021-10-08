/*
 HostDeviceScreenCast.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Range;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MP4Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.SurfaceMP4Recorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Host Device Screen Cast.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenCastRecorder extends AbstractMediaRecorder {
    private static final String ID = "screen";
    private static final String NAME = "AndroidHost Screen";

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_screen_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".jpg";

    /** 日付のフォーマット. */
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private final ScreenCastManager mScreenCastMgr;
    private final ScreenCastSettings mSettings;
    private final Handler mImageReaderHandler = new Handler(Looper.getMainLooper());

    private final ScreenCastPreviewServerProvider mScreenCastPreviewServerProvider;
    private final ScreenCastBroadcasterProvider mScreenCastBroadcasterProvider;
    private final ScreenCastSurfaceDrawingThread mScreenCastSurfaceDrawingThread;

    public ScreenCastRecorder(Context context, FileManager fileMgr, MediaProjectionProvider provider) {
        super(context, fileMgr, provider);

        mSettings = new ScreenCastSettings(context, this);

        initSupportedSettings();

        mScreenCastMgr = new ScreenCastManager(context, provider);
        mScreenCastSurfaceDrawingThread = new ScreenCastSurfaceDrawingThread(this);
        mScreenCastPreviewServerProvider = new ScreenCastPreviewServerProvider(context, this);
        mScreenCastBroadcasterProvider = new ScreenCastBroadcasterProvider(context, this);
    }

    private List<Size> getEncoderSizeList(List<Size> supportPreviewSizes) {
        List<Size> sizes = new ArrayList<>();
        Size maxSize = CapabilityUtil.getSupportedMaxSize(VideoCodec.H264.getMimeType());
        for (Size size : supportPreviewSizes) {
            if (size.getWidth() <= maxSize.getWidth() && size.getHeight() <= maxSize.getHeight()
                    || size.getWidth() <= maxSize.getHeight() && size.getHeight() <= maxSize.getWidth()) {
                sizes.add(size);
            }
        }
        return sizes;
    }

    /**
     * レコーダの設定を初期化します.
     */
    private void initSupportedSettings() {
        Size originalSize = getDisplaySize();
        List<Size> supportPictureSizes = new ArrayList<>();
        List<Size> supportPreviewSizes = new ArrayList<>();
        final int num = 4;
        final int w = originalSize.getWidth();
        final int h = originalSize.getHeight();
        for (int i = 1; i <= num; i++) {
            float scale = i / ((float) num);
            // MediaCodec に解像度を渡す時に端数を持っているとエラーになってしまう
            // 場合があったので、キリの良い値になるように調整しています。
            int width = (int) (w * scale);
            int height = (int) (h * scale);
            width += 10 - (width % 10);
            height += 10 - (height % 10);

            Size size = new Size(width, height);
            supportPictureSizes.add(size);
            supportPreviewSizes.add(size);
        }
        mSettings.mSupportedPreviewSize = supportPreviewSizes;
        mSettings.mSupportedPictureSize = supportPictureSizes;
        mSettings.mSupportEncoderSizeList = getEncoderSizeList(supportPreviewSizes);

        List<Range<Integer>> supportFps = new ArrayList<>();
        supportFps.add(new Range<>(30, 30));
        mSettings.mSupportedFps = supportFps;

        if (!mSettings.isInitialized()) {
            mSettings.setPreviewSize(mSettings.getSupportedPreviewSizes().get(0));
            mSettings.setPictureSize(mSettings.getSupportedPictureSizes().get(0));
            mSettings.setOrientation(Surface.ROTATION_90);

            // 音声設定
            mSettings.setPreviewAudioSource(null);
            mSettings.setPreviewAudioBitRate(128 * 1024);
            mSettings.setPreviewSampleRate(48000);
            mSettings.setPreviewChannel(1);
            mSettings.setUseAEC(true);

            // プレビュー配信サーバ設定
            mSettings.addEncoder(getId() + "-MJPEG");
            EncoderSettings mjpeg = mSettings.getEncoderSetting(getId() + "-MJPEG");
            mjpeg.setName("MJPEG");
            mjpeg.setMimeType(MimeType.MJPEG);
            mjpeg.setPort(21000);
            mjpeg.setPreviewSize(mSettings.getSupportedPreviewSizes().get(0));
            mjpeg.setPreviewQuality(80);
            mjpeg.setPreviewMaxFrameRate(30);

            mSettings.addEncoder(getId() + "-RTSP");
            EncoderSettings rtsp = mSettings.getEncoderSetting(getId() + "-RTSP");
            rtsp.setName("RTSP");
            rtsp.setMimeType(MimeType.RTSP);
            rtsp.setPort(22000);
            rtsp.setPreviewSize(mSettings.getSupportedPreviewSizes().get(0));
            rtsp.setPreviewBitRate(2 * 1024 * 1024);
            rtsp.setPreviewMaxFrameRate(30);
            rtsp.setPreviewKeyFrameInterval(5);

            mSettings.addEncoder(getId() + "-SRT");
            EncoderSettings srt = mSettings.getEncoderSetting(getId() + "-SRT");
            srt.setName("SRT");
            srt.setMimeType(MimeType.SRT);
            srt.setPort(23000);
            srt.setPreviewSize(mSettings.getSupportedPreviewSizes().get(0));
            srt.setPreviewBitRate(2 * 1024 * 1024);
            srt.setPreviewMaxFrameRate(30);
            srt.setPreviewKeyFrameInterval(5);

            // 配信設定
            mSettings.addEncoder(getId() + "-RTMP");
            EncoderSettings rtmp = mSettings.getEncoderSetting(getId() + "-RTMP");
            rtmp.setName("RTMP");
            rtmp.setMimeType(MimeType.RTMP);
            rtmp.setPreviewSize(mSettings.getSupportedPreviewSizes().get(0));
            rtmp.setPreviewBitRate(2 * 1024 * 1024);
            rtmp.setPreviewMaxFrameRate(30);
            rtmp.setPreviewKeyFrameInterval(5);
            rtmp.setBroadcastURI("rtmp://localhost:1935");

            mSettings.finishInitialization();
        }
    }

    /**
     * 画面のサイズを取得します.
     *
     * @return 画面サイズ
     */
    private Size getDisplaySize() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }

        int insetsWidth = 0;
        int insetsHeight = 0;
        Size displaySize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
            WindowInsets windowInsets = windowMetrics.getWindowInsets();
            Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()
                    | WindowInsets.Type.displayCutout());
            int width = windowMetrics.getBounds().width();
            int height = windowMetrics.getBounds().height();
            insetsWidth = insets.right + insets.left;
            insetsHeight = insets.top + insets.bottom;
            displaySize = new Size(width - insetsWidth, height - insetsHeight);
        } else {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            displaySize =  new Size(metrics.widthPixels, metrics.heightPixels);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Display.Mode[] modes = wm.getDefaultDisplay().getSupportedModes();
            if (modes.length > 0){
                Display.Mode mode = modes[modes.length - 1];
                int width = mode.getPhysicalWidth();
                int height = mode.getPhysicalHeight();
                // 4K サイズの解像度がある場合はそちらを優先する
                displaySize  = new Size(width - insetsWidth, height - insetsHeight);
            }
        }

        return displaySize;
    }

    @Override
    public EGLSurfaceDrawingThread getSurfaceDrawingThread() {
        return mScreenCastSurfaceDrawingThread;
    }

    @Override
    public void clean() {
        super.clean();
        mScreenCastBroadcasterProvider.stop();
        mScreenCastPreviewServerProvider.stop();
        mScreenCastMgr.clean();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getMimeType() {
        // デフォルトのマイムタイプを返却
        return MIME_TYPE_JPEG;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        List<String> mimeTypes = mScreenCastPreviewServerProvider.getSupportedMimeType();
        mimeTypes.addAll(mScreenCastBroadcasterProvider.getSupportedMimeType());
        mimeTypes.add(0, MIME_TYPE_JPEG);
        return mimeTypes;
    }

    @Override
    public Settings getSettings() {
        return mSettings;
    }

    @Override
    public PreviewServerProvider getServerProvider() {
        return mScreenCastPreviewServerProvider;
    }

    @Override
    public BroadcasterProvider getBroadcasterProvider() {
        return mScreenCastBroadcasterProvider;
    }

    @Override
    public void requestPermission(final PermissionCallback callback) {
        if (mSettings.getPreviewAudioSource() == AudioSource.DEFAULT ||
                mSettings.getPreviewAudioSource() == AudioSource.MIC) {
            // マイクを使用する場合にはパーミッションを確認
            requestPermission(new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, new PermissionCallback() {
                @Override
                public void onAllowed() {
                    requestMediaProjection(callback);
                }
                @Override
                public void onDisallowed() {
                    callback.onDisallowed();
                }
            });
        } else {
            requestMediaProjection(callback);
        }
    }

    // Implements HostDevicePhotoRecorder method.

    @Override
    public void takePhoto(final OnPhotoEventListener listener) {
        postRequestHandler(() -> takePhotoInternal(listener));
    }

    @Override
    public void turnOnFlashLight(final @NonNull TurnOnFlashLightListener listener,
                                 final @NonNull Handler handler) {
        handler.post(() -> listener.onError(Error.UNSUPPORTED));
    }

    @Override
    public void turnOffFlashLight(final @NonNull TurnOffFlashLightListener listener,
                                  final @NonNull Handler handler) {
        handler.post(() -> listener.onError(Error.UNSUPPORTED));
    }

    @Override
    public boolean isFlashLightState() {
        return false;
    }

    @Override
    public boolean isUseFlashLight() {
        return false;
    }

    // Implements AbstractMediaRecorder method.

    @Override
    protected MP4Recorder createMP4Recorder() {
        File file = new File(getFileManager().getBasePath(), generateVideoFileName());
        return new SurfaceMP4Recorder(file, mSettings, mScreenCastSurfaceDrawingThread);
    }

    // private method.

    public ScreenCastManager getScreenCastMgr() {
        return mScreenCastMgr;
    }

    private String generateVideoFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + ".mp4";
    }

    private String generateImageFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    @SuppressLint("WrongConstant")
    private void takePhotoInternal(final @NonNull OnPhotoEventListener listener) {
        try {
            setState(State.RECORDING);

            Size size = mSettings.getPictureSize();
            int w = size.getWidth();
            int h = size.getHeight();
            ImageReader imageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 4);
            final ImageScreenCast screenCast = mScreenCastMgr.createScreenCast(imageReader, w, h);
            final AtomicReference<Bitmap> screenshot = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);
            imageReader.setOnImageAvailableListener((reader) -> {
                screenshot.set(screenCast.getScreenshot());
                latch.countDown();
            }, mImageReaderHandler);
            screenCast.startCast();
            if (!latch.await(5, TimeUnit.SECONDS)) {
                setState(State.INACTIVE);
                listener.onFailedTakePhoto("Failed to take screenshot.");
                return;
            }
            screenCast.stopCast();

            Bitmap bitmap = screenshot.get();
            if (bitmap == null) {
                setState(State.INACTIVE);
                listener.onFailedTakePhoto("Failed to take screenshot.");
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            storePhoto(generateImageFileName(), baos.toByteArray(), listener);

            setState(State.INACTIVE);
        } catch (OutOfMemoryError e) {
            setState(State.ERROR);
            listener.onFailedTakePhoto("Out of memory.");
        } catch (Exception e) {
            setState(State.ERROR);
            listener.onFailedTakePhoto("Taking screenshot is shutdown.");
        }
    }

    private class ScreenCastSettings extends Settings {
        private List<Size> mSupportedPictureSize = new ArrayList<>();
        private List<Size> mSupportedPreviewSize = new ArrayList<>();
        private List<Range<Integer>> mSupportedFps = new ArrayList<>();
        private List<Size> mSupportEncoderSizeList = new ArrayList<>();

        ScreenCastSettings(Context context, HostMediaRecorder recorder) {
            super(context, recorder);
        }

        @Override
        protected EncoderSettings createEncoderSettings(String encoderId) {
            return new EncoderSettings(getContext(), encoderId) {
                @Override
                public List<Size> getSupportedEncoderSizes() {
                    return mSupportEncoderSizeList;
                }
            };
        }

        @Override
        public List<Size> getSupportedPictureSizes() {
            return mSupportedPictureSize;
        }

        @Override
        public List<Size> getSupportedPreviewSizes() {
            return mSupportedPreviewSize;
        }

        @Override
        public List<Range<Integer>> getSupportedFps() {
            return mSupportedFps;
        }
    }
}
