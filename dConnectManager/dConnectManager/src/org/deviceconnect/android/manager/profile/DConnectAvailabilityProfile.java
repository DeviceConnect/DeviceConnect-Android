/*
 DConnectAvailabilityProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AvailabilityProfileConstants;

import android.content.Intent;

/**
 * Availability Profile.
 * 
 * @author NTT DOCOMO, Inc.
 */
public class DConnectAvailabilityProfile extends DConnectProfile implements AvailabilityProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        ((DConnectService) getContext()).sendResponse(request, response);
        return true;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        MessageUtils.setNotSupportActionError(response);
        ((DConnectService) getContext()).sendResponse(request, response);
        return true;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        MessageUtils.setNotSupportActionError(response);
        ((DConnectService) getContext()).sendResponse(request, response);
        return true;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        MessageUtils.setNotSupportActionError(response);
        ((DConnectService) getContext()).sendResponse(request, response);
        return true;
    }

}
