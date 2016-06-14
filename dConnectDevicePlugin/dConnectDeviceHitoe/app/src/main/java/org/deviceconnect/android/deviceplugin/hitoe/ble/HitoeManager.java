/*
 HitoeManager
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.ble;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hitoe.BuildConfig;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDBHelper;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.PoseEstimationData;
import org.deviceconnect.android.deviceplugin.hitoe.data.StressEstimationData;
import org.deviceconnect.android.deviceplugin.hitoe.data.TargetDeviceData;
import org.deviceconnect.android.deviceplugin.hitoe.data.TempExData;
import org.deviceconnect.android.deviceplugin.hitoe.data.WalkStateData;
import org.deviceconnect.android.deviceplugin.hitoe.util.RawDataParseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import jp.ne.docomo.smt.dev.hitoetransmitter.HitoeSdkAPI;
import jp.ne.docomo.smt.dev.hitoetransmitter.sdk.HitoeSdkAPIImpl;

/**
 * This class manages a Hitoe devices.
 * @author NTT DOCOMO, INC.
 */
public class HitoeManager {
    
    private static final String TAG = "HitoeManager";
    
    
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
    /** ECG Datas. */
    private final Map<HitoeDevice, HeartRateData> mECGData;
    /** Pose Estimation datas. */
    private final Map<HitoeDevice, PoseEstimationData> mPoseEstimationData;
    /** Stress Estimation datas. */
    private final Map<HitoeDevice, StressEstimationData> mStressEstimationData;
    /** Walk State datas. */
    private final Map<HitoeDevice, WalkStateData> mWalkStateData;

    // 拡張分析のための保存データ
    private ArrayList<TempExData> mListForEx;
    // 拡張分析のためのロック
    private ReentrantLock mLockForEx;
    // 拡張分析中フラグ
    private boolean mFlagForEx;



    /** Hitoe API Callback. */
    HitoeSdkAPI.APICallback mAPICallback = new HitoeSdkAPI.APICallback() {

        @Override
        public void onResponse(int apiId, int responseId, String responseString) {

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
                        // センサーの切断
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

                        if (responseId != HitoeConstants.RES_ID_SUCCESS || responseString == null) {
                            // 確認に失敗
                        } else {
                            messageTextBuilder.append(responseString);
                        }
                        break;
                    default:
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "etc state");
                        }
                        break;
                }
            

            return;
        }
    };




    /**
     * データレシーバ
     */
    HitoeSdkAPI.DataReceiverCallback mDataReceiverCallback = new HitoeSdkAPI.DataReceiverCallback() {
        @Override
        public void onDataReceive(final String connectionId, final int responseId, final String dataKey, final String rawData) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "DataCallback:connectId=" + connectionId
                        + ",dataKey=" + dataKey + ",response_id="
                        + responseId + ",rawData=" + rawData.replace("\n", ","));
            }
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

            // データ受信時のコールバック
            if (dataKey.equals("raw.ecg")) {
                extractHealth(HeartData.HeartRateType.ECG, rawData, receiveDevice);
            } else if (dataKey.equals("raw.acc")) {
//                parseACCStr(rawData);

            } else if (dataKey.equals("raw.rri")) {
                extractHealth(HeartData.HeartRateType.RRI, rawData, receiveDevice);
            } else if (dataKey.equals("raw.bat")) {
//                parseBatStr(rawData);
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

            } else if (dataKey.equals("raw.hr")) {
                extractHealth(HeartData.HeartRateType.Rate, rawData, receiveDevice);
            } else if (dataKey.equals("raw.saved_hr")) {

            } else if (dataKey.equals("raw.saved_rri")) {

            } else if (dataKey.equals("ba.extracted_rri")) {
//                Log.d("MainActivity", "ba.extracted_rri:" + rawData);

            } else if (dataKey.equals("ba.cleaned_rri")) {
//                Log.d("MainActivity", "ba.cleaned_rri:" + rawData);

            } else if (dataKey.equals("ba.interpolated_rri")) {
//                Log.d("MainActivity", "ba.interpolated_rri:" + rawData);

            } else if (dataKey.equals("ba.freq_domain")) {
//                parseFreqDomain(rawData);

            } else if (dataKey.equals("ba.time_domain")) {
//                Log.d("MainActivity", "ba.time_domain:" + rawData);

            } else if (dataKey.equals("ex.stress")) {
//                parseStress(rawData);

            } else if (dataKey.equals("ex.posture")) {
//                parsePosture(rawData);

            } else if (dataKey.equals("ex.walk")) {
//                parseWalk(rawData);

            } else if (dataKey.equals("ex.lr_balance")) {
//                parseLRBalance(rawData);

            }

            if (dataKey.startsWith(HitoeConstants.EX_DATA_PREFFIX)) {
                // 拡張分析はコネクションを破棄する
                receiveDevice.removeConnectionId(connectionId);
            } else {

                // 拡張があれば実行
                // 既に実行中であれば実行しない
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

                    // nullでなければ続けて投げる
                    addExReceiverProcess(pos, exData);
                }
            }

            notifyListeners(receiveDevice);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "DataCallback:end========================>");
            }

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
        mListForEx = new ArrayList<TempExData>();
        mLockForEx = new ReentrantLock();
        mRegisterDevices = Collections.synchronizedList(
                new ArrayList<HitoeDevice>());
        mHRData = new ConcurrentHashMap<>();
        mECGData = new ConcurrentHashMap<>();
        mPoseEstimationData = new ConcurrentHashMap<>();
        mStressEstimationData = new ConcurrentHashMap<>();
        mWalkStateData = new ConcurrentHashMap<>();
        mConnectionListeners = new ArrayList<OnHitoeConnectionListener>();
        mHitoeSdkAPI = HitoeSdkAPIImpl.getInstance(context);
        mHitoeSdkAPI.setAPICallback(mAPICallback);
        List<HitoeDevice> list = mDBHelper.getHitoeDevices(null);
        for (HitoeDevice device : list) {
            if (device.isRegisterFlag() && !mRegisterDevices.contains(device)) {
                mRegisterDevices.add(device);
            }
        }

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
     * Set Hitoe HeartRate Listener
     * @param l listener
     */
    public void setHitoeHeartRateEventListener(final OnHitoeHeartRateEventListener l) {
        mHeartRataListener = l;
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




    public void start() {
        // TODO want to connected
        synchronized (mRegisterDevices) {
            for (HitoeDevice device : mRegisterDevices) {
                if (device.isRegisterFlag()) {
                    connectHitoeDevice(device);
                }
            }
        }

    }
    /**
     * Stops the HeartRateManager.
     */
    public void stop() {
        for (int i = 0; i < mRegisterDevices.size(); i++) {
            mHitoeSdkAPI.disconnect(mRegisterDevices.get(i).getSessionId());
            mRegisterDevices.get(i).setRegisterFlag(false);
            mDBHelper.updateHitoeDevice(mRegisterDevices.get(i));
            mRegisterDevices.get(i).setSessionId(null);
        }
    }
    /**
     * Discovery hitoe device.
     */
    public void discoveryHitoeDevices() {
        StringBuilder paramStringBuilder = new StringBuilder();
        // 利用可能センサを問い合わせ
        if(HitoeConstants.GET_AVAILABLE_SENSOR_PARAM_SEARCH_TIME != -1) {
            paramStringBuilder.append("search_time=" + String.valueOf(HitoeConstants.GET_AVAILABLE_SENSOR_PARAM_SEARCH_TIME));
        }
        mHitoeSdkAPI.getAvailableSensor(HitoeConstants.GET_AVAILABLE_SENSOR_DEVICE_TYPE, paramStringBuilder.toString());
    }


    /**
     * Connect to Hitoe Device by address.
     *
     * @param device device for hitoe device
     */
    public void connectHitoeDevice(final HitoeDevice device) {
        if (device == null || device.getPinCode() == null) {
            return;
        }
        StringBuilder paramBuilder = new StringBuilder();
        if (HitoeConstants.CONNECT_DISCONNECT_RETRY_TIME > 0) {

            paramBuilder.append("disconnect_retry_time=" + HitoeConstants.CONNECT_DISCONNECT_RETRY_TIME);
        }
        if (HitoeConstants.CONNECT_DISCONNECT_RETRY_COUNT > 0) {

            if (paramBuilder.length() > 0) {
                paramBuilder.append(HitoeConstants.BR);
            }
            paramBuilder.append("disconnect_retry_count=" + HitoeConstants.CONNECT_DISCONNECT_RETRY_COUNT);
        }
        if (HitoeConstants.CONNECT_NOPACKET_RETRY_TIME > 0) {

            if (paramBuilder.length() > 0) {
                paramBuilder.append(HitoeConstants.BR);
            }
            paramBuilder.append("nopacket_retry_time=" + HitoeConstants.CONNECT_NOPACKET_RETRY_TIME);
        }
        if(paramBuilder.length() > 0) {

            paramBuilder.append(HitoeConstants.BR);
        }
        paramBuilder.append("pincode=");
        paramBuilder.append(device.getPinCode());
        String param = paramBuilder.toString();
        mHitoeSdkAPI.connect(device.getType(), device.getId(), device.getConnectMode(), param);
        device.setResponseId(HitoeConstants.RES_ID_SENSOR_CONNECT);

        for (int i = 0; i < mRegisterDevices.size(); i++) {
            if (mRegisterDevices.get(i).getId().equals(device.getId())) {
                mRegisterDevices.set(i, device);
            }
        }
    }

    /**
     * Disconnect hitoe device.
     * @param device hitoe device
     */
    public void disconnectHitoeDevice(final HitoeDevice device) {
        if(device.getSessionId() == null) {
            return;
        }
        mHitoeSdkAPI.disconnect(device.getSessionId());
        device.setRegisterFlag(false);
        device.setSessionId(null);
        mDBHelper.updateHitoeDevice(device);
        for (OnHitoeConnectionListener l: mConnectionListeners) {
            if (l != null) {
                l.onDisconnected(device);
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
                if (d.getId().equalsIgnoreCase(id) && d.getSessionId() != null) {
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
            //利用可能なセンサが見つからない
            return;
        }

        String[] sensorList = responseString.split(HitoeConstants.BR, -1);
        List<HitoeDevice> devices = new ArrayList<HitoeDevice>();
        List<HitoeDevice> pins = mDBHelper.getHitoeDevices(null);
        mRegisterDevices.clear();
        for (int i = 0; i < sensorList.length; i++) {
            String sensorStr = sensorList[i].trim();
            if (sensorStr.length() == 0) {
                continue;
            }
            if (sensorStr.indexOf("memory_setting") < 0 && sensorStr.indexOf("memory_get") < 0) {
                HitoeDevice device = new HitoeDevice(sensorStr);
                if (!devices.contains(device)) {
                    devices.add(device);
                }
                if (!mRegisterDevices.contains(device)) {
                    mRegisterDevices.add(device);
                }
            }
        }
        for (HitoeDevice pin : pins) {
            for (HitoeDevice register: devices) {
                if (register.getId().equals(pin.getId())) {
                    register.setPinCode(pin.getPinCode());
                }
            }
        }
        for (OnHitoeConnectionListener l: mConnectionListeners) {
            if (l != null) {
                l.onDiscovery(devices);
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
            // センサー接続が途切れた
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                if (l != null) {

                    l.onConnectFailed(mRegisterDevices.get(pos));
                }
            }
            return;
        } else if(responseId == HitoeConstants.RES_ID_SENSOR_CONNECT_NOTICE) {
            // センサー接続が再開
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                l.onConnected(mRegisterDevices.get(pos));
            }
            return;
        } else if (responseId != HitoeConstants.RES_ID_SENSOR_CONNECT) {
            //センサー接続に失敗
            for (OnHitoeConnectionListener l: mConnectionListeners) {
                if (l != null) {

                    l.onConnectFailed(mRegisterDevices.get(pos));
                }
            }
            return;
        }
        mRegisterDevices.get(pos).setSessionId(responseString);
        mRegisterDevices.get(pos).setRegisterFlag(true);
        mDBHelper.addHitoeDevice(mRegisterDevices.get(pos));
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
        // 基本分析と拡張分析どちらの場合もある
        if (responseId != HitoeConstants.RES_ID_SUCCESS || responseString == null) {
            //利用可能データ取得の失敗
            return;
        }
        int pos = getCurrentPos(responseId);
        if (pos == -1) {
            return;
        }
        mRegisterDevices.get(pos).setAvailableData(responseString);
        // データ種別文字列作成
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
                if (HitoeConstants.ADD_RECEIVER_PARAM_ECG_SAMPLING_INTERVAL != -1) {
                    paramStringBuilder.append("raw.ecg_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_ECG_SAMPLING_INTERVAL));
                }
            } else if (keyList.get(i).equals("raw.acc")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                if (HitoeConstants.ADD_RECEIVER_PARAM_ACC_SAMPLING_INTERVAL != -1) {
                    paramStringBuilder.append("raw.acc_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_ACC_SAMPLING_INTERVAL));
                }
            } else if (keyList.get(i).equals("raw.rri")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                if (HitoeConstants.ADD_RECEIVER_PARAM_RRI_SAMPLING_INTERVAL != -1) {
                    paramStringBuilder.append("raw.rri_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_RRI_SAMPLING_INTERVAL));
                }
            } else if (keyList.get(i).equals("raw.hr")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                if (HitoeConstants.ADD_RECEIVER_PARAM_HR_SAMPLING_INTERVAL != -1) {
                    paramStringBuilder.append("raw.hr_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_HR_SAMPLING_INTERVAL));
                }
            } else if (keyList.get(i).equals("raw.bat")) {

                if (paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                if (HitoeConstants.ADD_RECEIVER_PARAM_BAT_SAMPLING_INTERVAL != -1) {
                    paramStringBuilder.append("raw.bat_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BAT_SAMPLING_INTERVAL));
                }
            } else if (keyList.get(i).equals("raw.saved_hr") || keyList.get(i).equals("raw.saved_rri")) {

            } else {
                // Unknown
            }
        }

        paramString = paramStringBuilder.toString();
        mHitoeSdkAPI.addReceiver(mRegisterDevices.get(pos).getSessionId(), keys, mDataReceiverCallback, paramString, null);
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
        if(responseString.startsWith(HitoeConstants.RAW_CONNECTION_PREFFIX)) {
            addBaReceiverProcess(pos);
        } else {
            TempExData exData = null;
            try{

                mLockForEx.lock();

                if(mListForEx.size() > 0) {
                    exData = mListForEx.get(0);
                    mListForEx.remove(0);
                } else {

                    mFlagForEx = false;
                }

            }finally {

                mLockForEx.unlock();
            }
            if(exData != null) {
                // nullでなければ続けて投げる
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

        // 終了プロセス
        if(responseString.startsWith(HitoeConstants.BA_CONNECTION_PREFFIX)) {
            removeRawReceiverProcess(mRegisterDevices.get(pos).getRawConnectionId());
        } else if(responseString.startsWith(HitoeConstants.RAW_CONNECTION_PREFFIX)) {
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
    }




    /**
     * Add ba Receiver process.
     * @param pos device pos
     * @return true:end process, false:continue process
     */
    private void addBaReceiverProcess(final int pos) {
        List<String> keyList = mRegisterDevices.get(pos).getAvailableBaDataList();

        StringBuilder paramStringBuilder = new StringBuilder();
        String[] keys = new String[keyList.size()];
        String paramString;

        if(keyList.size() == 0) {
            return;
        }

        for(int i = 0; i < keyList.size(); i++) {

            keys[i] = keyList.get(i);

            if(keyList.get(i).equals("ba.extracted_rri")) {

                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL != -1 && paramStringBuilder.indexOf("ba.sampling_interval") == -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.sampling_interval="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_ECG_THRESHHOLD != -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.ecg_threshhold="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_ECG_THRESHHOLD));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_SKIP_COUNT != -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.ecg_skip_count="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SKIP_COUNT));
                }

            } else if(keyList.get(i).equals("ba.cleaned_rri")) {

                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL != -1 && paramStringBuilder.indexOf("ba.sampling_interval") == -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.sampling_interval="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_MIN != -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.rri_min="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_MIN));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_MAX != -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.rri_max="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_MAX));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLE_COUNT != -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.sample_count="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_SAMPLE_COUNT));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_INPUT != null) {

                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.rri_input="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_INPUT));
                }
            } else if(keyList.get(i).equals("ba.interpolated_rri")) {

                if (HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL != -1 && paramStringBuilder.indexOf("ba.freq_sampling_interval") == -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW != -1 && paramStringBuilder.indexOf("ba.freq_sampling_window") == -1) {
                    if (paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_window="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE != -1 && paramStringBuilder.indexOf("ba.rri_sampling_rate") == -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.rri_sampling_rate="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE));
                }
            } else if(keyList.get(i).equals("ba.freq_domain")) {

                if (HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL != -1 && paramStringBuilder.indexOf("ba.freq_sampling_interval") == -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW != -1 && paramStringBuilder.indexOf("ba.freq_sampling_window") == -1) {
                    if (paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.freq_sampling_window="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE != -1 && paramStringBuilder.indexOf("ba.rri_sampling_rate") == -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.rri_sampling_rate="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE));
                }
            } else if(keyList.get(i).equals("ba.time_domain")) {

                if (HitoeConstants.ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_INTERVAL != -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.time_sampling_interval=" + String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_INTERVAL));
                }
                if(HitoeConstants.ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_WINDOW != -1) {
                    if(paramStringBuilder.length() > 0 && paramStringBuilder.lastIndexOf(HitoeConstants.BR) != paramStringBuilder.length() - 1) {

                        paramStringBuilder.append(HitoeConstants.BR);
                    }
                    paramStringBuilder.append("ba.time_sampling_window="+String.valueOf(HitoeConstants.ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_WINDOW));
                }
            }
        }

        paramString = paramStringBuilder.toString();

        int resId = mHitoeSdkAPI.addReceiver(mRegisterDevices.get(pos).getSessionId(), keys, mDataReceiverCallback, paramString, null);
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

        if(!mRegisterDevices.get(pos).getAvailableExDataList().contains(keyString)) {

            // 対象でなければ登録しない
            try{

                mLockForEx.lock();
                mFlagForEx = false;

            }finally {

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

        for(int i = 0; i < dataList.size(); i ++) {

            if(dataStringBuilder.length() > 0) {

                dataStringBuilder.append(HitoeConstants.BR);
            }
            dataStringBuilder.append(dataList.get(i));
        }

        if(keyString.equals("ex.stress")) {


        } else if(keyString.equals("ex.posture")) {

            if(HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ != null) {

                if(paramStringBuilder.length() > 0) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ex.acc_axis_xyz="+HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ);
            }
            if(HitoeConstants.ADD_RECEIVER_PARAM_EX_POSTURE_WINDOW != -1) {

                if(paramStringBuilder.length() > 0) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ex.posture_window="+HitoeConstants.ADD_RECEIVER_PARAM_EX_POSTURE_WINDOW);
            }
        } else if(keyString.equals("ex.walk")) {

            if(HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ != null) {

                if(paramStringBuilder.length() > 0) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ex.acc_axis_xyz="+HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ);
            }
            if(HitoeConstants.ADD_RECEIVER_PARAM_EX_WALK_STRIDE != -1) {

                if(paramStringBuilder.length() > 0) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ex.walk_stride="+HitoeConstants.ADD_RECEIVER_PARAM_EX_WALK_STRIDE);
            }
            if(HitoeConstants.ADD_RECEIVER_PARAM_EX_RUN_STRIDE_COF != -1) {

                if(paramStringBuilder.length() > 0) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ex.run_stride_cof="+HitoeConstants.ADD_RECEIVER_PARAM_EX_RUN_STRIDE_COF);
            }
            if(HitoeConstants.ADD_RECEIVER_PARAM_EX_RUN_STRIDE_INT != -1) {

                if(paramStringBuilder.length() > 0) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ex.run_stride_int="+HitoeConstants.ADD_RECEIVER_PARAM_EX_RUN_STRIDE_INT);
            }
        } else if(keyString.equals("ex.lr_balance")) {
            if(HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ != null) {

                if(paramStringBuilder.length() > 0) {

                    paramStringBuilder.append(HitoeConstants.BR);
                }
                paramStringBuilder.append("ex.acc_axis_xyz="+HitoeConstants.ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ);
            }
        }

        paramString = paramStringBuilder.toString();
        dataString = dataStringBuilder.toString();

        responseId = mHitoeSdkAPI.addReceiver(null, keys, mDataReceiverCallback, paramString, dataString);
        if (responseId != HitoeConstants.RES_ID_SUCCESS) {
            try{

                mLockForEx.lock();
                mFlagForEx = false;

            }finally {

                mLockForEx.unlock();
            }
        }

        return;
    }

    /**
     * Remove raw receiver process.
     * @param rawConnectionId raw connection id
     */
    private void removeRawReceiverProcess(final String rawConnectionId) {
        if(rawConnectionId == null) {
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

        if(device.getSessionId() == null) {
            return;
        }
        mRegisterDevices.get(pos).setSessionId(null);
        mRegisterDevices.get(pos).setRegisterFlag(false);
        mDBHelper.updateHitoeDevice(mRegisterDevices.get(pos));
        mHitoeSdkAPI.disconnect(device.getSessionId());
//        mRegisterDevices.remove(pos);
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
            if (mRegisterDevices.get(i).getRawConnectionId() != null) {
                if (mRegisterDevices.get(i).getId().equals(serviceId)) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }


    /**
     * Extract health data.
     * @param type Health data type
     * @param rawData raw data
     * @param receiveDevice Health device
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
         * @param device disconnect device
         */
        void onDisconnected(final HitoeDevice device);
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
}
