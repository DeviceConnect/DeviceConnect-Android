/*
 SmartMeterDeviceProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.profiles;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.smartmeter.SmartMeterMessageService;
import org.deviceconnect.android.deviceplugin.smartmeter.device.UsbSerialDevice;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;

/**
 * SmartMeter Device Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class SmartMeterDeviceProfile extends DConnectProfile {
    public SmartMeterDeviceProfile() {
        // DELETE /device/pairing
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "pairing";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // USBシリアルデバイスリストの空チェック
                if (((SmartMeterMessageService) getContext()).isEmptyUsbSerialDevice()) {
                    MessageUtils.setIllegalDeviceStateError(response, "Not found USB dongle.");
                    return true;
                }

                // USBシリアルデバイスの取得
                UsbSerialDevice device = ((SmartMeterMessageService) getContext()).getUsbSerialDevice(serviceId);
                if (device == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not match serviceId.");
                    return true;
                }

                // Smart meterとの通信切断
                ((SmartMeterMessageService) getContext()).disconnectSmartMeter(response, device);

                return false;
            }
        });

        // POST /device/pairing
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "pairing";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // USBシリアルデバイスリストの空チェック
                if (((SmartMeterMessageService) getContext()).isEmptyUsbSerialDevice()) {
                    MessageUtils.setIllegalDeviceStateError(response, "Not found USB dongle.");
                    return true;
                }

                // USBシリアルデバイスの取得
                UsbSerialDevice device = ((SmartMeterMessageService) getContext()).getUsbSerialDevice(serviceId);
                if (device == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not match serviceId.");
                    return true;
                }

                // Smart meterとの通信接続
                ((SmartMeterMessageService) getContext()).connectSmartMeter(response, device);

                return false;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "device";
    }
}
