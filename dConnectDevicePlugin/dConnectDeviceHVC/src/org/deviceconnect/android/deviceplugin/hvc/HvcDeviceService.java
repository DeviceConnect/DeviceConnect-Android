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

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManager;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManagerUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
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

import android.content.Intent;
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
     * timeout judge timer interval[msec].
     */
    private static final long TIMEOUT_JUDGE_MSEC = 1 * 60 * 1000;
    
    /**
     * HVC comm managers(1serviceId,1record).<br>
     * - if first serviceId, add record.<br>
     * - if no access long time, remove record. by timer process.<br>
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
     * - null: stop timer.
     * - not null: running timer.
     */
    private HvcTimerInfo mTimeoutJudgeTimer;
    
    
    
    @Override
    public void onCreate() {

        super.onCreate();

        // EventManagerの初期化
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // add supported profiles
        addProfile(new HvcHumanDetectProfile());
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent == null) {
            return START_STICKY;
        }
        String action = intent.getAction();
//        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//                    HvcConnectProfile.PROFILE_NAME,
//                    List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
//                    null,
//                    HvcConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
//
//            for (int i = 0; i < events.size(); i++) {
//                Event event = events.get(i);
//                Intent mIntent = EventManager.createEventMessage(event);
//                HvcConnectProfile.setAttribute(mIntent, HvcConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
//                Bundle bluetoothConnecting = new Bundle();
//                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                HvcConnectProfile.setEnable(bluetoothConnecting, mBluetoothAdapter.isEnabled());
//                HvcConnectProfile.setConnectStatus(mIntent, bluetoothConnecting);
//                getContext().sendBroadcast(mIntent);
//            }
//            return START_STICKY;
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HvcSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new HvcServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HvcServiceDiscoveryProfile();
    }
    
    
    //
    // for human detect profile
    //
    
    /**
     * get comm manager.
     * @param serviceId serviceId
     * @return comm manager
     */
    public HvcCommManager getCommManager(final String serviceId) {
        
        // search CommManager by serviceId.
        HvcCommManager commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        if (commManager == null) {
            // if null, add CommManager.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "getCommManager(). add HvcCommManager");
            }
            commManager = new HvcCommManager(this, serviceId);
            mHvcCommManagerArray.add(commManager);
        }
        
        return commManager;
    }
    
    
    
    // 
    // register detection event.
    // 
    
    /**
     * Human Detect Profile register detection event.<br>
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
        HvcCommManager commManager = getCommManager(serviceId);
        
        // start interval timer. (if no event with same interval.)
        startIntervalTimer(requestParams.getEvent().getInterval());
        
        // start timeout judget timer.
        startTimeoutJudgetTimer();
        
        // add event data to commManager.
        commManager.registerDetectEvent(detectKind, requestParams, response, sessionKey);
        
//        // register
//        if (!commManager.checkRegisterDetectEvent(detectKind, sessionKey)) {
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "registerDetectionEvent(). add registerDetectEvent");
//            }
//            commManager.registerDetectEvent(detectKind, requestParams, sessionKey);
//        }
//        
//        // if timer not start , start timer.
//        if (!mIsIntervalTimerRunning) {
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "registerDetectionEvent(). start timer");
//            }
//            mIntervalTimer = new Timer();
//            mIntervalTimer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    requestDetectAllServices();
//                }
//            }, 0, TIMER_INTERVAL);
//            mIsIntervalTimerRunning = true;
//        }
//        
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
        HvcCommManager commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        if (commManager == null) {
            // no register.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "unregisterDetectionEvent(). commManager not found.");
            }
            MessageUtils.setError(response, HvcHumanDetectProfile.ERROR_VALUE_IS_NULL, "no register event.");
            return;
        }
        // get event interval.
        Long interval = commManager.getEventInterval(detectKind, sessionKey);
        if (interval == null) {
            // event not found.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "unregisterDetectionEvent(). event not found.");
            }
            MessageUtils.setError(response, HvcHumanDetectProfile.ERROR_VALUE_IS_NULL, "no register event.");
            return;
        }
        
        // unregister
        commManager.unregisterDetectEvent(detectKind, sessionKey);
        
        // if has no event commManager, remove commManager.
        if (commManager.getEventCount() <= 0) {
            commManager.destroy();
            HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        }
        
        // if no event with same interval, stop interval timer (and remove interval timer info record).
        if (!HvcCommManagerUtils.checkExistEventByInterval(mHvcCommManagerArray, interval)) {
            stopIntervalTimer(interval);
        }
        
        // if no event, stop timeout judge timer.
        if (!HvcCommManagerUtils.checkExistEvent(mHvcCommManagerArray)) {
            if (mTimeoutJudgeTimer != null) {
                mTimeoutJudgeTimer.stopTimer();
                mTimeoutJudgeTimer = null;
            }
        }
        
        response.putExtra(DConnectMessage.EXTRA_RESULT,
                DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE,
                "Unregister OnDetection event");
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
        HvcCommManager commManager = getCommManager(serviceId);
        
        // get detection process.
        commManager.doGetDetectionProc(detectKind, requestParams, response);
    }
    
    

    /**
     * stop interval timer(and remove record).
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
     * start interval timer(if no event with same interval.).
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
                    // call event process.
                    for (HvcCommManager commManager : mHvcCommManagerArray) {
                        commManager.onEventProc(getContext(), interval);
                    }
                }
            });
            mIntervalTimerInfoArray.add(timerInfo);
        }
    }
    
    /**
     * start timeout judget timer.
     */
    private void startTimeoutJudgetTimer() {
        
        mTimeoutJudgeTimer = new HvcTimerInfo(TIMEOUT_JUDGE_MSEC);
        mTimeoutJudgeTimer.startTimer(new TimerTask() {
            @Override
            public void run() {
                // call timeout judge process.
                for (HvcCommManager commManager : mHvcCommManagerArray) {
                    commManager.onTimeoutJudgeProc();
                }
            }
        });
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
         * @param interval interval[msec]
         */
        public HvcTimerInfo(final long interval) {
            mInterval = interval;
            mTimer = new Timer();
            mIsTimerRunning = false;
        }
        
        /**
         * get interval.
         * @return interval[msec]
         */
        public long getInterval() {
            return mInterval;
        }
        
        /**
         * set interval.
         * @param interval interval[msec]
         */
        public void setInterval(final long interval) {
            mInterval = interval;
        }
        
        /**
         * start timer.
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
        
    };
}
