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

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.param.FirmataV32;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoGPIOProfile;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoSystemProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.FaBoService;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualService;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualServiceFactory;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.VirtualServiceDBHelper;
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
    private static final String TAG = "FaBo";

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
    private int[] mDigitalPortStatus = {0, 0, 0};

    /**
     * 監視用Thread.
     */
    private WatchFirmataThread mWatchFirmataThread;

    /**
     * Version
     */
    private static final int[] VERSION = {0x02, 0x05};

    /**
     * ServiceIDを保持する.
     */
    private List<String> mServiceIdStore = new ArrayList<>();

    /**
     * Statusを保持.
     */
    private int mStatus;

    /**
     * 仮装サービスを管理するクラス.
     */
    private VirtualServiceDBHelper mDBHelper;

    /**
     * GPIOの値変更通知リスナー.
     */
    private final List<OnGPIOListener> mOnGPIOListeners = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();

        mDBHelper = new VirtualServiceDBHelper(getApplicationContext());
        test();

        // Set status.
        setStatus(FaBoConst.STATUS_FABO_NOCONNECT);

        // USBのEvent用のBroadcast Receiverを設定.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbEventReceiver, filter);

        // FaBoを直接操作するためのサービス
        getServiceProvider().addService(new FaBoService());

        // 仮装サービスの初期化
        initVirtualService();

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

    @Override
    protected SystemProfile getSystemProfile() {
        return new FaBoSystemProfile();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        EventManager.INSTANCE.removeAll();
        mServiceIdStore.clear();
    }

    /**
     * テスト用仮装データ
     */
    private void test() {
        if (mDBHelper.getServiceDataList().isEmpty()) {
            ServiceData serviceData = new ServiceData();
            serviceData.setName("RobotCar(Car)");
            serviceData.setServiceId("car_service_id");

            ProfileData profileData = new ProfileData();
            profileData.setServiceId("car_service_id");
            profileData.setType(ProfileData.Type.I2C_ROBOT_DRIVE_CONTROLLER);
            serviceData.addProfileData(profileData);

            mDBHelper.addServiceData(serviceData);
        }

        if (mDBHelper.getServiceDataList().size() == 1) {
            ServiceData serviceData = new ServiceData();
            serviceData.setName("FaBo LED");
            serviceData.setServiceId("led_service_id");

            ProfileData profileData = new ProfileData();
            profileData.setServiceId("led_service_id");
            profileData.setType(ProfileData.Type.GPIO_LIGHT);
            profileData.addPin(ArduinoUno.Pin.PIN_D2.getPinNumber());
            profileData.addPin(ArduinoUno.Pin.PIN_D3.getPinNumber());
            serviceData.addProfileData(profileData);

            ProfileData vibration = new ProfileData();
            vibration.setServiceId("led_service_id");
            vibration.setType(ProfileData.Type.GPIO_VIBRATION);
            vibration.addPin(ArduinoUno.Pin.PIN_D2.getPinNumber());
            vibration.addPin(ArduinoUno.Pin.PIN_D3.getPinNumber());
            serviceData.addProfileData(vibration);

            mDBHelper.addServiceData(serviceData);
        }

        if (mDBHelper.getServiceDataList().size() == 2) {
            ServiceData serviceData = new ServiceData();
            serviceData.setName("RobotCar(Mouse)");
            serviceData.setServiceId("mouse_service_id");

            ProfileData profileData = new ProfileData();
            profileData.setServiceId("mouse_service_id");
            profileData.setType(ProfileData.Type.I2C_MOUSE_DRIVE_CONTROLLER);
            serviceData.addProfileData(profileData);

            mDBHelper.addServiceData(serviceData);
        }
    }

    /**
     * 仮装サービスのデータをDBに追加します.
     * @param serviceData 追加する仮装サービスのデータ
     * @return 追加に成功した場合はtrue、それ以外はfalse
     */
    public boolean addServiceData(final ServiceData serviceData) {
        String serviceId = mDBHelper.createServiceId();
        serviceData.setServiceId(serviceId);
        boolean result = mDBHelper.addServiceData(serviceData) >= 0;
        if (result) {
            DConnectService service = VirtualServiceFactory.createService(serviceData);
            service.setOnline(FaBoConst.STATUS_FABO_RUNNING == mStatus);
            getServiceProvider().addService(service);
        }
        return result;
    }

    /**
     * 仮装サービスのデータを更新します.
     * @param serviceData 更新する仮装サービスのデータ
     * @return 更新に成功した場合はtrue、それ以外はfalse
     */
    public boolean updateServiceData(final ServiceData serviceData) {
        boolean result = mDBHelper.updateServiceData(serviceData) >= 0;
        if (result) {
            DConnectService service = getServiceProvider().getService(serviceData.getServiceId());
            if (service != null && service instanceof VirtualService) {
                ((VirtualService)service).setServiceData(serviceData);
            }
        }
        return result;
    }

    /**
     * 仮装サービスのデータを削除します.
     * @param serviceData 削除する仮装サービスのデータ
     */
    public void removeServiceData(final ServiceData serviceData) {
        getServiceProvider().removeService(serviceData.getServiceId());
        mDBHelper.removeServiceData(serviceData);
    }

    /**
     * 仮装サービスの初期化を行います.
     */
    private void initVirtualService() {
        if (DEBUG) {
            Log.i(TAG, "------------------------------------");
            Log.i(TAG, "Create virtual service list.");
            Log.i(TAG, "------------------------------------");
        }

        List<ServiceData> serviceDataList = mDBHelper.getServiceDataList();
        for (ServiceData serviceData : serviceDataList) {
            DConnectService service = VirtualServiceFactory.createService(serviceData);
            getServiceProvider().addService(service);
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

        initGPIO();

        // 5秒たってFirmataを検出できない場合はエラー.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mStatus == FaBoConst.STATUS_FABO_INIT) {
                    sendResultToActivity(FaBoConst.FAILED_CONNECT_FIRMATA);
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
                case FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB:
                    UsbDevice device = intent.getParcelableExtra("usbDevice");
                    openUsb(device);
                    break;

                case FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB:
                    initUsbDevice();
                    sendStatusToActivity(mStatus);
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
    private void sendMessage(final byte[] mByte) {
        if (mByte != null) {
            if (DEBUG) {
                Log.i(TAG, "sendMessage:" + mByte.length);
            }
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

        if (DEBUG) {
            Log.i(TAG, "setPortStatus:" + mDigitalPortStatus[port]);
        }
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
    public int getAnalogValue(final ArduinoUno.Pin pin) {
        return getAnalogValue(pin.getPinNumber());
    }

    /**
     * ANALOGの値の取得.
     *
     * @param pin PIN番号
     * @return Analogの値
     */
    private int getAnalogValue(final int pin) {
        ArduinoUno.Pin p = ArduinoUno.Pin.getPin(pin);
        if (p != null) {
            return p.getValue();
        }
        return -1;
    }

    /**
     * GPIOのデジタル値の取得.
     * @param pin 取得するPIN
     * @return GPIOのデジタル値
     */
    public int getDigitalValue(final ArduinoUno.Pin pin) {
        return getDigitalValue(pin.getPort(), pin.getBit());
    }

    /**
     * GPIOのデジタル値の取得.
     *
     * @param port PORT
     * @param pin  PIN
     * @return GPIOのデジタル値
     */
    private int getDigitalValue(final int port, final int pin) {
        int value = mDigitalPortStatus[port];
        if ((value & pin) == pin) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 各PINの設定.
     * @param pin ピン
     * @param mode モード
     */
    public void setPinMode(final ArduinoUno.Pin pin, final ArduinoUno.Mode mode) {
        byte[] command = new byte[3];
        command[0] = (byte) (FirmataV32.SET_PIN_MODE);
        command[1] = (byte) (pin.getPinNumber());
        command[2] = (byte) (mode.getValue());
        sendMessage(command);
        pin.setMode(mode);
    }

    /**
     * Analogの書き込みを行います.
     * @param pin 書き込みを行うピン
     * @param value 書き込む値(0〜255)
     */
    public void analogWrite(final ArduinoUno.Pin pin, final int value) {
        analogWrite(pin.getPinNumber(), value);
    }

    /**
     * Analogの書き込み.
     *
     * @param pinNo PIN番号
     * @param value 値
     */
    private void analogWrite(int pinNo, int value) {
        byte[] bytes = new byte[5];
        bytes[0] = (byte) FirmataV32.START_SYSEX;
        bytes[1] = (byte) (0x6F);
        bytes[2] = (byte) pinNo;
        bytes[3] = (byte) value;
        bytes[4] = (byte) FirmataV32.END_SYSEX;
        sendMessage(bytes);
    }

    /**
     * Digitalの書き込みを行います.
     * @param pin 書き込みを行うピン
     * @param hl HIGH or LOW
     */
    public void digitalWrite(final ArduinoUno.Pin pin, final ArduinoUno.Level hl) {
        digitalWrite(pin.getPort(), pin.getBit(), hl);
    }

    /**
     * Digitalの書き込み.
     *
     * @param port PORT番号
     * @param pinBit PIN番号
     * @param hl HIGHとLOWの値
     */
    private void digitalWrite(final int port, final int pinBit, final ArduinoUno.Level hl) {
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

    /**
     * Activityにメッセージを返信する.
     *
     * @param resultId 結果のID.
     */
    private void sendResultToActivity(final int resultId) {
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT);
        intent.putExtra("resultId", resultId);
        sendBroadcast(intent);
    }

    /**
     * Activityにステータス状態を返信する.
     *
     * @param statusId 結果のID.
     */
    private void sendStatusToActivity(final int statusId) {
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB_RESULT);
        intent.putExtra("statusId", statusId);
        sendBroadcast(intent);
    }

    /**
     *  OnGPIOListenerを追加します.
     * @param listener 追加するリスナー
     */
    public void addOnGPIOListener(final OnGPIOListener listener) {
        synchronized (mOnGPIOListeners) {
            if (!mOnGPIOListeners.contains(listener)) {
                mOnGPIOListeners.add(listener);
            }
        }
    }

    /**
     * OnGPIOListenerを削除します.
     * @param listener 削除するリスナー
     */
    public void removeOnGPIOListener(final OnGPIOListener listener) {
        synchronized (mOnGPIOListeners) {
            mOnGPIOListeners.remove(listener);
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
                        sendResultToActivity(FaBoConst.SUCCESS_CONNECT_FIRMATA);
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
                Log.i(TAG, "Start watch a fragment_fabo_firmata.");
                Log.i(TAG, "---------------------------------");
            }

            while (!mStopFlag) {
                for (int s = 0; s < mServiceIdStore.size(); s++) {
                    String serviceId = mServiceIdStore.get(s);
                    List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                            FaBoGPIOProfile.PROFILE_NAME, null, FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);

                    for (Event event : events) {
                        Bundle pins = new Bundle();
                        for (ArduinoUno.Pin pin : ArduinoUno.Pin.values()) {
                            switch (pin.getMode()) {
                                case GPIO_IN:
                                    pins.putInt(pin.getPinNames()[1], getDigitalValue(pin));
                                    break;
                                case ANALOG:
                                    pins.putInt(pin.getPinNames()[1], getAnalogValue(pin));
                                    break;
                            }
                        }

                        // Eventに値をおくる.
                        Intent intent = EventManager.createEventMessage(event);
                        intent.putExtra("pins", pins);
                        sendEvent(intent, event.getAccessToken());
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
                Log.i(TAG, "Stop watch a fragment_fabo_firmata.");
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

    public interface OnGPIOListener {
        void onAnalog();
        void onDigital();
    }
}