package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.content.Context;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.gles.OffscreenSurface;

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
     * カメラの映像描画用の Surface のリスト.
     */
    private List<Surface> mSurfaces = new ArrayList<>();

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraMJPEGEncoder(Context context) {
        mContext = context;
    }

    @Override
    protected OffscreenSurface createOffscreenSurface() {
        MJPEGQuality quality = getMJPEGQuality();
        boolean isSwapped = Camera2WrapperManager.isSwappedDimensions(mContext, quality.getFacing());
        int w = isSwapped ? quality.getHeight() : quality.getWidth();
        int h = isSwapped ? quality.getWidth() : quality.getHeight();
        return new OffscreenSurface(w, h);
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
            }
        });
        mCamera2.getSettings().setPreviewSize(new Size(videoWidth, videoHeight));
        mCamera2.open(getSurfaceTexture(), new ArrayList<>(mSurfaces));
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
        return mCamera2 != null && mCamera2.isSwappedDimensions();
    }

    @Override
    protected int getDisplayRotation() {
        return mCamera2 == null ? Surface.ROTATION_0 : mCamera2.getDisplayRotation();
    }

    /**
     * カメラの映像を描画する Surface を追加します.
     *
     * @param surface 追加する Surface
     */
    public void addSurface(Surface surface) {
        mSurfaces.add(surface);
    }

    /**
     * カメラの映像を描画する Surface を削除します.
     *
     * <p>
     * 追加されていない Surface が指定された場合には何もしません。
     * </p>
     *
     * @param surface 削除する Surface
     */
    public void removeSurface(Surface surface) {
        mSurfaces.remove(surface);
    }
}
