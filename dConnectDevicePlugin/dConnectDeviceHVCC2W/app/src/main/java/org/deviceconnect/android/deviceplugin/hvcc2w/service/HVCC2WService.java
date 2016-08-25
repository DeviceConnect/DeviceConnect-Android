package org.deviceconnect.android.deviceplugin.hvcc2w.service;


import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcc2w.profile.HVCC2WHumanDetectProfile;
import org.deviceconnect.android.service.DConnectService;

public class HVCC2WService extends DConnectService {

    public HVCC2WService(final HVCCameraInfo camera) {
        super(camera.getID());
        setName(camera.getName());
        setNetworkType(NetworkType.WIFI);

        addProfile(new HVCC2WHumanDetectProfile());
    }
}
