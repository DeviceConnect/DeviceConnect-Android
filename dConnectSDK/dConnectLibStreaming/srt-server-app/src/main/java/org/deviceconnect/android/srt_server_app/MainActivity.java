package org.deviceconnect.android.srt_server_app;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTStats;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SRTサーバからAndroid端末のカメラ映像を配信する画面.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * タグ.
     */
    private static final String TAG = "SRTServer";

    /**
     * パーミッションのリクエストコード.
     */
    private static final int PERMISSION_REQUEST_CODE = 123456;

    /**
     * 使用するパーミッションのリスト.
     */
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    /**
     * 映像配信を行う設定.
     */
    private Settings mSettings;

    /**
     * SRT で映像を配信するサーバ.
     */
    private SRTServer mSRTServer;

    /**
     * カメラのプレビューを表示する SurfaceView.
     */
    private SurfaceView mCameraView;

    /**
     * カメラを操作するためのクラス.
     */
    private Camera2Wrapper mCamera2;

    /**
     * ハンドラ
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.surface_view);
        mSettings = new Settings(getApplicationContext());
        SRT.startup();
    }

    @Override
    protected void onDestroy() {
        SRT.cleanup();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<String> denies = PermissionUtil.checkPermissions(this, PERMISSIONS);
        if (!denies.isEmpty()) {
            PermissionUtil.requestPermissions(this, denies, PERMISSION_REQUEST_CODE);
        } else {
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    startStreaming();
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    stopCamera();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        stopStreaming();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> denies = PermissionUtil.checkRequestPermissionsResult(permissions, grantResults);
            if (!denies.isEmpty()) {
                Toast.makeText(this, getString(R.string.app_name) + "にはカメラの許可が必要です", Toast.LENGTH_LONG).show();
                finish();
            } else {
                startStreaming();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            gotoPreferences();
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mCamera2 != null) {
            mHandler.postDelayed(() -> adjustSurfaceView(mCamera2.isSwappedDimensions()), 500);
        } else {
            if (mSRTServer != null && mSRTServer.getSRTSession() != null) {
                SRTSession srtSession = mSRTServer.getSRTSession();
                srtSession.restartVideoEncoder();
                CameraSurfaceVideoEncoder videoEncoder = (CameraSurfaceVideoEncoder) srtSession.getVideoEncoder();
                if (videoEncoder != null) {
                    mHandler.postDelayed(() -> adjustSurfaceView(videoEncoder.isSwappedDimensions()), 500);
                }
            }
        }
    }

    private void gotoPreferences() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SettingsPreferenceActivity.class);
        startActivity(intent);
    }

    private void setVideoQuality(CameraVideoQuality videoQuality) {
        int facing = mSettings.getCameraFacing();
        int fps = mSettings.getEncoderFrameRate();
        int biteRate = mSettings.getEncoderBitRate();
        Size previewSize = mSettings.getCameraPreviewSize(facing);

        videoQuality.setFacing(facing);
        videoQuality.setBitRate(biteRate);
        videoQuality.setFrameRate(fps);
        videoQuality.setVideoWidth(previewSize.getWidth());
        videoQuality.setVideoHeight(previewSize.getHeight());
    }

    private void startStreaming() {
        String serverAddress = getIpAddress();
        if (serverAddress == null) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "WiFi ルーターに接続してください", Toast.LENGTH_LONG).show());
            return;
        }
        showServerAddress(serverAddress);

        if (mSRTServer != null) {
            if (DEBUG) {
                Log.w(TAG, "SRTServer is already started.");
            }
            return;
        }

        try {
            startCamera();

            Map<Integer, Object> socketOptions = new HashMap<>();
            socketOptions.put(SRT.SRTO_PEERLATENCY, mSettings.getPeerLatency());
            socketOptions.put(SRT.SRTO_LOSSMAXTTL, mSettings.getLossMaxTTL());
            socketOptions.put(SRT.SRTO_CONNTIMEO, mSettings.getConnTimeo());
            socketOptions.put(SRT.SRTO_PEERIDLETIMEO, mSettings.getPeerIdleTimeo());

            mSRTServer = new SRTServer(12345);
            mSRTServer.setSocketOptions(socketOptions);
            if (DEBUG) {
                mSRTServer.setStatsListener((SRTSocket client, SRTStats stats)
                        -> Log.d(TAG, "stats: " + stats));
            }
            mSRTServer.setCallback(new SRTServer.Callback() {
                @Override
                public void createSession(SRTSession session) {
                    if (DEBUG) {
                        Log.d(TAG, "Create a SRTSession.");
                    }

                    stopCamera();

                    String mimeType = mSettings.getEncoderName();

                    CameraSurfaceVideoEncoder videoEncoder = new CameraSurfaceVideoEncoder(getApplicationContext(), mimeType);
                    videoEncoder.addSurface(mCameraView.getHolder().getSurface());

                    setVideoQuality((CameraVideoQuality) videoEncoder.getVideoQuality());

                    session.setVideoEncoder(videoEncoder);

                    if (mSettings.isAudioEnabled()) {
                        MicAACLATMEncoder audioEncoder = new MicAACLATMEncoder();
                        audioEncoder.setMute(false);

                        AudioQuality audioQuality = audioEncoder.getAudioQuality();
                        audioQuality.setBitRate(mSettings.getAudioBitRate());
                        audioQuality.setSamplingRate(mSettings.getSamplingRate());
                        audioQuality.setChannel(AudioFormat.CHANNEL_IN_MONO);
                        audioQuality.setFormat(AudioFormat.ENCODING_PCM_16BIT);

                        session.setAudioEncoder(audioEncoder);
                    }

                    runOnUiThread(() -> findViewById(R.id.text_view).setVisibility(View.VISIBLE));
                }

                @Override
                public void releaseSession(SRTSession session) {
                    if (DEBUG) {
                        Log.d(TAG, "Release a SRTSession.");
                    }

                    runOnUiThread(() -> findViewById(R.id.text_view).setVisibility(View.GONE));

                    mHandler.postDelayed(() -> startCamera(), 500);
                }
            });
            mSRTServer.start();

            if (DEBUG) {
                Log.d(TAG, "Started SRT Server: " + serverAddress);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopStreaming() {
        if (mSRTServer != null) {
            mSRTServer.stop();
            mSRTServer = null;
        }
        stopCamera();
    }

    private void showServerAddress(final String address) {
        runOnUiThread(() -> {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.app_name) + " (" + address + ")");
            }
        });
    }

    private String getIpAddress() {
        IpAddressManager addressManager = new IpAddressManager();
        addressManager.storeIPAddress();
        InetAddress address = addressManager.getIPv4Address();
        if (address == null) {
            address = addressManager.getWifiIPv4Address();
        }
        if (address == null) {
            address = addressManager.getVpnIPv4Address();
        }
        if (address == null) {
            address = addressManager.getBluetoothIPv4Address();
        }
        if (address != null) {
            return address.getHostAddress();
        }
        return null;
    }

    private synchronized void startCamera() {
        if (mCamera2 != null) {
            if (DEBUG) {
                Log.w(TAG, "Camera is already opened.");
            }
            return;
        }

        int facing = mSettings.getCameraFacing();
        Size previewSize = mSettings.getCameraPreviewSize(facing);
        int cameraWidth = previewSize.getWidth();
        int cameraHeight = previewSize.getHeight();

        mCamera2 = Camera2WrapperManager.createCamera(getApplicationContext(), facing);
        mCamera2.setCameraEventListener(new Camera2Wrapper.CameraEventListener() {
            @Override
            public void onOpen() {
                if (DEBUG) {
                    Log.d(TAG, "MainActivity::onOpen");
                }
                if (mCamera2 != null) {
                    mCamera2.startPreview();
                }
            }

            @Override
            public void onStartPreview() {
                if (DEBUG) {
                    Log.d(TAG, "MainActivity::onStartPreview");
                }
            }

            @Override
            public void onStopPreview() {
                if (DEBUG) {
                    Log.d(TAG, "MainActivity::onStopPreview");
                }
            }

            @Override
            public void onError(Camera2WrapperException e) {
                if (DEBUG) {
                    Log.d(TAG, "MainActivity::onError", e);
                }
            }
        });
        mCamera2.getSettings().setPreviewSize(new Size(cameraWidth, cameraHeight));
        mCamera2.open(Collections.singletonList(mCameraView.getHolder().getSurface()));

        // SurfaceView のサイズを調整
        adjustSurfaceView(mCamera2.isSwappedDimensions());
    }

    private synchronized void stopCamera() {
        if (mCamera2 != null) {
            mCamera2.close();
            mCamera2 = null;
        }
    }

    private void adjustSurfaceView(boolean isSwappedDimensions) {
        runOnUiThread(() -> {
            int facing = mSettings.getCameraFacing();
            Size previewSize = mSettings.getCameraPreviewSize(facing);
            int cameraWidth = previewSize.getWidth();
            int cameraHeight = previewSize.getHeight();

            SurfaceView surfaceView = findViewById(R.id.surface_view);
            View root = findViewById(R.id.root);

            Size changeSize;
            Size viewSize = new Size(root.getWidth(), root.getHeight());
            if (isSwappedDimensions) {
                changeSize = calculateViewSize(cameraHeight, cameraWidth, viewSize);
            } else {
                changeSize = calculateViewSize(cameraWidth, cameraHeight, viewSize);
            }

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = changeSize.getWidth();
            layoutParams.height = changeSize.getHeight();
            surfaceView.setLayoutParams(layoutParams);

            surfaceView.getHolder().setFixedSize(previewSize.getWidth(), previewSize.getHeight());
        });
    }

    private Size calculateViewSize(int width, int height, Size viewSize) {
        int h =  (int) (height * (viewSize.getWidth() / (float) width));
        if (viewSize.getHeight() < h) {
            int w = (int) (width * (viewSize.getHeight() / (float) height));
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, viewSize.getHeight());
        }
        return new Size(viewSize.getWidth(), h);
    }
}
