package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.device.IHTS221;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * I2C用Humidityプロファイル.
 * <p>
 * ID: #208<br>
 * Name: Humidity I2C Brick<br>
 * </p>
 */
public class I2CHumidityProfile extends BaseFaBoProfile {

    public I2CHumidityProfile() {
        // GET /gotapi/humidity
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                IHTS221 hts221 = getFaBoDeviceControl().getHTS221();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (hts221 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support");
                } else {
                    hts221.readHumidity(new IHTS221.OnHumidityCallback() {
                        @Override
                        public void onHumidity(final double humidity) {
                            response.putExtra("humidity", humidity / 100.0);
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                        }

                        @Override
                        public void onError(final String message) {
                            MessageUtils.setIllegalDeviceStateError(response, message);
                            sendResponse(response);
                        }
                    });
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "humidity";
    }
}
