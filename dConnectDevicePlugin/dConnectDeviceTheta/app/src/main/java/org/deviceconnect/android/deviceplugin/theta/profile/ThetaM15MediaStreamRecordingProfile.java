package org.deviceconnect.android.deviceplugin.theta.profile;


import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.provider.FileManager;

public class ThetaM15MediaStreamRecordingProfile extends ThetaMediaStreamRecordingProfile {

    public ThetaM15MediaStreamRecordingProfile(final ThetaDeviceClient client,
                                               final FileManager fileMgr) {
        super(client, fileMgr);
    }
}
