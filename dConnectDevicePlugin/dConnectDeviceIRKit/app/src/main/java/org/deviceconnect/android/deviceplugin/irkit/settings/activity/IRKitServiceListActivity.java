/*
 IRKitServiceListActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.activity;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.service.IRKitService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * IRKitサービス一覧画面.
 * @author NTT DOCOMO, INC.
 */
public class IRKitServiceListActivity extends DConnectServiceListActivity {
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
        return IRKitDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return IRKitSettingActivity.class;
    }

    @Override
    protected ListItemFilter getListItemFilter() {
        return new ListItemFilter() {
            @Override
            public boolean doFilter(final DConnectService service) {
                return service instanceof IRKitService;
            }
        };
    }

    @Override
    protected boolean enablesItemClick() {
        return true;
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        Intent intent = new Intent(getApplicationContext(), IRKitVirtualDeviceListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IRKitVirtualDeviceListActivity.EXTRA_SERVICE_ID, service.getId());
        startActivity(intent);
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
                Intent i = new Intent(IRKitServiceListActivity.this, IRKitDeviceService.class);
                i.setAction(IRKitDeviceService.ACTION_RESTART_DETECTION_IRKIT);
                LocalBroadcastManager.getInstance(IRKitServiceListActivity.this).sendBroadcast(i);
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
}
