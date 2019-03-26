/*
 EventSession.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.event;


import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.manager.core.DConnectConst;

import java.io.IOException;

/**
 * イベントセッション.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class EventSession {
    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * receiver_id.
     */
    private String mReceiverId;

    /**
     * サービスID.
     */
    private String mServiceId;

    /**
     * プラグインID.
     */
    private String mPluginId;

    /**
     * プロファイル名.
     */
    private String mProfileName;

    /**
     * インターフェース名.
     */
    private String mInterfaceName;

    /**
     * アトリビュート名.
     */
    private String mAttributeName;

    /**
     * アクセストークン.
     */
    private String mAccessToken;

    public Context getContext() {
        return mContext;
    }

    public void setContext(final Context context) {
        mContext = context;
    }

    public String getReceiverId() {
        return mReceiverId;
    }

    public void setReceiverId(final String receiverId) {
        mReceiverId = receiverId;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    public String getPluginId() {
        return mPluginId;
    }

    public void setPluginId(final String pluginId) {
        mPluginId = pluginId;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public void setProfileName(final String profileName) {
        mProfileName = profileName;
    }

    public String getInterfaceName() {
        return mInterfaceName;
    }

    public void setInterfaceName(final String interfaceName) {
        mInterfaceName = interfaceName;
    }

    public String getAttributeName() {
        return mAttributeName;
    }

    public void setAttributeName(final String attributeName) {
        mAttributeName = attributeName;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(final String accessToken) {
        mAccessToken = accessToken;
    }

    public String createKey() {
        StringBuilder result = new StringBuilder();
        result.append(getReceiverId())
                .append(DConnectConst.SEPARATOR)
                .append(getPluginId());
        return result.toString();
    }

    public abstract void sendEvent(final Intent event) throws IOException;

    @Override
    public String toString() {
        return "{\n" +
                "    ServiceId: " + getServiceId() + "\n" +
                "    PluginId: " + getPluginId() + "\n" +
                "    ReceiverId: " + getReceiverId() + "\n" +
                "    Profile: " + getProfileName() + "\n" +
                "    Interface: " + getInterfaceName() + "\n" +
                "    Attribute: " + getAttributeName() + "\n" +
                "    AccessToken: " + getAccessToken() + "\n" +
                "}\n";
    }
}
