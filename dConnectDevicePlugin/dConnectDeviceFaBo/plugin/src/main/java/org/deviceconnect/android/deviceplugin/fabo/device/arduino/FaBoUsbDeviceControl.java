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
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabo.serialkit.FaBoUsbConst;
import io.fabo.serialkit.FaBoUsbListenerInterface;
import io.fabo.serialkit.FaBoUsbManager;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.ANALOG_MESSAGE;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.DIGITAL_MESSAGE;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.END_SYSEX;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.REPORT_VERSION;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.START_SYSEX;
import static org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst.STATUS_FABO_INIT;

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
     * FirmataのVersion.
     */
    private static final int[] VERSION = {0x02, 0x05};

    /**
     * Statusを保持.
     */
    private int mStatus;

    /**
     * USB機器へのアクセス許可を行なっているかフラグ.
     */
    private boolean mRequestPermission;

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
     * sysex messageを解析中フラグ.
     * <p>
     * sysex messageを解析している間はtrue、それ以外はfalse
     * </p>
     */
    private boolean mParsingSysex;

    /**
     * Arduinoから送られてきたピン番号.
     */
    private int mChannel;

    /**
     * Arduinoから送られてきたコマンド.
     * <p>
     * 以下のコマンドが存在します。
     *  - DIGITAL_MESSAGE
     *  - ANALOG_MESSAGE
     *  - REPORT_ANALOG
     *  - REPORT_DIGITAL
     * </p>
     */
    private byte mCommand;

    /**
     * コマンドのデータカウント.
     */
    private int mWaitForData;

    /**
     * Arduinoから送られてきたデータを一時的に格納するバッファ.
     */
    private ByteArrayOutputStream mStoredInputData;

    /**
     * マウス型RobotCarを操作するクラス.
     */
    private MouseCar mMouseCar;

    /**
     * RobotCarを操作するクラス.
     */
    private RobotCar mRobotCar;

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
     * I2Cを格納するリスト.
     */
    private List<BaseI2C> mI2CList = new ArrayList<>();

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public FaBoUsbDeviceControl(final Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void initialize() {
        mStoredInputData = new ByteArrayOutputStream();
        mParsingSysex = false;

        // Set status.
        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);

        // USBのEvent用のBroadcast Receiverを設定.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbEventReceiver, filter);

        initUsbDevice();
        initI2CSDevice();
    }

    @Override
    public void destroy() {
        mOnFaBoDeviceControlListener = null;
        mOnGPIOListeners.clear();
        mI2CList.clear();
        closeUsb();
        mContext.unregisterReceiver(mUsbEventReceiver);
    }

    @Override
    public boolean isPinSupported(final FaBoShield.Pin pin) {
        return true;
    }

    @Override
    public void writeAnalog(final FaBoShield.Pin pin, final int value) {
        byte[] bytes = new byte[5];
        bytes[0] = (byte) START_SYSEX;
        bytes[1] = (byte) (0x6F);
        bytes[2] = (byte) pin.getPinNumber();
        bytes[3] = (byte) value;
        bytes[4] = (byte) END_SYSEX;
        sendMessage(bytes);
    }

    @Override
    public void writeDigital(final FaBoShield.Pin pin, final FaBoShield.Level hl) {
        int port = pin.getPort();
        int pinBit = pin.getBit();
        if (hl == FaBoShield.Level.HIGH) {
            int status = getPortStatus(port) | pinBit;
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (DIGITAL_MESSAGE | port);
            bytes[1] = (byte) (status & 0xFF);
            bytes[2] = (byte) ((status >> 8) & 0xFF);
            sendMessage(bytes);
            setPortStatus(port, status);
        } else if (hl == FaBoShield.Level.LOW) {
            int status = getPortStatus(port) & ~pinBit;
            byte[] bytes = new byte[3];
            bytes[0] = (byte) (DIGITAL_MESSAGE | port);
            bytes[1] = (byte) (status & 0xFF);
            bytes[2] = (byte) ((status >> 8) & 0xFF);
            sendMessage(bytes);
            setPortStatus(port, status);
        }
    }

    @Override
    public int getAnalog(final FaBoShield.Pin pin) {
        return pin.getValue();
    }

    @Override
    public FaBoShield.Level getDigital(final FaBoShield.Pin pin) {
        int port = pin.getPort();
        int pinBit = pin.getBit();
        int value = mDigitalPortStatus[port];
        if ((value & pinBit) == pinBit) {
            return FaBoShield.Level.HIGH;
        } else {
            return FaBoShield.Level.LOW;
        }
    }

    @Override
    public void setPinMode(final FaBoShield.Pin pin, final FaBoShield.Mode mode) {
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
    public IRobotCar getRobotCar() {
        mRobotCar.setFaBoDeviceControl(this);
        return mRobotCar;
    }

    @Override
    public IMouseCar getMouseCar() {
        mMouseCar.setFaBoDeviceControl(this);
        return mMouseCar;
    }

    @Override
    public IADXL345 getADXL345() {
        mADXL345.setFaBoDeviceControl(this);
        return mADXL345;
    }

    @Override
    public IADT7410 getADT7410() {
        mADT7410.setFaBoDeviceControl(this);
        return mADT7410;
    }

    @Override
    public IHTS221 getHTS221() {
        mHTS221.setFaBoDeviceControl(this);
        return mHTS221;
    }

    @Override
    public IVCNL4010 getVCNL4010() {
        mVCNL4010.setFaBoDeviceControl(this);
        return mVCNL4010;
    }

    @Override
    public IISL29034 getISL29034() {
        mISL29034.setFaBoDeviceControl(this);
        return mISL29034;
    }

    @Override
    public IMPL115 getMPL115() {
        mMPL115.setFaBoDeviceControl(this);
        return mMPL115;
    }

    @Override
    public ILIDARLiteV3 getLIDARLite() {
        mLIDARLiteV3.setFaBoDeviceControl(this);
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
     * I2Cデバイスを初期化します.
     */
    private void initI2CSDevice() {
        mRobotCar = new RobotCar();
        mMouseCar = new MouseCar();
        mADXL345 = new ADXL345();
        mADT7410 = new ADT7410();
        mHTS221 = new HTS221();
        mVCNL4010 = new VCNL4010();
        mISL29034 = new ISL29034();
        mMPL115 = new MPL115();
        mLIDARLiteV3 = new LIDARLiteV3();

        mI2CList.add(mADXL345);
        mI2CList.add(mADT7410);
        mI2CList.add(mHTS221);
        mI2CList.add(mVCNL4010);
        mI2CList.add(mISL29034);
        mI2CList.add(mMPL115);
        mI2CList.add(mLIDARLiteV3);
    }

    /**
     * GPIOのPIN情報を初期化します.
     */
    private void initGPIO() {
        for (FaBoShield.Pin pin : FaBoShield.Pin.values()) {
            if (pin.getPinNumber() < FaBoShield.PIN_NO_A0) {
                pin.setMode(FaBoShield.Mode.GPIO_OUT);
            } else {
                pin.setMode(FaBoShield.Mode.ANALOG);
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
    private synchronized void openUsb(final UsbDevice usbDevice) {
        if (DEBUG) {
            Log.i(TAG, "----------------------------------------");
            Log.i(TAG, "Open USB.");
            Log.i(TAG, "DeviceName: " + usbDevice.getDeviceName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i(TAG, "DeviceProductName: " + usbDevice.getProductName());
            }
            Log.i(TAG, "----------------------------------------");
        }

        mParsingSysex = false;

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
    private synchronized void closeUsb() {
        if (DEBUG) {
            Log.i(TAG, "----------------------------------------");
            Log.i(TAG, "Close USB.");
            Log.i(TAG, "----------------------------------------");
        }

        if (mFaBoUsbManager != null) {
            mFaBoUsbManager.closeConnection();
            mFaBoUsbManager.unregisterMyReceiver();
            mFaBoUsbManager = null;
        }

        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);

        notifyDisconnectFaBoDevice();

        mRequestPermission = false;
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
        setStatus(STATUS_FABO_INIT);

        // Arduinoの初期化が行われるまで少し待つ
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // GPIOを格納しているクラスの初期化
        initGPIO();

        // FirmataのVersion取得のコマンドを送付
        byte command[] = { REPORT_VERSION };
        sendMessage(command);

        // 5秒たってFirmataを検出できない場合はエラー.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStatus == STATUS_FABO_INIT) {
                    setStatus(FaBoConst.STATUS_FABO_NOCONNECT);
                    if (mOnFaBoDeviceControlListener != null) {
                        mOnFaBoDeviceControlListener.onFailedConnected();
                    }
                }
            }
        }, 5000);

        // Statusをinitへ.
        setStatus(STATUS_FABO_INIT);
    }

    /**
     * Firmataの初期設定.
     */
    private void intFirmata() {
        if (DEBUG) {
            Log.i(TAG, "initFirmata");
        }

        byte[] command = new byte[2];

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // AnalogPin A0-A5の値に変化があったら通知する設定をおこなう(Firmata)
        for (int analogPin = 0; analogPin < 7; analogPin++) {
            command[0] = (byte) (FirmataV32.REPORT_ANALOG + analogPin);
            command[1] = (byte) FirmataV32.ENABLE;
            sendMessage(command);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Portのデジタル値に変化があったら通知する設定をおこなう(Firmata)
        for (int digitalPort = 0; digitalPort < 3; digitalPort++) {
            command[0] = (byte) (FirmataV32.REPORT_DIGITAL + digitalPort);
            command[1] = (byte) FirmataV32.ENABLE;
            sendMessage(command);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);

        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        for (final UsbDevice device : deviceList.values()) {
            switch (device.getVendorId()) {
                case FaBoUsbConst.ARDUINO_UNO_VID:
                case FaBoUsbConst.ARDUINO_CC_UNO_VID:
                    if (DEBUG) {
                        Log.i(TAG, "Find device connecting arduino");
                    }
                    if (manager.hasPermission(device)) {
                        openUsb(device);
                    } else {
                        Intent i = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
                        PendingIntent p = PendingIntent.getBroadcast(mContext, 0, i, 0);
                        manager.requestPermission(device, p);
                    }
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
                    mRequestPermission = false;
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        UsbManager m = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
                        if (m.hasPermission(device)) {
                            openUsb(device);
                        }
                    }
                }   break;

                case FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB:
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        UsbManager m = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
                        if (m.hasPermission(device)) {
                            openUsb(device);
                        } else {
                            if (mRequestPermission) {
                                // 既に許可のリクエストを投げている場合に処理しない
                                return;
                            }
                            mRequestPermission = true;

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
     * I2Cへの書き込みを行います.
     * @param buffer 書き込むバッファ
     */
    void writeI2C(final byte[] buffer) {
        sendMessage(buffer);
    }

    /**
     * メッセージの送信
     *
     * @param buf Byte型のメッセージ
     */
    private synchronized void sendMessage(final byte[] buf) {
        if (buf != null && mFaBoUsbManager != null) {
            mFaBoUsbManager.writeBuffer(buf);
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
     * REPORT_VERIONの解析を行います.
     * @param data データ
     */
    private void reportVersion(final byte[] data) {
        if (DEBUG) {
            Log.i(TAG, "REPORT_VERSION");
            Log.i(TAG, "  Version: " + data[0] + "." + data[1]);
        }

        if ((byte) (data[0] & 0xFF) == (byte) VERSION[0] && (byte) (data[1] & 0xFF) == (byte) VERSION[1]) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setStatus(FaBoConst.STATUS_FABO_RUNNING);
                    intFirmata();
                    notifyConnectFaBoDevice();
                }
            }).start();
        } else {
            if (DEBUG) {
                Log.w(TAG, "Not support version.");
            }
        }
    }

    /**
     * Arduinoから送られてきたアナログのデータを設定します.
     * @param port ピン番号
     * @param value 値
     */
    private void setAnalogData(final int port, final int value) {
        if (port < 7) {
            FaBoShield.Pin p = FaBoShield.Pin.getPin(port + 14);
            if (p != null) {
                p.setValue(value);
            }
            notifyAnalog();
        }
    }

    /**
     * Arduinoから送られてきたデジタルのデータを設定します.
     * @param port ピン番号
     * @param value 値
     */
    private void setDigitalData(final int port, final int value) {
        if (port < 3) {
            mDigitalPortStatus[port] = value;
            notifyDigital();
        }
    }

    /**
     * sysex messageを解析します.
     * @param data sysex messageのデータ
     */
    private void parseSysExMessage(final byte[] data) {
        switch (data[0]) {
            case FirmataV32.REPORT_FIRMWARE:
                break;

            case FirmataV32.I2C_REPLY:
                int offset = 1;
                int address = decodeByte(data[offset++], data[offset]);
                for (BaseI2C i2c : mI2CList) {
                    if (address == i2c.getAddress()) {
                        i2c.onReadData(data);
                    }
                }
                break;

            case FirmataV32.STRING_DATA:
                if (DEBUG) {
                    Log.i(TAG, "FirmataV32.STRING_DATA");
                    Log.i(TAG, FirmataUtil.decodeString(data, 1, data.length));
                }
                break;

            default:
                if (DEBUG) {
                    Log.e(TAG, "Unknown: " + data[0]);
                }
                break;
        }
    }

    /**
     * Arduinoから読み込んだデータを処理します.
     * @param inputData 読み込んだデータ
     */
    private void processInput(final byte inputData) {
        if (mParsingSysex) {
            if (inputData == END_SYSEX) {
                parseSysExMessage(mStoredInputData.toByteArray());
                mParsingSysex = false;
            } else {
                mStoredInputData.write(inputData);
            }
        } else if (mWaitForData > 0 && (inputData & 0x80) != 0x80) {
            mWaitForData--;
            mStoredInputData.write(inputData);

            if (mWaitForData == 0) {
                switch (mCommand) {
                    case DIGITAL_MESSAGE:
                        setDigitalData(mChannel, FirmataUtil.decodeByte(mStoredInputData.toByteArray()));
                        break;
                    case ANALOG_MESSAGE:
                        setAnalogData(mChannel, FirmataUtil.decodeByte(mStoredInputData.toByteArray()));
                        break;
                    case REPORT_VERSION:
                        reportVersion(mStoredInputData.toByteArray());
                        break;
                }
            }
        } else {
            byte command;
            if ((inputData & 0xFF) < 0xF0) {
                command = (byte) (inputData & 0xF0);
            } else {
                command = inputData;
            }

            switch (command) {
                case START_SYSEX:
                    mParsingSysex = true;
                    mStoredInputData.reset();
                    break;

                case DIGITAL_MESSAGE:
                case ANALOG_MESSAGE:
                case REPORT_VERSION:
                    mWaitForData = 2;
                    mStoredInputData.reset();
                    mCommand = command;
                    mChannel = (byte) (inputData & 0x0F);
                    break;
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
        public void readBuffer(final int deviceId, final byte[] datas) {

//            if (DEBUG) {
//                final StringBuilder sb = new StringBuilder();
//                for (byte data : datas) {
//                    sb.append(Integer.toHexString(data & 0xff));
//                    sb.append(" ");
//                }
//                Log.i(TAG, "  readBuffer: " + sb.toString());
//            }

            try {
                for (byte data : datas) {
                    processInput(data);
                }
            } catch (OutOfMemoryError e) {
                mStoredInputData = new ByteArrayOutputStream();
                mParsingSysex = false;
            } catch (Exception e) {
                mStoredInputData.reset();
                mParsingSysex = false;
            }
        }
    };
}
