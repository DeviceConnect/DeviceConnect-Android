/*
 IRKitRmeoteControllerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;


import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.irkit.BuildConfig;
import org.deviceconnect.android.deviceplugin.irkit.IRKitDevice;
import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.IRKitManager;
import org.deviceconnect.android.deviceplugin.irkit.IRKitManager.GetMessageCallback;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * IRKit Remote Controller Profile.
 * @author NTT DOCOMO, INC.
 */
public class IRKitRemoteControllerProfile extends DConnectProfile {
    
    /** Debug . */
    private static final String TAG = "IRKit";

    /** プロファイル名. */
    public static final String PROFILE_NAME = "remoteController";
    
    /** 
     * パラメータ: {@value} .
     */
    public static final String PARAM_MESSAGE = "message";

    public IRKitRemoteControllerProfile() {
        addApi(mGetApi);
        addApi(mPostApi);
    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    private final DConnectApi mGetApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            boolean send = true;

            String serviceId = getServiceID(request);
            final IRKitDeviceService service = (IRKitDeviceService) getContext();
            IRKitDevice device = service.getDevice(serviceId);

            if (device == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                send = false;
                IRKitManager.INSTANCE.fetchMessage(device.getIp(), new GetMessageCallback() {

                    @Override
                    public void onGetMessage(final String message) {
                        if (message == null) {
                            MessageUtils.setUnknownError(response);
                        } else {
                            response.putExtra(PARAM_MESSAGE, message);
                            setResult(response, DConnectMessage.RESULT_OK);
                        }
                        service.sendResponse(response);
                    }
                });
            }
            return send;
        }
    };

    private final DConnectApi mPostApi = new PostApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            boolean send = true;
            String serviceId = getServiceID(request);
            String message = request.getStringExtra(PARAM_MESSAGE);

            final IRKitDeviceService service = (IRKitDeviceService) getContext();
            IRKitDevice device = service.getDevice(serviceId);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPostRequest service=" + service + " device" + device);
            }

            if (device == null) {
                MessageUtils.setNotFoundServiceError(response);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostRequest setNotFoundServiceError");
                }
            } else if (!checkData(message)) {
                MessageUtils.setInvalidRequestParameterError(response);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostRequest setInvalidRequestParameterError");
                }
            } else {
                send = false;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostRequest ip=" + device.getIp() + " message=" + message);
                }
                IRKitManager.INSTANCE.sendMessage(device.getIp(), message, new IRKitManager.PostMessageCallback() {
                    @Override
                    public void onPostMessage(final boolean result) {
                        if (result) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onPostRequest setUnknownError");
                            }
                            MessageUtils.setUnknownError(response);
                        }
                        service.sendResponse(response);
                    }
                });
            }
            return send;
        }
    };

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
}
