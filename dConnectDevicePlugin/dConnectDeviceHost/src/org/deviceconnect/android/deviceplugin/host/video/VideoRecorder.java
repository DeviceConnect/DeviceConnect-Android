/*
 VideoRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.video;

import java.io.File;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.provider.FileManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * Video Recorder.
 * 
 * @author NTT DOCOMO, INC.
 */
public class VideoRecorder extends Activity implements SurfaceHolder.Callback {

    /** MediaRecorder. */
    private MediaRecorder mMediaRecorder;

    /** SurfaceHolder. */
    private SurfaceHolder mHolder;

    /** Camera. */
    private Camera mCamera;

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** フォルダURI. */
    private File mFile;

    /** ファイル名. */
    private String mFileName;

    /** Ready flag. */
    private Boolean mIsReady = false;
    private boolean mIsInitialized = false;

    /** 開始インテント。 */
    private Intent mIntent;

    /** コールバック。 */
    private ResultReceiver mCallback;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_main);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);

        Button stopBtn = (Button) findViewById(R.id.btn_stop);
        stopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // レシーバーを登録
        IntentFilter filter = new IntentFilter();
        filter.addAction(VideoConst.SEND_HOSTDP_TO_VIDEO);
        registerReceiver(mMyReceiver, filter);

        mIntent = getIntent();
        if (mIntent == null) {
            finish();
            return;
        }
        mCallback = mIntent.getParcelableExtra(VideoConst.EXTRA_CALLBACK);
        if (mCallback == null) {
            finish();
            return;
        }

        if (!mIsInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermissionUtility.requestPermissions(this, new Handler(Looper.getMainLooper()),
                        new String[] { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                try {
                                    initVideoContext();
                                } catch (Exception e) {
                                    // e.printStackTrace();
                                    Bundle data = new Bundle();
                                    data.putString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE,
                                            e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                                    mCallback.send(Activity.RESULT_CANCELED, data);
                                    finish();
                                    return;
                                }
                                mCallback.send(Activity.RESULT_OK, null);
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                Bundle data = new Bundle();
                                data.putString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE,
                                        "Permission " + deniedPermission + " not granted.");
                                mCallback.send(Activity.RESULT_CANCELED, data);
                                finish();
                            }
                        });
            } else {
                try {
                    initVideoContext();
                } catch (Exception e) {
                    // e.printStackTrace();
                    Bundle data = new Bundle();
                    data.putString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE,
                            e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                    mCallback.send(Activity.RESULT_CANCELED, data);
                    finish();
                    return;
                }
            }
        }
    }

    private void initVideoContext() {
        mFileMgr = new FileManager(this);

        mMediaRecorder = new MediaRecorder();
        mCamera = getCameraInstance();
        mCamera.unlock();

        mFileName = mIntent.getStringExtra(VideoConst.EXTRA_FILE_NAME);
        if (mFileName != null) {
            mMediaRecorder.setCamera(mCamera);
            mFile = new File(mFileMgr.getBasePath(), mFileName);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setOutputFile(mFile.toString());
        } else {
            Bundle data = new Bundle();
            data.putString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE, "File name must be specified.");
            mCallback.send(Activity.RESULT_CANCELED, data);
            finish();
            return;
        }

        mIsInitialized = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseMediaRecorder();
        releaseCamera();
        mIsInitialized = false;

        if (mHolder != null) {
            mHolder = null;
        }

        // レシーバーを削除
        unregisterReceiver(mMyReceiver);

        if (checkVideoFile()) {
            // Content Providerに登録する.
            MediaMetadataRetriever mediaMeta = new MediaMetadataRetriever();
            mediaMeta.setDataSource(mFile.toString());
            ContentResolver resolver = getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Video.Media.TITLE, mFileName);
            values.put(Video.Media.DISPLAY_NAME, mFileName);
            values.put(Video.Media.ARTIST, "DeviceConnect");
            values.put(Video.Media.MIME_TYPE, VideoConst.FORMAT_TYPE);
            values.put(Video.Media.DATA, mFile.toString());
            resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    /**
     * Check the existence of file.
     * 
     * @return true is exist
     */
    private boolean checkVideoFile() {
        return mFile != null && mFile.exists() && mFile.length() > 0;
    }

    /**
     * MediaRecorderを解放.
     */
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            if (mIsReady) {
                mMediaRecorder.stop();
            }
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mIsReady = false;
        }
    }

    /**
     * Cameraのインスタンスを取得.
     * 
     * @return cameraのインスタンス
     */
    private synchronized Camera getCameraInstance() {
        return Camera.open();
    }

    /**
     * Release camera.
     */
    private synchronized void releaseCamera() {
        if (mCamera != null) {
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        if (mIsInitialized) {
            try {
                mHolder = holder;

                if (mIsReady) {
                    mMediaRecorder.setPreviewDisplay(null);
                }

                mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
                mMediaRecorder.prepare();
                mMediaRecorder.start();
                mIsReady = true;
            } catch (Throwable throwable) {
                if (BuildConfig.DEBUG) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            finish();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 受信用のReceiver.
     */
    private BroadcastReceiver mMyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(VideoConst.SEND_HOSTDP_TO_VIDEO)) {
                String videoAction = intent.getStringExtra(VideoConst.EXTRA_NAME);
                if (videoAction.equals(VideoConst.EXTRA_VALUE_VIDEO_RECORD_STOP)) {
                    finish();
                }
            }
        }
    };
}
