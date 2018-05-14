/*
 WearDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageEventListener;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageResultListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DeviceOrientation Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearDeviceOrientationProfile extends DeviceOrientationProfile {

    /**
     * 通知先のリスナーを保持するリスト.
     */
    private final List<OnMessageEventListener> mListeners = 
            Collections.synchronizedList(new ArrayList<OnMessageEventListener>());

    /**
     * Android Wearからのイベントを受け取るリスナー.
     */
    private OnMessageEventListener mListener = new OnMessageEventListener() {
        @Override
        public void onEvent(final String nodeId, final String message) {
            synchronized (mListeners) {
                for (OnMessageEventListener l : mListeners) {
                    l.onEvent(WearUtils.createServiceId(nodeId), message);
                }
            }
        }
    };

    /**
     * コンストラクタ.
     * @param mgr Android Wear管理クラス
     */
    public WearDeviceOrientationProfile(final WearManager mgr) {
        mgr.addMessageEventListener(WearConst.WEAR_TO_DEVICE_DEIVCEORIENTATION_DATA, mListener);
        mListeners.add(new OnMessageEventListener() {
            @Override
            public void onEvent(final String nodeId, final String message) {
                sendMessageToEvent(nodeId, message);
            }
        });
        addApi(mGetOnDeviceOrientation);
        addApi(mPutOnDeviceOrientation);
        addApi(mDeleteOnDeviceOrientation);
    }

    private final DConnectApi mGetOnDeviceOrientation = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final WearDeviceService service = (WearDeviceService) getContext();
            final String serviceId = WearUtils.getNodeId(getServiceID(request));
            final OnMessageEventListener l = new OnMessageEventListener() {
                @Override
                public void onEvent(final String nodeId, final String message) {
                    Bundle orientation = createOrientation(message);
                    setResult(response, DConnectMessage.RESULT_OK);
                    setOrientation(response, orientation);
                    service.sendResponse(response);
                    removeListener(this);
                    if (isEmptyEvent(serviceId)) {
                        stopSensor(serviceId);
                    }
                }
            };
            addListener(l);

            getManager().sendMessageToWear(serviceId,
                WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                    }
                    @Override
                    public void onError() {
                        removeListener(l);
                        MessageUtils.setIllegalDeviceStateError(response);
                        service.sendResponse(response);
                    }
                });
            return false;
        }
    };

    private final DConnectApi mPutOnDeviceOrientation = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                        sendResponse(response);
                    }
                    @Override
                    public void onError() {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendResponse(response);
                    }
                });
            return false;
        }
    };

    private final DConnectApi mDeleteOnDeviceOrientation = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            stopSensor(nodeId);

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    /**
     * イベントの配送先の空状態を取得する.
     * @param nodeId wearのID
     * @return 空の場合はtrue、それ以外はfalse
     */
    private boolean isEmptyEvent(final String nodeId) {
        List<Event> events = EventManager.INSTANCE.getEventList(
                nodeId, PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_ORIENTATION);
        return events == null || events.size() == 0;
    }

    /**
     * 指定されたWearのセンサーを停止する.
     * @param nodeId wearのID
     */
    private void stopSensor(final String nodeId) {
        getManager().sendMessageToWear(nodeId, 
                WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER,
                "", new OnMessageResultListener() {
            @Override
            public void onResult() {
            }
            @Override
            public void onError() {
            }
        });
    }

    /**
     * リスナーを追加する.
     * @param l 追加するリスナー
     */
    private void addListener(final OnMessageEventListener l) {
        if (!mListeners.contains(l)) {
            mListeners.add(l);
        }
    }

    /**
     * リスナーを削除する.
     * @param l 削除するリスナー
     */
    private void removeListener(final OnMessageEventListener l) {
        mListeners.remove(l);
    }

    /**
     * Send a message to the registration event.
     * 
     * @param nodeId node id
     * @param data Received Strings.
     */
    private void sendMessageToEvent(final String nodeId, final String data) {
        Bundle orientation = createOrientation(data);

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
     * 受信したデータからOrientationのデータを作成する.
     * @param data 受信したデータ
     * @return Orientationのデータ
     */
    private Bundle createOrientation(final String data) {
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

        // Conversion the unit of angular speed from rad/s to degree/s.
        double alpha = Math.toDegrees(Double.parseDouble(mDataArray[3]));
        double beta = Math.toDegrees(Double.parseDouble(mDataArray[4]));
        double gamma = Math.toDegrees(Double.parseDouble(mDataArray[5]));
        Bundle r = new Bundle();
        r.putDouble(DeviceOrientationProfile.PARAM_ALPHA, alpha);
        r.putDouble(DeviceOrientationProfile.PARAM_BETA, beta);
        r.putDouble(DeviceOrientationProfile.PARAM_GAMMA, gamma);
        orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION, a1);
        orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION_INCLUDING_GRAVITY, a2);
        orientation.putBundle(DeviceOrientationProfile.PARAM_ROTATION_RATE, r);
        orientation.putLong(DeviceOrientationProfile.PARAM_INTERVAL, 0);
        setInterval(orientation, Integer.parseInt(mDataArray[6]));
        return orientation;
    }

    /**
     * Android Wear管理クラスを取得する.
     * @return WearManager管理クラス
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
