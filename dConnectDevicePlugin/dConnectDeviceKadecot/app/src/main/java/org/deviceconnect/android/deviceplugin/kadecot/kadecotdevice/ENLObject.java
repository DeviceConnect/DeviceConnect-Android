/*
 ENLObject
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice;

import org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfileConstants;
import org.deviceconnect.profile.LightProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

import java.util.ArrayList;

/**
 * ECHONET Lite Object.
 *
 * @author NTT DOCOMO, INC.
 */
public class ENLObject {
    /** ECHONET Lite Object List. */
    ArrayList<DeviceData> mENLObjectList = new ArrayList<>();

    /**
     * Constructor.
     */
    public ENLObject() {
        ArrayList<String> scopes = new ArrayList<>();
        String profile = AirConditionerProfileConstants.PROFILE_NAME;
        setBaseProfile(scopes);
        scopes.add(profile);
        mENLObjectList.add(new DeviceData("HomeAirConditioner", profile, scopes));

        scopes = new ArrayList<>();
        profile = LightProfileConstants.PROFILE_NAME;
        setBaseProfile(scopes);
        scopes.add(profile);
        mENLObjectList.add(new DeviceData("GeneralLighting", profile, scopes));
    }

    /**
     * Get object count.
     *
     * @return Object count.
     */
    public int getObjectCount() {
        if (mENLObjectList != null) {
            return mENLObjectList.size();
        } else {
            return 0;
        }
    }

    /**
     * Exchange ServiceId.
     *
     * @param className ECHONET Lite Class name.
     * @return Found : DeviceConnect ServiceId.
     *         Not found : null.
     */
    public String exchangeServiceId(final String className) {
        for (int i = 0; i < mENLObjectList.size(); i++) {
            DeviceData dd = mENLObjectList.get(i);
            if (dd.getENLClassName().equals(className)) {
                return dd.getDCServiceId();
            }
        }
        return null;
    }

    /**
     * Get scopes for class name.
     *
     * @param className ECHONET Lite Class name.
     * @return Scopes.
     */
    public ArrayList<String> getScopesFromClassName(final String className) {
        for (int i = 0; i < mENLObjectList.size(); i++) {
            DeviceData dd = mENLObjectList.get(i);
            if (dd.getENLClassName().equals(className)) {
                return dd.getScopes();
            }
        }
        return null;
    }

    /**
     * Get scopes for property name.
     *
     * @param profileName Profile name.
     * @return Scopes.
     */
    public ArrayList<String> getScopesFromProfileName(final String profileName) {
        for (int i = 0; i < mENLObjectList.size(); i++) {
            DeviceData dd = mENLObjectList.get(i);
            if (dd.getDCServiceId().equals(profileName)) {
                return dd.getScopes();
            }
        }
        return null;
    }

    /**
     * Set base profile.
     *
     * @param scopes Scope list.
     */
    private void setBaseProfile(final ArrayList<String> scopes) {
        scopes.add(ServiceInformationProfileConstants.PROFILE_NAME);
    }
}
