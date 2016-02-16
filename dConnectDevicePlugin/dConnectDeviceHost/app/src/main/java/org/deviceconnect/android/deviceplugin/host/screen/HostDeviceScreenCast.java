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
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.camera.MixedReplaceMediaServer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

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

    private static final String TAG = "AAA";

    private static final String ID = "screen";

    private static final String NAME = "AndroidHost Screen";

    private static final String MIME_TYPE = "video/x-mjpeg";

    private final Context mContext;

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private MediaProjectionManager mManager;

    private MediaProjection mMediaProjection;

    private int mWidth;

    private int mHeight;

    private VirtualDisplay mVirtualDisplay;

    private ImageReader mImageReader;

    private boolean mIsCasting;

    private Thread mThread;

    public HostDeviceScreenCast(final Context context) {
        mContext = context;
        mManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private BroadcastReceiver mPermissionReceiver;

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
    public boolean mutableInputPictureSize() {
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
    public PictureSize getInputPictureSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInputPictureSize(final PictureSize size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
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
                                setupVirtualDisplay();
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
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mPermissionReceiver != null) {
                mContext.unregisterReceiver(mPermissionReceiver);
            }
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            stopScreenCast();
        }
    }

    private void requestPermission() {
        Log.d(TAG, "requestPermission");
        Intent intent = new Intent();
        intent.setClass(mContext, PermissionReceiverActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void setupMediaProjection(int resultCode, Intent data) {
        mMediaProjection = mManager.getMediaProjection(resultCode, data);
    }

    private void setupVirtualDisplay() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        int density = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 10);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
            "Android Host Screen",
            mWidth,
            mHeight,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader.getSurface(),
            new VirtualDisplay.Callback() {
                @Override
                public void onPaused() {
                    Log.d(TAG, "VirtualDisplay.Callback.onPaused");
                    stopScreenCast();
                }

                @Override
                public void onResumed() {
                    Log.d(TAG, "VirtualDisplay.Callback.onResumed");
                    startScreenCast();
                }

                @Override
                public void onStopped() {
                    Log.d(TAG, "VirtualDisplay.Callback.onStopped");
                }
            }, null);
    }

    private synchronized void startScreenCast() {
        if (mIsCasting) {
            return;
        }
        mIsCasting = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Server URL: " + mServer.getUrl());
                try {
                    while (mIsCasting) {
                        Thread.sleep(100);

                        Bitmap bitmap = getScreenshot();
                        if (bitmap == null) {
                            continue;
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] media = baos.toByteArray();
                        Log.d(TAG, "Media: " + media.length);
                        mServer.offerMedia(media);
                    }
                } catch (InterruptedException e) {
                    // Nothing to do.
                }
            }
        });
        mThread.start();
    }

    private synchronized void stopScreenCast() {
        if (!mIsCasting) {
            return;
        }
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        mIsCasting = false;
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
        //byte[] newData = new byte[width * height * 4];

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
}
