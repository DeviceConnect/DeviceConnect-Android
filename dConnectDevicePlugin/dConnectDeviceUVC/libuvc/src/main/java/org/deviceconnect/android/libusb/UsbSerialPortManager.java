/*
 UsbSerialPortManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libusb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.deviceconnect.android.libuvc.BuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * USBデバイスとのアタッチ・デタッチを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class UsbSerialPortManager {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグタグ.
     */
    private static final String TAG = "USB";

    /**
     * タイムアウトのメッセージタイプを定義します.
     */
    private static final int MSG_TYPE_TIMEOUT = 123;

    /**
     * リクエストコードを定義します.
     */
    private static final int REQUEST_CODE = 12345;

    /**
     * USBパーミッションのアクションを定義します.
     */
    private static final String ACTION_USB_PERMISSION = "org.deviceconnect.android.libuvc.USB_PERMISSION";

    /**
     * USB管理クラス.
     */
    private final UsbManager mUsbManager;

    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * USBのイベントを通知するリスナー.
     */
    private final List<Holder> mOnUsbEventListenerHolders = new ArrayList<>();

    /**
     * モニタリングフラグ.
     * <p>
     * モニタリング中はtrue、それ以外はfalse.
     * </p>
     */
    private boolean mIsMonitoringFlag;

    /**
     * 通知を行うフィルターのリスト.
     */
    private final List<UsbDeviceFilter> mFilters = new ArrayList<>();

    /**
     * 接続しているUSBデバイスのリスト.
     */
    private final List<UsbSerialPort> mUsbSerialPorts = new ArrayList<>();

    /**
     * パーミッションの取得を行うためのキュー.
     */
    private final List<UsbDevice> mRequestPermissionQueue = new ArrayList<>();

    /**
     * パーミッションのリクエストを処理中フラグ.
     */
    private boolean mProcessingRequestPermission;

    /**
     * パーミッション要求を行うためのスレッド.
     */
    private HandlerThread mHandlerThread;

    /**
     * パーミッション要求を行うためのハンドラ.
     */
    private Handler mHandler;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     * @throws UnsupportedOperationException USBデバイスをサポートしていない場合に発生
     */
    public UsbSerialPortManager(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null.");
        }
        mContext = context;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (mUsbManager == null) {
            throw new UnsupportedOperationException("Not support USB.");
        }
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * フィルターにvendorIdとproductIdを追加します.
     * @param vendorId ベンダーID
     * @param productId プロダクトID
     */
    public void addFilter(final int vendorId, final int productId) {
        mFilters.add(new UsbDeviceIdFilter(vendorId, productId));
    }

    /**
     * フィルターにclassを追加します.
     * @param classType USBデバイスのクラス
     */
    public void addFilter(final int classType) {
        mFilters.add(new UsbDeviceClassFilter(classType));
    }

    /**
     * フィルターをクリアします.
     */
    public void clearFilter() {
        mFilters.clear();
    }

    /**
     * USBデバイスのアタッチ・デタッチのイベントを通知するリスナーを設定します.
     * <p>
     *     deviceClassが一致するリスナーに対して通知します。
     *     deviceClassが-1の場合には、全てのイベントを通知します。
     * </p>
     * @param deviceClass USBのデバイスクラス
     * @param listener リスナー
     */
    public void addOnUsbEventListener(final int deviceClass, final OnUsbEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }

        Holder holder = new Holder();
        holder.mDeviceClass = deviceClass;
        holder.mListener = listener;

        synchronized (mOnUsbEventListenerHolders) {
            mOnUsbEventListenerHolders.add(holder);
        }
    }

    /**
     * USBデバイスのアタッチ・デタッチのイベントを通知するリスナーを削除します.
     *
     * @param deviceClass USBのデバイスクラス
     * @param listener リスナー
     */
    public void removeOnUsbEventListener(final int deviceClass, final OnUsbEventListener listener) {
        synchronized (mOnUsbEventListenerHolders) {
            for (Holder holder : mOnUsbEventListenerHolders) {
                if (holder.mDeviceClass == deviceClass && holder.mListener == listener) {
                    mOnUsbEventListenerHolders.add(holder);
                    return;
                }
            }
        }
    }

    /**
     * USBデバイスの監視を開始します.
     * <p>
     * 既に監視が開始されている場合には何も処理を行いません。<br>
     * 監視を開始するときに、既に接続されているデバイスは即座にイベントを通知します。
     * </p>
     */
    public synchronized void startUsbMonitoring() {
        if (mIsMonitoringFlag) {
            if (DEBUG) {
                Log.d(TAG, "Monitoring is already started.");
            }
            return;
        }
        mIsMonitoringFlag = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReceiver, filter);

        // 既にUSBに接続されているデバイスがあれば、通知する
        List<UsbDevice> deviceList = getDeviceList();
        for (UsbDevice device : deviceList) {
            checkAttach(device);
        }

        mHandlerThread = new HandlerThread("request-permission");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE_TIMEOUT) {
                    if (msg.obj instanceof UsbDevice) {
                        checkAttach((UsbDevice) msg.obj);
                    }
                }
            }
        };
    }

    /**
     * USBデバイスの監視を停止します.
     */
    public synchronized void stopUsbMonitoring() {
        if (!mIsMonitoringFlag) {
            if (DEBUG) {
                Log.d(TAG, "Monitoring is already stopped.");
            }
            return;
        }
        mIsMonitoringFlag = false;
        mProcessingRequestPermission = false;

        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        mHandler = null;

        synchronized (mRequestPermissionQueue) {
            mRequestPermissionQueue.clear();
        }

        try {
            mContext.unregisterReceiver(mUsbReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * 接続されている全てのデバイスをデタッチします.
     */
    public void dispose() {
        synchronized (mUsbSerialPorts) {
            List<UsbSerialPort> serialPorts = new ArrayList<>(mUsbSerialPorts);
            for (UsbSerialPort serialPort : serialPorts) {
                detachUsbSerialPort(serialPort.getUsbDevice());
            }
            mUsbSerialPorts.clear();
        }
    }

    /**
     * 接続しているUSBデバイスのリストを取得します.
     * @return USBデバイスのリスト
     */
    public List<UsbSerialPort> getUsbSerialPorts() {
        synchronized (mUsbSerialPorts) {
            return new ArrayList<>(mUsbSerialPorts);
        }
    }

    /**
     * 接続されているUSBデバイスのパーミッションを再度要求します.
     */
    public void requestPermissionAgain() {
        List<UsbDevice> deviceList = getDeviceList();
        for (UsbDevice device : deviceList) {
            if (!isConnected(device)) {
                requestPermission(device);
            }
        }
    }

    /**
     * 指定されたUSBデバイスが接続されているか確認します.
     *
     * @param device 確認を行うUSBデバイス
     * @return 接続されている場合はtrue、それ以外はfalse
     */
    private boolean isConnected(final UsbDevice device) {
        synchronized (mUsbSerialPorts) {
            for (UsbSerialPort port : mUsbSerialPorts) {
                if (device.equals(port.getUsbDevice())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * UsbDevice に対応する UsbSerialPort を取得します.
     * <p>
     * 保持しているリストに存在しない場合は null を返却します。
     * </p>
     * @param device UsbDeviceのインスタンス
     * @return UsbSerialPortのインスタンス
     */
    private UsbSerialPort getUsbSerialPort(final UsbDevice device) {
        synchronized (mUsbSerialPorts) {
            for (UsbSerialPort port : mUsbSerialPorts) {
                if (device.equals(port.getUsbDevice())) {
                    return port;
                }
            }
        }
        return null;
    }

    /**
     * 接続中のUSBデイバスのリストを取得します.
     * @return USBデバイスのリスト
     */
    private List<UsbDevice> getDeviceList() {
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        return new ArrayList<>(deviceList.values());
    }

    /**
     * USBのパーミッションを確認して、問題がなければ接続を行います。
     * <p>
     * パーミッションがない場合には許可ダイアログを表示します。
     * </p>
     * @param device 接続を行うUSBデバイス
     */
    private void checkAttach(final UsbDevice device) {
        if (!checkFilter(device)) {
            return;
        }

        if (mUsbManager.hasPermission(device)) {
            attachUsbSerialPort(device);
        } else {
            synchronized (mRequestPermissionQueue) {
                if (mProcessingRequestPermission) {
                    mRequestPermissionQueue.add(device);
                } else {
                    requestPermission(device);
                }
            }
        }
    }

    /**
     * USBのパーミッションをリクエストします.
     *
     * @param device パーミッションをリクエストするUSBデバイス
     */
    private void requestPermission(final UsbDevice device) {
        mProcessingRequestPermission = true;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, REQUEST_CODE, new Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mUsbManager.requestPermission(device, pendingIntent);

        // パーミッションのレスポンスを待つタイムアウト処理
        if (mHandler != null) {
            Message message = Message.obtain();
            message.what = MSG_TYPE_TIMEOUT;
            message.obj = device;
            mHandler.sendMessageDelayed(message, 30000);
        }
    }

    /**
     * USBデバイスとアタッチします.
     *
     * @param device アタッチするUSBデバイス
     */
    private synchronized void attachUsbSerialPort(final UsbDevice device) {
        if (!checkFilter(device)) {
            return;
        }

        UsbSerialPort serialPort = getUsbSerialPort(device);
        if (serialPort != null) {
            return;
        }

        serialPort = new UsbSerialPort(mUsbManager, device);
        synchronized (mUsbSerialPorts) {
            mUsbSerialPorts.add(serialPort);
        }
        postAttach(device, serialPort);
    }

    /**
     * USBデバイスとデタッチします.
     *
     * @param device デタッチするUSBデバイス
     */
    private synchronized void detachUsbSerialPort(final UsbDevice device) {
        UsbSerialPort serialPort = getUsbSerialPort(device);
        if (serialPort != null) {
            synchronized (mUsbSerialPorts) {
                mUsbSerialPorts.remove(serialPort);
            }
            postDetach(device, serialPort);
        }
    }

    /**
     * 指定されたUSBデバイスがフィルターで一致するか確認します.
     * <p>
     * フィルターが設定されていない場合には、trueを返却します。
     * </p>
     * @param device USBデバイス
     * @return フィルターに合致する場合はtrue、それ以外はfalse
     */
    private boolean checkFilter(final UsbDevice device) {
        if (mFilters.isEmpty()) {
            return true;
        }

        for (UsbDeviceFilter filter : mFilters) {
            if (filter.checkFilter(device)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 次のリクエストパーミッションを処理します.
     * <p>
     * リクエストのキューがなくなった場合には、mProcessingRequestPermission を false に設定します。
     * </p>
     */
    private void nextRequestPermission() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_TYPE_TIMEOUT);
        }

        synchronized (mRequestPermissionQueue) {
            if (mRequestPermissionQueue.isEmpty()) {
                mProcessingRequestPermission = false;
            } else {
                // パーミッション要求のレスポンスを受け取ってから直ぐにパーミッション要求を行う
                // と失敗するので、ここでは、1秒ほど停止してから要求を行う。
                mHandler.postDelayed(() -> {
                    synchronized (mRequestPermissionQueue) {
                        requestPermission(mRequestPermissionQueue.remove(0));
                    }
                }, 500);
            }
        }
    }

    /**
     * アタッチのイベントを通知します.
     *
     * @param device アタッチされたデバイス
     * @param serialPort アタッチされたデバイス
     */
    private void postAttach(final UsbDevice device, final UsbSerialPort serialPort) {
        synchronized (mOnUsbEventListenerHolders) {
            for (Holder holder : mOnUsbEventListenerHolders) {
                if (holder.mDeviceClass == -1 || holder.mDeviceClass == device.getDeviceClass()) {
                    try {
                        holder.mListener.onAttached(serialPort);
                    } catch (Exception e) {
                        // ignore.
                    }
                }
            }
        }
    }

    /**
     * デタッチのイベントを通知します.
     *
     * @param device デタッチされたデバイス
     * @param serialPort デタッチされたデバイス
     */
    private void postDetach(final UsbDevice device, final UsbSerialPort serialPort) {
        synchronized (mOnUsbEventListenerHolders) {
            for (Holder holder : mOnUsbEventListenerHolders) {
                if (holder.mDeviceClass == -1 || holder.mDeviceClass == device.getDeviceClass()) {
                    try {
                        holder.mListener.onDetached(serialPort);
                    } catch (Exception e) {
                        // ignore.
                    }
                }
            }
        }
    }

    /**
     * 接続にエラーが発生したことを通知します.
     *
     * @param device エラーが発生したデバイス
     */
    private void postError(final UsbDevice device) {
        synchronized (mOnUsbEventListenerHolders) {
            for (Holder holder : mOnUsbEventListenerHolders) {
                if (holder.check(device)) {
                    try {
                        holder.mListener.onError(new SecurityException("permission denied for device. " + device.getDeviceName()));
                    } catch (Exception e) {
                        // ignore.
                    }
                }
            }
        }
    }

    /**
     * パーミッションを要求します.
     *
     * @param device USB デバイス
     */
    private void postOnRequestPermission(final UsbDevice device) {
        synchronized (mOnUsbEventListenerHolders) {
            for (Holder holder : mOnUsbEventListenerHolders) {
                if (holder.check(device)) {
                    try {
                        holder.mListener.onRequestPermission(new PermissionCallback() {
                            @Override
                            public void allow() {
                                checkAttach(device);
                            }

                            @Override
                            public void deny() {
                                postError(device);
                            }
                        });
                    } catch (Exception e) {
                        // ignore.
                    }
                }
            }
        }
    }

    /**
     * OnUsbEventListenerを保持するためのクラス.
     */
    private static class Holder {
        /**
         * デバイスクラス.
         */
        private int mDeviceClass;

        /**
         * 通知を行うリスナー.
         */
        private OnUsbEventListener mListener;

        /**
         * デバイスクラスを確認します.
         *
         * @param device USBデバイス
         * @return デバイスクラスが一致する場合はtrue、それ以外はfalse
         */
        boolean check(UsbDevice device) {
            return mDeviceClass == -1 || mDeviceClass == device.getDeviceClass();
        }
    }

    /**
     * USBデバイスのアタッチ・デタッチを受け取る BroadcastReceiver.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        attachUsbSerialPort(device);
                    } else {
                        postOnRequestPermission(device);
                    }
                }
                nextRequestPermission();
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    checkAttach(device);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    detachUsbSerialPort(device);
                }
            }
        }
    };

    /**
     * パーミッション不足を通知して、許可を受け取るためのリスナー.
     */
    public interface PermissionCallback {
        /**
         * 許可された場合に呼び出します.
         */
        void allow();

        /**
         * 拒否された場合に呼び出します.
         */
        void deny();
    }

    /**
     * USBデバイスのアタッチ・デタッチのイベントを通知するリスナー.
     */
    public interface OnUsbEventListener {

        /**
         * USBデバイスにアタッチを通知します.
         * @param device USBデバイス
         */
        void onAttached(UsbSerialPort device);

        /**
         * USBデバイスとのデタッチを通知します.
         * @param device USBデバイス
         */
        void onDetached(UsbSerialPort device);

        /**
         * エラーを通知します.
         * @param e 例外
         */
        void onError(Exception e);

        /**
         * パーミッション要求を通知します.
         *
         * @param callback パーミッション要求の結果通知するコールバック
         */
        void onRequestPermission(PermissionCallback callback);
    }
}
