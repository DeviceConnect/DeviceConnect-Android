/*
 HitoeManager
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.hitoe.BuildConfig;
import org.deviceconnect.android.deviceplugin.hitoe.util.RawDataParseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import jp.ne.docomo.smt.dev.hitoetransmitter.HitoeSdkAPI;
import jp.ne.docomo.smt.dev.hitoetransmitter.sdk.HitoeSdkAPIImpl;

/**
 * This class manages a Hitoe devices.
 * @author NTT DOCOMO, INC.
 */
public class HitoeManager {
    /** Log's tag name. */
    private static final String TAG = "HitoeManager";

    /**
     * Instance of ScheduledExecutorService.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * ScheduledFuture of scan timer.
     */
    private ScheduledFuture<?> mScanTimerFuture;

    /**
     * Defines a delay 1 second at first execution.
     */
    private static final long SCAN_FIRST_WAIT_PERIOD = 30 * 1000;

    /**
     * Defines a period 10 seconds between successive executions.
     */
    private static final long SCAN_WAIT_PERIOD = 20 * 1000;

    /**
     * Stops scanning after 1 second.
     */
    private static final long SCAN_PERIOD = 2000;
    /** Wait 5000 msec. */
    private static final int CONNECTING_RETRY_WAIT = 500;
    /** Connecting retry count. */
    private static final int CONNECTING_RETRY_COUNT = 10;
    /** Device scanning flag. */
    private boolean mScanning;
    /** Device scanning running. */
    private boolean mIsCallbackRunning;

    /** Device scan timestamp. */
    private final Map<HitoeDevice, Long> mNowTimestamps;
    /** Handler. */
    private Handler mHandler = new Handler();

    /**
     * Application context.
     */
    private Context mContext;
    /**
     * Instance of {@link HitoeDBHelper}.
     */
    private HitoeDBHelper mDBHelper;
    /**
     * Hitoe SDK API.
     */
    private HitoeSdkAPI mHitoeSdkAPI;

    // ------------------------------------
    // Listener.
    // ------------------------------------

    /** Hitoe Discovery Listener. */
    private List<OnHitoeConnectionListener> mConnectionListeners;
    /** Notify HeartRate data listener. */
    private OnHitoeHeartRateEventListener mHeartRataListener;
    /** Notify Accleration data listener. */
    private OnHitoeDeviceOrientationEventListener mDeviceOrientationListener;
    /** Notify ECG data listener. */
    private OnHitoeECGEventListener mECGListener;
    /** Notify Pose Estimation data listener. */
    private OnHitoePoseEstimationEventListener mPoseEstimationListener;
    /** Notify Stress Estimation data listener. */
    private OnHitoeStressEstimationEventListener mStressEstimationListener;
    /** Notify Walk state data listener. */
    private OnHitoeWalkStateEventListener mWalkStateListener;


    /** Registered Hitoe devices .*/
    private final List<HitoeDevice> mRegisterDevices;

    /** HeartRate Datas. */
    private final Map<HitoeDevice, HeartRateData> mHRData;
    /** Acceleration Datas. */
    private final Map<HitoeDevice, AccelerationData> mAccelData;
    /** ECG Datas. */
    private final Map<HitoeDevice, HeartRateData> mECGData;
    /** Pose Estimation datas. */
    private final Map<HitoeDevice, PoseEstimationData> mPoseEstimationData;
    /** Stress Estimation datas. */
    private final Map<HitoeDevice, StressEstimationData> mStressEstimationData;
    /** Walk State datas. */
    private final Map<HitoeDevice, WalkStateData> mWalkStateData;

    /** Save data for extended analysis. */
    private ArrayList<TempExData> mListForEx;
    /** Lock for the extension analysis. */
    private ReentrantLock mLockForEx;
    /** Expanded analysis flag. */
    private boolean mFlagForEx;
    /** Acceleration's interval. */
    private long mInterval = 0;
    /** Temporary storage data for pose estimation. */
    private ArrayList<String> mListForPosture;
    /** Lock for pose estimation. */
    private ReentrantLock mLockForPosture;
    /** Temporary storage data for walking state estimation. */
    private ArrayList<String> mListForWalk;
    /** Lock for walking state estimation. */
    private ReentrantLock mLockForWalk;
    /** Temporary storage data for the left and right balance estimation. */
    private ArrayList<String> mListForLRBalance;
    /** Lock for the left and right balance estimation. */
    private ReentrantLock mLockForLRBalance;
    /** Hitoe API Callback. */
    HitoeSdkAPI.APICallback mAPICallback = (apiId, responseId, responseString) -> {

        final StringBuilder messageTextBuilder = new StringBuilder();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "CbCallback:apiId=" + String.valueOf(apiId) + ",responseId="
                    + String.valueOf(responseId) + ",resonseObject="
                    + responseString.replace(HitoeConstants.BR, HitoeConstants.VB));
        }
        switch (apiId) {
            case HitoeConstants.API_ID_GET_AVAILABLE_SENSOR:
                notifyDiscoveryHitoeDevice(responseId, responseString);
                break;
            case HitoeConstants.API_ID_CONNECT:
                notifyConnectHitoeDevice(responseId, responseString);
                break;
            case HitoeConstants.API_ID_DISCONNECT:
                // disconnect sensor
                try{

                    mLockForEx.lock();

                    mListForEx.clear();
                    mFlagForEx = false;
                }finally {

                    mLockForEx.unlock();
                }
                break;
            case HitoeConstants.API_ID_GET_AVAILABLE_DATA:
                notifyAvailableData(responseId, responseString);
                break;
            case HitoeConstants.API_ID_ADD_RECIVER:
                notifyAddReceiver(responseId, responseString);
                break;
            case HitoeConstants.API_ID_REMOVE_RECEIVER:

                notifyRemoveReceiver(responseId, responseString);
                break;
            case HitoeConstants.API_ID_GET_STATUS:
                break;
            default:
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "etc state");
                }
                break;
        }
    };




    /**
     * Data receiver.
     */
    HitoeSdkAPI.DataReceiverCallback mDataReceiverCallback = new HitoeSdkAPI.DataReceiverCallback() {
        @Override
        public void onDataReceive(final String connectionId, final int responseId,
                                  final String dataKey, final String rawData) {

            int pos = getPosForConnectionId(connectionId);
            if (pos == -1) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "no connectionId");
                }
                return;
            }
            HitoeDevice receiveDevice = mRegisterDevices.get(pos);
            if (receiveDevice.getSessionId() == null) {
                return;
            }

            if (dataKey.equals("raw.ecg")) {
                extractHealth(HeartData.HeartRateType.ECG, rawData, receiveDevice);
            } else if (dataKey.equals("raw.acc")) {
                analyzeAccelerationData(rawData, receiveDevice);
                AccelerationData currentAccel = mAccelData.get(receiveDevice);
                if (currentAccel == null) {
                    currentAccel = new AccelerationData();
                }
                currentAccel = RawDataParseUtils.parseAccelerationData(currentAccel, rawData);
                mAccelData.put(receiveDevice, currentAccel);
            } else if (dataKey.equals("raw.rri")) {
                extractHealth(HeartData.HeartRateType.RRI, rawData, receiveDevice);
            } else if (dataKey.equals("raw.bat")) {
                extractBattery(rawData, receiveDevice);
            } else if (dataKey.equals("raw.hr")) {
                extractHealth(HeartData.HeartRateType.Rate, rawData, receiveDevice);
            } else if (dataKey.equals("ba.freq_domain")) {
                parseFreqDomain(receiveDevice, rawData);
            } else if (dataKey.equals("ex.stress")) {
                StressEstimationData stress = RawDataParseUtils.parseStressEstimation(rawData);
                mStressEstimationData.put(receiveDevice, stress);
            } else if (dataKey.equals("ex.posture")) {
                PoseEstimationData pose = RawDataParseUtils.parsePoseEstimation(rawData);
                mPoseEstimationData.put(receiveDevice, pose);
            } else if (dataKey.equals("ex.walk")) {
                WalkStateData walk = mWalkStateData.get(receiveDevice);
                if (walk == null) {
                    walk = new WalkStateData();
                }
                walk = RawDataParseUtils.parseWalkState(walk, rawData);
                mWalkStateData.put(receiveDevice, walk);
            } else if (dataKey.equals("ex.lr_balance")) {
                WalkStateData walk = mWalkStateData.get(receiveDevice);
                if (walk == null) {
                    walk = new WalkStateData();
                }
                walk = RawDataParseUtils.parseWalkStateForBalance(walk, rawData);
                mWalkStateData.put(receiveDevice, walk);
            }

            if (dataKey.startsWith(HitoeConstants.EX_DATA_PREFFIX)) {
                // Expanded analysis discard the connection
                receiveDevice.removeConnectionId(connectionId);
            } else {
                // Perform any extension
                //Do not run if it is already running
                TempExData exData = null;
                try {

                    mLockForEx.lock();

                    if (!mFlagForEx && mListForEx.size() > 0) {
                        mFlagForEx = true;
                        exData = mListForEx.get(0);
                        mListForEx.remove(0);
                    }
                } finally {

                    mLockForEx.unlock();
                }
                if (exData != null) {
                   addExReceiverProcess(pos, exData);
                }
            }

            notifyListeners(receiveDevice);
        }
    };




    /**
     * Constructor.
     *
     * @param context application context
     */
    public HitoeManager(final Context context) {
        mContext = context;
        mDBHelper = new HitoeDBHelper(context);
        mListForEx = new ArrayList<>();
        mLockForEx = new ReentrantLock();
        mRegisterDevices = Collections.synchronizedList(
                new ArrayList<HitoeDevice>());
        mHRData = new ConcurrentHashMap<>();
        mECGData = new ConcurrentHashMap<>();
        mPoseEstimationData = new ConcurrentHashMap<>();
        mStressEstimationData = new ConcurrentHashMap<>();
        mWalkStateData = new ConcurrentHashMap<>();
        mAccelData = new ConcurrentHashMap<>();
        mConnectionListeners = new ArrayList<>();
        mListForPosture = new ArrayList<>();
        mLockForPosture = new ReentrantLock();
        mListForWalk = new ArrayList<>();
        mLockForWalk = new ReentrantLock();
        mListForLRBalance = new ArrayList<>();
        mLockForLRBalance = new ReentrantLock();
        mListForEx = new ArrayList<>();
        mLockForEx = new ReentrantLock();
        mNowTimestamps = new ConcurrentHashMap<>();
        mHitoeSdkAPI = HitoeSdkAPIImpl.getInstance(context);
        mHitoeSdkAPI.setAPICallback(mAPICallback);
        readHitoeDeviceForDB();

    }

    // ------------------------------------
    // Public Method
    // ------------------------------------
    /**
     * Set Hitoe Connection Listener.
     * @param l listener
     */
    public void addHitoeConnectionListener(final OnHitoeConnectionListener l) {
        mConnectionListeners.add(l);
    }

    /**
     * Remove Hitoe Connection listener.
     * @param l connection listener
     */
    public void removeHitoeConnectionListener(final OnHitoeConnectionListener l) {
        mConnectionListeners.remove(l);
    }
    /**
     * Set Hitoe HeartRate Listener.
     * @param l listener
     */
    public void setHitoeHeartRateEventListener(final OnHitoeHeartRateEventListener l) {
        mHeartRataListener = l;
    }
    /**
     * Set Hitoe Acceleration Listener.
     * @param l listener
     */
    public void setHitoeDeviceOrientationEventListener(final OnHitoeDeviceOrientationEventListener l) {
        mDeviceOrientationListener = l;
    }

    /**
     * Set Hitoe ECG listener.
     * @param l listener
     */
    public void setHitoeECGEventListener(final OnHitoeECGEventListener l) {
        mECGListener = l;
    }

    /**
     * Set Hitoe Pose Estimation listener.
     * @param l listener
     */
    public void setHitoePoseEstimationEventListener(final OnHitoePoseEstimationEventListener l) {
        mPoseEstimationListener = l;
    }

    /**
     * Set Hitoe Stress Estimation listener.
     * @param l listener
     */
    public void setHitoeStressEstimationEventListener(final OnHitoeStressEstimationEventListener l) {
        mStressEstimationListener = l;
    }

    /**
     * Set Hitoe Walk state listener.
     * @param l listener
     */
    public void setHitoeWalkStateEventListener(final OnHitoeWalkStateEventListener l) {
        mWalkStateListener = l;
    }

    /**
     * Gets the list of BLE device that was registered to automatic connection.
     *
     * @return list of BLE device
     */
    public List<HitoeDevice> getRegisterDevices() {
        return mRegisterDevices;
    }

    /**
     * Read device info.
     */
    public void readHitoeDeviceForDB() {
        List<HitoeDevice> list = mDBHelper.getHitoeDevices(null);
        for (int i = 0; i < list.size(); i++) {
            HitoeDevice device = list.get(i);
            if (mRegisterDevices.size() > 0) {
                if (!containsDevice(device.getId())) {
                    mRegisterDevices.add(device);
                }
            } else {
                mRegisterDevices.add(device);
            }
        }
    }

    /**
     * Get mRegisterDevice for service id.
     * @param serviceId service Id
     * @return Hitoe Device object
     */
    public HitoeDevice getHitoeDeviceForServiceId(final String serviceId) {
        for (int i = 0; i < mRegisterDevices.size(); i++) {
            if (mRegisterDevices.get(i).getId() != null) {
                if (mRegisterDevices.get(i).getId().equals(serviceId)) {
                    return mRegisterDevices.get(i);
                }
            }
        }
        return null;
    }
    /**
     * Get HeartRateData.
     * @param serviceId index id
     * @return HeartRateData
     */
    public HeartRateData getHeartRateData(final String serviceId) {
        int pos = getPosForServiceId(serviceId);
        if (pos == -1) {
            return null;
        }
        return mHRData.get(mRegisterDevices.get(pos));
    }
    /**
     * Get ECG Data.
     * @param serviceId index id
     * @return ECGData
     */
    public HeartRateData getECGData(final String serviceId) {
        int pos = getPosForServiceId(serviceId);
        if (pos == -1) {
            return null;
        }
        return mECGData.get(mRegisterDevices.get(pos));
    }
    /**
     * Get Stress Estimation Data.
     * @param serviceId index id
     * @return StressEstimationData
     */
    public StressEstimationData getStressEstimationData(final String serviceId) {
        int pos = getPosForServiceId(serviceId);
        if (pos == -1) {
            return null;
        }
        return mStressEstimationData.get(mRegisterDevices.get(pos));
    }
    /**
     * Get Pose Estimation Data.
     * @param serviceId index id
     * @return Pose Estimation Data
     */
    public PoseEstimationData getPoseEstimationData(final String serviceId) {
        int pos = getPosForServiceId(serviceId);
        if (pos == -1) {
            return null;
        }
        return mPoseEstimationData.get(mRegisterDevices.get(pos));
    }
    /**
     * Get Walk State Data.
     * @param serviceId index id
     * @return Walk State data
     */
    public WalkStateData getWalkStateData(final String serviceId) {
        int pos = getPosForServiceId(serviceId);
        if (pos == -1) {
            return null;
        }
        return mWalkStateData.get(mRegisterDevices.get(pos));
    }
    /**
     * Get AccelerationData.
     * @param serviceId index id
     * @return AccelerationData
     */
    public AccelerationData getAccelerationData(final String serviceId) {
        int pos = getPosForServiceId(serviceId);
        if (pos == -1) {
            return null;
        }
        return mAccelData.get(mRegisterDevices.get(pos));
    }

    /**
     * Stats the HitoeManager.
     */
    public void start() {
        synchronized (mRegisterDevices) {
            for (int i = 0; i < mRegisterDevices.size(); i++) {
                HitoeDevice device = mRegisterDevices.get(i);
                if (device.isRegisterFlag()) {
                    connectHitoeDevice(device);
                }
            }
        }

    }
    /**
     * Stops the HitoeManager.
     */
    public void stop() {
        for (int i = 0; i < mRegisterDevices.size(); i++) {
            mHitoeSdkAPI.disconnect(mRegisterDevices.get(i).getSessionId());
            mRegisterDevices.get(i).setRegisterFlag(false);
            mDBHelper.updateHitoeDevice(mRegisterDevices.get(i));
            mRegisterDevices.get(i).setSessionId(null);
        }
        scanHitoeDevice(false);
    }
    /**
     * Discovery hitoe device.
     */
    public void discoveryHitoeDevices() {
        StringBuilder paramStringBuilder = new StringBuilder();
        paramStringBuilder.append("search_time=")
                .append(String.valueOf(HitoeConstants.GET_AVAILABLE_SENSOR_PARAM_SEARCH_TIME));
        mHitoeSdkAPI.getAvailableSensor(HitoeConstants.GET_AVAILABLE_SENSOR_DEVICE_TYPE, paramStringBuilder.toString());

        if (mRegisterDevices.size() > 0) {
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                if (l != null) {
                    l.onDiscovery(mRegisterDevices);
                }
            }
        }
    }


    /**
     * Connect to Hitoe Device by address.
     *
     * @param device device for hitoe device
     */
    public void connectHitoeDevice(final HitoeDevice device) {
        mExecutor.submit(() -> {
            if (device == null || device.getPinCode() == null) {
                return;
            }


            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append("disconnect_retry_time=" + HitoeConstants.CONNECT_DISCONNECT_RETRY_TIME);
            if (paramBuilder.length() > 0) {
                paramBuilder.append(HitoeConstants.BR);
            }
            paramBuilder.append("disconnect_retry_count=" + HitoeConstants.CONNECT_DISCONNECT_RETRY_COUNT);
            if (paramBuilder.length() > 0) {
                paramBuilder.append(HitoeConstants.BR);
            }
            paramBuilder.append("nopacket_retry_time=" + HitoeConstants.CONNECT_NOPACKET_RETRY_TIME);
            if (paramBuilder.length() > 0) {
                paramBuilder.append(HitoeConstants.BR);
            }
            paramBuilder.append("pincode=");
            paramBuilder.append(device.getPinCode());
            String param = paramBuilder.toString();
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice remoteDevice = adapter.getRemoteDevice(device.getId());
            if (remoteDevice.getName() == null) {
                // If RemoteDevice is Null, Discovery process needs to be done once.
                // Retry 10 times and continue processing when RemoteDevice is found.
                discoveryHitoeDevices();
                int i = 0;
                for (i = 0; i < CONNECTING_RETRY_COUNT; i++) {
                    try {
                        Thread.sleep(CONNECTING_RETRY_WAIT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    remoteDevice = adapter.getRemoteDevice(device.getId());
                    if (remoteDevice.getName() != null) {
                        break;
                    }
                }
                if (i == CONNECTING_RETRY_COUNT && remoteDevice.getName() == null) {
                    return;
                }
            }
            mHitoeSdkAPI.connect(device.getType(), device.getId(), device.getConnectMode(), param);
            device.setResponseId(HitoeConstants.RES_ID_SENSOR_CONNECT);
            mDBHelper.addHitoeDevice(device);
            mStressEstimationData.put(device, new StressEstimationData());
            for (int i = 0; i < mRegisterDevices.size(); i++) {
                if (mRegisterDevices.get(i).getId().equals(device.getId())) {
                    mRegisterDevices.set(i, device);
                } else {

                    mRegisterDevices.get(i).setResponseId(HitoeConstants.RES_ID_SENSOR_DISCONECT_NOTICE);
                }
            }
        });
    }

    /**
     * Disconnect hitoe device.
     * @param device hitoe device
     */
    public void disconnectHitoeDevice(final HitoeDevice device) {
        mExecutor.submit(() -> {
            HitoeDevice current = getHitoeDeviceForServiceId(device.getId());
            int res = mHitoeSdkAPI.disconnect(current.getSessionId());
            current.setRegisterFlag(false);
            current.setSessionId(null);
            mDBHelper.updateHitoeDevice(current);
            if (!existConnected()) {
                scanHitoeDevice(false);
            }
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                if (l != null) {
                    l.onDisconnected(res, device);
                }
            }
        });

    }

    /**
     * Delete hitoe device info for db.
     * @param device hitoe device
     */
    public void deleteHitoeDevice(final HitoeDevice device) {

        mDBHelper.removeHitoeDevice(device);

        for (int i = 0; i < mRegisterDevices.size(); i++) {
            if (mRegisterDevices.get(i).getId().equals(device.getId())) {
                HitoeDevice d = mRegisterDevices.remove(i);
                for (OnHitoeConnectionListener l: mConnectionListeners) {
                    if (l != null) {
                        l.onDeleted(d);
                    }
                }
            }
        }
    }
    /**
     * Tests whether this mConnectedDevices contains the address.
     * @param id address will be checked
     * @return true if address is an element of mConnectedDevices, false otherwise
     */
    public boolean containConnectedHitoeDevice(final String id) {
        synchronized (mRegisterDevices) {
            for (HitoeDevice d : mRegisterDevices) {
                if (d.getId().equals(id) && d.getSessionId() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    // Private method
    // ------------------------------------
    // Notifyメソッド群
    // ------------------------------------

    /**
     *  Notify for found Hitoe Devices.
     * @param responseId Response id
     * @param responseString Response String
     */
    private void notifyDiscoveryHitoeDevice(final int responseId, final String responseString) {
        if (responseId != HitoeConstants.RES_ID_SUCCESS || responseString == null) {
            return;
        }

        String[] sensorList = responseString.split(HitoeConstants.BR, -1);
        List<HitoeDevice> pins = mDBHelper.getHitoeDevices(null);
        for (int i = 0; i < sensorList.length; i++) {
            String sensorStr = sensorList[i].trim();
            if (sensorStr.length() == 0) {
                continue;
            }
            if (!sensorStr.contains("memory_setting") && !sensorStr.contains("memory_get")) {
                HitoeDevice device = new HitoeDevice(sensorStr);
                if (mRegisterDevices.size() == 0) {
                    mRegisterDevices.add(device);
                }
                if (!containsDevice(device.getId())) {
                    mRegisterDevices.add(device);
                }
            }
        }
        for (HitoeDevice pin : pins) {
            for (HitoeDevice register: mRegisterDevices) {
                if (register.getId().equals(pin.getId())) {
                    register.setPinCode(pin.getPinCode());
                    register.setRegisterFlag(pin.isRegisterFlag());
                }
            }
        }
        for (OnHitoeConnectionListener l: mConnectionListeners) {
            if (l != null) {
                l.onDiscovery(mRegisterDevices);
            }
        }
    }

    /**
     * Notify for connected hitoe devices.
     * @param responseId Response id
     * @param responseString Response string
     */
    private void notifyConnectHitoeDevice(final int responseId, final String responseString) {
        int pos = getCurrentPos(responseId);
        if (pos == -1) {
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                if (l != null) {
                    l.onConnectFailed(null);
                }
            }
            return;
        }
        if (responseId == HitoeConstants.RES_ID_SENSOR_DISCONECT_NOTICE) {
            // 切断の場合もフラグを落とす
            try{

                mLockForEx.lock();

                mListForEx.clear();
                mFlagForEx = false;
            }finally {

                mLockForEx.unlock();
            }
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                if (l != null) {

                    l.onConnectFailed(mRegisterDevices.get(pos));
                }
            }
            return;
        } else if (responseId == HitoeConstants.RES_ID_SENSOR_CONNECT_NOTICE) {
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                l.onConnected(mRegisterDevices.get(pos));
            }
            return;
        } else if (responseId != HitoeConstants.RES_ID_SENSOR_CONNECT) {
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                if (l != null) {

                    l.onConnectFailed(mRegisterDevices.get(pos));
                }
            }
            return;
        }
        mRegisterDevices.get(pos).setSessionId(responseString);
        mRegisterDevices.get(pos).setRegisterFlag(true);
        mDBHelper.updateHitoeDevice(mRegisterDevices.get(pos));
        mHitoeSdkAPI.getAvailableData(mRegisterDevices.get(pos).getSessionId());
        mRegisterDevices.get(pos).setResponseId(HitoeConstants.RES_ID_SUCCESS);
        for (OnHitoeConnectionListener l: mConnectionListeners) {
            if (l != null) {
                l.onConnected(mRegisterDevices.get(pos));
            }
        }
    }

    /**
     * Notify AvailableData.
     * @param responseId response id
     * @param responseString Response string
     */
    private void notifyAvailableData(final int responseId, final String responseString) {
        if (responseId != HitoeConstants.RES_ID_SUCCESS || responseString == null) {
            return;
        }
        int pos = getCurrentPos(responseId);
        if (pos == -1) {
            return;
        }
        mRegisterDevices.get(pos).setAvailableData(responseString);
        List<String> keyList = mRegisterDevices.get(pos).getAvailableRawDataList();

        StringBuilder paramStringBuilder = new StringBuilder();
        String[] keys = new String[keyList.size()];
        String paramString;

        for (int i = 0; i < keyList.size(); i++) {

            keys[i] = keyList.get(i);

            if (keyList.get(i).equals("raw.ecg")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("raw.ecg_interval=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_ECG_SAMPLING_INTERVAL));
            } else if (keyList.get(i).equals("raw.acc")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("raw.acc_interval=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_ACC_SAMPLING_INTERVAL));
            } else if (keyList.get(i).equals("raw.rri")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("raw.rri_interval=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_RRI_SAMPLING_INTERVAL));
            } else if (keyList.get(i).equals("raw.hr")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("raw.hr_interval=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_HR_SAMPLING_INTERVAL));
            } else if (keyList.get(i).equals("raw.bat")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("raw.bat_interval=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BAT_SAMPLING_INTERVAL));
            }
        }

        paramString = paramStringBuilder.toString();
        mHitoeSdkAPI.addReceiver(mRegisterDevices.get(pos).getSessionId(),
                                        keys, mDataReceiverCallback, paramString, null);
        scanHitoeDevice(true);
    }

    /**
     * Notify add receiver.
     * @param responseId response id
     * @param responseString response string
     */
    private void notifyAddReceiver(final int responseId, final String responseString) {
        if (responseId != HitoeConstants.RES_ID_SUCCESS || responseString == null) {
            mFlagForEx = false;
            return;
        }
        int pos = getCurrentPos(responseId);
        if (pos == -1) {
            return;
        }
        mRegisterDevices.get(pos).setConnectionId(responseString);
        if (responseString.startsWith(HitoeConstants.RAW_CONNECTION_PREFFIX)) {
            addBaReceiverProcess(pos);
        } else {
            TempExData exData = null;
            try {
                mLockForEx.lock();

                if (mListForEx.size() > 0) {
                    exData = mListForEx.get(0);
                    mListForEx.remove(0);
                } else {
                    mFlagForEx = false;
                }

            } finally {

                mLockForEx.unlock();
            }
            if (exData != null) {
                addExReceiverProcess(pos, exData);
            }
        }
    }

    /**
     * Notify Remove receiver.
     * @param responseId response id
     * @param responseString response string
     */
    private void notifyRemoveReceiver(final int responseId, final String responseString) {
        if (responseId != HitoeConstants.RES_ID_SUCCESS || responseString == null) {
            return;
        }
        int pos = getCurrentPos(responseId);
        if (pos == -1) {
            return;
        }
        mRegisterDevices.get(pos).setRegisterFlag(false);
        mRegisterDevices.get(pos).removeConnectionId(responseString);
        mDBHelper.updateHitoeDevice(mRegisterDevices.get(pos));

        if (responseString.startsWith(HitoeConstants.BA_CONNECTION_PREFFIX)) {
            removeRawReceiverProcess(mRegisterDevices.get(pos).getRawConnectionId());
        } else if (responseString.startsWith(HitoeConstants.RAW_CONNECTION_PREFFIX)) {
            disconnectProcess(pos);
        }
    }

    /**
     * Notify Listeners.
     * @param receiveDevice now receive device
     */
    private void notifyListeners(final HitoeDevice receiveDevice) {
        if (mHeartRataListener != null) {
            mHeartRataListener.onReceivedData(receiveDevice, mHRData.get(receiveDevice));
        }
        if (mECGListener != null) {
            mECGListener.onReceivedData(receiveDevice, mHRData.get(receiveDevice));
        }
        if (mPoseEstimationListener != null) {
            mPoseEstimationListener.onReceivedData(receiveDevice, mPoseEstimationData.get(receiveDevice));
        }
        if (mStressEstimationListener != null) {
            mStressEstimationListener.onReceivedData(receiveDevice, mStressEstimationData.get(receiveDevice));
        }
        if (mWalkStateListener != null) {
            mWalkStateListener.onReceivedData(receiveDevice, mWalkStateData.get(receiveDevice));
        }
        if (mDeviceOrientationListener != null) {
            mDeviceOrientationListener.onReceivedData(receiveDevice, mAccelData.get(receiveDevice));
        }
        if (mECGListener != null) {
            mECGListener.onReceivedData(receiveDevice, mECGData.get(receiveDevice));
        }
        if (mStressEstimationListener != null) {
            mStressEstimationListener.onReceivedData(receiveDevice, mStressEstimationData.get(receiveDevice));
        }
        if (mPoseEstimationListener != null) {
            mPoseEstimationListener.onReceivedData(receiveDevice, mPoseEstimationData.get(receiveDevice));
        }
    }




    /**
     * Add ba Receiver process.
     * @param pos device pos
     */
    private void addBaReceiverProcess(final int pos) {
        List<String> keyList = mRegisterDevices.get(pos).getAvailableBaDataList();

        StringBuilder paramStringBuilder = new StringBuilder();
        String[] keys = new String[keyList.size()];
        String paramString;

        if (keyList.size() == 0) {
            return;
        }

        for (int i = 0; i < keyList.size(); i++) {

            keys[i] = keyList.get(i);

            if (keyList.get(i).equals("ba.extracted_rri")) {

                if (paramStringBuilder.indexOf("ba.sampling_interval") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.sampling_interval=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL));
                }
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.ecg_threshhold=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_ECG_THRESHHOLD));
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.ecg_skip_count=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SKIP_COUNT));

            } else if (keyList.get(i).equals("ba.cleaned_rri")) {

                if (paramStringBuilder.indexOf("ba.sampling_interval") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.sampling_interval=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL));
                }
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.rri_min=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_MIN));
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.rri_max=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_MAX));
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.sample_count=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLE_COUNT));
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.rri_input=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_INPUT));
            } else if (keyList.get(i).equals("ba.interpolated_rri")) {

                if (paramStringBuilder.indexOf("ba.freq_sampling_interval") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_interval=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL));
                }
                if (paramStringBuilder.indexOf("ba.freq_sampling_window") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_window=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW));
                }
                if (paramStringBuilder.indexOf("ba.rri_sampling_rate") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.rri_sampling_rate=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE));
                }
            } else if (keyList.get(i).equals("ba.freq_domain")) {

                if (paramStringBuilder.indexOf("ba.freq_sampling_interval") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_interval=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL));
                }
                if (paramStringBuilder.indexOf("ba.freq_sampling_window") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_window=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW));
                }
                if (paramStringBuilder.indexOf("ba.rri_sampling_rate") == -1) {
                    if (paramStringBuilder.length() > 0
                            && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.rri_sampling_rate=")
                            .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE));
                }
            } else if (keyList.get(i).equals("ba.time_domain")) {
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.time_sampling_interval=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_INTERVAL));
                if (paramStringBuilder.length() > 0
                        && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ba.time_sampling_window=")
                        .append(String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_WINDOW));
            }
        }

        paramString = paramStringBuilder.toString();

        int resId = mHitoeSdkAPI.addReceiver(mRegisterDevices.get(pos).getSessionId(),
                keys, mDataReceiverCallback, paramString, null);
        mRegisterDevices.get(pos).setResponseId(resId);
    }

    /**
     *  Register Ex receiver.
     *  @param pos device pos
     *  @param exData ex data
     */
    private void addExReceiverProcess(final int pos, final TempExData exData) {

        String keyString = exData.getKey();
        ArrayList<String> dataList = exData.getDataList();

        if (!mRegisterDevices.get(pos).getAvailableExDataList().contains(keyString)) {

            try {
                mLockForEx.lock();
                mFlagForEx = false;

            } finally {

                mLockForEx.unlock();
            }
            return;
        }

        int responseId;

        String[] keys = new String[1];
        keys[0] = keyString;

        StringBuilder paramStringBuilder = new StringBuilder();
        StringBuilder dataStringBuilder = new StringBuilder();
        String paramString;
        String dataString;

        for (int i = 0; i < dataList.size(); i++) {

            if (dataStringBuilder.length() > 0) {

                dataStringBuilder.append(HitoeConstants.BR);
            }
            dataStringBuilder.append(dataList.get(i));
        }
        if (keyString.equals("ex.posture")) {

            if (paramStringBuilder.length() > 0) {

                paramStringBuilder.append(HitoeConstants.BR);
            }
            paramStringBuilder.append("ex.acc_axis_xyz=")
                    .append(HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ);
            if (paramStringBuilder.length() > 0) {

                paramStringBuilder.append(HitoeConstants.BR);
            }
            paramStringBuilder.append("ex.posture_window=")
                    .append(HitoeConstants.ADD_RECEIVER_PARAM_EX_POSTURE_WINDOW);
        } else if (keyString.equals("ex.walk")) {
            if (paramStringBuilder.length() > 0) {

                paramStringBuilder.append(HitoeConstants.BR);
            }
            paramStringBuilder.append("ex.acc_axis_xyz=")
                    .append(HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ);
            if (paramStringBuilder.length() > 0) {

                paramStringBuilder.append(HitoeConstants.BR);
            }
            paramStringBuilder.append("ex.walk_stride=")
                    .append(HitoeConstants.ADD_RECEIVER_PARAM_EX_WALK_STRIDE);
            if (paramStringBuilder.length() > 0) {

                paramStringBuilder.append(HitoeConstants.BR);
            }
            paramStringBuilder.append("ex.run_stride_cof=")
                    .append(HitoeConstants.ADD_RECEIVER_PARAM_EX_RUN_STRIDE_COF);
            if (paramStringBuilder.length() > 0) {

                paramStringBuilder.append(HitoeConstants.BR);
            }
            paramStringBuilder.append("ex.run_stride_int=")
                    .append(HitoeConstants.ADD_RECEIVER_PARAM_EX_RUN_STRIDE_INT);
        } else if (keyString.equals("ex.lr_balance")) {

            if (paramStringBuilder.length() > 0) {

                paramStringBuilder.append(HitoeConstants.BR);
            }
            paramStringBuilder.append("ex.acc_axis_xyz=")
                    .append(HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ);
        }

        paramString = paramStringBuilder.toString();
        dataString = dataStringBuilder.toString();
        mHitoeSdkAPI.removeReceiver(null);
        responseId = mHitoeSdkAPI.addReceiver(null, keys, mDataReceiverCallback, paramString, dataString);
        if (responseId != HitoeConstants.RES_ID_SUCCESS) {
            try {
                mLockForEx.lock();
                mFlagForEx = false;

            } finally {

                mLockForEx.unlock();
            }
        }
    }

    /**
     * Remove raw receiver process.
     * @param rawConnectionId raw connection id
     */
    private void removeRawReceiverProcess(final String rawConnectionId) {
        if (rawConnectionId == null) {
            return;
        }
        mHitoeSdkAPI.removeReceiver(rawConnectionId);
    }

    /**
     * Disconnect Hitoe device.
     * @param pos hitoe devie position
     */
    private void disconnectProcess(final int pos) {
        if (pos == -1) {
            return;
        }
        HitoeDevice device = mRegisterDevices.get(pos);

        if (device.getSessionId() == null) {
            return;
        }
        mRegisterDevices.get(pos).setSessionId(null);
        mRegisterDevices.get(pos).setRegisterFlag(false);
        mDBHelper.updateHitoeDevice(mRegisterDevices.get(pos));
        mHitoeSdkAPI.disconnect(device.getSessionId());
        if (mRegisterDevices.size() == 0) {
            scanHitoeDevice(false);
        }
    }

    /**
     * Get Current register device.
     * @param responseId current response id
     * @return current register pos
     */
    private int getCurrentPos(final int responseId) {
        int pos = -1;

        for (int i = 0; i < mRegisterDevices.size(); i++) {

           if (mRegisterDevices.get(i).getResponseId() == responseId) {
                pos = i;
                break;
            }
        }

        return pos;
    }

    /**
     * Is exist register device.
     * @param id service id
     * @return true:exist false: non exist
     */
    private boolean containsDevice(final String id) {
        boolean isRegister = false;
        for (int i = 0; i < mRegisterDevices.size(); i++) {
            if (mRegisterDevices.get(i).getId().equals(id)) {
                isRegister = true;
            }
        }
        return isRegister;
    }

    /**
     * Get mRegisterDevice's Position for Connection id.
     * @param connectionId connection Id
     * @return position
     */
    private int getPosForConnectionId(final String connectionId) {
        int pos = -1;
        for (int i = 0; i < mRegisterDevices.size(); i++) {
            if (mRegisterDevices.get(i).getRawConnectionId() != null) {
                 if (mRegisterDevices.get(i).getRawConnectionId().equals(connectionId)) {
                     pos = i;
                     break;
                 }

            }
            if (mRegisterDevices.get(i).getBaConnectionId() != null) {
                if (mRegisterDevices.get(i).getBaConnectionId().equals(connectionId)) {
                    pos = i;
                    break;
                }
            }
            if (mRegisterDevices.get(i).getExConnectionList().size() > 0) {
                for (int j = 0; j < mRegisterDevices.get(i).getExConnectionList().size(); j++) {
                    String exConnectionId = mRegisterDevices.get(i).getExConnectionList().get(j);
                    if (exConnectionId == null) {
                        continue;
                    }
                    if (exConnectionId.equals(connectionId)) {
                        pos = i;
                        break;
                    }
                }
            }
        }
        return pos;
    }

    /**
     * Get mRegisterDevice's Position for service id.
     * @param serviceId service Id
     * @return position
     */
    private int getPosForServiceId(final String serviceId) {
        int pos = -1;
        for (int i = 0; i < mRegisterDevices.size(); i++) {
            if (mRegisterDevices.get(i).getId() != null) {
                if (mRegisterDevices.get(i).getId().equals(serviceId)) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }

    /**
     *  周波数領域特徴量データをパースする.
     *  @param receiveDevice ReceiveDevice
     * @param data 周波数領域特徴量データ
     */
    private void parseFreqDomain(final HitoeDevice receiveDevice, final String data) {

        String[] lineList = data.split(HitoeConstants.BR);
        ArrayList<String> stressInputList = new ArrayList<String>();

        if (receiveDevice.getAvailableExDataList().contains("ex.stress")) {
            for (int i = 0; i < lineList.length; i++) {
                stressInputList.add(lineList[i]);
            }

            try {
                mLockForEx.lock();
                mListForEx.add(new TempExData("ex.stress", stressInputList));
            } finally {
                mLockForEx.unlock();
            }
        }
    }
    /**
     * Extract health data.
     * @param type Health data type
     * @param rawData raw data
     * @param receiveDevice Hitoe device
     */
    private void extractHealth(final HeartData.HeartRateType type,
                               final String rawData, final HitoeDevice receiveDevice) {
        HeartRateData currentHeartRate = mHRData.get(receiveDevice);
        if (currentHeartRate == null) {
            currentHeartRate = new HeartRateData();
        }
        if (type == HeartData.HeartRateType.Rate) {
            HeartData heart = RawDataParseUtils.parseHeartRate(rawData);
            currentHeartRate.setHeartRate(heart);
            mHRData.put(receiveDevice, currentHeartRate);
        } else if (type == HeartData.HeartRateType.RRI) {
            HeartData rri = RawDataParseUtils.parseRRI(rawData);
            currentHeartRate.setRRInterval(rri);
            mHRData.put(receiveDevice, currentHeartRate);
        } else if (type == HeartData.HeartRateType.EnergyExpended) {
            HeartData energy = RawDataParseUtils.parseEnergyExpended(rawData);
            currentHeartRate.setEnergyExpended(energy);
            mHRData.put(receiveDevice, currentHeartRate);
        } else if (type == HeartData.HeartRateType.ECG) {
            HeartData ecg = RawDataParseUtils.parseECG(rawData);
            currentHeartRate.setECG(ecg);
            mECGData.put(receiveDevice, currentHeartRate);

        }
    }

    /**
     * Extract Battery data.
     * @param rawData raw data
     * @param receiveDevice Hitoe device
     */
    private void extractBattery(final String rawData, final HitoeDevice receiveDevice) {
        String[] lineList = rawData.split(HitoeConstants.BR);
        String levelString = lineList[lineList.length - 1];
        String[] level = levelString.split(",", -1);

        TargetDeviceData current = RawDataParseUtils.parseDeviceData(receiveDevice,
                Float.parseFloat(level[1]));
        HeartRateData currentHeartRate = mHRData.get(receiveDevice);
        if (currentHeartRate == null) {
            currentHeartRate = new HeartRateData();
        }
        currentHeartRate.setDevice(current);
        mHRData.put(receiveDevice, currentHeartRate);
    }
    /**
     * Analyze Acceleration data.
     * Get Posture Data, Walk State data, LR Balance data.
     * @param rawData raw data
     * @param receiveDevice receive device
     */
    private void analyzeAccelerationData(final String rawData, final HitoeDevice receiveDevice) {
        String[] lineList = rawData.split(HitoeConstants.BR);
        ArrayList<String> postureInputList  = new ArrayList<String>();
        ArrayList<String> walkInputList  = new ArrayList<String>();
        ArrayList<String> lrBalanceInputList  = new ArrayList<String>();

        ArrayList<String> workList = new ArrayList<String>();

        for (int i = 0; i < lineList.length; i++) {

            if (receiveDevice.getAvailableExDataList().contains("ex.posture")) {
                try {
                    mLockForPosture.lock();
                    mListForPosture.add(lineList[i]);
                    if (mListForPosture.size() > HitoeConstants.EX_POSTURE_UNIT_NUM + 5) {

                        for (int j = 0; j < HitoeConstants.EX_POSTURE_UNIT_NUM; j++) {

                            postureInputList.add(mListForPosture.get(j));
                        }
                        for (int j = HitoeConstants.EX_POSTURE_UNIT_NUM;
                                    j < HitoeConstants.EX_POSTURE_UNIT_NUM + 5; j++) {

                            postureInputList.add(mListForPosture.get(j));
                        }
                        workList = new ArrayList<>();
                        for (int j = 25; j < mListForPosture.size(); j++) {

                            workList.add(mListForPosture.get(j));
                        }
                        mListForPosture = workList;
                    }
                } finally {

                    mLockForPosture.unlock();
                }
                if (postureInputList.size() > 0) {

                    try {
                        mLockForEx.lock();
                        mListForEx.add(new TempExData("ex.posture", postureInputList));

                    } finally {
                        mLockForEx.unlock();
                    }

                    postureInputList.clear();
                }
            }
            if (receiveDevice.getAvailableExDataList().contains("ex.walk")) {
                try {
                    mLockForWalk.lock();
                    mListForWalk.add(lineList[i]);
                    if (mListForWalk.size() > HitoeConstants.EX_WALK_UNIT_NUM + 5) {

                        for (int j = 0; j < HitoeConstants.EX_WALK_UNIT_NUM; j++) {

                            walkInputList.add(mListForWalk.get(j));
                        }
                        for (int j = HitoeConstants.EX_WALK_UNIT_NUM;
                                    j < HitoeConstants.EX_WALK_UNIT_NUM + 5; j++) {

                            walkInputList.add(mListForWalk.get(j));
                        }

                        workList = new ArrayList<>();
                        for (int j = 25; j < mListForWalk.size(); j++) {

                            workList.add(mListForWalk.get(j));
                        }
                        mListForWalk = workList;
                    }
                } finally {

                    mLockForWalk.unlock();
                }
                if (walkInputList.size() > 0) {

                    try {
                        mLockForEx.lock();
                        mListForEx.add(new TempExData("ex.walk", walkInputList));
                    } finally {
                        mLockForEx.unlock();
                    }

                    walkInputList.clear();
                }
            }
            if (receiveDevice.getAvailableExDataList().contains("ex.lr_balance")) {
                try {
                    mLockForLRBalance.lock();
                    mListForLRBalance.add(lineList[i]);
                    if (mListForLRBalance.size() > HitoeConstants.EX_LR_BALANCE_UNIT_NUM + 5) {

                        for (int j = 0; j < HitoeConstants.EX_LR_BALANCE_UNIT_NUM; j++) {

                            lrBalanceInputList.add(mListForLRBalance.get(j));
                        }
                        for (int j = HitoeConstants.EX_LR_BALANCE_UNIT_NUM;
                                j < HitoeConstants.EX_LR_BALANCE_UNIT_NUM + 5; j++) {

                            lrBalanceInputList.add(mListForLRBalance.get(j));
                        }
                        workList = new ArrayList<>();
                        for (int j = 25; j < mListForLRBalance.size(); j++) {

                            workList.add(mListForLRBalance.get(j));
                        }
                        mListForLRBalance = workList;

                    }
                } finally {

                    mLockForLRBalance.unlock();
                }
                if (lrBalanceInputList.size() > 0) {

                    try {
                        mLockForEx.lock();
                        mListForEx.add(new TempExData("ex.lr_balance", lrBalanceInputList));
                    } finally {
                        mLockForEx.unlock();
                    }

                    lrBalanceInputList.clear();
                }
            }
        }
    }

    /**
     * Scan Hitoe device.
     * @param enable scan flag
     */
    private synchronized void scanHitoeDevice(final boolean enable) {


        if (enable) {
            if (mScanning || mScanTimerFuture != null) {
                // scan have already started.
                return;
            }
            mScanning = true;
            mIsCallbackRunning = true;
            mNowTimestamps.clear();
            for (HitoeDevice heart: mRegisterDevices) {
                mNowTimestamps.put(heart, System.currentTimeMillis());
            }
            mScanTimerFuture = mExecutor.scheduleAtFixedRate(() -> {

                for (HitoeDevice heart: mHRData.keySet()) {
                    HeartRateData data = mHRData.get(heart);
                    long timestamp = data.getHeartRate().getTimeStamp();
                    long history = mNowTimestamps.get(heart);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "================>");
                        Log.d(TAG, "timestamp:" + timestamp);
                        Log.d(TAG, "history:" + history);
                        Log.d(TAG, "CallbackRunning:" + mIsCallbackRunning);
                        Log.d(TAG, "isRegisterFlag:" + heart.isRegisterFlag());
                        Log.d(TAG, "<================");
                    }
                    if (mIsCallbackRunning && history == timestamp && heart.isRegisterFlag()) {
                        final String name = heart.getName();

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "Disconnect to " + name,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        mIsCallbackRunning = false;
                    } else if (!mIsCallbackRunning && history < timestamp && heart.isRegisterFlag()) {
                        final String name = heart.getName();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "Connect to " + name,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        mIsCallbackRunning = true;
                    }
                    mNowTimestamps.put(heart, timestamp);
                    if (!mIsCallbackRunning && heart.isRegisterFlag()) {
                        connectHitoeDevice(heart);
                    }
                }
            }, SCAN_FIRST_WAIT_PERIOD, SCAN_WAIT_PERIOD, TimeUnit.MILLISECONDS);
        } else {
            mScanning = false;
            cancelScanTimer();
        }
    }

    /**
     * Stopped the scan timer.
     */
    private synchronized void cancelScanTimer() {
        if (mScanTimerFuture != null) {
            mScanTimerFuture.cancel(true);
            mScanTimerFuture = null;
        }
    }

    /**
     * Is Exist Disconnected.
     * @return true:exist connect, false: non exist connect
     */
    private boolean existConnected() {
        int connectCount = 0;
        for (int i = 0; i < mRegisterDevices.size(); i++) {
            if (mRegisterDevices.get(i).isRegisterFlag()) {
                connectCount++;
            }
        }
        return (connectCount > 0);
    }
    // ------------------------------------
    // Listener.
    // ------------------------------------


    /**
     * Hitoe Device Discovery Listener.
     */
    public interface OnHitoeConnectionListener {
        /**
         * Connected Device.
         * @param device Hitoe device
         */
        void onConnected(final HitoeDevice device);

        /**
         * Connect fail device.
         * @param device Hitoe device
         */
        void onConnectFailed(final HitoeDevice device);
        /**
         * Discovery Listener.
         * @param devices Found hitoe devices
         */
        void onDiscovery(List<HitoeDevice> devices);

        /**
         * Disconnected Listener.
         * @param res response id
         * @param device disconnect device
         */
        void onDisconnected(final int res, final HitoeDevice device);

        /**
         * Deleted Listener.
         * @param device delte device
         */
        void onDeleted(final HitoeDevice device);
    }

    /**
     * Hitoe Device HeartRate Listener.
     */
    public interface OnHitoeHeartRateEventListener {
        /**
         * Received data for Hitoe HeartRate data.
         * @param device Hitoe device
         * @param data HeartRate data
         */
        void onReceivedData(final HitoeDevice device, final HeartRateData data);
    }

    /**
     * Hitoe Device ECG Listener.
     */
    public interface OnHitoeECGEventListener {
        /**
         * Received data for Hitoe ECG Data.
         * @param device Hitoe device
         * @param data ECG data
         */
        void onReceivedData(final HitoeDevice device, final HeartRateData data);
    }

    /**
     * Hitoe Device Pose Estimation Listener.
     */
    public interface OnHitoePoseEstimationEventListener {
        /**
         * Received data for Hitoe Pose estimation data.
         * @param device Hitoe device
         * @param data pose estimation data
         */
        void onReceivedData(final HitoeDevice device, final PoseEstimationData data);
    }

    /**
     * Hitoe Device Stress Estimation Listener.
     */
    public interface OnHitoeStressEstimationEventListener {
        /**
         * Received data for Hitoe Stress Estimation data.
         * @param device Hitoe device
         * @param data stress estimation data
         */
        void onReceivedData(final HitoeDevice device, final StressEstimationData data);
    }

    /**
     * Hitoe Device Walk State Listener.
     */
    public interface OnHitoeWalkStateEventListener {
        /**
         * Received data for Hitoe walk state data.
         * @param device Hitoe device
         * @param data walk state
         */
        void onReceivedData(final HitoeDevice device, final WalkStateData data);
    }

    /**
     * Hitoe Device Device Orientation Listener.
     */
    public interface OnHitoeDeviceOrientationEventListener {
        /**
         * Received data for Hitoe device orientation data.
         * @param device Hitoe device
         * @param data device orientation
         */
        void onReceivedData(final HitoeDevice device, final AccelerationData data);
    }
}
