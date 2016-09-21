package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

import android.content.Context;

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

    /** 遠隔にあるDevice Connect Managerのリスト. */
    private List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();
    /** Database Helper. */
    private AWSIotDBHelper mDBHelper;
    /** OnEventListener Instance. */
    private OnEventListener mOnEventListener;
    private Context mContext;
    private AWSIotController mAWSIotController;

    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mFuture;

    /** OnEventListener Interface. */
    public interface OnEventListener{
        void onRDCMListUpdateSubscribe(RemoteDeviceConnectManager manager);
    }

    public interface UpdateManagerListCallback {
        void onUpdateManagerList(List<RemoteDeviceConnectManager> managerList);
    }

    /**
     * Constructor.
     * @param context Context.
     */
    public RDCMListManager(final Context context, final AWSIotController controller) {
        mContext = context;
        mAWSIotController = controller;
        mDBHelper = new AWSIotDBHelper(context);
    }

    /**
     * 定期的にManager情報を更新するタイマーを開始します。
     */
    public void startUpdateManagerListTimer() {
        if (mFuture != null) {
            return;
        }

        mFuture = mExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                AWSIotDeviceApplication.getInstance().updateMyManagerShadow(true);
                updateManagerList(new UpdateManagerListCallback() {
                    @Override
                    public void onUpdateManagerList(final List<RemoteDeviceConnectManager> managerList) {
                        if (managerList != null) {
                            if (mOnEventListener != null) {
                                for (RemoteDeviceConnectManager remote : managerList) {
                                    mOnEventListener.onRDCMListUpdateSubscribe(remote);
                                }
                            }
                        }
                    }
                });
            }
        }, 15, 5 * 60, TimeUnit.SECONDS);
    }

    /**
     * 定期的にManager情報を更新するタイマーを停止します。
     */
    public void stopUpdateManagerListTimer() {
        if (mFuture != null) {
            mFuture.cancel(true);
        }
    }

    /**
     * Set OnEventListener.
     * @param eventListener listner.
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
     * Set RDCMList.
     * @param managerList RDCMList.
     */
    public void setRDCMList(final List<RemoteDeviceConnectManager> managerList) {
        mManagerList = managerList;
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
}
