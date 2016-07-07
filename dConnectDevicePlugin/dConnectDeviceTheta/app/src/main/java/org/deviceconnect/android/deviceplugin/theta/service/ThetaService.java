package org.deviceconnect.android.deviceplugin.theta.service;


import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTracker;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaBatteryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaFileProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaOmnidirectionalImageProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;

public class ThetaService extends DConnectService {

    public ThetaService(final ThetaDevice device,
                        final ThetaDeviceClient client,
                        final FileManager fileMgr,
                        final HeadTracker headTracker) {
        super(device.getId());
        setName(device.getName());
        setNetworkType(NetworkType.WIFI);
        addProfile(new ThetaBatteryProfile(client));
        addProfile(new ThetaFileProfile(client, fileMgr));
        addProfile(new ThetaMediaStreamRecordingProfile(client, fileMgr));
        addProfile(new ThetaOmnidirectionalImageProfile(headTracker));
    }

}
