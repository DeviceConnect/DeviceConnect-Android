package org.deviceconnect.android.deviceplugin.uvc.service;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.uvc.profile.UVCMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.service.DConnectService;

import java.io.IOException;
import java.util.List;

public class UVCService extends DConnectService {
    private final UvcH264Recorder mUvcRecorder;

    public UVCService(Context context, UVCCamera camera) {
        super(camera.getDeviceName());

        try {
            List<Parameter> parameters = camera.getParameter();
            for (Parameter p : parameters) {
                Log.e("ABC", " [" + p.hasExtH264() + "] " + p.getWidth() + "x" + p.getHeight());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mUvcRecorder = new UvcH264Recorder(context, camera);

        setOnline(true);
        setNetworkType(NetworkType.UNKNOWN);

        addProfile(new UVCMediaStreamRecordingProfile());
    }

    public UvcH264Recorder getUvcRecorder() {
        return mUvcRecorder;
    }
}
