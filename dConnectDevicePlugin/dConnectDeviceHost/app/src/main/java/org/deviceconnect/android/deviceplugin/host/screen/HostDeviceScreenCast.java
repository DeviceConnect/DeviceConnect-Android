/*
 HostDeviceScreenCast.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.screen;


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
import android.util.DisplayMetrics;

import org.deviceconnect.android.deviceplugin.host.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.camera.MixedReplaceMediaServer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Host Device Screen Cast.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(21)
public class HostDeviceScreenCast implements HostDeviceRecorder, HostDevicePreviewServer {

    public static final String ACTION_PERMISSION =
        HostDeviceScreenCast.class.getPackage().getName() + ".permission";

    static final String RESULT_CODE = "result_code";

    static final String RESULT_DATA = "result_data";

    private static final String ID = "screen";

    private static final String NAME = "AndroidHost Screen";

    private static final String MIME_TYPE = "video/x-mjpeg";

    private static final double DEFAULT_MAX_FPS = 10.0d;

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

    private final List<PictureSize> mSupportedPreviewSizes = new ArrayList<PictureSize>();

    private PictureSize mPreviewSize;

    private BroadcastReceiver mPermissionReceiver;

    private long mFrameInterval;

    private double mMaxFps;

    public HostDeviceScreenCast(final Context context) {
        mContext = context;
        mManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        PictureSize size = new PictureSize(metrics.widthPixels, metrics.heightPixels);
        mDisplayDensityDpi = metrics.densityDpi;
        initSupportedPreviewSizes(size);
        mPreviewSize = mSupportedPreviewSizes.get(0);

        mMaxFps = DEFAULT_MAX_FPS;
        setPreviewFrameRate(mMaxFps);
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
    public String[] getSupportedMimeTypes() {
        return new String[] {MIME_TYPE};
    }

    @Override
    public RecorderState getState() {
        return mIsCasting ? RecorderState.RECORDING : RecorderState.INACTTIVE;
    }

    @Override
    public void initialize() {
        // Nothing to do.
    }

    @Override
    public boolean mutablePictureSize() {
        return false;
    }

    @Override
    public boolean usesCamera() {
        return false;
    }

    @Override
    public int getCameraId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsPictureSize(int width, int height) {
        throw new UnsupportedOperationException();
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
        mImageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 10);
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
                    } catch (InterruptedException e) {
                        break;
                    }
                }
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

        int offset = 0;
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        Bitmap bitmap = Bitmap.createBitmap(metrics, width, height, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = planes[0].getBuffer();
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int pixel = 0;
                pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                pixel |= (buffer.get(offset + 2) & 0xff);       // B
                pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                bitmap.setPixel(j, i, pixel);
                offset += pixelStride;
            }
            offset += rowPadding;
        }
        img.close();

        return bitmap;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        return mSupportedPreviewSizes;
    }

    @Override
    public boolean supportsPreviewSize(final int width, final int height) {
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
    public double getPreviewMaxFrameRate() {
        return mMaxFps;
    }

    @Override
    public void setPreviewFrameRate(final double max) {
        mMaxFps = max;
        mFrameInterval = (long) (1 / max) * 1000L;
    }
}
