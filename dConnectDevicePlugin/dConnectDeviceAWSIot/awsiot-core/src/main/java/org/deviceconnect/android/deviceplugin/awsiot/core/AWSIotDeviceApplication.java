/*
 AWSIotDeviceApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * AWS IoT Device Plugin Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceApplication extends Application {

    /** AWSIotコントローラー */
    private final AWSIotController mIot = new AWSIotController();

    /** 遠隔にあるDevice Connect Managerのリスト. */
    private List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();

    /** Database Helper. */
    private AWSIotDBHelper mDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = new AWSIotDBHelper(getApplicationContext());
    }

    public AWSIotController getAWSIotController() {
        return mIot;
    }

    public List<RemoteDeviceConnectManager> getManagerList() {
        return mManagerList;
    }

    public void setManagerList(final List<RemoteDeviceConnectManager> managerList) {
        mManagerList = managerList;
    }

    /**
     * Update Subscribe flag.
     * @param id Manager Id.
     * @param flag subscribe flag.
     * @return true(Success) / false(Failed).
     */
    public boolean updateSubscribeFlag(final String id, final boolean flag) {
        boolean result = false;
        if (mManagerList != null) {
            RemoteDeviceConnectManager manager = findRegisteredManagerById(id);
            if (manager != null) {
                int index = mManagerList.indexOf(manager);
                manager.setSubscribeFlag(flag);
                mDBHelper.updateManager(manager);
                mManagerList.set(index, manager);
                result = true;
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
