/*
 ThetaBatteryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Theta Battery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaBatteryProfile extends BatteryProfile {

    private final ThetaDeviceClient mClient;

    private final DConnectApi mGetAllApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            mClient.getBatteryLevel(serviceId, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onBatteryLevel(final double level) {
                    setLevel(response, level);
                    setCharging(response, false);
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    MessageUtils.setUnknownError(response, cause.getMessage());
                    sendResponse(response);
                }

            });
            return false;
        }
    };

    private final DConnectApi mGetLevelApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            mClient.getBatteryLevel(serviceId, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onBatteryLevel(final double level) {
                    setLevel(response, level);
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    MessageUtils.setUnknownError(response, cause.getMessage());
                    sendResponse(response);
                }

            });
            return false;
        }
    };

    /**
     * Constructor.
     *
     * @param client an instance of {@link ThetaDeviceClient}
     */
    public ThetaBatteryProfile(final ThetaDeviceClient client) {
        mClient = client;
        addApi(mGetAllApi);
        addApi(mGetLevelApi);
    }

}
