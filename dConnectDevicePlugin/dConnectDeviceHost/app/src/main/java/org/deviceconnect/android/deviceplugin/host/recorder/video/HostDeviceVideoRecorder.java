/*
 HostDeviceVideoRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.video;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.HostDeviceCameraRecorder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Host Device Video Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class HostDeviceVideoRecorder implements HostDeviceRecorder, HostDeviceStreamRecorder {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "HOST";

    private static final String ID_BASE = "video";

    private static final String NAME_BASE = "AndroidHost Video Recorder";

    private static final String MIME_TYPE = "video/3gp";

    private List<String> mMimeTypes = new ArrayList<String>(){
        {
            add("video/3gp");
        }
    };
    /** 現在録画中のファイル名. */
    private String mNowRecordingFileName;

    /**
     * デフォルトのプレビューサイズの閾値を定義.
     */
    private static final int DEFAULT_PREVIEW_WIDTH_THRESHOLD = 640;

    /**
     * デフォルトのプレビューサイズの閾値を定義.
     */
    private static final int DEFAULT_PREVIEW_HEIGHT_THRESHOLD = 480;

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private Context mContext;
    private int mCameraId;
    private HostDeviceCameraRecorder.CameraFacing mFacing;

    private boolean mIsInitialized;

    private List<PictureSize> mSupportedPictureSizes = new ArrayList<>();

    private RecorderState mState;
    private PictureSize mPictureSize;
    private double mMaxFrameRate;

    public HostDeviceVideoRecorder(final Context context, final int cameraId,
                                   final HostDeviceCameraRecorder.CameraFacing facing) {
        mContext = context;
        mCameraId = cameraId;
        mFacing = facing;
        mState = RecorderState.INACTTIVE;
    }

    @Override
    public void initialize() {
        if (mIsInitialized) {
            return;
        }

        try {
            Camera camera = Camera.open(mCameraId);
            Camera.Parameters params = camera.getParameters();
            Camera.Size picture = params.getPictureSize();
            setPictureSize(new PictureSize(picture.width, picture.height));
            for (Camera.Size size : params.getSupportedPictureSizes()) {
                mSupportedPictureSizes.add(new PictureSize(size.width, size.height));
            }
            PictureSize defaultSize = getDefaultPictureSize();
            if (defaultSize != null) {
                setPictureSize(defaultSize);
            } else {
                setPictureSize(new PictureSize(picture.width, picture.height));
            }
            camera.release();

            mIsInitialized = true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    @Override
    public void clean() {
        stopRecording(null);
    }

    @Override
    public String getId() {
        return ID_BASE + "_" + mCameraId;
    }

    @Override
    public String getName() {
        return NAME_BASE + " - " + mFacing.getName();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public RecorderState getState() {
        return mState;
    }

    @Override
    public PictureSize getPictureSize() {
        return mPictureSize;
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        mPictureSize = size;
    }

    @Override
    public PictureSize getPreviewSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPreviewSize(final PictureSize size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMaxFrameRate() {
        return mMaxFrameRate;
    }

    @Override
    public void setMaxFrameRate(double frameRate) {
        mMaxFrameRate = frameRate;
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        return mSupportedPictureSizes;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public boolean isSupportedPictureSize(int width, int height) {
        if (mSupportedPictureSizes != null) {
            for (PictureSize size : mSupportedPictureSizes) {
                if (size.getWidth() == width && size.getHeight() == height) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedPreviewSize(int width, int height) {
        return false;
    }

    @Override
    public boolean canPauseRecording() {
        return false;
    }

    @Override
    public synchronized void startRecording(final String serviceId, final RecordingListener listener) {
        if (getState() == RecorderState.RECORDING) {
            throw new IllegalStateException();
        }
        mState = RecorderState.RECORDING;
        mNowRecordingFileName = generateVideoFileName();
        Intent intent = new Intent();
        intent.setClass(mContext, VideoRecorderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(VideoConst.EXTRA_RECORDER_ID, getId());
        intent.putExtra(VideoConst.EXTRA_CAMERA_ID, mCameraId);
        intent.putExtra(VideoConst.EXTRA_SERVICE_ID, serviceId);
        intent.putExtra(VideoConst.EXTRA_FILE_NAME, mNowRecordingFileName);
        intent.putExtra(VideoConst.EXTRA_PICTURE_SIZE, getPictureSize());
        intent.putExtra(VideoConst.EXTRA_FRAME_RATE, (int) getMaxFrameRate());
        intent.putExtra(VideoConst.EXTRA_CALLBACK, new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    listener.onRecorded(HostDeviceVideoRecorder.this, mNowRecordingFileName);
                } else {
                    String msg =
                        resultData.getString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE, "Unknown error.");
                    listener.onFailed(HostDeviceVideoRecorder.this, msg);
                }
            }
        });
        mContext.startActivity(intent);
    }

    @Override
    public synchronized void stopRecording(final StoppingListener listener) {
        Intent intent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEO);
        intent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_RECORD_STOP);
        mContext.sendBroadcast(intent);
        mState = RecorderState.INACTTIVE;
        if (listener != null) {
            if (mNowRecordingFileName != null) {
                listener.onStopped(this, mNowRecordingFileName);
            } else {
                listener.onFailed(this, "Failed to Stop recording.");
            }
        }
        mNowRecordingFileName = null;
    }

    @Override
    public void pauseRecording() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeRecording() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "{ face: " + mFacing + ", mPictureSize: " + mPictureSize + " }";
    }

    public void setState(RecorderState state) {
        mState = state;
    }

    private String generateVideoFileName() {
        return "video" + mSimpleDateFormat.format(new Date()) + VideoConst.FORMAT_TYPE;
    }

    /**
     * デフォルトのプレビューサイズを取得します.
     * @return デフォルトのプレビューサイズ
     */
    private PictureSize getDefaultPictureSize() {
        if (mSupportedPictureSizes.size() == 0) {
            return null;
        }
        PictureSize defaultSize = null;
        for (PictureSize size : mSupportedPictureSizes) {
            if (size.getWidth() == DEFAULT_PREVIEW_WIDTH_THRESHOLD &&
                    size.getHeight() == DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                defaultSize = size;
            }
        }
        if (defaultSize != null) {
            return defaultSize;
        }
        for (PictureSize size : mSupportedPictureSizes) {
            if (size.getWidth() * size.getHeight() <=
                    DEFAULT_PREVIEW_WIDTH_THRESHOLD * DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                defaultSize = size;
            }
        }
        if (defaultSize != null) {
            return defaultSize;
        }
        return mSupportedPictureSizes.get(0);
    }
}
