/*
 VideoRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.video;

import java.io.File;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.provider.FileManager;

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
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.Log;
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
    private MediaRecorder mRecorder;

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_main);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);

        mFileMgr = new FileManager(this);

        Button stopBtn = (Button) findViewById(R.id.btn_stop);
        stopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
        registerReceiver(myReceiver, filter);

        mCamera = getCameraInstance();
        mRecorder = new MediaRecorder();
        try {
            mCamera.unlock();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        Intent intent = getIntent();
        if (intent != null) {
            mFileName = intent.getStringExtra(VideoConst.EXTRA_FILE_NAME);
        }
        if (mFileName != null) {
            mRecorder.setCamera(mCamera);
            mFile = new File(mFileMgr.getBasePath(), mFileName);
            try {
                mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
                mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mRecorder.setOutputFile(mFile.toString());
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseMediaRecorder();
        releaseCamera();

        if (mHolder != null) {
            mHolder = null;
        }

        // レシーバーを削除
        unregisterReceiver(myReceiver);

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
     * @return true is exist
     */
    private boolean checkVideoFile() {
        return mFile != null && mFile.exists() && mFile.length() > 0;
    }

    /**
     * MediaRecorderを解放.
     */
    private void releaseMediaRecorder() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    /**
     * Cameraのインスタンスを取得.
     * 
     * @return cameraのインスタンス
     */
    private synchronized Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return c;
    }

    private synchronized void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.lock();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        mHolder = holder;
        mRecorder.setPreviewDisplay(mHolder.getSurface());
        try {
            mRecorder.prepare();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        try {
            mRecorder.start();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
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
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
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
