/*
 KadecotServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.ENLObject;
import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;

/**
 * Kadecot Service Information Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotServiceInformationProfile extends ServiceInformationProfile {

    /** Kadecot prefix. */
    static final String PREFIX_KADECOT = "kadecot";

    /** "No result" string. */
    static final String NO_RESULT = "{}";

    /** Index of prefix. */
    static final int IDX_PREFIX = 0;

    /** Index of kadecot deviceId. */
    static final int IDX_DEVICEID = 1;

    /** Index of profile name. */
    static final int IDX_PROFILENAME = 2;

    /**
     * Constructor.
     *
     * @param provider profile provider.
     */
    public KadecotServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetInformation(final Intent request, final Intent response, final String serviceId) {
        if (checkServiceId(serviceId, response)) {
            KadecotDeviceService service = (KadecotDeviceService) getContext();
            String[] element = service.getElementFromServiceId(serviceId);
            if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                    && element[IDX_PROFILENAME] != null) {
                ENLObject object = ((KadecotDeviceService) getContext()).getKadecotDeviceApplication().getENLObject();
                ArrayList<String> scopes = object.getScopesFromProfileName(element[IDX_PROFILENAME]);
                if (scopes != null) {
                    setDefaultServiceInformation(response, serviceId);
                    setSupports(response, scopes.toArray(new String[0]));
                } else {
                    super.onGetInformation(request, response, serviceId);
                }
            } else {
                super.onGetInformation(request, response, serviceId);
            }
        } else {
            super.onGetInformation(request, response, serviceId);
        }
        return true;
    }

    /**
     * Check serviceID.
     *
     * @param serviceId ServiceId.
     * @param response Response intent.
     * @return Normal(true) / Abnormal(false).
     */
    private boolean checkServiceId(final String serviceId, final Intent response) {
        KadecotDeviceService service = (KadecotDeviceService) getContext();
        if (serviceId == null) {
            createEmptyServiceId(response);
            return false;
        } else if (!(service.checkServiceId(serviceId))) {
            createNotFoundService(response);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Creates an error of "serviceId is empty".
     *
     * @param response Intent to store the response.
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * Creates an error of "service not found".
     *
     * @param response Intent to store the response.
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    /**
     * Set default Service Information parameters.
     *
     * @param response response.
     * @param serviceId ServiceId
     */
    private void setDefaultServiceInformation(final Intent response, final String serviceId) {
        Bundle connect = new Bundle();
        setWifiState(connect, getWifiState(serviceId));
        setBluetoothState(connect, getBluetoothState(serviceId));
        setNFCState(connect, getNFCState(serviceId));
        setBLEState(connect, getBLEState(serviceId));
        setConnect(response, connect);

        // version
        setVersion(response, getCurrentVersionName());
        setResult(response, DConnectMessage.RESULT_OK);
    }

    /**
     * Get versionName from AndroidManifest.xml.
     *
     * @return Version name.
     */
    private String getCurrentVersionName() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

}
