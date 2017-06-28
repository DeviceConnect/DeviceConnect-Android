package org.deviceconnect.android.deviceplugin.fabo.device.things;

import android.content.Context;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.IADT7410;
import org.deviceconnect.android.deviceplugin.fabo.device.IADXL345;
import org.deviceconnect.android.deviceplugin.fabo.device.IHTS221;
import org.deviceconnect.android.deviceplugin.fabo.device.IISL29034;
import org.deviceconnect.android.deviceplugin.fabo.device.IMPL115;
import org.deviceconnect.android.deviceplugin.fabo.device.IMouseCar;
import org.deviceconnect.android.deviceplugin.fabo.device.IRobotCar;
import org.deviceconnect.android.deviceplugin.fabo.device.IVCNL4010;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaBoThingsDeviceControl implements FaBoDeviceControl {

    private static final String TAG = "FaBo";

    private Context mContext;

    private Map<Integer, Gpio> mGpioMap = new HashMap<>();

    private Map<Integer, I2cDevice> mI2cDeviceMap = new HashMap<>();

    private RobotCar mRobotCar;
    private MouseCar mMouseCar;
    private ADXL345 mADXL345;

    private OnFaBoDeviceControlListener mOnFaBoDeviceControlListener;

    /**
     * GPIOの値変更通知リスナー.
     */
    private final List<OnGPIOListener> mOnGPIOListeners = new ArrayList<>();

    /**
     * GPIO,I2cなどのデバイスを管理するクラス.
     */
    private PeripheralManagerService mManagerService;

    public FaBoThingsDeviceControl(final Context context) {
        mContext = context;
    }

    @Override
    public void initialize() {
        mManagerService = new PeripheralManagerService();

        List<String> portList = mManagerService.getGpioList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);

            Map<String, ArduinoUno.Pin> pins = new HashMap<>();
            pins.put("BCM4", ArduinoUno.Pin.PIN_D4);
            pins.put("BCM5", ArduinoUno.Pin.PIN_D5);
            pins.put("BCM6", ArduinoUno.Pin.PIN_D6);
            pins.put("BCM12", ArduinoUno.Pin.PIN_D12);

            for (String name : portList) {
                Log.i(TAG, "      " + name);
                try {
                    ArduinoUno.Pin pin = pins.get(name);
                    if (pin != null) {
                        Gpio gpio = mManagerService.openGpio(name);
                        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                        gpio.setActiveType(Gpio.ACTIVE_HIGH);
                        gpio.setValue(true);

//                        gpio.setDirection(Gpio.DIRECTION_IN);
//                        gpio.setActiveType(Gpio.ACTIVE_LOW);
//                        gpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
//                        gpio.registerGpioCallback(mGpioCallback);

                        mGpioMap.put(pin.getPinNumber(), gpio);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mOnFaBoDeviceControlListener != null) {
            mOnFaBoDeviceControlListener.onConnected();
        }
    }

    @Override
    public void destroy() {

        Log.i(TAG, "FaBoThingsDeviceControl::destroy");

        mOnGPIOListeners.clear();

        for (Gpio gpio : mGpioMap.values()) {
            try {
                gpio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mGpioMap.clear();

        for (I2cDevice device : mI2cDeviceMap.values()) {
            try {
                device.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mI2cDeviceMap.clear();
    }

    @Override
    public void writeAnalog(final ArduinoUno.Pin pin, final int value) {
    }

    @Override
    public void writeDigital(final ArduinoUno.Pin pin, final ArduinoUno.Level hl) {
        try {
            Gpio gpio = mGpioMap.get(pin.getPinNumber());
            if (gpio != null) {
                gpio.setValue(hl == ArduinoUno.Level.HIGH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getAnalog(final ArduinoUno.Pin pin) {
        return 0;
    }

    @Override
    public ArduinoUno.Level getDigital(final ArduinoUno.Pin pin) {
        try {
            Gpio gpio = mGpioMap.get(pin.getPinNumber());
            if (gpio != null) {
                return gpio.getValue() ? ArduinoUno.Level.HIGH : ArduinoUno.Level.LOW;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setPinMode(final ArduinoUno.Pin pin, final ArduinoUno.Mode mode) {
        Log.e("ABC", "setPinMode::");
        try {
            Gpio gpio = mGpioMap.get(pin.getPinNumber());
            if (gpio != null) {

                Log.e("ABC", "setPinMode:: 2");
                try {
                    gpio.unregisterGpioCallback(mGpioCallback);
                    gpio.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Map<ArduinoUno.Pin, String> pins = new HashMap<>();
                pins.put(ArduinoUno.Pin.PIN_D4, "BCM4");
                pins.put(ArduinoUno.Pin.PIN_D5, "BCM5");
                pins.put(ArduinoUno.Pin.PIN_D6, "BCM6");
                pins.put(ArduinoUno.Pin.PIN_D12, "BCM12");

                gpio = mManagerService.openGpio(pins.get(pin));

                switch (mode) {
                    case GPIO_IN:
                        Log.e("ABC", "setPinMode:: A");
                        gpio.setDirection(Gpio.DIRECTION_IN);
                        gpio.setActiveType(Gpio.ACTIVE_LOW);
                        gpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
                        gpio.registerGpioCallback(mGpioCallback);
                        Log.e("ABC", "setPinMode:: B");
                        break;

                    case GPIO_OUT:
                        Log.e("ABC", "setPinMode:: C");
                        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                        gpio.setActiveType(Gpio.ACTIVE_HIGH);
                        Log.e("ABC", "setPinMode:: D");
                        break;
                }
                pin.setMode(mode);
            }
        } catch (IOException e) {
            Log.w(TAG, "", e);
        }
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public void writeI2C(final byte[] buffer) {
    }

    @Override
    public void readI2C() {
    }

    @Override
    public IRobotCar getRobotCar() {
        return mRobotCar;
    }

    @Override
    public IMouseCar getMouseCar() {
        return mMouseCar;
    }

    @Override
    public IADXL345 getADXL345() {
        return mADXL345;
    }

    @Override
    public IADT7410 getADT7410() {
        return null;
    }

    @Override
    public IHTS221 getHTS221() {
        return null;
    }

    @Override
    public IVCNL4010 getVCNL4010() {
        return null;
    }

    @Override
    public IISL29034 getISL29034() {
        return null;
    }

    @Override
    public IMPL115 getMPL115() {
        return null;
    }

    @Override
    public void setOnFaBoDeviceControlListener(final OnFaBoDeviceControlListener listener) {
        mOnFaBoDeviceControlListener = listener;
    }

    @Override
    public void addOnGPIOListener(final OnGPIOListener listener) {
        synchronized (mOnGPIOListeners) {
            if (!mOnGPIOListeners.contains(listener)) {
                mOnGPIOListeners.add(listener);
            }
        }
    }

    @Override
    public void removeOnGPIOListener(final OnGPIOListener listener) {
        synchronized (mOnGPIOListeners) {
            mOnGPIOListeners.remove(listener);
        }
    }

    /**
     * デジタルのピンデータが変更されたことを通知します.
     */
    private void notifyDigital() {
        synchronized (mOnGPIOListeners) {
            for (OnGPIOListener l : mOnGPIOListeners) {
                l.onDigital();
            }
        }
    }

    /**
     * GPIOからの通知を受け取るコールバック.
     */
    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(final Gpio gpio) {
            Log.i(TAG, "onGpioEdge : " + gpio);
            notifyDigital();
            return true;
        }

        @Override
        public void onGpioError(final Gpio gpio, final int error) {
            Log.w(TAG, gpio + ": Error event " + error);
        }
    };

    /**
     * I2cDeviceを取得します.
     * <p>
     * 取得できなかった場合はnullを返却します。
     * </p>
     * <p>
     * 一度開いているI2cDeviceは、マップで管理して、取得します。
     * </p>
     * @param address アドレス
     * @return I2cDeviceのインスタンス
     */
    I2cDevice getI2cDevice(final int address) {
        if (mI2cDeviceMap.containsKey(address)) {
            return mI2cDeviceMap.get(address);
        }

        List<String> i2cList = mManagerService.getI2cBusList();
        if (i2cList.isEmpty()) {
            Log.i(TAG, "No I2C port available on this device.");
            return null;
        } else {
            try {
                I2cDevice device = mManagerService.openI2cDevice(i2cList.get(0), address);
                if (device != null) {
                    mI2cDeviceMap.put(address, device);
                }
                return device;
            } catch (IOException e) {
                return null;
            }
        }
    }
}
