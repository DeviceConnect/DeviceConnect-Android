package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fabo.BuildConfig;
import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.IMouseCar;
import org.deviceconnect.android.deviceplugin.fabo.device.IRobotCar;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.param.FirmataV32;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabo.serialkit.FaBoUsbConst;
import io.fabo.serialkit.FaBoUsbListenerInterface;
import io.fabo.serialkit.FaBoUsbManager;

/**
 * Usb経由でFaBoデバイスを操作するクラス.
 */
public class FaBoUsbDeviceControl implements FaBoDeviceControl {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Tag.
     */
    private static final String TAG = "FaBo";

    /**
     * USB Serial Manager.
     */
    private FaBoUsbManager mFaBoUsbManager;

    /**
     * Port status.
     */
    private int[] mDigitalPortStatus = {0, 0, 0};

    /**
     * Version
     */
    private static final int[] VERSION = {0x02, 0x05};

    /**
     * Statusを保持.
     */
    private int mStatus;

    /**
     * GPIOの値変更通知リスナー.
     */
    private final List<OnGPIOListener> mOnGPIOListeners = new ArrayList<>();

    /**
     * FaBoデバイスの接続状態などを通知するリスナー.
     */
    private OnFaBoDeviceControlListener mOnFaBoDeviceControlListener;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * マウス型RobotCarを操作するクラス.
     */
    private MouseCar mMouseCar;

    /**
     * RobotCarを操作するクラス.
     */
    private RobotCar mRobotCar;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public FaBoUsbDeviceControl(final Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void initialize() {
        // Set status.
        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);

        // USBのEvent用のBroadcast Receiverを設定.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mContext.registerReceiver(mUsbEventReceiver, filter);

        initUsbDevice();
    }

    @Override
    public void destroy() {
        mOnFaBoDeviceControlListener = null;
        mOnGPIOListeners.clear();
        closeUsb();
        mContext.unregisterReceiver(mUsbEventReceiver);
    }

    @Override
    public void writeAnalog(final ArduinoUno.Pin pin, final int value) {
        byte[] bytes = new byte[5];
        bytes[0] = (byte) FirmataV32.START_SYSEX;
        bytes[1] = (byte) (0x6F);
        bytes[2] = (byte) pin.getPinNumber();
        bytes[3] = (byte) value;
        bytes[4] = (byte) FirmataV32.END_SYSEX;
        sendMessage(bytes);
    }

    @Override
    public void writeDigital(final ArduinoUno.Pin pin, final ArduinoUno.Level hl) {
        int port = pin.getPort();
        int pinBit = pin.getBit();
        if (hl == ArduinoUno.Level.HIGH) {
            int status = getPortStatus(port) | pinBit;
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (FirmataV32.DIGITAL_MESSAGE | port);
            bytes[1] = (byte) (status & 0xff);
            bytes[2] = (byte) ((status >> 8) & 0xff);
            sendMessage(bytes);
            setPortStatus(port, status);
        } else if (hl == ArduinoUno.Level.LOW) {
            int status = getPortStatus(port) & ~pinBit;
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (FirmataV32.DIGITAL_MESSAGE | port);
            bytes[1] = (byte) (status & 0xff);
            bytes[2] = (byte) ((status >> 8) & 0xff);
            sendMessage(bytes);
            setPortStatus(port, status);
        }
    }

    @Override
    public int getAnalog(final ArduinoUno.Pin pin) {
        return pin.getValue();
    }

    @Override
    public ArduinoUno.Level getDigital(final ArduinoUno.Pin pin) {
        int port = pin.getPort();
        int pinBit = pin.getBit();
        int value = mDigitalPortStatus[port];
        if ((value & pinBit) == pinBit) {
            return ArduinoUno.Level.HIGH;
        } else {
            return ArduinoUno.Level.LOW;
        }
    }

    @Override
    public void setPinMode(final ArduinoUno.Pin pin, final ArduinoUno.Mode mode) {
        byte[] command = new byte[3];
        command[0] = (byte) (FirmataV32.SET_PIN_MODE);
        command[1] = (byte) (pin.getPinNumber());
        command[2] = (byte) (mode.getValue());
        sendMessage(command);
        pin.setMode(mode);
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public void writeI2C(final byte[] buffer) {
        sendMessage(buffer);
    }

    @Override
    public void readI2C() {
        // TODO
    }

    @Override
    public IRobotCar getRobotCar() {
        synchronized (this) {
            if (mRobotCar == null) {
                mRobotCar = new RobotCar();
            }
            mRobotCar.setFaBoDeviceControl(this);
        }
        return mRobotCar;
    }

    @Override
    public IMouseCar getMouseCar() {
        synchronized (this) {
            if (mMouseCar == null) {
                mMouseCar = new MouseCar();
            }
            mMouseCar.setFaBoDeviceControl(this);
        }
        return mMouseCar;
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
     * GPIOのPIN情報を初期化します.
     */
    private void initGPIO() {
        for (ArduinoUno.Pin pin : ArduinoUno.Pin.values()) {
            if (pin.getPinNumber() < ArduinoUno.PIN_NO_A0) {
                pin.setMode(ArduinoUno.Mode.GPIO_OUT);
            } else {
                pin.setMode(ArduinoUno.Mode.ANALOG);
            }
            pin.setValue(0);
        }

        // Portの状態をすべて0(Low)にする.
        mDigitalPortStatus[0] = 0; // 0000 0000
        mDigitalPortStatus[1] = 0; // 0000 0000
        mDigitalPortStatus[2] = 0; // 0000 0000
    }

    /**
     * USBをOpenする.
     */
    private void openUsb(final UsbDevice usbDevice) {
        if (DEBUG) {
            Log.i(TAG, "----------------------------------------");
            Log.i(TAG, "Open USB.");
            Log.i(TAG, "DeviceName: " + usbDevice.getDeviceName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i(TAG, "DeviceProductName: " + usbDevice.getProductName());
            }
            Log.i(TAG, "----------------------------------------");
        }


        if (mFaBoUsbManager != null) {
            mFaBoUsbManager.closeConnection();
            mFaBoUsbManager.checkDevice(usbDevice);
            mFaBoUsbManager.connection(usbDevice);
        } else {
            mFaBoUsbManager = new FaBoUsbManager(mContext);
            mFaBoUsbManager.setParameter(FaBoUsbConst.BAUNDRATE_57600,
                    FaBoUsbConst.PARITY_NONE,
                    FaBoUsbConst.STOP_1,
                    FaBoUsbConst.FLOW_CONTROL_OFF,
                    FaBoUsbConst.BITRATE_8);
            mFaBoUsbManager.setListener(mInterface);
            mFaBoUsbManager.checkDevice(usbDevice);
            mFaBoUsbManager.connection(usbDevice);
        }
    }

    /**
     * USBをClose.
     */
    private void closeUsb() {
        if (DEBUG) {
            Log.i(TAG, "----------------------------------------");
            Log.i(TAG, "Close USB.");
            Log.i(TAG, "----------------------------------------");
        }

        if (mFaBoUsbManager != null) {
            mFaBoUsbManager.closeConnection();
            mFaBoUsbManager = null;
        }

        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);

        notifyDisconnectFaBoDevice();
    }

    /**
     * ステータスを変化する.
     *
     * @param status ステータス
     */
    private void setStatus(int status) {
        mStatus = status;

        if (DEBUG) {
            Log.i(TAG, "status:" + status);
        }
    }

    /**
     * シリアル通信を開始.
     */
    private void onDeviceStateChange() {
        setStatus(FaBoConst.STATUS_FABO_INIT);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // FirmataのVersion取得のコマンドを送付
        byte command[] = {(byte) 0xF9};
        sendMessage(command);

        initGPIO();

        // 5秒たってFirmataを検出できない場合はエラー.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStatus == FaBoConst.STATUS_FABO_INIT) {
                    setStatus(FaBoConst.STATUS_FABO_NOCONNECT);
                }
            }
        }, 5000);

        // Statusをinitへ.
        setStatus(FaBoConst.STATUS_FABO_INIT);
    }

    /**
     * Firmataの初期設定.
     */
    private void intFirmata() {
        if (DEBUG) {
            Log.i(TAG, "initFirmata");
        }

        byte[] command = new byte[2];

        // AnalogPin A0-A5の値に変化があったら通知する設定をおこなう(Firmata)
        for (int analogPin = 0; analogPin < 7; analogPin++) {
            command[0] = (byte) (FirmataV32.REPORT_ANALOG + analogPin);
            command[1] = (byte) FirmataV32.ENABLE;
            sendMessage(command);
        }

        // Portのデジタル値に変化があったら通知する設定をおこなう(Firmata)
        for (int digitalPort = 0; digitalPort < 3; digitalPort++) {
            command[0] = (byte) (FirmataV32.REPORT_DIGITAL + digitalPort);
            command[1] = (byte) FirmataV32.ENABLE;
            sendMessage(command);
        }
    }

    /**
     * Usbデバイスを初期化します.
     * <p>
     * Usbに接続されたデバイスの中にFaboデバイスが存在した場合に接続を行います。
     * Faboデバイスの場合には何もしません。
     * </p>
     */
    private void initUsbDevice() {
        if (DEBUG) {
            Log.i(TAG, "initUsbDevice");
        }

        notifyDisconnectFaBoDevice();

        setStatus(FaBoConst.STATUS_FABO_INIT);

        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        for (final UsbDevice device : deviceList.values()) {
            switch (device.getVendorId()) {
                case FaBoUsbConst.ARDUINO_UNO_VID:
                case FaBoUsbConst.ARDUINO_CC_UNO_VID:
                    if (DEBUG) {
                        Log.i(TAG, "Find device connecting arduino");
                    }
                    openUsb(device);
                    break;

                default:
                    if (DEBUG) {
                        Log.w(TAG, "Found the device that is not FaBo device.");
                        Log.w(TAG, "    VendorId: " + device.getVendorId());
                        Log.w(TAG, "    DeviceName: " + device.getDeviceName());
                    }
                    break;
            }
        }
    }

    /**
     * Broadcast receiver for usb event.
     */
    private BroadcastReceiver mUsbEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB: {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        UsbManager m = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
                        if (m.hasPermission(device)) {
                            openUsb(device);
                        }
                    }
                }   break;

                case FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB:
                    initUsbDevice();
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        UsbManager m = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
                        if (m.hasPermission(device)) {
                            openUsb(device);
                        } else {
                            Intent i = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
                            PendingIntent p = PendingIntent.getBroadcast(context, 0, i, 0);
                            m.requestPermission(device, p);
                        }
                    }
                }   break;

                case FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB:
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    closeUsb();
                    break;
            }
        }
    };

    /**
     * メッセージの送信
     *
     * @param mByte Byte型のメッセージ
     */
    private void sendMessage(final byte[] mByte) {
        if (mByte != null) {
            mFaBoUsbManager.writeBuffer(mByte);
        }
    }

    /**
     * Portの状態の保存.
     *
     * @param port   Port番号
     * @param status Portのステータス
     */
    private void setPortStatus(final int port, final int status) {
        mDigitalPortStatus[port] = status;
    }

    /**
     * Portの状態の取得.
     *
     * @param port Port番号
     */
    private int getPortStatus(final int port) {
        return mDigitalPortStatus[port];
    }

    /**
     * FaBoデバイスと接続されたことを通知します.
     */
    private void notifyConnectFaBoDevice() {
        if (mOnFaBoDeviceControlListener != null) {
            mOnFaBoDeviceControlListener.onConnected();
        }
    }

    /**
     * FaBoデバイスが切断されたことを通知します.
     */
    private void notifyDisconnectFaBoDevice() {
        if (mOnFaBoDeviceControlListener != null) {
            mOnFaBoDeviceControlListener.onDisconnected();
        }
    }

    /**
     * アナログのピンデータが変更されたことを通知します.
     */
    private void notifyAnalog() {
        synchronized (mOnGPIOListeners) {
            for (OnGPIOListener l : mOnGPIOListeners) {
                l.onAnalog();
            }
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
     * FaBoUsbManagerからの通知を受け取るリスナー.
     */
    private FaBoUsbListenerInterface mInterface = new FaBoUsbListenerInterface() {

        @Override
        public void onFind(final UsbDevice usbDevice, final int type) {
            if (DEBUG) {
                Log.i(TAG, "onFind:" + usbDevice.getDeviceName());
                Log.i(TAG, "onFind: type = " + type);
            }
        }

        @Override
        public void onStatusChanged(final UsbDevice usbDevice, final int status) {
            if (DEBUG) {
                Log.i(TAG, "onStatusChanged: status=" + status + " device=" + usbDevice.getDeviceName());
            }

            if (status == FaBoUsbConst.CONNECTED) {
                if (DEBUG) {
                    Log.i(TAG, "onStatusChanged:CONNECTED");
                }
                onDeviceStateChange();
            }
        }

        @Override
        public void readBuffer(final int deviceId, final byte[] data) {
            for (int i = 0; i < data.length; i++) {
                if (mStatus == FaBoConst.STATUS_FABO_INIT) {
                    if ((i + 2) < data.length) {
                        if ((byte) (data[i] & 0xff) == (byte) 0xf9 &&
                                (byte) (data[i + 1] & 0xff) == (byte) VERSION[0] &&
                                (byte) (data[i + 2] & 0xff) == (byte) VERSION[1]) {
                            setStatus(FaBoConst.STATUS_FABO_RUNNING);
                            intFirmata();
                            notifyConnectFaBoDevice();
                        }
                    }
                } else {
                    // 7bit目が1の場合は、コマンド.
                    if ((data[i] & 0x80) == 0x80) {
                        if ((byte) (data[i] & 0xf0) == FirmataV32.ANALOG_MESSAGE) {
                            if ((i + 2) < data.length) {
                                int pin = (data[i] & 0x0f);
                                if (pin < 7) {
                                    int value = ((data[i + 2] & 0xff) << 7) + (data[i + 1] & 0xff);
                                    ArduinoUno.Pin p = ArduinoUno.Pin.getPin(pin + 14);
                                    if (p != null) {
                                        p.setValue(value);
                                    }
                                    notifyAnalog();
                                }
                            }
                        } else if ((byte) (data[i] & 0xf0) == FirmataV32.DIGITAL_MESSAGE) {
                            if ((i + 2) < data.length) {
                                int port = (data[i] & 0x0f);
                                int value = ((data[i + 2] & 0xff) << 8) + (data[i + 1] & 0xff);
                                // Arduino UNOは3Portまで.
                                if (port < 3) {
                                    // 取得した値は保存する.
                                    mDigitalPortStatus[port] = value;
                                    notifyDigital();
                                }
                            }
                        }
                    }
                }
            }
        }
    };
}
