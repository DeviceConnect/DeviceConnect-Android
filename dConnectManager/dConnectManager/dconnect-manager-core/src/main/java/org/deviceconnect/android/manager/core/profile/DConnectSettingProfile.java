/*
 DConnectSettingProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class DConnectSettingProfile extends DConnectProfile {

    public DConnectSettingProfile() {

        // GET /gotapi/setting/copyGuard
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "copyGuard";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                Bundle root = response.getExtras();
                root.putBoolean("enabled", false);
                response.putExtras(root);
                return true;
            }
        });

        // PUT /gotapi/setting/copyGuard
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "copyGuard";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // DELETE /gotapi/setting/copyGuard
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "copyGuard";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // PUT /gotapi/setting/copyGuard/onChange
        addApi(new PutApi() {
            @Override
            public String getInterface() {
                return "copyGuard";
            }

            @Override
            public String getAttribute() {
                return "onChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        setResult(response, DConnectMessage.RESULT_OK);

                        // 以下、サンプルのイベントの定期的送信を開始.
                        String taskId = serviceId;
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                Event event = EventManager.INSTANCE.getEvent(request);
                                Intent message = EventManager.createEventMessage(event);
                                Bundle root = message.getExtras();
                                root.putBoolean("enabled", false);
                                message.putExtras(root);
                                sendEvent(message, event.getAccessToken());
                            }
                        };
                        startTimer(taskId, task, 1000L);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // DELETE /gotapi/setting/copyGuard/onChange
        addApi(new DeleteApi() {
            @Override
            public String getInterface() {
                return "copyGuard";
            }

            @Override
            public String getAttribute() {
                return "onChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        setResult(response, DConnectMessage.RESULT_OK);

                        // 以下、サンプルのイベントの定期的送信を停止.
                        String taskId = serviceId;
                        stopTimer(taskId);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    case NOT_FOUND:
                        MessageUtils.setUnknownError(response, "Event is not registered.");
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "setting";
    }

    private final Map<String, TimerTask> mTimerTasks = new ConcurrentHashMap<>();
    private final Timer mTimer = new Timer();

    private void startTimer(final String taskId, final TimerTask task, final Long interval) {
        synchronized (mTimerTasks) {
            stopTimer(taskId);
            mTimerTasks.put(taskId, task);
            mTimer.scheduleAtFixedRate(task, 0, interval != null ? interval : 1000L);
        }
    }

    private void stopTimer(final String taskId) {
        synchronized (mTimerTasks) {
            TimerTask timer = mTimerTasks.remove(taskId);
            if (timer != null) {
                timer.cancel();
            }
        }
    }
}
