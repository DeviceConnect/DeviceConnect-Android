package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.mjpeg.UvcMjpgRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uncompressed.UvcUncompressedRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaRecorderManager {
    /**
     * レコーディング停止アクションを定義.
     */
    public static final String ACTION_STOP_RECORDING = "org.deviceconnect.android.deviceplugin.uvc.STOP_RECORDING";

    /**
     * プレビュー停止用アクションを定義.
     */
    public static final String ACTION_STOP_PREVIEW = "org.deviceconnect.android.deviceplugin.uvc.STOP_PREVIEW";

    /**
     * ブロードキャスト停止用アクションを定義.
     */
    public static final String ACTION_STOP_BROADCAST = "org.deviceconnect.android.deviceplugin.uvc.STOP_BROADCAST";

    /**
     * レコーダのIDを格納するためのキーを定義.
     */
    public static final String KEY_RECORDER_ID = "recorder_id";

    /**
     * プレビュー停止・レコーディング停止・ブロードキャスト停止・画面の回転イベントなどを受け取るための BroadcastReceiver.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
           if (ACTION_STOP_PREVIEW.equals(action)) {
                stopPreviewServer(intent.getStringExtra(KEY_RECORDER_ID));
            } else if (ACTION_STOP_RECORDING.equals(action)) {
                stopRecording(intent.getStringExtra(KEY_RECORDER_ID));
            } else if (ACTION_STOP_BROADCAST.equals(action)) {
                stopBroadcast(intent.getStringExtra(KEY_RECORDER_ID));
            }
        }
    };

    /**
     * レコーダのリスト.
     */
    private final List<UvcRecorder> mUvcRecorderList = new ArrayList<>();
    private final Context mContext;

    /**
     * 各レコーダのイベントを通知するためのリスナー.
     */
    private final WeakReferenceList<OnEventListener> mOnEventListeners = new WeakReferenceList<>();

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public MediaRecorderManager(Context context, UVCCamera uvcCamera) {
        mContext = context;
        initRecorders(context, uvcCamera);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_PREVIEW);
        filter.addAction(ACTION_STOP_RECORDING);
        filter.addAction(ACTION_STOP_BROADCAST);
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * 後始末を行います.
     */
    public void destroy() {
        try {
            mContext.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            // ignore.
        }

        mOnEventListeners.clear();

        for (MediaRecorder recorder : mUvcRecorderList) {
            try {
                recorder.destroy();
            } catch (Exception e) {
                // ignore.
            }
        }

        mUvcRecorderList.clear();
    }

    /**
     * レコーダが使用できるか確認します.
     *
     * @param recorder レコーダ
     * @return 使用できる場合はtrue、それ以外はfalse
     */
    public boolean canUseRecorder(UvcRecorder recorder) {
        for (UvcRecorder uvcRecorder : getUvcRecorderList()) {
            if (uvcRecorder == recorder) {
                continue;
            }
            if (uvcRecorder.isPreviewRunning() || uvcRecorder.isBroadcasterRunning() ||
                    uvcRecorder.getState() == MediaRecorder.State.RECORDING) {
                // カメラが使用中
                return false;
            }
        }
        return true;
    }

    /**
     * 使用するレコーダ以外のレコーダを停止します。
     *
     * プレビュー配信中などの停止できない場合には、停止しません。
     * その場合には、カメラを使用できないので注意が必要になります。
     *
     * @param useRecorder 使用するレコーダ
     */
    public void stopCameraRecorder(MediaRecorder useRecorder) {
        for (MediaRecorder recorder : getUvcRecorderList()) {
            if (recorder == useRecorder) {
                continue;
            }

            if (recorder instanceof UvcRecorder) {
                if (!recorder.isPreviewRunning() && !recorder.isBroadcasterRunning() &&
                        recorder.getState() != MediaRecorder.State.RECORDING) {
                    // 強制的にカメラを停止
                    EGLSurfaceDrawingThread drawingThread = recorder.getSurfaceDrawingThread();
                    drawingThread.stop(true);

                    // カメラの処理は別スレッドで行われているので、ここで少し待機します
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // ignore.
                    }
                }
            }
        }
    }


    /**
     * レコーダのリストを取得します.
     *
     * @return レコーダ
     */
    public List<UvcRecorder> getUvcRecorderList() {
        return mUvcRecorderList;
    }

    /**
     * レコーダ ID を指定してレコーダを取得します.
     *
     * レコーダが見つからない場合は null を返却します。
     *
     * @param id レコーダID
     * @return レコーダ
     */
    public UvcRecorder findUvcRecorderById(String id) {
        if (id != null) {
            for (UvcRecorder recorder : getUvcRecorderList()) {
                if (id.equalsIgnoreCase(recorder.getId())) {
                    return recorder;
                }
            }
        }
        return null;
    }

    /**
     * デフォルトのレコーダを取得します.
     *
     * レコーダが存在しない場合には null を返却します。
     *
     * @return デフォルトのレコーダ
     */
    public UvcRecorder getDefaultRecorder() {
        if (mUvcRecorderList.isEmpty()) {
            return null;
        }
        return mUvcRecorderList.get(0);
    }

    /**
     * レコーダの初期化処理を行います.
     *
     * @param context コンテキスト
     * @param camera UVC デバイス
     */
    private void initRecorders(Context context, UVCCamera camera) {
        boolean hasMJPEG = false;
        boolean hasH264 = false;
        boolean hasUncompressed = false;
        try {
            List<Parameter> parameters = camera.getParameter();
            for (Parameter p : parameters) {
                switch (p.getFrameType()) {
                    case UNCOMPRESSED:
                        hasUncompressed = true;
                        break;
                    case MJPEG:
                        hasMJPEG = true;
                        if (p.hasExtH264()) {
                            // Extension Unit を持っている場合に H264 として使用できる。
                            hasH264 = true;
                        }
                        break;
                    case H264:
                        hasH264 = true;
                        break;
                }
            }
        } catch (IOException e) {
            // ignore.
        }

        if (hasMJPEG) {
            addUvcRecorder(new UvcMjpgRecorder(context, camera));
        }

        if (hasH264) {
            addUvcRecorder(new UvcH264Recorder(context, camera));
        }

        if (hasUncompressed) {
            addUvcRecorder(new UvcUncompressedRecorder(context, camera));
        }

        for (MediaRecorder recorder : mUvcRecorderList) {
            recorder.initialize();
        }
    }

    /**
     * UvcRecorder をリストに追加します.
     *
     * @param mediaRecorder 追加するレコーダ
     */
    private void addUvcRecorder(UvcRecorder mediaRecorder) {
        mediaRecorder.setOnEventListener(new MediaRecorder.OnEventListener() {
            @Override
            public void onConfigChanged() {
                postOnConfigChanged(mediaRecorder);
            }

            @Override
            public void onPreviewStarted(List<PreviewServer> servers) {
                postOnPreviewStarted(mediaRecorder, servers);
            }

            @Override
            public void onPreviewStopped() {
                postOnPreviewStopped(mediaRecorder);
            }

            @Override
            public void onPreviewError(Exception e) {
                postOnPreviewError(mediaRecorder, e);
            }

            @Override
            public void onBroadcasterStarted(Broadcaster broadcaster) {
                postOnBroadcasterStarted(mediaRecorder, broadcaster);
            }

            @Override
            public void onBroadcasterStopped(Broadcaster broadcaster) {
                postOnBroadcasterStopped(mediaRecorder, broadcaster);
            }

            @Override
            public void onBroadcasterError(Broadcaster broadcaster, Exception e) {
                postOnBroadcasterError(mediaRecorder, broadcaster, e);
            }

            @Override
            public void onError(Exception e) {
                postOnError(mediaRecorder, e);
            }
        });
        mUvcRecorderList.add(mediaRecorder);
    }

    /**
     * 指定された ID のレコーダのプレビューを停止します.
     *
     * @param id レコーダの ID
     */
    private void stopPreviewServer(final String id) {
        MediaRecorder recorder = findUvcRecorderById(id);
        if (recorder != null) {
            recorder.stopPreview();
        }
    }

    /**
     * 指定された ID のレコードのブロードキャストを停止します.
     *
     * @param id レコードの ID
     */
    private void stopBroadcast(final String id) {
        MediaRecorder recorder = findUvcRecorderById(id);
        if (recorder != null) {
            recorder.stopBroadcaster();
        }
    }

    /**
     * 指定された ID のレコードの録音・録画を停止します.
     *
     * @param id レコードの ID
     */
    private void stopRecording(final String id) {
    }

    /**
     * イベント通知用のリスナーを追加します.
     *
     * @param listener 追加するリスナー
     */
    public void addOnEventListener(OnEventListener listener) {
        mOnEventListeners.add(listener);
    }

    /**
     * イベント通知用のリスナーを削除します.
     *
     * @param listener 削除するリスナー
     */
    public void removeOnEventListener(OnEventListener listener) {
        mOnEventListeners.remove(listener);
    }

    private void postOnConfigChanged(MediaRecorder recorder) {
        for (OnEventListener l : mOnEventListeners) {
            l.onConfigChanged(recorder);
        }
    }

    private void postOnPreviewStarted(MediaRecorder recorder, List<PreviewServer> servers) {
        for (OnEventListener l : mOnEventListeners) {
            l.onPreviewStarted(recorder, servers);
        }
    }

    private void postOnPreviewStopped(MediaRecorder recorder) {
        for (OnEventListener l : mOnEventListeners) {
            l.onPreviewStopped(recorder);
        }
    }

    private void postOnPreviewError(MediaRecorder recorder, Exception e) {
        for (OnEventListener l : mOnEventListeners) {
            l.onPreviewError(recorder, e);
        }
    }

    private void postOnBroadcasterStarted(MediaRecorder recorder, Broadcaster broadcaster) {
        for (OnEventListener l : mOnEventListeners) {
            l.onBroadcasterStarted(recorder, broadcaster);
        }
    }

    private void postOnBroadcasterStopped(MediaRecorder recorder, Broadcaster broadcaster) {
        for (OnEventListener l : mOnEventListeners) {
            l.onBroadcasterStopped(recorder, broadcaster);
        }
    }

    private void postOnBroadcasterError(MediaRecorder recorder, Broadcaster broadcaster, Exception e) {
        for (OnEventListener l : mOnEventListeners) {
            l.onBroadcasterError(recorder, broadcaster, e);
        }
    }

//    private void postOnTakePhoto(MediaRecorder recorder, String uri, String filePath, String mimeType) {
//        for (OnEventListener l : mOnEventListeners) {
//            l.onTakePhoto(recorder, uri, filePath, mimeType);
//        }
//    }
//
//    private void postOnRecordingStarted(MediaRecorder recorder, String fileName) {
//        for (OnEventListener l : mOnEventListeners) {
//            l.onRecordingStarted(recorder, fileName);
//        }
//    }
//
//    private void postOnRecordingPause(MediaRecorder recorder) {
//        for (OnEventListener l : mOnEventListeners) {
//            l.onRecordingPause(recorder);
//        }
//    }
//
//    private void postOnRecordingResume(MediaRecorder recorder) {
//        for (OnEventListener l : mOnEventListeners) {
//            l.onRecordingResume(recorder);
//        }
//    }
//
//    private void postOnRecordingStopped(MediaRecorder recorder, String fileName) {
//        for (OnEventListener l : mOnEventListeners) {
//            l.onRecordingStopped(recorder, fileName);
//        }
//    }

    private void postOnError(MediaRecorder recorder, Exception e) {
        for (OnEventListener l : mOnEventListeners) {
            l.onError(recorder, e);
        }
    }

    public interface OnEventListener {
        void onConfigChanged(MediaRecorder recorder);

        void onPreviewStarted(MediaRecorder recorder, List<PreviewServer> servers);
        void onPreviewStopped(MediaRecorder recorder);
        void onPreviewError(MediaRecorder recorder, Exception e);

        void onBroadcasterStarted(MediaRecorder recorder, Broadcaster broadcaster);
        void onBroadcasterStopped(MediaRecorder recorder, Broadcaster broadcaster);
        void onBroadcasterError(MediaRecorder recorder, Broadcaster broadcaster, Exception e);

//        void onTakePhoto(MediaRecorder recorder, String uri, String filePath, String mimeType);
//
//        void onRecordingStarted(MediaRecorder recorder, String fileName);
//        void onRecordingPause(MediaRecorder recorder);
//        void onRecordingResume(MediaRecorder recorder);
//        void onRecordingStopped(MediaRecorder recorder, String fileName);

        void onError(MediaRecorder recorder, Exception e);
    }
}
