package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.io.File;
import java.io.IOException;

public abstract class MP4Recorder {
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;
    private Handler mRecorderThread;
    private HostMediaRecorder.Settings mSettings;
    private State mState = State.INACTIVE;

    public MP4Recorder(File filePath) {
        mOutputFile = filePath;

        HandlerThread thread = new HandlerThread("MediaRecorder");
        thread.start();
        mRecorderThread = new Handler(thread.getLooper());
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    public void start(OnRecordingStartListener listener) {
        Runnable r;
        if (mState == State.INACTIVE) {
            mState = State.RECORDING;
            r = () -> {
                try {
                    mMediaRecorder = setUpMediaRecorder(mOutputFile);
                    mMediaRecorder.start();
                    if (listener != null) {
                        listener.onRecordingStart();
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onRecordingStartError(e);
                    }
                }
            };
        } else if (mState == State.RECORDING) {
            r = () -> {
                if (listener != null) {
                    listener.onRecordingStart();
                }
            };
        } else {
            r = () -> {
                if (listener != null) {
                    listener.onRecordingStartError(new IllegalStateException());
                }
            };
        }
        mRecorderThread.post(r);
    }

    public void pause() {
        Runnable r;
        if (canUsePause() && mState == State.RECORDING) {
            mState = State.PAUSED;
            r = () -> {
                if (mMediaRecorder != null) {
                    try {
                        mMediaRecorder.pause();
                    } catch (IllegalStateException e) {
                        // ignore.
                    }
                }
            };
        } else {
            r = () -> {};
        }
        mRecorderThread.post(r);
    }

    public void resume() {
        Runnable r;
        if (canUsePause() && mState == State.PAUSED) {
            r = () -> {
                if (mMediaRecorder != null) {
                    try {
                        mMediaRecorder.resume();
                    } catch (IllegalStateException e) {
                        // ignore.
                    }
                }
            };
        } else {
            r = () -> {};
        }
        mRecorderThread.post(r);
    }

    public void stop(OnRecordingStopListener listener) {
        Runnable r;
        if (mState != State.INACTIVE) {
            mState = State.INACTIVE;
            r = () -> {
                tearDownMediaRecorder();
                stopMediaRecorder();
                if (listener != null) {
                    listener.onRecordingStop();
                }
            };
        } else {
            r = () -> {
                if (listener != null) {
                    listener.onRecordingStop();
                }
            };
        }
        mRecorderThread.post(r);
    }

    public synchronized void release() {
        stopMediaRecorder();

        if (mRecorderThread != null) {
            mRecorderThread.getLooper().quit();
            mRecorderThread = null;
        }
    }

    public boolean canUsePause() {
        return Build.VERSION_CODES.N <= Build.VERSION.SDK_INT;
    }

    public abstract MediaRecorder setUpMediaRecorder(final File outputFile) throws IOException;

    public abstract void tearDownMediaRecorder();

    private void stopMediaRecorder() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            } catch (Exception e) {
                // ignore.
            }

            if (mMediaRecorder != null) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }
    }

    public interface OnRecordingStartListener {
        void onRecordingStart();
        void onRecordingStartError(Throwable e);
    }

    public interface OnRecordingStopListener {
        void onRecordingStop();
        void onRecordingStopError(Throwable e);
    }

    /**
     * MediaRecorder の状態.
     */
    enum State {
        /**
         * 動作していない.
         */
        INACTIVE,

        /**
         * 録画が一時停止中の状態.
         */
        PAUSED,

        /**
         * 録画・静止画撮影中の状態.
         */
        RECORDING,

        /**
         * エラーで停止している状態.
         */
        ERROR
    }
}
