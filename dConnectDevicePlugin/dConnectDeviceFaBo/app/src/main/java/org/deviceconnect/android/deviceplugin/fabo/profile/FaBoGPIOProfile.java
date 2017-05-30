/*
FaBoGPIOProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.param.FirmataV32;
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

    private final static String TAG = "FABO_PLUGIN";

    /**
     * Digital Pinに書き込むコマンド.
     */
    private final static byte CMD_DIGITAL_WRITE = FirmataV32.DIGITAL_MESSAGE;

    /**
     * Pinモードの設定コマンド.
     */
    private final static byte CMD_PIN_SETTING = FirmataV32.SET_PIN_MODE;

    /**
     * GPIOのHigh.
     */
    private final static int HIGH = 1;

    /**
     * GPIOのLow.
     */
    private final static int LOW = 0;

    private void addGetAnalogApi(final ArduinoUno.Pin pin) {
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
                            int value = ((FaBoDeviceService) getContext()).getAnalogValue(pin.getPinNumber());
                            setValue(response, value);
                            setResult(response, RESULT_OK);
                            return true;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    private void addGetDigitalApi(final ArduinoUno.Pin pin) {
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
                            int value = ((FaBoDeviceService) getContext()).getDigitalValue(pin.getPort());
                            if ((value & pin.getBit()) == pin.getBit()) {
                                setValue(response, HIGH);
                            } else {
                                setValue(response, LOW);
                            }
                            setResult(response, RESULT_OK);
                            return true;
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    private void addPostExportApi(final ArduinoUno.Pin pin) {
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
                    int modeValue;
                    String mode = request.getStringExtra("mode");
                    if(mode != null) {
                        try {
                            modeValue = Integer.parseInt(mode);
                            if (modeValue < 0 || modeValue > 4) {
                                MessageUtils.setInvalidRequestParameterError(response, "The value of mode must be defined 0-4.");
                                return true;
                            }
                        } catch (Exception e) {
                            MessageUtils.setInvalidRequestParameterError(response, "The value of mode must be defined 0-4.");
                            return true;
                        }
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "The value of mode is null.");
                        return true;
                    }
                    settingPin(pin.getPinNumber(), modeValue);

                    String[] modeStr = {"GPIOIN", "GPIOOUT", "ANALOG", "PWM", "SERVO"};
                    setMessage(response, pinName + "を" + modeStr[modeValue] + "モードに設定しました。");
                    setResult(response, RESULT_OK);
                    return true;
                }
            });
        }
    }

    private void addPostDigitalApi(final ArduinoUno.Pin pin) {
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
                    int hlValue;
                    String hl = request.getStringExtra(PARAM_VALUE);

                    if(hl != null) {
                        try {
                            hlValue = Integer.parseInt(hl);
                            if (hlValue != HIGH && hlValue != LOW) {
                                // 値が無効
                                MessageUtils.setInvalidRequestParameterError(response, "Value must be defined 1 or 0.");
                                return true;
                            }
                        } catch (Exception e) {
                            // 値が無効
                            MessageUtils.setInvalidRequestParameterError(response, "Value must be defined 1 or 0.");
                            return true;
                        }
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "Value is null.");
                        return true;
                    }
                    digitalWrite(pin.getPort(), pin.getBit(), hlValue);
                    String[] hlStr = {"LOW","HIGH"};
                    setMessage(response, pinName + "の値を" + hlStr[hlValue] + "(" + hlValue + ")に変更");
                    setResult(response, RESULT_OK);
                    return true;
                }
            });
        }
    }
    
    private void addPostAnalogApi(final ArduinoUno.Pin pin) {
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
                            int hlValue;
                            String hl = request.getStringExtra(PARAM_VALUE);
                            if(hl != null) {
                                try {
                                    hlValue = Integer.parseInt(hl);
                                    if (hlValue > 255) {
                                        // 値が無効
                                        MessageUtils.setInvalidRequestParameterError(response, "Value must be defined under 255.");
                                        return true;
                                    }
                                } catch (Exception e) {
                                    // 値が無効
                                    MessageUtils.setInvalidRequestParameterError(response, "Value must be defined 0-255.");
                                    return true;
                                }
                            } else {
                                MessageUtils.setInvalidRequestParameterError(response, "Value is null.");
                                return true;
                            }
                            analogWrite(pin.getPinNumber(), hlValue);
                            setResult(response, RESULT_OK);
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
                    ((FaBoDeviceService) getContext()).registerOnChange(getServiceID(request));
                    setResult(response, RESULT_OK);
                    return true;
                } else {
                    MessageUtils.setError(response, 100, "Failed add event.");
                    return true;
                }
            }
        });
    }

    private void addPutDigitalApi(final ArduinoUno.Pin pin) {
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

                    digitalWrite(pin.getPort(), pin.getBit(), HIGH);
                    setMessage(response, pinName + "の値をHIGH(1)に変更");
                    setResult(response, RESULT_OK);
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
                    ((FaBoDeviceService) getContext()).unregisterOnChange(getServiceID(request));
                    setResult(response, RESULT_OK);
                    return true;
                } else {
                    MessageUtils.setError(response, 100, "Failed delete event.");
                    return true;
                }
            }
        });
    }

    private void addDeleteDigitalApi(final ArduinoUno.Pin pin) {
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
                    digitalWrite(pin.getPort(), pin.getBit(), LOW);
                    setMessage(response, pinName + "の値をLOW(0)に変更");
                    setResult(response, RESULT_OK);
                    return true;
                }
            });
        }
    }

    public FaBoGPIOProfile() {
        for (ArduinoUno.Pin pin : ArduinoUno.Pin.values()) {
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

    /**
     * Digitalの書き込み.
     *
     * @param port PORT番号
     * @param pinBit PIN番号
     * @param hl HIGHとLOWの値
     */
    private void digitalWrite(int port, int pinBit, int hl){

        FaBoDeviceService service = (FaBoDeviceService) getContext();

        if (hl == HIGH){
            int status = service.getPortStatus(port) | pinBit;
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (CMD_DIGITAL_WRITE | port);
            bytes[1] = (byte) (status & 0xff);
            bytes[2] = (byte) ((status >> 8) & 0xff);
            service.sendMessage(bytes);
            service.setPortStatus(port, status);
        } else if(hl == LOW){
            int status = service.getPortStatus(port) & ~pinBit;
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (CMD_DIGITAL_WRITE | port);
            bytes[1] = (byte) (status & 0xff);
            bytes[2] = (byte) ((status >> 8) & 0xff);
            service.sendMessage(bytes);
            service.setPortStatus(port, status);
        }
    }

    /**
     * Analogの書き込み.
     *
     * @param pinNo PIN番号
     * @param value 値
     */
    private void analogWrite(int pinNo, int value){

        FaBoDeviceService service = (FaBoDeviceService) getContext();

        byte[] bytes = new byte[5];
        bytes[0] = (byte) FirmataV32.START_SYSEX;
        bytes[1] = (byte) (0x6F);
        bytes[2] = (byte) pinNo;
        bytes[3] = (byte) value;
        bytes[4] = (byte) FirmataV32.END_SYSEX;
        service.sendMessage(bytes);
    }

    /**
     * 各PINの設定.
     *
     * @param pinNo PIN番号
     * @param mode モード、0:GPIO IN, 1:GPIO OUT, 2: ANALOG, 3: PWM, 4:SERVO
     */
    private void settingPin(int pinNo, int mode){

        FaBoDeviceService service = (FaBoDeviceService) getContext();

        byte[] command = new byte[3];
        command[0] = (byte) (CMD_PIN_SETTING);
        command[1] = (byte) (pinNo);
        command[2] = (byte) (mode);

        service.sendMessage(command);
        service.setPin(pinNo, mode);
    }
}
