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

    private ChromeCastMediaPlayerProfile mMediaPlayerProfile;

    public ChromeCastDeviceService(final String ip) {
        super(ip);
        setName(getDeviceName(ip));
        setNetworkType(ServiceDiscoveryProfileConstants.NetworkType.WIFI);
        mMediaPlayerProfile = new ChromeCastMediaPlayerProfile();
        addProfile(new ChromeCastCanvasProfile());
        addProfile(new ChromeCastNotificationProfile());
        addProfile(mMediaPlayerProfile);
    }

    public ChromeCastMediaPlayerProfile getMediaPlayerProfile() {
        return mMediaPlayerProfile;
    }
    private String getDeviceName(final String name) {
        return String.format("Chromecast (%1$s)", name);
    }

}
