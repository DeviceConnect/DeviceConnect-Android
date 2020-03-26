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
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaSharing;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
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

import androidx.annotation.NonNull;

/**
 * Host Device Screen Cast.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenCastRecorder implements HostMediaRecorder, HostDevicePhotoRecorder, HostDeviceStreamRecorder {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "host.dplugin";

    private static final String ID = "screen";

    private static final String NAME = "AndroidHost Screen";

    private static final double DEFAULT_MAX_FPS = 30.0d;

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_screen_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".jpg";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private final List<PictureSize> mSupportedPreviewSizes = new ArrayList<>();
    private final List<PictureSize> mSupportedPictureSizes = new ArrayList<>();
    private final ScreenCastManager mScreenCastMgr;
    private final ExecutorService mPhotoThread = Executors.newFixedThreadPool(4);
    private final Handler mImageReaderHandler = new Handler(Looper.getMainLooper());

    private FileManager mFileMgr;
    private PictureSize mPreviewSize;
    private PictureSize mPictureSize;
    private int mPreviewBitRate = 1024 * 1024;
    private double mMaxFps = DEFAULT_MAX_FPS;
    private int mIFrameInterval = 2;
    private RecorderState mState = RecorderState.INACTIVE;

    private final MediaSharing mMediaSharing = MediaSharing.getInstance();

    private ScreenCastPreviewServerProvider mScreenCastPreviewServerProvider;
    private Context mContext;

    public ScreenCastRecorder(final Context context,
                              final FileManager fileMgr) {
        mContext = context;
        mFileMgr = fileMgr;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        PictureSize size = new PictureSize(metrics.widthPixels, metrics.heightPixels);
        initSupportedPreviewSizes(size);
        setMaxFrameRate(mMaxFps);

        mScreenCastMgr = new ScreenCastManager(context);
        mScreenCastPreviewServerProvider = new ScreenCastPreviewServerProvider(context, this);
    }

    private void initSupportedPreviewSizes(final PictureSize originalSize) {
        if (DEBUG) {
            Log.d(TAG, "ScreenCastSupportedPreviewSize");
            Log.d(TAG, "  size: " + originalSize);
        }

        final int num = 4;
        final int w = originalSize.getWidth();
        final int h = originalSize.getHeight();
        mSupportedPreviewSizes.clear();
        for (int i = 1; i <= num; i++) {
            float scale = i / ((float) num);
            // MediaCodec に解像度を渡す時に端数を持っているとエラーになってしまう
            // 場合があったので、キリの良い値になるように調整しています。
            int width = (int) (w * scale);
            int height = (int) (h * scale);
            width += 10 - (width % 10);
            height += 10 - (height % 10);
            PictureSize previewSize = new PictureSize(width, height);
            mSupportedPreviewSizes.add(previewSize);
            mSupportedPictureSizes.add(previewSize);
        }
        mPreviewSize = mSupportedPreviewSizes.get(0);
        mPictureSize = mSupportedPictureSizes.get(num - 1);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void initialize() {
        // Nothing to do.
    }

    @Override
    public void clean() {
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
    public RecorderState getState() {
        return mState;
    }

    @Override
    public PictureSize getPictureSize() {
        return mPictureSize;
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        mPictureSize = size;
    }

    @Override
    public PictureSize getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public synchronized void setPreviewSize(final PictureSize size) {
        mPreviewSize = size;
    }

    @Override
    public double getMaxFrameRate() {
        return mMaxFps;
    }

    @Override
    public void setMaxFrameRate(double frameRate) {
        mMaxFps = frameRate;
    }

    @Override
    public int getPreviewBitRate() {
        return mPreviewBitRate;
    }

    @Override
    public void setPreviewBitRate(int bitRate) {
        mPreviewBitRate = bitRate;
    }

    @Override
    public int getIFrameInterval() {
        return mIFrameInterval;
    }

    @Override
    public void setIFrameInterval(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval is invalid. interval=" + interval);
        }
        mIFrameInterval = interval;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        return mSupportedPreviewSizes;
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        return mSupportedPictureSizes;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        List<String> mimeTypes = mScreenCastPreviewServerProvider.getSupportedMimeType();
        mimeTypes.add(0, MIME_TYPE_JPEG);
        return mimeTypes;
    }

    @Override
    public boolean isSupportedPictureSize(int width, int height) {
        for (PictureSize size : mSupportedPictureSizes) {
            if (width == size.getWidth() && height == size.getHeight()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedPreviewSize(int width, int height) {
        for (PictureSize size : mSupportedPreviewSizes) {
            if (width == size.getWidth() && height == size.getHeight()) {
                return true;
            }
        }
        return false;
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
    public boolean canPauseRecording() {
        return false;
    }

    @Override
    public void startRecording(RecordingListener listener) {
    }

    @Override
    public void stopRecording(StoppingListener listener) {
    }

    @Override
    public void pauseRecording() {
    }

    @Override
    public void resumeRecording() {
    }

    @Override
    public String getStreamMimeType() {
        return null;
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

    @Override
    public PreviewServerProvider getServerProvider() {
        return mScreenCastPreviewServerProvider;
    }

    @Override
    public List<PreviewServer> startPreviews() {
        return mScreenCastPreviewServerProvider.startServers();
    }

    @Override
    public void stopPreviews() {
        mScreenCastPreviewServerProvider.stopServers();
    }

    @Override
    public boolean isAudioEnabled() {
        return RecorderSetting.getInstance(mContext).isAudioEnabled();
    }

    @Override
    public int getPreviewAudioBitRate() {
        return RecorderSetting.getInstance(mContext).getPreviewAudioBitRate();
    }

    @Override
    public int getPreviewSampleRate() {
        return RecorderSetting.getInstance(mContext).getPreviewSampleRate();
    }

    @Override
    public int getPreviewChannel() {
        return RecorderSetting.getInstance(mContext).getPreviewChannel();
    }

    @Override
    public boolean isUseAEC() {
        return RecorderSetting.getInstance(mContext).isUseAEC();
    }

    @Override
    public void onDisplayRotation(final int rotation) {
        mScreenCastPreviewServerProvider.onConfigChange();
    }

    public ScreenCastManager getScreenCastMgr() {
        return mScreenCastMgr;
    }

    private void takePhotoInternal(final @NonNull OnPhotoEventListener listener) {
        try {
            mState = RecorderState.RECORDING;

            HostMediaRecorder.PictureSize size = getPreviewSize();
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
                mState = RecorderState.INACTIVE;
                listener.onFailedTakePhoto("Failed to take screenshot.");
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] media = baos.toByteArray();
            String filename = createNewFileName();
            // 常に新しいファイル名になるため重複はない。そのため、Overwrite フラグを true にする。
            mFileMgr.saveFile(filename, media, true, new FileManager.SaveFileCallback() {
                @Override
                public void onSuccess(@NonNull final String uri) {
                    mState = RecorderState.INACTIVE;
                    registerPhoto(new File(mFileMgr.getBasePath(), filename));
                    listener.onTakePhoto(uri, null, MIME_TYPE_JPEG);
                }

                @Override
                public void onFail(@NonNull final Throwable throwable) {
                    mState = RecorderState.INACTIVE;
                    listener.onFailedTakePhoto(throwable.getMessage());
                }
            });
        } catch (OutOfMemoryError e) {
            listener.onFailedTakePhoto("Out of memory.");
        } catch (Exception e) {
            listener.onFailedTakePhoto("Taking screenshot is shutdown.");
        }
    }

    private String createNewFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    private void registerPhoto(final File photoFile) {
        Uri uri = mMediaSharing.sharePhoto(getContext(), photoFile);
        if (DEBUG) {
            if (uri != null) {
                Log.d(TAG, "Registered screen: uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register screen: file=" + photoFile.getAbsolutePath());
            }
        }
    }
}
