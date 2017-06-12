package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

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

    private static final byte ADT7410_ADDRESS = 0x48;

    public I2CTemperatureProfile() {
        // GET /gotpai/temperature
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                test();
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "temperature";
    }


    private void test() {
//        byte[] commandDataLeft = {
//                START_SYSEX,
//                I2C_REQUEST,
//                ADT7410_ADDRESS,
//                0x03,
//                (byte) 0x80,
//                END_SYSEX
//        };
//        getFaBoUsbManager().writeBuffer(commandDataLeft);
    }
}
