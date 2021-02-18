package org.deviceconnect.android.deviceplugin.uvc.service;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.uvc.profile.UVCMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
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
    private final List<UvcRecorder> mUvcRecorderList = new ArrayList<>();
    private final Context mContext;

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
        initRecorders(mContext, camera);
    }

    /**
     * UVC が切断された時の処理を行います.
     */
    public synchronized void disconnect() {
        setOnline(false);
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
            Log.d("ABC", "UVCCamera: " + camera.getDeviceName());
            Log.d("ABC", "DeviceId: " + camera.getDeviceId());
            List<Parameter> parameters = camera.getParameter();
            for (Parameter p : parameters) {
                Log.d("ABC", p.getFrameType() + " [" + p.hasExtH264() + "]: " + p.getWidth() + "x" + p.getHeight());
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
            mUvcRecorderList.add(new UvcMjpgRecorder(context, camera));
        }

        if (hasH264) {
            mUvcRecorderList.add(new UvcH264Recorder(context, camera));
        }

        if (hasUncompressed) {
            mUvcRecorderList.add(new UvcUncompressedRecorder(context, camera));
        }

        for (MediaRecorder recorder : mUvcRecorderList) {
            recorder.initialize();
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

    public UvcRecorder getDefaultRecorder() {
        if (mUvcRecorderList.isEmpty()) {
            return null;
        }
        return mUvcRecorderList.get(0);
    }
}
