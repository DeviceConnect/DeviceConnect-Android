package org.deviceconnect.android.rtspserver;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
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

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.MicAACLATMStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.CameraH265VideoStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.CameraH264VideoStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;
import org.deviceconnect.android.libmedia.streaming.util.CameraSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.util.StreamingRecorder;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

    /**
     * カメラを操作するためのクラス.
     */
    private Camera2Wrapper mCamera2;

    private CameraSurfaceDrawingThread mCameraSurfaceDrawingThread;

    /**
     * カメラのプレビューを表示する SurfaceView.
     */
    private SurfaceView mCameraView;

    /**
     * ハンドラ.
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreferences  = new RtspPreferences(getApplicationContext());
        mCameraView = findViewById(R.id.surface_view);

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
        stopRtspServer();
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mRtspServer == null || mRtspServer.getRtspSession() == null) {
            mHandler.postDelayed(() -> {
                if (mCamera2 != null) {
                    adjustSurfaceView(mCamera2.isSwappedDimensions());
                }
            }, 500);
        } else {
            RtspSession session = mRtspServer.getRtspSession();
            session.restartVideoStream();
            CameraH264VideoStream videoStream = (CameraH264VideoStream) session.getVideoStream();
            CameraSurfaceVideoEncoder videoEncoder = (CameraSurfaceVideoEncoder) videoStream.getVideoEncoder();
            mHandler.postDelayed(() -> adjustSurfaceView(videoEncoder.isSwappedDimensions()), 500);
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

        mRtspServer = new RtspServer();
        mRtspServer.setServerName(mPreferences.getServerName());
        mRtspServer.setServerPort(mPreferences.getServerPort());
        mRtspServer.setCallback(new RtspServer.Callback() {
            @Override
            public void createSession(RtspSession session) {
//                ScreenCastVideoStream videoStream = new ScreenCastVideoStream(getApplicationContext());
//                videoStream.setDestinationPort(5006);

                startStreamingRecorder();

                String mimeType = mPreferences.getEncoderName();

                VideoStream videoStream;
                if (mimeType.equalsIgnoreCase("video/hevc")) {
                    videoStream = new CameraH265VideoStream(mCameraSurfaceDrawingThread);
                    videoStream.setDestinationPort(5006);
                    if (mStreamingRecorder != null) {
                        EGLSurfaceBase eglSurfaceBase = mCameraSurfaceDrawingThread.createEGLSurfaceBase(mStreamingRecorder.getSurface());
                        eglSurfaceBase.setTag(mStreamingRecorder.getSurface());
                        mCameraSurfaceDrawingThread.addEGLSurfaceBase(eglSurfaceBase);
                    }
                } else {
                    videoStream = new CameraH264VideoStream(mCameraSurfaceDrawingThread);
                    videoStream.setDestinationPort(5006);
                    if (mStreamingRecorder != null) {
                        EGLSurfaceBase eglSurfaceBase = mCameraSurfaceDrawingThread.createEGLSurfaceBase(mStreamingRecorder.getSurface());
                        eglSurfaceBase.setTag(mStreamingRecorder.getSurface());
                        mCameraSurfaceDrawingThread.addEGLSurfaceBase(eglSurfaceBase);
                    }
                }

                VideoEncoder videoEncoder = videoStream.getVideoEncoder();
                CameraVideoQuality videoQuality = (CameraVideoQuality) videoEncoder.getVideoQuality();
                videoQuality.setFacing(mPreferences.getFacing());
                videoQuality.setVideoWidth(mPreferences.getVideoWidth());
                videoQuality.setVideoHeight(mPreferences.getVideoHeight());
                videoQuality.setIFrameInterval(mPreferences.getIFrameInterval());
                videoQuality.setFrameRate(mPreferences.getFrameRate());
                videoQuality.setBitRate(mPreferences.getVideoBitRate() * 1024);
                videoQuality.setFacing(mPreferences.getFacing());

                session.setVideoMediaStream(videoStream);


                if (mPreferences.isEnabledAudio()) {
                    AudioStream audioStream = new MicAACLATMStream();
                    audioStream.setDestinationPort(5004);

                    AudioEncoder audioEncoder = audioStream.getAudioEncoder();
                    audioEncoder.setMute(false);

                    AudioQuality audioQuality = audioEncoder.getAudioQuality();
                    audioQuality.setChannel(AudioFormat.CHANNEL_IN_MONO);
                    audioQuality.setSamplingRate(mPreferences.getSamplingRate());
                    audioQuality.setBitRate(mPreferences.getAudioBitRate() * 1024);
                    audioQuality.setUseAEC(true);

                    session.setAudioMediaStream(audioStream);
                }

                runOnUiThread(() -> findViewById(R.id.text_view).setVisibility(View.VISIBLE));
            }

            @Override
            public void releaseSession(RtspSession session) {
                runOnUiThread(() -> findViewById(R.id.text_view).setVisibility(View.INVISIBLE));
                releaseStreamingRecorder();
            }
        });

        try {
            mRtspServer.start();
        } catch (IOException e) {
            showFailToStartServer();
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
            File file = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            mStreamingRecorder.setUpMediaRecorder(new File(file, mPreferences.getFileName()));
            mStreamingRecorder.startRecording();
        } catch (IOException e) {
            Log.e("ABC", "##", e);
        }
    }

    private void releaseStreamingRecorder() {
        if (mStreamingRecorder != null) {
            mCameraSurfaceDrawingThread.removeEGLSurfaceBase(mStreamingRecorder.getSurface());
            mStreamingRecorder.stopRecording();
            mStreamingRecorder.release();
            mStreamingRecorder = null;
        }
    }

    private void showNoPermission() {
    }

    private void showFailToStartServer() {
    }

    private synchronized void startCamera() {
        if (mCameraSurfaceDrawingThread != null) {
            return;
        }

        int cameraWidth = mPreferences.getVideoWidth();
        int cameraHeight = mPreferences.getVideoHeight();
        int facing = mPreferences.getFacing();

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

        if (mCamera2 != null) {
            mCamera2.close();
            mCamera2 = null;
        }
    }

    private void adjustSurfaceView(boolean isSwappedDimensions) {
        runOnUiThread(() -> {
            int cameraWidth = mPreferences.getVideoWidth();
            int cameraHeight = mPreferences.getVideoHeight();

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

            surfaceView.getHolder().setFixedSize(cameraWidth, cameraHeight);
        });
    }

    private Size calculateViewSize(int width, int height, Size viewSize) {
        int h = viewSize.getWidth() * height / width;
        if (viewSize.getHeight() < h) {
            int w = viewSize.getHeight() * width / height;
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, viewSize.getHeight());
        }
        return new Size(viewSize.getWidth(), h);
    }
}
