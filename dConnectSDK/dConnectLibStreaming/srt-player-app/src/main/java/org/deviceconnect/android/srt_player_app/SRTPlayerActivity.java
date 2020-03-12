package org.deviceconnect.android.srt_player_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.libsrt.SRTStats;
import org.deviceconnect.android.libsrt.client.SRTPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static org.deviceconnect.android.srt_player_app.BuildConfig.DEBUG;

public class SRTPlayerActivity extends AppCompatActivity {

    static final String EXTRA_URI = "_extra_uri";

    private SRTPlayer mSRTPlayer;
    private SRTSetting mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srt_player);

        mSetting = new SRTSetting(getApplicationContext());

        int visibility = mSetting.isEnabledDebugLog() ? View.VISIBLE : View.GONE;
        findViewById(R.id.text_view_debug).setVisibility(visibility);
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

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        SurfaceView surfaceView = findViewById(R.id.surface_view);

        Map<Integer, Object> socketOptions = new HashMap<>();
        socketOptions.put(SRT.SRTO_RCVLATENCY, mSetting.getRcvLatency());
        socketOptions.put(SRT.SRTO_CONNTIMEO, mSetting.getConnTimeo());
        socketOptions.put(SRT.SRTO_PEERIDLETIMEO, mSetting.getPeerIdleTimeo());

        mSRTPlayer = new SRTPlayer();
        mSRTPlayer.setUri(uri);
        mSRTPlayer.setSocketOptions(socketOptions);
        mSRTPlayer.setSurface(surfaceView.getHolder().getSurface());
        mSRTPlayer.setStatsInterval(BuildConfig.STATS_INTERVAL);
        mSRTPlayer.setShowStats(DEBUG);
        mSRTPlayer.setOnEventListener(new SRTPlayer.OnEventListener() {
            @Override
            public void onConnected() {
                addDebugLog("サーバに接続しました。");
            }

            @Override
            public void onDisconnected() {
                addDebugLog("サーバから切断しました。");
            }

            @Override
            public void onReady() {
                addDebugLog("PMT を受信しました。");
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }

            @Override
            public void onSizeChanged(int width, int height) {
                addDebugLog("サイズ変更: " + width + "x" + height);
                runOnUiThread(() -> changePlayerSize(width, height));
            }

            @Override
            public void onError(Exception e) {
                Log.e("SRT-PLAYER", "Error", e);
                showErrorDialog("エラー", e.getMessage());
            }

            @Override
            public void onStats(SRTStats stats) {
                addDebugLog(stats.toString());
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

    private final List<String> mDebugMessage = new ArrayList<>();

    private void addDebugLog(String message) {
        synchronized (mDebugMessage) {
            mDebugMessage.add(message);

            if (mDebugMessage.size() > 5) {
                mDebugMessage.remove(0);
            }
        }
        runOnUiThread(() -> {
            TextView textView = findViewById(R.id.text_view_debug);

            StringBuilder sb = new StringBuilder();
            synchronized (mDebugMessage) {
                for (String s : mDebugMessage) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(s);
                }
            }

            textView.setText(sb.toString());
        });
    }

    private void showErrorDialog(String title, String message) {
        runOnUiThread(() ->
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("はい", null)
                    .setCancelable(false)
                    .setOnDismissListener((dialogInterface) -> finish())
                    .show());
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