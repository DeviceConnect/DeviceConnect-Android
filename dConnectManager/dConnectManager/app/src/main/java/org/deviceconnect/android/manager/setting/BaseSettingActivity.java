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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.DConnectWebService;
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstance = savedInstanceState;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindManager();
    }

    @Override
    protected void onPause() {
        unbindManager();
        super.onPause();
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
     * マネージャ本体とのバインドが切断されたことを通知.
     */
    protected void onManagerLost() {
        // NOP.
    }

    protected boolean isBonded() {
        return mDConnectService != null;
    }

    /**
     * DConnectServiceと接続するためのクラス.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            DConnectService manager = ((DConnectService.LocalBinder) service).getDConnectService();
            mDConnectService = manager;
            onManagerBonded(manager);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
            onManagerLost();
        }
    };

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

    protected DConnectService getManagerService() {
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
