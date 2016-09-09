/*
 AWSIotDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.profile.AWSIotServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.awsiot.profile.AWSIotSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceService extends DConnectMessageService {
    private static final String TAG_LOCAL = "AWS-Local";

    private AWSIotRemoteManager mAWSIotRemoteManager;
    private AWSIotPrefUtil mPrefUtil;

    @Override
    public void onCreate() {
        android.os.Debug.waitForDebugger();
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());
        mPrefUtil = new AWSIotPrefUtil(this);
        mAWSIotRemoteManager = new AWSIotRemoteManager(this);

//        getLocalDevice();
        // TODO 開始タイミングを検討すること
        startAWSIot();

        addProfile(new AWSIotServiceDiscoveryProfile(mAWSIotRemoteManager, getServiceProvider()));
    }

    @Override
    public void onDestroy() {
        stopAWSIot();
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new AWSIotSystemProfile();
    }


    @Override
    protected void onManagerUninstalled() {
        stopAWSIot();
    }

    @Override
    protected void onDevicePluginReset() {
        stopAWSIot();
        startAWSIot();
    }

    @Override
    protected boolean executeRequest(final String profileName, final Intent request, final Intent response) {
        DConnectProfile profile = getProfile(profileName);
        if (profile == null) {
            return mAWSIotRemoteManager.sendRequest(request, response);
        } else {
            return profile.onRequest(request, response);
        }
    }

    private void startAWSIot() {
        availability(new DConnectLocalHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        AWSIotPrefUtil pref = new AWSIotPrefUtil(AWSIotDeviceService.this);
                        String name = jsonObject.optString("name");
                        String uuid = jsonObject.optString("uuid");
                        if (name == null || uuid == null) {
                            // TODO 古いManager場合の処理
                            String prefName = pref.getManagerName();
                            name = "TEST";
                            if (prefName.matches(name)) {
                                uuid = pref.getManagerUuid();
                            }
                            if (uuid == null) {
                                uuid = UUID.randomUUID().toString();
                            }
                        }
                        pref.setManagerName(name);
                        pref.setManagerUuid(uuid);

                        startAWSIot(name, uuid);
                    } else {
                        // TODO Managerが起動していない場合の処理
                    }
                } catch (JSONException e) {
                    Log.e(TAG_LOCAL, "", e);
                }
            }
        });
    }

    private synchronized void startAWSIot(final String name, final String uuid) {
        if (mAWSIotRemoteManager != null) {
            mAWSIotRemoteManager.disconnect();
        }

        if (mPrefUtil.getAccessKey() == null ||
                mPrefUtil.getManagerUuid() == null ||
                mPrefUtil.getRegions() == null) {
            Log.d("ABC", "Login data failed.");
        } else {
//            mAWSIotRemoteManager = new AWSIotRemoteManager(this);
            mAWSIotRemoteManager.connectAWSIoT(mPrefUtil.getAccessKey(),
                    mPrefUtil.getSecretKey(), mPrefUtil.getRegions());
        }
    }

    private void stopAWSIot() {
        if (mAWSIotRemoteManager != null) {
            mAWSIotRemoteManager.disconnect();
        }
    }

    private void availability(final DConnectLocalHelper.FinishCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DConnectLocalHelper.INSTANCE.sendRequest("GET", "http://localhost:4035/gotapi/availability", callback);
            }
        }).start();
    }

    public AWSIotRemoteManager getAWSIotRemoteManager() {
        return mAWSIotRemoteManager;
    }

    public AWSIotPrefUtil getPrefUtil() {
        return mPrefUtil;
    }

    private void getLocalDevice() {
        serviceDiscovery(new DConnectLocalHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        Log.d("ABC", "result : " + jsonObject.toString());
                    } else {
                        // TODO Managerが起動していない場合の処理
                    }
                } catch (JSONException e) {
                }
            }
        });

    }

    private void serviceDiscovery(final DConnectLocalHelper.FinishCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DConnectLocalHelper.INSTANCE.sendRequest("GET", "http://localhost:4035/gotapi/servicediscovery", callback);
            }
        }).start();
    }

}