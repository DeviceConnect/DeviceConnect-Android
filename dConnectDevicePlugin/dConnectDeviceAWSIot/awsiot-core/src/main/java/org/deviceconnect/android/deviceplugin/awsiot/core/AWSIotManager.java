package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class AWSIotManager extends AWSIotCore {

    private List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();
    private Context mContext;

    /** Instance of {@link AWSIotDBHelper}. */
    private AWSIotDBHelper mDBHelper;

    public AWSIotManager(final Context context, final AWSIotController controller) {
        mContext = context;
        mIot = controller;
        mDBHelper = new AWSIotDBHelper(mContext);
    }

    public void getShadow(final GetShadowCallback callback) {
        mIot.getShadow(KEY_DCONNECT_SHADOW_NAME, new AWSIotController.GetShadowCallback() {
            @Override
            public void onReceivedShadow(final String thingName, final String result, final Exception err) {
                if (err != null) {
                    mManagerList = null;
                } else {
                    mManagerList = parseDeviceShadow(mContext, result);
                }
                callback.onReceivedShadow(mManagerList);
            }
        });
    }
    /**
     * Update Subscribe flag.
     * @param id Manager Id.
     * @param flag subscribe flag.
     * @return true(Success) / false(Failed).
     */
    public boolean updateSubscribeFlag(final String id, final boolean flag) {
        boolean result = false;
        RemoteDeviceConnectManager manager = findRegisteredManagerById(id);
        if (manager != null) {
            int index = mManagerList.indexOf(manager);
            manager.setSubscribeFlag(flag);
            mDBHelper.updateManager(manager);
            mManagerList.set(index, manager);
            result = true;
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
        synchronized (mManagerList) {
            for (RemoteDeviceConnectManager d : mManagerList) {
                if (d.getServiceId().equalsIgnoreCase(id)) {
                    return d;
                }
            }
        }
        return null;
    }

    public interface GetShadowCallback {
        void onReceivedShadow(List<RemoteDeviceConnectManager> list);
    }
}
