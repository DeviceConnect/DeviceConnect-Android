/*
 KadecotDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.json.JSONObject;

import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Kadecot Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotDeviceService extends DConnectMessageService {

    /** Application instance. */
    private KadecotDeviceApplication mApp;
    /** Logger. */
    private final Logger mLogger = Logger.getLogger("kadecot.dplugin");
    /** Service Discovery Profile. */
    private KadecotServiceDiscoveryProfile mServiceDiscoveryProfile;

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = (KadecotDeviceApplication) this.getApplication();
        mServiceDiscoveryProfile = new KadecotServiceDiscoveryProfile(getServiceProvider());

        addProfile(mServiceDiscoveryProfile);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        if (BuildConfig.DEBUG) {
            if (intent != null) {
                String action = intent.getAction();
                mLogger.info("onStartCommand: action=" + action);
            }
        }

        return START_STICKY;
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new KadecotSystemProfile();
    }

    /**
     * Get element from ServiceID.
     *
     * @param serviceId ServiceId.
     * @return Elements.
     */
    public static String[] getElementFromServiceId(final String serviceId) {
        Pattern p = Pattern.compile("[_\\.]+");
        return p.split(serviceId);
    }

    /**
     * Get property name.
     *
     * @param response JSON response.
     * @return Property name(Success) / null (Failure).
     */
    public static String getPropertyName(final String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("propertyName");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get property value.
     *
     * @param response JSON response.
     * @return Property value(Success) / null (Failure).
     */
    public static String getPropertyValue(final String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String propertyValue = jsonObject.getString("propertyValue");
            if (propertyValue != null) {
                propertyValue = propertyValue.replace("[", "").replace("]", "");
            }
            return propertyValue;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get NickName.
     *
     * @param serviceId ServiceId.
     * @return Nickname(Success) / null(not found)
     */
    public String getNickName(final String serviceId) {
        return mServiceDiscoveryProfile.getNickName(serviceId);
    }

    /**
     * Get application instance.
     *
     * @return Application instance.
     */
    public KadecotDeviceApplication getKadecotDeviceApplication() {
        return mApp;
    }

}
