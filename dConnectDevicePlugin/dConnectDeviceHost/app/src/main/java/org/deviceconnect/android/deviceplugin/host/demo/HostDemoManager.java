package org.deviceconnect.android.deviceplugin.host.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.host.R;

import java.io.File;
import java.io.IOException;

public class HostDemoManager {

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * デモページインストーラ.
     */
    private DemoInstaller mDemoInstaller;

    /**
     * デモページアップデート通知.
     */
    private DemoInstaller.Notification mDemoNotification;

    public HostDemoManager(Context context) {
        mContext = context;
        initDemoInstaller();
    }

    public void destroy() {
        unregsiterDemoNotification();
    }

    private void initDemoInstaller() {
        mDemoInstaller = new HostDemoInstaller(mContext);
        mDemoNotification = new DemoInstaller.Notification(
                1,
                mContext.getString(R.string.app_name_host),
                R.drawable.dconnect_icon,
                "org.deviceconnect.android.deviceconnect.host.channel.demo",
                "Host Plugin Demo Page",
                "Host Plugin Demo Page"
        );
        registerDemoNotification();
        updateDemoPageIfNeeded();
    }

    private void registerDemoNotification() {
        IntentFilter filter  = new IntentFilter();
        filter.addAction(DemoInstaller.Notification.ACTON_CONFIRM_NEW_DEMO);
        filter.addAction(DemoInstaller.Notification.ACTON_UPDATE_DEMO);
        mContext.registerReceiver(mDemoNotificationReceiver, filter);
    }

    private void unregsiterDemoNotification() {
        try {
            mContext.unregisterReceiver(mDemoNotificationReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    private void updateDemoPageIfNeeded() {
        if (mDemoInstaller.isUpdateNeeded()) {
            updateDemoPage(mContext);
        }
    }

    private void updateDemoPage(final Context context) {
        mDemoInstaller.update(new DemoInstaller.UpdateCallback() {
            @Override
            public void onBeforeUpdate(final File demoDir) {
            }

            @Override
            public void onAfterUpdate(final File demoDir) {
                mDemoNotification.showUpdateSuccess(context);
            }

            @Override
            public void onFileError(final IOException e) {
                mDemoNotification.showUpdateError(context);
            }

            @Override
            public void onUnexpectedError(final Throwable e) {
                mDemoNotification.showUpdateError(context);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    /**
     * デモページ関連の通知を受信するレシーバー.
     */
    private final BroadcastReceiver mDemoNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            mDemoNotification.cancel(context);
            if (DemoInstaller.Notification.ACTON_UPDATE_DEMO.equals(action)) {
                updateDemoPage(context);
            }
        }
    };
}
