/*
 TestUniqueProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile.unique;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.deviceconnect.android.deviceplugin.test.DeviceTestService;
import org.deviceconnect.android.deviceplugin.test.profile.Util;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.content.Intent;
import android.os.Bundle;


/**
 * 独自プロファイルテスト用プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestUniqueProfile extends DConnectProfile {

    /**
     * プロファイル名: {@value} .
     */
    public static final String PROFILE_NAME = "unique";

    /**
     * インターフェース名: {@value} .
     */
    public static final String INTERFACE_TEST = "test";

    /**
     * 属性名: {@value} .
     */
    public static final String ATTRIBUTE_PING = "ping";

    /**
     * 属性名: {@value} .
     */
    public static final String ATTRIBUTE_HEAVY = "heavy";

    /**
     * 属性名: {@value} .
     */
    public static final String ATTRIBUTE_EVENT = "event";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_PATH = "path";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_KEY = "key";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_TIME = "time";

    /**
     * イベントのタイマー.
     */
    private Timer mEventTimer;

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    public boolean onRequest(final Intent request, final Intent response) {
        final String inter = getInterface(request);
        final String attribute = getAttribute(request);
        final String path = createPath(request);
        final String action = request.getAction();
        final String key = request.getStringExtra(PARAM_KEY);
        final String serviceId = getServiceID(request);
        if (inter == null && ATTRIBUTE_EVENT.equals(attribute)) {
            if (IntentDConnectMessage.ACTION_PUT.equals(action)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            
                            // 定期的イベントの開始
                            startEvent(serviceId);
                        } else {
                            MessageUtils.setUnknownError(response, "event error: " + error.name());
                        }
                        response.putExtra(PARAM_KEY, key);
                        ((DeviceTestService) getContext()).sendResponse(response);
                    }
                }).start();
                return false;
            } else if (IntentDConnectMessage.ACTION_DELETE.equals(action)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 定期的イベントの停止
                        stopEvent();
                        
                        EventManager.INSTANCE.removeEvent(request);
                        setResult(response, DConnectMessage.RESULT_OK);
                        response.putExtra(PARAM_KEY, key);
                        ((DeviceTestService) getContext()).sendResponse(response);
                    }
                }).start();
                return false;
            } else {
                MessageUtils.setUnknownAttributeError(response);
                return true;
            }
        } else if (inter == null && ATTRIBUTE_HEAVY.equals(attribute)) {
            if (key == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setResult(response, DConnectMessage.RESULT_OK);
                        response.putExtra(PARAM_KEY, key);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        MessageUtils.setUnknownError(response, "thread is interrupted. key=" + key);
                    }
                    ((DeviceTestService) getContext()).sendResponse(response);
                }
            }).start();
            return false;
        } else if ((inter == null && attribute == null)
                || (inter == null && ATTRIBUTE_PING.equals(attribute))
                || (INTERFACE_TEST.equals(inter) && ATTRIBUTE_PING.equals(attribute))) {
            setResult(response, DConnectMessage.RESULT_OK);
            setPath(response, path);
            Bundle params = request.getExtras();
            response.putExtra("key1", params.getString("key1"));
            response.putExtra("key2", params.getString("key2"));
            response.putExtra("key3", params.getString("key3"));
            response.putExtra("key4", params.getString("key4"));
            return true;
        } else {
            MessageUtils.setUnknownAttributeError(response);
            return true;
        }
    }

    /**
     * Start Event.
     * @param serviceId serviceId
     */
    private synchronized void startEvent(final String serviceId) {
        if (mEventTimer == null) {
            mEventTimer = new Timer(true);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    List<Event> events = EventManager.INSTANCE.getEventList(
                          serviceId, getProfileName(), null, ATTRIBUTE_EVENT);
                    for (Event event : events) {
                        Intent eventMsg = EventManager.createEventMessage(event);
                        eventMsg.putExtra(PARAM_TIME, System.currentTimeMillis());
                        Util.sendBroadcast(getContext(), eventMsg, 0);
                    }
                }
            };
            mEventTimer.schedule(task, 0, 500);
        }
    }

    /**
     * Stop Event.
     */
    private synchronized void stopEvent() {
        if (mEventTimer != null) {
            mEventTimer.cancel();
            mEventTimer = null;
        }
    }

    /**
     * Set Path.
     * @param response Response
     * @param path Path
     */
    private void setPath(final Intent response, final String path) {
        response.putExtra(PARAM_PATH, path);
    }

    /**
     * Create Path.
     * @param request Request
     * @return Create Path
     */
    private String createPath(final Intent request) {
        String action = request.getAction();
        if (action == null) {
            return null;
        }
        String method;
        if (IntentDConnectMessage.ACTION_GET.equals(action)) {
            method = "GET";
        } else if (IntentDConnectMessage.ACTION_POST.equals(action)) {
            method = "POST";
        } else if (IntentDConnectMessage.ACTION_PUT.equals(action)) {
            method = "PUT";
        } else if (IntentDConnectMessage.ACTION_DELETE.equals(action)) {
            method = "DELETE";
        } else {
            return null;
        }
        String inter = getInterface(request);
        String attribute = getAttribute(request);
        StringBuilder builder = new StringBuilder();
        builder.append(method);
        builder.append(" /");
        builder.append(PROFILE_NAME);
        if (inter != null) {
            builder.append("/");
            builder.append(inter);
        }
        if (attribute != null) {
            builder.append("/");
            builder.append(attribute);
        }
        return builder.toString();
    }
}
