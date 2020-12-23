/*
 HostDeviceScreenCast.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MP4Recorder;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Host Device Screen Cast.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenCastRecorder extends AbstractMediaRecorder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "host.dplugin";

    private static final String ID = "screen";
    private static final String NAME = "AndroidHost Screen";

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_screen_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".jpg";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private final Context mContext;

    private final ScreenCastManager mScreenCastMgr;
    private final ExecutorService mPhotoThread = Executors.newFixedThreadPool(1);
    private final Handler mImageReaderHandler = new Handler(Looper.getMainLooper());
    private final Settings mSettings;
    private MP4Recorder mMP4Recorder;

    private ScreenCastPreviewServerProvider mScreenCastPreviewServerProvider;
    private ScreenCastBroadcasterProvider mScreenCastBroadcasterProvider;
    private ScreenCastSurfaceDrawingThread mScreenCastSurfaceDrawingThread;

    private State mState = State.INACTIVE;

    public ScreenCastRecorder(final Context context, final FileManager fileMgr) {
        super(context, 2, fileMgr);
        mContext = context;
        mSettings = new Settings(context, this);

        initSupportedSettings();

        mScreenCastMgr = new ScreenCastManager(context);
        mScreenCastPreviewServerProvider = new ScreenCastPreviewServerProvider(context, this);
        mScreenCastBroadcasterProvider = new ScreenCastBroadcasterProvider(this);
        mScreenCastSurfaceDrawingThread = new ScreenCastSurfaceDrawingThread(this);
    }

    /**
     * レコーダの設定を初期化します.
     */
    private void initSupportedSettings() {
        if (DEBUG) {
            Log.d(TAG, "ScreenCastSupportedPreviewSize");
        }
        // MediaCodec でエンコードできる最大解像度を取得
        // TODO h264, h265 で最大解像度が違う場合はどうするべきか？
        // TODO ハードウェアエンコーダとソフトウェアエンコーダで最大解像度が違うのはどうするべきか？
        Size maxSize = CapabilityUtil.getSupportedMaxSize("video/avc");

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
            if (size.getWidth() <= maxSize.getWidth() && size.getHeight() <= maxSize.getHeight()) {
                supportPreviewSizes.add(size);
            }
        }
        mSettings.setSupportedPreviewSizes(supportPreviewSizes);
        mSettings.setSupportedPictureSizes(supportPictureSizes);

        List<Range<Integer>> supportFps = new ArrayList<>();
        supportFps.add(new Range<>(30, 30));
        mSettings.setSupportedFps(supportFps);

        if (!mSettings.load()) {
            mSettings.setPreviewSize(mSettings.getSupportedPreviewSizes().get(0));
            mSettings.setPictureSize(mSettings.getSupportedPictureSizes().get(0));
            mSettings.setPreviewBitRate(2 * 1024 * 1024);
            mSettings.setPreviewMaxFrameRate(30);
            mSettings.setPreviewKeyFrameInterval(1);
            mSettings.setPreviewQuality(80);

            mSettings.setAudioEnabled(false);
            mSettings.setPreviewAudioBitRate(64 * 1024);
            mSettings.setPreviewSampleRate(8000);
            mSettings.setPreviewChannel(1);
            mSettings.setUseAEC(true);

            mSettings.setMjpegPort(21000);
            mSettings.setMjpegSSLPort(21100);
            mSettings.setRtspPort(22000);
            mSettings.setSrtPort(23000);

            mSettings.save();
        }
    }

    /**
     * 画面のサイズを取得します.
     *
     * @return 画面サイズ
     */
    private Size getDisplaySize() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        boolean isSwap;
        switch (wm.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                isSwap = false;
                break;
            default:
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                isSwap = true;
                break;
        }
        // 画面が回転している場合には、縦横をスワップしておく。
        int width = isSwap ? metrics.heightPixels : metrics.widthPixels;
        int height = isSwap ? metrics.widthPixels : metrics.heightPixels;
        return new Size(width, height);
    }

    @Override
    public EGLSurfaceDrawingThread getSurfaceDrawingThread() {
        return mScreenCastSurfaceDrawingThread;
    }

    @Override
    public void clean() {
        stopRecordingInternal(null);
        mScreenCastBroadcasterProvider.stopBroadcaster();
        mScreenCastPreviewServerProvider.stopServers();
        mScreenCastMgr.clean();
    }

    @Override
    public void destroy() {
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
        mimeTypes.add(0, MIME_TYPE_JPEG);
        return mimeTypes;
    }

    @Override
    public State getState() {
        // TODO ステート管理
        return mState;
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
    public void onDisplayRotation(final int rotation) {
        mScreenCastBroadcasterProvider.onConfigChange();
        mScreenCastPreviewServerProvider.onConfigChange();
    }

    @Override
    public void onConfigChange() {
        mScreenCastBroadcasterProvider.onConfigChange();
        mScreenCastPreviewServerProvider.onConfigChange();
    }

    @Override
    public void requestPermission(final PermissionCallback callback) {
        mScreenCastMgr.requestPermission(new ScreenCastManager.PermissionCallback() {
            @Override
            public void onAllowed() {
                callback.onAllowed();
            }

            @Override
            public void onDisallowed() {
                callback.onDisallowed();
            }
        });
    }

    // HostDevicePhotoRecorder

    @Override
    public void takePhoto(final OnPhotoEventListener listener) {
        mScreenCastMgr.requestPermission(new ScreenCastManager.PermissionCallback() {
            @Override
            public void onAllowed() {
                mPhotoThread.execute(() -> takePhotoInternal(listener));
            }

            @Override
            public void onDisallowed() {
                listener.onFailedTakePhoto("Media projection is not allowed by user.");
            }
        });
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

    // HostDeviceStreamRecorder

    @Override
    public boolean canPauseRecording() {
        return false;
    }

    @Override
    public void startRecording(RecordingCallback callback) {
        startRecordingInternal(callback);
    }

    @Override
    public void stopRecording(StoppingCallback callback) {
        stopRecordingInternal(callback);
    }

    @Override
    public void pauseRecording() {
    }

    @Override
    public void resumeRecording() {
    }

    @Override
    public String getStreamMimeType() {
        return "video/mp4";
    }

    @Override
    public void muteTrack() {
    }

    @Override
    public void unMuteTrack() {
    }

    @Override
    public boolean isMutedTrack() {
        return false;
    }

    // private method.

    public ScreenCastManager getScreenCastMgr() {
        return mScreenCastMgr;
    }

    private void takePhotoInternal(final @NonNull OnPhotoEventListener listener) {
        try {
            mState = State.RECORDING;

            Size size = mSettings.getPictureSize();
            int w = size.getWidth();
            int h = size.getHeight();
            ImageReader imageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 4);
            final ImageScreenCast screenCast = mScreenCastMgr.createScreenCast(imageReader, size.getWidth(), size.getHeight());
            final AtomicReference<Bitmap> screenshot = new AtomicReference<>();
            final CountDownLatch mLatch = new CountDownLatch(1);
            imageReader.setOnImageAvailableListener((reader) -> {
                screenshot.set(screenCast.getScreenshot());
                mLatch.countDown();
            }, mImageReaderHandler);
            screenCast.startCast();
            mLatch.await(5, TimeUnit.SECONDS);
            screenCast.stopCast();

            Bitmap bitmap = screenshot.get();
            if (bitmap == null) {
                mState = State.INACTIVE;
                listener.onFailedTakePhoto("Failed to take screenshot.");
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            storePhoto(createNewFileName(), baos.toByteArray(), listener);

            mState = State.INACTIVE;
        } catch (OutOfMemoryError e) {
            mState = State.ERROR;
            listener.onFailedTakePhoto("Out of memory.");
        } catch (Exception e) {
            mState = State.ERROR;
            listener.onFailedTakePhoto("Taking screenshot is shutdown.");
        }
    }

    private String createNewFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    /**
     * 録画を行います.
     *
     * @param callback 録画開始結果を通知するリスナー
     */
    private void startRecordingInternal(final RecordingCallback callback) {
        if (mMP4Recorder != null) {
            if (callback != null) {
                callback.onFailed(this, "Recording has started already.");
            }
            return;
        }

        File filePath = new File(getFileManager().getBasePath(), generateVideoFileName());
        mMP4Recorder = new SurfaceMP4Recorder(filePath, mSettings, mScreenCastSurfaceDrawingThread);
        mMP4Recorder.start(new MP4Recorder.OnStartingCallback() {
            @Override
            public void onSuccess() {
                mState = State.RECORDING;

                if (callback != null) {
                    callback.onRecorded(ScreenCastRecorder.this, mMP4Recorder.getOutputFile().getAbsolutePath());
                }
            }

            @Override
            public void onFailure(Throwable e) {
                if (mMP4Recorder != null) {
                    mMP4Recorder.release();
                    mMP4Recorder = null;
                }

                if (callback != null) {
                    callback.onFailed(ScreenCastRecorder.this,
                            "Failed to start recording because of camera problem: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 録画停止を行います.
     *
     * @param callback 録画停止結果を通知するリスナー
     */
    private void stopRecordingInternal(final StoppingCallback callback) {
        if (mMP4Recorder == null) {
            if (callback != null) {
                callback.onFailed(this, "Recording has stopped already.");
            }
            return;
        }

        mState = State.INACTIVE;

        mMP4Recorder.stop(new MP4Recorder.OnStoppingCallback() {
            @Override
            public void onSuccess() {
                File videoFile = mMP4Recorder.getOutputFile();
                registerVideo(videoFile);
                mMP4Recorder.release();
                mMP4Recorder = null;

                if (callback != null) {
                    callback.onStopped(ScreenCastRecorder.this, videoFile.getAbsolutePath());
                }
            }

            @Override
            public void onFailure(Throwable e) {
                if (mMP4Recorder != null) {
                    mMP4Recorder.release();
                    mMP4Recorder = null;
                }

                if (callback != null) {
                    callback.onFailed(ScreenCastRecorder.this,
                            "Failed to stop recording for unexpected error: " + e.getMessage());
                }
            }
        });
    }

    private String generateVideoFileName() {
        return "android_screen_" + mSimpleDateFormat.format(new Date()) + ".mp4";
    }
}
