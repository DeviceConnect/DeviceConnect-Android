package org.deviceconnect.android.deviceplugin.chromecast;

import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastCanvasProfile;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastMediaPlayerProfile;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastNotificationProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

/**
 * Chromecast device service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastDeviceService extends DConnectService {


    public ChromeCastDeviceService(final String ip) {
        super(ip);
        setName(getDeviceName(ip));
        setNetworkType(ServiceDiscoveryProfileConstants.NetworkType.WIFI);

        addProfile(new ChromeCastCanvasProfile());
        addProfile(new ChromeCastNotificationProfile());
        addProfile(new ChromeCastMediaPlayerProfile());
    }
    private String getDeviceName(final String name) {
        return getContext().getResources().getString(R.string.device_name, name);
    }

}
