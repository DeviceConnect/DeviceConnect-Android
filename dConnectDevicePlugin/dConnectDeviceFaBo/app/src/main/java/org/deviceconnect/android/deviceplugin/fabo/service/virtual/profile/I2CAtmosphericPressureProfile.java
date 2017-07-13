package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.device.IMPL115;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * I2C用気圧センサープロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #204<br>
 * Name: Barometer I2C Brick<br>
 * </p>
 */
public class I2CAtmosphericPressureProfile extends BaseFaBoProfile {
    public I2CAtmosphericPressureProfile() {
        addApi(new GetApi() {
            @Override
            public boolean onRequest(Intent request, final Intent response) {
                IMPL115 mpl115 = getFaBoDeviceControl().getMPL115();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (mpl115 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    mpl115.readAtmosphericPressure(new IMPL115.OnAtmosphericPressureListener() {
                        @Override
                        public void onData(double hpa, double temperature) {
                            response.putExtra("atmosphericPressure", hpa);
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
        return "atmosphericPressure";
    }
}
