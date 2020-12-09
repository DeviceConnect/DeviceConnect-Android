package org.deviceconnect.android.rtmp_broadcast_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Camera;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.rtmp.RtmpClient;
import org.deviceconnect.android.libmedia.streaming.util.CameraSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * タグ.
     */
    private static final String TAG = "RTMP-CLIENT";

    /**
     * パーミッションのリクエストコード.
     */
    private static final int PERMISSION_REQUEST_CODE = 123456;

    /**
     * 使用するパーミッションのリスト.
     */
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    /**
     * 映像配信を行う設定.
     */
    private Settings mSettings;

    /**
     * RTMP クライアント.
     */
    private RtmpClient mRtmpClient;

    /**
     * カメラのプレビューを表示する SurfaceView.
     */
    private SurfaceView mCameraView;

    /**
     * カメラを操作するためのクラス.
     */
    private Camera2Wrapper mCamera2;

    private CameraSurfaceDrawingThread mCameraSurfaceDrawingThread;

    /**
     * ハンドラ
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.surface_view);
        mSettings = new Settings(getApplicationContext());

        findViewById(R.id.power).setOnClickListener((v) -> toggleStreaming());
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
                    startCamera();
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
        stopCamera();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> denies = PermissionUtil.checkRequestPermissionsResult(permissions, grantResults);
            if (!denies.isEmpty()) {
                Toast.makeText(this, "Denied a camera permission.", Toast.LENGTH_SHORT).show();
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
            if (mRtmpClient != null && mRtmpClient.getVideoEncoder() != null) {
                mRtmpClient.restartVideoEncoder();
                CameraSurfaceVideoEncoder encoder = (CameraSurfaceVideoEncoder) mRtmpClient.getVideoEncoder();
                mHandler.postDelayed(() -> adjustSurfaceView(encoder.isSwappedDimensions()), 500);
            }
        }
    }

    private void gotoPreferences() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SettingsPreferenceActivity.class);
        startActivity(intent);
    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                default:
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_180:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
        } else {
            switch(rotation) {
                default:
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_180:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
        }
    }

    private void setDisplayRotation(boolean fixed) {
        if (fixed) {
            setRequestedOrientation(getScreenOrientation());
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private boolean mScreenRotationFixed = false;

    private void toggleScreenRotation() {
        mScreenRotationFixed = !mScreenRotationFixed;
        setDisplayRotation(mScreenRotationFixed);
    }

    private synchronized void toggleStreaming() {
        ColorStateList stateList;
        Resources res = getResources();
        if (mRtmpClient == null) {
            startStreaming();
            stateList = new ColorStateList(new int[][]{{}}, new int[]{res.getColor(R.color.colorPink)});
        } else {
            stopStreaming();
            startCamera();
            stateList = new ColorStateList(new int[][]{{}}, new int[]{res.getColor(R.color.colorGray)});
        }
        findViewById(R.id.power).setBackgroundTintList(stateList);
    }

    private void startStreaming() {
        String serverAddress = getIpAddress();
        if (serverAddress == null) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "WiFi ルーターに接続してください", Toast.LENGTH_LONG).show());
            return;
        }
        showServerAddress(serverAddress);

        if (mRtmpClient != null) {
            if (DEBUG) {
                Log.w(TAG, "RtmpClient is already started.");
            }
            return;
        }

        CameraSurfaceVideoEncoder videoEncoder = new CameraSurfaceVideoEncoder(this, mCameraSurfaceDrawingThread);

        CameraVideoQuality videoQuality = (CameraVideoQuality) videoEncoder.getVideoQuality();
        videoQuality.setFacing(mSettings.getCameraFacing());
        videoQuality.setVideoWidth(mSettings.getCameraPreviewSize(mSettings.getCameraFacing()).getWidth());
        videoQuality.setVideoHeight(mSettings.getCameraPreviewSize(mSettings.getCameraFacing()).getHeight());
        videoQuality.setIFrameInterval(mSettings.getEncoderIFrameInterval());
        videoQuality.setFrameRate(mSettings.getEncoderFrameRate());
        videoQuality.setBitRate(mSettings.getEncoderBitrate() * 1024);


        AudioEncoder audioEncoder = null;
        if (mSettings.isEnabledAudio()) {
            audioEncoder = new MicAACLATMEncoder();
            AudioQuality audioQuality = audioEncoder.getAudioQuality();
            audioQuality.setBitRate(mSettings.getAudioBitRate() * 1024);
            audioQuality.setSamplingRate(mSettings.getSamplingRate());
        }


        mRtmpClient = new RtmpClient(mSettings.getBroadcastURI());
        mRtmpClient.setVideoEncoder(videoEncoder);
        mRtmpClient.setAudioEncoder(audioEncoder);
        mRtmpClient.setOnEventListener(new RtmpClient.OnEventListener() {
            @Override
            public void onStarted() {
                Log.i(TAG, "RtmpClient::onStarted");
            }

            @Override
            public void onStopped() {
                Log.i(TAG, "RtmpClient::onStopped");
            }

            @Override
            public void onError(MediaEncoderException e) {
                Log.e(TAG, "RtmpClient::onError", e);
            }

            @Override
            public void onConnected() {
                Log.i(TAG, "RtmpClient::onConnected");
            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, "RtmpClient::onDisconnected");
            }

            @Override
            public void onNewBitrate(long bitrate) {
                Log.i(TAG, "RtmpClient::onNewBitrate: " + bitrate);
            }
        });
        mRtmpClient.start();
    }

    private void stopStreaming() {
        if (mRtmpClient != null) {
            mRtmpClient.stop();
            mRtmpClient = null;
        }
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
        mCamera2.getSettings().setPreviewSize(new Size(cameraWidth, cameraHeight));

        mCameraSurfaceDrawingThread = new CameraSurfaceDrawingThread(mCamera2);
        mCameraSurfaceDrawingThread.addOnDrawingEventListener(new EGLSurfaceDrawingThread.OnDrawingEventListener() {
            @Override
            public void onStarted() {
                EGLSurfaceBase surfaceBase = mCameraSurfaceDrawingThread.createEGLSurfaceBase(mCameraView.getHolder().getSurface());
                mCameraSurfaceDrawingThread.addEGLSurfaceBase(surfaceBase);
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onError(Exception e) {
            }

            @Override
            public void onDrawn(EGLSurfaceBase eglSurfaceBase) {
            }
        });
        mCameraSurfaceDrawingThread.start();

        // SurfaceView のサイズを調整
        adjustSurfaceView(mCamera2.isSwappedDimensions());
    }

    private synchronized void stopCamera() {
        if (mCameraSurfaceDrawingThread != null) {
            mCameraSurfaceDrawingThread.terminate();
            mCameraSurfaceDrawingThread = null;
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
