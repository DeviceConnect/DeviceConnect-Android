package org.deviceconnect.android.deviceplugin.awsiot.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AWSIotCore {

    public static final String KEY_DCONNECT_SHADOW_NAME = "DeviceConnect";

    public static final String KEY_REQUEST_CODE = "requestCode";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_RESPONSE = "response";
    public static final String KEY_P2P_REMOTE = "p2p_remote";
    public static final String KEY_P2P_LOCAL = "p2p_local";

    public static final String PARAM_SELF_FLAG = "_selfOnly";

    protected AWSIotController mIot;

    public void getDeviceShadow() {
        mIot.getShadow(KEY_DCONNECT_SHADOW_NAME);
    }

    public List<RemoteDeviceConnectManager> parseDeviceShadow(final String message) {
        // TODO Shadowを解析して、RemoteDeviceConnectを追加する

        List<RemoteDeviceConnectManager> managers = new ArrayList<>();
        managers.add(new RemoteDeviceConnectManager("abc", "test"));
//        managers.add(new RemoteDeviceConnectManager("5807D0DF-1D5F-4D8E-9779-A9417C5CA1D0", "test"));
        return managers;
    }

    public void updateDeviceShadow(final RemoteDeviceConnectManager remote) {
        // TODO Remoteが登録されていない場合は追加すること
    }

    public String createRequest(final int requestCode, final String request) {
        return "{\"" + KEY_REQUEST + "\":" + request + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public String createResponse(final int requestCode, final String response) {
        return "{\"" + KEY_RESPONSE + "\":" + response + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public String createRemoteP2P(final String p2p) {
        return createRemoteP2P(generateRequestCode(), p2p);
    }

    public String createRemoteP2P(final int requestCode, final String p2p) {
        return "{\"" + KEY_P2P_REMOTE + "\":" + p2p + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public String createLocalP2P(final String p2p) {
        return createLocalP2P(generateRequestCode(), p2p);
    }

    public String createLocalP2P(final int requestCode, final String p2p) {
        return "{\"" + KEY_P2P_LOCAL + "\":" + p2p + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    protected int generateRequestCode() {
        return UUID.randomUUID().hashCode();
    }
}
