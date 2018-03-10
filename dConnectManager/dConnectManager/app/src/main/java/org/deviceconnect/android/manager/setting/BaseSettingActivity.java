/*
 BaseSettingActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.WebSocketInfoManager;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.plugin.MessagingException;


/**
 * 設定画面のベースクラス.
 *
 * <p>
 * 画面起動時にマネージャ本体とのバインドし、マネージャ本体を制御できるようにする.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BaseSettingActivity extends AppCompatActivity {

    /**
     * マネージャ本体を操作するクラス.
     */
    private DConnectService mDConnectService;

    /**
     * 前回起動時に保存していたインスタンス.
     */
    private Bundle mSavedInstance;

    /**
     * マネージャのサーバー起動完了を待機するスレッド.
     *
     * マネージャ本体のサービスとバインドした際にスレッドのインスタンスを作成する.
     * マネージャのサーバー起動完了確認、予期しないバインド切断、または本画面非表示の際に破棄する.
     */
    private Thread mManagerMonitorThread;

    private final Object mManagerMonitorLock = new Object();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstance = savedInstanceState;
        bindManager();
    }

    @Override
    protected void onDestroy() {
        stopManagerMonitor();
        unbindManager();
        super.onDestroy();
    }

    protected boolean hasSavedInstance() {
        return mSavedInstance != null;
    }

    /**
     * マネージャ本体とのバインド待ち状態になったことを通知.
     */
    protected void onManagerBinding() {
        // NOP.
    }

    /**
     * マネージャ本体とのバインドが不可能な状態であることを通知.
     */
    protected void onCannotManagerBonded() {
        // NOP.
    }

    /**
     * マネージャ本体とバインドしたことを通知.
     * @param manager マネージャ本体
     */
    protected void onManagerBonded(final DConnectService manager) {
        // NOP.
    }

    /**
     * マネージャの起動状態を通知.
     * @param manager マネージャ本体
     * @param isRunning サーバ起動フラグ
     */
    protected void onManagerDetected(final DConnectService manager, final boolean isRunning) {
        // NOP.
    }

    /**
     * マネージャ本体とのバインドが切断されたことを通知.
     */
    protected void onManagerLost() {
        // NOP.
    }

    protected boolean isBonded() {
        return mDConnectService != null;
    }

    protected boolean isDConnectServiceRunning() {
        return mDConnectService != null && mDConnectService.isRunning();
    }

    /**
     * DConnectServiceと接続するためのクラス.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            DConnectService manager = ((DConnectService.LocalBinder) service).getDConnectService();
            mDConnectService = manager;
            startManagerMonitor(manager);
            onManagerBonded(manager);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
            onManagerLost();
            stopManagerMonitor();
        }
    };

    private void startManagerMonitor(final DConnectService manager) {
        synchronized (mManagerMonitorLock) {
            if (mManagerMonitorThread == null) {
                mManagerMonitorThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean isRunning = waitForManagerStartup(manager);
                            onManagerDetected(manager, isRunning);
                        } catch (InterruptedException e) {
                            // NOP.
                        } finally {
                            mManagerMonitorThread = null;
                        }
                    }
                });
                mManagerMonitorThread.start();
            }
        }
    }

    private void stopManagerMonitor() {
        synchronized (mManagerMonitorLock) {
            if (mManagerMonitorThread != null) {
                mManagerMonitorThread.interrupt();
            }
        }
    }

    /**
     * マネージャの起動設定がONの場合は、サーバー起動完了するまで、スレッドをブロックする.
     *
     * それ以外の場合は、即座に処理を返す.
     *
     * @param manager マネージャ本体のサービス
     * @return 起動完了を確認した場合は<code>true</code>、それ以外の場合は<code>false</code>
     * @throws InterruptedException キャンセルされた場合
     */
    private boolean waitForManagerStartup(final DConnectService manager) throws InterruptedException {
        DConnectApplication application = (DConnectApplication) getApplication();
        DConnectSettings settings = application.getSettings();
        boolean isRunning = manager.isRunning();
        if (!isRunning && settings.isManagerStartFlag()) {
            while (!manager.isRunning()) {
                Thread.sleep(100);
            }
            return true;
        }
        return isRunning;
    }

    private synchronized void bindManager() {
        if (isBonded()) {
            return;
        }
        Intent bindIntent = new Intent(getApplicationContext(), DConnectService.class);
        boolean canBind = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (canBind) {
            onManagerBinding();
        } else {
            onCannotManagerBonded();
        }
    }

    private synchronized void unbindManager() {
        if (!isBonded()) {
            return;
        }
        unbindService(mServiceConnection);
        mDConnectService = null;
    }

    protected WebSocketInfoManager getWebSocketInfoManager() {
        if (mDConnectService == null) {
            return null;
        }
        return mDConnectService.getWebSocketInfoManager();
    }

    protected DevicePluginManager getPluginManager() {
        if (mDConnectService == null) {
            return null;
        }
        return mDConnectService.getPluginManager();
    }

    protected Boolean isSSL() {
        DConnectApplication application = (DConnectApplication) getApplication();
        DConnectSettings settings = application.getSettings();
        if (settings == null) {
            return null;
        }
        return settings.isSSL();
    }

    public DConnectService getManagerService() {
        return mDConnectService;
    }

    public void showMessagingErrorDialog(final MessagingException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String errorMessage = findErrorMessage(e);

                Bundle args = new Bundle();
                args.putString(ErrorDialogFragment.EXTRA_MESSAGE, errorMessage);
                ErrorDialogFragment f = new ErrorDialogFragment();
                f.setArguments(args);
                f.show(getSupportFragmentManager(), "error");
            }
        });
    }

    private String findErrorMessage(final MessagingException e) {
        switch (e.getReason()) {
            case NOT_ENABLED:
                return getString(R.string.dconnect_error_plugin_not_enabled);
            case CONNECTION_SUSPENDED:
                return getString(R.string.dconnect_error_plugin_suspended);
            default:
                return getString(R.string.dconnect_error_plugin_not_connected);
        }
    }
}
