package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.content.Context;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;

import java.util.ArrayList;
import java.util.List;

public class CameraMJPEGEncoder extends SurfaceMJPEGEncoder {
    /**
     * カメラ.
     */
    private Camera2Wrapper mCamera2;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraMJPEGEncoder(Context context) {
        mContext = context;
    }

    @Override
    protected void prepare() {
        MJPEGQuality quality = getMJPEGQuality();

        int videoWidth = quality.getWidth();
        int videoHeight = quality.getHeight();

        mCamera2 = Camera2WrapperManager.createCamera(mContext, quality.getFacing());
        mCamera2.setCameraEventListener(new Camera2Wrapper.CameraEventListener() {
            @Override
            public void onOpen() {
                mCamera2.startPreview();
            }

            @Override
            public void onStartPreview() {
            }

            @Override
            public void onStopPreview() {
            }

            @Override
            public void onError(Camera2WrapperException e) {
                postOnError(new MJPEGEncoderException(e));
            }
        });
        mCamera2.getSettings().setPreviewSize(new Size(videoWidth, videoHeight));
        mCamera2.open(getSurfaceTexture());
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    @Override
    protected void release() {
        if (mCamera2 != null) {
            mCamera2.close();
            mCamera2 = null;
        }
    }

    @Override
    public boolean isSwappedDimensions() {
        return Camera2WrapperManager.isSwappedDimensions(mContext, getMJPEGQuality().getFacing());
    }

    @Override
    protected int getDisplayRotation() {
        return mCamera2 == null ? Surface.ROTATION_0 : mCamera2.getDisplayRotation();
    }
}
