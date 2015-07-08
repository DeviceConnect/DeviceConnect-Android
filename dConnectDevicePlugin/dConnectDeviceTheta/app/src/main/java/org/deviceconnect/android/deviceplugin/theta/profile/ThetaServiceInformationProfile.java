package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * Theta Service Information Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaServiceInformationProfile extends ServiceInformationProfile {

    public ThetaServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    public boolean onRequest(Intent request, Intent response) {
        return super.onRequest(request, response);
    }

}
