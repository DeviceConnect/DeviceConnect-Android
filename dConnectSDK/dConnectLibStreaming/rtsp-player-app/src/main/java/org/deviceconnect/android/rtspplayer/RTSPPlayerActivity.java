package org.deviceconnect.android.rtspplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.deviceconnect.android.libmedia.streaming.rtsp.player.RtspPlayer;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RTSPPlayerActivity extends AppCompatActivity {

    static final String EXTRA_URI = "_extra_uri";

    private RtspPlayer mRtspPlayer;
    private RTSPSetting mSetting;
    private boolean mRunningFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp_player);

        mSetting = new RTSPSetting(getApplicationContext());

        int visibility = mSetting.isEnabledDebugLog() ? View.VISIBLE : View.GONE;
        findViewById(R.id.text_view_debug).setVisibility(visibility);
    }

    @Override
    protected void onPause() {
        mRunningFlag = false;
        stopPlayer();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunningFlag = true;
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

        String uri = getExtraUri();
        if (uri == null) {
            finish();
        }

        SurfaceView surfaceView = findViewById(R.id.surface_view);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        mRtspPlayer = new RtspPlayer(uri);
        mRtspPlayer.setSurface(surfaceView.getHolder().getSurface());
        mRtspPlayer.setOnEventListener(new RtspPlayer.OnEventListener() {
            @Override
            public void onConnected() {
                addDebugLog("サーバに接続しました。");
            }

            @Override
            public void onDisconnected() {
                addDebugLog("サーバから切断されました。");
            }

            @Override
            public void onReady() {
                addDebugLog("SDP を受信しました。");
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }

            @Override
            public void onSizeChanged(int width, int height) {
                addDebugLog("サイズ変更: " + width + "x" + height);
                runOnUiThread(() -> changePlayerSize(width, height));
            }

            @Override
            public void onError(Exception e) {
                Log.e("RTSP-PLAYER", "Error", e);
                showErrorDialog("エラー", e.getMessage());
            }
        });
        mRtspPlayer.start();
    }

    private void stopPlayer() {
        if (mRtspPlayer != null) {
            mRtspPlayer.stop();
            mRtspPlayer.setOnEventListener(null);
            mRtspPlayer = null;
        }
    }

    private final List<String> mDebugMessage = new ArrayList<>();

    private void addDebugLog(String message) {
        if (!mRunningFlag) {
            return;
        }

        synchronized (mDebugMessage) {
            mDebugMessage.add(message);

            if (mDebugMessage.size() > 5) {
                mDebugMessage.remove(0);
            }
        }

        runOnUiThread(() -> {
            if (!mRunningFlag) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            synchronized (mDebugMessage) {
                for (String s : mDebugMessage) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(s);
                }
            }

            TextView textView = findViewById(R.id.text_view_debug);
            textView.setText(sb.toString());
        });
    }

    private void showErrorDialog(String title, String message) {
        if (!mRunningFlag) {
            return;
        }
        runOnUiThread(() -> {
            if (mRunningFlag) {
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("はい", null)
                        .setCancelable(false)
                        .setOnDismissListener((dialogInterface) -> finish())
                        .show();
            }
        });
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
}
