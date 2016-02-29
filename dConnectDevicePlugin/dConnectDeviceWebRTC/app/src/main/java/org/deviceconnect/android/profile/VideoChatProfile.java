/*
 VideoChatProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

/**
 * VideoChat Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class VideoChatProfile extends DConnectProfile implements VideoChatProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;
        if (ATTR_PROFILE.equals(attribute)) {
            result = onGetProfile(request, response);
        } else if (ATTR_ADDRESS.equals(attribute)) {
            result = onGetAddress(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;
        if (ATTR_CALL.equals(attribute)) {
            String addressId = request.getStringExtra(PARAM_ADDRESSID);
            String video = request.getStringExtra(PARAM_VIDEO);
            String audio = request.getStringExtra(PARAM_AUDIO);
            String outputs = request.getStringExtra(PARAM_OUTPUTS);
            String config = request.getStringExtra(PARAM_CONFIG);
            result = onPostCall(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;
        if (ATTR_PROFILE.equals(attribute)) {
            String name = request.getStringExtra(PARAM_NAME);
            String config = request.getStringExtra(PARAM_NAME);
            result = onPutProfile(request, response);
        } else if (ATTR_INCOMING.equals(attribute)) {
            result = onPutIncoming(request, response);
        } else if (ATTR_ONCALL.equals(attribute)) {
            result = onPutOnCall(request, response);
        } else if (ATTR_HANGUP.equals(attribute)) {
            result = onPutHangup(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;
        if (ATTR_CALL.equals(attribute)) {
            String addressId = request.getStringExtra(PARAM_ADDRESSID);
            String config = request.getStringExtra(PARAM_CONFIG);
            result = onDeleteCall(request, response);
        } else if (ATTR_INCOMING.equals(attribute)) {
            result = onDeleteIncoming(request, response);
        } else if (ATTR_ONCALL.equals(attribute)) {
            result = onDeleteOnCall(request, response);
        } else if (ATTR_HANGUP.equals(attribute)) {
            result = onDeleteHangup(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    protected boolean onGetProfile(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onGetAddress(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onPostCall(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onPutProfile(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onPutIncoming(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onPutOnCall(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onPutHangup(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onDeleteCall(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onDeleteIncoming(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onDeleteOnCall(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onDeleteHangup(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

}
