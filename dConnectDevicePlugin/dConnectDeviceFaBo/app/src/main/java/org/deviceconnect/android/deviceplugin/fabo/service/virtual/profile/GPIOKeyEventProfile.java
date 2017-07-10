package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GPIO用のKeyEventプロファイル.
 * <p>
 *  ID: #103<br>
 *  Name: Button Brick<br>
 * </p>
 */
public class GPIOKeyEventProfile extends BaseFaBoProfile {

    /**
     * KeyEvent操作を行うピンのリスト.
     */
    private List<FaBoShield.Pin> mPinList;

    /**
     * ピンの値を保持するためのMap.
     */
    private Map<FaBoShield.Pin, Integer> mValues = new HashMap<>();

    /**
     * コンストラクタ.
     *
     * @param pinList KeyEventに対応するPinのリスト
     */
    public GPIOKeyEventProfile(final List<FaBoShield.Pin> pinList) {
        mPinList = pinList;

        // PUT /gotapi/keyEvent/onDown
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onDown";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        getFaBoDeviceControl().addOnGPIOListener(mOnGPIOListenerImpl);
                        for (FaBoShield.Pin pin : mPinList) {
                            if (pin.getMode() == FaBoShield.Mode.ANALOG) {
                                mValues.put(pin, getFaBoDeviceControl().getAnalog(pin));
                            } else {
                                getFaBoDeviceControl().setPinMode(pin, FaBoShield.Mode.GPIO_IN);
                                mValues.put(pin, getFaBoDeviceControl().getDigital(pin).getValue());
                            }
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // DELETE /gotapi/keyEvent/onDown
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onDown";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        if (isEmptyEvent()) {
                            getFaBoDeviceControl().removeOnGPIOListener(mOnGPIOListenerImpl);
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

        // PUT /gotapi/keyEvent/onUp
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onUp";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        getFaBoDeviceControl().addOnGPIOListener(mOnGPIOListenerImpl);
                        for (FaBoShield.Pin pin : mPinList) {
                            if (pin.getMode() == FaBoShield.Mode.ANALOG) {
                                mValues.put(pin, getFaBoDeviceControl().getAnalog(pin));
                            } else {
                                getFaBoDeviceControl().setPinMode(pin, FaBoShield.Mode.GPIO_IN);
                                mValues.put(pin, getFaBoDeviceControl().getDigital(pin).getValue());
                            }
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // DELETE /gotapi/keyEvent/onUp
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onUp";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        if (isEmptyEvent()) {
                            getFaBoDeviceControl().removeOnGPIOListener(mOnGPIOListenerImpl);
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

        // PUT /gotapi/keyEvent/onKeyChange
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        getFaBoDeviceControl().addOnGPIOListener(mOnGPIOListenerImpl);
                        for (FaBoShield.Pin pin : mPinList) {
                            if (pin.getMode() == FaBoShield.Mode.ANALOG) {
                                mValues.put(pin, getFaBoDeviceControl().getAnalog(pin));
                            } else {
                                getFaBoDeviceControl().setPinMode(pin, FaBoShield.Mode.GPIO_IN);
                                mValues.put(pin, getFaBoDeviceControl().getDigital(pin).getValue());
                            }
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // DELETE /gotapi/keyEvent/onKeyChange
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        if (isEmptyEvent()) {
                            getFaBoDeviceControl().removeOnGPIOListener(mOnGPIOListenerImpl);
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
        return "keyEvent";
    }

    /**
     * イベント登録が空か確認を行います.
     * @return イベント登録がされていない場合はtrue、それ以外はfalse
     */
    private boolean isEmptyEvent() {
        String serviceId = getService().getId();

        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                "keyEvent", null, "onKeyChange");
        if (!events.isEmpty()) {
            return false;
        }

        events = EventManager.INSTANCE.getEventList(serviceId,
                "keyEvent", null, "onDown");
        if (!events.isEmpty()) {
            return false;
        }

        events = EventManager.INSTANCE.getEventList(serviceId,
                "keyEvent", null, "onUp");

        return events.isEmpty();
    }

    /**
     * KeyDownイベントを通知します.
     * @param pin Keyが押されたピン
     */
    private synchronized void notifyKeyDown(final FaBoShield.Pin pin) {
        String serviceId = getService().getId();

        {
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    "keyEvent", null, "onKeyChange");
            for (Event event : events) {
                Bundle keyEvent = new Bundle();
                keyEvent.putString("state", "down");
                keyEvent.putInt("id", pin.getPinNumber());
                keyEvent.putString("config", pin.getPinNames()[1]);

                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra("keyevent", keyEvent);
                sendEvent(intent, event.getAccessToken());
            }
        }

        {
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    "keyEvent", null, "onDown");
            for (Event event : events) {
                Bundle keyEvent = new Bundle();
                keyEvent.putInt("id", pin.getPinNumber());
                keyEvent.putString("config", pin.getPinNames()[1]);

                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra("keyevent", keyEvent);
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    /**
     * KeyUpイベントを通知します.
     * @param pin Keyが離されたピン
     */
    private synchronized void notifyKeyUp(final FaBoShield.Pin pin) {
        String serviceId = getService().getId();
        {
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    "keyEvent", null, "onKeyChange");
            for (Event event : events) {
                Bundle keyEvent = new Bundle();
                keyEvent.putString("state", "up");
                keyEvent.putInt("id", pin.getPinNumber());
                keyEvent.putString("config", pin.getPinNames()[1]);

                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra("keyevent", keyEvent);
                sendEvent(intent, event.getAccessToken());
            }
        }
        {
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    "keyEvent", null, "onUp");
            for (Event event : events) {
                Bundle keyEvent = new Bundle();
                keyEvent.putInt("id", pin.getPinNumber());
                keyEvent.putString("config", pin.getPinNames()[1]);

                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra("keyevent", keyEvent);
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    /**
     * GPIOの値変化の通知を受け取るリスナー.
     */
    private FaBoDeviceControl.OnGPIOListener mOnGPIOListenerImpl = new FaBoDeviceControl.OnGPIOListener() {
        @Override
        public void onAnalog() {
            for (FaBoShield.Pin pin : mPinList) {
                if (pin.getMode() == FaBoShield.Mode.ANALOG) {
                    int oldValue = mValues.get(pin);
                    int newValue = getFaBoDeviceControl().getAnalog(pin);
                    if (oldValue < 10 && newValue > 1000) {
                        notifyKeyDown(pin);
                    } else if (oldValue > 1000 && newValue < 10) {
                       notifyKeyUp(pin);
                    }
                    mValues.put(pin, newValue);
                }
            }
        }

        @Override
        public void onDigital() {
            for (FaBoShield.Pin pin : mPinList) {
                if (pin.getMode() != FaBoShield.Mode.ANALOG) {
                    int oldValue = mValues.get(pin);
                    int newValue = getFaBoDeviceControl().getDigital(pin).getValue();
                    if (oldValue == 0 && newValue == 1) {
                        notifyKeyDown(pin);
                    } else if (oldValue == 1 && newValue == 0) {
                        notifyKeyUp(pin);
                    }
                    mValues.put(pin, newValue);
                }
            }
        }
    };
}
