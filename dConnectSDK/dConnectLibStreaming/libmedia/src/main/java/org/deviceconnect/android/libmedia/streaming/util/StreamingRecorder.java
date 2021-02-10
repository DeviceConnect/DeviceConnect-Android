package org.deviceconnect.android.libmedia.streaming.util;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import org.deviceconnect.android.libmedia.BuildConfig;

public class StreamingRecorder {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "MJPEG";

    /**
     * カメラの向きと画面の向きを調整する配列.
     */
    private static final SparseIntArray[] ORIENTATIONS = new SparseIntArray[2];
    static {
        ORIENTATIONS[0] = new SparseIntArray();
        ORIENTATIONS[1] = new SparseIntArray();
        ORIENTATIONS[0].append(Surface.ROTATION_0, 90);
        ORIENTATIONS[0].append(Surface.ROTATION_90, 0);
        ORIENTATIONS[0].append(Surface.ROTATION_180, 270);
        ORIENTATIONS[0].append(Surface.ROTATION_270, 180);
        ORIENTATIONS[1].append(Surface.ROTATION_0, 270);
        ORIENTATIONS[1].append(Surface.ROTATION_90, 180);
        ORIENTATIONS[1].append(Surface.ROTATION_180, 90);
        ORIENTATIONS[1].append(Surface.ROTATION_270, 0);
    }

    /**
     * レコーディング.
     */
    private MediaRecorder mMediaRecorder;

    /**
     * 動画撮影の設定を格納するクラス.
     */
    private final Settings mSettings = new Settings();

    /**
     * コンテキスト
     */
    private Context mContext;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public StreamingRecorder(Context context) {
        mContext = context;
    }

    /**
     * 動画撮影の設定を取得します.
     *
     * <p>
     * このクラスに値を設定することで、動画撮影のパラメータを変更することができます。
     * </p>
     *
     * @return {@link Settings}
     */
    public Settings getSettings() {
        return mSettings;
    }

    /**
     * 動画に変換するための映像を描画する Surface を取得します.
     *
     * <p>
     * ここで取得した Surface に描画した画像が動画がとして保存されます。
     * </p>
     *
     * {@link #setUpMediaRecorder(File)} が行われていないと Surface は取得できません。
     *
     * @return Surface
     */
    public synchronized Surface getSurface() {
        if (mMediaRecorder == null) {
            throw new IllegalStateException("MediaRecorder is not setup.");
        }

        return mMediaRecorder.getSurface();
    }

    /**
     * 動画撮影のための準備を行います.
     *
     * @throws IOException 動画撮影の準備に失敗した場合に発生
     */
    public synchronized void setUpMediaRecorder(File outputFile) throws IOException {
        if (DEBUG) {
            Log.e(TAG, "Set up MediaRecorder");
            Log.e(TAG, "  VideoSize: " + mSettings.getWidth() + "x" + mSettings.getHeight());
            Log.e(TAG, "  BitRate: " + mSettings.getBitRate());
            Log.e(TAG, "  FrameRate: " + mSettings.getFrameRate());
            Log.e(TAG, "  OutputFile: " + outputFile.getAbsolutePath());
        }

        int rotation = getDisplayRotation();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mMediaRecorder.setVideoEncodingBitRate(mSettings.getBitRate());
        mMediaRecorder.setVideoFrameRate(mSettings.getFrameRate());
        mMediaRecorder.setVideoSize(mSettings.getWidth(), mSettings.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(ORIENTATIONS[mSettings.getSensorOrientation()].get(rotation));
        mMediaRecorder.setOnInfoListener(mOnInfoListener);
        mMediaRecorder.setOnErrorListener(mOnErrorListener);
        mMediaRecorder.prepare();
    }

    /**
     * 動画撮影を開始します.
     */
    public synchronized void startRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.start();
        }
    }

    /**
     * 動画撮影を停止します.
     *
     * <p>
     * 動画撮影されていない場合に呼び出された場合には何も処理を行いません。
     * </p>
     */
    public synchronized void stopRecording() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    /**
     * 動画撮影用のインスタンスを破棄します.
     */
    public synchronized void release() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                // ignore.
            }
            try {
                mMediaRecorder.release();
            } catch (Exception e) {
                // ignore.
            }
            mMediaRecorder = null;
        }
    }

    /**
     * 画面の向きを取得します.
     *
     * @return 画面の向き
     */
    private int getDisplayRotation() {
        WindowManager m = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return m == null ? 0 : m.getDefaultDisplay().getRotation();
    }

    /**
     * MediaRecorder からのエラー通知を受け取るリスナー.
     */
    private final MediaRecorder.OnErrorListener mOnErrorListener = (mr, what, extra) -> {

    };

    /**
     * MediaRecorder からの情報通知を受け取るリスナー.
     */
    private final MediaRecorder.OnInfoListener mOnInfoListener = (mr, what, extra) -> {

    };

    public static class Settings {
        private int mWidth = 640;
        private int mHeight = 480;
        private int mBitRate = 10 * 1024 * 1024;
        private int mFrameRate = 30;

        /**
         * カメラの向き.
         */
        private int mSensorOrientation;

        public int getWidth() {
            return mWidth;
        }

        public void setWidth(int width) {
            mWidth = width;
        }

        public int getHeight() {
            return mHeight;
        }

        public void setHeight(int height) {
            mHeight = height;
        }

        public int getBitRate() {
            return mBitRate;
        }

        public void setBitRate(int bitRate) {
            mBitRate = bitRate;
        }

        public int getFrameRate() {
            return mFrameRate;
        }

        public void setFrameRate(int frameRate) {
            mFrameRate = frameRate;
        }

        public int getSensorOrientation() {
            return mSensorOrientation;
        }

        public void setSensorOrientation(int sensorOrientation) {
            mSensorOrientation = sensorOrientation;
        }

        @Override
        public String toString() {
            return "Settings{" +
                    "mWidth=" + mWidth +
                    ", mHeight=" + mHeight +
                    ", mBitRate=" + mBitRate +
                    ", mFrameRate=" + mFrameRate +
                    '}';
        }
    }
}
