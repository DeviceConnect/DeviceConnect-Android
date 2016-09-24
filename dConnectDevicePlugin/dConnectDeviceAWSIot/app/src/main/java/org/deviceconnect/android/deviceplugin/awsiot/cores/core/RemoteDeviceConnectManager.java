/*
 RemoteDeviceConnectManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

public class RemoteDeviceConnectManager {
    public static final String DEVICE_CONNECT = "DeviceConnect/";
    public static final String REQUEST = "/request";
    public static final String RESPONSE = "/response";
    public static final String EVENT = "/event";

    private String mName;
    private String mServiceId;
    private boolean mSubscribeFlag;
    private boolean mOnline;
    private double mTimeStamp;

    public RemoteDeviceConnectManager(final String name, final String id) {
        mName = name;
        mServiceId = id;
        mSubscribeFlag = false;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public boolean isSubscribe() {
        return mSubscribeFlag;
    }

    public void setSubscribeFlag(final boolean flag) {
        mSubscribeFlag = flag;
    }

    public boolean isOnline() {
        return mOnline;
    }

    public void setOnline(boolean online) {
        mOnline = online;
    }

    public double getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        mTimeStamp = timeStamp;
    }

    public String getRequestTopic() {
        return DEVICE_CONNECT + mServiceId + REQUEST;
    }

    public String getResponseTopic() {
        return DEVICE_CONNECT + mServiceId + RESPONSE;
    }

    public String getEventTopic() {
        return DEVICE_CONNECT + mServiceId + EVENT;
    }

    @Override
    public String toString() {
        return "{name: " + mName + ", uuid: " + mServiceId + ", subscribe: " + mSubscribeFlag + " }";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!(o instanceof RemoteDeviceConnectManager)) {
            return false;
        }

        RemoteDeviceConnectManager obj = (RemoteDeviceConnectManager) o;
        return obj.mServiceId.equals(mServiceId) && obj.mName.equals(mName);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mName.hashCode();
        result = 31 * result + mServiceId.hashCode();
        return result;
    }
}
