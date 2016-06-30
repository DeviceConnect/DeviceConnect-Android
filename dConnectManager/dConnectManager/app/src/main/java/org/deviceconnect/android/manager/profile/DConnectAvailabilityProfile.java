/*
 DConnectAvailabilityProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AvailabilityProfileConstants;

/**
 * Availability Profile.
 * 
 * @author NTT DOCOMO, Inc.
 */
public class DConnectAvailabilityProfile extends DConnectProfile implements AvailabilityProfileConstants {

    public DConnectAvailabilityProfile() {
        addApi(mGetRequest);
    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    private final DConnectApi mGetRequest = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
}
