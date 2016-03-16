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
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.param.FirmataV32;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoGPIOProfile;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.fabo.profile.FaBoSystemProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class FaBoDeviceService extends DConnectMessageService {

    /** Tag. */
    private final static String TAG = "FABO_PLUGIN";

    /** USB Port. */
    private static UsbSerialPort mSerialPort = null;

    /** USBのSerial IO Manager. */
    private static SerialInputOutputManager mSerialIoManager;

    /** Executor. */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /** Port status. */
    private static int digitalPortStatus[] = {0,0,0};

    /** Analog pin values. A0-A5(Arduino UNO). */
    private static int analogPinValues[] = new int[20];

    /** ServiceID. */
    private static String mServiceId = "";

    /** Thredd. */
    private static Thread mThread;

    /** PinStatus. */
    private static int pinMode[] = new int[20];

    /** PinPort. */
    private static int pinPort[] = {ArduinoUno.PORT_D0, ArduinoUno.PORT_D1, ArduinoUno.PORT_D2, ArduinoUno.PORT_D3,
            ArduinoUno.PORT_D4, ArduinoUno.PORT_D5, ArduinoUno.PORT_D6, ArduinoUno.PORT_D7,
            ArduinoUno.PORT_D8, ArduinoUno.PORT_D9, ArduinoUno.PORT_D10, ArduinoUno.PORT_D11,
            ArduinoUno.PORT_D12, ArduinoUno.PORT_D13};

    /** PinPort. */
    private static int pinBit[] = {ArduinoUno.BIT_D0, ArduinoUno.BIT_D1, ArduinoUno.BIT_D2, ArduinoUno.BIT_D3,
            ArduinoUno.BIT_D4, ArduinoUno.BIT_D5, ArduinoUno.BIT_D6, ArduinoUno.BIT_D7,
            ArduinoUno.BIT_D8, ArduinoUno.BIT_D9, ArduinoUno.BIT_D10, ArduinoUno.BIT_D11,
            ArduinoUno.BIT_D12, ArduinoUno.BIT_D13};

    /** ServiceIDを保持する. */
    List<String> mServiceIdStore = new ArrayList<String>();

    @Override
    public void onCreate() {
        super.onCreate();

        //ここで、DConnectMessageServiceProviderにプロファイルを追加.
        addProfile(new FaBoServiceDiscoveryProfile(this));
        addProfile(new FaBoSystemProfile());
        addProfile(new FaBoGPIOProfile());

        // Eventの設定.
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // pinモードの初期状態を保存.
        for(int i = 0; i < 14; i++){
            pinMode[i] = FirmataV32.PIN_MODE_GPIO_OUT;
        }
        for(int i = 14; i < 20; i++){
            pinMode[i] = FirmataV32.PIN_MODE_ANALOG;
        }

        // USBのEvent用のBroadcast Receiverを設定.
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
        registerReceiver(mUSBEvent, mIntentFilter);

        // Threadを立ち上げ値の変化を保持.
        if(mThread == null) {
            mThread = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        for(int s = 0; s < mServiceIdStore.size(); s++) {

                            String serviceId = mServiceIdStore.get(s);
                            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                                    FaBoGPIOProfile.PROFILE_NAME, null, FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);

                            synchronized (events) {
                                for (Event event : events) {

                                    Bundle pins = new Bundle();
                                    for (int i = 0; i < 20; i++) {
                                        if (pinMode[i] == FirmataV32.PIN_MODE_GPIO_IN) {
                                            pins.putInt("" + i, getGPIOValue(pinPort[i], pinBit[i]));
                                        } else if (pinMode[i] == FirmataV32.PIN_MODE_ANALOG) {
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
                            e.printStackTrace();
                        }
                    }
                }
            }
            );
            mThread.start();
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new FaBoSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this){};
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new FaBoServiceDiscoveryProfile(this);
    }

    /**
     * USBをOpenする.
     */
    private void openUsb(){

        // USBManagerを取得.
        UsbManager mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

        // 使用可能なUSB Portを取得.
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();

        if(drivers.size() == 0) {
            sendResult(FaBoConst.CAN_NOT_FIND_USB);
            return;
        } else {
            //  発見したPortをResultに一時格納.
            for (final UsbSerialDriver driver : drivers) {
                final List<UsbSerialPort> ports = driver.getPorts();
                result.addAll(ports);
            }

            // 一番最後に発見されたPortをmSerialPortに格納.
            int count = result.size();
            mSerialPort = result.get(count - 1);

            // PortをOpen.
            UsbDeviceConnection connection = mUsbManager.openDevice(mSerialPort.getDriver().getDevice());

            if (connection == null) {
                sendResult(FaBoConst.FAILED_CONNECT_USB);
                Toast.makeText(this, R.string.can_not_open_usb, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Firmataは、57600bpsで接続する.
                mSerialPort.open(connection);
                mSerialPort.setParameters(FirmataV32.BAUDRATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                sendResult(FaBoConst.SUCCESS_CONNECT_ARDUINO);
            } catch (IOException e) {
                sendResult(FaBoConst.FAILED_CONNECT_ARDUINO);
                Toast.makeText(this.getContext(), "Error:" + e, Toast.LENGTH_SHORT).show();
                try {
                    mSerialPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mSerialPort = null;
                return;
            }
        }

        onDeviceStateChange();
    }

    /**
     * シリアル通信を開始.
     */
    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();

        // Portの状態をすべて0(Low)にする。
        digitalPortStatus[0] = 0; // 0000 0000
        digitalPortStatus[1] = 0; // 0000 0000
        digitalPortStatus[2] = 0; // 0000 0000

        byte[] command = new byte[2];

        // AnalogPin A0-A5の値に変化があったら通知する設定をおこなう(Firmata)
        for(int analogPin = 0; analogPin < 7; analogPin++) {
            command[0] = (byte) (FirmataV32.REPORT_ANALOG + analogPin);
            command[1] = (byte) FirmataV32.ENABLE;
            SendMessage(command);
        }

        // Portのデジタル値に変化があったら通知する設定をおこなう(Firmata)
        for(int digitalPort = 0; digitalPort < 3; digitalPort++) {
            command[0] = (byte) (FirmataV32.REPORT_DIGITAL + digitalPort);
            command[1] = (byte) FirmataV32.ENABLE;
            SendMessage(command);
        }

        Toast.makeText(this, R.string.open_usb, Toast.LENGTH_SHORT).show();
    }

    /**
     * シリアル通信をストップする.
     */
    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    /**
     * シリアル通信を開始する.
     */
    private void startIoManager() {
        if (mSerialPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mSerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    /**
     * Arduino側から返答のあるメッセージを受信するLisener.
     */
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.i(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {

                    for(int i = 0; i < data.length; i++) {

                        // 7bit目が1の場合は、コマンド.
                        if ((data[i] & 0x80) == 0x80) {
                            if((byte)(data[i] & 0xf0) == FirmataV32.ANALOG_MESSAGE) {
                                if ((i + 2) < data.length) {
                                    int pin = (data[i] & 0x0f);
                                    if (pin < 7) {
                                        int value = ((data[i + 2] & 0xff) << 7) + (data[i + 1] & 0xff);
                                        analogPinValues[pin + 14] = value;
                                    }
                                }
                            }
                            else if ((byte)(data[i] & 0xf0) == FirmataV32.DIGITAL_MESSAGE) {
                                if((i + 2) < data.length) {
                                    int port = (data[i] & 0x0f);
                                    int value = ((data[i + 2] & 0xff) << 8) + (data[i + 1] & 0xff);

                                    // Arduino UNOは3Portまで.
                                    if (port < 3) {

                                        // 1つ前の値を取得する.
                                        int lastValue =  digitalPortStatus[port];

                                        // 取得した値は保存する.
                                        digitalPortStatus[port] = value;
                                    }
                                }
                            }
                        }
                    }
                }
            };

    /**
     * Broadcast receiver for usb event.
     */
    private BroadcastReceiver mUSBEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if(action.equals(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB)) {
                openUsb();
            }
        }
    };

    /**
     * Arduinoへのメッセージの送信
     * @param msg String型のメッセージ
     */
    public void SendMessage(String msg) {

        if (mSerialIoManager != null) {

            try {
                mSerialPort.write(msg.getBytes(), 10000);
            } catch(Exception e){
                Log.e(TAG, "SendMessage:Error " + e);
            }

        }
    }

    /**
     * メッセージの送信
     * @param mByte Byte型のメッセージ
     */
    public void SendMessage(byte[] mByte) {
        Log.i(TAG, "SendMessage");
        if(mSerialIoManager != null) {

            try {
                mSerialPort.write(mByte, 10000);
            } catch (Exception e){
                Log.e(TAG, "SendMessage:Error " + e);
            }
        } else {
            Log.e(TAG, "Error: mSerialIoManager is null");
        }
    }

    /**
     * Portの状態の保存.
     * @param port Port番号
     * @param status Portのステータス
     */
    public void setPortStatus(int port, int status){
        digitalPortStatus[port] = status;
        Log.i(TAG, "setStatus:" + digitalPortStatus[port]);

    }

    /**
     * Portの状態の取得.
     * @param port Port番号
     *
     */
    public int getPortStatus(int port){
        return digitalPortStatus[port];
    }

    /**
     * Digitalの値の取得.
     * @param port ArduinoのPort番号.
     */
    public int getDigitalValue(int port){

        return digitalPortStatus[port];
    }

    /**
     * onChangeイベントの登録.
     * @param serviceId  現在接続中のデバイスプラグインのServiceId.
     */
    public void registerOnChange(String serviceId) {
        mServiceIdStore.add(serviceId);

        Log.i(TAG, "mServiceIdStore.size()" + mServiceIdStore.size());
        for(int i = 0; i < mServiceIdStore.size(); i++) {
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                    FaBoGPIOProfile.PROFILE_NAME, null, FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);
            Log.i(TAG, "size" + events.size());
        }
    }

    /**
     * onChangeイベントの削除.
     */
    public void unregisterOnChange(String serviceId) {
        Log.i(TAG, "unregisterOnChange()");

        Iterator serviceIds = mServiceIdStore.iterator();
        while(serviceIds.hasNext()){
            String tmpServiceId = (String)serviceIds.next();
            if(tmpServiceId.equals(serviceId)) serviceIds.remove();
        }

        Log.i(TAG, "mServiceIdStore.size()" + mServiceIdStore.size());
        for(int i = 0; i < mServiceIdStore.size(); i++) {
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                    FaBoGPIOProfile.PROFILE_NAME, null, FaBoGPIOProfile.ATTRIBUTE_ON_CHANGE);
            Log.i(TAG, "size" + events.size());
        }

    }

    /**
     * ANALOGの値の取得.
     * @param pin PIN番号
     * @return Analogの値
     */
    public int getAnalogValue(int pin) {
        return analogPinValues[pin];
    }

    /**
     * GPIOの値の取得.
     * @param port PORT
     * @param pin PIN
     * @return GPIOの値
     */
    public int getGPIOValue(int port, int pin) {
        int value = digitalPortStatus[port];
        if((value&pin) == pin){
            return 1;
        } else{
            return 0;
        }
    }

    /**
     * Pinの状態を保存する.
     * @param pinNo pin番号　
     * @param mode modeの値
     */
    public void setPin(int pinNo, int mode){
        pinMode[pinNo] = mode;
    }

    /**
     * Activityに状態を返信する.
     * @param resultId 結果のID.
     */
    private void sendResult(int resultId){
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT);
        intent.putExtra("resultId", resultId);
        sendBroadcast(intent);
    }
}