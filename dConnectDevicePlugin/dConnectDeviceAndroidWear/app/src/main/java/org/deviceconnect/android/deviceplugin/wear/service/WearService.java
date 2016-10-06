package org.deviceconnect.android.deviceplugin.wear.service;


import com.google.android.gms.wearable.Node;

import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.profile.WearCanvasProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearConst;
import org.deviceconnect.android.deviceplugin.wear.profile.WearDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearKeyEventProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearNotificationProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearTouchProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearUtils;
import org.deviceconnect.android.deviceplugin.wear.profile.WearVibrationProfile;
import org.deviceconnect.android.service.DConnectService;

public class WearService extends DConnectService {

    private WearService(final String id) {
        super(id);
    }

    public static WearService getInstance(final Node node, final WearManager mgr) {
        String nodeId = node.getId();
        String[] serviceId = nodeId.split("-");

        WearService service = new WearService(WearUtils.createServiceId(nodeId));
        service.setName(WearConst.DEVICE_NAME + "(" + serviceId[0] + ")");
        service.setNetworkType(NetworkType.BLE);
        service.addProfile(new WearCanvasProfile(mgr));
        service.addProfile(new WearDeviceOrientationProfile(mgr));
        service.addProfile(new WearKeyEventProfile(mgr));
        service.addProfile(new WearNotificationProfile());
        service.addProfile(new WearTouchProfile(mgr));
        service.addProfile(new WearVibrationProfile());
        return service;
    }
}
