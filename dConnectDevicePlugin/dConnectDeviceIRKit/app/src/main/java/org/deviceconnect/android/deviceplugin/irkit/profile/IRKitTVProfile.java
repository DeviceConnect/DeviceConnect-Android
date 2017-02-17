/*
 IRKitTVProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.service.VirtualService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;

import java.util.List;

/**
 * 仮想デバイスのTVプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class IRKitTVProfile extends DConnectProfile {

    /**
     * プロファイル名: {@value}.
     */
    public static final String PROFILE_NAME = "tv";
    /**
     * 属性: {@value}.
     */
    public static final String ATTRIBUTE_CHANNEL = "channel";
    /**
     * 属性: {@value}.
     */
    public static final String ATTRIBUTE_VOLUME = "volume";

    /**
     * 属性: {@value}.
     */
    public static final String ATTRIBUTE_BROADCASTWAVE = "broadcastWave";

    /**
     * 属性: {@value}.
     */
    public static final String ATTRIBUTE_MUTE = "mute";

    /**
     * 属性: {@value}.
     */
    public static final String ATTRIBUTE_ENLPROPERTY = "enlProperty";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_TVID = "tvId";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_CONTROL = "control";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_TUNING = "tuning";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_SELECT = "select";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_EPC = "epc";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_VALUE = "value";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_POWERSTATUS = "powerstatus";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_PROPERTIES = "properties";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_ON = "ON";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_OFF = "OFF";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_UNKNOWN = "UNKNOWN";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_NEXT = "next";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_PREVIOUS = "previous";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_UP = "up";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_DOWN = "down";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_DTV = "DTV";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_BS = "BS";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_CS = "CS";

    public IRKitTVProfile() {
        addApi(mPutPowerOnApi);
        addApi(mPutChannelApi);
        addApi(mPutVolumeApi);
        addApi(mPutBroadcastWaveApi);
        addApi(mDeletePowerOffApi);
    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    private final DConnectApi mPutPowerOnApi = new PutApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String tv = "/" + PROFILE_NAME;
            ;
            return ((VirtualService) getService()).sendTVRequest(getServiceID(request), "PUT", tv, response);
        }
    };

    private final DConnectApi mPutChannelApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_CHANNEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String control = null;
            if (request.getExtras().getString(PARAM_CONTROL) != null) {
                control = "/" + PROFILE_NAME + "/" + ATTRIBUTE_CHANNEL
                    + "?" + PARAM_CONTROL + "=" + request.getExtras().getString(PARAM_CONTROL);
            }
            if (request.getExtras().getString(PARAM_TUNING) != null) {
                if (request.getExtras().getString(PARAM_CONTROL) == null) {
                    control = "/" + PROFILE_NAME + "/" + ATTRIBUTE_CHANNEL + "?";
                } else {
                    control = control + "&";
                }
                control = control + PARAM_TUNING + "=" + request.getExtras().getString(PARAM_TUNING);
            }
            return ((VirtualService) getService()).sendTVRequest(getServiceID(request), "PUT", control, response);
        }
    };

    private final DConnectApi mPutVolumeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_VOLUME;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String control = "/" + PROFILE_NAME + "/" + ATTRIBUTE_VOLUME
                + "?" + PARAM_CONTROL + "=" +  request.getExtras().getString(PARAM_CONTROL);
            return ((VirtualService) getService()).sendTVRequest(getServiceID(request), "PUT", control, response);
        }
    };

    private final DConnectApi mPutBroadcastWaveApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_BROADCASTWAVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String select = "/" + PROFILE_NAME + "/" + ATTRIBUTE_BROADCASTWAVE
                + "?" + PARAM_SELECT + "=" +  request.getExtras().getString(PARAM_SELECT);
            return ((VirtualService) getService()).sendTVRequest(getServiceID(request), "PUT", select, response);
        }
    };

    private final DConnectApi mDeletePowerOffApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String tv = "/" + PROFILE_NAME;
            return ((VirtualService) getService()).sendTVRequest(getServiceID(request), "DELETE", tv, response);
        }
    };



}
