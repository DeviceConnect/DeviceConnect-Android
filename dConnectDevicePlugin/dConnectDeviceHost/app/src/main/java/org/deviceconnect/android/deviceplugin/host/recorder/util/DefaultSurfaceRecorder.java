/*
 DefaultSurfaceRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 指定された Surface を録画します.
 */
public class DefaultSurfaceRecorder implements SurfaceRecorder {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * 日付のフォーマット.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private MediaRecorder mMediaRecorder;

    private boolean mIsRecording;

    private final Context mContext;

    private final Camera2Recorder.CameraFacing mFacing;

    private final Integer mSensorOrientation;

    private final Size mVideoSize;

    private final File mOutputFile;

    private Handler mRecorderThread;

    public DefaultSurfaceRecorder(final Context context,
                           final Camera2Recorder.CameraFacing facing,
                           final Integer sensorOrientation,
                           final Size videoSize,
                           final File basePath) {
        mContext = context;
        mFacing = facing;
        mSensorOrientation = sensorOrientation;
        mVideoSize = videoSize;
        mOutputFile = new File(basePath, generateVideoFileName());

        HandlerThread thread = new HandlerThread("MediaRecorder");
        thread.start();
        mRecorderThread = new Handler(thread.getLooper());
    }

    private String generateVideoFileName() {
        return "video_" + DATE_FORMAT.format(new Date()) + ".mp4";
    }

    private void setUpMediaRecorder(final File outputFile) throws IOException {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int hint;
        SparseIntArray orientations;
        if (mFacing == Camera2Recorder.CameraFacing.FRONT) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    rotation = Surface.ROTATION_0;
                    break;
                case Surface.ROTATION_90:
                    rotation = Surface.ROTATION_270;
                    break;
                case Surface.ROTATION_180:
                    rotation = Surface.ROTATION_180;
                    break;
                case Surface.ROTATION_270:
                    rotation = Surface.ROTATION_90;
                    break;
            }
        }
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                orientations = INVERSE_ORIENTATIONS;
                break;
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
            default:
                orientations = DEFAULT_ORIENTATIONS;
                break;
        }
        hint = orientations.get(rotation);
        mMediaRecorder.setOrientationHint(hint);
        mMediaRecorder.prepare();
        if (DEBUG) {
            Log.d(TAG, "VideoSize: " + mVideoSize.getWidth() + "x" + mVideoSize.getHeight());
            Log.d(TAG, "OutputFile: " + outputFile.getAbsolutePath());
            Log.d(TAG, "Facing: " + mFacing.getName());
            Log.d(TAG, "SensorOrientation: " + mSensorOrientation);
            Log.d(TAG, "DisplayRotation: " + rotation);
            Log.d(TAG, "OrientationHint: " + hint);
        }
    }

    private WindowManager getWindowManager() {
        return (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public Surface getInputSurface() {
        if (mMediaRecorder == null) {
            return null;
        }
        return mMediaRecorder.getSurface();
    }

    @Override
    public synchronized void start(final OnRecordingStartListener listener) {
        Runnable r;
        if (!mIsRecording) {
            mIsRecording = true;
            r = () -> {
                try {
                    setUpMediaRecorder(mOutputFile);
                    mMediaRecorder.start();
                    listener.onRecordingStart();
                } catch (IllegalStateException | IOException e) {
                    listener.onRecordingStartError(e);
                }
            };
        } else {
            r = listener::onRecordingStart;
        }
        mRecorderThread.post(r);
    }

    @Override
    public synchronized void stop(final OnRecordingStopListener listener) {
        Runnable r;
        if (mIsRecording) {
            mIsRecording = false;
            r = () -> {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                listener.onRecordingStop();
            };
        } else {
            r = listener::onRecordingStop;
        }
        mRecorderThread.post(r);
    }

    @Override
    public synchronized void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mRecorderThread != null) {
            mRecorderThread.getLooper().quit();
            mRecorderThread = null;
        }
    }

    @Override
    public File getOutputFile() {
        return mOutputFile;
    }
}
