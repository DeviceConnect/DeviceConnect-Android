package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

import static org.deviceconnect.android.event.EventManager.INSTANCE;

public class GPIOProximityProfile extends BaseFaBoProfile {
    /**
     * Humidity操作を行うピンのリスト.
     */
    private List<ArduinoUno.Pin> mPinList;

    public GPIOProximityProfile(final List<ArduinoUno.Pin> pinList) {
        mPinList = pinList;

        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        getFaBoDeviceService().addOnGPIOListener(mOnGPIOListenerImpl);
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });


        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onDeviceProximity";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        if (isEmptyEvent()) {
                            getFaBoDeviceService().removeOnGPIOListener(mOnGPIOListenerImpl);
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
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "proximity";
    }

    private boolean isEmptyEvent() {
        return true;
    }

    private int arduino_map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private void notifyProximity(final ArduinoUno.Pin pin) {
        String serviceId = getService().getId();

        int value = getFaBoDeviceService().getAnalogValue(pin);
        value = arduino_map(value, 0, 1023, 0, 5000);
        value = arduino_map(value, 3200, 500, 5, 80);

        // この範囲以外はデータおかしいので弾く
        if (value < 10 || value > 80) {
            return;
        }

        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                "proximity", null, "onDeviceProximity");
        for (Event event : events) {
            Bundle proximity = new Bundle();
            proximity.putInt("min", 10);
            proximity.putInt("max", 80);
            proximity.putInt("value", value);

            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("proximity", proximity);
            sendEvent(intent, event.getAccessToken());
        }
    }

    /**
     * GPIOの値変化の通知を受け取るリスナー.
     */
    private FaBoDeviceService.OnGPIOListener mOnGPIOListenerImpl = new FaBoDeviceService.OnGPIOListener() {
        @Override
        public void onAnalog() {
            for (ArduinoUno.Pin pin : mPinList) {
                notifyProximity(pin);
            }
        }

        @Override
        public void onDigital() {
        }
    };
}
