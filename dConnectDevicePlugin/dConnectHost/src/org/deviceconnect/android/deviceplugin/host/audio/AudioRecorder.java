/*
 AudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.audio;

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
import android.media.MediaRecorder;
import android.os.Bundle;
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
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        Intent intent = getIntent();
        if (intent != null) {
            mFileName = intent.getStringExtra(AudioConst.EXTRA_FINE_NAME);
        }
        if (mFileName == null) {
            finish();
        } else {
            mFile = new File(mFileMgr.getBasePath(), mFileName);
            mMediaRecorder.setOutputFile(mFile.toString());
            try {
                mMediaRecorder.prepare();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            mMediaRecorder.start();
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
