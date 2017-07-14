/*
FaBoGPIOProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.GPIOProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;

import static org.deviceconnect.message.DConnectMessage.RESULT_OK;

/**
 * GPIO Profile.
 * @author NTT DOCOMO, INC.
 */
public class FaBoGPIOProfile extends GPIOProfile {

    private void addGetAnalogApi(final FaBoShield.Pin pin) {
        // GET /gpio/analog/{pinName}
        switch (pin) {
            case PIN_A0:
            case PIN_A1:
            case PIN_A2:
            case PIN_A3:
            case PIN_A4:
            case PIN_A5:
                for (final String pinName : pin.getPinNames()) {
                    addApi(new GetApi() {
                        @Override
                        public String getInterface() {
                            return INTERFACE_ANALOG;
                        }

                        @Override
                        public String getAttribute() {
                            return pinName;
                        }

                        @Override
                        public boolean onRequest(final Intent request, final Intent response) {
                            if (!isOnline()) {
                                MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                            } else {
                                if (getFaBoDeviceControl().isPinSupported(pin)) {
                                    int value = getFaBoDeviceControl().getAnalog(pin);
                                    setValue(response, value);
                                    setResult(response, RESULT_OK);
                                } else {
                                    MessageUtils.setNotSupportAttributeError(response, pin.getPinNames()[1] + " is not supported.");
                                }
                            }
                            return true;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    private void addGetDigitalApi(final FaBoShield.Pin pin) {
        // GET /gpio/digital/{pinName}
        switch (pin) {
            case PIN_D0:
            case PIN_D1:
            case PIN_D2:
            case PIN_D3:
            case PIN_D4:
            case PIN_D5:
            case PIN_D6:
            case PIN_D7:
            case PIN_D8:
            case PIN_D9:
            case PIN_D10:
            case PIN_D11:
            case PIN_D12:
            case PIN_D13:
                for (final String pinName : pin.getPinNames()) {
                    addApi(new GetApi() {
                        @Override
                        public String getInterface() {
                            return INTERFACE_DIGITAL;
                        }

                        @Override
                        public String getAttribute() {
                            return pinName;
                        }

                        @Override
                        public boolean onRequest(final Intent request, final Intent response) {
                            if (!isOnline()) {
                                MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                            } else {
                                if (getFaBoDeviceControl().isPinSupported(pin)) {
                                    FaBoShield.Level value = getFaBoDeviceControl().getDigital(pin);
                                    setValue(response, value.getValue());
                                    setResult(response, RESULT_OK);
                                } else {
                                    MessageUtils.setNotSupportAttributeError(response, pin.getPinNames()[1] + " is not support.");
                                }
                            }
                            return true;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    private void addPostExportApi(final FaBoShield.Pin pin) {
        // POST /gpio/export/{pinName}
        for (final String pinName : pin.getPinNames()) {
            addApi(new PostApi() {
                @Override
                public String getInterface() {
                    return INTERFACE_EXPORT;
                }

                @Override
                public String getAttribute() {
                    return pinName;
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
                    if (!isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                    } else {
                        if (getFaBoDeviceControl().isPinSupported(pin)) {
                            Integer modeValue = parseInteger(request, "mode");
                            if (modeValue != null) {
                                FaBoShield.Mode mode = FaBoShield.Mode.getMode(modeValue);
                                if (mode != null) {
                                    getFaBoDeviceControl().setPinMode(pin, mode);
                                    setMessage(response, pinName + "を" + mode.getName() + "モードに設定しました。");
                                    setResult(response, RESULT_OK);
                                } else {
                                    MessageUtils.setInvalidRequestParameterError(response, "The value of mode must be defined 0-4.");
                                }
                            } else {
                                MessageUtils.setInvalidRequestParameterError(response, "The value of mode is null.");
                            }
                        } else {
                            MessageUtils.setNotSupportAttributeError(response, pin.getPinNames()[1] + " is not supported.");
                        }
                    }
                    return true;
                }
            });
        }
    }

    private void addPostDigitalApi(final FaBoShield.Pin pin) {
        // POST /gpio/digital/{pinName}
        for (final String pinName : pin.getPinNames()) {
            addApi(new PostApi() {
                @Override
                public String getInterface() {
                    return INTERFACE_DIGITAL;
                }

                @Override
                public String getAttribute() {
                    return pinName;
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
                    if (!isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                    } else {
                        if (getFaBoDeviceControl().isPinSupported(pin)) {
                            Integer hlValue = parseInteger(request, PARAM_VALUE);
                            if (hlValue != null) {
                                FaBoShield.Level level = FaBoShield.Level.getLevel(hlValue);
                                if (level != null) {
                                    getFaBoDeviceControl().writeDigital(pin, level);
                                    setMessage(response, pinName + "の値を" + level.getName() + "(" + level.getValue() + ")に変更");
                                    setResult(response, RESULT_OK);
                                } else {
                                    MessageUtils.setInvalidRequestParameterError(response, "Value must be defined 1 or 0.");
                                }
                            } else {
                                MessageUtils.setInvalidRequestParameterError(response, "Value is null.");
                            }
                        } else {
                            MessageUtils.setNotSupportAttributeError(response, pin.getPinNames()[1] + " is not supported.");
                        }
                    }
                    return true;
                }
            });
        }
    }
    
    private void addPostAnalogApi(final FaBoShield.Pin pin) {
        // POST /gpio/analog/{pinName}
        switch (pin) {
            case PIN_D3:
            case PIN_D5:
            case PIN_D6:
            case PIN_D9:
            case PIN_D10:
            case PIN_D11:
                for (final String pinName : pin.getPinNames()) {
                    addApi(new PostApi() {
                        @Override
                        public String getInterface() {
                            return INTERFACE_ANALOG;
                        }

                        @Override
                        public String getAttribute() {
                            return pinName;
                        }

                        @Override
                        public boolean onRequest(final Intent request, final Intent response) {
                            if (!isOnline()) {
                                MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                            } else {
                                if (getFaBoDeviceControl().isPinSupported(pin)) {
                                    Integer hlValue = parseInteger(request, PARAM_VALUE);
                                    if (hlValue != null) {
                                        if (hlValue >= 0 && hlValue <= 255) {
                                            getFaBoDeviceControl().writeAnalog(pin, hlValue);
                                            setResult(response, RESULT_OK);
                                        } else {
                                            MessageUtils.setInvalidRequestParameterError(response, "Value must be defined under 255.");
                                        }
                                    } else {
                                        MessageUtils.setInvalidRequestParameterError(response, "Value is null.");
                                    }
                                } else {
                                    MessageUtils.setNotSupportAttributeError(response, pin.getPinNames()[1] + " is not supported.");
                                }
                            }
                            return true;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    private void addPutOnChangeApi() {
        // PUT /gpio/onChange
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return ATTRIBUTE_ON_CHANGE;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (EventError.NONE == error) {
                    getFaBoDeviceService().registerOnChange(getServiceID(request));
                    setResult(response, RESULT_OK);
                    return true;
                } else {
                    MessageUtils.setError(response, 100, "Failed add event.");
                    return true;
                }
            }
        });
    }

    private void addPutDigitalApi(final FaBoShield.Pin pin) {
        // PUT /gpio/digital/{pinName}
        for (final String pinName : pin.getPinNames()) {
            addApi(new PutApi() {
                @Override
                public String getInterface() {
                    return INTERFACE_DIGITAL;
                }

                @Override
                public String getAttribute() {
                    return pinName;
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
                    if (!isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                    } else {
                        if (getFaBoDeviceControl().isPinSupported(pin)) {
                            getFaBoDeviceControl().writeDigital(pin, FaBoShield.Level.HIGH);
                            setMessage(response, pinName + "の値をHIGH(1)に変更");
                            setResult(response, RESULT_OK);
                        } else {
                            MessageUtils.setNotSupportAttributeError(response, pin.getPinNames()[1] + " is not supported.");
                        }
                    }
                    return true;
                }
            });
        }
    }

    private void addDeleteOnChangeApi() {
        // DELETE /gpio/onChange
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return ATTRIBUTE_ON_CHANGE;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                boolean result = EventManager.INSTANCE.removeEvents(getOrigin(request));
                if (result) {
                    getFaBoDeviceService().unregisterOnChange(getServiceID(request));
                    setResult(response, RESULT_OK);
                    return true;
                } else {
                    MessageUtils.setError(response, 100, "Failed delete event.");
                    return true;
                }
            }
        });
    }

    private void addDeleteDigitalApi(final FaBoShield.Pin pin) {
        // DELETE /gpio/digital/{pinName}
        for (final String pinName : pin.getPinNames()) {
            addApi(new DeleteApi() {
                @Override
                public String getInterface() {
                    return INTERFACE_DIGITAL;
                }

                @Override
                public String getAttribute() {
                    return pinName;
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
                    if (!isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                    } else {
                        if (getFaBoDeviceControl().isPinSupported(pin)) {
                            getFaBoDeviceControl().writeDigital(pin, FaBoShield.Level.LOW);
                            setMessage(response, pinName + "の値をLOW(0)に変更");
                            setResult(response, RESULT_OK);
                        } else {
                            MessageUtils.setNotSupportAttributeError(response, pin.getPinNames()[1] + " is not supported.");
                        }
                    }
                    return true;
                }
            });
        }
    }

    public FaBoGPIOProfile() {
        for (FaBoShield.Pin pin : FaBoShield.Pin.values()) {
            addGetAnalogApi(pin);
            addGetDigitalApi(pin);
            addPostExportApi(pin);
            addPostDigitalApi(pin);
            addPostAnalogApi(pin);
            addPutDigitalApi(pin);
            addDeleteDigitalApi(pin);
        }
        addPutOnChangeApi();
        addDeleteOnChangeApi();
    }

    private boolean isOnline() {
        return getService().isOnline();
    }

    private FaBoDeviceControl getFaBoDeviceControl() {
        return getFaBoDeviceService().getFaBoDeviceControl();
    }

    private FaBoDeviceService getFaBoDeviceService() {
        return (FaBoDeviceService) getContext();
    }
}
