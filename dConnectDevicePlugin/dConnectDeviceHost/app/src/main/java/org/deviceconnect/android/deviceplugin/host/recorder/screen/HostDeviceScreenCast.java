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
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;

import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static android.R.attr.max;

/**
 * Host Device Screen Cast.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(21)
public class HostDeviceScreenCast extends HostDevicePreviewServer {

    public static final String ACTION_PERMISSION =
        HostDeviceScreenCast.class.getPackage().getName() + ".permission";

    static final String RESULT_CODE = "result_code";

    static final String RESULT_DATA = "result_data";

    private static final String ID = "screen";

    private static final String NAME = "AndroidHost Screen";

    private static final String MIME_TYPE = "video/x-mjpeg";

    private static final double DEFAULT_MAX_FPS = 10.0d;

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

    private boolean mIsCasting;

    private Thread mThread;

    private final List<PictureSize> mSupportedPreviewSizes = new ArrayList<>();

    private PictureSize mPreviewSize;

    private BroadcastReceiver mPermissionReceiver;

    private long mFrameInterval;

    private double mMaxFps;

    public HostDeviceScreenCast(final Context context) {
        super(context, 2000);
        mContext = context;
        mManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        PictureSize size = new PictureSize(metrics.widthPixels, metrics.heightPixels);
        mDisplayDensityDpi = metrics.densityDpi;
        initSupportedPreviewSizes(size);
        mPreviewSize = mSupportedPreviewSizes.get(0);

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
        }
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
        return mIsCasting ? RecorderState.RECORDING : RecorderState.INACTTIVE;
    }


    @Override
    public PictureSize getPictureSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        throw new UnsupportedOperationException();
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
        mFrameInterval = (long) (1 / max) * 1000L;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        return mSupportedPreviewSizes;
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public boolean isSupportedPictureSize(int width, int height) {
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
    public void startWebServer(final OnWebServerStartCallback callback) {
        mLogger.info("Starting web server...");
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin ScreenCast Server");
                mServer.setContentType("image/jpg");
                final String ip = mServer.start();

                if (mPermissionReceiver != null) {
                    mContext.unregisterReceiver(mPermissionReceiver);
                }
                mPermissionReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(final Context context, final Intent intent) {
                        if (ACTION_PERMISSION.equals(intent.getAction())) {
                            int resultCode = intent.getIntExtra(RESULT_CODE, -1);
                            if (resultCode == Activity.RESULT_OK) {
                                Intent data = intent.getParcelableExtra(RESULT_DATA);
                                setupMediaProjection(resultCode, data);
                                startScreenCast();
                                callback.onStart(ip);
                            } else {
                                callback.onFail();
                            }
                        }
                    }
                };
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PERMISSION);
                mContext.registerReceiver(mPermissionReceiver, filter);
                requestPermission();
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
            if (mPermissionReceiver != null) {
                mContext.unregisterReceiver(mPermissionReceiver);
                mPermissionReceiver = null;
            }
            stopScreenCast();
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
        }
        mLogger.info("Stopped web server.");
    }

    private void requestPermission() {
        Intent intent = new Intent();
        intent.setClass(mContext, PermissionReceiverActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void setupMediaProjection(int resultCode, Intent data) {
        mMediaProjection = mManager.getMediaProjection(resultCode, data);
    }

    private void setupVirtualDisplay() {
        int w = mPreviewSize.getWidth();
        int h = mPreviewSize.getHeight();
        mImageReader = ImageReader.newInstance(w, h, ImageFormat.RGB_565, 10);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
            "Android Host Screen",
            w,
            h,
            mDisplayDensityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader.getSurface(),
            new VirtualDisplay.Callback() {
                @Override
                public void onPaused() {
                    mLogger.info("VirtualDisplay.Callback.onPaused");
                    stopScreenCast();
                }

                @Override
                public void onResumed() {
                    mLogger.info("VirtualDisplay.Callback.onResumed");
                    startScreenCast();
                }

                @Override
                public void onStopped() {
                    mLogger.info("VirtualDisplay.Callback.onStopped");
                }
            }, null);
    }

    private void startScreenCast() {
        if (mIsCasting) {
            return;
        }
        mIsCasting = true;
        setupVirtualDisplay();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mLogger.info("Server URL: " + mServer.getUrl());
                sendNotification();
                while (mIsCasting) {
                    try {
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
                    } catch (Throwable e) {
                        break;
                    }
                }
                hideNotification();
            }
        });
        mThread.start();
    }

    private void stopScreenCast() {
        if (!mIsCasting) {
            return;
        }
        mIsCasting = false;
        if (mThread != null) {
            try {
                mThread.interrupt();
                mThread.join();
            } catch (InterruptedException e) {
                // NOP
            }
            mThread = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
    }

    private void restartScreenCast() {
        stopScreenCast();
        startScreenCast();
    }

    private Bitmap getScreenshot() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            return null;
        }
        return decodeToBitmap(image);
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
                Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
        img.close();

        return bitmap;
    }
}
