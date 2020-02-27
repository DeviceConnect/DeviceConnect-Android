package org.deviceconnect.android.rtspserver.screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.DisplayMetrics;

import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ScreenCastVideoEncoder extends VideoEncoder {

    private Context mContext;

    private Handler mCallbackHandler = new Handler(Looper.getMainLooper());

    private MediaProjectionManager mMediaProjectionMgr;

    private MediaProjection mMediaProjection;

    private VideoQuality mVideoQuality;

    private ScreenCast mScreenCast;

    public ScreenCastVideoEncoder(Context context) {
        mContext = context;
        mVideoQuality = new VideoQuality("video/avc");

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//        mVideoQuality.setVideoWidth(metrics.widthPixels / 4);
//        mVideoQuality.setVideoHeight(metrics.heightPixels / 4);
        mVideoQuality.setVideoWidth(360);
        mVideoQuality.setVideoHeight(640);

        mMediaProjectionMgr = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    // VideoEncoder

    @Override
    public int getColorFormat() {
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    }

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        super.prepare();
        try {
            requestMediaProjection();
            mScreenCast = new SurfaceScreenCast(mContext, mMediaProjection,
                mMediaCodec.createInputSurface(), mVideoQuality);
            mScreenCast.startCast();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    @Override
    protected void release() {
        if (mScreenCast != null) {
            mScreenCast.stopCast();
            mScreenCast = null;
        }
        super.release();
    }

    private void clean() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void requestMediaProjection() throws IOException {
        CountDownLatch latch = new CountDownLatch(1);

        Intent intent = new Intent();
        intent.setClass(mContext, MediaProjectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callback", new ResultReceiver(mCallbackHandler) {
            @Override
            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    Intent data = resultData.getParcelable("result");
                    if (data != null) {
                        mMediaProjection = mMediaProjectionMgr.getMediaProjection(resultCode, data);
                        mMediaProjection.registerCallback(new MediaProjection.Callback() {
                            @Override
                            public void onStop() {
                                clean();
                            }
                        }, new Handler(Looper.getMainLooper()));
                    }
                }
                latch.countDown();
            }
        });
        mContext.startActivity(intent);

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException();
        }
    }
}
