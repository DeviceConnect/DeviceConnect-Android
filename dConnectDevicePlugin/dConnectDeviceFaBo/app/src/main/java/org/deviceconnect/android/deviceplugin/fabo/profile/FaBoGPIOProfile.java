/*
FaBoGPIOProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.param.FirmataV32;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;

import static org.deviceconnect.message.DConnectMessage.*;

/**
 * GPIO Profile.
 * @author NTT DOCOMO, INC.
 */
public class FaBoGPIOProfile extends DConnectProfile {

    private final static String TAG = "FABO_PLUGIN";

    /** Profile名. */
    public  final static String PROFILE_NAME = "gpio";

    /** Digital Pinに書き込むコマンド. */
    private final static byte CMD_DIGITAL_WRITE = FirmataV32.DIGITAL_MESSAGE;

    /** Pinモードの設定コマンド. */
    private final static byte CMD_PIN_SETTING = FirmataV32.SET_PIN_MODE;

    /** GPIOのHigh. */
    private final static int HIGH = 1;

    /** GPIOのLow. */
    private final static int LOW = 0;

    /** onchangeイベント. */
    public final static String ATTRIBUTE_ON_CHANGE = "onchange";


    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {

        String mInterface = getInterface(request);
        String mAttribute = getAttribute(request);
        String mServiceId = getServiceID(request);

        FaBoDeviceService service = (FaBoDeviceService) getContext();

        if (mServiceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(mServiceId)) {
            createNotFoundService(response);
            return true;
        } else {
            switch (mInterface) {
                case "analog":
                    // ArduinoUnoでは、0-19もしくはD0-D13, A0-A5までが有効PIN
                    switch (mAttribute) {
                        case "14":
                        case "A0": {
                            int value = service.getAnalogValue(ArduinoUno.PIN_NO_A0);
                            response.putExtra("value", value);
                            break;
                        }
                        case "15":
                        case "A1": {
                            int value = service.getAnalogValue(ArduinoUno.PIN_NO_A1);
                            response.putExtra("value", value);
                            break;
                        }
                        case "16":
                        case "A2": {
                            int value = service.getAnalogValue(ArduinoUno.PIN_NO_A2);
                            response.putExtra("value", value);
                            break;
                        }
                        case "17":
                        case "A3": {
                            int value = service.getAnalogValue(ArduinoUno.PIN_NO_A3);
                            response.putExtra("value", value);
                            break;
                        }
                        case "18":
                        case "A4": {
                            int value = service.getAnalogValue(ArduinoUno.PIN_NO_A4);
                            response.putExtra("value", value);
                            break;
                        }
                        case "19":
                        case "A5": {
                            int value = service.getAnalogValue(ArduinoUno.PIN_NO_A5);
                            response.putExtra("value", value);
                            break;
                        }
                        default:
                            // PIN指定が無効
                            MessageUtils.setInvalidRequestParameterError(response, "Support pin is 14-19 or A0-A5, not support pin no: " + mAttribute);
                            setResult(response, RESULT_ERROR);
                            return true;
                    }
                    setResult(response, RESULT_OK);
                    return true;
                case "digital":

                    switch (mAttribute) {
                        case "0":
                        case "D0": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D0);
                            if ((value & ArduinoUno.BIT_D0) == ArduinoUno.BIT_D0) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "1":
                        case "D1": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D1);
                            if ((value & ArduinoUno.BIT_D1) == ArduinoUno.BIT_D1) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "2":
                        case "D2": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D2);
                            if ((value & ArduinoUno.BIT_D2) == ArduinoUno.BIT_D2) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "3":
                        case "D3": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D3);
                            if ((value & ArduinoUno.BIT_D3) == ArduinoUno.BIT_D3) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "4":
                        case "D4": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D4);
                            if ((value & ArduinoUno.BIT_D4) == ArduinoUno.BIT_D4) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "5":
                        case "D5": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D5);
                            if ((value & ArduinoUno.BIT_D5) == ArduinoUno.BIT_D5) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "6":
                        case "D6": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D6);
                            if ((value & ArduinoUno.BIT_D6) == ArduinoUno.BIT_D6) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "7":
                        case "D7": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D7);
                            if ((value & ArduinoUno.BIT_D7) == ArduinoUno.BIT_D7) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "8":
                        case "D8": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D8);
                            if ((value & ArduinoUno.BIT_D8) == ArduinoUno.BIT_D8) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "9":
                        case "D9": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D9);
                            if ((value & ArduinoUno.BIT_D9) == ArduinoUno.BIT_D9) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "10":
                        case "D10": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D10);
                            if ((value & ArduinoUno.BIT_D10) == ArduinoUno.BIT_D10) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "11":
                        case "D11": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D11);
                            if ((value & ArduinoUno.BIT_D11) == ArduinoUno.BIT_D11) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "12":
                        case "D12": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D12);
                            if ((value & ArduinoUno.BIT_D12) == ArduinoUno.BIT_D12) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        case "13":
                        case "D13": {
                            int value = service.getDigitalValue(ArduinoUno.PORT_D13);
                            if ((value & ArduinoUno.BIT_D13) == ArduinoUno.BIT_D13) {
                                response.putExtra("value", HIGH);
                            } else {
                                response.putExtra("value", LOW);
                            }
                            break;
                        }
                        default:
                            // PIN指定が無効
                            MessageUtils.setInvalidRequestParameterError(response, "Support pin is 0-13 or D0-D13, not support pin no: " + mAttribute);
                            setResult(response, RESULT_ERROR);
                            return true;
                    }
                    setResult(response, RESULT_OK);
                    return true;
                default:
                    // Attributeが存在しない
                    MessageUtils.setNotSupportAttributeError(response, "GET method is supported only /gpio/analog/pin_no or /gpio/digital/pin_no.");
                    setResult(response, RESULT_ERROR);
                    return true;
            }
        }
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {

        String mInterface = getInterface(request);
        String mAttribute = getAttribute(request);
        String mServiceId = getServiceID(request);

        if (mServiceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(mServiceId)) {
            createNotFoundService(response);
            return true;
        } else {
            switch (mInterface) {
                case "export": {

                    int mModeValue = 0;
                    String mMode = request.getStringExtra("mode");
                    if(mMode != null) {
                        try {
                            mModeValue = Integer.parseInt(mMode);
                            if (mModeValue != 1 && mModeValue != 2 && mModeValue != 3) {
                                MessageUtils.setInvalidRequestParameterError(response, "The value of mode must be defined 1-3.");
                                setResult(response, RESULT_ERROR);
                                return true;
                            }
                        } catch (Exception e) {
                            MessageUtils.setInvalidRequestParameterError(response, "The value of mode must be defined 1-3.");
                            setResult(response, RESULT_ERROR);
                            return true;
                        }
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "The value of mode is null.");
                        setResult(response, RESULT_ERROR);
                        return true;
                    }

                    switch (mAttribute) {
                        case "0":
                        case "D0":
                            settingPin(ArduinoUno.PIN_NO_D0, mModeValue);
                            break;
                        case "1":
                        case "D1":
                            settingPin(ArduinoUno.PIN_NO_D1, mModeValue);
                            break;
                        case "2":
                        case "D2":
                            settingPin(ArduinoUno.PIN_NO_D2, mModeValue);
                            break;
                        case "3":
                        case "D3":
                            settingPin(ArduinoUno.PIN_NO_D3, mModeValue);
                            break;
                        case "4":
                        case "D4":
                            settingPin(ArduinoUno.PIN_NO_D4, mModeValue);
                            break;
                        case "5":
                        case "D5":
                            settingPin(ArduinoUno.PIN_NO_D5, mModeValue);
                            break;
                        case "6":
                        case "D6":
                            settingPin(ArduinoUno.PIN_NO_D6, mModeValue);
                            break;
                        case "7":
                        case "D7":
                            settingPin(ArduinoUno.PIN_NO_D7, mModeValue);
                            break;
                        case "8":
                        case "D8":
                            settingPin(ArduinoUno.PIN_NO_D8, mModeValue);
                            break;
                        case "9":
                        case "D9":
                            settingPin(ArduinoUno.PIN_NO_D9, mModeValue);
                            break;
                        case "10":
                        case "D10":
                            settingPin(ArduinoUno.PIN_NO_D10, mModeValue);
                            break;
                        case "11":
                        case "D11":
                            settingPin(ArduinoUno.PIN_NO_D11, mModeValue);
                            break;
                        case "12":
                        case "D12":
                            settingPin(ArduinoUno.PIN_NO_D12, mModeValue);
                            break;
                        case "13":
                        case "D13":
                            settingPin(ArduinoUno.PIN_NO_D13, mModeValue);
                            break;
                        case "14":
                        case "A0":
                            settingPin(ArduinoUno.PIN_NO_A0, mModeValue);
                            break;
                        case "15":
                        case "A1":
                            settingPin(ArduinoUno.PIN_NO_A1, mModeValue);
                            break;
                        case "16":
                        case "A2":
                            settingPin(ArduinoUno.PIN_NO_A2, mModeValue);
                            break;
                        case "17":
                        case "A3":
                            settingPin(ArduinoUno.PIN_NO_A3, mModeValue);
                            break;
                        case "18":
                        case "A4":
                            settingPin(ArduinoUno.PIN_NO_A4, mModeValue);
                            break;
                        case "19":
                        case "A5":
                            settingPin(ArduinoUno.PIN_NO_A5, mModeValue);
                            break;
                        default:
                            // PIN指定が無効
                            MessageUtils.setInvalidRequestParameterError(response, "Support pin is 0-20 or D0-D13 or A0-A5, not support pin no: " + mAttribute);
                            setResult(response, RESULT_ERROR);
                            return true;
                    }

                    setResult(response, RESULT_OK);
                    return true;

                }
                case "digital": {

                    int mHLValue = 0;
                    String mHL = request.getStringExtra("value");

                    if(mHL != null) {
                        try {

                            mHLValue = Integer.parseInt(mHL);

                            if (mHLValue != HIGH && mHLValue != LOW) {
                                // 値が無効
                                MessageUtils.setInvalidRequestParameterError(response, "Value must be defined 1 or 0.");
                                setResult(response, RESULT_ERROR);
                                return true;
                            }

                        } catch (Exception e) {
                            // 値が無効
                            MessageUtils.setInvalidRequestParameterError(response, "Value must be defined 1 or 0.");
                            setResult(response, RESULT_ERROR);
                            return true;
                        }
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "Value is null.");
                        setResult(response, RESULT_ERROR);
                        return true;
                    }

                    switch (mAttribute) {
                        case "0":
                        case "D0":
                            digitalWrite(ArduinoUno.PORT_D0, ArduinoUno.BIT_D0, mHLValue);
                            break;
                        case "1":
                        case "D1":
                            digitalWrite(ArduinoUno.PORT_D1, ArduinoUno.BIT_D1, mHLValue);
                            break;
                        case "2":
                        case "D2":
                            digitalWrite(ArduinoUno.PORT_D2, ArduinoUno.BIT_D2, mHLValue);
                            break;
                        case "3":
                        case "D3":
                            digitalWrite(ArduinoUno.PORT_D3, ArduinoUno.BIT_D3, mHLValue);
                            break;
                        case "4":
                        case "D4":
                            digitalWrite(ArduinoUno.PORT_D4, ArduinoUno.BIT_D4, mHLValue);
                            break;
                        case "5":
                        case "D5":
                            digitalWrite(ArduinoUno.PORT_D5, ArduinoUno.BIT_D5, mHLValue);
                            break;
                        case "6":
                        case "D6":
                            digitalWrite(ArduinoUno.PORT_D6, ArduinoUno.BIT_D6, mHLValue);
                            break;
                        case "7":
                        case "D7":
                            digitalWrite(ArduinoUno.PORT_D7, ArduinoUno.BIT_D7, mHLValue);
                            break;
                        case "8":
                        case "D8":
                            digitalWrite(ArduinoUno.PORT_D8, ArduinoUno.BIT_D8, mHLValue);
                            break;
                        case "9":
                        case "D9":
                            digitalWrite(ArduinoUno.PORT_D9, ArduinoUno.BIT_D9, mHLValue);
                            break;
                        case "10":
                        case "D10":
                            digitalWrite(ArduinoUno.PORT_D10, ArduinoUno.BIT_D10, mHLValue);
                            break;
                        case "11":
                        case "D11":
                            digitalWrite(ArduinoUno.PORT_D11, ArduinoUno.BIT_D11, mHLValue);
                            break;
                        case "12":
                        case "D12":
                            digitalWrite(ArduinoUno.PORT_D12, ArduinoUno.BIT_D12, mHLValue);
                            break;
                        case "13":
                        case "D13":
                            digitalWrite(ArduinoUno.PORT_D13, ArduinoUno.BIT_D13, mHLValue);
                            break;
                        case "14":
                        case "A0":
                            digitalWrite(ArduinoUno.PORT_A0, ArduinoUno.BIT_A0, mHLValue);
                            break;
                        case "15":
                        case "A1":
                            digitalWrite(ArduinoUno.PORT_A1, ArduinoUno.BIT_A1, mHLValue);
                            break;
                        case "16":
                        case "A2":
                            digitalWrite(ArduinoUno.PORT_A2, ArduinoUno.BIT_A2, mHLValue);
                            break;
                        case "17":
                        case "A3":
                            digitalWrite(ArduinoUno.PORT_A3, ArduinoUno.BIT_A3, mHLValue);
                            break;
                        case "18":
                        case "A4":
                            digitalWrite(ArduinoUno.PORT_A4, ArduinoUno.BIT_A4, mHLValue);
                            break;
                        case "19":
                        case "A5":
                            digitalWrite(ArduinoUno.PORT_A5, ArduinoUno.BIT_A5, mHLValue);
                            break;
                        default:
                            MessageUtils.setInvalidRequestParameterError(response, "Support pin is 0-20 or D0-D13 or A0-A5, not support pin no: " + mAttribute);
                            setResult(response, RESULT_ERROR);
                            return true;
                    }
                    setResult(response, RESULT_OK);
                    return true;
                }
                case "analog": {

                    int mHLValue = 0;
                    String mHL = request.getStringExtra("value");

                    if(mHL != null) {
                        try {

                            mHLValue = Integer.parseInt(mHL);

                            if (mHLValue > 255) {
                                // 値が無効
                                MessageUtils.setInvalidRequestParameterError(response, "Value must be defined under 255.");
                                setResult(response, RESULT_ERROR);
                                return true;
                            }

                        } catch (Exception e) {
                            // 値が無効
                            MessageUtils.setInvalidRequestParameterError(response, "Value must be defined 0-255.");
                            setResult(response, RESULT_ERROR);
                            return true;
                        }
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "Value is null.");
                        setResult(response, RESULT_ERROR);
                        return true;
                    }

                    switch (mAttribute) {
                        case "3":
                        case "D3":
                            analogWrite(ArduinoUno.PIN_NO_D3, mHLValue);
                            break;
                        case "5":
                        case "D5":
                            analogWrite(ArduinoUno.PIN_NO_D5, mHLValue);
                            break;
                        case "6":
                        case "D6":
                            analogWrite(ArduinoUno.PIN_NO_D6, mHLValue);
                            break;
                        case "9":
                        case "D9":
                            analogWrite(ArduinoUno.PIN_NO_D9, mHLValue);
                            break;
                        case "10":
                        case "D10":
                            analogWrite(ArduinoUno.PIN_NO_D10, mHLValue);
                            break;
                        case "11":
                        case "D11":
                            analogWrite(ArduinoUno.PIN_NO_D11, mHLValue);
                            break;
                        default:
                            MessageUtils.setInvalidRequestParameterError(response, "Support pin is [3,5,6,9,10,11] or [D3,D5,D6,D9,D10,D11], not support pin no: " + mAttribute);
                            setResult(response, RESULT_ERROR);
                            return true;
                    }

                    setResult(response, RESULT_OK);
                    return true;
                }
                default:
                    MessageUtils.setNotSupportAttributeError(response, "POST method is supported only /gpio/export/pin_no or /gpio/analog/pin_no or /gpio/digital/pin_no.");
                    setResult(response, RESULT_ERROR);
                    return true;
            }
        }
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {

        String mInterface = getInterface(request);
        String mAttribute = getAttribute(request);
        String mServiceId = getServiceID(request);

        FaBoDeviceService service = (FaBoDeviceService) getContext();

        if (mServiceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(mServiceId)) {
            createNotFoundService(response);
            return true;
        } else {

            if (mAttribute.equals(ATTRIBUTE_ON_CHANGE)) {
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (EventError.NONE == error) {
                    service.registerOnChange(mServiceId);
                    setResult(response, RESULT_OK);
                    return true;
                } else {
                    MessageUtils.setError(response, 100, "Failed add event.");
                    setResult(response, RESULT_ERROR);
                    return true;
                }
            } else if (mInterface != null && mInterface.equals("digital")) {

                switch (mAttribute) {
                    case "0":
                    case "D0":
                        digitalWrite(ArduinoUno.PORT_D0, ArduinoUno.BIT_D0, HIGH);
                        break;
                    case "1":
                    case "D1":
                        digitalWrite(ArduinoUno.PORT_D1, ArduinoUno.BIT_D1, HIGH);
                        break;
                    case "2":
                    case "D2":
                        digitalWrite(ArduinoUno.PORT_D2, ArduinoUno.BIT_D2, HIGH);
                        break;
                    case "3":
                    case "D3":
                        digitalWrite(ArduinoUno.PORT_D3, ArduinoUno.BIT_D3, HIGH);
                        break;
                    case "4":
                    case "D4":
                        digitalWrite(ArduinoUno.PORT_D4, ArduinoUno.BIT_D4, HIGH);
                        break;
                    case "5":
                    case "D5":
                        digitalWrite(ArduinoUno.PORT_D5, ArduinoUno.BIT_D5, HIGH);
                        break;
                    case "6":
                    case "D6":
                        digitalWrite(ArduinoUno.PORT_D6, ArduinoUno.BIT_D6, HIGH);
                        break;
                    case "7":
                    case "D7":
                        digitalWrite(ArduinoUno.PORT_D7, ArduinoUno.BIT_D7, HIGH);
                        break;
                    case "8":
                    case "D8":
                        digitalWrite(ArduinoUno.PORT_D8, ArduinoUno.BIT_D8, HIGH);
                        break;
                    case "9":
                    case "D9":
                        digitalWrite(ArduinoUno.PORT_D9, ArduinoUno.BIT_D9, HIGH);
                        break;
                    case "10":
                    case "D10":
                        digitalWrite(ArduinoUno.PORT_D10, ArduinoUno.BIT_D10, HIGH);
                        break;
                    case "11":
                    case "D11":
                        digitalWrite(ArduinoUno.PORT_D11, ArduinoUno.BIT_D11, HIGH);
                        break;
                    case "12":
                    case "D12":
                        digitalWrite(ArduinoUno.PORT_D12, ArduinoUno.BIT_D12, HIGH);
                        break;
                    case "13":
                    case "D13":
                        digitalWrite(ArduinoUno.PORT_D13, ArduinoUno.BIT_D13, HIGH);
                        break;
                    case "14":
                    case "A0":
                        digitalWrite(ArduinoUno.PORT_A0, ArduinoUno.BIT_A0, HIGH);
                        break;
                    case "15":
                    case "A1":
                        digitalWrite(ArduinoUno.PORT_A1, ArduinoUno.BIT_A1, HIGH);
                        break;
                    case "16":
                    case "A2":
                        digitalWrite(ArduinoUno.PORT_A2, ArduinoUno.BIT_A2, HIGH);
                        break;
                    case "17":
                    case "A3":
                        digitalWrite(ArduinoUno.PORT_A3, ArduinoUno.BIT_A3, HIGH);
                        break;
                    case "18":
                    case "A4":
                        digitalWrite(ArduinoUno.PORT_A4, ArduinoUno.BIT_A4, HIGH);
                        break;
                    case "19":
                    case "A5":
                        digitalWrite(ArduinoUno.PORT_A5, ArduinoUno.BIT_A5, HIGH);
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "Support pin is 0-20 or D0-D13 or A0-A5, not support pin no: " + mAttribute);
                        setResult(response, RESULT_ERROR);
                        return true;
                }
                setResult(response, RESULT_OK);
                return true;
            } else {
                MessageUtils.setNotSupportAttributeError(response, "PUT method is supported only /gpio/digital/pin_no or /gpio/onchange.");
                setResult(response, RESULT_ERROR);
                return true;
            }
        }
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {

        String mInterface = getInterface(request);
        String mAttribute = getAttribute(request);
        String mServiceId = getServiceID(request);

        FaBoDeviceService service = (FaBoDeviceService) getContext();

        if (mServiceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(mServiceId)) {
            createNotFoundService(response);
            return true;
        } else {
            if (mAttribute.equals(ATTRIBUTE_ON_CHANGE)) {
                Boolean result = EventManager.INSTANCE.removeEvents(getSessionKey(request));
                if (result) {
                    service.unregisterOnChange(mServiceId);
                    setResult(response, RESULT_OK);
                    return true;
                } else {
                    MessageUtils.setError(response, 100, "Failed delete event.");
                    setResult(response, RESULT_ERROR);
                    return true;
                }
            }
            else if(mInterface.equals("digital")) {
                byte[] bytes = new byte[3];
                switch (mAttribute) {
                    case "0":
                    case "D0":
                        digitalWrite(ArduinoUno.PORT_D0, ArduinoUno.BIT_D0, LOW);
                        break;
                    case "1":
                    case "D1":
                        digitalWrite(ArduinoUno.PORT_D1, ArduinoUno.BIT_D1, LOW);
                        break;
                    case "2":
                    case "D2":
                        digitalWrite(ArduinoUno.PORT_D2, ArduinoUno.BIT_D2, LOW);
                        break;
                    case "3":
                    case "D3":
                        digitalWrite(ArduinoUno.PORT_D3, ArduinoUno.BIT_D3, LOW);
                        break;
                    case "4":
                    case "D4":
                        digitalWrite(ArduinoUno.PORT_D4, ArduinoUno.BIT_D4, LOW);
                        break;
                    case "5":
                    case "D5":
                        digitalWrite(ArduinoUno.PORT_D5, ArduinoUno.BIT_D5, LOW);
                        break;
                    case "6":
                    case "D6":
                        digitalWrite(ArduinoUno.PORT_D6, ArduinoUno.BIT_D6, LOW);
                        break;
                    case "7":
                    case "D7":
                        digitalWrite(ArduinoUno.PORT_D7, ArduinoUno.BIT_D7, LOW);
                        break;
                    case "8":
                    case "D8":
                        digitalWrite(ArduinoUno.PORT_D8, ArduinoUno.BIT_D8, LOW);
                        break;
                    case "9":
                    case "D9":
                        digitalWrite(ArduinoUno.PORT_D9, ArduinoUno.BIT_D9, LOW);
                        break;
                    case "10":
                    case "D10":
                        digitalWrite(ArduinoUno.PORT_D10, ArduinoUno.BIT_D10, LOW);
                        break;
                    case "11":
                    case "D11":
                        digitalWrite(ArduinoUno.PORT_D11, ArduinoUno.BIT_D11, LOW);
                        break;
                    case "12":
                    case "D12":
                        digitalWrite(ArduinoUno.PORT_D12, ArduinoUno.BIT_D12, LOW);
                        break;
                    case "13":
                    case "D13":
                        digitalWrite(ArduinoUno.PORT_D13, ArduinoUno.BIT_D13, LOW);
                        break;
                    case "14":
                    case "A0":
                        digitalWrite(ArduinoUno.PORT_A0, ArduinoUno.BIT_A0, LOW);
                        break;
                    case "15":
                    case "A1":
                        digitalWrite(ArduinoUno.PORT_A1, ArduinoUno.BIT_A1, LOW);
                        break;
                    case "16":
                    case "A2":
                        digitalWrite(ArduinoUno.PORT_A2, ArduinoUno.BIT_A2, LOW);
                        break;
                    case "17":
                    case "A3":
                        digitalWrite(ArduinoUno.PORT_A3, ArduinoUno.BIT_A3, LOW);
                        break;
                    case "18":
                    case "A4":
                        digitalWrite(ArduinoUno.PORT_A4, ArduinoUno.BIT_A4, LOW);
                        break;
                    case "19":
                    case "A5":
                        digitalWrite(ArduinoUno.PORT_A5, ArduinoUno.BIT_A5, LOW);
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "Support pin is 0-20 or D0-D13 or A0-A5, not support pin no: " + mAttribute);
                        setResult(response, RESULT_ERROR);
                        return true;
                }
                setResult(response, RESULT_OK);
                return true;
            } else {
                MessageUtils.setNotSupportAttributeError(response, "DELETE method is supported only /gpio/digital/pin_no or /gpio/onchange.");
                setResult(response, RESULT_ERROR);
                return true;
            }
        }
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
            service.SendMessage(bytes);
            service.setPortStatus(port, status);
        } else if(hl == LOW){
            int status = service.getPortStatus(port) & ~pinBit;
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (CMD_DIGITAL_WRITE | port);
            bytes[1] = (byte) (status & 0xff);
            bytes[2] = (byte) ((status >> 8) & 0xff);
            service.SendMessage(bytes);
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
        service.SendMessage(bytes);
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

        service.SendMessage(command);
        service.setPin(pinNo, mode);
    }

    /**
     * Check serviceId.
     *
     * @param serviceId ServiceId
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = FaBoServiceDiscoveryProfile.SERVICE_ID;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(serviceId);
        return m.find();
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     *
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     *
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }
}
