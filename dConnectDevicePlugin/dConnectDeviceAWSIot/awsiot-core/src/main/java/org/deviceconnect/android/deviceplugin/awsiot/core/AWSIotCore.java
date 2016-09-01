package org.deviceconnect.android.deviceplugin.awsiot.core;

import java.util.List;
import java.util.UUID;

public class AWSIotCore {

    public static final String KEY_DCONNECT_SHADOW_NAME = "dconnect";

    public static final String KEY_REQUEST_CODE = "requestCode";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_RESPONSE = "response";
    public static final String KEY_P2P = "p2p";

    protected AWSIotController mIot;

    public void getDeviceShadow() {
        mIot.getShadow(KEY_DCONNECT_SHADOW_NAME);
    }

    public List<RemoteDeviceConnectManager> parseDeviceShadow(final String message) {
        // TODO Shadowを解析して、RemoteDeviceConnectを追加する
        return null;
    }

    public void updateDeviceShadow(final RemoteDeviceConnectManager remote) {
        // TODO Remoteが登録されていない場合は追加すること
    }

    public String createRequest(final String request) {
        return createRequest(generateRequestCode(), request);
    }

    public String createRequest(final int requestCode, final String request) {
        return "{\"" + KEY_REQUEST + "\":" + request + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public String createResponse(final int requestCode, final String response) {
        return "{\"" + KEY_RESPONSE + "\":" + response + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public String createP2P(final String p2p) {
        return createP2P(generateRequestCode(), p2p);
    }

    public String createP2P(final int requestCode, final String p2p) {
        return "{\"" + KEY_P2P + "\":" + p2p + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    protected int generateRequestCode() {
        return UUID.randomUUID().hashCode();
    }
}
