package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.device.IVCNL4010;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

import static org.deviceconnect.android.event.EventManager.INSTANCE;

/**
 * I2C用Proximityプロファイル.
 * <p>
 * ID: #205<br>
 * Name: Proximity I2C Brick<br>
 * </p>
 */
public class I2CProximityProfile extends BaseFaBoProfile {
    /**
     * イベントを送信するためのインターバル.
     */
    private long mInterval = 100;

    /**
     * 前回送信したイベントの時間.
     */
    private long mSendTime;

    /**
     * コンストラクタ.
     */
    public I2CProximityProfile() {
        // GET /gotapi/proximity/onDeviceProximity
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {

                IVCNL4010 ivcnl4010 = getFaBoDeviceControl().getVCNL4010();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (ivcnl4010 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    ivcnl4010.readProximity(new IVCNL4010.OnProximityListener() {
                        @Override
                        public void onStarted() {
                        }

                        @Override
                        public void onData(double proximity) {
                            response.putExtra("proximity", createProximity(proximity));
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                        }

                        @Override
                        public void onError(String message) {
                            MessageUtils.setIllegalDeviceStateError(response, message);
                            sendResponse(response);
                        }
                    });
                }
                return false;
            }
        });

        // PUT /gotapi/proximity/onDeviceProximity
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final Integer interval = parseInteger(request, "interval");
                if (interval != null) {
                    mInterval = interval;
                } else {
                    mInterval = 100;
                }

                IVCNL4010 ivcnl4010 = getFaBoDeviceControl().getVCNL4010();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (ivcnl4010 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    EventError error = INSTANCE.addEvent(request);
                    switch (error) {
                        case NONE:
                            ivcnl4010.startProximity(mOnProximityListener);
                            setResult(response, DConnectMessage.RESULT_OK);
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                }
                return true;
            }
        });

        // DELETE /gotapi/proximity/onDeviceProximity
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                IVCNL4010 ivcnl4010 = getFaBoDeviceControl().getVCNL4010();
                if (ivcnl4010 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    EventError error = INSTANCE.removeEvent(request);
                    switch (error) {
                        case NONE:
                            if (isEmptyEvent()) {
                                ivcnl4010.stopProximity(mOnProximityListener);
                            }
                            setResult(response, DConnectMessage.RESULT_OK);
                            break;
                        case NOT_FOUND:
                            MessageUtils.setIllegalDeviceStateError(response, "Not register event.");
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                }
                return true;
            }
        });
    }
    @Override
    public String getProfileName() {
        return "proximity";
    }

    /**
     * 登録されているイベントが空か確認します.
     * @return イベントが登録されていない場合はtrue、それ以外はfalse
     */
    private boolean isEmptyEvent() {
        String serviceId = getService().getId();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                "proximity", null, "onDeviceProximity");
        return events.isEmpty();
    }

    /**
     * Proximityのオブジェクトを作成します.
     * @param value 距離
     * @return Proximityのオブジェクト
     */
    private Bundle createProximity(double value) {
        Bundle proximity = new Bundle();
        proximity.putFloat("min", 0.1f);
        proximity.putFloat("max", 2.0f);
        proximity.putDouble("value", value);
        return proximity;
    }

    /**
     * Arduinoから渡されてきた値をProximityとして通知します.
     * @param value 値が渡されてきたピン
     */
    private void notifyProximity(double value) {
        long interval = (System.currentTimeMillis() - mSendTime);
        if (interval >= mInterval) {
            String serviceId = getService().getId();
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    "proximity", null, "onDeviceProximity");
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra("proximity", createProximity(value));
                sendEvent(intent, event.getAccessToken());
            }

            mSendTime = System.currentTimeMillis();
        }
    }

    private IVCNL4010.OnProximityListener mOnProximityListener = new IVCNL4010.OnProximityListener() {
        @Override
        public void onData(final double proximity) {
            notifyProximity(proximity);
        }

        @Override
        public void onError(final String message) {
        }

        @Override
        public void onStarted() {

        }
    };
}
