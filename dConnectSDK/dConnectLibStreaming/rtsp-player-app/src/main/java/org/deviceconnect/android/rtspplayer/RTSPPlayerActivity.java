package org.deviceconnect.android.rtspplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.libmedia.streaming.rtsp.player.RtspPlayer;

import androidx.appcompat.app.AppCompatActivity;

public class RTSPPlayerActivity extends AppCompatActivity {

    static final String EXTRA_URI = "_extra_uri";

    private RtspPlayer mRtspPlayer;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp_player);
    }

    @Override
    protected void onPause() {
        stopPlayer();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPlayer();
    }

    private String getExtraUri() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(EXTRA_URI);
    }

    private void startPlayer() {
        if (mRtspPlayer != null) {
            return;
        }

        if (mMediaPlayer != null) {
            return;
        }

        String uri = getExtraUri();
        if (uri == null) {
            finish();
        }

        SurfaceView surfaceView = findViewById(R.id.surface_view);

//        try {
//            mMediaPlayer = new MediaPlayer();
//            mMediaPlayer.setDataSource(url);
//            mMediaPlayer.setSurface(surfaceView.getHolder().getSurface());
//            mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
//            mMediaPlayer.setOnPreparedListener((MediaPlayer mp) -> mMediaPlayer.start());
//            mMediaPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        mRtspPlayer = new RtspPlayer(uri);
        mRtspPlayer.setSurface(surfaceView.getHolder().getSurface());
        mRtspPlayer.setOnEventListener(new RtspPlayer.OnEventListener() {
            @Override
            public void onError(Exception e) {
            }

            @Override
            public void onOpened() {
            }

            @Override
            public void onSizeChanged(int width, int height) {
                runOnUiThread(() -> changePlayerSize(width, height));
            }
        });
        mRtspPlayer.start();
    }

    private void changePlayerSize(int pictureWidth, int pictureHeight) {
        View root = findViewById(R.id.root);
        SurfaceView surfaceView = findViewById(R.id.surface_view);

        Size changeSize;
        Size viewSize = new Size(root.getWidth(), root.getHeight());
        changeSize = calculateViewSize(pictureWidth, pictureHeight, viewSize);

        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.width = changeSize.getWidth();
        layoutParams.height = changeSize.getHeight();
        surfaceView.setLayoutParams(layoutParams);
    }

    protected Size calculateViewSize(int width, int height, Size maxSize) {
        int h = maxSize.getWidth() * height / width;
        if (h % 2 != 0) {
            h--;
        }
        if (maxSize.getHeight() < h) {
            int w = maxSize.getHeight() * width / height;
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, maxSize.getHeight());
        }
        return new Size(maxSize.getWidth(), h);
    }

    private void stopPlayer() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        if (mRtspPlayer != null) {
            mRtspPlayer.stop();
            mRtspPlayer = null;
        }
    }
}
