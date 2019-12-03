/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.GLDrawer2D;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSettingData;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;


/**
 * カメラのプレビューをMJPEG形式で配信するサーバー.
 *
 * {@link SurfaceTexture} をもとに実装.
 */
class Camera2MJPEGPreviewServer implements PreviewServer {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "host.dplugin";

    private static final String MIME_TYPE = "video/x-mjpeg";

    private final Camera2Recorder mRecorder;

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private HandlerThread mPreviewThread;

    private Handler mPreviewHandler;

    private boolean mIsRecording;

    private boolean requestDraw;

    private Object mSync = new Object();

    private final Object mDrawTaskSync = new Object();

    private Thread mDrawTaskThread;

    private DrawTask mDrawTask;

    private final MixedReplaceMediaServer.Callback mMediaServerCallback = new MixedReplaceMediaServer.Callback() {
        @Override
        public boolean onAccept(final Socket socket) {
            try {
                if (DEBUG) {
                    Log.d(TAG, "MediaServerCallback.onAccept: recorder=" + mRecorder.getName());
                }
                if (mRecorder.isStartedPreview()) {
                    return false;
                }
                return startDrawTask();
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to start preview.", e);
                }
                return false;
            }
        }

        @Override
        public void onClosed(final Socket socket) {
            if (DEBUG) {
                Log.d(TAG, "MediaServerCallback.onClosed: recorder=" + mRecorder.getName());
            }
            stopDrawTask();
        }
    };

    Camera2MJPEGPreviewServer(final Camera2Recorder recorder) {
        mRecorder = recorder;
    }

    private int getCurrentRotation() {
        WindowManager windowManager = (WindowManager) mRecorder.getContext().getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getRotation();
    }

    @Override
    public int getQuality() {
        return RecorderSettingData.getInstance(mRecorder.getContext()).readPreviewQuality(mRecorder.getId());
    }

    @Override
    public void setQuality(int quality) {
        RecorderSettingData.getInstance(mRecorder.getContext()).storePreviewQuality(mRecorder.getId(),
                quality);
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            if (mIsRecording) {
                callback.onStart(mServer.getUrl());
                return;
            }
            mIsRecording = true;

            mPreviewThread = new HandlerThread("MotionJPEG");
            mPreviewThread.start();
            mPreviewHandler = new Handler(mPreviewThread.getLooper());

            final String uri;
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpeg");
                mServer.setCallback(mMediaServerCallback);
                uri = mServer.start();
            } else {
                uri = mServer.getUrl();
            }
            callback.onStart(uri);
        }
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (!mIsRecording) {
                return;
            }
            mIsRecording = false;
            stopDrawTask();
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }

            CameraWrapper camera = mRecorder.getCameraWrapper();
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (CameraWrapperException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to stop preview.", e);
                    }
                }
            }
            mPreviewThread.quit();
            mPreviewThread = null;
            mPreviewHandler = null;

            mRecorder.hideNotification();
        }
    }

    private boolean startDrawTask() {
        synchronized (mDrawTaskSync) {
            if (mDrawTaskThread == null) {
                mDrawTask = new DrawTask();
                mDrawTaskThread = new Thread(mDrawTask);
                mDrawTaskThread.start();
                return true;
            } else {
                return false;
            }
        }
    }

    private void stopDrawTask() {
        synchronized (mDrawTaskSync) {
            if (mDrawTaskThread != null) {
                mDrawTask.release(false);
                mDrawTaskThread = null;
            }
        }
    }

    @Override
    public void onDisplayRotation(final int rotation) {
        DrawTask drawTask = mDrawTask;
        synchronized (mLockObj) {
            if (drawTask != null) {
                drawTask.onDisplayRotationChange(rotation);
            }
        }
    }

    @Override
    public void mute() {
        // NOP
    }

    @Override
    public void unMute() {
        // NOP
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final class DrawTask extends EglTask {

        private long intervals;
        private EGLBase.IEglSurface mEncoderSurface;
        private GLDrawer2D mDrawer;
        private final float[] mTexMatrix = new float[16];
        private int mTexId;
        private SurfaceTexture mSourceTexture;
        private Surface mSourceSurface;

        private HostDeviceRecorder.PictureSize mPreviewSize;
        private Bitmap mBitmap;
        private ByteBuffer mByteBuffer;
        private ByteArrayOutputStream mOutput;
        private int mJpegQuality;

        private final Object mDrawSync = new Object();
        private int mRotationDegree;
        private float mDeltaX;
        private float mDeltaY;

        DrawTask() {
            super(null, 0);
        }

        private void createSurface(final HostDeviceRecorder.PictureSize size) {
            int w = size.getWidth();
            int h = size.getHeight();
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mByteBuffer = ByteBuffer.allocateDirect(w * h * 4);
            mOutput = new ByteArrayOutputStream();
            mSourceTexture = new SurfaceTexture(mTexId);
            setDefaultBufferSize(getCurrentRotation(), w, h);
            mSourceSurface = new Surface(mSourceTexture);
            mSourceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener, mPreviewHandler);
            mEncoderSurface = getEgl().createOffscreen(w, h);
        }

        private void releaseSurface() {
            if (mSourceSurface != null) {
                mSourceSurface.release();
                mSourceSurface = null;
            }
            if (mSourceTexture != null) {
                mSourceTexture.release();
                mSourceTexture = null;
            }
            if (mEncoderSurface != null) {
                mEncoderSurface.release();
                mEncoderSurface = null;
            }
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
        }

        @Override
        protected void onStart() {
            mDrawer = new GLDrawer2D(true);
            mTexId = mDrawer.initTex();

            detectDisplayRotation(getCurrentRotation());
            createSurface(mPreviewSize);

            intervals = (long)(1000f / mRecorder.getMaxFrameRate());
            mJpegQuality = getQuality();

            try {
                mRecorder.startPreview(mSourceSurface);
                mRecorder.sendNotification();
                if (DEBUG) {
                    Log.d(TAG, "Started camera preview.");
                }
            } catch (CameraWrapperException e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to start camera preview.", e);
                }
            }

            // 録画タスクを起床
            queueEvent(mSurfaceDrawTask);
        }

        @Override
        protected void onStop() {
            if (mDrawer != null) {
                mDrawer.release();
                mDrawer = null;
            }
            mDrawTask = null;
            releaseSurface();
            try {
                mRecorder.stopPreview();
            } catch (CameraWrapperException e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to stop camera preview.", e);
                }
            }
            makeCurrent();
        }

        @Override
        protected boolean onError(final Exception e) {
            Log.w(TAG, "mScreenCaptureTask:", e);
            return false;
        }

        @Override
        protected Object processRequest(final int request, final int arg1, final int arg2, final Object obj) {
            return null;
        }

        // TextureSurfaceで映像を受け取った際のコールバックリスナー
        private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
                if (mIsRecording) {
                    synchronized (mSync) {
                        requestDraw = true;
                        mSync.notifyAll();
                    }
                }
            }
        };

        private long mLastTime = 0;

        private final Runnable mSurfaceDrawTask = new Runnable() {
            @Override
            public void run() {
                boolean localRequestDraw;
                synchronized (mSync) {
                    localRequestDraw = requestDraw;
                    if (!requestDraw) {
                        try {
                            mSync.wait(intervals);
                            localRequestDraw = requestDraw;
                            requestDraw = false;
                        } catch (final InterruptedException e) {
                            if (DEBUG) {
                                Log.v(TAG, "draw:InterruptedException");
                            }
                            return;
                        }
                    }
                }

                long now = System.currentTimeMillis();
                long interval = intervals - (now - mLastTime);
                if (interval > 0) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                mLastTime = now;

                if (mIsRecording) {
                    synchronized (mDrawSync) {
                        if (localRequestDraw) {
                            mSourceTexture.updateTexImage();
                            mSourceTexture.getTransformMatrix(mTexMatrix);
                            Matrix.scaleM(mTexMatrix, 0, 1, -1, 0);
                            Matrix.rotateM(mTexMatrix, 0, mRotationDegree, 0, 0, 1);
                            Matrix.translateM(mTexMatrix, 0, mDeltaX, mDeltaY, 0);
                        }

                        // SurfaceTextureで受け取った画像をMediaCodecの入力用Surfaceへ描画する
                        mEncoderSurface.makeCurrent();
                        mDrawer.draw(mTexId, mTexMatrix, 0);

                        GLES20.glFinish();
                        mByteBuffer.rewind();
                        GLES20.glReadPixels(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mByteBuffer);
                        mBitmap.copyPixelsFromBuffer(mByteBuffer);

                        mOutput.reset();
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, mJpegQuality, mOutput);
                    }
                    byte[] jpeg = mOutput.toByteArray();
                    offerMedia(jpeg);
                    queueEvent(this);
                } else {
                    releaseSelf();
                }
            }
        };

        private void offerMedia(final byte[] jpeg) {
            MixedReplaceMediaServer server = mServer;
            if (server != null) {
                server.offerMedia(jpeg);
            }
        }

        private void onDisplayRotationChange(final int rotation) {
            queueEvent(() -> {
                synchronized (mDrawSync) {
                    if (mBitmap != null && !mBitmap.isRecycled()) {
                        mBitmap.recycle();
                    }
                    if (mEncoderSurface != null) {
                        mEncoderSurface.release();
                    }

                    // プレビューサイズ更新
                    detectDisplayRotation(rotation);
                    int w = mPreviewSize.getWidth();
                    int h = mPreviewSize.getHeight();
                    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    setDefaultBufferSize(rotation, w, h);
                    mEncoderSurface = getEgl().createOffscreen(w, h);
                }
            });
        }
        private void setDefaultBufferSize(final int rotation, final int w, final int h) {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    mSourceTexture.setDefaultBufferSize(h, w);
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                default:
                    mSourceTexture.setDefaultBufferSize(w, h);
                    break;
            }
        }
        private void detectDisplayRotation(final int rotation) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    mRotationDegree = 0;
                    mDeltaX = 0;
                    mDeltaY = -1;
                    break;
                case Surface.ROTATION_90:
                    mRotationDegree = 90;
                    mDeltaX = -1;
                    mDeltaY = -1;
                    break;
                case Surface.ROTATION_180:
                    mRotationDegree = 180;
                    mDeltaX = -1;
                    mDeltaY = 0;
                    break;
                case Surface.ROTATION_270:
                    mRotationDegree = 270;
                    mDeltaX = 0;
                    mDeltaY = 0;
                    break;
            }
            mPreviewSize = mRecorder.getRotatedPreviewSize();
        }

    }

    @Override
    public boolean isMuted() {
        return false;
    }
}
