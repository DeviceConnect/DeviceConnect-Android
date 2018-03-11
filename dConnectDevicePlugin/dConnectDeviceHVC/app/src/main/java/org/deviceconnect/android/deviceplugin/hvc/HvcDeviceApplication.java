/*
 HvcDeviceApplication.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcLocationAlertDialog;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/**
 * HVC Device Plugin Application.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceApplication extends Application {
    private static HvcDeviceApplication instance = null;
    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // start accept service
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.setClass(this, HvcDeviceProvider.class);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfile.PROFILE_NAME);
        sendBroadcast(request);
    }

    public static HvcDeviceApplication getInstance() {
        return instance;
    }

    public void checkLocationEnable() {
        Context context = getApplicationContext();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            Intent intent = new Intent(context, HvcLocationAlertDialog.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }
}
