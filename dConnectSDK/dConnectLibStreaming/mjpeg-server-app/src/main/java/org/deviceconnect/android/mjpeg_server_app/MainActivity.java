package org.deviceconnect.android.mjpeg_server_app;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.mjpeg.CameraMJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoderException;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;
import org.deviceconnect.android.libmedia.streaming.util.CameraSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * タグ.
     */
    private static final String TAG = "MJPEG-SERVER";

    /**
     * パーミッションのリクエストコード.
     */
    private static final int PERMISSION_REQUEST_CODE = 123456;

    /**
     * 使用するパーミッションのリスト.
     */
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    /**
     * 映像配信を行う設定.
     */
    private Settings mSettings;

    /**
     * MJPEG サーバ.
     */
    private MJPEGServer mMJPEGServer;

    /**
     * カメラのプレビューを表示する SurfaceView.
     */
    private SurfaceView mCameraView;

    /**
     * カメラを操作するためのクラス.
     */
    private Camera2Wrapper mCamera2;

    /**
     * カメラ描画クラス.
     */
    private CameraSurfaceDrawingThread mCameraSurfaceDrawingThread;

    /**
     * ハンドラ
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 画面の Surface.
     */
    private Surface mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.surface_view);
        mSettings = new Settings(getApplicationContext());
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
                    mSurface = surfaceHolder.getSurface();
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

        if (mMJPEGServer != null && mMJPEGServer.getMJPEGEncoder() != null) {
            boolean swap = mCameraSurfaceDrawingThread.isSwappedDimensions();
            int facing = mSettings.getCameraFacing();
            Size previewSize = mSettings.getCameraPreviewSize(facing);
            int videoWidth = swap ? previewSize.getHeight() : previewSize.getWidth();
            int videoHeight = swap ? previewSize.getWidth() : previewSize.getHeight();
            MJPEGQuality quality = mMJPEGServer.getMJPEGEncoder().getMJPEGQuality();
            quality.setWidth(videoWidth);
            quality.setHeight(videoHeight);
            mMJPEGServer.restartEncoder();
        }
        mHandler.postDelayed(() -> adjustSurfaceView(mCameraSurfaceDrawingThread.isSwappedDimensions()), 500);
    }

    private void gotoPreferences() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SettingsPreferenceActivity.class);
        startActivity(intent);
    }

    private void startStreaming() {
        String serverAddress = getIpAddress();
        if (serverAddress == null) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "WiFi ルーターに接続してください", Toast.LENGTH_LONG).show());
            return;
        }
        showServerAddress(serverAddress);

        if (mMJPEGServer != null) {
            if (DEBUG) {
                Log.w(TAG, "MJPEGServer is already started.");
            }
            return;
        }

        try {
            startCamera();

            mMJPEGServer = new MJPEGServer();
            mMJPEGServer.setServerName("MJPEG-SERVER");
            mMJPEGServer.setServerPort(12345);
            mMJPEGServer.setServerPath("mjpeg");
            mMJPEGServer.setCallback(new MJPEGServer.Callback() {
                @Override
                public boolean onAccept(Socket socket) {
                    return true;
                }

                @Override
                public void onClosed(Socket socket) {
                }

                @Override
                public MJPEGEncoder createMJPEGEncoder() throws MJPEGEncoderException {
                    boolean swap = mCameraSurfaceDrawingThread.isSwappedDimensions();
                    int facing = mSettings.getCameraFacing();
                    int fps = mSettings.getEncoderFrameRate();
                    int quality = mSettings.getEncoderQuality();
                    Size previewSize = mSettings.getCameraPreviewSize(facing);
                    int videoWidth = swap ? previewSize.getHeight() : previewSize.getWidth();
                    int videoHeight = swap ? previewSize.getWidth() : previewSize.getHeight();

                    CameraMJPEGEncoder encoder = new CameraMJPEGEncoder(mCameraSurfaceDrawingThread);
                    encoder.getMJPEGQuality().setFacing(facing);
                    encoder.getMJPEGQuality().setWidth(videoWidth);
                    encoder.getMJPEGQuality().setHeight(videoHeight);
                    encoder.getMJPEGQuality().setFrameRate(fps);
                    encoder.getMJPEGQuality().setQuality(quality);

                    runOnUiThread(() -> findViewById(R.id.text_view).setVisibility(View.VISIBLE));

                    return encoder;
                }

                @Override
                public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
                    runOnUiThread(() -> findViewById(R.id.text_view).setVisibility(View.GONE));
                }
            });
            mMJPEGServer.start();

            if (DEBUG) {
                Log.d(TAG, "Started MJPEG Server: " + serverAddress);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopStreaming() {
        if (mMJPEGServer != null) {
            mMJPEGServer.stop();
            mMJPEGServer = null;
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
        if (mCameraSurfaceDrawingThread != null) {
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
        mCameraSurfaceDrawingThread.addEGLSurfaceBase(mSurface);
        mCameraSurfaceDrawingThread.start();

        // SurfaceView のサイズを調整
        adjustSurfaceView(mCameraSurfaceDrawingThread.isSwappedDimensions());
    }

    private synchronized void stopCamera() {
        if (mCameraSurfaceDrawingThread != null) {
            mCameraSurfaceDrawingThread.stop();
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
