package org.deviceconnect.android.libmedia.streaming.rtsp.session.video;

import android.content.Context;
import android.view.Surface;

import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoSurfaceEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class CameraVideoStream extends H264VideoStream {
    /**
     * 映像用エンコーダ.
     */
    private VideoEncoder mVideoEncoder;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public CameraVideoStream(Context context) {
        mContext = context;
        mVideoEncoder = new CameraVideoSurfaceEncoder(context);
    }

    @Override
    protected boolean isSwappedDimensions() {
        return ((CameraVideoQuality) mVideoEncoder.getVideoQuality()).isSwappedDimensions(mContext);
    }

    /**
     * カメラの映像を描画する Surface を追加します.
     *
     * @param surface Surface
     */
    public void addSurface(Surface surface) {
        ((CameraVideoSurfaceEncoder) mVideoEncoder).addSurface(surface);
    }

    /**
     * カメラの映像を描画する Surface を削除します.
     *
     * @param surface Surface
     */
    public void removeSurface(Surface surface) {
        ((CameraVideoSurfaceEncoder) mVideoEncoder).removeSurface(surface);
    }

    @Override
    public VideoEncoder getVideoEncoder() {
        return mVideoEncoder;
    }
}
