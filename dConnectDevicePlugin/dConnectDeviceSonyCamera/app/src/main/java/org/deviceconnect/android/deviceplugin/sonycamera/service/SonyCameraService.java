package org.deviceconnect.android.deviceplugin.sonycamera.service;

import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraZoomProfile;
import org.deviceconnect.android.service.DConnectService;

public class SonyCameraService extends DConnectService {

    /** デバイス名. */
    public static final String DEVICE_NAME = "Sony Camera";

    /** サービスID. */
    public static final String SERVICE_ID = "sony_camera";

    /** 接続中カメラが利用できるRemote API一覧. */
    private String mAvailableApiList = "";

    public SonyCameraService() {
        this(SERVICE_ID);
    }

    public SonyCameraService(final String id) {
        super(id);

        setName(DEVICE_NAME);
        setNetworkType(NetworkType.WIFI);

        addProfile(new SonyCameraMediaStreamRecordingProfile());
        addProfile(new SonyCameraZoomProfile());
    }

    public void setAvailableApiList(final String availableApiList) {
        mAvailableApiList = availableApiList;
    }
}
