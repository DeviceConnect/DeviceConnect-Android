/*
 WearDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import java.util.List;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageEventListener;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageResultListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.wearable.MessageApi.SendMessageResult;

/**
 * DeviceOrientation Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearDeviceOrientationProfile extends DeviceOrientationProfile {

    /**
     * Android Wearからのイベントを受け取るリスナー.
     */
    private OnMessageEventListener listener = new OnMessageEventListener() {
        @Override
        public void onEvent(final String nodeId, final String message) {
            sendMessageToEvent(WearUtils.createServiceId(nodeId), message);
        }
    };

    /**
     * コンストラクタ.
     * @param mgr Android Wear管理クラス
     */
    public WearDeviceOrientationProfile(final WearManager mgr) {
        mgr.addMessageEventListener(WearConst.WEAR_TO_DEVICE_DEIVCEORIENTATION_DATA, listener);
    }

    @Override
    protected boolean onPutOnDeviceOrientation(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    getContext().sendBroadcast(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    getContext().sendBroadcast(response);
                }
            });
            return false;
        }
    }

    @Override
    protected boolean onDeleteOnDeviceOrientation(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, 
                    WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                }
                @Override
                public void onError() {
                }
            });

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
        }
        return true;
    }

    /**
     * Send a message to the registration event.
     * 
     * @param nodeId node id
     * @param data Received Strings.
     */
    private void sendMessageToEvent(final String nodeId, final String data) {
        String[] mDataArray = data.split(",", 0);

        Bundle orientation = new Bundle();
        Bundle a1 = new Bundle();
        a1.putDouble(DeviceOrientationProfile.PARAM_X, 0.0);
        a1.putDouble(DeviceOrientationProfile.PARAM_Y, 0.0);
        a1.putDouble(DeviceOrientationProfile.PARAM_Z, 0.0);

        Bundle a2 = new Bundle();
        a2.putDouble(DeviceOrientationProfile.PARAM_X, Double.parseDouble(mDataArray[0]));
        a2.putDouble(DeviceOrientationProfile.PARAM_Y, Double.parseDouble(mDataArray[1]));
        a2.putDouble(DeviceOrientationProfile.PARAM_Z, Double.parseDouble(mDataArray[2]));

        Bundle r = new Bundle();
        r.putDouble(DeviceOrientationProfile.PARAM_ALPHA, Double.parseDouble(mDataArray[3]));
        r.putDouble(DeviceOrientationProfile.PARAM_BETA, Double.parseDouble(mDataArray[4]));
        r.putDouble(DeviceOrientationProfile.PARAM_GAMMA, Double.parseDouble(mDataArray[5]));
        orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION, a1);
        orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION_INCLUDING_GRAVITY, a2);
        orientation.putBundle(DeviceOrientationProfile.PARAM_ROTATION_RATE, r);
        orientation.putLong(DeviceOrientationProfile.PARAM_INTERVAL, 0);
        setInterval(orientation, Integer.parseInt(mDataArray[6]));

        List<Event> events = EventManager.INSTANCE.getEventList(
                nodeId, PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_ORIENTATION);
        synchronized (events) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra(DeviceOrientationProfile.PARAM_ORIENTATION, orientation);
                ((WearDeviceService) getContext()).sendEvent(intent, event.getAccessToken());
            }
        }
    }

    /**
     * Android Wear管理クラスを取得する.
     * @return WearManager管理クラス
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
