package org.deviceconnect.android.libmedia.streaming.util;

import android.graphics.SurfaceTexture;
import android.media.ImageReader;

import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CameraSurfaceDrawingThread extends EGLSurfaceDrawingThread {
    /**
     * カメラを操作するためのクラス.
     */
    private Camera2Wrapper mCamera2;

    /**
     * カメラの状態を確認。
     */
    private boolean mRunningFlag;

    public CameraSurfaceDrawingThread(Camera2Wrapper camera2) {
        if (camera2 == null) {
            throw new IllegalArgumentException("camera2 is null.");
        }
        mCamera2 = camera2;
    }

    // EGLSurfaceDrawingThread

    @Override
    public int getDisplayRotation() {
        return mCamera2.getDisplayRotation();
    }

    @Override
    public boolean isSwappedDimensions() {
        return mCamera2.isSwappedDimensions();
    }

    @Override
    protected void onStarted() {
        startCamera(getSurfaceTexture());
    }

    @Override
    protected void onStopped() {
        stopCamera();
    }

    // private method.

    /**
     * カメラを操作するクラスを取得します.
     *
     * @return カメラを操作するためのクラス
     */
    public Camera2Wrapper getCamera2Wrapper() {
        return mCamera2;
    }

    /**
     * 写真撮影を行います.
     *
     * @param l 撮影した写真を通知するリスナー
     */
    public void takePicture(ImageReader.OnImageAvailableListener l) {
        mCamera2.takePicture(l);
    }

    /**
     * カメラの準備を行います.
     */
    private synchronized void startCamera(SurfaceTexture surfaceTexture) {
        if (mRunningFlag) {
            return;
        }
        mRunningFlag = true;

        CountDownLatch latch = new CountDownLatch(1);
        mCamera2.setCameraEventListener(new Camera2Wrapper.CameraEventListener() {
            @Override
            public void onOpen() {
                if (mCamera2 != null) {
                    mCamera2.startPreview();
                }
                latch.countDown();
            }

            @Override
            public void onStartPreview() {
            }

            @Override
            public void onStopPreview() {
            }

            @Override
            public void onError(Camera2WrapperException e) {
                postOnError(e);
            }
        });
        mCamera2.open(surfaceTexture);

        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                // タイムアウト
                throw new RuntimeException("Timed out opening a camera.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * カメラの解放を行います.
     */
    private synchronized void stopCamera() {
        if (!mRunningFlag) {
            return;
        }
        mRunningFlag = false;

        mCamera2.stopPreview();
        mCamera2.close();
    }
}
