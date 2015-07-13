package org.deviceconnect.android.deviceplugin.alljoyn;

import android.os.Build;
import android.os.Debug;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynLightProfile;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * AllJoynデバイスプラグインサービス。
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynDeviceService extends DConnectMessageService
//        implements AboutListener
{

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
        return new ServiceInformationProfile(this) {
        };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new AllJoynServiceDiscoveryProfile(this);
    }

}
