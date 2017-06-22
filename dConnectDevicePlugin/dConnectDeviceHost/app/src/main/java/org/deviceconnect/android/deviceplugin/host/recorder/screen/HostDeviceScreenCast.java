/*
 HostDeviceScreenCast.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static android.R.attr.max;

/**
 * Host Device Screen Cast.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(21)
public class HostDeviceScreenCast extends HostDevicePreviewServer implements HostDevicePhotoRecorder {

    static final String RESULT_DATA = "result_data";

    static final String EXTRA_CALLBACK = "callback";

    private static final String ID = "screen";

    private static final String NAME = "AndroidHost Screen";

    private static final String MIME_TYPE = "video/x-mjpeg";

    private static final double DEFAULT_MAX_FPS = 10.0d;

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_screen_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".jpg";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    /**
     * マイムタイプ一覧を定義.
     */
    private List<String> mMimeTypes = new ArrayList<String>() {
        {
            add(MIME_TYPE);
        }
    };

    private final Context mContext;

    private final int mDisplayDensityDpi;

    private final Object mLockObj = new Object();

    private final Logger mLogger = Logger.getLogger("host.dplugin");

    private MixedReplaceMediaServer mServer;

    private MediaProjectionManager mManager;

    private MediaProjection mMediaProjection;

    private VirtualDisplay mVirtualDisplay;

    private ImageReader mImageReader;

    private FileManager mFileMgr;

    private boolean mIsCasting;

    private Thread mThread;

    private final List<PictureSize> mSupportedPreviewSizes = new ArrayList<>();
    private final List<PictureSize> mSupportedPictureSizes = new ArrayList<>();

    private PictureSize mPreviewSize;
    private PictureSize mPictureSize;

    private long mFrameInterval;

    private double mMaxFps;

    private RecorderState mState;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BroadcastReceiver mConfigChangeReceiver;

    public HostDeviceScreenCast(final Context context, final FileManager fileMgr) {
        super(context, 2000);
        mContext = context;
        mFileMgr = fileMgr;
        mManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        PictureSize size = new PictureSize(metrics.widthPixels, metrics.heightPixels);
        mDisplayDensityDpi = metrics.densityDpi;
        initSupportedPreviewSizes(size);

        mState = RecorderState.INACTTIVE;
        mMaxFps = DEFAULT_MAX_FPS;
        setMaxFrameRate(mMaxFps);
    }

    private void initSupportedPreviewSizes(final PictureSize originalSize) {
        final int num = 4;
        final int w = originalSize.getWidth();
        final int h = originalSize.getHeight();
        mSupportedPreviewSizes.clear();
        for (int i = 1; i <= num; i++) {
            float scale = i / ((float) num);
            PictureSize previewSize = new PictureSize((int) (w * scale), (int) (h * scale));
            mSupportedPreviewSizes.add(previewSize);
            mSupportedPictureSizes.add(previewSize);
        }
        mPreviewSize = mSupportedPreviewSizes.get(0);
        mPictureSize = mSupportedPictureSizes.get(num - 1);
    }

    @Override
    public void initialize() {
        // Nothing to do.
    }

    @Override
    public void clean() {
        stopWebServer();
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
        return MIME_TYPE;
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
        if (mIsCasting) {
            restartScreenCast();
        }
    }

    @Override
    public double getMaxFrameRate() {
        return mMaxFps;
    }

    @Override
    public void setMaxFrameRate(double frameRate) {
        mMaxFps = frameRate;
        mFrameInterval =  1000L / max;
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
        return mMimeTypes;
    }

    @Override
    public boolean isSupportedPictureSize(int width, int height) {
        if (mSupportedPictureSizes != null) {
            for (PictureSize size : mSupportedPictureSizes) {
                if (width == size.getWidth() && height == size.getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedPreviewSize(int width, int height) {
        if (mSupportedPreviewSizes != null) {
            for (PictureSize size : mSupportedPreviewSizes) {
                if (width == size.getWidth() && height == size.getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isBack() {
        return false;
    }

    @Override
    public void turnOnFlashLight() {

    }

    @Override
    public void turnOffFlashLight() {

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
    public void startWebServer(final OnWebServerStartCallback callback) {
        mLogger.info("Starting web server...");
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin ScreenCast Server");
                mServer.setContentType(MIME_TYPE);
                final String ip = mServer.start();
                requestPermission(new PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        sendNotification();
                        startScreenCast();
                        registerConfigChangeReceiver();
                        callback.onStart(ip);
                    }

                    @Override
                    public void onDisallowed() {
                        stopWebServer();
                        callback.onFail();
                    }
                });
            } else {
                callback.onStart(mServer.getUrl());
            }
        }
        mLogger.info("Started web server.");
    }

    @Override
    public void stopWebServer() {
        mLogger.info("Stopping web server...");

        synchronized (mLockObj) {
            hideNotification();
            stopScreenCast();
            if (mMediaProjection != null) {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
            unregisterConfigChangeReceiver();
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
        }
        mLogger.info("Stopped web server.");
    }

    @Override
    public void takePhoto(final OnPhotoEventListener listener) {
        mState = RecorderState.RECORDING;

        if (!mIsCasting) {
            requestPermission(new PermissionCallback() {
                @Override
                public void onAllowed() {
                    takePhoto(listener, null);
                }

                @Override
                public void onDisallowed() {
                    mState = RecorderState.INACTTIVE;
                    listener.onFailedTakePhoto();
                }
            });
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    takePhotoInternal(new OnPhotoEventListener() {
                        @Override
                        public void onTakePhoto(final String uri, final String filePath) {
                            listener.onTakePhoto(uri, filePath);
                        }

                        @Override
                        public void onFailedTakePhoto() {
                            mState = RecorderState.INACTTIVE;
                            listener.onFailedTakePhoto();
                        }
                    });
                }
            }).start();
        }
    }

    private void takePhoto(final OnPhotoEventListener listener, final FinishCallback callback) {
        setupVirtualDisplay();
        new Thread(new Runnable() {
            @Override
            public void run() {
                takePhotoInternal(new OnPhotoEventListener() {
                    @Override
                    public void onTakePhoto(final String uri, final String filePath) {
                        listener.onTakePhoto(uri, filePath);
                        releaseVirtualDisplay();
                        if (mMediaProjection != null) {
                            mMediaProjection.stop();
                            mMediaProjection = null;
                        }

                    }

                    @Override
                    public void onFailedTakePhoto() {
                        listener.onFailedTakePhoto();
                        releaseVirtualDisplay();
                        if (mMediaProjection != null) {
                            mMediaProjection.stop();
                            mMediaProjection = null;
                        }
                    }
                });
            }
         }).start();

    }

    private void takePhotoInternal(final OnPhotoEventListener listener) {
        long t = System.currentTimeMillis();
        Bitmap bitmap = getScreenshot();
        while (bitmap == null && (System.currentTimeMillis() - t) < 5000) {
            bitmap = getScreenshot();
            if (bitmap == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bitmap == null) {
            mState = RecorderState.INACTTIVE;
            listener.onFailedTakePhoto();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] media = baos.toByteArray();
        if (media == null) {
            mState = RecorderState.INACTTIVE;
            listener.onFailedTakePhoto();
            return;
        }
        // 常に新しいファイル名になるため重複はない。そのため、Overwriteフラグをtrueにする。
        mFileMgr.saveFile(createNewFileName(), media, true, new FileManager.SaveFileCallback() {
            @Override
            public void onSuccess(@NonNull final String uri) {
                mState = RecorderState.INACTTIVE;
                listener.onTakePhoto(uri, null);
            }

            @Override
            public void onFail(@NonNull final Throwable throwable) {
                mState = RecorderState.INACTTIVE;
                listener.onFailedTakePhoto();
            }
        });
    }

    private String createNewFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    private void requestPermission(final PermissionCallback callback) {
        if (mMediaProjection != null) {
            callback.onAllowed();
        } else {
            Intent intent = new Intent();
            intent.setClass(mContext, PermissionReceiverActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_CALLBACK, new ResultReceiver(new Handler(Looper.getMainLooper())) {
                @Override
                protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                    if (resultCode == Activity.RESULT_OK) {
                        Intent data = resultData.getParcelable(RESULT_DATA);
                        if (data != null) {
                            mMediaProjection = mManager.getMediaProjection(resultCode, data);
                            mMediaProjection.registerCallback(new MediaProjection.Callback() {
                                @Override
                                public void onStop() {
                                    clean();
                                }
                            }, new Handler(Looper.getMainLooper()));
                        }
                    }

                    if (mMediaProjection != null) {
                        callback.onAllowed();
                    } else {
                        callback.onDisallowed();
                    }
                }
            });
            mContext.startActivity(intent);
        }
    }

    private void setupVirtualDisplay() {
        setupVirtualDisplay(mPreviewSize, new VirtualDisplay.Callback() {
            @Override
            public void onPaused() {
            }

            @Override
            public void onResumed() {
            }

            @Override
            public void onStopped() {
            }
        });
    }

    private void setupVirtualDisplay(final PictureSize size, final VirtualDisplay.Callback callback) {
        int w = size.getWidth();
        int h = size.getHeight();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        if (dm.widthPixels > dm.heightPixels) {
            if (w < h) {
                w = size.getHeight();
                h = size.getWidth();
            }
        } else {
            if (w > h) {
                w = size.getHeight();
                h = size.getWidth();
            }
        }

        mImageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 4);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "Android Host Screen",
                w,
                h,
                mDisplayDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), callback, null);
    }

    private void releaseVirtualDisplay() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mImageReader != null) {
            mImageReader.setOnImageAvailableListener(null, null);
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void startScreenCast() {
        if (mIsCasting) {
            mLogger.info("MediaProjection is already running.");
            return;
        }
        mIsCasting = true;

        setupVirtualDisplay();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mServer != null) {
                    mLogger.info("Server URL: " + mServer.getUrl());
                }
                try {
                    while (mIsCasting) {
                        long start = System.currentTimeMillis();

                        Bitmap bitmap = getScreenshot();
                        if (bitmap == null) {
                            continue;
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] media = baos.toByteArray();
                        mServer.offerMedia(media);

                        long end = System.currentTimeMillis();
                        long interval = mFrameInterval - (end - start);
                        if (interval > 0) {
                            Thread.sleep(interval);
                        }
                    }
                } catch (Throwable e) {
                    mLogger.warning("MediaProjection is broken." + e.getMessage());
                    stopWebServer();
                }
            }
        });
        mThread.start();
    }

    private void stopScreenCast() {
        if (!mIsCasting) {
            mLogger.info("MediaProjection is already stopping.");
            return;
        }
        mIsCasting = false;

        if (mThread != null) {
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread = null;
        }
        releaseVirtualDisplay();
    }

    private void restartScreenCast() {
        stopScreenCast();
        startScreenCast();
    }

    private synchronized Bitmap getScreenshot() {
        try {
            if (mImageReader == null) {
                return null;
            }
            Image image = mImageReader.acquireLatestImage();
            if (image == null) {
                return null;
            }
            return decodeToBitmap(image);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap decodeToBitmap(final Image img) {
        Image.Plane[] planes = img.getPlanes();
        if (planes[0].getBuffer() == null) {
            return null;
        }

        int width = img.getWidth();
        int height = img.getHeight();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride, height,
                Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
        img.close();

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, null, true);
    }

    private void registerConfigChangeReceiver() {
        mConfigChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                restartScreenCast();
            }
        };
        IntentFilter filter = new IntentFilter(
                "android.intent.action.CONFIGURATION_CHANGED");
        mContext.registerReceiver(mConfigChangeReceiver, filter);
    }

    private void unregisterConfigChangeReceiver() {
        if (mConfigChangeReceiver != null) {
            mContext.unregisterReceiver(mConfigChangeReceiver);
            mConfigChangeReceiver = null;
        }
    }

    private interface PermissionCallback {
        void onAllowed();
        void onDisallowed();
    }

    private interface FinishCallback {
        void onFinish();
    }
}
