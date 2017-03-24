package org.deviceconnect.android.deviceplugin.sonycamera.service;

import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraZoomProfile;
import org.deviceconnect.android.service.DConnectService;

public class SonyCameraService extends DConnectService {

    /** デバイス名. */
    public static final String DEVICE_NAME = "Sony Camera ";

    private String mPassword;

    public SonyCameraService(final String id) {
        super(id);

        setName(DEVICE_NAME + id);
        setNetworkType(NetworkType.WIFI);

        addProfile(new SonyCameraMediaStreamRecordingProfile());
        addProfile(new SonyCameraZoomProfile());
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }
}
