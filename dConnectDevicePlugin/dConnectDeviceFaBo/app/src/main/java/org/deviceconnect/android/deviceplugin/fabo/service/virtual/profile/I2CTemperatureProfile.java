package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.device.IADT7410;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * I2C用Temperatureプロファイル.
 * <p>
 * ID: #207<br>
 * Name: Temperature I2C Brick<br>
 * </p>
 */
public class I2CTemperatureProfile extends BaseFaBoProfile {
    /**
     * コンストラクタ.
     */
    public I2CTemperatureProfile() {
        // GET /gotpai/temperature
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final Integer type = parseInteger(request, "type");
                final IADT7410 adt = getFaBoDeviceControl().getADT7410();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (adt == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    adt.read(new IADT7410.OnADT7410Listener() {
                        @Override
                        public void onStarted() {
                        }

                        @Override
                        public void onError(final String message) {
                            MessageUtils.setIllegalDeviceStateError(response, message);
                            sendResponse(response);
                        }

                        @Override
                        public void onData(final double temperature) {
                            if (type == null || type == 1) {
                                response.putExtra("temperature", temperature);
                            } else {
                                response.putExtra("temperature", convertC2F(temperature));
                            }
                            setResult(response, DConnectMessage.RESULT_OK);
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
        return "temperature";
    }

    /**
     * 摂氏を華氏に変換します.
     * @param temp 摂氏
     * @return 華氏
     */
    private double convertC2F(double temp) {
        return temp * 1.8 + 32;
    }
}
