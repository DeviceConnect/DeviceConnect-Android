/*
 SWDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.sw.SWApplication;
import org.deviceconnect.android.deviceplugin.sw.SWDeviceOrientationCache;
import org.deviceconnect.android.deviceplugin.sw.SWDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * SonySWデバイスプラグインの{@link DeviceOrientationProfile}実装.
 * @author NTT DOCOMO, INC.
 */
public class SWDeviceOrientationProfile extends DeviceOrientationProfile {

    private final DConnectApi mGetOnDeviceOrientationApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            SWDeviceService service = (SWDeviceService) getContext();
            SWApplication application = (SWApplication) service.getApplication();
            SWDeviceOrientationCache cache = application.getDeviceOrientation(getServiceID(request));
            if (cache == null) {
                MessageUtils.setNotSupportAttributeError(response);
            } else {
                float[] values = cache.getValues();
                Bundle acceleration = new Bundle();
                acceleration.putDouble(DeviceOrientationProfile.PARAM_X, values[0]);
                acceleration.putDouble(DeviceOrientationProfile.PARAM_Y, values[1]);
                acceleration.putDouble(DeviceOrientationProfile.PARAM_Z, values[2]);

                Bundle orientation = new Bundle();
                DeviceOrientationProfile.setAccelerationIncludingGravity(orientation, acceleration);
                DeviceOrientationProfile.setInterval(orientation, cache.getInterval());

                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                DeviceOrientationProfile.setOrientation(response, orientation);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnDeviceOrientationApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnDeviceOrientationApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    public SWDeviceOrientationProfile() {
        addApi(mGetOnDeviceOrientationApi);
        addApi(mPutOnDeviceOrientationApi);
        addApi(mDeleteOnDeviceOrientationApi);
    }

}
