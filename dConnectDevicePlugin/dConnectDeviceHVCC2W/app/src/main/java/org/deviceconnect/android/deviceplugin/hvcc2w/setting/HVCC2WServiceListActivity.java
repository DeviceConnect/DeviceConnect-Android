/*
 HVCC2WServiceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.setting;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.hvcc2w.HVCC2WDeviceService;
import org.deviceconnect.android.deviceplugin.hvcc2w.util.DConnectHelper;
import org.deviceconnect.android.deviceplugin.hvcc2w.util.Utils;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HVC-C2Wサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WServiceListActivity extends DConnectServiceListActivity {
    /**
     * 初回実行待ち時間.
     */
    private static final int CHK_FIRST_WAIT_PERIOD = 5 * 1000;

    /**
     * 周期.
     */
    private static final int CHK_WAIT_PERIOD = 5 * 1000;
    /**
     * ScheduledExecutorServiceのインスタンス.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * サービス検索タイマー.
     */
    private ScheduledFuture<?> mAutoConnectTimerFuture;

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
        start();
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
    }

    /**
     * タイマーのスタート.
     */
    private synchronized void start() {
        if (mAutoConnectTimerFuture != null) {
            return;
        }
        mAutoConnectTimerFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                authorization();
            }
        }, CHK_FIRST_WAIT_PERIOD, CHK_WAIT_PERIOD, TimeUnit.MILLISECONDS);
    }

    /**
     * タイマーのストップ.
     */
    private synchronized void stop() {
        if (mAutoConnectTimerFuture != null) {
            mAutoConnectTimerFuture.cancel(true);
            mAutoConnectTimerFuture = null;
        }
    }


    /**
     * 認証とサービス検索.
     */
    private void authorization() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.connect(HVCC2WServiceListActivity.this, new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
                    @Override
                    public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                        Utils.fetchServices(HVCC2WServiceListActivity.this, new DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>>() {
                            @Override
                            public void onFinish(List<DConnectHelper.ServiceInfo> serviceInfos, Exception error) {

                            }
                        });
                    }
                });

            }
        });
    }
}
