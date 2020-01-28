package com.example.switchbotdemoapp;

import android.app.Application;
import android.util.Log;

import com.example.switchbotdemoapp.profile.ButtonProfile;
import com.example.switchbotdemoapp.profile.SwitchProfile;
import com.example.switchbotdemoapp.utility.DConnectWrapper;

import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

public class SwitchBotDemoApp extends Application {
    private static final String TAG = "SwitchBotDemoApp";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String[] SCOPES = {
            ServiceDiscoveryProfileConstants.PROFILE_NAME,
            ServiceInformationProfileConstants.PROFILE_NAME,
            SystemProfileConstants.PROFILE_NAME,
            ButtonProfile.PROFILE_NAME,
            SwitchProfile.PROFILE_NAME
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if(DEBUG){
            Log.d(TAG, "onCreate()");
        }
        DConnectWrapper.initialize(this, getString(R.string.app_name), "localhost", 4035, SCOPES);
    }
}
