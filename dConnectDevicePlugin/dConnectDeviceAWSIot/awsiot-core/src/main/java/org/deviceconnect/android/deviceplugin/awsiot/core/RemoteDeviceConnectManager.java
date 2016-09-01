package org.deviceconnect.android.deviceplugin.awsiot.core;

public class RemoteDeviceConnectManager {
    public static final String DEVICE_CONNECT = "dconnect/";
    public static final String REQUEST = "/request";
    public static final String RESPONSE = "/response";
    public static final String EVENT = "/event";

    private String mName;
    private String mServiceId;

    public RemoteDeviceConnectManager(final String name, final String id) {
        mName = name;
        mServiceId = id;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public String getName() {
        return mName;
    }

    public String getRequestTopic() {
        return DEVICE_CONNECT + mName + REQUEST;
    }

    public String getResponseTopic() {
        return DEVICE_CONNECT + mName + RESPONSE;
    }

    public String getEventTopic() {
        return DEVICE_CONNECT + mName + EVENT;
    }

    @Override
    public String toString() {
        return "{\nname:" + mName + "\n" + "topic: {\n"
                + "request:" + getRequestTopic() + "\n"
                + "response:" + getRequestTopic() + "\n"
                + "event:" + getRequestTopic() + "\n"
                + "}"
                + "}";
    }
}
