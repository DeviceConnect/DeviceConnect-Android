package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.util.ImageUtils;
import org.deviceconnect.android.deviceplugin.webrtc.util.MixedReplaceMediaClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.SurfaceTextureHelper;
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
     * Flag for debugging.
     */
    private final static boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Tag for debugging.
     */
    private final static String TAG = "VideoExternal";

    private int mWidth;
    private int mHeight;
    private int mFPS;

    /**
     * Observer to send the buffer of capture image.
     */
    private VideoCapturerAndroid.CapturerObserver mFrameObserver;

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
     * Uri of resource.
     */
    private String mUri;

    /**
     * Lock object for updating the image.
     */
    private final Object mLockObj = new Object();

    private SurfaceTextureHelper mSurfaceHelper;
    private Handler mCameraThreadHandler;
    private Surface mSurface;
    private final float[] mTransformMatrix = new float[16];
    private Paint mPaint = new Paint();

    /**
     * Constructor.
     *
     * @param uri    uri of resource
     * @param width  width
     * @param height height
     */
    public VideoCapturerExternalResource(EglBase.Context sharedContext, final String uri, final int width, final int height) {
        mUri = uri;
        mWidth = width;
        mHeight = height;
        mFPS = 30;

        HandlerThread cameraThread = new HandlerThread(TAG);
        cameraThread.start();
        mCameraThreadHandler = new Handler(cameraThread.getLooper());
        mSurfaceHelper = SurfaceTextureHelper.create(sharedContext, mCameraThreadHandler);
    }

    @Override
    public String getSupportedFormatsAsJson() throws JSONException {
        JSONArray json_formats = new JSONArray();
        JSONObject json_format = new JSONObject();
        json_format.put("width", mWidth);
        json_format.put("height", mHeight);
        json_format.put("framerate", mFPS);
        json_formats.put(json_format);

        JSONObject json_format2 = new JSONObject();
        json_format2.put("width", mHeight);
        json_format2.put("height", mWidth);
        json_format2.put("framerate", mFPS);
        json_formats.put(json_format2);

        if (DEBUG) {
            Log.e(TAG, "@@@@ getSupportedFormatsAsJson: json=" + json_formats.toString(4));
        }
        return json_formats.toString();
    }

    @Override
    public void startCapture(final int width, final int height, final int frameRate,
                             final Context applicationContext,
                             final VideoCapturerAndroid.CapturerObserver frameObserver) {
        if (DEBUG) {
            Log.i(TAG, "@@@ startCapture size:[" + width + ", " + height
                    + "] frameRate:" + frameRate);
        }

        mSurfaceHelper.getSurfaceTexture().setDefaultBufferSize(width, height);
        mSurface = new Surface(mSurfaceHelper.getSurfaceTexture());

        mRequestWidth = width;
        mRequestHeight = height;
        mFrameObserver = frameObserver;

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
        if (DEBUG) {
            Log.i(TAG, "@@@ stopCapture");
        }

        if (mClient != null) {
            mClient.stop();
            mClient = null;
        }

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }

        mFrameObserver = null;
    }

    @Override
    public void returnBuffer(final long timeStamp) {
        if (DEBUG) {
            Log.d(TAG, "@@@ returnBuffer");
        }
    }

    @Override
    public void onOutputFormatRequest(final int width, final int height, final int fps) {
        if (DEBUG) {
            Log.d(TAG, "@@@ onOutputFormatRequest");
        }
    }

    @Override
    public void release() {
        if (DEBUG) {
            Log.d(TAG, "@@@ release");
        }

        stopCapture();

        if (mSurfaceHelper != null) {
            mSurfaceHelper.disconnect(mCameraThreadHandler);
            mSurfaceHelper = null;
        }
    }

    @Override
    public void switchCamera(VideoCapturerAndroid.CameraSwitchHandler cameraSwitchHandler) {
        if (DEBUG) {
            Log.e(TAG, "switchCamera:");
        }
    }

    @Override
    public void changeCaptureFormat(int width, int height, int frameRate) {
        if (DEBUG) {
            Log.i(TAG, "changeCaptureFormat: (" + width + ", " + height + ") frameRate=" + frameRate);
        }
    }

    @Override
    public SurfaceTextureHelper getSurfaceTextureHelper() {
        return mSurfaceHelper;
    }

    @Override
    public void onTextureFrameAvailable(int oesTextureId, float[] transformMatrix, long timestampNs) {
        if (mFrameObserver != null) {
            mFrameObserver.onTextureFrameCaptured(mRequestWidth, mRequestHeight, oesTextureId, transformMatrix, 0, timestampNs);
        }
    }

    private void deliverTextureFrame() {
        mSurfaceHelper.getSurfaceTexture().getTransformMatrix(mTransformMatrix);
        long timestampNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
        onTextureFrameAvailable(mSurfaceHelper.getOesTextureId(), mTransformMatrix, timestampNs);
    }

    /**
     * Receive an event from MixedReplaceMediaClient.
     */
    private final MixedReplaceMediaClient.OnMixedReplaceMediaListener mOnMixedReplaceMediaListener
            = new MixedReplaceMediaClient.OnMixedReplaceMediaListener() {
        @Override
        public void onConnected() {
            mFrameObserver.onCapturerStarted(true);
        }

        @Override
        public void onReceivedData(final InputStream in) {
            final Bitmap bitmap = ImageUtils.resize(BitmapFactory.decodeStream(in));
            if (bitmap != null) {
                synchronized (mLockObj) {
                    if (mRequestWidth != bitmap.getWidth() || mRequestHeight != bitmap.getHeight()) {
                        if (DEBUG) {
                            Log.d(TAG, "@@@ ChangeSize: " + bitmap.getWidth() + " " + bitmap.getHeight());
                        }

                        mRequestWidth = bitmap.getWidth();
                        mRequestHeight = bitmap.getHeight();
                        mFrameObserver.onOutputFormatRequest(mRequestWidth, mRequestHeight, mFPS);

                        if (mSurface != null) {
                            mSurface.release();
                            mSurface = null;
                        }

                        mSurfaceHelper.getSurfaceTexture().setDefaultBufferSize(mRequestWidth, mRequestHeight);
                        mSurface = new Surface(mSurfaceHelper.getSurfaceTexture());
                    }

                    Canvas canvas = mSurface.lockCanvas(null);
                    canvas.drawBitmap(bitmap, 0, 0, mPaint);
                    mSurface.unlockCanvasAndPost(canvas);

                    mCameraThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSurfaceHelper.getSurfaceTexture().updateTexImage();
                            deliverTextureFrame();
                        }
                    });
                }

                bitmap.recycle();
            }
        }

        @Override
        public void onError(MixedReplaceMediaClient.MixedReplaceMediaError error) {
            if (DEBUG) {
                Log.w(TAG, "Error occurred by MixedReplaceMediaClient. " + error);
            }
            if (mFrameObserver != null) {
                mFrameObserver.onCapturerStarted(false);
            }
        }
    };
}
