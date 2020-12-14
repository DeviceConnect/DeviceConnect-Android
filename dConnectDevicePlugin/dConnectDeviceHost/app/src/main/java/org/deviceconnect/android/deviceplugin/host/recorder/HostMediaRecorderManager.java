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
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
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
    private FileManager mFileManager;

    /**
     * 画面の回転イベントを受け取るための BroadcastReceiver.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
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
            } else if (PreviewServerProvider.DELETE_PREVIEW_ACTION.equals(action)) {
                stopPreviewServer(intent.getStringExtra(PreviewServerProvider.EXTRA_CAMERA_ID));
            }
        }
    };

    public HostMediaRecorderManager(final DevicePluginContext pluginContext, final FileManager fileManager) {
        mHostDevicePluginContext = pluginContext;
        mFileManager = fileManager;
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
    public void initRecorders() {
        if (checkCameraHardware()) {
            mCameraWrapperManager = new CameraWrapperManager(getContext());
            createCameraRecorders(mCameraWrapperManager, mFileManager);
        }

        if (checkMicrophone()) {
            createAudioRecorders();
        }

        if (checkMediaProjection()) {
            createScreenCastRecorder(mFileManager);
        }

        try {
            initRecorderSetting();
        } catch (Exception e) {
            // TODO レコーダの初期化に失敗した場合の処理
        }
    }

    private void createAudioRecorders() {
        mRecorders.add(new HostAudioRecorder(getContext()));
    }

    private void createScreenCastRecorder(final FileManager fileMgr) {
        mRecorders.add(new ScreenCastRecorder(getContext(), fileMgr));
    }

    private void createCameraRecorders(final CameraWrapperManager cameraMgr, final FileManager fileMgr) {
        List<Camera2Recorder> photoRecorders = new ArrayList<>();
        for (CameraWrapper camera : cameraMgr.getCameraList()) {
            photoRecorders.add(new Camera2Recorder(getContext(), camera, fileMgr));
        }
        mRecorders.addAll(photoRecorders);
        if (!photoRecorders.isEmpty()) {
            mDefaultPhotoRecorder = photoRecorders.get(0);
        }
    }

    private void initRecorderSetting() {
        RecorderSetting setting = RecorderSetting.getInstance(getContext().getApplicationContext());
        List<RecorderSetting.Target> targets = setting.getTargets();
        for (HostMediaRecorder recorder : mRecorders) {
            targets.add(new RecorderSetting.Target(recorder.getId(), recorder.getName(), recorder.getMimeType()));
        }
        setting.saveTargets(targets);
    }

    /**
     * 初期化処理を行います.
     * <p>
     */
    public void initialize() {
        for (HostMediaRecorder recorder : getRecorders()) {
            recorder.initialize();
        }
    }

    /**
     * 画面回転の監視を開始します.
     */
    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(PreviewServerProvider.DELETE_PREVIEW_ACTION);
        getContext().registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * 画面回転の監視を停止します.
     */
    public void stop() {
        // 登録されていない BroadcastReceiver を解除すると例外が発生するので、try-catchしておく。
        try {
            getContext().unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * レコーダのクリア処理を行います.
     */
    public void clean() {
        for (HostMediaRecorder recorder : getRecorders()) {
            recorder.clean();
        }
    }

    /**
     * レコーダの破棄処理を行います.
     */
    public void destroy() {
        stop();

        clean();

        for (HostMediaRecorder recorder : getRecorders()) {
            recorder.destroy();
        }
        mCameraWrapperManager.destroy();
    }

    /**
     * レコーダの配列を取得します.
     *
     * @return レコーダの配列
     */
    public synchronized HostMediaRecorder[] getRecorders() {
        return mRecorders.toArray(new HostMediaRecorder[mRecorders.size()]);
    }

    /**
     * 指定された ID に対応するレコーダを取得します.
     *
     * <p>
     * id に null が指定された場合には、デフォルトに設定されているレコーダを返却します。
     * </p>
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
     * 指定された ID 以外のレコーダが.
     *
     * <p>
     * id に null が指定された場合には、デフォルトに設定されているレコーダを返却します。
     * </p>
     *
     * @param id レコーダの識別子
     * @return 他のレコーダーが使用中であるかどうか true:使用中 false:使用されていない
     */
    public boolean usingPreviewOrStreamingRecorder(String id) {
        if (mRecorders.size() == 0) {
            return false;
        }
        if (id == null) {
            if (mDefaultPhotoRecorder != null) {
                id = mDefaultPhotoRecorder.getId();
            } else {
                id = mRecorders.get(0).getId();
            }
        }
        for (HostMediaRecorder recorder : mRecorders) {
            if (!id.equals(recorder.getId())
                    && (recorder.getState() == HostMediaRecorder.State.PREVIEW
                        || recorder.getState() == HostMediaRecorder.State.RECORDING)) {
                return true;
            } else if (recorder instanceof HostDeviceLiveStreamRecorder
                    && ((HostDeviceLiveStreamRecorder) recorder).isStreaming()) {
                return true;
            }
        }
        return false;
    }

    /**
     * レコーダーが使用中である、あるいはストリーミングが開始している場合はtrueを返す.
     *
     * @return true:使用中 false:使用されていない
     */
    public boolean usingStreamingRecorder() {
        for (HostMediaRecorder recorder : mRecorders) {
            return recorder.getState() == HostMediaRecorder.State.PREVIEW
                    || recorder.getState() == HostMediaRecorder.State.RECORDING
                    || ((HostDeviceLiveStreamRecorder) recorder).isStreaming();
        }
        return false;
    }

    /**
     * 指定された ID に対応する静止画用のレコーダを取得します.
     *
     * <p>
     * id に null が指定された場合には、デフォルトに設定されているレコーダを返却します。
     * </p>
     *
     * @param id  レコーダの識別子
     * @return レコーダ
     */
    public HostDevicePhotoRecorder getCameraRecorder(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
        }
        for (HostMediaRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDevicePhotoRecorder) {
                return (HostDevicePhotoRecorder) recorder;
            }
        }
        return null;
    }

    /**
     * 指定された ID に対応する録画・録音用のレコーダを取得します.
     *
     * <p>
     * id に null が指定された場合には、デフォルトに設定されているレコーダを返却します。
     * </p>
     *
     * @param id  レコーダの識別子
     * @return レコーダ
     */
    public HostDeviceStreamRecorder getStreamRecorder(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
        }
        for (HostMediaRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDeviceStreamRecorder) {
                return (HostDeviceStreamRecorder) recorder;
            }
        }
        return null;
    }

    /**
     * 指定された ID のレコーダのプレビューを停止します.
     *
     * @param id レコーダのID
     */
    public void stopPreviewServer(final String id) {
        if (id == null) {
            return;
        }

        HostMediaRecorder recorder = getRecorder(id);
        if (recorder != null) {
            PreviewServerProvider provider = recorder.getServerProvider();
            if (provider != null) {
                provider.stopServers();
            }
        }
    }

    public static boolean isSupportedMediaProjection() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * カメラを端末がサポートしているかチェックします.
     *
     * @return カメラをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkCameraHardware() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * マイク入力を端末がサポートしているかチェックします.
     *
     * @return マイク入力をサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkMicrophone() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    /**
     * MediaProjection APIを端末がサポートしているかチェックします.
     *
     * @return MediaProjection APIをサポートしている場合はtrue、それ以外はfalse
     */
    private boolean checkMediaProjection() {
        return HostMediaRecorderManager.isSupportedMediaProjection();
    }
}
