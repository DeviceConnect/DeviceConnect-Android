package org.deviceconnect.android.deviceplugin.fabo.device.things;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import org.deviceconnect.android.deviceplugin.fabo.BuildConfig;
import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.IADT7410;
import org.deviceconnect.android.deviceplugin.fabo.device.IADXL345;
import org.deviceconnect.android.deviceplugin.fabo.device.IHTS221;
import org.deviceconnect.android.deviceplugin.fabo.device.IISL29034;
import org.deviceconnect.android.deviceplugin.fabo.device.ILIDARLiteV3;
import org.deviceconnect.android.deviceplugin.fabo.device.IMPL115;
import org.deviceconnect.android.deviceplugin.fabo.device.IMouseCar;
import org.deviceconnect.android.deviceplugin.fabo.device.IRobotCar;
import org.deviceconnect.android.deviceplugin.fabo.device.IVCNL4010;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst.STATUS_FABO_RUNNING;

/**
 * Android Things版FaBoを操作するためのクラス.
 */
public class FaBoThingsDeviceControl implements FaBoDeviceControl {

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "FaBo";

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * GPIOを保持するマップ.
     */
    private Map<Integer, Gpio> mGpioMap = new HashMap<>();

    /**
     * I2Cデバイスを保持するマップ.
     */
    private Map<Integer, I2cDevice> mI2cDeviceMap = new HashMap<>();

    /**
     * RobotCarを操作するためのクラス.
     */
    private RobotCar mRobotCar;

    /**
     * RobotCar(Mouse)を操作するためのクラス.
     */
    private MouseCar mMouseCar;

    /**
     * Brick #201 を操作するためのクラス.
     */
    private ADXL345 mADXL345;

    /**
     * Brick #207 を操作するためのクラス.
     */
    private ADT7410 mADT7410;

    /**
     * Brick #208 を操作するためのクラス.
     */
    private HTS221 mHTS221;

    /**
     * Brick #217 を操作するクラス.
     */
    private ISL29034 mISL29034;

    /**
     * Brick #204 を操作するクラス.
     */
    private MPL115 mMPL115;

    /**
     * Brick # 205 を操作するクラス.
     */
    private VCNL4010 mVCNL4010;

    /**
     * LIDARLite v3を操作するクラス.
     */
    private LIDARLiteV3 mLIDARLiteV3;

    /**
     * FaBoとの接続状態を通知するリスナー.
     */
    private OnFaBoDeviceControlListener mOnFaBoDeviceControlListener;

    /**
     * GPIOの値変更通知リスナー.
     */
    private final List<OnGPIOListener> mOnGPIOListeners = new ArrayList<>();

    /**
     * GPIO,I2cなどのデバイスを管理するクラス.
     */
    private PeripheralManager mManagerService;

    /**
     * GPIOの処理を行うハンドラ.
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * コンストラクタ.
     */
    public FaBoThingsDeviceControl() {
    }

    @Override
    public void initialize() {
        if (DEBUG) {
            Log.i(TAG, "FaBoThingsDeviceControl::initialize");
        }

        mManagerService = PeripheralManager.getInstance();

        initGpio();
    }

    @Override
    public void destroy() {
        if (DEBUG) {
            Log.i(TAG, "FaBoThingsDeviceControl::destroy");
        }

        synchronized (mOnGPIOListeners) {
            mOnGPIOListeners.clear();
        }

        for (Gpio gpio : mGpioMap.values()) {
            try {
                gpio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mGpioMap.clear();

        if (mADXL345 != null) {
            mADXL345.destroy();
            mADXL345 = null;
        }

        if (mADT7410 != null) {
            mADT7410.destroy();
            mADT7410 = null;
        }

        if (mHTS221 != null) {
            mHTS221.destroy();
            mHTS221 = null;
        }

        if (mISL29034 != null) {
            mISL29034.destroy();
            mISL29034 = null;
        }

        if (mMPL115 != null) {
            mMPL115.destroy();
            mMPL115 = null;
        }

        if (mVCNL4010 != null) {
            mVCNL4010.destroy();
            mVCNL4010 = null;
        }

        if (mLIDARLiteV3 != null) {
            mLIDARLiteV3.destroy();
            mLIDARLiteV3 = null;
        }

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
    public boolean isPinSupported(final FaBoShield.Pin pin) {
        return mGpioMap.containsKey(pin.getPinNumber());
    }

    @Override
    public void writeAnalog(final FaBoShield.Pin pin, final int value) {
        throw new RuntimeException("Analog is not supported.");
    }

    @Override
    public void writeDigital(final FaBoShield.Pin pin, final FaBoShield.Level hl) {
        try {
            Gpio gpio = mGpioMap.get(pin.getPinNumber());
            if (gpio != null) {
                gpio.setValue(hl == FaBoShield.Level.HIGH);
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    @Override
    public int getAnalog(final FaBoShield.Pin pin) {
        throw new RuntimeException("Analog is not supported.");
    }

    @Override
    public FaBoShield.Level getDigital(final FaBoShield.Pin pin) {
        try {
            Gpio gpio = mGpioMap.get(pin.getPinNumber());
            if (gpio != null) {
                return gpio.getValue() ? FaBoShield.Level.HIGH : FaBoShield.Level.LOW;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setPinMode(final FaBoShield.Pin pin, final FaBoShield.Mode mode) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Gpio gpio = mGpioMap.get(pin.getPinNumber());
                    if (gpio != null && pin.getMode() != mode) {
                        switch (mode) {
                            case GPIO_IN:
                                gpio.setEdgeTriggerType(Gpio.EDGE_NONE);
                                gpio.setDirection(Gpio.DIRECTION_IN);
                                gpio.setActiveType(Gpio.ACTIVE_HIGH);
                                gpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
                                break;

                            case GPIO_OUT:
                                gpio.setEdgeTriggerType(Gpio.EDGE_NONE);
                                gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                                gpio.setActiveType(Gpio.ACTIVE_HIGH);
                                break;
                        }
                        pin.setMode(mode);
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.w(TAG, "setPinMode", e);
                    }
                }
            }
        });
    }

    @Override
    public int getStatus() {
        return STATUS_FABO_RUNNING;
    }

    @Override
    public IRobotCar getRobotCar() {
        if (mRobotCar == null) {
            mRobotCar = new RobotCar(this);
        }
        return mRobotCar;
    }

    @Override
    public IMouseCar getMouseCar() {
        if (mMouseCar == null) {
            mMouseCar = new MouseCar(this);
        }
        return mMouseCar;
    }

    @Override
    public IADXL345 getADXL345() {
        if (mADXL345 == null) {
            mADXL345 = new ADXL345(this);
        }
        return mADXL345;
    }

    @Override
    public IADT7410 getADT7410() {
        if (mADT7410 == null) {
            mADT7410 = new ADT7410(this);
        }
        return mADT7410;
    }

    @Override
    public IHTS221 getHTS221() {
        if (mHTS221 == null) {
            mHTS221 = new HTS221(this);
        }
        return mHTS221;
    }

    @Override
    public IVCNL4010 getVCNL4010() {
        if (mVCNL4010 == null) {
            mVCNL4010 = new VCNL4010(this);
        }
        return mVCNL4010;
    }

    @Override
    public IISL29034 getISL29034() {
        if (mISL29034 == null) {
            mISL29034 = new ISL29034(this);
        }
        return mISL29034;
    }

    @Override
    public IMPL115 getMPL115() {
        if (mMPL115 == null) {
            mMPL115 = new MPL115(this);
        }
        return mMPL115;
    }

    @Override
    public ILIDARLiteV3 getLIDARLite() {
        if (mLIDARLiteV3 == null) {
            mLIDARLiteV3 = new LIDARLiteV3(this);
        }
        return mLIDARLiteV3;
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
            if (DEBUG) {
                try {
                    Log.e(TAG, "GPIO: " + gpio.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            notifyDigital();
            return true;
        }
        @Override
        public void onGpioError(final Gpio gpio, final int error) {
            if (DEBUG) {
                Log.w(TAG, gpio + ": Error event " + error);
            }
        }
    };

    /**
     * GPIOを初期化します.
     */
    private void initGpio() {
        List<String> portList = mManagerService.getGpioList();
        if (portList.isEmpty()) {
            if (DEBUG) {
                Log.i(TAG, "No GPIO port available on this device.");
            }

            if (mOnFaBoDeviceControlListener != null) {
                mOnFaBoDeviceControlListener.onFailedConnected();
            }
        } else {
            if (DEBUG) {
                Log.i(TAG, "List of available ports: " + portList);
            }

            Map<String, FaBoShield.Pin> pins = new HashMap<>();
            pins.put("BCM4", FaBoShield.Pin.PIN_D4);
            pins.put("BCM5", FaBoShield.Pin.PIN_D5);
            pins.put("BCM6", FaBoShield.Pin.PIN_D6);
            pins.put("BCM12", FaBoShield.Pin.PIN_D12);

            for (String name : portList) {
                try {
                    FaBoShield.Pin pin = pins.get(name);
                    if (pin != null) {
                        Gpio gpio = mManagerService.openGpio(name);
                        gpio.setEdgeTriggerType(Gpio.EDGE_NONE);
                        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                        gpio.setActiveType(Gpio.ACTIVE_HIGH);
                        gpio.registerGpioCallback(mGpioCallback);
                        gpio.setValue(false);

                        mGpioMap.put(pin.getPinNumber(), gpio);
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.w(TAG, " ", e);
                    }
                }
            }

            if (mOnFaBoDeviceControlListener != null) {
                mOnFaBoDeviceControlListener.onConnected();
            }
        }
    }

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
            if (DEBUG) {
                Log.i(TAG, "No I2C port available on this device.");
            }
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
