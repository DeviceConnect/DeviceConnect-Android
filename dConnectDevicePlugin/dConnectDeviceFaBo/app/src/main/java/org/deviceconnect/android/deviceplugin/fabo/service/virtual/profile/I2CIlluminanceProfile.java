package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.device.IISL29034;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * I2C用Illuminaceプロファイル.
 * <p>
 * ID #217<br>
 * Name: Ambient Light I2C Brick<br>
 * </p>
 */
public class I2CIlluminanceProfile extends BaseFaBoProfile {

    public I2CIlluminanceProfile() {
        // GET /gotpai/illuminance
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                IISL29034 isl29034 = getFaBoDeviceControl().getISL29034();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (isl29034 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    isl29034.read(new IISL29034.OnAmbientLightListener() {
                        @Override
                        public void onStarted() {
                        }

                        @Override
                        public void onData(double lux) {
                            response.putExtra("illuminance", lux);
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                        }

                        @Override
                        public void onError(String message) {
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
        return "illuminance";
    }
}
