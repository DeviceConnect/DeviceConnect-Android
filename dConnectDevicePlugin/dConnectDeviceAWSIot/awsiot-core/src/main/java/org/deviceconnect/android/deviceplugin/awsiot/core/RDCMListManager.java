package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

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
    /** OnEventListener Interface. */
    public interface OnEventListener{
        void onRDCMListUpdateSubscribe(RemoteDeviceConnectManager manager);
    }

    /**
     * Constructor.
     * @param context Context.
     */
    public RDCMListManager(final Context context) {
        mDBHelper = new AWSIotDBHelper(context);
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
        if (mManagerList != null) {
            RemoteDeviceConnectManager manager = findRegisteredManagerById(id);
            if (manager != null) {
                int index = mManagerList.indexOf(manager);
                manager.setSubscribeFlag(flag);
                mDBHelper.updateManager(manager);
                mManagerList.set(index, manager);
                result = true;
                if (mOnEventListener != null) {
                    mOnEventListener.onRDCMListUpdateSubscribe(manager);
                }
            }
        }
        return result;
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
