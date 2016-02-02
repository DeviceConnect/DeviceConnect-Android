/*
 FPLUGSettingsProfile.java
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
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SettingsProfile;
import org.deviceconnect.message.DConnectMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Settings Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGSettingsProfile extends SettingsProfile {

    private final static String RFC_3339 = "yyyy-MM-dd'T'HH:mm:ssZ";

    @Override
    protected boolean onPutDate(Intent request, final Intent response, String serviceId, String date) {
        Calendar calendar = createCalendar(date);
        if (calendar == null) {
            MessageUtils.setInvalidRequestParameterError(response, "date parse error");
            sendResultError(response);
            return true;
        }
        if (BuildConfig.DEBUG) {
            Log.d("Settings", "calendar:" + calendar.toString());
        }
        FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
        FPLUGController controller = app.getConnectedController(serviceId);
        if (controller == null) {
            MessageUtils.setUnknownError(response, "F-PLUG not connected");
            sendResultError(response);
            return false;
        }
        controller.requestSetDate(calendar, new FPLUGRequestCallback() {
            @Override
            public void onSuccess(FPLUGResponse fResponse) {
                sendResultOK(response);
            }

            @Override
            public void onError(String message) {
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
        MessageUtils.setUnknownError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultTimeout(Intent response) {
        MessageUtils.setTimeoutError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

}
