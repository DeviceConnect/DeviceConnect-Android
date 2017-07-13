package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.device.ILIDARLiteV3;
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

public class I2CLIDARLiteProximityProfile extends BaseFaBoProfile {
    private ILIDARLiteV3.OnLIDARLiteListener mOnLIDARLiteListener;

    public I2CLIDARLiteProximityProfile() {
        // GET /gotapi/proximity/onUserProximity
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                ILIDARLiteV3 lidarLite = getFaBoDeviceControl().getLIDARLite();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (lidarLite == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    lidarLite.read(new ILIDARLiteV3.OnLIDARLiteListener() {
                        @Override
                        public void onStarted() {
                        }

                        @Override
                        public void onData(final int distance) {
                            response.putExtra("proximity", createProximity(distance));
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                        }

                        @Override
                        public void onError(final String message) {
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
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final ILIDARLiteV3 lidarLite = getFaBoDeviceControl().getLIDARLite();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (lidarLite == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    boolean empty = isEmptyEvent();
                    EventError error = INSTANCE.addEvent(request);
                    switch (error) {
                        case NONE:
                            if (empty) {
                                mOnLIDARLiteListener = new ILIDARLiteV3.OnLIDARLiteListener() {
                                    @Override
                                    public void onStarted() {
                                        setResult(response, DConnectMessage.RESULT_OK);
                                        sendResponse(response);
                                    }

                                    @Override
                                    public void onData(final int proximity) {
                                        notifyProximity(proximity);
                                    }

                                    @Override
                                    public void onError(final String message) {
                                        MessageUtils.setIllegalDeviceStateError(response, message);
                                        sendResponse(response);
                                        INSTANCE.removeEvent(request);
                                        lidarLite.stopRead(this);

                                    }
                                };
                                lidarLite.startRead(mOnLIDARLiteListener);
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
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final ILIDARLiteV3 lidarLite = getFaBoDeviceControl().getLIDARLite();
                if (lidarLite == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    EventError error = INSTANCE.removeEvent(request);
                    switch (error) {
                        case NONE:
                            if (isEmptyEvent() && mOnLIDARLiteListener != null) {
                                lidarLite.stopRead(mOnLIDARLiteListener);
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
    private Bundle createProximity(final int value) {
        Bundle proximity = new Bundle();
        proximity.putInt("value", value);
        return proximity;
    }

    /**
     * Arduinoから渡されてきた値をProximityとして通知します.
     * @param value 値が渡されてきたピン
     */
    private void notifyProximity(final int value) {
        String serviceId = getService().getId();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                "proximity", null, "onDeviceProximity");
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("proximity", createProximity(value));
            sendEvent(intent, event.getAccessToken());
        }
    }
}
