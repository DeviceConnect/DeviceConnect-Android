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
     * VCNL4010からの通知を受け取るリスナー.
     */
    private IVCNL4010.OnProximityListener mOnProximityListener;

    /**
     * コンストラクタ.
     */
    public I2CProximityProfile() {
        // GET /gotapi/proximity/onUserProximity
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onUserProximity";
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
                        public void onData(final boolean proximity) {
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

        // PUT /gotapi/proximity/onUserProximity
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onUserProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final IVCNL4010 ivcnl4010 = getFaBoDeviceControl().getVCNL4010();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (ivcnl4010 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    boolean empty = isEmptyEvent();
                    EventError error = INSTANCE.addEvent(request);
                    switch (error) {
                        case NONE:
                            if (empty) {
                                mOnProximityListener = new IVCNL4010.OnProximityListener() {
                                    @Override
                                    public void onStarted() {
                                        setResult(response, DConnectMessage.RESULT_OK);
                                        sendResponse(response);
                                    }

                                    @Override
                                    public void onData(final boolean proximity) {
                                        notifyProximity(proximity);
                                    }

                                    @Override
                                    public void onError(final String message) {
                                        MessageUtils.setIllegalDeviceStateError(response, message);
                                        sendResponse(response);
                                        EventManager.INSTANCE.removeEvent(request);
                                        ivcnl4010.stopProximity(mOnProximityListener);

                                    }
                                };
                                ivcnl4010.startProximity(mOnProximityListener);
                                return false;
                            }
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

        // DELETE /gotapi/proximity/onUserProximity
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onUserProximity";
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
                            if (isEmptyEvent() && mOnProximityListener != null) {
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
                "proximity", null, "onUserProximity");
        return events.isEmpty();
    }

    /**
     * Proximityのオブジェクトを作成します.
     * @param value 距離
     * @return Proximityのオブジェクト
     */
    private Bundle createProximity(final boolean value) {
        Bundle proximity = new Bundle();
        proximity.putBoolean("near", value);
        return proximity;
    }

    /**
     * Arduinoから渡されてきた値をProximityとして通知します.
     * @param value 値が渡されてきたピン
     */
    private void notifyProximity(final boolean value) {
        String serviceId = getService().getId();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                "proximity", null, "onUserProximity");
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("proximity", createProximity(value));
            sendEvent(intent, event.getAccessToken());
        }
    }
}
