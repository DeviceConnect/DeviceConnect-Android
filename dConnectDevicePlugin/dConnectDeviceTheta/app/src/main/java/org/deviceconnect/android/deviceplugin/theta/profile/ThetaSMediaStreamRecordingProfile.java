package org.deviceconnect.android.deviceplugin.theta.profile;


import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.provider.FileManager;

public class ThetaSMediaStreamRecordingProfile extends ThetaMediaStreamRecordingProfile {

    public ThetaSMediaStreamRecordingProfile(final ThetaDeviceClient client,
                                             final FileManager fileMgr) {
        super(client, fileMgr);
        addApi(mPutPreviewApi);
        addApi(mDeletePreviewApi);
    }
}
