/*
 AudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.Window;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.file.HostFileProvider;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * AudioRecorder.
 * 
 * @author NTT DOCOMO, INC.
 */
public class AudioRecorderActivity extends Activity {

    /** MediaRecoder. */
    private MediaRecorder mMediaRecorder;

    /** ファイル名. */
    private String mFileName;

    /** サービスID. */
    private String mServiceId;

    /** フォルダURI. */
    private File mFile;

    /** コールバック。 */
    private ResultReceiver mCallback;

    /** 開始インテント */
    private Intent mIntent;
    /** 本アクティビティを起動したレコーダーID. */
    private String mRecorderId;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audio_main);
        mIntent = getIntent();
        if (mIntent == null) {
            finish();
            return;
        }
        mCallback = mIntent.getParcelableExtra(AudioConst.EXTRA_CALLBACK);
        if (mCallback == null) {
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtility.requestPermissions(this, new Handler(),
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            try {
                                initAudioContext();
                            } catch (Exception e) {
                                releaseMediaRecorder(null);

                                Bundle data = new Bundle();
                                data.putString(AudioConst.EXTRA_CALLBACK_ERROR_MESSAGE,
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
                            data.putString(AudioConst.EXTRA_CALLBACK_ERROR_MESSAGE,
                                    "Permission " + deniedPermission + " not granted.");
                            mCallback.send(Activity.RESULT_CANCELED, data);
                            finish();
                        }
                    });
        } else {
            try {
                initAudioContext();
            } catch (Exception e) {
                releaseMediaRecorder(null);

                Bundle data = new Bundle();
                data.putString(AudioConst.EXTRA_CALLBACK_ERROR_MESSAGE,
                        e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                mCallback.send(Activity.RESULT_CANCELED, data);
                finish();
                return;
            }
            mCallback.send(Activity.RESULT_OK, null);
        }
    }

    private void initAudioContext() throws IOException {
        FileManager fileMgr = new FileManager(this, HostFileProvider.class.getName());

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        mFileName = mIntent.getStringExtra(AudioConst.EXTRA_FILE_NAME);
        mServiceId = mIntent.getStringExtra(AudioConst.EXTRA_SERVICE_ID);
        mRecorderId = mIntent.getStringExtra(VideoConst.EXTRA_RECORDER_ID);

        if (mFileName != null) {
            mFile = new File(fileMgr.getBasePath(), mFileName);
            mMediaRecorder.setOutputFile(mFile.toString());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } else {
            Bundle data = new Bundle();
            data.putString(AudioConst.EXTRA_CALLBACK_ERROR_MESSAGE, "File name must be specified.");
            mCallback.send(Activity.RESULT_CANCELED, data);
            finish();
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
        Uri uri = null;
        if (checkAudioFile()) {
            // Contents Providerに登録.
            ContentResolver resolver = this.getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Video.Media.TITLE, mFileName);
            values.put(Video.Media.DISPLAY_NAME, mFileName);
            values.put(Video.Media.ARTIST, "DeviceConnect");
            values.put(Video.Media.MIME_TYPE, AudioConst.FORMAT_TYPE);
            values.put(Video.Media.DATA, mFile.toString());
            uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }
        releaseMediaRecorder(uri);
        finish();
    }

    /**
     * Check the existence of file.
     * 
     * @return true is exist
     */
    private boolean checkAudioFile() {
        return mFile != null && mFile.exists() && mFile.length() > 0;
    }

    /**
     * MediaRecorderを解放.
     */
    private void releaseMediaRecorder(Uri uri) {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        sendRecorderStateEvent(uri, HostDeviceRecorder.RecorderState.INACTTIVE);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mMediaRecorder.stop();
            finish();
        }
        return true;
    }
    private void sendRecorderStateEvent(final Uri uri, final HostDeviceRecorder.RecorderState state) {
        Intent intent = new Intent(VideoConst.SEND_VIDEO_TO_HOSTDP);
        intent.putExtra(VideoConst.EXTRA_RECORDER_ID, mRecorderId);
        intent.putExtra(VideoConst.EXTRA_VIDEO_RECORDER_STATE, state);
        intent.putExtra(VideoConst.EXTRA_FILE_NAME, "/" + mFileName);
        intent.putExtra(VideoConst.EXTRA_SERVICE_ID, mServiceId);
        intent.putExtra(VideoConst.EXTRA_URI, uri);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    /**
     * 受信用Receiver.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() == null || mMediaRecorder == null) {
                return;
            }
            if (intent.getAction().equals((AudioConst.SEND_HOSTDP_TO_AUDIO))) {
                String videoAction = intent.getStringExtra(AudioConst.EXTRA_NAME);
                if (videoAction.equals(AudioConst.EXTRA_NAME_AUDIO_RECORD_STOP)) {
                    mMediaRecorder.stop();
                    finish();
                }
            }
        }
    };
}
