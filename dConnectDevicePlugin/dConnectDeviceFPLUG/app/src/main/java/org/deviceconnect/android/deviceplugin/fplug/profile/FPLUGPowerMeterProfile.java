/*
 FPLUGPowerMeterProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fplug.BuildConfig;
import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.FPLUGDeviceService;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGRequestCallback;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGResponse;
import org.deviceconnect.android.deviceplugin.fplug.fplug.WattHour;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.PowerMeterProfile;
import org.deviceconnect.android.profile.PowerMeterProfileConstants;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.RFC3339DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Power Meter Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGPowerMeterProfile extends PowerMeterProfile {

    private final DConnectApi mGetIntegratedPowerValueApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTR_INTEGRATEDPOWER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            String date = request.getStringExtra(PARAM_DATE);
            String unit = (String) request.getExtras().get("unit");
            // 積算電力量単位パラメータ取得
            if (unit == null) {
                unit = "Wh";
            }

            switch (unit) {
                case "kWh":
                case "Wh":
                    break;
                default:
                    MessageUtils.setInvalidRequestParameterError(response, "unit parse error");
                    sendResultError(response);
                    return true;
            }
            Calendar calendar;
            if (date != null) {
                calendar = RFC3339DateUtils.toCalendar(date);
                if (calendar == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "date parse error");
                    sendResultError(response);
                    return true;
                }
            } else {
                calendar = Calendar.getInstance();
            }
            FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
            FPLUGController controller = app.getConnectedController(serviceId);
            if (controller == null) {
                MessageUtils.setUnknownError(response, "F-PLUG not connected");
                sendResultError(response);
                return false;
            }
            controller.requestPastWattHour(calendar, new FPLUGRequestCallback() {
                @Override
                public void onSuccess(FPLUGResponse fResponse) {
                    List<WattHour> wattHourList = fResponse.getWattHourList();
                    int[] wattList = new int[wattHourList.size()];
                    for (int i = 0; i < wattHourList.size(); i++) {
                        wattList[i] = wattHourList.get(i).getWatt();
                    }
                    response.putExtra(PowerMeterProfileConstants.PARAM_INTEGRATEDPOWER, wattList);
                    sendResultOK(response);
                }

                @Override
                public void onError(String message) {
                    MessageUtils.setUnknownError(response, message);
                    sendResultError(response);
                }

                @Override
                public void onTimeout() {
                    sendResultTimeout(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mGetInstantaneousPowerValueApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTR_INSTANTANEOUSPOWER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = request.getStringExtra(PARAM_SERVICE_ID);
            String unit = (String) request.getExtras().get("unit");
            // 瞬時電力量単位パラメータ取得
            if (unit == null) {
                unit = "W";
            }

            switch (unit) {
                case "kW":
                case "W":
                    break;
                default:
                    MessageUtils.setInvalidRequestParameterError(response, "unit parse error");
                    sendResultError(response);
                    return true;
            }

            FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
            FPLUGController controller = app.getConnectedController(serviceId);
            if (controller == null) {
                MessageUtils.setUnknownError(response, "F-PLUG not connected");
                sendResultError(response);
                return false;
            }
            controller.requestRealtimeWatt(new FPLUGRequestCallback() {
                @Override
                public void onSuccess(FPLUGResponse fResponse) {
                    response.putExtra(PowerMeterProfileConstants.PARAM_INSTANTANEOUSPOWER, fResponse.getRealtimeWatt());
                    sendResultOK(response);
                }

                @Override
                public void onError(String message) {
                    MessageUtils.setUnknownError(response, message);
                    sendResultError(response);
                }

                @Override
                public void onTimeout() {
                    sendResultTimeout(response);
                }
            });
            return false;
        }
    };

    public FPLUGPowerMeterProfile() {
        addApi(mGetIntegratedPowerValueApi);
        addApi(mGetInstantaneousPowerValueApi);
    }

    private void sendResultOK(Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultError(Intent response) {
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultTimeout(Intent response) {
        MessageUtils.setTimeoutError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

}
