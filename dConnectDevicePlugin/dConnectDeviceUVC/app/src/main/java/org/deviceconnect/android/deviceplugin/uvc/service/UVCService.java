package org.deviceconnect.android.deviceplugin.uvc.service;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.uvc.profile.UVCMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.mjpeg.UvcMjpgRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.service.DConnectService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UVCService extends DConnectService {
    private final List<UvcRecorder> mUvcRecorderList = new ArrayList<>();

    public UVCService(Context context, UVCCamera camera) {
        super("UVC-" + camera.getDeviceId());

        initRecorders(context, camera);

        setName(camera.getDeviceName());
        setOnline(true);
        setNetworkType(NetworkType.UNKNOWN);

        addProfile(new UVCMediaStreamRecordingProfile());
    }

    private void initRecorders(Context context, UVCCamera camera) {
        boolean hasMJPEG = false;
        boolean hasH264 = false;
        try {
            List<Parameter> parameters = camera.getParameter();
            for (Parameter p : parameters) {
                Log.e("ABC", p.getFrameType() + " [" + p.hasExtH264() + "]: " + p.getWidth() + "x" + p.getHeight());
                switch (p.getFrameType()) {
                    case MJPEG:
                        hasMJPEG = true;
                        if (p.hasExtH264()) {
                            // Extension Unit を持っていない場合に H264 としては使用できない
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
    }

    public List<UvcRecorder> getUvcRecorderList() {
        return mUvcRecorderList;
    }

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
}
