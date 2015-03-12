/*
 SWDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import org.deviceconnect.android.deviceplugin.sw.SWApplication;
import org.deviceconnect.android.deviceplugin.sw.SWDeviceOrientationCache;
import org.deviceconnect.android.deviceplugin.sw.SWService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

/**
 * SonySWデバイスプラグインの{@link DeviceOrientationProfile}実装.
 * @author NTT DOCOMO, INC.
 */
public class SWDeviceOrientationProfile extends DeviceOrientationProfile {

    @Override
    protected boolean onGetOnDeviceOrientation(final Intent request, final Intent response,
            final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No device is found: " + serviceId);
            return true;
        }

        SWService service = (SWService) getContext();
        SWApplication application = (SWApplication) service.getApplication();
        SWDeviceOrientationCache cache = application.getDeviceOrientation(serviceId);
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

    @Override
    protected boolean onPutOnDeviceOrientation(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No device is found: " + serviceId);
            return true;
        }
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

    @Override
    protected boolean onDeleteOnDeviceOrientation(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No device is found: " + serviceId);
            return true;
        }
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

}
