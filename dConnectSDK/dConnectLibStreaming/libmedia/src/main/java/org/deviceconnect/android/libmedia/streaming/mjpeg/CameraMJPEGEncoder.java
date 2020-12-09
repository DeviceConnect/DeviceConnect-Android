package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.content.Context;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.util.CameraSurfaceDrawingThread;

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

    public CameraMJPEGEncoder(Context context, CameraSurfaceDrawingThread thread) {
        super(thread);
        mContext = context;
    }

    @Override
    protected void prepare() {
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    @Override
    protected void release() {
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
