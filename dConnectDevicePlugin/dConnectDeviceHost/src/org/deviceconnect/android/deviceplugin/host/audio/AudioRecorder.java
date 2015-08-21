/*
 AudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.audio;

import java.io.File;
import java.io.IOException;

import org.deviceconnect.android.activity.PermissionRequestActivity;
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
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.view.MotionEvent;
import android.view.Window;

/**
 * AudioRecorder.
 * 
 * @author NTT DOCOMO, INC.
 */
public class AudioRecorder extends Activity {

    /** MediaRecoder. */
    private MediaRecorder mMediaRecorder;
    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** ファイル名. */
    private String mFileName;

    /** フォルダURI. */
    private File mFile;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audio_main);

        mFileMgr = new FileManager(this);

        mMediaRecorder = new MediaRecorder();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String[] requiredPermissions = new String[] { Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE };
            PermissionRequestActivity.requestPermissions(this, requiredPermissions, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                    String[] permissions = resultData.getStringArray(PermissionRequestActivity.EXTRA_PERMISSIONS);
                    int[] grantResults = resultData.getIntArray(PermissionRequestActivity.EXTRA_GRANT_RESULTS);

                    if (permissions == null || permissions.length != requiredPermissions.length || grantResults == null
                            || grantResults.length != requiredPermissions.length) {
                        finish();
                        return;
                    }

                    int count = requiredPermissions.length;
                    for (int i = 0; i < permissions.length; ++i) {
                        if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)
                                || permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                --count;
                            } else {
                                finish();
                                return;
                            }
                        }
                    }

                    if (count == 0) {
                        try {
                            initAudioContext();
                        } catch (Exception e) {
                            e.printStackTrace();
                            finish();
                            return;
                        }
                    }
                }
            });
        } else {
            try {
                initAudioContext();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
        }
    }

    private void initAudioContext() throws IOException {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        Intent intent = getIntent();
        if (intent != null) {
            mFileName = intent.getStringExtra(AudioConst.EXTRA_FINE_NAME);
        } else {
            finish();
            return;
        }
        if (mFileName != null) {
            mFile = new File(mFileMgr.getBasePath(), mFileName);
            mMediaRecorder.setOutputFile(mFile.toString());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } else {
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 受信を開始
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioConst.SEND_HOSTDP_TO_AUDIO);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 受信を停止.
        unregisterReceiver(mReceiver);

        if (checkAudioFile()) {
            // Contents Providerに登録.
            ContentResolver resolver = this.getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Video.Media.TITLE, mFileName);
            values.put(Video.Media.DISPLAY_NAME, mFileName);
            values.put(Video.Media.ARTIST, "DeviceConnect");
            values.put(Video.Media.MIME_TYPE, AudioConst.FORMAT_TYPE);
            values.put(Video.Media.DATA, mFile.toString());
            resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    /**
     * Check the existence of file.
     * 
     * @return true is exist
     */
    private boolean checkAudioFile() {
        return mFile != null && mFile.exists() && mFile.length() > 0;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            finish();
        }
        return true;
    }

    /**
     * 受信用Receiver.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals((AudioConst.SEND_HOSTDP_TO_AUDIO))) {
                String videoAction = intent.getStringExtra(AudioConst.EXTRA_NAME);
                if (videoAction.equals(AudioConst.EXTRA_NAME_AUDIO_RECORD_STOP)) {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                    finish();
                }
            }
        }
    };
}
