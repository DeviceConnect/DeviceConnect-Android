package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;

/**
 * I2C用加速度センサープロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #202<br>
 * Name: 9Axis I2C Brick<br>
 * </p>
 */
public class I2C9AxisDeviceOrientationProfile extends BaseFaBoProfile {

    public I2C9AxisDeviceOrientationProfile() {
        // GET /gotapi/deviceOrientation/onDeviceOrientation
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onDeviceOrientation";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                MessageUtils.setNotSupportProfileError(response, "Not implements yet.");
                return true;
            }
        });

        // PUT /gotapi/deviceOrientation/onDeviceOrientation
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onDeviceOrientation";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                MessageUtils.setNotSupportProfileError(response, "Not implements yet.");
                return true;
            }
        });

        // DELETE /gotapi/deviceOrientation/onDeviceOrientation
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onDeviceOrientation";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                MessageUtils.setNotSupportProfileError(response, "Not implements yet.");
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "deviceOrientation";
    }
}
