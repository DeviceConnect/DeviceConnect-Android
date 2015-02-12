/*
 HeartRateHealthProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.profile;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.profile.DConnectProfile;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateHealthProfile extends DConnectProfile {
    /**
     * Implementation of OnHeartRateEventListener.
     */
    private HeartRateManager.OnHeartRateEventListener mHeartRateEventListener =
            new HeartRateManager.OnHeartRateEventListener() {
                @Override
                public void onReceivedData(HeartRateDevice device, HeartRateData data) {
                }
            };

    @Override
    public String getProfileName() {
        return "health";
    }
}
