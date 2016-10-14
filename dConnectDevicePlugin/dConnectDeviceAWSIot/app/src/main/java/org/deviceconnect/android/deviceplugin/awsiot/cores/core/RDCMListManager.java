/*
 RDCMListManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

import android.content.Context;
import android.support.v4.BuildConfig;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Remote Device Connect Manager List Manager Class.
 */
public class RDCMListManager {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "RDCM";

    /** 遠隔にあるDevice Connect Managerのリスト. */
    private List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();
    /** Database Helper. */
    private AWSIotDBHelper mDBHelper;
    /** OnEventListener Instance. */
    private OnEventListener mOnEventListener;
    /** Context. */
    private Context mContext;
    /** AWSIotController instance. */
    private AWSIotController mAWSIotController;
    /** ScheduledExecutorService instance. */
    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    /** ScheduledFuture instance. */
    private ScheduledFuture mFuture;
    /** Manager list shadow 名称. */
    private final String MANAGER_LIST_SHADOW = "$aws/things/"+ AWSIotUtil.KEY_DCONNECT_SHADOW_NAME +"/shadow/update/accepted";

    /** OnEventListener Interface. */
    public interface OnEventListener {
        void onRDCMListUpdateSubscribe(RemoteDeviceConnectManager manager);
    }

    /** UpdateManagerListCallback Interface. */
    public interface UpdateManagerListCallback {
        void onUpdateManagerList(List<RemoteDeviceConnectManager> managerList);
    }

    private UpdateManagerListCallback mUpdateManagerListCallback = new UpdateManagerListCallback() {
        @Override
        public void onUpdateManagerList(List<RemoteDeviceConnectManager> managerList) {
            if (managerList != null) {
                if (mOnEventListener != null) {
                    for (RemoteDeviceConnectManager remote : managerList) {
                        mOnEventListener.onRDCMListUpdateSubscribe(remote);
                    }
                }
            }
        }
    };

    /**
     * Constructor.
     * @param context Context.
     */
    RDCMListManager(final Context context, final AWSIotController controller) {
        mContext = context;
        mAWSIotController = controller;
        mDBHelper = new AWSIotDBHelper(context);
    }

    /**
     * 定期的にManager情報を更新するタイマーを開始します。
     */
    void startUpdateManagerListTimer() {
        if (mFuture != null) {
            return;
        }

        mAWSIotController.addOnAWSIotEventListener(mOnAWSIotEventListener);

        mFuture = mExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    Log.i(TAG, "Update the online status for Device Shadow.");
                }
                AWSIotPrefUtil pref = new AWSIotPrefUtil(mContext);
                AWSIotDeviceApplication.getInstance().updateMyManagerShadow(pref.getManagerRegister());
            }
        }, 30, 5 * 60, TimeUnit.SECONDS);
    }

    /**
     * 定期的にManager情報を更新するタイマーを停止します。
     */
    void stopUpdateManagerListTimer() {
        if (mFuture != null) {
            mFuture.cancel(true);
            mFuture = null;
        }

        mAWSIotController.removeOnAWSIotEventListener(mOnAWSIotEventListener);

        unsubscribeShadow();
    }

    /**
     * Manager List Shadowの購読を開始する.
     */
    public void subscribeShadow() {
        mAWSIotController.subscribe(MANAGER_LIST_SHADOW, new AWSIotController.MessageCallback() {
            @Override
            public void onReceivedMessage(String topic, String message, Exception err) {
                if (err == null) {
                    updateManagerList(mUpdateManagerListCallback);
                }
            }
        });
    }

    /**
     * Manager List Shadowの購読を停止する.
     */
    void unsubscribeShadow() {
        mAWSIotController.unsubscribe(MANAGER_LIST_SHADOW);
    }

    /**
     * Set OnEventListener.
     * @param eventListener listener.
     */
    public void setOnEventListener(final OnEventListener eventListener) {
        mOnEventListener = eventListener;
    }

    /**
     * Get RDCMList.
     * @return RDCMList.
     */
    public List<RemoteDeviceConnectManager> getRDCMList() {
        return mManagerList;
    }

    /**
     * Update Subscribe.
     * @param id Manager Id.
     * @param flag subscribe flag.
     * @return true(Success) / false(Failed).
     */
    public boolean updateSubscribe(final String id, final boolean flag) {
        boolean result = false;
        RemoteDeviceConnectManager manager = findRegisteredManagerById(id);
        if (manager != null) {
            manager.setSubscribeFlag(flag);
            mDBHelper.updateManager(manager);
            result = true;
            if (mOnEventListener != null) {
                mOnEventListener.onRDCMListUpdateSubscribe(manager);
            }
        }
        return result;
    }

    /**
     * Manager情報を更新します。
     * @param callback 更新完了通知を行うコールバック
     */
    public void updateManagerList(final UpdateManagerListCallback callback) {
        mAWSIotController.getShadow(AWSIotUtil.KEY_DCONNECT_SHADOW_NAME, new AWSIotController.GetShadowCallback() {
            @Override
            public void onReceivedShadow(final String thingName, final String result, final Exception err) {
                if (err != null) {
                    if (callback != null) {
                        callback.onUpdateManagerList(null);
                    }
                } else {
                    mManagerList = AWSIotUtil.parseDeviceShadow(mContext, result);
                    if (callback != null) {
                        callback.onUpdateManagerList(mManagerList);
                    }
                }
            }
        });
    }

    /**
     * Find the {@link RemoteDeviceConnectManager} from id.
     *
     * @param id id of Manager
     * @return {@link RemoteDeviceConnectManager}, or null
     */
    private RemoteDeviceConnectManager findRegisteredManagerById(final String id) {
        if (mManagerList != null) {
            for (RemoteDeviceConnectManager d : mManagerList) {
                if (d.getServiceId().equalsIgnoreCase(id)) {
                    return d;
                }
            }
        }
        return null;
    }

    private final AWSIotController.OnAWSIotEventListener mOnAWSIotEventListener = new AWSIotController.OnAWSIotEventListener() {
        @Override
        public void onLogin() {
        }

        @Override
        public void onConnected() {
            subscribeShadow();
            updateManagerList(mUpdateManagerListCallback);
        }
    };
}
