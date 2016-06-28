package com.mtaka.newdeviceconnectpluginsdksample;


import com.mtaka.newdeviceconnectpluginsdksample.profile.SampleCanvasProfile;

import org.deviceconnect.android.profile.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectServiceEndPoint;

public class SampleService extends DConnectMessageService {


    private void onDeviceConnected(final String serviceId) {
        // TODO: 実際にはプラグインごとにデバイス検知処理は変わる. 下記はあくまでもイメージ.

        DConnectServiceEndPoint service = new DConnectServiceEndPoint(serviceId);
        service.addApi(SampleCanvasProfile.DRAW_IMAGE_API);
        service.addApi(SampleCanvasProfile.DELETE_IMAGE_API);
        addService(service);
    }

    private void onDeviceDisconnected(final String serviceId) {
        removeService(serviceId);
    }

}
