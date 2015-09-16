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
import org.deviceconnect.message.DConnectMessage;

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

    private final static String RFC_3339 = "yyyy-MM-dd'T'HH:mm:ssZ";

    @Override
    protected boolean onGetIntegratedPowerValue(Intent request, final Intent response) {
        final String serviceId = request.getStringExtra(PARAM_SERVICE_ID);
        String date = request.getStringExtra(PARAM_DATE);
        Calendar calendar;
        if (date != null) {
            calendar = createCalendar(date);
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
                response.putExtra(PowerMeterProfileConstants.PARAM_INTEGRATEDPOWERVALUE, wattList);
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

    @Override
    protected boolean onGetInstantaneousPowerValue(Intent request, final Intent response) {
        final String serviceId = request.getStringExtra(PARAM_SERVICE_ID);
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
                response.putExtra(PowerMeterProfileConstants.PARAM_INSTANTANEOUSPOWERVALUE, fResponse.getRealtimeWatt());
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

    private Calendar createCalendar(String date) {
        if (BuildConfig.DEBUG) {
            Log.d("Settings", "date:" + date);
        }
        SimpleDateFormat format = new SimpleDateFormat(RFC_3339, Locale.US);

        Date converted;
        try {
            converted = new Date(format.parse(date).getTime());
        } catch (java.text.ParseException e) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(converted);
        return calendar;
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
