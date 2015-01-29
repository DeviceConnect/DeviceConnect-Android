/*
 CameraActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.IHostMediaStreamRecordingService;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.provider.FileManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Device Connect連携カメラアプリ.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CameraActivity extends Activity implements Camera.PreviewCallback {

    /** ハンドラー. */
    private Handler mHandler;

    /** プレビュー画面. */
    private Preview mPreview;

    /** 使用するカメラのインスタンス. */
    private Camera mCamera;

    /** カメラの個数. */
    private int numberOfCameras;

    /** カメラの固定. */
    private int cameraCurrentlyLocked;

    /** デフォルトのカメラID. */
    private int defaultCameraId;

    /** プロセス間通信でつなぐService. */
    private IHostMediaStreamRecordingService mService;

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
            "yyyyMMdd_kkmmss", Locale.JAPAN);

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_camera_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".png";

    /**
     * 写真撮影後に終了するか確認するためのフラグ.
     * <ul>
     * <li>true: 撮影後にActivityを終了する
     * <li>false: 撮影後もActivityを続ける
     * </ul>
     */
    private boolean mFinishFlag;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mHandler = new Handler();
        mFileMgr = new FileManager(this);
        
        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        setContentView(R.layout.preview_main);

        mPreview = (Preview) findViewById(R.id.preview);

        Button stopBtn = (Button) findViewById(R.id.btn_stop);
        stopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCloseApplication();
            }
        });

        ImageButton takeBtn = (ImageButton) findViewById(R.id.btn_take_photo);
        takeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureRunnable(null);
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (CameraConst.SEND_HOSTDP_TO_CAMERA.equals(action)) {
                String name = intent.getStringExtra(CameraConst.EXTRA_NAME);
                if (CameraConst.EXTRA_NAME_SHUTTER.equals(name)) {
                    mFinishFlag = true;
                    takeBtn.setVisibility(View.GONE);
                    stopBtn.setVisibility(View.GONE);
                }
            }
        }
        // Find the total number of cameras available
        numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }

        bindService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = Camera.open();
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
        mCamera.setPreviewCallback(this);

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (CameraConst.SEND_HOSTDP_TO_CAMERA.equals(action)) {
                String name = intent.getStringExtra(CameraConst.EXTRA_NAME);
                if (CameraConst.EXTRA_NAME_SHUTTER.equals(name)) {
                    String requestid = intent.getStringExtra(CameraConst.EXTRA_REQUESTID);
                    takePictureRunnable(requestid);
                }
            }
        }

        // BroadcastReceiver登録 
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CameraConst.SEND_HOSTDP_TO_CAMERA);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.stopPreview(); 
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        mService = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate our menu which can gather user input for switching camera
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.switch_cam:
            // check for availability of multiple cameras
            if (numberOfCameras == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(this.getString(R.string.camera_alert)).setNeutralButton("Close", null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }

            // OK, we have multiple cameras.
            // Release this camera -> cameraCurrentlyLocked
            if (mCamera != null) {
                mPreview.setCamera(null);
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }

            // Acquire the next camera and request Preview to reconfigure
            // parameters.
            mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
            cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras;
            mPreview.switchCamera(mCamera);
            // Start the preview
            mCamera.startPreview();
            return true;
        case R.id.item_shutter:
            takePictureRunnable(null);
            return true;
        case R.id.item_zoom_in:
            zoomInRunnable(null);
            return true;
        case R.id.item_zoom_out:
            zoomOutRunnable(null);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * BroadcastReceiverをインナークラスで定義.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (CameraConst.SEND_HOSTDP_TO_CAMERA.equals(action)) {
                String name = intent.getStringExtra(CameraConst.EXTRA_NAME);
                if (CameraConst.EXTRA_NAME_SHUTTER.equals(name)) {
                    // シャッター操作依頼通知を受信
                    String requestid = intent.getStringExtra(CameraConst.EXTRA_REQUESTID);
                    takePictureRunnable(requestid);
                } else if (CameraConst.EXTRA_NAME_ZOOMIN.equals(name)) {
                    // ズームイン操作依頼通知を受信
                    String requestid = intent.getStringExtra(CameraConst.EXTRA_REQUESTID);
                    zoomInRunnable(requestid);
                } else if (CameraConst.EXTRA_NAME_ZOOMOUT.equals(name)) {
                    // ズームアウト操作依頼通知を受信
                    String requestid = intent.getStringExtra(CameraConst.EXTRA_REQUESTID);
                    zoomOutRunnable(requestid);
                } else if (CameraConst.EXTRA_NAME_FINISH.equals(name)) {
                    checkCloseApplication();
                }
            }
        }
    };

    /**
     * 写真撮影用Runnable実行する.
     * 
     * @param requestid リクエストID
     */
    private void takePictureRunnable(final String requestid) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPreview.takePicture(new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        String fileName = createNewFileName();
                        String pictureUri = null;
                        try {
                            pictureUri = mFileMgr.saveFile(fileName, data);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        // リクエストIDが登録されていたら、撮影完了後にホストデバイスプラグインへ撮影完了通知を送信する
                        if (requestid != null) {
                            Context context = CameraActivity.this;
                            Intent intent = new Intent(CameraConst.SEND_CAMERA_TO_HOSTDP);
                            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_SHUTTER);
                            intent.putExtra(CameraConst.EXTRA_REQUESTID, requestid);
                            intent.putExtra(CameraConst.EXTRA_PICTURE_URI, pictureUri);
                            context.sendBroadcast(intent);
                        }

                        if (mFinishFlag) {
                            checkCloseApplication();
                        } else {
                            mCamera.startPreview();
                        }
                    }
                });
            }
        }, 2000);
    }

    /**
     * ズームイン用Runnable実行.
     * 
     * @param requestid リクエストID
     */
    private void zoomInRunnable(final String requestid) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPreview.zoomIn(requestid);
            }
        });
    }

    /**
     * ズームアウト用Runnable実行.
     * 
     * @param requestid リクエストID
     */
    private void zoomOutRunnable(final String requestid) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPreview.zoomOut(requestid);
            }
        });
    }

    /**
     * HostDeviceServiceとデータをやり取りするためのAIDL.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mService = IHostMediaStreamRecordingService.Stub.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            unbindService(mServiceConnection);
            mService = null;
        }
    };

    /**
     * サービスをバインドする.
     */
    private void bindService() {
        Intent mIntent = new Intent(this, HostDeviceService.class);
        mIntent.setAction("camera");
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * アプリケーションクローズ時処理.
     */
    private void checkCloseApplication() {
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (CameraConst.SEND_HOSTDP_TO_CAMERA.equals(action)) {
                finish();
            }
        }
    }

    /**
     * 新規のファイル名を作成する.
     * @return ファイル名
     */
    private String createNewFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        mCamera.setPreviewCallback(null);

        if (mService != null) {
            int format = mCamera.getParameters().getPreviewFormat();
            int width = mCamera.getParameters().getPreviewSize().width;
            int height = mCamera.getParameters().getPreviewSize().height;
    
            YuvImage yuvimage = new YuvImage(data, format, width, height, null);
            Rect rect = new Rect(0, 0, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(rect, 50, baos);
            byte[] jdata = baos.toByteArray();
            try {
                mService.sendPreviewData(jdata, format, width, height);
            } catch (RemoteException e) {
                unbindService(mServiceConnection);
                mService = null;
                bindService();
            }
        }

        mCamera.setPreviewCallback(this);
    }
}
