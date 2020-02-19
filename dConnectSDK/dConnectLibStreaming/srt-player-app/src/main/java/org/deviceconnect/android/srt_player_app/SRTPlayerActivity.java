package org.deviceconnect.android.srt_player_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.libsrt.client.SRTPlayer;

import androidx.appcompat.app.AppCompatActivity;

public class SRTPlayerActivity extends AppCompatActivity {

    static final String EXTRA_URI = "_extra_uri";

    private SRTPlayer mSRTPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srt_player);
    }

    @Override
    protected void onPause() {
        stopSRTPlayer();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSRTPlayer();
    }

    private String getExtraUri() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(EXTRA_URI);
    }

    private void startSRTPlayer() {
        if (mSRTPlayer != null) {
            return;
        }

        String uri = getExtraUri();
        if (uri == null) {
            finish();
        }

        SurfaceView surfaceView = findViewById(R.id.surface_view);

        mSRTPlayer = new SRTPlayer();
        mSRTPlayer.setUri(uri);
        mSRTPlayer.setSurface(surfaceView.getHolder().getSurface());
        mSRTPlayer.setOnEventListener(new SRTPlayer.OnEventListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                runOnUiThread(() -> changePlayerSize(width, height));
            }

            @Override
            public void onError(Exception e) {

            }
        });
        mSRTPlayer.start();
    }

    private void stopSRTPlayer() {
        if (mSRTPlayer != null) {
            mSRTPlayer.stop();
            mSRTPlayer = null;
        }
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

    private Size calculateViewSize(int width, int height, Size maxSize) {
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
}