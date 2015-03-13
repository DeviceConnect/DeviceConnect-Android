/*
 HvcDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.hvc.ble.BleDeviceDetector;
import org.deviceconnect.android.deviceplugin.hvc.ble.BleDeviceDetector.BleDeviceDiscoveryListener;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManager;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManagerUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcHumanDetectProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * HVC Device Service.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceService extends DConnectMessageService {

    /**
     * log tag.
     */
    private static final String TAG = HvcDeviceService.class.getSimpleName();

    /**
     * HVC comm managers(1serviceId,1record).
     */
    private List<HvcCommManager> mHvcCommManagerArray = new ArrayList<HvcCommManager>();

    /**
     * event interval timer information array.<br>
     * - not exist record : stop timer.<br>
     * - exist record : running timer.<br>
     */
    private List<HvcTimerInfo> mIntervalTimerInfoArray = new ArrayList<HvcTimerInfo>();

    /**
     * timeout judget timer information.<br>
     * - null: stop timer. - not null: running timer.
     */
    private HvcTimerInfo mTimeoutJudgeTimer;

    /**
     * HVC found device list.
     */
    private List<BluetoothDevice> mCacheDeviceList = new ArrayList<BluetoothDevice>();

    /**
     * BLE device detector.
     */
    private BleDeviceDetector mDetector;
    
    
    @Override
    public void onCreate() {

        super.onCreate();

        // start HVC device search.
        mDetector = new BleDeviceDetector(getContext());
        if (mDetector.isEnabled()) {
            mDetector.initialize();
            mDetector.setListener(new BleDeviceDiscoveryListener() {
                
                @Override
                public void onDiscovery(final List<BluetoothDevice> devices) {
                    
                    // remove duplicate data or non HVC data.
                    removeDuplicateOrNonHvcData(devices);
                    
                    // store.
                    synchronized (mCacheDeviceList) {
                        mCacheDeviceList.clear();
                        mCacheDeviceList.addAll(devices);
                    }
                }
            });
            mDetector.startScan();
        }
        
        // Initialize EventManager
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // add supported profiles
        addProfile(new HvcHumanDetectProfile());
        
        // start timeout judget timer.
        startTimeoutJudgetTimer();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent == null) {
            return START_STICKY;
        }
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "bluetooth state change.");
                Bundle extras = intent.getExtras();
                int state = extras.getInt(BluetoothAdapter.EXTRA_STATE);
                int prevState = extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE);

                Log.d(TAG, "state:"
                        + (state == BluetoothAdapter.STATE_ON ? "STATE_ON"
                                : state == BluetoothAdapter.STATE_OFF ? "STATE_OFF"
                                        : state == BluetoothAdapter.STATE_TURNING_OFF ? "STATE_TURNING_OFF"
                                                : state == BluetoothAdapter.STATE_TURNING_ON ? "STATE_TURNING_ON"
                                                        : "other"));
                Log.d(TAG, "prevState:"
                        + (prevState == BluetoothAdapter.STATE_ON ? "STATE_ON"
                                : prevState == BluetoothAdapter.STATE_OFF ? "STATE_OFF"
                                        : prevState == BluetoothAdapter.STATE_TURNING_OFF ? "STATE_TURNING_OFF"
                                                : prevState == BluetoothAdapter.STATE_TURNING_ON ? "STATE_TURNING_ON"
                                                        : "other"));
                
                if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    // Bluetooth ON -> OFF
                    
                    // stop scan process.
                    mDetector.stopScan();
                    
                    // remove all detection event and timer.
                    removeAllDetectionEvents();
                    
                } else if (state == BluetoothAdapter.STATE_ON) {
                    // Bluetooth OFF -> ON
                    
                    // start scan process.
                    mDetector.startScan();
                    
                }
                
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HvcSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new HvcServiceInformationProfile(this) {
        };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HvcServiceDiscoveryProfile(this);
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
        List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
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
     * @param sessionKey sessionKey
     */
    public void registerDetectionEvent(final HumanDetectKind detectKind, final HumanDetectRequestParams requestParams,
            final Intent response, final String serviceId, final String sessionKey) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "registerDetectionEvent(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId
                    + " sessionKey:" + sessionKey);
        }

        // search CommManager by serviceId(if not found, add CommManager.).
        HvcCommManager commManager = null;
        synchronized (mHvcCommManagerArray) {
            commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }
        if (commManager == null) {
            
            // search cache bluetooth device.(if not found, not found service error)
            BluetoothDevice bluetoothDevice = searchCacheHvcDevice(serviceId);
            if (bluetoothDevice == null) {
                MessageUtils.setNotFoundServiceError(response);
                return;
            }
            
            // add comm manager.
            commManager = new HvcCommManager(this, serviceId, bluetoothDevice);
            synchronized (mHvcCommManagerArray) {
                mHvcCommManagerArray.add(commManager);
            }
            
        }

        // start interval timer. (if no event with same interval.)
        startIntervalTimer(requestParams.getEvent().getInterval());

        // add event data to commManager.
        commManager.registerDetectEvent(detectKind, requestParams, response, sessionKey);
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
     * @param sessionKey sessionKey
     */
    public void unregisterDetectionEvent(final HumanDetectKind detectKind, final Intent response,
            final String serviceId, final String sessionKey) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "unregisterDetectionEvent(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId
                    + " sessionKey:" + sessionKey);
        }

        // search CommManager by serviceId.
        HvcCommManager commManager = null;
        synchronized (mHvcCommManagerArray) {
            commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }
        if (commManager == null) {
            MessageUtils.setNotFoundServiceError(response, "service id not found");
            return;
        }
        // get event interval.
        Long interval = commManager.getEventInterval(detectKind, sessionKey);
        if (interval == null) {
            MessageUtils.setInvalidRequestParameterError(response, "detectKind and sessionKey pair not found.");
            return;
        }
        
        // unregister
        commManager.unregisterDetectEvent(detectKind, sessionKey);

        // if no event with same interval, stop interval timer (and remove
        // interval timer info record).
        boolean result = HvcCommManagerUtils.checkExistEventByInterval(mHvcCommManagerArray, interval);
        if (!result) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "stop interval timer. interval:" + interval);
            }
            stopIntervalTimer(interval);
        }

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister OnDetection event");
        sendBroadcast(response);
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "doGetDetectionProc(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId);
        }

        // search CommManager by serviceId(if not found, add CommManager.).
        HvcCommManager commManager = null;
        synchronized (mHvcCommManagerArray) {
            commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }
        if (commManager == null) {
            
            // search cache bluetooth device.(if not found, not found service error)
            BluetoothDevice bluetoothDevice = searchCacheHvcDevice(serviceId);
            if (bluetoothDevice == null) {
                MessageUtils.setNotFoundServiceError(response);
                return;
            }
            
            // add comm manager.
            commManager = new HvcCommManager(this, serviceId, bluetoothDevice);
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
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "judgeRetry()");
                        }
                        // retry(Now in the device communication)
                        if (commManagerFinal.checkCommBusy()) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "retry");
                            }
                            return true;
                        }
                        // no retry
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "no retry");
                        }
                        return false;
                    }

                    @Override
                    public void procOnNewThread() {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "proc()");
                        }

                        // get detection process.
                        commManagerFinal.doGetDetectionProc(detectKind, requestParams, response);
                    }

                    @Override
                    public void timeoutProc() {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "timeoutProc()");
                        }

                        // timeout error.
                        MessageUtils.setTimeoutError(response, "GET API timeout.");

                    }
                });
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

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "retryProcOnNewThread() start");
        }

        // new thread start
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
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
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "retryProcOnNewThread() - Retry");
                    }
                    try {
                        sleep(retryInteval);
                    } catch (InterruptedException e) {
                        if (BuildConfig.DEBUG) {
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
    
    /**
     * remove all detection events and timer.
     */
    private void removeAllDetectionEvents() {
        
        // stop all event interval timer.
        for (HvcTimerInfo intervalTimerInfo : mIntervalTimerInfoArray) {
            intervalTimerInfo.stopTimer();
        }
        
        // unregister comm manager all event info.
        for (HvcCommManager commManager : mHvcCommManagerArray) {
            commManager.unregisterAllDetectEvent();
        }
        
        // remove all event info.
        mIntervalTimerInfoArray.clear();
        
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
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "event interval timer proc.");
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
     * start timeout judget timer.
     */
    private void startTimeoutJudgetTimer() {

        mTimeoutJudgeTimer = new HvcTimerInfo(HvcConstants.TIMEOUT_JUDGE_INTERVAL);
        mTimeoutJudgeTimer.startTimer(new TimerTask() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
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
         * @param intervalTimerTtask interval timer task
         */
        public void startTimer(final TimerTask intervalTimerTtask) {
            if (!mIsTimerRunning) {
                // add timertask.
                mTimer.scheduleAtFixedRate(intervalTimerTtask, 0, mInterval);
                mIsTimerRunning = true;
            } else {
                // change timertask.
                mTimer.cancel();
                mTimer.scheduleAtFixedRate(intervalTimerTtask, 0, mInterval);
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
    };

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
