/*
 HvcDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hvc.ble.BleDeviceDetector;
import org.deviceconnect.android.deviceplugin.hvc.ble.BleDeviceDetector.BleDeviceDiscoveryListener;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManager;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManagerUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcHumanDetectionProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcSystemProfile;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestUtils;
import org.deviceconnect.android.deviceplugin.hvc.service.HvcService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * HVC Device Service.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceService extends DConnectMessageService implements HvcCommManager.HVCConnectionListener {

    /**
     * log tag.
     */
    private static final String TAG = HvcDeviceService.class.getSimpleName();

    /**
     * Debug.
     */
    private static final Boolean DEBUG = BuildConfig.DEBUG;

    /**
     * HVC comm managers(1serviceId,1record).
     */
    private final List<HvcCommManager> mHvcCommManagerArray = new ArrayList<>();

    /**
     * event interval timer information array.<br>
     * - not exist record : stop timer.<br>
     * - exist record : running timer.<br>
     */
    private List<HvcTimerInfo> mIntervalTimerInfoArray = new ArrayList<>();

    /**
     * HVC found device list.
     */
    private final List<BluetoothDevice> mCacheDeviceList = new ArrayList<>();

    /**
     * BLE device detector.
     */
    private BleDeviceDetector mDetector;
    /** BLE Receiver. */
    private final BroadcastReceiver mBLEReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (DEBUG) {
                    Log.d(TAG, "bluetooth state change.");
                }

                Bundle extras = intent.getExtras();
                int state = extras.getInt(BluetoothAdapter.EXTRA_STATE);
                if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    // Bluetooth ON -> OFF
                    // stop scan process.
                    stopSearchHvcDevice();

                    turnOff(getServiceProvider().getServiceList());
                } else if (state == BluetoothAdapter.STATE_ON) {
                    // Bluetooth OFF -> ON
                    // start scan process.
                    startSearchHvcDevice();
                }
            }
        }
    };
    @Override
    public void onConnected(final String serviceId) {
        DConnectService service = getServiceProvider().getService(serviceId);
        if (service != null) {
            service.setOnline(true);
            getServiceProvider().addService(service);
        }
    }

    @Override
    public void onDisconnected(final String serviceId) {
        DConnectService service = getServiceProvider().getService(serviceId);
        if (service != null) {
            service.setOnline(false);
            getServiceProvider().addService(service);
            for (int i = 0; i < mHvcCommManagerArray.size(); i++) {
                if (mHvcCommManagerArray.get(i).getServiceId().equals(serviceId)) {
                    mHvcCommManagerArray.remove(i);
                }
            }
            if (mDetector != null && service.getName() != null) {
                mDetector.removeCacheDevice(service.getName());
            }
        }
    }


    
    @Override
    public void onCreate() {
        super.onCreate();

        // start HVC device search.
        startSearchHvcDevice();

        // add supported profiles
        addProfile(new HvcServiceDiscoveryProfile(getServiceProvider()));
        addProfile(new HvcHumanDetectionProfile());
        
        // start timeout judge timer.
        startTimeoutJudgeTimer();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBLEReceiver, filter);
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(mBLEReceiver);
        resetPluginResource();
        super.onDestroy();
    }
    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (DEBUG) {
            Log.i(TAG, "Plug-in : onManagerUninstalled");
        }

    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (DEBUG) {
            Log.i(TAG, "Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String origin) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (DEBUG) {
            Log.i(TAG, "Plug-in : onManagerEventTransmitDisconnected");
        }
        if (origin != null) {
            unregisterDetectionEventByMatchedOrigin(origin);
        } else {
            removeAllDetectEvent();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (DEBUG) {
            Log.i(TAG, "Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        /* 全イベント削除. */
        removeAllDetectEvent();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent == null) {
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HvcSystemProfile();
    }

    //
    // service discovery profile
    //

    /**
     * get HVC device list.
     * 
     * @return HVC device list
     */
    public List<BluetoothDevice> getHvcDeviceList() {
        List<BluetoothDevice> deviceList = new ArrayList<>();
        synchronized (mCacheDeviceList) {
            deviceList.addAll(mCacheDeviceList);
        }
        synchronized (mHvcCommManagerArray) {
            deviceList.addAll(HvcCommManagerUtils.getConnectedBluetoothDevices(mHvcCommManagerArray));
        }
        removeDuplicateOrNonHvcData(deviceList);
        return deviceList;
    }

    //
    // for human detect profile
    //


    //
    // register detection event.
    //

    /**
     * Human Detect Profile register detection event.
     * 
     * @param detectKind detectKind
     * @param requestParams request parameters.
     * @param response response
     * @param serviceId serviceId
     * @param origin origin
     */
    public void registerDetectionEvent(final HumanDetectKind detectKind, final HumanDetectRequestParams requestParams,
            final Intent response, final String serviceId, final String origin) {

        if (DEBUG) {
            Log.d(TAG, "registerDetectionEvent(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId
                    + " origin:" + origin);
        }

        // Bluetooth OFF
        if (mDetector == null || !mDetector.isEnabled()) {
            if (DEBUG) {
                Log.d(TAG, "Bluetooth OFF");
            }
            MessageUtils.setIllegalDeviceStateError(response, "bluetooth OFF.");
            sendResponse(response);
            return;
        }

        // search CommManager by serviceId(if not found, add CommManager.).
        HvcCommManager commManager;
        synchronized (mHvcCommManagerArray) {
            commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }
        if (commManager == null) {
            
            // search cache bluetooth device.(if not found, not found service error)
            BluetoothDevice bluetoothDevice = searchCacheHvcDevice(serviceId);
            if (bluetoothDevice == null) {
                MessageUtils.setNotFoundServiceError(response);
                sendResponse(response);
                return;
            }
            
            // add comm manager.
            commManager = new HvcCommManager(this, this, serviceId, bluetoothDevice);
            synchronized (mHvcCommManagerArray) {
                mHvcCommManagerArray.add(commManager);
            }
            
        }

        // start interval timer. (if no event with same interval.)
        startIntervalTimer(requestParams.getEvent().getInterval());

        // add event data to commManager.
        commManager.registerDetectEvent(detectKind, requestParams, response, origin);
    }

    //
    // unregister detection event.
    //

    /**
     * Human Detect Profile unregister detection event.<br>
     * 
     * @param detectKind detectKind
     * @param response response
     * @param serviceId serviceId
     * @param origin origin
     */
    public void unregisterDetectionEvent(final HumanDetectKind detectKind, final Intent response,
            final String serviceId, final String origin) {

        if (DEBUG) {
            Log.d(TAG, "unregisterDetectionEvent(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId
                    + " origin:" + origin);
        }

        // search CommManager by serviceId.
        HvcCommManager commManager;
        synchronized (mHvcCommManagerArray) {
            commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }
        if (commManager == null) {
            MessageUtils.setNotFoundServiceError(response, "service id not found");
            sendResponse(response);
            return;
        }
        // get event interval.
        Long interval = commManager.getEventInterval(detectKind, origin);
        if (interval == null) {
            MessageUtils.setInvalidRequestParameterError(response, "detectKind and origin pair not found.");
            sendResponse(response);
            return;
        }
        
        // unregister
        commManager.unregisterDetectEvent(detectKind, origin);

        // if no event with same interval, stop interval timer (and remove
        // interval timer info record).
        boolean result = HvcCommManagerUtils.checkExistEventByInterval(mHvcCommManagerArray, interval);
        if (!result) {
            if (DEBUG) {
                Log.d(TAG, "stop interval timer. interval:" + interval);
            }
            stopIntervalTimer(interval);
        }

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister OnDetection event");
        sendResponse(response);
    }

    /**
     * Human Detect Profile unregister detection event by matched origin.
     * @param origin origin
     */
    private void unregisterDetectionEventByMatchedOrigin(final String origin) {
        for (HvcCommManager commManager : mHvcCommManagerArray) {
            commManager.removeDetectEvent(origin);
            HumanDetectKind kind;
            for (int i = 0; i < HumanDetectKind.values().length; i++) {
                switch (i) {
                    case 0:
                        kind = HumanDetectKind.BODY;
                        break;
                    case 1:
                        kind = HumanDetectKind.HAND;
                        break;
                    case 2:
                        kind = HumanDetectKind.FACE;
                        break;
                    case 3:
                    default:
                        kind = HumanDetectKind.HUMAN;
                        break;
                }
                Long interval = commManager.getEventInterval(kind, origin);
                if (interval != null) {
                    stopIntervalTimer(interval);
                }
            }
        }
    }

    /**
     * remove all detect event.
     */
    private void removeAllDetectEvent() {
        for (HvcCommManager commManager : mHvcCommManagerArray) {
            /* 全イベント解除. */
            commManager.removeAllDetectEvent();
            /* 全インターバルタイマー削除. */
            int count = mIntervalTimerInfoArray.size();
            for (int index = (count - 1); index >= 0; index--) {
                HvcTimerInfo timerInfo = mIntervalTimerInfoArray.get(index);
                timerInfo.stopTimer();
                mIntervalTimerInfoArray.remove(index);
            }
        }
    }

    /**
     * Search HVC.
     * @param serviceId HVC's serviceID
     */
    public void searchHVC(final String serviceId) {

        // Bluetooth OFF
        if (mDetector == null || !mDetector.isEnabled()) {
            if (DEBUG) {
                Log.d(TAG, "Bluetooth OFF");
            }
            return;
        }

        // search CommManager by serviceId(if not found, add CommManager.).
        HvcCommManager commManager;
        synchronized (mHvcCommManagerArray) {
            commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }

        if (commManager == null) {
            // search cache bluetooth device.(if not found, not found service error)
            BluetoothDevice bluetoothDevice = searchCacheHvcDevice(serviceId);
            if (bluetoothDevice == null) {
                if (DEBUG) {
                    Log.d(TAG, "service not found");
                }
                return;
            }

            // add comm manager.
            commManager = new HvcCommManager(this, this, serviceId, bluetoothDevice);
            synchronized (mHvcCommManagerArray) {
                mHvcCommManagerArray.add(commManager);
            }
        }

        final HvcCommManager commManagerFinal = commManager;
        // get detection process. (if in communication, wait and retry)
        retryProcInNewThread(HvcConstants.HVC_COMM_RETRY_COUNT, HvcConstants.HVC_COMM_RETRY_INTERVAL,
                new HvcRetryProcListener() {
                    @Override
                    public boolean judgeRetry() {
                        if (DEBUG) {
                            Log.d(TAG, "judgeRetry()");
                        }
                        // retry(Now in the device communication)
                        if (commManagerFinal.checkCommBusy()) {
                            if (DEBUG) {
                                Log.d(TAG, "retry");
                            }
                            return true;
                        }
                        // no retry
                        if (DEBUG) {
                            Log.d(TAG, "no retry");
                        }
                        return false;
                    }

                    @Override
                    public void procOnNewThread() {
                        if (DEBUG) {
                            Log.d(TAG, "proc()");
                        }

                        // get detection process.
                        commManagerFinal.detectHVC();
                    }

                    @Override
                    public void timeoutProc() {
                        if (DEBUG) {
                            Log.d(TAG, "timeoutProc()");
                        }
                    }
                });
    }
    /**
     * Human Detect Profile get detection.<br>
     * 
     * @param detectKind detectKind
     * @param requestParams request parameters.
     * @param response response
     * @param serviceId serviceId
     */
    public void doGetDetectionProc(final HumanDetectKind detectKind, final HumanDetectRequestParams requestParams,
            final Intent response, final String serviceId) {
        if (DEBUG) {
            Log.d(TAG, "doGetDetectionProc(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId);
        }
        
        // Bluetooth OFF
        if (mDetector == null || !mDetector.isEnabled()) {
            MessageUtils.setIllegalDeviceStateError(response, "bluetooth OFF.");
            sendResponse(response);
            if (DEBUG) {
                Log.d(TAG, "Bluetooth OFF");
            }
            return;
        }
        
        // search CommManager by serviceId(if not found, add CommManager.).
        HvcCommManager commManager;
        synchronized (mHvcCommManagerArray) {
            commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }

        if (commManager == null) {
            // search cache bluetooth device.(if not found, not found service error)
            BluetoothDevice bluetoothDevice = searchCacheHvcDevice(serviceId);
            if (bluetoothDevice == null) {
                MessageUtils.setNotFoundServiceError(response);
                sendResponse(response);
                if (DEBUG) {
                    Log.d(TAG, "service not found");
                }
                return;
            }
            
            // add comm manager.
            commManager = new HvcCommManager(this, this, serviceId, bluetoothDevice);
            synchronized (mHvcCommManagerArray) {
                mHvcCommManagerArray.add(commManager);
            }
        }

        final HvcCommManager commManagerFinal = commManager;
        // get detection process. (if in communication, wait and retry)
        retryProcInNewThread(HvcConstants.HVC_COMM_RETRY_COUNT, HvcConstants.HVC_COMM_RETRY_INTERVAL,
                new HvcRetryProcListener() {
                    @Override
                    public boolean judgeRetry() {
                        if (DEBUG) {
                            Log.d(TAG, "judgeRetry()");
                        }
                        // retry(Now in the device communication)
                        if (commManagerFinal.checkCommBusy()) {
                            if (DEBUG) {
                                Log.d(TAG, "retry");
                            }
                            return true;
                        }
                        // no retry
                        if (DEBUG) {
                            Log.d(TAG, "no retry");
                        }
                        return false;
                    }

                    @Override
                    public void procOnNewThread() {
                        if (DEBUG) {
                            Log.d(TAG, "proc()");
                        }

                        // get detection process.
                        commManagerFinal.doGetDetectionProc(detectKind, requestParams, response);
                    }

                    @Override
                    public void timeoutProc() {
                        if (DEBUG) {
                            Log.d(TAG, "timeoutProc()");
                        }

                        // timeout error.
                        MessageUtils.setTimeoutError(response, "GET API timeout.");
                        sendResponse(response);
                    }
                });
    }

    /**
     * Initialize BLE device detector.
     */
    private void initDetector() {
        if (mDetector == null) {
            mDetector = new BleDeviceDetector(this);
            mDetector.setListener((currentDevices) -> {
                // remove duplicate data or non HVC data.
                removeDuplicateOrNonHvcData(currentDevices);

                synchronized (mCacheDeviceList) {
                    mCacheDeviceList.clear();
                    mCacheDeviceList.addAll(currentDevices);
                }

                turnOn(currentDevices);
            });
        }
    }

    private List<DConnectService> findLostServices(final List<BluetoothDevice> currentList) {
        List<DConnectService> lostServices = new ArrayList<DConnectService>();
        for (DConnectService cached : getServiceProvider().getServiceList()) {
            boolean isFound = false;
            check:
            for (BluetoothDevice current : currentList) {
                if (cached.getId().equals(HvcService.createServiceId(current))) {
                    isFound = true;
                    break check;
                }
            }
            if (!isFound) {
                lostServices.add(cached);
            }
        }
        return lostServices;
    }

    /**
     * Change service status to ON.
     * @param devices HVC device
     */
    private void turnOn(final List<BluetoothDevice> devices) {
        for (BluetoothDevice device : devices) {
            DConnectService service = getServiceProvider().getService(device.getAddress());
            if (service == null) {
                service = new HvcService(device);
                getServiceProvider().addService(service);
                // For offline detection.
                searchHVC(HvcCommManager.getServiceId(device.getAddress()));
            }
            service.setOnline(true);
        }
    }
    /**
     * Change service status to ON.
     * @param services HVC device
     */
    private void turnOff(final List<DConnectService> services) {
        for (DConnectService service : services) {
            service.setOnline(false);
        }
    }

    /**
     * retry process in new thread.
     * 
     * @param retryCount retry count
     * @param retryInteval retry interval[msec]
     * @param listener retry process listener.
     */
    private void retryProcInNewThread(final int retryCount, final int retryInteval, 
            final HvcRetryProcListener listener) {

        if (DEBUG) {
            Log.d(TAG, "retryProcOnNewThread() start");
        }

        // new thread start
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (DEBUG) {
                    Log.d(TAG, "retryProcOnNewThread() - run() start");
                }

                // retry loop
                for (int index = 0; index < retryCount; index++) {
                    // judge retry.
                    if (!listener.judgeRetry()) {
                        // if no retry, call process.
                        listener.procOnNewThread();
                        return;
                    }

                    // if retry, interval sleep and next loop.
                    if (DEBUG) {
                        Log.d(TAG, "retryProcOnNewThread() - Retry");
                    }
                    try {
                        sleep(retryInteval);
                    } catch (InterruptedException e) {
                        if (DEBUG) {
                            Log.d(TAG, "retryProcOnNewThread() - run() sleep exception, " + e.getMessage());
                        }
                    }
                }

                // timeout process.
                listener.timeoutProc();
            }
        };
        thread.start();
    }

    /**
     * retry process listener.
     */
    private interface HvcRetryProcListener {
        /**
         * judge retry.
         * 
         * @return true: retry / false: no retry
         */
        boolean judgeRetry();

        /**
         * process on new thread.
         */
        void procOnNewThread();

        /**
         * timeout process.
         */
        void timeoutProc();
    }
    
    /**
     * search cache HVC device by serviceId.
     * 
     * @param serviceId serviceId
     * @return not null: found device / null: not found
     */
    public BluetoothDevice searchCacheHvcDevice(final String serviceId) {
        List<BluetoothDevice> hvcDeviceList = getHvcDeviceList();
        for (BluetoothDevice device : hvcDeviceList) {
            if (serviceId.equals(HvcCommManager.getServiceId(device.getAddress()))) {
                return device;
            }
        }
        return null;
    }

    public synchronized void startSearchHvcDevice() {
        if (mDetector == null) {
            initDetector();
        }
        HvcDeviceApplication.getInstance().checkLocationEnable();
        if (mDetector.isEnabled()) {
            mDetector.startScan();
        }
    }

    public synchronized void stopSearchHvcDevice() {
        mDetector.stopScan();
    }

    /**
     * start interval timer(if no event with same interval.).
     * 
     * @param interval interval[msec]
     */
    private void startIntervalTimer(final long interval) {
        // search timer, if interval to match.
        HvcTimerInfo timerInfo = HvcTimerInfoUtils.search(mIntervalTimerInfoArray, interval);
        if (timerInfo == null) {
            // if no match, start interval timer.
            timerInfo = new HvcTimerInfo(interval);
            timerInfo.startTimer(new TimerTask() {
                @Override
                public void run() {
                    if (DEBUG) {
                        Log.d(TAG, "event interval timer proc.");
                    }
                    // Bluetooth OFF
                    if (mDetector == null || !mDetector.isEnabled()) {
                        if (DEBUG) {
                            Log.d(TAG, "can not send event. (bluetooth OFF)");
                        }
                        return;
                    }
                    // call event process.
                    synchronized (mHvcCommManagerArray) {
                        for (HvcCommManager commManager : mHvcCommManagerArray) {
                            commManager.onEventProc(interval);
                        }
                    }
                }
            });
            mIntervalTimerInfoArray.add(timerInfo);
        }
    }

    /**
     * stop interval timer(and remove record).
     * 
     * @param interval interval
     */
    private void stopIntervalTimer(final long interval) {
        int count = mIntervalTimerInfoArray.size();
        for (int index = (count - 1); index >= 0; index--) {
            HvcTimerInfo timerInfo = mIntervalTimerInfoArray.get(index);
            if (timerInfo.getInterval() == interval) {
                timerInfo.stopTimer();
                mIntervalTimerInfoArray.remove(index);
            }
        }
    }

    /**
     * start timeout judge timer.
     */
    private void startTimeoutJudgeTimer() {
        // timeout judge timer information.
        HvcTimerInfo mTimeoutJudgeTimer = new HvcTimerInfo(HvcConstants.TIMEOUT_JUDGE_INTERVAL);
        mTimeoutJudgeTimer.startTimer(new TimerTask() {
            @Override
            public void run() {
                if (DEBUG) {
                    Log.d(TAG, "timeout judge timer proc.");
                }
                // call timeout judge process.
                synchronized (mHvcCommManagerArray) {
                    for (HvcCommManager commManager : mHvcCommManagerArray) {
                        commManager.onTimeoutJudgeProc();
                    }
                }
            }
        });
    }

    /**
     * remove duplicate data.
     * @param devices found devices.
     */
    private void removeDuplicateOrNonHvcData(final List<BluetoothDevice> devices) {
        Pattern p = Pattern.compile(HvcConstants.HVC_DEVICE_NAME_PREFIX);
        int deviceCount = devices.size();
        for (int deviceIndex = (deviceCount - 1); deviceIndex >= 0; deviceIndex--) {
            
            // remove if non HVC device name.
            if (devices.get(deviceIndex) == null
            || devices.get(deviceIndex).getName() == null
            || !p.matcher(devices.get(deviceIndex).getName()).find()) {
                devices.remove(deviceIndex);
                continue;
            }
            
            // remove if duplicate.
            boolean isFoundDuplicate = false;
            for (int compareIndex = 0; compareIndex < deviceIndex; compareIndex++) {
                if (devices.get(deviceIndex).equals(devices.get(compareIndex))) {
                    isFoundDuplicate = true;
                    break;
                }
            }
            if (isFoundDuplicate) {
                devices.remove(deviceIndex);
            }
        }
    }
    
    /**
     * timer information.
     * 
     * @author NTT DOCOMO, INC.
     */
    private class HvcTimerInfo {

        /**
         * Interval[msec].
         */
        private long mInterval;

        /**
         * Timer.
         */
        private Timer mTimer;

        /**
         * Timer running flag.
         */
        private boolean mIsTimerRunning;

        /**
         * Constructor.
         * 
         * @param interval interval[msec]
         */
        public HvcTimerInfo(final long interval) {
            mInterval = interval;
            mTimer = new Timer();
            mIsTimerRunning = false;
        }

        /**
         * get interval.
         * 
         * @return interval[msec]
         */
        public long getInterval() {
            return mInterval;
        }

        /**
         * start timer.
         * 
         * @param intervalTimerTask interval timer task
         */
        public void startTimer(final TimerTask intervalTimerTask) {
            if (!mIsTimerRunning) {
                // add timertask.
                mTimer.scheduleAtFixedRate(intervalTimerTask, 0, mInterval);
                mIsTimerRunning = true;
            } else {
                // change timertask.
                mTimer.cancel();
                mTimer.scheduleAtFixedRate(intervalTimerTask, 0, mInterval);
            }
        }

        /**
         * stop timer.
         */
        public void stopTimer() {
            if (mIsTimerRunning) {
                mTimer.cancel();
            }
        }
    }

    /**
     * timer information utility.
     * 
     * @author NTT DOCOMO, INC.
     */
    private static class HvcTimerInfoUtils {

        /**
         * search data by interval.
         * 
         * @param hvcTimerInfoArray array
         * @param interval interval(search key)
         * @return not null: found data. / null: not found.
         */
        public static HvcTimerInfo search(final List<HvcTimerInfo> hvcTimerInfoArray, final long interval) {
            for (HvcTimerInfo hvcTimerInfo : hvcTimerInfoArray) {
                if (hvcTimerInfo.getInterval() == interval) {
                    return hvcTimerInfo;
                }
            }
            return null;
        }

    }
}
