package org.deviceconnect.android.deviceplugin.uvc.service;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.UVCRecorder;
import org.deviceconnect.android.service.DConnectService;

public class UVCService extends DConnectService {
    private UVCDeviceManager mDeviceManager;
    private UVCRecorder mUVCRecorder;

    public UVCService(UVCDeviceManager deviceMgr, UVCDevice device) {
        super(device.getId());

        mDeviceManager = deviceMgr;

        setName("UVC: " + device.getName());
        setOnline(false);
        setNetworkType(NetworkType.UNKNOWN);
        addProfile(new UVCMediaStreamRecordingProfile());
    }

    public UVCRecorder getUVCRecorder() {
        return mUVCRecorder;
    }

    public void openUVCDevice(UVCDevice device) {
        mUVCRecorder = new UVCRecorder(mDeviceManager, device);
        mUVCRecorder.initialize();
    }

    public void closeUVCDevice() {
        mUVCRecorder.clean();
    }

    public void reset() {
        if (mUVCRecorder != null) {
            mUVCRecorder.stopPreview();
        }
    }
}
