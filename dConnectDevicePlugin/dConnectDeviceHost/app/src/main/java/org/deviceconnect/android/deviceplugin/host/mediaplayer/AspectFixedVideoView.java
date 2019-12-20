package org.deviceconnect.android.deviceplugin.host.mediaplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

public class AspectFixedVideoView extends VideoView {

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoRotation;

    public AspectFixedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectFixedVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AspectFixedVideoView(Context context) {
        super(context);
    }


    @Override
    public void setVideoURI(Uri uri) {
        if (BuildConfig.DEBUG) {
            Log.d("AspectFixedVideoView", "setVideoURI: uri = " + uri);
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this.getContext(), uri);
        // 音声ファイルの場合はビデオサイズを取得しない
        if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) != null
                && retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) != null) {
            int w = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int h = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            mVideoRotation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            if (mVideoRotation == 90 || mVideoRotation == 270) {
                mVideoWidth = h;
                mVideoHeight = w;
            } else {
                mVideoWidth = w;
                mVideoHeight = h;
            }
            if (BuildConfig.DEBUG) {
                Log.d("AspectFixedVideoView", "setVideoURI: " + mVideoWidth + "x" + mVideoHeight + " " + mVideoRotation);
            }
        }
        super.setVideoURI(uri);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (BuildConfig.DEBUG) {
            Log.d("AspectFixedVideoView", "onMeasure: widthMeasureSpec=" + widthMeasureSpec + " heightMeasureSpec=" + heightMeasureSpec);
        }

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                width = height * mVideoWidth / mVideoHeight;
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d("AspectFixedVideoView", "onMeasure: width=" + width + " height=" + height);
        }
        setMeasuredDimension(width, height);
    }
}