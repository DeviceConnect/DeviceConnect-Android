package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.SurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public class DummyVideoEncoder extends SurfaceVideoEncoder {
    /**
     * 映像のエンコード設定.
     */
    private CameraVideoQuality mVideoQuality;
    Surface mSurface;
    private Handler mImageFileWriteHandler = new Handler(Looper.getMainLooper());

    public DummyVideoEncoder() {
        mVideoQuality = new CameraVideoQuality("video/avc");
    }

    @Override
    protected int getDisplayRotation() {
        return 0;
    }

    @Override
    public boolean isSwappedDimensions() {
        return false;
    }

    @Override
    protected void onStartSurfaceDrawing() {
        mImageFileWriteHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // SurfaceTextureが取得できない場合は終了とする
                                            if (getSurfaceTexture() == null) {
                                                return;
                                            }

                                            if (mSurface == null) {
                                                mSurface = new Surface(getSurfaceTexture());
                                                mImageFileWriteHandler.postDelayed(this, 100);
                                                return;
                                            }
                                            try {
                                                Canvas canvas = mSurface.lockCanvas(null);
                                                canvas.drawColor(Color.BLACK);
                                                mSurface.unlockCanvasAndPost(canvas);
                                            } catch (Exception e) {
                                                // ignore
                                            }
                                            mImageFileWriteHandler.postDelayed(this, 100);
                                        }
                                    });
    }

    @Override
    protected void onStopSurfaceDrawing() {
        mSurface = null;
    }

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }
}
