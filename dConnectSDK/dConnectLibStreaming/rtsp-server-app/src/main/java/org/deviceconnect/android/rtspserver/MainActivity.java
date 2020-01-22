package org.deviceconnect.android.rtspserver;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.hardware.camera2.CameraCharacteristics;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.MicAACLATMStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.CameraVideoStream;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.util.StreamingRecorder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.rtspserver.ui.AutoFitSurfaceView;


public class MainActivity extends AppCompatActivity {
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * RTSP サーバ.
     */
    private RtspServer mRtspServer;

    /**
     * 録画用レコーダー.
     */
    private StreamingRecorder mStreamingRecorder;

    /**
     * RTSP サーバの設定.
     */
    private RtspPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreferences  = new RtspPreferences(getApplicationContext());

        findViewById(R.id.power).setOnClickListener((v) -> toggleRtspServer());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            gotoPreferences();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<String> denies = PermissionUtil.checkPermissions(this, PERMISSIONS);
        if (!denies.isEmpty()) {
            PermissionUtil.requestPermissions(this, denies, PERMISSION_REQUEST_CODE);
        }
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

    private void gotoPreferences() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), RtspPreferenceActivity.class);
        startActivity(intent);
    }

    private synchronized void toggleRtspServer() {
        ColorStateList stateList;
        Resources res = getResources();
        if (mRtspServer == null) {
            startRtspServer();
            stateList = new ColorStateList(new int[][]{{}}, new int[]{res.getColor(R.color.colorPink)});
        } else {
            stopRtspServer();
            stateList = new ColorStateList(new int[][]{{}}, new int[]{res.getColor(R.color.colorGray)});
        }
        findViewById(R.id.power).setBackgroundTintList(stateList);
    }

    private void startRtspServer() {
        if (mRtspServer != null) {
            return;
        }

        List<String> denies = PermissionUtil.checkPermissions(this, PERMISSIONS);
        if (!denies.isEmpty()) {
            showNoPermission();
            return;
        }

        AutoFitSurfaceView surfaceView = findViewById(R.id.surface_view);
        Surface surface = surfaceView.getHolder().getSurface();

        mRtspServer = new RtspServer();
        mRtspServer.setServerName(mPreferences.getServerName());
        mRtspServer.setServerPort(mPreferences.getServerPort());
        mRtspServer.setCallback(new RtspServer.Callback() {
            @Override
            public void createSession(RtspSession session) {
                startStreamingRecorder();

                CameraVideoStream videoStream = new CameraVideoStream(getApplicationContext());
                videoStream.setDestinationPort(5006);
                videoStream.addSurface(surface);
                if (mStreamingRecorder != null) {
                    videoStream.addSurface(mStreamingRecorder.getSurface());
                }

                VideoEncoder videoEncoder = videoStream.getVideoEncoder();
                CameraVideoQuality videoQuality = (CameraVideoQuality) videoEncoder.getVideoQuality();
                videoQuality.setVideoWidth(mPreferences.getVideoWidth());
                videoQuality.setVideoHeight(mPreferences.getVideoHeight());
                videoQuality.setIFrameInterval(mPreferences.getIFrameInterval());
                videoQuality.setFrameRate(mPreferences.getFrameRate());
                videoQuality.setBitRate(mPreferences.getVideoBitRate() * 1024);
                videoQuality.setFacing(CameraCharacteristics.LENS_FACING_BACK);
                videoQuality.setRotation(mPreferences.getVideoRotation());

                session.setVideoMediaStream(videoStream);


                if (mPreferences.isEnabledAudio()) {
                    AudioStream audioStream = new MicAACLATMStream();
                    audioStream.setDestinationPort(5004);

                    AudioEncoder audioEncoder = audioStream.getAudioEncoder();
                    AudioQuality audioQuality = audioEncoder.getAudioQuality();
                    audioQuality.setChannel(AudioFormat.CHANNEL_IN_MONO);
                    audioQuality.setSamplingRate(mPreferences.getSamplingRate());
                    audioQuality.setBitRate(mPreferences.getAudioBitRate() * 1024);
                    audioQuality.setUseAEC(true);

                    session.setAudioMediaStream(audioStream);
                }

                runOnUiThread(() -> {
                    boolean isSwapped = videoQuality.isSwappedDimensions(getApplicationContext());
                    int w = isSwapped ? videoQuality.getVideoHeight() : videoQuality.getVideoWidth();
                    int h = isSwapped ? videoQuality.getVideoWidth() : videoQuality.getVideoHeight();
                    surfaceView.setVisibility(View.VISIBLE);
                    surfaceView.setAspectRatio(w, h);
                });
            }

            @Override
            public void releaseSession(RtspSession session) {
                runOnUiThread(() -> surfaceView.setVisibility(View.INVISIBLE));
                releaseStreamingRecorder();
            }
        });

        try {
            mRtspServer.start();
        } catch (IOException e) {
            showFaileToStartServer();
        }
    }

    private void stopRtspServer() {
        if (mRtspServer != null) {
            mRtspServer.stop();
            mRtspServer = null;
        }
    }

    private void startStreamingRecorder() {
        if (!mPreferences.isEnabledRecorder()) {
            return;
        }

        mStreamingRecorder = new StreamingRecorder(MainActivity.this);
        StreamingRecorder.Settings settings = mStreamingRecorder.getSettings();
        settings.setWidth(mPreferences.getVideoWidth());
        settings.setHeight(mPreferences.getVideoHeight());
        settings.setSensorOrientation(0);

        try {
            File file = Environment.getExternalStorageDirectory();
            mStreamingRecorder.setUpMediaRecorder(new File(file, mPreferences.getFileName()));
            mStreamingRecorder.startRecording();
        } catch (IOException e) {
            Log.e("ABC", "##", e);
        }
    }

    private void releaseStreamingRecorder() {
        if (mStreamingRecorder != null) {
            mStreamingRecorder.stopRecording();
            mStreamingRecorder.release();
            mStreamingRecorder = null;
        }
    }

    private void showNoPermission() {

    }

    private void showFaileToStartServer() {

    }
}
