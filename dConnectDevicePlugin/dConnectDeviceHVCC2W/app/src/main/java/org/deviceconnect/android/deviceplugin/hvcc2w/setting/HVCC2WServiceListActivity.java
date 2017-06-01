/*
 HVCC2WServiceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.setting;


import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hvcc2w.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcc2w.HVCC2WDeviceService;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcc2w.service.HVCC2WService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * HVC-C2Wサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WServiceListActivity extends DConnectServiceListActivity {
    /** Timer 用Handler.*/
    private final Handler mHandler = new Handler();
   @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return HVCC2WDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return SettingActivity.class;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
                mHandler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * タイマーのスタート.
     */
    private synchronized void start() {
        HVCManager.INSTANCE.getCameraList(new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {

                final Map<String, HVCCameraInfo> devices = HVCManager.INSTANCE.getHVCDevices();
                final CountDownLatch countDownLatch = new CountDownLatch(devices.size());
                final DConnectServiceProvider provider = HVCC2WServiceListActivity.super.getMessageService().getServiceProvider();
                final List<DConnectService> disappeared = provider.getServiceList();
                for (String key : devices.keySet()) {
                    final HVCCameraInfo camera = devices.get(key);
                    HVCManager.INSTANCE.setCamera(camera.getID(), new HVCManager.ResponseListener() {
                        @Override
                        public void onReceived(String json) {
                            if (json == null) {
                                countDownLatch.countDown();
                                return;
                            }
                            try {
                                JSONObject result = new JSONObject(json);
                                if (result.getInt("result") != 1) {
                                    countDownLatch.countDown();
                                    return;
                                }
                            } catch (JSONException e) {
                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace();
                                }
                                countDownLatch.countDown();
                                return;
                            }

                            // 検出されたデバイスについてのサービスを登録.
                            DConnectService hvcService = provider.getService(camera.getID());
                            if (hvcService == null) {
                                hvcService = new HVCC2WService(camera);
                                provider.addService(hvcService);
                            } else {
                                for (Iterator<DConnectService> it = disappeared.iterator(); ; it.hasNext()) {
                                    DConnectService cache = it.next();
                                    if (cache.getId().equals(hvcService.getId())) {
                                        it.remove();
                                        break;
                                    }
                                }
                            }
                            // 検出されたデバイスはオンライン状態とみなす.
                            hvcService.setOnline(true);

                            countDownLatch.countDown();
                        }
                    });
                }
                try {
                    countDownLatch.await();

                    // 消失したデバイスについてはオフライン状態とみなす.
                    for (DConnectService service : disappeared) {
                        service.setOnline(false);
                    }
                } catch (InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}
