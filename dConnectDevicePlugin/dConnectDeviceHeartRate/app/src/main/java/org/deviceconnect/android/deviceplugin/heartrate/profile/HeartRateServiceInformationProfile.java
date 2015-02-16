/*
 HeartRateServiceInformationProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.profile;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateServiceInformationProfile extends ServiceInformationProfile {
    /**
     * Constructor.
     * @param provider profile provider
     */
    public HeartRateServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }
}
