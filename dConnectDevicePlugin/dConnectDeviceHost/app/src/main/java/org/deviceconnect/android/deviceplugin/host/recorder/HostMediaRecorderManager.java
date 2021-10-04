/*
 HostMediaRecorderManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperManager;
import org.deviceconnect.android.deviceplugin.host.recorder.audio.HostAudioRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.screen.ScreenCastRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.provider.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Host Device Recorder Manager.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostMediaRecorderManager {
    /**
     * レコーディング停止アクションを定義.
     */
    public static final String ACTION_STOP_RECORDING = "org.deviceconnect.android.deviceplugin.host.STOP_RECORDING";

    /**
     * プレビュー停止用アクションを定義.
     */
    public static final String ACTION_STOP_PREVIEW = "org.deviceconnect.android.deviceplugin.host.STOP_PREVIEW";

    /**
     * ブロードキャスト停止用アクションを定義.
     */
    public static final String ACTION_STOP_BROADCAST = "org.deviceconnect.android.deviceplugin.host.STOP_BROADCAST";

    /**
     * レコーダのIDを格納するためのキーを定義.
     */
    public static final String KEY_RECORDER_ID = "recorder_id";

    /**
     * List of HostMediaRecorder.
     */
    private final List<HostMediaRecorder> mRecorders = new ArrayList<>();

    /**
     * HostDevicePhotoRecorder.
     */
    private Camera2Recorder mDefaultPhotoRecorder;

    /**
     * コンテキスト.
     */
    private final DevicePluginContext mHostDevicePluginContext;

    /**
     * カメラ管理クラス.
     */
    private CameraWrapperManager mCameraWrapperManager;

    /**
     * ファイル管理クラス.
     */
    private final FileManager mFileManager;

    /**
     * 各レコーダのイベントを通知するためのリスナー.
     */
    private final WeakReferenceList<OnEventListener> mOnEventListeners = new WeakReferenceList<>();

    /**
     * MediaProjection
     */
    private final MediaProjectionProvider mMediaProjectionProvider;

    /**
     * プレビュー停止・レコーディング停止・ブロードキャスト停止・画面の回転イベントなどを受け取るための BroadcastReceiver.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                onDisplayRotationChanged(context);
            } else if (ACTION_STOP_PREVIEW.equals(action)) {
                stopPreviewServer(intent.getStringExtra(KEY_RECORDER_ID));
            } else if (ACTION_STOP_RECORDING.equals(action)) {
                stopRecording(intent.getStringExtra(KEY_RECORDER_ID));
            } else if (ACTION_STOP_BROADCAST.equals(action)) {
                stopBroadcast(intent.getStringExtra(KEY_RECORDER_ID));
            }
        }
    };

    /**
     * コンストラクタ.
     *
     * @param pluginContext コンテキスト
     * @param fileManager ファイル管理クラス
     */
    public HostMediaRecorderManager(final DevicePluginContext pluginContext, final FileManager fileManager) {
        mHostDevicePluginContext = pluginContext;
        mMediaProjectionProvider = new MediaProjectionProvider(pluginContext.getContext());
        mFileManager = fileManager;
        initRecorders();
    }

    private Context getContext() {
        return mHostDevicePluginContext.getContext();
    }

    /**
     * レコーダの初期化処理を行います.
     *
     * <p>
     * ここで使用できるレコーダの登録を行います。
     * </p>
     */
    private void initRecorders() {
        if (checkCameraHardware(getContext())) {
            try {
                mCameraWrapperManager = new CameraWrapperManager(getContext());
                createCameraRecorders(mCameraWrapperManager, mFileManager);
            } catch (Exception e) {
                // ignore.
            }
        }

        if (checkMicrophone(getContext())) {
            try {
                createAudioRecorders(mFileManager);
            } catch (Exception e) {
                // ignore.
            }
        }

        if (checkMediaProjection()) {
            try {
                createScreenCastRecorder(mFileManager);
            } catch (Exception e) {
                // ignore.
            }
        }

        for (HostMediaRecorder recorder : mRecorders) {
            recorder.setOnEventListener(new HostMediaRecorder.OnEventListener() {
                @Override
                public void onMuteChanged(boolean mute) {
                    postOnMuteChanged(recorder, mute);
                }

                @Override
                public void onConfigChanged() {
                    postOnConfigChanged(recorder);
                }

                @Override
                public void onPreviewStarted(List<LiveStreaming> servers) {
                    postOnPreviewStarted(recorder, servers);
                }

                @Override
                public void onPreviewStopped() {
                    postOnPreviewStopped(recorder);
                }

                @Override
                public void onPreviewError(Exception e) {
                    postOnPreviewError(recorder, e);
                }

                @Override
                public void onBroadcasterStarted(List<LiveStreaming> broadcasters) {
                    postOnBroadcasterStarted(recorder, broadcasters);
                }

                @Override
                public void onBroadcasterStopped() {
                    postOnBroadcasterStopped(recorder);
                }

                @Override
                public void onBroadcasterError(LiveStreaming broadcaster, Exception e) {
                    postOnBroadcasterError(recorder, broadcaster, e);
                }

                @Override
                public void onError(Exception e) {
                    postOnError(recorder, e);
                }

                @Override
                public void onTakePhoto(String uri, String filePath, String mimeType) {
                    postOnTakePhoto(recorder, uri, filePath, mimeType);
                }

                @Override
                public void onRecordingStarted(String fileName) {
                    postOnRecordingStarted(recorder, fileName);
                }

                @Override
                public void onRecordingPause() {
                    postOnRecordingPause(recorder);
                }

                @Override
                public void onRecordingResume() {
                    postOnRecordingResume(recorder);
                }

                @Override
                public void onRecordingStopped(String fileName) {
                    postOnRecordingStopped(recorder, fileName);
                }
            });
        }
    }

    /**
     * 音声用の HostMediaRecorder を作成します.
     *
     * @param fileMgr ファイル管理クラス
     */
    private void createAudioRecorders(final FileManager fileMgr) {
        mRecorders.add(new HostAudioRecorder(getContext(), fileMgr, mMediaProjectionProvider));
    }

    /**
     * 画面キャプチャー用の HostMediaRecorder を作成します.
     *
     * @param fileMgr ファイル管理クラス
     */
    private void createScreenCastRecorder(final FileManager fileMgr) {
        mRecorders.add(new ScreenCastRecorder(getContext(), fileMgr, mMediaProjectionProvider));
    }

    /**
     * カメラ用の HostMediaRecorder を作成します.
     *
     * @param cameraMgr カメラ管理クラス
     * @param fileMgr ファイル管理クラス
     */
    private void createCameraRecorders(final CameraWrapperManager cameraMgr, final FileManager fileMgr) {
        List<Camera2Recorder> photoRecorders = new ArrayList<>();
        for (CameraWrapper camera : cameraMgr.getCameraList()) {
            photoRecorders.add(new Camera2Recorder(getContext(), camera, fileMgr, mMediaProjectionProvider));
        }
        mRecorders.addAll(photoRecorders);

        // デフォルトになるレコーダを設定
        if (!photoRecorders.isEmpty()) {
            mDefaultPhotoRecorder = photoRecorders.get(0);
        }
    }

    /**
     * 初期化処理を行います.
     */
    public void initialize() {
        for (HostMediaRecorder recorder : getRecorders()) {
            recorder.initialize();
        }
        onDisplayRotationChanged(getContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(ACTION_STOP_PREVIEW);
        filter.addAction(ACTION_STOP_RECORDING);
        filter.addAction(ACTION_STOP_BROADCAST);
        getContext().registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * レコーダのクリア処理を行います.
     */
    public void clean() {
        for (HostMediaRecorder recorder : getRecorders()) {
            recorder.clean();
        }
        mMediaProjectionProvider.stop();
    }

    /**
     * レコーダの破棄処理を行います.
     */
    public void destroy() {
        // 登録されていない BroadcastReceiver を解除すると例外が発生するので、try-catchしておく。
        try {
            getContext().unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            // ignore.
        }

        mOnEventListeners.clear();

        clean();

        for (HostMediaRecorder recorder : getRecorders()) {
            recorder.destroy();
        }
        mCameraWrapperManager.destroy();
    }

    /**
     * 端末が対応しているレコーダを読み込みし直す
     */
    public void reloadRecorders() {
        destroy();
        mRecorders.clear();
        initRecorders();
    }
    /**
     * 指定されたレコーダが使用できるか確認します.
     *
     * @param recorder 使用できるか確認するレコーダ
     * @return 使用できる場合はtrue、それ以外はfalse
     */
    public boolean canUseRecorder(HostMediaRecorder recorder) {
        if (recorder instanceof Camera2Recorder) {
            for (HostMediaRecorder cameraRecorder: getCameraRecorders()) {
                if (recorder == cameraRecorder) {
                    continue;
                }

                if (cameraRecorder.isPreviewRunning() || cameraRecorder.isBroadcasterRunning() ||
                        cameraRecorder.getState() == HostMediaRecorder.State.RECORDING) {
                    // カメラが使用中
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * カメラ用レコーダで、使用するレコーダ以外のカメラ用レコーダを停止します。
     *
     * プレビュー配信中などの停止できない場合には、停止しません。
     * その場合には、カメラを使用できないので注意が必要になります。
     *
     * @param useRecorder 使用するレコーダ
     */
    public void stopCameraRecorder(HostMediaRecorder useRecorder) {
        for (HostMediaRecorder recorder : getRecorders()) {
            if (recorder == useRecorder) {
                continue;
            }

            if (recorder instanceof Camera2Recorder) {
                if (!recorder.isPreviewRunning() && !recorder.isBroadcasterRunning() &&
                    recorder.getState() != HostMediaRecorder.State.RECORDING) {
                    // 強制的にカメラを停止
                    EGLSurfaceDrawingThread drawingThread = recorder.getSurfaceDrawingThread();
                    drawingThread.stop(true);

                    // カメラの処理は別スレッドで行われているので、ここで少し待機します
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // ignore.
                    }
                }
            }
        }
    }

    /**
     * レコーダの配列を取得します.
     *
     * @return レコーダの配列
     */
    public synchronized HostMediaRecorder[] getRecorders() {
        return mRecorders.toArray(new HostMediaRecorder[0]);
    }

    /**
     * カメラ用レコーダのリストを取得します.
     *
     * @return カメラ用レコーダの配列
     */
    public List<Camera2Recorder> getCameraRecorders() {
        List<Camera2Recorder> recorders = new ArrayList<>();
        for (HostMediaRecorder recorder : getRecorders()) {
            if (recorder instanceof Camera2Recorder) {
                recorders.add((Camera2Recorder) recorder);
            }
        }
        return recorders;
    }

    /**
     * 指定された ID に対応するレコーダを取得します.
     *
     * <p>
     * id に null が指定された場合には、デフォルトに設定されているレコーダを返却します。
     * </p>
     *
     * 指定された ID に対応するレコーダが存在しない場合には null を返却します。
     *
     * @param id レコーダの識別子
     * @return レコーダ
     */
    public HostMediaRecorder getRecorder(final String id) {
        if (mRecorders.size() == 0) {
            return null;
        }
        if (id == null) {
            if (mDefaultPhotoRecorder != null) {
                return mDefaultPhotoRecorder;
            }
            return mRecorders.get(0);
        }
        for (HostMediaRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId())) {
                return recorder;
            }
        }
        return null;
    }

    /**
     * 指定された ID に対応する静止画用のレコーダを取得します.
     *
     * <p>
     * id に null が指定された場合には、デフォルトに設定されているレコーダを返却します。
     * </p>
     *
     * 指定された ID に対応する静止画用のレコーダが存在しない場合には null を返却します。
     *
     * @param id  レコーダの識別子
     * @return レコーダ
     */
    public HostDevicePhotoRecorder getCameraRecorder(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
        }
        for (HostMediaRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId())) {
                return recorder;
            }
        }
        return null;
    }

    /**
     * 指定された ID のレコーダのプレビューを停止します.
     *
     * @param id レコーダの ID
     */
    private void stopPreviewServer(final String id) {
        if (id == null) {
            return;
        }

        HostMediaRecorder recorder = getRecorder(id);
        if (recorder != null) {
            recorder.stopPreview();
        }
    }

    /**
     * 指定された ID のレコードの録音・録画を停止します.
     *
     * @param id レコードの ID
     */
    private void stopRecording(final String id) {
        if (id == null) {
            return;
        }

        HostMediaRecorder recorder = getRecorder(id);
        if (recorder != null) {
            recorder.stopRecording(null);
        }
    }

    /**
     * 指定された ID のレコードのブロードキャストを停止します.
     *
     * @param id レコードの ID
     */
    private void stopBroadcast(final String id) {
        if (id == null) {
            return;
        }

        HostMediaRecorder recorder = getRecorder(id);
        if (recorder != null) {
            recorder.stopBroadcaster();
        }
    }

    /**
     * 画面が回転されたときの処理を行います.
     *
     * @param context コンテキスト
     */
    private void onDisplayRotationChanged(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            int rotation = windowManager.getDefaultDisplay().getRotation();
            for (HostMediaRecorder recorder : mRecorders) {
                try {
                    recorder.onDisplayRotation(rotation);
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
    }

    /**
     * MediaProjection がサポートされている確認します.
     *
     * @return MediaProjection がサポートされている場合はtrue、それ以外はfalse
     */
    public static boolean isSupportedMediaProjection() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * カメラを端末がサポートしているかチェックします.
     *
     * @return カメラをサポートしている場合はtrue、それ以外はfalse
     */
    private static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * マイク入力を端末がサポートしているかチェックします.
     *
     * @return マイク入力をサポートしている場合はtrue、それ以外はfalse
     */
    private static boolean checkMicrophone(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    /**
     * MediaProjection APIを端末がサポートしているかチェックします.
     *
     * @return MediaProjection APIをサポートしている場合はtrue、それ以外はfalse
     */
    private static boolean checkMediaProjection() {
        return HostMediaRecorderManager.isSupportedMediaProjection();
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

    private void postOnMuteChanged(HostMediaRecorder recorder, boolean mute) {
        for (OnEventListener l : mOnEventListeners) {
            l.onMuteChanged(recorder, mute);
        }
    }

    private void postOnConfigChanged(HostMediaRecorder recorder) {
        for (OnEventListener l : mOnEventListeners) {
            l.onConfigChanged(recorder);
        }
    }

    private void postOnPreviewStarted(HostMediaRecorder recorder, List<LiveStreaming> servers) {
        for (OnEventListener l : mOnEventListeners) {
            l.onPreviewStarted(recorder, servers);
        }
    }

    private void postOnPreviewStopped(HostMediaRecorder recorder) {
        for (OnEventListener l : mOnEventListeners) {
            l.onPreviewStopped(recorder);
        }
    }

    private void postOnPreviewError(HostMediaRecorder recorder, Exception e) {
        for (OnEventListener l : mOnEventListeners) {
            l.onPreviewError(recorder, e);
        }
    }

    private void postOnBroadcasterStarted(HostMediaRecorder recorder, List<LiveStreaming> broadcasters) {
        for (OnEventListener l : mOnEventListeners) {
            l.onBroadcasterStarted(recorder, broadcasters);
        }
    }

    private void postOnBroadcasterStopped(HostMediaRecorder recorder) {
        for (OnEventListener l : mOnEventListeners) {
            l.onBroadcasterStopped(recorder);
        }
    }

    private void postOnBroadcasterError(HostMediaRecorder recorder, LiveStreaming broadcaster, Exception e) {
        for (OnEventListener l : mOnEventListeners) {
            l.onBroadcasterError(recorder, broadcaster, e);
        }
    }

    private void postOnTakePhoto(HostMediaRecorder recorder, String uri, String filePath, String mimeType) {
        for (OnEventListener l : mOnEventListeners) {
            l.onTakePhoto(recorder, uri, filePath, mimeType);
        }
    }

    private void postOnRecordingStarted(HostMediaRecorder recorder, String fileName) {
        for (OnEventListener l : mOnEventListeners) {
            l.onRecordingStarted(recorder, fileName);
        }
    }

    private void postOnRecordingPause(HostMediaRecorder recorder) {
        for (OnEventListener l : mOnEventListeners) {
            l.onRecordingPause(recorder);
        }
    }

    private void postOnRecordingResume(HostMediaRecorder recorder) {
        for (OnEventListener l : mOnEventListeners) {
            l.onRecordingResume(recorder);
        }
    }

    private void postOnRecordingStopped(HostMediaRecorder recorder, String fileName) {
        for (OnEventListener l : mOnEventListeners) {
            l.onRecordingStopped(recorder, fileName);
        }
    }

    private void postOnError(HostMediaRecorder recorder, Exception e) {
        for (OnEventListener l : mOnEventListeners) {
            l.onError(recorder, e);
        }
    }

    public interface OnEventListener {
        void onMuteChanged(HostMediaRecorder recorder, boolean mute);
        void onConfigChanged(HostMediaRecorder recorder);

        void onPreviewStarted(HostMediaRecorder recorder, List<LiveStreaming> servers);
        void onPreviewStopped(HostMediaRecorder recorder);
        void onPreviewError(HostMediaRecorder recorder, Exception e);

        void onBroadcasterStarted(HostMediaRecorder recorder, List<LiveStreaming> broadcasters);
        void onBroadcasterStopped(HostMediaRecorder recorder);
        void onBroadcasterError(HostMediaRecorder recorder, LiveStreaming broadcaster, Exception e);

        void onTakePhoto(HostMediaRecorder recorder, String uri, String filePath, String mimeType);

        void onRecordingStarted(HostMediaRecorder recorder, String fileName);
        void onRecordingPause(HostMediaRecorder recorder);
        void onRecordingResume(HostMediaRecorder recorder);
        void onRecordingStopped(HostMediaRecorder recorder, String fileName);

        void onError(HostMediaRecorder recorder, Exception e);
    }
}
