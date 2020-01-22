package org.deviceconnect.android.libmedia.streaming.video;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.libimage.ImageConverter;
import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;

public class CameraVideoImageReaderEncoder extends VideoImageReaderEncoder {
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
     * リスナーを実行するハンドラ.
     */
    private Handler mBackgroundHandler;

    /**
     * プレビュー画像の受信処理を行うハンドラ.
     */
    private HandlerThread mBackgroundThread;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public CameraVideoImageReaderEncoder(Context context) {
        this(context, new CameraVideoQuality("video/avc"));
    }

    /**
     * コンストラクタ.
     *
     * @param context      コンテキスト
     * @param videoQuality 映像エンコードの設定
     */
    public CameraVideoImageReaderEncoder(Context context, CameraVideoQuality videoQuality) {
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

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    @Override
    protected void startRecording() {
        super.startRecording();

        startCamera();
    }

    @Override
    protected void stopRecording() {
        if (mCamera2 != null) {
            mCamera2.stopPreview();
        }
        super.stopRecording();
    }

    @Override
    protected void release() {
        releaseCamera();
        super.release();
    }

    @Override
    public boolean isSwappedDimensions() {
        return mVideoQuality.isSwappedDimensions(mContext);
    }

    @Override
    protected int getDisplayRotation() {
        return mCamera2 == null ? Surface.ROTATION_0 : mCamera2.getDisplayRotation();
    }

    /**
     * 写真撮影を行います.
     *
     * @param l 撮影した写真を通知するリスナー
     */
    public void takePicture(ImageReader.OnImageAvailableListener l) {
        if (mCamera2 == null) {
            return;
        }
        mCamera2.takePicture(l);
    }

    /**
     * カメラの準備を行います.
     */
    private synchronized void startCamera() {
        if (mCamera2 != null) {
            throw new RuntimeException("Camera already initialized.");
        }

        int videoWidth = mVideoQuality.getVideoWidth();
        int videoHeight = mVideoQuality.getVideoHeight();

        boolean isSwapped = mVideoQuality.isSwappedDimensions(mContext);
        int w = isSwapped ? videoHeight : videoWidth;
        int h = isSwapped ? videoWidth : videoHeight;

        mBackgroundThread = new HandlerThread("Camera2");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        mImageReaderYuv = ImageReader.newInstance(w, h, ImageFormat.YUV_420_888, 2);
        mImageReaderYuv.setOnImageAvailableListener(mImageYuvListener, mBackgroundHandler);

        mSurfaces.add(mImageReaderYuv.getSurface());

        mCamera2 = Camera2WrapperManager.createCamera(mContext, mVideoQuality.getFacing());
        mCamera2.setCameraEventListener(new Camera2Wrapper.CameraEventListener() {
            @Override
            public void onOpen() {
                if (DEBUG) {
                    Log.d(TAG, "CameraVideoSurfaceEncoder::onOpen");
                }
                if (mCamera2 != null) {
                    mCamera2.startPreview();
                }
            }

            @Override
            public void onStartPreview() {
                if (DEBUG) {
                    Log.d(TAG, "CameraVideoSurfaceEncoder::onStartPreview");
                }
            }

            @Override
            public void onStopPreview() {
                if (DEBUG) {
                    Log.d(TAG, "CameraVideoSurfaceEncoder::onStopPreview");
                }
            }

            @Override
            public void onError(Camera2WrapperException e) {
                postOnError(new MediaEncoderException(e));
            }
        });
        mCamera2.getSettings().setRotation(mVideoQuality.getRotation());
        mCamera2.getSettings().setPreviewSize(new Size(videoWidth, videoHeight));
        mCamera2.open(new ArrayList<>(mSurfaces));
    }

    private ImageReader mImageReaderYuv;
    private ImageReader.OnImageAvailableListener mImageYuvListener = (reader) -> {
        try (Image image = reader.acquireNextImage()) {
            byte[] bytes = ImageConverter.convert(image);
            long timestamp = image.getTimestamp();
            writeInputBuffer(bytes, timestamp);
        } catch (IllegalStateException e) {
            Log.e(TAG, "", e);
        }
    };

    /**
     * カメラの解放を行います.
     */
    private synchronized void releaseCamera() {
        if (DEBUG) {
            Log.d(TAG, "releasing camera");
        }

        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            mBackgroundThread = null;
        }
        mBackgroundHandler = null;

        if (mImageReaderYuv != null) {
            mImageReaderYuv.close();
            mImageReaderYuv = null;
        }

        if (mCamera2 != null) {
            mCamera2.close();
            mCamera2 = null;
        }
    }
}
