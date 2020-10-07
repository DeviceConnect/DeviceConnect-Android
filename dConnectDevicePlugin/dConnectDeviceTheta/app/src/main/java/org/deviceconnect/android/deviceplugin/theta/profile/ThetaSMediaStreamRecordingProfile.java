package org.deviceconnect.android.deviceplugin.theta.profile;


import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.provider.FileManager;

import javax.net.ssl.SSLContext;

public class ThetaSMediaStreamRecordingProfile extends ThetaMediaStreamRecordingProfile {

    public ThetaSMediaStreamRecordingProfile(final SSLContext sslContext,
                                             final ThetaDeviceClient client,
                                             final FileManager fileMgr) {
        super(sslContext, client, fileMgr);
        addApi(mPutPreviewApi);
        addApi(mDeletePreviewApi);
    }
}
