/*
 IRKitTVProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;

import java.util.List;

/**
 * TVプロファイル.
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
    public static final String ATTRIBUTE_BROADCASTWAVE = "broadcastwave";

    /**
     * 属性: {@value}.
     */
    public static final String ATTRIBUTE_MUTE = "mute";

    /**
     * 属性: {@value}.
     */
    public static final String ATTRIBUTE_ENLPROPERTY = "enlproperty";

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


    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }


        @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        String serviceId = getServiceID(request);
        if (attribute == null) {
            String tv = "/" + PROFILE_NAME;
            return sendTVRequest(serviceId, "PUT", tv, response);
        } else if (attribute.equals(ATTRIBUTE_CHANNEL)){
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
            return sendTVRequest(serviceId, "PUT", control, response);
        } else if (attribute.equals(ATTRIBUTE_VOLUME)) {
            String control = "/" + PROFILE_NAME + "/" + ATTRIBUTE_VOLUME
                    + "?" + PARAM_CONTROL + "=" +  request.getExtras().getString(PARAM_CONTROL);
            return sendTVRequest(serviceId, "PUT", control, response);
        } else if (attribute.equals(ATTRIBUTE_BROADCASTWAVE)) {
            String select = "/" + PROFILE_NAME + "/" + ATTRIBUTE_BROADCASTWAVE
                    + "?" + PARAM_SELECT + "=" +  request.getExtras().getString(PARAM_SELECT);
            return sendTVRequest(serviceId, "PUT", select, response);
        } else {
            MessageUtils.setNotSupportAttributeError(response);
            return true;
        }
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {

        String attribute = getAttribute(request);
        String serviceId = getServiceID(request);
        if (attribute == null) {
            String tv = "/" + PROFILE_NAME;
            return sendTVRequest(serviceId, "DELETE", tv, response);
        } else {
            MessageUtils.setNotSupportAttributeError(response);
            return true;
        }
    }

    /**
     * ライト用の赤外線を送信する.
     * @param serviceId サービスID
     * @param method HTTP Method
     * @param uri URI
     * @param response レスポンス
     * @return true:同期　false:非同期
     */
    private boolean sendTVRequest(final String serviceId, final String method, final String uri,
                                  final Intent response) {
        boolean send = true;
        IRKitDBHelper helper = new IRKitDBHelper(getContext());
        List<VirtualProfileData> requests = helper.getVirtualProfiles(serviceId, "TV");
        if (requests.size() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "Invalid ServiceId");
            return send;
        }
        for (VirtualProfileData req : requests) {
            if (req.getUri().equals(uri) && req.getMethod().equals(method)
                    && req.getIr() != null) {
                final IRKitDeviceService service = (IRKitDeviceService) getContext();
                send = service.sendIR(serviceId, req.getIr(), response);
                break;
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "IR is not registered for that request");
            }
        }
        return send;
    }

}
