/*
 FaBoDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fabo.device.robotcar.car.RobotCarService;
import org.deviceconnect.android.deviceplugin.fabo.device.robotcar.mouse.MouseCarService;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.param.FirmataV32;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoGPIOProfile;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoSystemProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.FaBoService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import io.fabo.serialkit.FaBoUsbConst;
import io.fabo.serialkit.FaBoUsbListenerInterface;
import io.fabo.serialkit.FaBoUsbManager;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoDeviceService extends DConnectMessageService implements FaBoUsbListenerInterface {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Tag.
     */
    private static final String TAG = "FABO_PLUGIN_SERVICE";

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("fabo.dplugin");

    /**
     * USB Serial Manager.
     */
    private FaBoUsbManager mFaBoUsbManager;

    /**
     * Port status.
     */
    private int mDigitalPortStatus[] = {0, 0, 0};

    /**
     * Analog pin values. A0-A5(Arduino UNO).
     */
    private int mAnalogPinValues[] = new int[20];

    /**
     * 監視用Thread.
     */
    private WatchFirmataThread mWatchFirmataThread;

    /**
     * Version
     */
    private static final int VERSION[] = {0x02, 0x05};

    /**
     * PinStatus.
     */
    private int[] mPinMode = new int[20];

    /**
     * PinPort.
     */
    private int mPinPort[] = {
            ArduinoUno.PORT_D0, ArduinoUno.PORT_D1, ArduinoUno.PORT_D2, ArduinoUno.PORT_D3,
            ArduinoUno.PORT_D4, ArduinoUno.PORT_D5, ArduinoUno.PORT_D6, ArduinoUno.PORT_D7,
            ArduinoUno.PORT_D8, ArduinoUno.PORT_D9, ArduinoUno.PORT_D10, ArduinoUno.PORT_D11,
            ArduinoUno.PORT_D12, ArduinoUno.PORT_D13
    };

    /**
     * PinPort.
     */
    private int mPinBit[] = {
            ArduinoUno.BIT_D0, ArduinoUno.BIT_D1, ArduinoUno.BIT_D2, ArduinoUno.BIT_D3,
            ArduinoUno.BIT_D4, ArduinoUno.BIT_D5, ArduinoUno.BIT_D6, ArduinoUno.BIT_D7,
            ArduinoUno.BIT_D8, ArduinoUno.BIT_D9, ArduinoUno.BIT_D10, ArduinoUno.BIT_D11,
            ArduinoUno.BIT_D12, ArduinoUno.BIT_D13
    };

    /**
     * ServiceIDを保持する.
     */
    private List<String> mServiceIdStore = new ArrayList<>();

    /**
     * Statusを保持.
     */
    private int mStatus;

    /**
     * RobotTypeを保持.
     */
    private int mRobotType;


    @Override
    public void onCreate() {
        super.onCreate();

        // Set status.
        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);

        // pinモードの初期状態を保存.
        for (int i = 0; i < 14; i++) {
            mPinMode[i] = FirmataV32.PIN_MODE_GPIO_OUT;
        }

        for (int i = 14; i < 20; i++) {
            mPinMode[i] = FirmataV32.PIN_MODE_ANALOG;
        }

        // DefaultでMouse型
        mRobotType = 0;

        // USBのEvent用のBroadcast Receiverを設定.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbEventReceiver, filter);

        // FaBoサービスを登録.
        getServiceProvider().addService(new FaBoService());
        getServiceProvider().addService(new RobotCarService());
        getServiceProvider().addService(new MouseCarService());

        // USBが接続されている可能性があるので、初期化処理を行う
        initUsbDevice();
    }

    @Override
    public void onDestroy() {
        closeUsb();
        unregisterReceiver(mUsbEventReceiver);
        super.onDestroy();
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // ManagerのEvent送信経路切断通知受信時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (origin != null) {
            EventManager.INSTANCE.removeEvents(origin);
            List<Event> events = EventManager.INSTANCE.getEventList(FaBoGPIOProfile.PROFILE_NAME,
                    FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);
            for (Event event : events) {
                if (event.getOrigin().equals(origin)) {
                    String serviceId = event.getServiceId();
                    Iterator serviceIds = mServiceIdStore.iterator();
                    while (serviceIds.hasNext()) {
                        String tmpServiceId = (String) serviceIds.next();
                        if (tmpServiceId.equals(serviceId)) serviceIds.remove();
                    }
                }
            }
        } else {
            resetPluginResource();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理.
        if (DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        EventManager.INSTANCE.removeAll();
        mServiceIdStore.clear();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new FaBoSystemProfile();
    }

    /**
     * DConnectServiceのOnline状況を設定します.
     * @param online オンライン状態
     */
    private void setOnline(final boolean online) {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            service.setOnline(online);
        }
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
            return;
        } else {
            mFaBoUsbManager = new FaBoUsbManager(this);
            mFaBoUsbManager.setParameter(FaBoUsbConst.BAUNDRATE_57600,
                    FaBoUsbConst.PARITY_NONE,
                    FaBoUsbConst.STOP_1,
                    FaBoUsbConst.FLOW_CONTROL_OFF,
                    FaBoUsbConst.BITRATE_8);
            mFaBoUsbManager.setListener(this);
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

        endWatchFirmata();

        if (mFaBoUsbManager != null) {
            mFaBoUsbManager.closeConnection();
            mFaBoUsbManager = null;
        }

        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);
        setOnline(false);
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

        // Portの状態をすべて0(Low)にする.
        mDigitalPortStatus[0] = 0; // 0000 0000
        mDigitalPortStatus[1] = 0; // 0000 0000
        mDigitalPortStatus[2] = 0; // 0000 0000

        // 5秒たってFirmataを検出できない場合はエラー.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStatus == FaBoConst.STATUS_FABO_INIT) {
                    sendResult(FaBoConst.FAILED_CONNECT_FIRMATA);
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

        setOnline(false);

        setStatus(FaBoConst.STATUS_FABO_INIT);

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        for (final UsbDevice device : deviceList.values()) {
            switch (device.getVendorId()) {
                case FaBoUsbConst.ARDUINO_UNO_VID:
                case FaBoUsbConst.ARDUINO_CC_UNO_VID:
                    Log.w(TAG, "find device connecting arduino");
                    openUsb(device);
                    break;

                default:
                    if (DEBUG) {
                        Log.w(TAG, "Found the device that is not Fabo.");
                        Log.w(TAG, "VendorId: " + device.getVendorId());
                        Log.w(TAG, "DeviceName: " + device.getDeviceName());
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
                case FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB:
                    UsbDevice device = intent.getParcelableExtra("usbDevice");
                    openUsb(device);
                    break;

                case FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB:
                    initUsbDevice();
                    sendStatus(mStatus);
                    break;

                case FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB:
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    closeUsb();
                    break;
            }
        }
    };

    /**
     * FaboUsbManagerを取得します.
     * <p>
     * UsbにFaboデバイスが接続されていない場合はfalseを返却します.
     * </p>
     * @return FaboUsbManagerのインスタンス
     */
    public FaBoUsbManager getFaBoUsbManager() {
        return mFaBoUsbManager;
    }

    /**
     * メッセージの送信
     *
     * @param mByte Byte型のメッセージ
     */
    public void sendMessage(final byte[] mByte) {
        Log.i(TAG, "sendMessage:" + mByte.length);
        if(mByte != null) {
            mFaBoUsbManager.writeBuffer(mByte);
        }
    }

    /**
     * Portの状態の保存.
     *
     * @param port   Port番号
     * @param status Portのステータス
     */
    public void setPortStatus(final int port, final int status) {
        mDigitalPortStatus[port] = status;

        if (DEBUG) {
            Log.i(TAG, "setPortStatus:" + mDigitalPortStatus[port]);
        }
    }

    /**
     * Portの状態の取得.
     *
     * @param port Port番号
     */
    public int getPortStatus(final int port) {
        return mDigitalPortStatus[port];
    }

    /**
     * Digitalの値の取得.
     *
     * @param port ArduinoのPort番号.
     */
    public int getDigitalValue(final int port) {
        return mDigitalPortStatus[port];
    }

    /**
     * onChangeイベントの登録.
     *
     * @param serviceId 現在接続中のデバイスプラグインのServiceId.
     */
    public void registerOnChange(final String serviceId) {
        mServiceIdStore.add(serviceId);
    }

    /**
     * onChangeイベントの削除.
     */
    public void unregisterOnChange(final String serviceId) {
        Iterator serviceIds = mServiceIdStore.iterator();
        while (serviceIds.hasNext()) {
            String tmpServiceId = (String) serviceIds.next();
            if (tmpServiceId.equals(serviceId)) {
                serviceIds.remove();
            }
        }
    }

    /**
     * ANALOGの値の取得.
     *
     * @param pin PIN番号
     * @return Analogの値
     */
    public int getAnalogValue(final int pin) {
        return mAnalogPinValues[pin];
    }

    /**
     * GPIOの値の取得.
     *
     * @param port PORT
     * @param pin  PIN
     * @return GPIOの値
     */
    public int getGPIOValue(final int port, final int pin) {
        int value = mDigitalPortStatus[port];
        if ((value & pin) == pin) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Pinの状態を保存する.
     *
     * @param pinNo pin番号
     * @param mode  modeの値
     */
    public void setPin(final int pinNo, final int mode) {
        mPinMode[pinNo] = mode;
    }

    /**
     * Activityにメッセージを返信する.
     *
     * @param resultId 結果のID.
     */
    private void sendResult(final int resultId) {
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT);
        intent.putExtra("resultId", resultId);
        sendBroadcast(intent);
    }

    /**
     * Activityにステータス状態を返信する.
     *
     * @param statusId 結果のID.
     */
    private void sendStatus(final int statusId) {
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB_RESULT);
        intent.putExtra("statusId", statusId);
        sendBroadcast(intent);
    }

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
            Log.i(TAG, "onStatusChanged:CONNECTED");
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
                        sendResult(FaBoConst.SUCCESS_CONNECT_FIRMATA);
                        intFirmata();
                        startWatchFirmata();

                        setOnline(true);
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
                                mAnalogPinValues[pin + 14] = value;
                            }
                        }
                    } else if ((byte) (data[i] & 0xf0) == FirmataV32.DIGITAL_MESSAGE) {
                        if ((i + 2) < data.length) {
                            int port = (data[i] & 0x0f);
                            int value = ((data[i + 2] & 0xff) << 8) + (data[i + 1] & 0xff);

                            // Arduino UNOは3Portまで.
                            if (port < 3) {
                                // 1つ前の値を取得する.
                                int lastValue = mDigitalPortStatus[port];
                                // 取得した値は保存する.
                                mDigitalPortStatus[port] = value;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 値監視用のThread.
     */
    private void startWatchFirmata() {
        if (mWatchFirmataThread == null) {
            mWatchFirmataThread = new WatchFirmataThread();
            mWatchFirmataThread.start();
        }
    }

    /**
     * Threadを停止.
     */
    private void endWatchFirmata() {
        if (mWatchFirmataThread != null) {
            mWatchFirmataThread.stopWatchFirmata();
            mWatchFirmataThread = null;
        }
    }

    /**
     * 監視用スレッド.
     */
    private class WatchFirmataThread extends Thread {
        /**
         * 監視スレッド停止フラグ.
         */
        private boolean mStopFlag;

        @Override
        public void run() {
            if (DEBUG) {
                Log.i(TAG, "---------------------------------");
                Log.i(TAG, "Start watch a firmata.");
                Log.i(TAG, "---------------------------------");
            }

            while (!mStopFlag) {

                for (int s = 0; s < mServiceIdStore.size(); s++) {
                    String serviceId = mServiceIdStore.get(s);
                    List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                            FaBoGPIOProfile.PROFILE_NAME, null, FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);

                    synchronized (events) {
                        for (Event event : events) {
                            Bundle pins = new Bundle();

                            for (int i = 0; i < mPinMode.length; i++) {
                                if (mPinMode[i] == FirmataV32.PIN_MODE_GPIO_IN) {
                                    pins.putInt("" + i, getGPIOValue(mPinPort[i], mPinBit[i]));
                                } else if (mPinMode[i] == FirmataV32.PIN_MODE_ANALOG) {
                                    pins.putInt("" + i, getAnalogValue(i));
                                }
                            }

                            // Eventに値をおくる.
                            Intent intent = EventManager.createEventMessage(event);
                            intent.putExtra("pins", pins);
                            sendEvent(intent, event.getAccessToken());
                        }
                    }
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (DEBUG) {
                Log.i(TAG, "---------------------------------");
                Log.i(TAG, "Stop watch a firmata.");
                Log.i(TAG, "---------------------------------");
            }
        }

        /**
         * 監視用スレッドを停止します.
         */
        void stopWatchFirmata() {
            mStopFlag = true;
            interrupt();
        }
    }

    public void setRobotType(int type) {
        this.mRobotType = type;
    }

    public int getRobotType(){
        return this.mRobotType;
    }
}