/*
 EventSession.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.event;


import android.content.Context;
import android.content.Intent;

import java.io.IOException;

/**
 * イベントセッション.
 *
 * @author NTT DOCOMO, INC.
 */
abstract class EventSession {

    private Context mContext;
    private String mReceiverId;
    private String mServiceId;
    private String mPluginId;
    private String mProfileName;
    private String mInterfaceName;
    private String mAttributeName;
    private String mAccessToken;

    public Context getContext() {
        return mContext;
    }

    void setContext(final Context context) {
        mContext = context;
    }

    public String getReceiverId() {
        return mReceiverId;
    }

    void setReceiverId(final String receiverId) {
        mReceiverId = receiverId;
    }

    public String getServiceId() {
        return mServiceId;
    }

    void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    public String getPluginId() {
        return mPluginId;
    }

    void setPluginId(final String pluginId) {
        mPluginId = pluginId;
    }

    public String getProfileName() {
        return mProfileName;
    }

    void setProfileName(final String profileName) {
        mProfileName = profileName;
    }

    public String getInterfaceName() {
        return mInterfaceName;
    }

    void setInterfaceName(final String interfaceName) {
        mInterfaceName = interfaceName;
    }

    public String getAttributeName() {
        return mAttributeName;
    }

    void setAttributeName(final String attributeName) {
        mAttributeName = attributeName;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    void setAccessToken(final String accessToken) {
        mAccessToken = accessToken;
    }

    public abstract void sendEvent(final Intent event) throws IOException;

}
