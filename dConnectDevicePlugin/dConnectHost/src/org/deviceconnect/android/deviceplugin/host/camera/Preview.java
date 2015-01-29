/*
 Preview.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import java.io.IOException;
import java.util.List;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * カメラのプレビューを表示するクラス.
 * 
 * @author NTT DOCOMO, INC.
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback {
    /** デバック用タグ. */
    public static final String LOG_TAG = "DeviceConnectCamera:Preview";

    /** Debug Tag. */
    @SuppressWarnings("unused")
	private static final String TAG = "PluginHost";

    /** プレビューを表示するSurfaceView. */
    private SurfaceView mSurfaceView;
    /** SurfaceViewを一時的に保持するホルダー. */
    private SurfaceHolder mHolder;
    /** プレビューのサイズ. */
    private Size mPreviewSize;
    /** サポートしているプレビューのサイズ. */
    private List<Size> mSupportedPreviewSizes;
    /** カメラのインスタンス. */
    private Camera mCamera;

    /**
     * ホストデバイスプラグインから渡されたリクエストID.<br>
     * - Broadcastで指示された場合は設定する。<br>
     * - アプリ内ならの指示ならnullを設定する。<br>
     */
    private String mRequestid;

    /**
     * コンストラクタ.
     * 
     * @param context このクラスが属するコンテキスト
     */
    public Preview(final Context context) {
        super(context);

        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    /**
     * カメラのインスタンスを設定する.
     * 
     * @param camera カメラのインスタンス
     */
    public void setCamera(final Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    /**
     * カメラのインスタンスを切り替えます.
     * 
     * @param camera 切り替えるカメラのインスタンス
     */
    public void switchCamera(final Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "IOException caused by setPreviewDisplay()", exception);
            }
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        camera.setParameters(parameters);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
            }
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "IOException caused by setPreviewDisplay()", exception);
            }
        }
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int w, final int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }

    /**
     * 最適なプレビューサイズを取得する. 指定されたサイズに最適なものがない場合にはnullを返却する。
     * 
     * @param sizes プレビューサイズ一覧
     * @param w 横幅
     * @param h 縦幅
     * @return 最適なプレビューサイズ
     */
    private Size getOptimalPreviewSize(final List<Size> sizes, final int w, final int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * 写真撮影を開始する.
     * 
     * @param requestid リクエストID(Broadcastで指示された場合は設定する。アプリ内ならの指示ならnullを設定する)
     */
    public void takePicture(Camera.PictureCallback callback) {
        mCamera.takePicture(mShutterCallback, null, callback);
        Toast.makeText(getContext(), R.string.shutter, Toast.LENGTH_SHORT).show();
    }

    /**
     * ズームイン処理を行う.
     * 
     * @param requestid リクエストID(Broadcastで指示された場合は設定する。アプリ内ならの指示ならnullを設定する)
     */
    public void zoomIn(final String requestid) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "zoomIn() start - requestid:" + requestid);
        }

        mRequestid = requestid;

        /* ズームイン処理 */
        Camera.Parameters parameters = mCamera.getParameters();
        int nowZoom = parameters.getZoom();
        if (nowZoom < parameters.getMaxZoom()) {
            parameters.setZoom(nowZoom + 1);
        }
        mCamera.setParameters(parameters);

        /* Toast表示 */
        String debugToast = getResources().getString(R.string.zoomin) + " requestid:" + mRequestid;
        Toast.makeText(getContext(), debugToast, Toast.LENGTH_SHORT).show();

        /* リクエストIDが登録されていたら、撮影完了後にホストデバイスプラグインへズームイン完了通知を送信する */
        if (mRequestid != null) {
            Context context = getContext();
            Intent intent = new Intent(CameraConst.SEND_CAMERA_TO_HOSTDP);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_ZOOMIN);
            intent.putExtra(CameraConst.EXTRA_REQUESTID, mRequestid);
            context.sendBroadcast(intent);
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "sendBroadcast() - action:" + CameraConst.SEND_CAMERA_TO_HOSTDP + " name:"
                        + CameraConst.EXTRA_NAME_ZOOMIN + " mRequestid:" + mRequestid);
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "zoomIn() end");
        }
    }

    /**
     * ズームアウト処理を行う.
     * 
     * @param requestid リクエストID(Broadcastで指示された場合は設定する。アプリ内ならの指示ならnullを設定する)
     */
    public void zoomOut(final String requestid) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "zoomOut() start - requestid:" + requestid);
        }

        mRequestid = requestid;

        /* ズームアウト処理 */
        Camera.Parameters parameters = mCamera.getParameters();
        int nowZoom = parameters.getZoom();
        if (nowZoom > 0) {
            parameters.setZoom(nowZoom - 1);
        }
        mCamera.setParameters(parameters);

        /* Toast表示 */
        String debugToast = getResources().getString(R.string.zoomout) + " requestid:" + mRequestid;
        Toast.makeText(getContext(), debugToast, Toast.LENGTH_SHORT).show();

        /* リクエストIDが登録されていたら、撮影完了後にホストデバイスプラグインへズームアウト完了通知を送信する */
        if (mRequestid != null) {
            Context context = getContext();
            Intent intent = new Intent(CameraConst.SEND_CAMERA_TO_HOSTDP);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_ZOOMOUT);
            intent.putExtra(CameraConst.EXTRA_REQUESTID, mRequestid);
            context.sendBroadcast(intent);
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "sendBroadcast() - action:" + CameraConst.SEND_CAMERA_TO_HOSTDP + " name:"
                        + CameraConst.EXTRA_NAME_ZOOMOUT + " mRequestid:" + mRequestid);
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "zoomOut() end");
        }
    }

    /**
     * シャッターコールバック.
     * <p>
     * - シャッター音を鳴らすために使用する。
     */
    private final Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // NOP
        }
    };
}
