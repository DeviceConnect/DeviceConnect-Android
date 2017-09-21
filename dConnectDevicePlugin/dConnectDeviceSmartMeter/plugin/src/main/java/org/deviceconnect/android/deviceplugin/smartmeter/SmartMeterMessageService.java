/*
 SmartMeterMessageService.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.deviceconnect.android.deviceplugin.smartmeter.device.BP35C2;
import org.deviceconnect.android.deviceplugin.smartmeter.device.UsbSerialDevice;
import org.deviceconnect.android.deviceplugin.smartmeter.param.DongleConst;
import org.deviceconnect.android.deviceplugin.smartmeter.param.WiSunDevice;
import org.deviceconnect.android.deviceplugin.smartmeter.profiles.SmartMeterDeviceProfile;
import org.deviceconnect.android.deviceplugin.smartmeter.profiles.SmartMeterPowerMeterProfile;
import org.deviceconnect.android.deviceplugin.smartmeter.profiles.SmartMeterSystemProfile;
import org.deviceconnect.android.deviceplugin.smartmeter.util.ENLUtil;
import org.deviceconnect.android.deviceplugin.smartmeter.util.PrefUtil;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.deviceconnect.android.profile.DConnectProfile.setResult;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class SmartMeterMessageService extends DConnectMessageService {
    /** Tag. */
    private final static String TAG = "SMARTMETER_PLUGIN";
    /** デバッグフラグ. */
    private static final boolean DEBUG = BuildConfig.DEBUG;
    /** Dongleとの接続が完了した時にActivityを終了するフラグを格納するキー. */
    public static final String EXTRA_FINISH_FLAG = "flag";
    /** USBシリアルデバイス管理DBHelper. */
    private SmartMeterDBHelper mSmartMeterDBHelper;
    /** USB Port. */
    private static UsbSerialPort mSerialPort = null;
    /** USBのSerial IO Manager. */
    private static SerialInputOutputManager mSerialIoManager;
    /** Executor. */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    /** Status. */
    private static int mStatus;
    /** PrefUtil Instance. */
    private PrefUtil mPrefUtil;
    /** Wi-SUN Config. */
    WiSunDevice mWiSunDevice = new WiSunDevice();
    /** ECHONET Lite Utility Class. */
    ENLUtil mENLUtil = new ENLUtil();
    /** USBシリアルデバイスリスト. */
    ArrayList<UsbSerialDevice> mUsbSerialDevices = new ArrayList<>();
    /** ペアリング応答返信先Intent. */
    private Intent mPairingResponse;
    /** ペアリング先ServiceId. */
    private String mPairngServiceId;

    @Override
    public void onCreate() {
        super.onCreate();
//        if (DEBUG) {
//            android.os.Debug.waitForDebugger();
//        }

        mPrefUtil = new PrefUtil(this);
        mSmartMeterDBHelper = new SmartMeterDBHelper(getApplicationContext());

        // USBのEvent用のBroadcast Receiverを設定.
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(DongleConst.DEVICE_TO_DONGLE_OPEN_USB);
        mIntentFilter.addAction(DongleConst.DEVICE_TO_DONGLE_CHECK_USB);
        mIntentFilter.addAction(DongleConst.DEVICE_TO_DONGLE_CLOSE_USB);
        mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUSBEvent, mIntentFilter);
        /* DB保存USBシリアルデバイスリスト展開. */
        loadUSBSerialDeviceList();
        /* USBシリアルデバイス検索スレッド起動. */
        startSearchUSBSerialDeviceThread();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SmartMeterSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        // TODO Device Connect Managerアンインストール時に実行したい処理. 実装は任意.
        closeDevice();
        setStatus(DongleConst.STATUS_DONGLE_NOCONNECT);
    }

    @Override
    protected void onManagerTerminated() {
        // TODO Device Connect Manager停止時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // TODO アプリとのWebSocket接続が切断された時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onDevicePluginReset() {
        // TODO Device Connect Managerの設定画面上で「プラグイン再起動」を要求された場合の処理. 実装は任意.
        closeDevice();
        setStatus(DongleConst.STATUS_DONGLE_NOCONNECT);
        /* USBシリアルデバイス検索スレッド起動. */
        startSearchUSBSerialDeviceThread();
    }

    /**
     * Get ENLUtil instance.
     * @return ENLUtil Instance.
     */
    public ENLUtil getENLUtilInstance() {
        return mENLUtil;
    }

    /** SerialIOManager Instance for Search process. */
    SerialInputOutputManager mSearchSerialIoManager;

    /**
     * DBからUSBシリアルデバイスリスト取得しServiceへ展開.
     */
    private void loadUSBSerialDeviceList() {
        ArrayList<UsbSerialDevice> devices = mSmartMeterDBHelper.getDeviceList();
        if (devices != null && devices.size() != 0) {
            for (UsbSerialDevice device : devices) {
                addService(device);
            }
        }
    }

    /**
     * Service登録.
     * @param device USBシリアルデバイス情報.
     */
    private void addService(final UsbSerialDevice device) {
        DConnectService service = getServiceProvider().getService(device.getServiceId());
        if (service == null) {
            service = new DConnectService(device.getServiceId());
            service.setName("SmartMeterPlugin(" + device.getMacAddr() + ")");
            service.setOnline(false);
            service.setNetworkType(ServiceDiscoveryProfileConstants.NetworkType.UNKNOWN);
            service.addProfile(new SmartMeterDeviceProfile());
            service.addProfile(new SmartMeterPowerMeterProfile());
            getServiceProvider().addService(service);
        }
    }

    /**
     * USBシリアルデバイスリスト取得.
     * @return USBシリアルデバイスリスト
     */
    public ArrayList<UsbSerialDevice> getUsbSerialDevices() {
        return mUsbSerialDevices;
    }

    /**
     * USBシリアルデバイス検索開始.
     */
    public void startSearchUSBSerialDeviceThread() {
        // USB Manager 取得.
        final UsbManager mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        // 使用可能なUSB Portを取得.
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (drivers.size() == 0) {
            sendResult(DongleConst.CAN_NOT_FIND_USB);
            for (int n = 0; n < mUsbSerialDevices.size(); n++) {
                UsbSerialDevice closeDevice = mUsbSerialDevices.get(n);
                UsbSerialPort port = closeDevice.getSerialPort();
                if (port != null) {
                    SerialInputOutputManager manager = closeDevice.getSerialInputOutputManager();
                    if (manager != null) {
                        manager.stop();
                        closeDevice.setSerialInputOutputManager(null);
                    }
                    try {
                        port.close();
                        closeDevice.setSerialPort(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mUsbSerialDevices.set(n, closeDevice);
                }
            }
            mUsbSerialDevices.clear();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CountDownLatch latch = new CountDownLatch(drivers.size());
                    searchUSBSerialDeviceThread(latch);
                    try {
                        latch.await();
                        /* 自動接続処理 */
                        for (UsbSerialDevice device : mUsbSerialDevices) {
                            if (mSmartMeterDBHelper.hasDevice(device)) {
                                UsbSerialDevice dbDevice = mSmartMeterDBHelper.getDeviceByMacAddress(device.getMacAddr());
                                if (dbDevice.getOnline()) {
                                    connectSmartMeter(null, device);
                                    break;
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * USBシリアルデバイス検索スレッド.
     */
    public void searchUSBSerialDeviceThread(final CountDownLatch latch) {
        // USB Manager 取得.
        final UsbManager mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 使用可能なUSB Portを取得.
                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
                final List<UsbSerialPort> result = new ArrayList<>();

                if(drivers.size() != 0) {
                    //  発見したPortをResultに一時格納.
                    for (final UsbSerialDriver driver : drivers) {
                        final List<UsbSerialPort> ports = driver.getPorts();
                        result.addAll(ports);
                    }

                    for (int n = 0; n < result.size(); n++) {
                        final UsbSerialDevice device = new UsbSerialDevice();
                        device.setSerialPort(result.get(n));
                        final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                        // PortをOpen.
                        UsbDeviceConnection connection = mUsbManager.openDevice(device.getSerialPort().getDriver().getDevice());

                        if (connection == null) {
                            sendResult(DongleConst.FAILED_OPEN_USB);
                            return;
                        }

                        try {
                            // Dongle のSerialへ接続する.
                            device.getSerialPort().open(connection);
                            device.getSerialPort().setParameters(mWiSunDevice.getBaudrate(), 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                            sendResult(DongleConst.SUCCESS_CONNECT_DONGLE);
                            if (mSearchSerialIoManager != null) {
                                mSearchSerialIoManager.stop();
                                mSearchSerialIoManager = null;
                            }
                            mSearchSerialIoManager = new SerialInputOutputManager(device.getSerialPort(), new SerialInputOutputManager.Listener() {
                                @Override
                                public void onNewData(byte[] bytes) {
                                    boolean bFlag = false;
                                    String macAddr ="";
                                    for (byte aData : bytes) {
                                        byteBuffer.put(aData);
                                        if (bFlag && aData == '\n') {
                                            byteBuffer.flip();
                                            byte[] tmp = new byte[byteBuffer.limit()];
                                            byteBuffer.get(tmp);
                                            String strTmp = new String(tmp, 0, tmp.length);
                                            if (strTmp.contains(BP35C2.Command.INFO.getString())) {
                                                byteBuffer.clear();
                                                continue;
                                            } else if (strTmp.contains(BP35C2.Result.INFO.getString())) {
                                                String[] list = strTmp.split(" ", 0);
                                                macAddr = list[2];
                                                byteBuffer.clear();
                                            } else if (strTmp.contains(BP35C2.Result.OK.getString())) {
                                                byteBuffer.clear();
                                                if (mSearchSerialIoManager != null) {
                                                    mSearchSerialIoManager.stop();
                                                    mSearchSerialIoManager = null;
                                                }
                                                if (!(macAddr.equals(""))) {
                                                    boolean bMatchMacAddr = false;
                                                    for (UsbSerialDevice device : mUsbSerialDevices) {
                                                        String addr = device.getMacAddr();
                                                        if (addr.contains(macAddr)) {
                                                            bMatchMacAddr = true;
                                                            break;
                                                        }
                                                    }
                                                    if (!bMatchMacAddr) {
                                                        device.setServiceId("smartmeter-" + macAddr);
                                                        device.setName("USBSerialDevice(" + macAddr + ")");
                                                        device.setDeviceType("Serial");
                                                        device.setStatus("false");
                                                        device.setOnline(false);
                                                        device.setMacAddr(macAddr);
                                                        device.setSerialInputOutputManager(null);
                                                        mUsbSerialDevices.add(device);
                                                        if (!mSmartMeterDBHelper.hasDevice(device)) {
                                                            mSmartMeterDBHelper.addDevice(device);
                                                            addService(device);
                                                        }
                                                        macAddr = "";
                                                    }
                                                }
                                                latch.countDown();
                                            }
                                            bFlag = false;
                                        } else if (!bFlag && aData == '\r'){
                                            bFlag = true;
                                        }
                                    }
                                }

                                @Override
                                public void onRunError(Exception e) {
                                    e.printStackTrace();

                                }
                            });
                            mExecutor.submit(mSearchSerialIoManager);
                            try{
                                String command = BP35C2.Command.INFO.getString() + BP35C2.CRLF;
                                device.getSerialPort().write(command.getBytes(), 10000);
                            } catch(Exception e){
                                onDeviceStateChange();
                            }
                        } catch (IOException e) {
                            sendResult(DongleConst.FAILED_CONNECT_DONGLE);
                            try {
                                device.getSerialPort().close();
                            } catch (IOException e2) {
                                // Ignore.
                            }
                            return;
                        }
                    }
                }

                if (DEBUG) {
                    Log.i(TAG, "mUsbSerialDevices size = " + mUsbSerialDevices.size());
                    for (UsbSerialDevice device : mUsbSerialDevices) {
                        Log.i(TAG, " Service ID = " + device.getServiceId());
                        Log.i(TAG, " MAC Addr   = " + device.getMacAddr());
                        Log.i(TAG, " SerialPort = " + device.getSerialPort());
                        Log.i(TAG, " ");
                    }
                }
            }
        }).start();
    }

    /**
     * USBシリアルデバイスリストが空かチェックする.
     * @return true : 空 , false : デバイスあり.
     */
    public boolean isEmptyUsbSerialDevice() {
        return mUsbSerialDevices.size() == 0;
    }

    /**
     * serviceIdからUSBシリアルデバイスを取得する.
     * @param serviceId Service ID.
     * @return UsbSerialDevice または null(該当無し).
     */
    public UsbSerialDevice getUsbSerialDevice(final String serviceId) {
        // パラメータ、USB serial device list 数を確認.
        if (serviceId.isEmpty() || mUsbSerialDevices.size() == 0) {
            return null;
        }

        // service ID によるdevice検索.
        for (UsbSerialDevice device : mUsbSerialDevices) {
            if (serviceId.contains(device.getServiceId())) {
                return device;
            }
        }
        // 該当無し.
        return null;
    }

    /**
     * serialPortからServiceIDを取得する.
     * @param serialPort SerialPort.
     * @return serviceId または null(該当無し).
     */
    public String getServiceIdForSerialPort(final UsbSerialPort serialPort) {
        // パラメータ、USB serial device list 数を確認.
        if (serialPort == null || mUsbSerialDevices.size() == 0) {
            return null;
        }

        // serialPort によるdevice検索.
        for (UsbSerialDevice device : mUsbSerialDevices) {
            if (serialPort.equals(device.getSerialPort())) {
                return device.getServiceId();
            }
        }
        // 該当無し.
        return null;
    }

    /**
     * USBシリアルデバイスリスト更新.
     * @param device 更新するUSBシリアルデバイス情報.
     * @return true : 更新成功 / false : 更新失敗.
     */
    private boolean updateUsbSerialDeviceTable(final UsbSerialDevice device) {
        boolean bMatchDevice = false;
        for (int n = 0; n < mUsbSerialDevices.size(); n++) {
            UsbSerialDevice matchDevice = mUsbSerialDevices.get(n);
            if (matchDevice.getServiceId().contains(device.getServiceId())) {
                mUsbSerialDevices.set(n, device);
                bMatchDevice = true;
                break;
            }
        }
        return bMatchDevice;
    }

    /**
     * スマートメーター通信接続.
     * @param response response.
     * @param device 接続USBシリアルデバイス.
     */
    public void connectSmartMeter(final Intent response, final UsbSerialDevice device) {
        /* ID、パスワードチェック */
        if (mPrefUtil.getBRouteId() == null || mPrefUtil.getBRoutePass() == null) {
            if (response != null) {
                MessageUtils.setIllegalDeviceStateError(response, "Not found b-route ID or Password.");
                sendResponse(response);
            }
            return;
        }

        UsbSerialPort serialPort = device.getSerialPort();
        SerialInputOutputManager manager = device.getSerialInputOutputManager();
        if (manager != null) {
            manager.stop();
            device.setSerialInputOutputManager(null);
        }

        if (mSerialPort == null) {
            mSerialPort = device.getSerialPort();
        } else if (!(mSerialPort.equals(serialPort))) {
            try {
                mSerialPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort = device.getSerialPort();
        }

        mByteBuffer.clear();
        device.setSerialInputOutputManager(new SerialInputOutputManager(mSerialPort, mListener));
        updateUsbSerialDeviceTable(device);
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
        mSerialIoManager = device.getSerialInputOutputManager();
        mExecutor.submit(mSerialIoManager);

        // 初期コマンド送信.
        mPairingResponse = response;
        mPairngServiceId = device.getServiceId();
        mSequenceState = 0;
        ExecuteProcess(BP35C2.Command.ECHOBACK_OFF, null);
    }

    /**
     * スマートメーター通信切断.
     * @param response response.
     * @param device 切断USBシリアルデバイス.
     */
    public void disconnectSmartMeter(final Intent response, final UsbSerialDevice device) {
        /* ID、パスワードチェック */
        if (mPrefUtil.getBRouteId() == null || mPrefUtil.getBRoutePass() == null) {
            if (response != null) {
                MessageUtils.setIllegalDeviceStateError(response, "Not found b-route ID or Password.");
                sendResponse(response);
            }
            return;
        }

        UsbSerialPort serialPort = device.getSerialPort();
        if (mSerialPort == null) {
            mSerialPort = device.getSerialPort();
        } else if (!(mSerialPort.equals(serialPort))) {
            try {
                mSerialPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort = device.getSerialPort();
        }

        // 切断コマンド送信.
        mPairingResponse = response;
        mPairngServiceId = device.getServiceId();
        ExecuteProcess(BP35C2.Command.TERMINATE, null);
    }

    /**
     * USBシリアルデバイスクローズ.
     */
    private void closeDevice() {
        Iterator<UsbSerialDevice> index = mUsbSerialDevices.iterator();

        while (index.hasNext()) {
            UsbSerialDevice device = index.next();
            final UsbManager mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection connection = mUsbManager.openDevice(device.getSerialPort().getDriver().getDevice());
            if (connection == null) {
                SerialInputOutputManager manager = device.getSerialInputOutputManager();
                if (manager != null) {
                    manager.stop();
                }
                device.setSerialInputOutputManager(null);

                UsbSerialPort serialPort = device.getSerialPort();
                if (serialPort != null) {
                    try {
                        serialPort.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                device.setSerialPort(null);
                setOnlineStatus(device.getServiceId(), false, false);
                index.remove();
            }
        }
    }

    /**
     * 動作ステータス更新.
     * @param status status.
     */
    public void setStatus(int status) {
        mStatus = status;
        if (DEBUG) {
            Log.i(TAG, "status:" + status);
        }
    }

    /**
     * USBイベント関連のbroadcast receiver.
     */
    private BroadcastReceiver mUSBEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DongleConst.DEVICE_TO_DONGLE_OPEN_USB:
                    startSearchUSBSerialDeviceThread();
                    break;
                case DongleConst.DEVICE_TO_DONGLE_CHECK_USB:
                    sendStatus(mStatus);
                    break;
                case DongleConst.DEVICE_TO_DONGLE_CLOSE_USB:
                case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    closeDevice();
                    setStatus(DongleConst.STATUS_DONGLE_NOCONNECT);
                    break;
            }
        }
    };

    /**
     * Dongle未検出時のTimeout処理.
     */
    private final Runnable checkStatus = new Runnable() {
        @Override
        public void run() {
            if(mStatus == DongleConst.STATUS_DONGLE_INIT){
                sendResult(DongleConst.FAILED_CONNECT_DONGLE);
                setStatus(DongleConst.STATUS_DONGLE_NOCONNECT);
            }
        }
    };

    /**
     * シリアル通信を開始.
     */
    private void onDeviceStateChange() {

        // Init.
        stopIoManager();
        startIoManager();

        // 3秒経過後もDongleを検出できない場合はエラー.
        new Handler().postDelayed(checkStatus, 3000);

        // Statusをinitへ.
        setStatus(DongleConst.STATUS_DONGLE_INIT);

        if (mWiSunDevice.getDeviceType().equals(BP35C2.DEVICE_NAME)) {
            ExecuteProcess(BP35C2.Command.ECHOBACK_OFF, null);
        }
    }

    /**
     * シリアル通信をストップする.
     */
    private void stopIoManager() {
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    /**
     * シリアル通信を開始する.
     */
    private void startIoManager() {
        if (mSerialPort != null) {
            mByteBuffer.clear();
            mSerialIoManager = new SerialInputOutputManager(mSerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    /** 受信データ保存バッファ. */
    ByteBuffer mByteBuffer = ByteBuffer.allocate(1024);
    /** データデリミッタ(CRのみ)制御. */
    boolean mDelimiterCrOnly = false;

    /**
     * Dongle側から返答のあるメッセージを受信するListener.
     */
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                }

                @Override
                public void onNewData(final byte[] data) {
                    boolean bFlag = false;
                    for (byte aData : data) {
                        mByteBuffer.put(aData);
                        if (bFlag && aData == '\n') {
                            callResultProcess();
                            bFlag = false;
                        } else if (!bFlag && aData == '\r'){
                            if (mDelimiterCrOnly) {
                                mDelimiterCrOnly = false;
                                callResultProcess();
                            } else {
                                bFlag = true;
                            }
                        }
                    }
                }
            };

    /**
     * リザルト処理.
     */
    public void callResultProcess() {
        mByteBuffer.flip();
        byte[] tmp = new byte[mByteBuffer.limit()];
        mByteBuffer.get(tmp);
        if (DEBUG) {
            Log.i(TAG, "  length = " + tmp.length);
            Log.i(TAG, "  data = " + new String(tmp, 0, tmp.length));
        }
        ResultProcess(tmp);
        mByteBuffer.clear();
    }

    /**
     * オンラインステータス設定.
     * @param serviceId Service ID.
     * @param status status.
     * @param isDbUpdate DB更新判定フラグ.
     */
    public void setOnlineStatus(final String serviceId, final boolean status, final boolean isDbUpdate) {
        DConnectService service = getServiceProvider().getService(serviceId);
        if (service != null) {
            service.setOnline(status);
            UsbSerialDevice device = mSmartMeterDBHelper.getDeviceByServiceId(serviceId);
            if (device != null) {
                device.setOnline(status);
                if (isDbUpdate) {
                    mSmartMeterDBHelper.updateDevice(device);
                }
            }
        }
    }

    /**
     * オンラインステータス取得.
     * @param serviceId Service Id.
     * @return true : Online / false : Offline.
     */
    public boolean getOnlineStatus(final String serviceId) {
        DConnectService service = getServiceProvider().getService(serviceId);
        return service != null && service.isOnline();
    }

    /**
     * USB Serialへのメッセージの送信
     * @param msg String型のメッセージ
     */
    public void SendMessage(String msg) {

        if (mSerialIoManager != null) {

            try{
                mSerialPort.write(msg.getBytes(), 10000);
            } catch(Exception e){
                e.printStackTrace();
                onDeviceStateChange();
            }

        }  else {
            setStatus(DongleConst.STATUS_DONGLE_NOCONNECT);
        }
    }

    /**
     * メッセージの送信
     * @param mByte Byte型のメッセージ
     */
    public void SendMessage(byte[] mByte) {
        if(mSerialIoManager != null) {
            try {
                mSerialPort.write(mByte, 1000);
            } catch (Exception e){
                e.printStackTrace();
                onDeviceStateChange();
            }
        } else {
            setStatus(DongleConst.STATUS_DONGLE_NOCONNECT);
        }
    }

    /**
     * Activityにメッセージを返信する.
     * @param resultId 結果のID.
     */
    private void sendResult(int resultId){
        Intent intent = new Intent(DongleConst.DEVICE_TO_DONGLE_OPEN_USB_RESULT);
        intent.putExtra("resultId", resultId);
        intent.putExtra(EXTRA_FINISH_FLAG, true);
        sendBroadcast(intent);
    }

    /**
     * Activityにステータス状態を返信する.
     * @param statusId 結果のID.
     */
    private void sendStatus(int statusId){
        Intent intent = new Intent(DongleConst.DEVICE_TO_DONGLE_CHECK_USB_RESULT);
        intent.putExtra("statusId", statusId);
        intent.putExtra(EXTRA_FINISH_FLAG, true);
        sendBroadcast(intent);
    }

    /** 実行中コマンド. */
    private BP35C2.Command mExecCommand;
    /** シーケンス状態. */
    private int mSequenceState = 0;
    /** Wi-SUNスマートメーターデバイスリスト. */
    private List<WiSunDevice> mDevices = new ArrayList<>();
    /** Wi-SUNデバイス. */
    private WiSunDevice mTmpDevice;
    /** デバイスインデックス. */
    private int mDeviceIndex = 0;

    /**
     * コマンド実行処理.
     * @param command 実行コマンド.
     * @param param コマンドパラメータ.
     */
    public void ExecuteProcess(final BP35C2.Command command, final String param) {
        String executeCommand = command.getString();
        if (param != null) {
            executeCommand += param;
        }
        executeCommand += BP35C2.CRLF;

        if (DEBUG) {
            Log.i(TAG, "ExecuteProcess: command = " + executeCommand);
        }
        mExecCommand = command;
        SendMessage(executeCommand);
    }

    /**
     * 応答処理解析.
     * @param data 応答データ.
     */
    public void ResultProcess(final byte[] data) {
        if (mExecCommand != null) {
            String ok = BP35C2.Result.OK.getString();
            switch (mExecCommand) {
                case ECHOBACK_OFF:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_ECHOBACK_OFF");
                    }
                    String matchCmd = BP35C2.Command.ECHOBACK_OFF.getString();
                    if (data.length > matchCmd.length()) {
                        if (new String(data, 0, matchCmd.length()).equals(matchCmd)) {
                            return;
                        }
                    }

                    if (data.length >= ok.length()) {
                        if (new String(data, 0, ok.length()).equals(ok)) {
                            if (mStatus == DongleConst.STATUS_DONGLE_INIT) {
                                setStatus(DongleConst.STATUS_DONGLE_RUNNING);
                            }
                            ExecuteProcess(BP35C2.Command.ROPT, null);
                            mDelimiterCrOnly = true;
                        }
                    }
                    break;
                case ROPT:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_ROPT");
                    }
                    if (new String(data, 0, ok.length()).equals(ok)) {
                        String opt = new String(data, 3, 2);
                        if (DEBUG) {
                            Log.i(TAG, "  ROPT : " +  opt);
                        }
                        if (opt.equals("00")) {
                            mDelimiterCrOnly = true;
                            ExecuteProcess(BP35C2.Command.WOPT, BP35C2.DISP_BINARY);
                        } else {
                            ExecuteProcess(BP35C2.Command.DEVICE_SCAN, null);
                        }
                    }
                    break;
                case WOPT:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_WOPT");
                    }
                    if (new String(data, 0, ok.length()).equals(ok)) {
                        ExecuteProcess(BP35C2.Command.DEVICE_SCAN, null);
                    }
                    break;
                case DEVICE_SCAN:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_DEVICE_SCAN");
                    }
                    if (new String(data, 0, ok.length()).equals(ok)) {
                        mDevices.clear();
                    } else if (new String(data, 0, data.length).contains(BP35C2.Result.EVENT_20.getString())) {
                        mTmpDevice = new WiSunDevice();
                    } else if (new String(data, 0, data.length).contains("Channel:")) {
                        String strTmp = new String(data);
                        String strChannel = strTmp.substring(strTmp.indexOf(":") + 1).replaceAll(BP35C2.CRLF, "");
                        Integer channel = Integer.decode("0x" + strChannel);
                        mTmpDevice.setChannel(channel);
                    } else if (new String(data, 0, data.length).contains("Pan ID:")) {
                        String strTmp = new String(data);
                        mTmpDevice.setPanId(strTmp.substring(strTmp.indexOf(":") + 1).replaceAll(BP35C2.CRLF, ""));
                    } else if (new String(data, 0, data.length).contains("Addr:")) {
                        String strTmp = new String(data);
                        mTmpDevice.setMacAddress(strTmp.substring(strTmp.indexOf(":") + 1).replaceAll(BP35C2.CRLF, ""));
                        mDevices.add(mTmpDevice);
                    } else if (new String(data, 0, data.length).contains(BP35C2.Result.EVENT_22.getString())) {
                        if (mDevices.size() == 0) {
                            ExecuteProcess(BP35C2.Command.SET_BID, mWiSunDevice.getbRouteId());
                        } else {
                            mDeviceIndex = 0;
                            ExecuteProcess(BP35C2.Command.SKLL64, mDevices.get(0).getMacAddress());
                        }
                    }
                    break;
                case SKLL64:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_SKLL64");
                    }
                    String result = new String(data);
                    if (!result.contains(BP35C2.Result.FAIL.getString())) {
                        WiSunDevice config = mDevices.get(mDeviceIndex);
                        config.setIpv6Address(result.replaceAll(BP35C2.CRLF, ""));
                        mDevices.set(mDeviceIndex, config);
                        mDeviceIndex++;
                        if (mDevices.size() >= mDeviceIndex) {
                            // Next Process
//                        ExecuteProcess(BP35C2.Command.SET_BID, mWiSunDevice.getbRouteId());
                            ExecuteProcess(BP35C2.Command.SET_BID, mPrefUtil.getBRouteId());
                        } else {
                            ExecuteProcess(BP35C2.Command.SKLL64, mDevices.get(mDeviceIndex).getMacAddress());
                        }
                    } else {
                        mDeviceIndex++;
                        if (mDevices.size() >= mDeviceIndex) {
                            // Next Process
//                        ExecuteProcess(BP35C2.Command.SET_BID, mWiSunDevice.getbRouteId());
                            ExecuteProcess(BP35C2.Command.SET_BID, mPrefUtil.getBRouteId());
                        } else {
                            ExecuteProcess(BP35C2.Command.SKLL64, mDevices.get(mDeviceIndex).getMacAddress());
                        }
                    }
                    if (DEBUG) {
                        if (mExecCommand == BP35C2.Command.SET_BID) {
                            Log.i(TAG, "mDevices.size() = " + mDevices.size());
                            for (WiSunDevice config : mDevices) {
                                Log.i(TAG, "  -------  ");
                                Log.i(TAG, "  Channel: " + config.getChannel());
                                Log.i(TAG, "  PAN ID : " + config.getPanId());
                                Log.i(TAG, "  MAC    : " + config.getMacAddress());
                                Log.i(TAG, "  ADDR   : " + config.getIpv6Address());
                            }
                            Log.i(TAG, "  -------  ");
                        }
                        mWiSunDevice = mDevices.get(0);
                    }
                    break;
                case SET_BID:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_SET_BID");
                    }
                    if (new String(data, 0, ok.length()).equals(ok)) {
//                    ExecuteProcess(BP35C2.Command.SET_BPWD, String.format("%x", mWiSunDevice.getbRoutePassword().length()) + " " + mWiSunDevice.getbRoutePassword());
                        ExecuteProcess(BP35C2.Command.SET_BPWD, String.format("%x", mPrefUtil.getBRoutePass().length()) + " " + mPrefUtil.getBRoutePass());
                        mDeviceIndex = 0;
                    }
                    break;
                case SET_BPWD:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_SET_BPWD");
                    }
                    if (new String(data, 0, ok.length()).equals(ok)) {
                        mWiSunDevice = mDevices.get(mDeviceIndex++);
                        ExecuteProcess(BP35C2.Command.SET_CHANNEL, String.format("%x", mWiSunDevice.getChannel()));
                    }
                    break;
                case SET_CHANNEL:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_SET_CHANNEL");
                    }
                    if (new String(data, 0, ok.length()).equals(ok)) {
                        ExecuteProcess(BP35C2.Command.SET_PAN_ID, mWiSunDevice.getPanId());
                    }
                    break;
                case SET_PAN_ID:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_SET_PAN_ID");
                    }
                    if (new String(data, 0, ok.length()).equals(ok)) {
                        ExecuteProcess(BP35C2.Command.JOIN, mWiSunDevice.getIpv6Address());
                    }
                    break;
                case JOIN:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_JOIN");
                    }

                    if (!(new String(data, 0, ok.length()).equals(ok))) {
                        if (mPairngServiceId != null) {
                            setOnlineStatus(mPairngServiceId, false, true);
                        }
                        if (mPairingResponse != null) {
                            MessageUtils.setIllegalDeviceStateError(mPairingResponse, "Failed open session.");
                            sendResponse(mPairingResponse);
                        }
                        mPairngServiceId = null;
                        mPairingResponse = null;
                    }

                    mExecCommand = BP35C2.Command.NONE;
                    break;
                case TERMINATE:
                    if (DEBUG) {
                        Log.i(TAG, "Result: CMD_TERMINATE");
                    }
                    if (new String(data, 0, data.length).contains(BP35C2.Result.EVENT_21.getString())) {
                        return;
                    }
                    if (mPairingResponse != null) {
                        if (new String(data, 0, ok.length()).equals(ok)) {
                            setResult(mPairingResponse, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setIllegalDeviceStateError(mPairingResponse, "Already close session.");
                        }
                        sendResponse(mPairingResponse);
                    }
                    if (mPairngServiceId != null) {
                        setOnlineStatus(mPairngServiceId, false, true);
                    }
                    mPairngServiceId = null;
                    mPairingResponse = null;
                    mSequenceState = 0;
                    mExecCommand = BP35C2.Command.NONE;
                    break;
                default:
                    byte[] cmd;
                    if (new String(data, 0, data.length).contains(BP35C2.Result.EVENT_24.getString()) && mSequenceState == 0) {
                        if (mDeviceIndex >= mDevices.size()) {
                            // 接続失敗。再検索.
                            ExecuteProcess(BP35C2.Command.DEVICE_SCAN, null);
                        } else {
                            mWiSunDevice = mDevices.get(mDeviceIndex++);
                            ExecuteProcess(BP35C2.Command.SET_CHANNEL, String.format("%x", mWiSunDevice.getChannel()));
                        }
                    } else if (new String(data, 0, data.length).contains(BP35C2.Result.EVENT_25.getString()) && mSequenceState == 0) {
                        if (DEBUG) {
                            Log.i(TAG, "Result: CMD_RESULT_EVENT_25");
                        }
                        byte[] enlCmd = mENLUtil.makeEchonetLitePacket("INIT1", null);
                        String command = BP35C2.Command.SEND_TO.getString() + "1 " + mWiSunDevice.getIpv6Address() + " 0E1A 1 0 " + String.format("%04x", enlCmd.length) + " ";
                        cmd = command.getBytes();
                        ByteBuffer sendCmd = ByteBuffer.allocate(cmd.length + enlCmd.length);
                        sendCmd.put(cmd);
                        sendCmd.put(enlCmd);
                        mExecCommand = BP35C2.Command.SEND_TO;
                        SendMessage(sendCmd.array());
                        mSequenceState = 1;
                    } else if (mExecCommand == BP35C2.Command.SEND_TO) {
                        if (DEBUG) {
                            Log.i(TAG, "Result: CMD_SEND_TO");
                        }
                        if (new String(data, 0, data.length).contains(BP35C2.Result.RCV_UDP.getString())) {

                            byte[] enlData = mENLUtil.convertHex2Bin(data);

                            mENLUtil.analysisEchonetLitePacket(enlData);
                            if (mSequenceState == 1) {
                                byte[] enlCmd = mENLUtil.makeEchonetLitePacket("INIT2", null);
                                String command = BP35C2.Command.SEND_TO.getString() + "1 " + mWiSunDevice.getIpv6Address() + " 0E1A 1 0 " + String.format("%04x", enlCmd.length) + " ";
                                cmd = command.getBytes();
                                ByteBuffer sendCmd = ByteBuffer.allocate(cmd.length + enlCmd.length);
                                sendCmd.put(cmd);
                                sendCmd.put(enlCmd);
                                mExecCommand = BP35C2.Command.SEND_TO;
                                SendMessage(sendCmd.array());
                                mSequenceState = 2;

                                if (mPairngServiceId != null) {
                                    setOnlineStatus(mPairngServiceId, true, true);
                                }
                                if (mPairingResponse != null) {
                                    setResult(mPairingResponse, DConnectMessage.RESULT_OK);
                                    sendResponse(mPairingResponse);
                                }
                                mPairngServiceId = null;
                                mPairingResponse = null;
                            } else if (sequenceGetInstantaneousPowerProcess != 0) {
                                getInstantaneousPowerProcess(data);
                            } else if (sequenceGetInstantaneousCurrentProcess != 0) {
                                getInstantaneousCurrentProcess(data);
                            } else if (sequenceGetDayDataProcess != 0) {
                                getDayDataProcess(data);
                            } else if (sequenceGetOperationStatusProcess != 0) {
                                getOperationStatusProcess(data);
                            }
                        }
                    }
                    break;
            }
        }
    }

    /** 動作状態取得シーケンス管理用. */
    private int sequenceGetOperationStatusProcess = 0;
    /** 動作状態応答用Intent. */
    private Intent mGetOperationStatusResponse;

    /**
     * 動作状態取得.
     * @param response レスポンス設定用Intent.
     */
    public void getOperationStatus(final Intent response) {
        mGetOperationStatusResponse = response;
        sequenceGetOperationStatusProcess = 1;
        getOperationStatusProcess(null);
    }

    /**
     * 動作状態取得処理.
     * @param data シリアル受信データ.
     */
    public void getOperationStatusProcess(final byte[] data) {
        byte[] enlData;
        int esv;
        byte[] enlCmd;
        String command;
        byte[] cmd;
        ByteBuffer sendCmd;

        switch (sequenceGetOperationStatusProcess) {
            case 1:
                // 動作状態取得.
                enlCmd = mENLUtil.makeEchonetLitePacket("GET_80", null);
                command = BP35C2.Command.SEND_TO.getString() + "1 " + mWiSunDevice.getIpv6Address() + " 0E1A 1 0 " + String.format("%04x", enlCmd.length) + " ";
                cmd = command.getBytes();
                sendCmd = ByteBuffer.allocate(cmd.length + enlCmd.length);
                sendCmd.put(cmd);
                sendCmd.put(enlCmd);
                mExecCommand = BP35C2.Command.SEND_TO;
                SendMessage(sendCmd.array());
                sequenceGetOperationStatusProcess++;
                break;
            case 2:
                int operationStatus;
                enlData = mENLUtil.convertHex2Bin(data);
                esv = mENLUtil.checkEsv(enlData);
                if (esv == ENLUtil.ESV_GET_RES) {
                    // 正常応答.
                    ENLUtil.ResultData[] rd = mENLUtil.splitResultData(enlData);
                    for (ENLUtil.ResultData resultData : rd) {
                        if (resultData.mEpc == 0x80 && resultData.mPdc == 1) {
                            // 動作状態保存.
                            operationStatus = (resultData.mEdt[0] & 0xFF);
                        } else {
                            operationStatus = 0xFF;
                        }
                        resultOperationStatus(operationStatus);
                    }
                } else if (esv == ENLUtil.ESV_GET_SNA) {
                    // 不可応答.
                    operationStatus = 0xFF;
                    resultOperationStatus(operationStatus);
                } else {
                    break;
                }
                break;
        }
    }

    /**
     * 動作状態を要求元へ返却する.
     * @param operationStatus 動作状態.
     */
    public void resultOperationStatus(final int operationStatus) {
        String strOperationStatus;
        setResult(mGetOperationStatusResponse, DConnectMessage.RESULT_OK);
        Bundle root = mGetOperationStatusResponse.getExtras();
        switch (operationStatus) {
            case 0x31:
                strOperationStatus = "OFF";
                break;
            case 0x30:
                strOperationStatus = "ON";
                break;
            default:
                strOperationStatus = "UNKNOWN";
                break;
        }

        // 動作状態データ設定.
        root.putString("powerstatus", strOperationStatus);
        mGetOperationStatusResponse.putExtras(root);
        sendResponse(mGetOperationStatusResponse);
        sequenceGetOperationStatusProcess = 0;
    }

    /** 瞬時電力量取得シーケンス管理用. */
    private int sequenceGetInstantaneousPowerProcess = 0;
    /** 変換単位保存用. */
    private String mGetInstantaneousPowerUnit = null;
    /** 瞬時電流量応答用Intent. */
    private Intent mInstantaneousPowerResponse;

    /** オーバーフロー判定値(32bit). */
    final int DEF_INT_OVERFLOW = 0x7FFFFFFF;
    /** アンダーフロー判定値(32bit). */
    final int DEF_INT_UNDERFLOW = 0x80000000;
    /** 未計測判定値(32bit). */
    final int DEF_INT_NO_DATA = 0x7FFFFFFE;

    /**
     * 瞬時電力量取得.
     * @param unit 電力量単位.
     * @param response レスポンス設定用Intent.
     */
    public void getInstantaneousPower(final String unit, final Intent response) {
        mGetInstantaneousPowerUnit = unit;
        mInstantaneousPowerResponse = response;
        sequenceGetInstantaneousPowerProcess = 1;
        getInstantaneousPowerProcess(null);
    }

    /**
     * 瞬時電力量取得処理.
     * @param data シリアル受信データ.
     */
    public void getInstantaneousPowerProcess(final byte[] data) {
        byte[] enlData;
        int esv;
        byte[] enlCmd;
        String command;
        byte[] cmd;
        ByteBuffer sendCmd;

        switch (sequenceGetInstantaneousPowerProcess) {
            case 1:
                // 瞬時電力計測値取得.
                enlCmd = mENLUtil.makeEchonetLitePacket("GET_E7", null);
                command = BP35C2.Command.SEND_TO.getString() + "1 " + mWiSunDevice.getIpv6Address() + " 0E1A 1 0 " + String.format("%04x", enlCmd.length) + " ";
                cmd = command.getBytes();
                sendCmd = ByteBuffer.allocate(cmd.length + enlCmd.length);
                sendCmd.put(cmd);
                sendCmd.put(enlCmd);
                mExecCommand = BP35C2.Command.SEND_TO;
                SendMessage(sendCmd.array());
                sequenceGetInstantaneousPowerProcess++;
                break;
            case 2:
                long instantaneousPower;
                enlData = mENLUtil.convertHex2Bin(data);
                esv = mENLUtil.checkEsv(enlData);
                if (esv == ENLUtil.ESV_GET_RES) {
                    // 正常応答.
                    ENLUtil.ResultData[] rd = mENLUtil.splitResultData(enlData);
                    for (ENLUtil.ResultData resultData : rd) {
                        if (resultData.mEpc == 0xE7 && resultData.mPdc == 4) {
                            // 瞬時電力量保存.
                            byte[] byTmp = new byte[4];
                            System.arraycopy(resultData.mEdt, 0, byTmp, 0, 4);

                            int tmp = ByteBuffer.wrap(byTmp).asIntBuffer().get();
                            if (tmp == DEF_INT_NO_DATA) {
                                instantaneousPower = DEF_INT_NO_DATA;
                            } else if (tmp == DEF_INT_UNDERFLOW) {
                                instantaneousPower = DEF_INT_UNDERFLOW;
                            } else if (tmp == DEF_INT_OVERFLOW) {
                                instantaneousPower = DEF_INT_OVERFLOW;
                            } else {
                                instantaneousPower = (long)(tmp);
                            }
                        } else {
                            instantaneousPower = DEF_INT_NO_DATA;
                        }
                        resultInstantaneousPower(instantaneousPower);
                    }
                } else if (esv == ENLUtil.ESV_GET_SNA) {
                    // 不可応答.
                    instantaneousPower = DEF_INT_NO_DATA;
                    resultInstantaneousPower(instantaneousPower);
                } else {
                    break;
                }
                break;
        }
    }

    /**
     * 指定された単位変換をして瞬時電力量を要求元へ返却する.
     * @param instantaneousPower 瞬時電力量.
     */
    public void resultInstantaneousPower(final long instantaneousPower) {
        setResult(mInstantaneousPowerResponse, DConnectMessage.RESULT_OK);
        Bundle root = mInstantaneousPowerResponse.getExtras();
        double power;

        if (instantaneousPower == DEF_INT_UNDERFLOW || instantaneousPower == DEF_INT_OVERFLOW || instantaneousPower == DEF_INT_NO_DATA) {
            power = instantaneousPower;
        } else {
            // 単位変換.
            if (mGetInstantaneousPowerUnit == null) {
                mGetInstantaneousPowerUnit = "W";
            }

            if (mGetInstantaneousPowerUnit.contains("kW")) {
                power = instantaneousPower * 0.001;
            } else {
                power = instantaneousPower;
            }
        }

        // 瞬時電力量データ設定.
        root.putDouble("instantaneouspower", power);
        root.putString("unit", mGetInstantaneousPowerUnit);
        mInstantaneousPowerResponse.putExtras(root);
        sendResponse(mInstantaneousPowerResponse);
        sequenceGetInstantaneousPowerProcess = 0;
    }

    /** 瞬時電流量取得シーケンス管理用. */
    private int sequenceGetInstantaneousCurrentProcess = 0;
    /** 変換単位保存用. */
    private String mGetInstantaneousCurrentUnit = null;
    /** 瞬時電流量応答用Intent. */
    private Intent mInstantaneousCurrentResponse;

    /** オーバーフロー判定値(16bit). */
    final int DEF_SHORT_OVERFLOW = 0x7FFF;
    /** アンダーフロー判定値(16bit). */
    final int DEF_SHORT_UNDERFLOW = 0x8000;
    /** 未計測判定値(16bit). */
    final int DEF_SHORT_NO_DATA = 0x7FFE;

    /**
     * 瞬時電流量取得.
     * @param unit 電流量単位.
     * @param response レスポンス設定用Intent.
     */
    public void getInstantaneousCurrent(final String unit, final Intent response) {
        mGetInstantaneousCurrentUnit = unit;
        mInstantaneousCurrentResponse = response;
        sequenceGetInstantaneousCurrentProcess = 1;
        getInstantaneousCurrentProcess(null);
    }

    /**
     * 瞬時電流量取得処理.
     * @param data シリアル受信データ.
     */
    public void getInstantaneousCurrentProcess(final byte[] data) {
        byte[] enlData;
        int esv;
        byte[] enlCmd;
        String command;
        byte[] cmd;
        ByteBuffer sendCmd;
        float effectiveRPhase;
        float effectiveTPhase;

        switch (sequenceGetInstantaneousCurrentProcess) {
            case 1:
                // 瞬時電流計測値取得.
                enlCmd = mENLUtil.makeEchonetLitePacket("GET_E8", null);
                command = BP35C2.Command.SEND_TO.getString() + "1 " + mWiSunDevice.getIpv6Address() + " 0E1A 1 0 " + String.format("%04x", enlCmd.length) + " ";
                cmd = command.getBytes();
                sendCmd = ByteBuffer.allocate(cmd.length + enlCmd.length);
                sendCmd.put(cmd);
                sendCmd.put(enlCmd);
                mExecCommand = BP35C2.Command.SEND_TO;
                SendMessage(sendCmd.array());
                sequenceGetInstantaneousCurrentProcess++;
                break;
            case 2:
                enlData = mENLUtil.convertHex2Bin(data);
                esv = mENLUtil.checkEsv(enlData);
                if (esv == ENLUtil.ESV_GET_RES) {
                    // 正常応答.
                    ENLUtil.ResultData[] rd = mENLUtil.splitResultData(enlData);
                    for (ENLUtil.ResultData resultData : rd) {
                        if (resultData.mEpc == 0xE8 && resultData.mPdc == 4) {
                            float unit;
                            // 単位変換.
                            if (mGetInstantaneousCurrentUnit == null) {
                                mGetInstantaneousCurrentUnit = "A";
                            }

                            if (mGetInstantaneousCurrentUnit.contains("mA")) {
                                unit = 1000;
                            } else {
                                unit = 1;
                            }

                            // 瞬時電力量保存.
                            int rPhase;
                            byte[] tmp = new byte[4];
                            if ((resultData.mEdt[0] & 0x80) == 0x80) {
                                tmp[0] = tmp[1] = (byte)0xFF;
                            } else {
                                tmp[0] = tmp[1] = (byte)0x00;
                            }
                            tmp[2] = resultData.mEdt[0];
                            tmp[3] = resultData.mEdt[1];
                            rPhase = ByteBuffer.wrap(tmp).asIntBuffer().get();
                            if ((rPhase & 0xFFFF) == DEF_SHORT_UNDERFLOW || (rPhase & 0xFFFF) == DEF_SHORT_OVERFLOW || (rPhase & 0xFFFF) == DEF_SHORT_NO_DATA) {
                                effectiveRPhase = rPhase;
                            } else {
                                effectiveRPhase = rPhase * 0.1f * unit;
                            }

                            int tPhase;
                            tmp = new byte[4];
                            if ((resultData.mEdt[2] & 0x80) == 0x80) {
                                tmp[0] = tmp[1] = (byte)0xFF;
                            } else {
                                tmp[0] = tmp[1] = (byte)0x00;
                            }
                            tmp[2] = resultData.mEdt[2];
                            tmp[3] = resultData.mEdt[3];
                            tPhase = ByteBuffer.wrap(tmp).asIntBuffer().get();
                            if ((tPhase & 0xFFFF) == DEF_SHORT_UNDERFLOW || (tPhase & 0xFFFF) == DEF_SHORT_OVERFLOW || (tPhase & 0xFFFF) == DEF_SHORT_NO_DATA) {
                                effectiveTPhase = tPhase;
                            } else {
                                effectiveTPhase = tPhase * 0.1f * unit;
                            }
                        } else {
                            effectiveRPhase = effectiveTPhase = 0x7FFE;
                        }
                        resultInstantaneousCurrent(effectiveRPhase, effectiveTPhase);
                    }
                } else if (esv == ENLUtil.ESV_GET_SNA) {
                    // 不可応答.
                    resultInstantaneousCurrent(DEF_SHORT_NO_DATA, DEF_SHORT_NO_DATA);
                } else {
                    break;
                }
                break;
        }
    }

    /**
     * 瞬時電流量を要求元へ返却する.
     * @param rPhase R相電流量.
     * @param tPhase T相電流量.
     */
    public void resultInstantaneousCurrent(final float rPhase, final float tPhase) {
        setResult(mInstantaneousCurrentResponse, DConnectMessage.RESULT_OK);
        Bundle root = mInstantaneousCurrentResponse.getExtras();
        Bundle instantaneouscurrent = new Bundle();

        // 瞬時電力量データ設定.
        instantaneouscurrent.putDouble("rphase", rPhase);
        instantaneouscurrent.putDouble("tphase", tPhase);
        instantaneouscurrent.putString("unit", mGetInstantaneousCurrentUnit);
        root.putBundle("instantaneouscurrent", instantaneouscurrent);
        mInstantaneousCurrentResponse.putExtras(root);
        sendResponse(mInstantaneousCurrentResponse);
        sequenceGetInstantaneousCurrentProcess = 0;
    }

    /** 積算電力量取得シーケンス管理用. */
    private int sequenceGetDayDataProcess = 0;
    /** 取得日保存用. */
    private int mGetDateCount = 0;
    /** 取得時間（時）保存用. */
    private int mGetDateHour = 0;
    /** 取得時間（分）保存用. */
    private int mGetDateMinute = 0;
    /** データ格納数保存用. */
    private int mGetDateDataCount = 0;
    /** 積算電力量要求ループ数. */
    private int mGetDateLoop = 0;
    /** 積算電力量応答用Intent. */
    private Intent mDailyDataResponse;
    /** 積算電力量方向保存用. */
    private String mGetDatePowerFlow = null;
    /** 単位変換保存用. */
    private String mGetDatePowerUnit = null;
    /** 保存処理用インデックス. */
    private int mIndex = 0;
    /** 積算電力量一時保存用変数. */
    double mDayData[] = new double[96];

    /**
         * 積算電力量取得.
         * @param dateCount 取得日設定（0〜99日前）.
         * @param hour 取得開始指定時刻(時間).
         * @param minute 取得開始指定時刻（分）.
         * @param count コマ数（24 or 48）.
         * @param powerFlow 取得積算電力量方向（normal or reverse）.
         * @param unit 電力量単位.
         * @param response レスポンス設定用Intent.
         */
    public void getDailyData(final int dateCount, final int hour, final int minute, final int count, final String powerFlow, final String unit, final Intent response) {
        mGetDateCount = dateCount;
        mGetDateHour = hour;
        mGetDateMinute = minute;
        mGetDatePowerFlow = powerFlow;
        mGetDateDataCount = count;
        mGetDatePowerUnit = unit;
        mDailyDataResponse = response;

        if ((count == 48 && hour == 0 && minute < 30) ||
                (count == 24 && hour == 0 && minute < 59)) {
            mGetDateLoop = 1;
        } else {
            mGetDateLoop = 2;
        }
        mIndex = 0;
        sequenceGetDayDataProcess = 1;
        getDayDataProcess(null);
    }

    /**
     * 積算電力量取得要求.
     * @param dataCount 取得日.
     */
    public void sendCmdGetDateCount(final int dataCount) {
        byte[] enlCmd;
        String command;
        byte[] cmd;
        ByteBuffer sendCmd;
        byte[] excData = new byte[1];

        excData[0] = (byte)(dataCount & 0xFF);
        enlCmd = mENLUtil.makeEchonetLitePacket("SET_E5", excData);
        command = BP35C2.Command.SEND_TO.getString() + "1 " + mWiSunDevice.getIpv6Address() + " 0E1A 1 0 " + String.format("%04x", enlCmd.length) + " ";
        cmd = command.getBytes();
        sendCmd = ByteBuffer.allocate(cmd.length + enlCmd.length);
        sendCmd.put(cmd);
        sendCmd.put(enlCmd);
        mExecCommand = BP35C2.Command.SEND_TO;
        SendMessage(sendCmd.array());
    }

    /**
     * 積算電力量取得処置ルーチン.
     * @param data 受信データ.
     */
    public void getDayDataProcess(final byte[] data) {
        byte[] enlData;
        int esv;
        byte[] enlCmd;
        String command;
        byte[] cmd;
        ByteBuffer sendCmd;

        switch (sequenceGetDayDataProcess) {
            case 1:
                // 取得日設定
                if (mGetDateCount!= 0) {
                    sendCmdGetDateCount(mGetDateCount + 1);
                } else {
                    sendCmdGetDateCount(mGetDateCount);
                }
                sequenceGetDayDataProcess++;
                break;
            case 2:
                enlData = mENLUtil.convertHex2Bin(data);
                esv = mENLUtil.checkEsv(enlData);
                if (esv == ENLUtil.ESV_SET_RES) {
                    ENLUtil.ResultData[] rd = mENLUtil.splitResultData(enlData);
                    for(ENLUtil.ResultData resultData : rd) {
                        if (resultData.mEpc == 0xE5 && resultData.mPdc == 0) {
                            // Next.
                            switch (mGetDatePowerFlow) {
                                case "normal":
                                    enlCmd = mENLUtil.makeEchonetLitePacket("GET_E2", null);
                                    break;
                                case "reverse":
                                    enlCmd = mENLUtil.makeEchonetLitePacket("GET_E4", null);
                                    break;
                                default:
                                    return;
                            }
                            command = BP35C2.Command.SEND_TO.getString() + "1 " + mWiSunDevice.getIpv6Address() + " 0E1A 1 0 " + String.format("%04x", enlCmd.length) + " ";
                            cmd = command.getBytes();
                            sendCmd = ByteBuffer.allocate(cmd.length + enlCmd.length);
                            sendCmd.put(cmd);
                            sendCmd.put(enlCmd);
                            mExecCommand = BP35C2.Command.SEND_TO;
                            SendMessage(sendCmd.array());
                            sequenceGetDayDataProcess++;
                            break;
                        }
                    }
                }
                break;
            case 3:
                enlData = mENLUtil.convertHex2Bin(data);
                esv = mENLUtil.checkEsv(enlData);
                if (esv == ENLUtil.ESV_GET_RES) {
                    // 正常応答.
                    float unitValue = mENLUtil.getUnitValue();
                    int coeff = mENLUtil.getCoeffValue();
                    ENLUtil.ResultData[] rd = mENLUtil.splitResultData(enlData);
                    for (ENLUtil.ResultData resultData : rd) {
                        if ((resultData.mEpc == 0xE2 || resultData.mEpc == 0xE4) && resultData.mPdc == 194) {
                            // 積算電力量保存.
                            byte[] byTmp = new byte[4];
                            int pos = 2;
                            for (int n = 0; n < 48; n++) {
                                System.arraycopy(resultData.mEdt, pos, byTmp, 0, 4);
                                pos += 4;
                                int tmp = ByteBuffer.wrap(byTmp).asIntBuffer().get();
                                if (tmp == DEF_INT_NO_DATA || unitValue == 0) {
                                    mDayData[mIndex++] = DEF_INT_NO_DATA;
                                } else {
                                    mDayData[mIndex++] = tmp * coeff * unitValue;
                                }
                            }
                        } else {
                            for (int n = 0; n < 48; n++) {
                                mDayData[mIndex++] = DEF_INT_NO_DATA;
                            }
                        }
                    }
                } else if (esv == ENLUtil.ESV_GET_SNA) {
                    // 不可応答.
                    for (int n = 0; n < 48; n++) {
                        mDayData[mIndex++] = DEF_INT_NO_DATA;
                    }
                }else {
                    break;
                }
                mGetDateLoop--;
                if (mGetDateLoop == 0) {
                    resultIntegratedPower(mDayData);
                } else {
                    sendCmdGetDateCount(mGetDateCount);
                    sequenceGetDayDataProcess = 2;
                }
                break;
        }

    }

    /**
     * 指定された単位変換をして積算電力量を要求元へ返却する.
     * @param dayData 積算電力量データ.
     */
    public void resultIntegratedPower(final double[] dayData) {
        setResult(mDailyDataResponse, DConnectMessage.RESULT_OK);
        Bundle root = mDailyDataResponse.getExtras();

        // 単位変換.
        int coeff;
        if (mGetDatePowerUnit == null) {
            mGetDatePowerUnit = "Wh";
        }

        if (mGetDatePowerUnit.contains("kWh")) {
            coeff = 1;
        } else {
            coeff = 1000;
        }

        // 積算電力量データ設定.
        int index;
        double[] integratedpower = new double[mGetDateDataCount];
        if (mGetDateDataCount == 48) {
            // Index計算.
            index = ((mGetDateHour * 2) + (mGetDateMinute/30)) + 48;
            for (int n = mGetDateDataCount - 1; n >= 0; n--) {
                if (dayData[index] == DEF_INT_NO_DATA) {
                    integratedpower[n] = dayData[index--];
                } else {
                    integratedpower[n] = dayData[index--] * coeff;
                }
            }
        } else {
            // Index計算.
            index = (mGetDateHour * 2) + 48;
            for (int n = mGetDateDataCount - 1; n >= 0; n--) {
                if (dayData[index] == DEF_INT_NO_DATA && dayData[index + 1] == DEF_INT_NO_DATA) {
                    integratedpower[n] = dayData[index];
                } else if (dayData[index] != DEF_INT_NO_DATA && dayData[index + 1] == DEF_INT_NO_DATA) {
                    integratedpower[n] = dayData[index] * coeff;
                } else if (dayData[index] == DEF_INT_NO_DATA && dayData[index + 1] != DEF_INT_NO_DATA) {
                    integratedpower[n] = dayData[index + 1] * coeff;
                } else {
                    integratedpower[n] = (dayData[index] + dayData[index + 1]) * coeff;
                }
                index -= 2;
            }
        }
        root.putDoubleArray("integratedpower", integratedpower);
        root.putInt("count", mGetDateDataCount);
        root.putString("unit", mGetDatePowerUnit);
        root.putString("powerFlow", mGetDatePowerFlow);
        mDailyDataResponse.putExtras(root);
        sendResponse(mDailyDataResponse);
        sequenceGetDayDataProcess = 0;
    }
}
