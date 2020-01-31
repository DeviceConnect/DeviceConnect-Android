package org.deviceconnect.android.libmedia.streaming.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;

import java.util.ArrayList;
import java.util.List;

public class CameraSurfaceVideoEncoder extends SurfaceVideoEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CAMERA";

    /**
     * カメラを操作するためのクラス.
     */
    private Camera2Wrapper mCamera2;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * カメラのプレビューを描画する Surface.
     */
    private List<Surface> mSurfaces = new ArrayList<>();

    /**
     * 映像のエンコード設定.
     */
    private CameraVideoQuality mVideoQuality;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraSurfaceVideoEncoder(Context context) {
        this(context, new CameraVideoQuality("video/avc"));
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param videoQuality 映像エンコードの設定
     */
    public CameraSurfaceVideoEncoder(Context context, CameraVideoQuality videoQuality) {
        mContext = context;
        mVideoQuality = videoQuality;
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * カメラの映像を描画する Surface を追加します.
     *
     * @param surface カメラの映像を描画する Surface.
     */
    public void addSurface(Surface surface) {
        mSurfaces.add(surface);
    }

    /**
     * カメラの映像を描画する Surface を削除します.
     *
     * @param surface カメラの映像を描画する Surface
     */
    public void removeSurface(Surface surface) {
        mSurfaces.remove(surface);
    }

    // MediaEncoder

    @Override
    protected void release() {
        unregisterScreenChange();
        stopCamera();
        super.release();
    }

    // VideoEncoder

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    @Override
    protected int getDisplayRotation() {
        return Camera2WrapperManager.getDisplayRotation(mContext);
    }

    @Override
    public boolean isSwappedDimensions() {
        return Camera2WrapperManager.isSwappedDimensions(mContext, mVideoQuality.getFacing());
    }

    // SurfaceVideoEncoder

    @Override
    protected void onStartSurfaceDrawing() {
        startCamera();
        registerScreenChange();
    }

    @Override
    protected void onStopSurfaceDrawing() {
        unregisterScreenChange();
        stopCamera();
    }

    /**
     * 写真撮影を行います.
     *
     * @param l 撮影した写真を通知するリスナー
     */
    public void takePicture(ImageReader.OnImageAvailableListener l) {
        if (mCamera2 == null) {
            if (DEBUG) {
                Log.w(TAG, "Camera2 is null.");
            }
            return;
        }
        mCamera2.takePicture(l);
    }

    /**
     * カメラの準備を行います.
     */
    public synchronized void startCamera() {
        if (mCamera2 != null) {
            return;
        }

        int videoWidth = mVideoQuality.getVideoWidth();
        int videoHeight = mVideoQuality.getVideoHeight();

        mCamera2 = Camera2WrapperManager.createCamera(mContext, mVideoQuality.getFacing());
        mCamera2.setCameraEventListener(new Camera2Wrapper.CameraEventListener() {
            @Override
            public void onOpen() {
                if (DEBUG) {
                    Log.d(TAG, "CameraSurfaceVideoEncoder::onOpen");
                }
                if (mCamera2 != null) {
                    mCamera2.startPreview();
                }
            }

            @Override
            public void onStartPreview() {
                if (DEBUG) {
                    Log.d(TAG, "CameraSurfaceVideoEncoder::onStartPreview");
                }
            }

            @Override
            public void onStopPreview() {
                if (DEBUG) {
                    Log.d(TAG, "CameraSurfaceVideoEncoder::onStopPreview");
                }
            }

            @Override
            public void onError(Camera2WrapperException e) {
                postOnError(new MediaEncoderException(e));
            }
        });
        mCamera2.getSettings().setPreviewSize(new Size(videoWidth, videoHeight));
        mCamera2.open(getSurfaceTexture(), new ArrayList<>(mSurfaces));
    }

    /**
     * カメラの解放を行います.
     */
    public synchronized void stopCamera() {
        if (mCamera2 != null) {
            mCamera2.stopPreview();
            mCamera2.close();
            mCamera2 = null;
        }
    }

    /**
     * 画面の回転イベントを受け取る BroadcastReceiver を登録します.
     */
    private void registerScreenChange() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mConfigurationReceiver, filter);
    }

    /**
     * 画面の回転イベントを受け取る BroadcastReceiver を解除します.
     */
    private void unregisterScreenChange() {
        try {
            mContext.unregisterReceiver(mConfigurationReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * 画面の回転イベントを受け取る BroadcastReceiver.
     */
    private final BroadcastReceiver mConfigurationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestChangeVideoSize();
        }
    };
}
