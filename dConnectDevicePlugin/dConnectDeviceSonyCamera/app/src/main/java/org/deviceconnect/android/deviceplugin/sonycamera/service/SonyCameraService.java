package org.deviceconnect.android.deviceplugin.sonycamera.service;


import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraZoomProfile;
import org.deviceconnect.android.service.DConnectService;

public class SonyCameraService extends DConnectService {

    /** デバイス名. */
    public static final String DEVICE_NAME = "Sony Camera";
    /** サービスID. */
    public static final String SERVICE_ID = "sony_camera";

    public SonyCameraService() {
        super(SERVICE_ID);
        setName(DEVICE_NAME);
        setNetworkType(NetworkType.WIFI);

        addProfile(new SonyCameraMediaStreamRecordingProfile());
        addProfile(new SonyCameraZoomProfile());
    }

}
