package org.deviceconnect.android.deviceplugin.test.profile.unique;


import android.content.Intent;

import org.deviceconnect.android.deviceplugin.test.profile.TestServiceDiscoveryProfile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestRemoteControllerProfile extends DConnectProfile {

    /** プロファイル名. */
    public static final String PROFILE_NAME = "remoteController";

    /**
     * パラメータ: {@value} .
     */
    public static final String PARAM_MESSAGE = "message";

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    public boolean onGetRequest(final Intent request, final Intent response) {

        String attribute = getAttribute(request);
        if (attribute != null && attribute.length() != 0) {
            MessageUtils.setUnknownAttributeError(response);
        } else {
            String serviceId = getServiceID(request);
            if (!checkServiceId(serviceId)) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
            }
        }
        return true;
    }

    @Override
    public boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        if (attribute != null && attribute.length() != 0) {
            MessageUtils.setUnknownAttributeError(response);
        } else {
            String serviceId = getServiceID(request);
            String message = request.getStringExtra(PARAM_MESSAGE);
            if (!checkServiceId(serviceId)) {
                MessageUtils.setNotFoundServiceError(response);
            } else if (!checkData(message)) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
            }
        }
        return true;
    }

    /**
     * 送られてきたデータがIRKitに対応しているかチェックを行う.
     * @param message データ
     * @return フォーマットが問題ない場合はtrue、それ以外はfalse
     */
    private boolean checkData(final String message) {
        if (message == null || message.length() == 0) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(message);
            String format = json.getString("format");
            int freq = json.getInt("freq");
            JSONArray datas = json.getJSONArray("data");
            return (format != null && freq > 0 && datas != null);
        } catch (JSONException e) {
            return false;
        }
    }

    private boolean checkServiceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

}
