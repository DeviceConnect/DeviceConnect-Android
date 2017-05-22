package org.deviceconnect.android.deviceplugin.hvcc2w.service;


import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcc2w.profile.HVCC2WHumanDetectionProfile;
import org.deviceconnect.android.service.DConnectService;

public class HVCC2WService extends DConnectService {

    public HVCC2WService(final HVCCameraInfo camera) {
        super(camera.getID());
        setName(camera.getName() + "(" + camera.getID() + ")");
        setNetworkType(NetworkType.WIFI);

        addProfile(new HVCC2WHumanDetectionProfile());
    }
}
