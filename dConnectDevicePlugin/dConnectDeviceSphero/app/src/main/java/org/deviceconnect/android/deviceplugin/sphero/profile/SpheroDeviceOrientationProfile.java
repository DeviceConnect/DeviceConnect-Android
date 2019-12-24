/*
 SpheroDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import android.content.Intent;
import android.os.Bundle;

import com.orbotix.async.DeviceSensorAsyncMessage;

import org.deviceconnect.android.deviceplugin.sphero.SpheroDeviceService;
import org.deviceconnect.android.deviceplugin.sphero.SpheroManager;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo.DeviceSensorListener;
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
 * DeviceOrientation Profile.
 * @author NTT DOCOMO, INC.
 */
public class SpheroDeviceOrientationProfile extends DeviceOrientationProfile {

    public SpheroDeviceOrientationProfile() {
        addApi(mGetOnDeviceOrientationApi);
        addApi(mPutOnDeviceOrientationApi);
        addApi(mDeleteOnDeviceOrientationApi);
    }


    private final DConnectApi mGetOnDeviceOrientationApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            final DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
            if (device == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }
            SpheroManager.INSTANCE.startSensor(device, (info, data, interval) -> {
                Bundle orientation = SpheroManager.createOrientation(data, interval);
                DeviceOrientationProfile.setOrientation(response, orientation);
                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                SpheroDeviceService service = (SpheroDeviceService) getContext();
                service.sendResponse(response);
            });
            return false;
        }
    };

    private final DConnectApi mPutOnDeviceOrientationApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
            if (device == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }

            EventError error = EventManager.INSTANCE.addEvent(request);
            switch (error) {
                case NONE:
                    setResult(response, DConnectMessage.RESULT_OK);
                    SpheroManager.INSTANCE.startSensor(device);
                    break;
                case INVALID_PARAMETER:
                    MessageUtils.setInvalidRequestParameterError(response);
                    break;
                default:
                    MessageUtils.setUnknownError(response);
                    break;
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
            String serviceId = getServiceID(request);
            // Deleteはデバイスが無くてもゴミを残さないように削除できるようにしておく。
            EventError error = EventManager.INSTANCE.removeEvent(request);
            switch (error) {
                case NONE:
                    setResult(response, DConnectMessage.RESULT_OK);
                    DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
                    if (device != null) {
                        if (!SpheroManager.INSTANCE.hasSensorEvent(device)) {
                            SpheroManager.INSTANCE.stopSensor(device);
                        }
                    }
                    break;
                case INVALID_PARAMETER:
                    MessageUtils.setInvalidRequestParameterError(response);
                    break;
                default:
                    MessageUtils.setUnknownError(response);
                    break;
            }
            return true;
        }
    };

}
