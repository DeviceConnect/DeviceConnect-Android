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

/**
 * GPIO用のProximityプロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #116<br>
 * Name: Distance Brick<br>
 * </p>
 */
public class GPIOProximityProfile extends BaseFaBoProfile {
    /**
     * Proximity操作を行うピンのリスト.
     */
    private List<ArduinoUno.Pin> mPinList;

    /**
     * コンストラクタ.
     * @param pinList 操作を行うピンのリスト
     */
    public GPIOProximityProfile(final List<ArduinoUno.Pin> pinList) {
        mPinList = pinList;

        // PUT /gotapi/proximity/onDeviceProximity
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

        // DELETE /gotapi/proximity/onDeviceProximity
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
     * Arduinoから渡されてきた値をProximityとして通知します.
     * @param pin 値が渡されてきたピン
     */
    private void notifyProximity(final ArduinoUno.Pin pin) {
        String serviceId = getService().getId();

        int value = getFaBoDeviceService().getAnalogValue(pin);
        value = calcArduinoMap(value, 0, 1023, 0, 5000);
        value = calcArduinoMap(value, 3200, 500, 5, 80);

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
