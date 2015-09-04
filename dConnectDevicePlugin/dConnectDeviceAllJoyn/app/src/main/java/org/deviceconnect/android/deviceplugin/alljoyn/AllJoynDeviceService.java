package org.deviceconnect.android.deviceplugin.alljoyn;

import android.os.Debug;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynLightProfile;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * Device Connect device plug-in for AllJoyn.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(), "started");
        }

        addProfile(new AllJoynLightProfile());
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new AllJoynSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new AllJoynServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new AllJoynServiceDiscoveryProfile(this);
    }

}
