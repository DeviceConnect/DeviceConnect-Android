/*
 HitoeBatteryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Implement HitoeBatteryProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeBatteryProfile extends BatteryProfile {


    public HitoeBatteryProfile() {
        addApi(mGetAll);
        addApi(mGetLevel);
    }

    private final DConnectApi mGetAll = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getBattery(request, response);
        }
    };

    private final DConnectApi mGetLevel = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_LEVEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getBattery(request, response);
        }
    };


    private boolean getBattery(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else {
            HeartRateData data = getManager().getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }
            double level = (double) (data.getDevice().getBatteryLevel() + 1);
            if (level < 0) {
                MessageUtils.setUnknownError(response, "Battery level is unknown.");
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setLevel(response,  ( level / 4.0f ));
            }
        }
        return true;
    }
    /**
     * Gets a instance of HitoeManager.
     *
     * @return {@link HitoeManager}, or null on error
     */
    private HitoeManager getManager() {
        HitoeDeviceService service = (HitoeDeviceService) getContext();
        if (service == null) {
            return null;
        }
        HitoeApplication app = (HitoeApplication) service.getApplication();
        if (app == null) {
            return null;
        }
        return app.getHitoeManager();
    }
}
