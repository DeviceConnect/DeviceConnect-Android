package org.deviceconnect.android.deviceplugin.theta.service;


import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaBatteryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaFileProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaM15MediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaSMediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;


public class ThetaService extends DConnectService {

    public ThetaService(final ThetaDevice device,
                        final ThetaDeviceClient client,
                        final FileManager fileMgr) {
        super(device.getId());
        setName(device.getName());
        setNetworkType(NetworkType.WIFI);
        addProfile(new ThetaBatteryProfile(client));
        addProfile(new ThetaFileProfile(client, fileMgr));
        switch (device.getModel()) {
            case THETA_S:
            case THETA_V:
                addProfile(new ThetaSMediaStreamRecordingProfile(client, fileMgr));
                break;
            case THETA_M15:
                addProfile(new ThetaM15MediaStreamRecordingProfile(client, fileMgr));
                break;
            default:
                break;
        }
    }

}
