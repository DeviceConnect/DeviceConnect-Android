package org.deviceconnect.android.deviceplugin.fabo.device.things;

import android.content.Context;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.IMouseCar;
import org.deviceconnect.android.deviceplugin.fabo.device.IRobotCar;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaBoThingsDeviceControl implements FaBoDeviceControl {

    private static final String TAG = "FaBo";

    private Context mContext;

    private List<Gpio> mGpioList = new ArrayList<>();

    private RobotCar mRobotCar;
    private MouseCar mMouseCar;

    private SpiDevice mDevice;

    private Thread mThread;

    private OnFaBoDeviceControlListener mOnFaBoDeviceControlListener;
    private int busSpeed = 1000000;

    public FaBoThingsDeviceControl(final Context context) {
        mContext = context;
    }

    @Override
    public void initialize() {
        try {
            mRobotCar = new RobotCar();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);

            for (String name : portList) {
                Log.i(TAG, "      " + name);
                try {
                    Gpio gpio = manager.openGpio(name);
                    mGpioList.add(gpio);

                    gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                    gpio.setActiveType(Gpio.ACTIVE_HIGH);
                    gpio.setValue(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        final List<String> spiList = manager.getSpiBusList();
        if (spiList.isEmpty()) {
            Log.i(TAG, "No SPI bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + spiList);

            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mDevice = manager.openSpiDevice(spiList.get(0));
                        mDevice.setFrequency(busSpeed);
                        mDevice.setMode(SpiDevice.MODE0); // Mode 0 seems to work best for WS2801
                        mDevice.setBitsPerWord(8);
                        // Low clock, leading edge transfer
//                        mDevice.setMode(SpiDevice.MODE0);
//                        mDevice.setFrequency(16000000);     // 16MHz
//                        mDevice.setBitsPerWord(8);          // 8 BPW
//                        mDevice.setBitJustification(false); // MSB first
                        byte[] buf = new byte[4];
                        while (true) {
                            mDevice.read(buf, buf.length);
                            Log.e("ABC", " " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3]);
                            try {
                                Thread.sleep(66);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("ABC", " ,", e);
                    }
                }
            });
            mThread.start();


        }

        if (mOnFaBoDeviceControlListener != null) {
            mOnFaBoDeviceControlListener.onConnected();
        }
    }

    @Override
    public void destroy() {
        for (Gpio gpio : mGpioList) {
            try {
                gpio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mGpioList.clear();

        mThread.interrupt();
    }

    @Override
    public void writeAnalog(final ArduinoUno.Pin pin, final int value) {
    }

    @Override
    public void writeDigital(final ArduinoUno.Pin pin, final ArduinoUno.Level hl) {
        try {
            mGpioList.get(pin.getPinNumber()).setValue(hl == ArduinoUno.Level.HIGH);
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
            return mGpioList.get(pin.getPinNumber()).getValue() ? ArduinoUno.Level.HIGH : ArduinoUno.Level.LOW;
        } catch (Exception e) {
            e.printStackTrace();
            return ArduinoUno.Level.LOW;
        }
    }

    @Override
    public void setPinMode(final ArduinoUno.Pin pin, final ArduinoUno.Mode mode) {

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
    public void setOnFaBoDeviceControlListener(final OnFaBoDeviceControlListener listener) {
        mOnFaBoDeviceControlListener = listener;
    }

    @Override
    public void addOnGPIOListener(final OnGPIOListener listener) {

    }

    @Override
    public void removeOnGPIOListener(final OnGPIOListener listener) {

    }
}
