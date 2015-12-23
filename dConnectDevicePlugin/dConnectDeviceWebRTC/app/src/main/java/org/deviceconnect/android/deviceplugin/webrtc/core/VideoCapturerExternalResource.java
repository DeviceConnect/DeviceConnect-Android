package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.util.ImageUtils;
import org.deviceconnect.android.deviceplugin.webrtc.util.MixedReplaceMediaClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerObject;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Inputs the external resource to video on WebRTC.
 *
 * @author NTT DOCOMO, INC.
 */
public class VideoCapturerExternalResource implements VideoCapturerObject {
    /**
     * Tag for debugging.
     */
    private final static String TAG = "VideoCapturerExternal";

    private int mWidth;
    private int mHeight;
    private int mFPS;

    /**
     * The capturing of format.
     */
    private VideoCapturerAndroid.CaptureFormat mCaptureFormat;

    /**
     * Observer to send the buffer of capture image.
     */
    private VideoCapturerAndroid.CapturerObserver mFrameObserver;

    /**
     * The buffer of the image to be sent to the WebRTC.
     */
    private byte[] mYUVData;

    /**
     * The buffer of the bitmap.
     */
    private int[] mRGBData;

    /**
     * Width to request.
     */
    private int mRequestWidth;

    /**
     * Height to request.
     */
    private int mRequestHeight;

    /**
     * This client to get the image from the server.
     */
    private MixedReplaceMediaClient mClient;

    /**
     * Thread for updating the image.
     */
    private VideoThread mThread;

    /**
     * Uri of resource.
     */
    private String mUri;

    /**
     * Lock object for updating the image.
     */
    private final Object mLockObj = new Object();

    /**
     * Constructor.
     * @param uri uri of resource
     * @param width width
     * @param height height
     */
    public VideoCapturerExternalResource(final String uri, final int width, final int height) {
        mUri = uri;
        mWidth = width;
        mHeight = height;
        mFPS = 30;
    }

    @Override
    public boolean init(final String s) {
        return true;
    }

    @Override
    public String getSupportedFormatsAsJson() throws JSONException {
        JSONArray json_formats = new JSONArray();
        JSONObject json_format = new JSONObject();
        json_format.put("width", mWidth);
        json_format.put("height", mHeight);
        json_format.put("framerate", mFPS);
        json_formats.put(json_format);
        return json_formats.toString();
    }

    @Override
    public void startCapture(final int width, final int height, final int frameRate,
                             final Context applicationContext,
                             final VideoCapturerAndroid.CapturerObserver frameObserver) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@ startCapture size:[" + width + ", " + height
                    + "] frameRate:" + frameRate);
        }

        mRequestWidth = -1;
        mRequestHeight = -1;
        mFrameObserver = frameObserver;

        if (mThread != null) {
            mThread.joinThread();
            mThread = null;
        }

        mThread = new VideoThread();
        mThread.start();

        if (mClient != null) {
            mClient.stop();
            mClient = null;
        }

        mClient = new MixedReplaceMediaClient(mUri);
        mClient.setOnMixedReplaceMediaListener(mOnMixedReplaceMediaListener);
        mClient.start();
    }

    @Override
    public void stopCapture() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@ stopCapture");
        }

        if (mClient != null) {
            mClient.stop();
            mClient = null;
        }

        if (mThread != null) {
            mThread.joinThread();
            mThread = null;
        }

        mFrameObserver = null;
    }

    @Override
    public void returnBuffer(final long l) {
    }

    @Override
    public void onOutputFormatRequest(final int width, final int height, final int fps) {
        if (mFrameObserver != null) {
            synchronized (mLockObj) {
                mFrameObserver.OnOutputFormatRequest(width, height, fps);
            }
        }
    }

    @Override
    public void dispose() {
        stopCapture();
    }

    /**
     * Update the captured frame.
     */
    private void updateFrameCaptured() {
        synchronized (mLockObj) {
            if (mYUVData != null && mFrameObserver != null) {
                mFrameObserver.OnFrameCaptured(mYUVData, mCaptureFormat.frameSize(),
                        mCaptureFormat.width, mCaptureFormat.height,
                        0, TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime()));
            }
        }
    }

    /**
     * Thread for updating the image.
     */
    private class VideoThread extends Thread {
        /**
         * Keep alive flag.
         */
        private volatile boolean mKeepAlive = true;

        @Override
        public void run() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ VideoThread is start");
            }

            mFrameObserver.OnCapturerStarted(true);

            int sleep = 1000 / mFPS;
            while (mKeepAlive) {
                long oldTime = System.currentTimeMillis();
                updateFrameCaptured();
                long newTime = System.currentTimeMillis();
                long sleepTime = sleep - (newTime - oldTime);
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ VideoThread is stop");
            }
        }


        /**
         * Wait until this thread is finished.
         */
        public void joinThread() {
            mKeepAlive = false;
            interrupt();
            while (isAlive()) {
                try {
                    join();
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }
        }
    }

    /**
     * Receive an event from MixedReplaceMediaClient.
     */
    private final MixedReplaceMediaClient.OnMixedReplaceMediaListener mOnMixedReplaceMediaListener
            = new MixedReplaceMediaClient.OnMixedReplaceMediaListener() {
        @Override
        public void onReceivedData(final InputStream in) {
            Bitmap bitmap = ImageUtils.resize(BitmapFactory.decodeStream(in));
            if (bitmap != null) {
                synchronized (mLockObj) {
                    if (mRequestWidth != bitmap.getWidth() || mRequestHeight != bitmap.getHeight() || mRGBData == null) {
                        mRequestWidth = bitmap.getWidth();
                        mRequestHeight = bitmap.getHeight();
                        mFrameObserver.OnOutputFormatRequest(mRequestWidth, mRequestHeight, mFPS);
                        mCaptureFormat = new VideoCapturerAndroid.CaptureFormat(mRequestWidth, mRequestHeight, mFPS, mFPS);
                        mYUVData = ImageUtils.createBuffer(bitmap);
                        mRGBData = new int[mWidth * mHeight];
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "@@@ ChangeSize: " + mRequestWidth + " " + mRequestHeight);
                        }
                    }

                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    bitmap.getPixels(mRGBData, 0, width, 0, 0, width, height);
                    ImageUtils.argbToYV12(mYUVData, mRGBData, width, height);
                }
                bitmap.recycle();
            }
        }
        @Override
        public void onError(MixedReplaceMediaClient.MixedReplaceMediaError error) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Error occurred by MixedReplaceMediaClient. " + error);
            }
        }
    };
}
