package org.deviceconnect.android.deviceplugin.uvc.service;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.uvc.profile.UVCMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorderManager;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.mjpeg.UvcMjpgRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uncompressed.UvcUncompressedRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.service.DConnectService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UVCService extends DConnectService {
    private final Context mContext;
    private MediaRecorderManager mMediaRecorderManager;

    public UVCService(Context context, String serviceId) {
        super(serviceId);
        mContext = context;

        setOnline(false);
        setNetworkType(NetworkType.UNKNOWN);

        addProfile(new UVCMediaStreamRecordingProfile());
    }

    /**
     * UVC に接続した時の処理を行います.
     *
     * @param camera UVC デバイス
     */
    public synchronized void connect(UVCCamera camera) {
        setName(camera.getDeviceName());
        setOnline(true);

        if (mMediaRecorderManager != null) {
            mMediaRecorderManager.destroy();
        }
        mMediaRecorderManager = new MediaRecorderManager(mContext, camera);
    }

    /**
     * UVC が切断された時の処理を行います.
     */
    public synchronized void disconnect() {
        setOnline(false);
        if (mMediaRecorderManager != null) {
            mMediaRecorderManager.destroy();
            mMediaRecorderManager = null;
        }
    }

    public synchronized MediaRecorderManager getMediaRecorderManager() {
        return mMediaRecorderManager;
    }

    /**
     * レコーダのリストを取得します.
     *
     * @return レコーダ
     */
    public List<UvcRecorder> getUvcRecorderList() {
        return mMediaRecorderManager != null ? mMediaRecorderManager.getUvcRecorderList() : new ArrayList<>();
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
        return mMediaRecorderManager != null ? mMediaRecorderManager.findUvcRecorderById(id) : null;
    }

    public UvcRecorder getDefaultRecorder() {
        return mMediaRecorderManager != null ? mMediaRecorderManager.getDefaultRecorder() : null;
    }
}
