/*
 AWSIotDeviceApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * AWS IoT Device Plugin Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceApplication extends Application {

    /** Instance of RemoteDCMManager. */
    private AWSIotRemoteManager mMgr;

    /** My Manager Information. */
    private RemoteDeviceConnectManager mMyManagerInfo;

    /** My Manager Online Flag Key. */
    private final static String AWSIot_MyManagerOnline_Key = "my_manager_online";

    /** My Manager Online Flag */
    private boolean mMyManagerOnline = false;

    /** AWSIoT同期時間 Key. */
    private final static String AWSIot_SyncTime_Key = "sync_time";

    /** AWSIoT同期時間 */
    private long mAWSIotSyncTime = 10;

    /** AWSIotコントローラー */
    private final AWSIotController mIot = new AWSIotController();

    /** AWS IoT Access Key ID */
    String mAccessKeyId;

    /** AWS IoT Secret Access Key ID */
    String mSecretAccessKeyId;

    /** AWS IoT Region */
    Regions mRegion;

    /**
     * Initialize the AWSIoTApplication.
     */
    public void initialize() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

//        if (mMgr == null) {
//            mMgr = new RemoteDCMManager(getApplicationContext());
//        }

//        mMyManagerOnline = sp.getBoolean(AWSIot_MyManagerOnline_Key, false);
        mAWSIotSyncTime = sp.getLong(AWSIot_SyncTime_Key, 10);

        // for test
//        setMyManagerId("testid-aizu-gclue");

    }

    /**
     * Gets a instance of RemoteDCMManager.
     * @return RemoteDCMManager
     */
//    public RemoteDCMManager getRemoteDCMManager() {
//        return mMgr;
//    }

    public String getMyManagerName() {
        return mMyManagerInfo.getName();
    }

    public boolean isMyManagerOnline() {
        return mMyManagerOnline;
    }

    public void setMyManagerOnlineFlag(final boolean flag) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mMyManagerOnline = flag;
        sp.edit().putBoolean(AWSIot_MyManagerOnline_Key, mMyManagerOnline).apply();
    }

    public long getAWSIotSyncTime() {
        return mAWSIotSyncTime;
    }

    public void setAWSIotSyncTime(final long time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mAWSIotSyncTime = time;
        sp.edit().putLong(AWSIot_SyncTime_Key, mAWSIotSyncTime).apply();
    }

    public AWSIotController getAWSIotController() {
        return mIot;
    }

    public String getAccessKeyId() {
        return mAccessKeyId;
    }

    public void setAccessKeyId(final String accessKeyId) {
        mAccessKeyId = accessKeyId;
    }

    public String getSecretAccessKeyId() {
        return mSecretAccessKeyId;
    }

    public void setSecretAccessKeyId(final String secretAccessKeyId) {
        mSecretAccessKeyId = secretAccessKeyId;
    }

    public Regions getRegion() {
        return mRegion;
    }

    public void setRegion(final Regions region) {
        mRegion = region;
    }
}
