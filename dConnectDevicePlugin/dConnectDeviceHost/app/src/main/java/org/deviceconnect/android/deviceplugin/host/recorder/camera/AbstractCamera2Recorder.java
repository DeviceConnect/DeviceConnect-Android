/*
 AbstractCamera2Recorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera2 API で実装されるレコーダーの基底クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class AbstractCamera2Recorder extends AbstractPreviewServerProvider {

    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * NotificationID のベースを定義.
     */
    private static final int NOTIFICATION_ID_BASE = 1010;

    /**
     * {@link CameraManager} のインスタンス.
     */
    protected final CameraManager mCameraManager;

    /**
     * カメラID.
     *
     * @see CameraManager#getCameraIdList()
     */
    protected final String mCameraId;

    /**
     * カメラのインスタンス.
     */
    protected CameraDevice mCamera;

    /**
     * カメラオプション.
     */
    protected final AbstractCamera2Recorder.Options mOptions = new AbstractCamera2Recorder.Options();

    /**
     * セッション作成用ハンドラー.
     */
    protected final Handler mHandler;

    /**
     * カメラの位置.
     */
    protected final Camera2PhotoRecorder.CameraFacing mFacing;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param cameraId カメラID
     */
    protected AbstractCamera2Recorder(final @NonNull Context context, final @NonNull String cameraId) {
        super(context, NOTIFICATION_ID_BASE + cameraId.hashCode());
        mCameraId = cameraId;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mFacing = detectFacing();
        mHandler = new Handler();
    }

    @SuppressWarnings("MissingPermission")
    protected synchronized void openCamera(final @NonNull CameraOpenCallback callback) {
        if (DEBUG) {
            Log.d(TAG, "openCamera: id=" + mCameraId);
        }
        CameraDevice camera = mCamera;
        if (camera != null) {
            callback.onOpen(camera, false);
            return;
        }

        try {
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final @NonNull CameraDevice camera) {
                    if (DEBUG) {
                        Log.d(TAG, "onOpened: id=" + camera.getId());
                    }
                    mCamera = camera;

                    callback.onOpen(camera, true);
                }

                @Override
                public void onDisconnected(final @NonNull CameraDevice camera) {
                    callback.onError(new RecorderException(RecorderException.REASON_DISABLED));
                }

                @Override
                public void onError(final @NonNull CameraDevice camera, final int error) {
                    int reason;
                    switch (error) {
                        case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                            reason = RecorderException.REASON_DISABLED;
                            break;
                        case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                            reason = RecorderException.REASON_IN_USE;
                            break;
                        case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                            reason = RecorderException.REASON_TOO_MANY;
                            break;
                        case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                        case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                        default:
                            reason = RecorderException.REASON_FATAL;
                            break;
                    }
                    callback.onError(new RecorderException(reason));
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            int error = e.getReason();
            int reason;
            switch (error) {
                case CameraAccessException.CAMERA_DISABLED:
                    reason = RecorderException.REASON_DISABLED;
                    break;
                case CameraAccessException.CAMERA_DISCONNECTED:
                    reason = RecorderException.REASON_DISCONNECTED;
                    break;
                case CameraAccessException.CAMERA_IN_USE:
                    reason = RecorderException.REASON_IN_USE;
                    break;
                case CameraAccessException.MAX_CAMERAS_IN_USE:
                    reason = RecorderException.REASON_TOO_MANY;
                    break;
                case CameraAccessException.CAMERA_ERROR:
                default:
                    reason = RecorderException.REASON_FATAL;
                    break;
            }
            callback.onError(new RecorderException(reason, e));
        }
    }

    /**
     * カメラオプションを保持するクラス.
     */
    public static class Options {

        /**
         * デフォルトのプレビューサイズの閾値を定義.
         */
        private static final int DEFAULT_PREVIEW_WIDTH_THRESHOLD = 640;

        /**
         * デフォルトのプレビューサイズの閾値を定義.
         */
        private static final int DEFAULT_PREVIEW_HEIGHT_THRESHOLD = 480;

        private Size mPictureSize;

        private Size mPreviewSize;

        private List<Size> mSupportedPictureSizeList = new ArrayList<>();

        private List<Size> mSupportedPreviewSizeList = new ArrayList<>();

        Size getPictureSize() {
            return mPictureSize;
        }

        void setPictureSize(final Size pictureSize) {
            mPictureSize = pictureSize;
        }

        Size getPreviewSize() {
            return mPreviewSize;
        }

        void setPreviewSize(final Size previewSize) {
            mPreviewSize = previewSize;
        }

        List<Size> getSupportedPictureSizeList() {
            return mSupportedPictureSizeList;
        }

        void setSupportedPictureSizeList(final List<Size> supportedPictureSizeList) {
            mSupportedPictureSizeList = new ArrayList<>(supportedPictureSizeList);
        }

        List<Size> getSupportedPreviewSizeList() {
            return mSupportedPreviewSizeList;
        }

        void setSupportedPreviewSizeList(final List<Size> supportedPreviewSizeList) {
            mSupportedPreviewSizeList = new ArrayList<>(supportedPreviewSizeList);
        }

        Size getDefaultPictureSize() {
            return getDefaultSizeFromeList(mSupportedPictureSizeList);
        }

        Size getDefaultPreviewSize() {
            return getDefaultSizeFromeList(mSupportedPreviewSizeList);
        }

        private static Size getDefaultSizeFromeList(final List<Size> sizeList) {
            if (sizeList.size() == 0) {
                return null;
            }
            Size defaultSize = null;
            for (Size size : sizeList) {
                if (size.getWidth() == DEFAULT_PREVIEW_WIDTH_THRESHOLD &&
                        size.getHeight() == DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                    defaultSize = size;
                }
            }
            if (defaultSize != null) {
                return defaultSize;
            }
            for (Size size : sizeList) {
                if (size.getWidth() * size.getHeight() <=
                        DEFAULT_PREVIEW_WIDTH_THRESHOLD * DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                    defaultSize = size;
                }
            }
            if (defaultSize != null) {
                return defaultSize;
            }
            return sizeList.get(0);
        }
    }

    /**
     * カメラの位置を判定する.
     * @return カメラの位置
     */
    private CameraFacing detectFacing() {
        try {
            int facing = Camera2Helper.getFacing(mCameraManager, mCameraId);
            switch (facing) {
                case CameraCharacteristics.LENS_FACING_BACK:
                    return CameraFacing.BACK;
                case CameraCharacteristics.LENS_FACING_FRONT:
                    return CameraFacing.FRONT;
                case CameraCharacteristics.LENS_FACING_EXTERNAL:
                    return CameraFacing.EXTERNAL;
                default:
                    return CameraFacing.UNKNOWN;
            }
        } catch (CameraAccessException e) {
            return CameraFacing.UNKNOWN;
        }
    }

    /**
     * カメラの位置.
     */
    public enum CameraFacing {
        /** スマートフォンの裏側. */
        BACK("back"),

        /** スマートフォンの正面. */
        FRONT("front"),

        /** 外部接続. (e.g. OTG 接続されている USB カメラ) */
        EXTERNAL("external"),

        /** 不明. */
        UNKNOWN("unknown");

        /** カメラの位置を表現する名前. */
        private final String mName;

        /**
         * コンストラクタ.
         * @param name カメラの位置を表現する名前
         */
        CameraFacing(final String name) {
            mName = name;
        }

        /**
         * カメラの位置を表現する名前を取得する.
         * @return 名前
         */
        public String getName() {
            return mName;
        }
    }

}
