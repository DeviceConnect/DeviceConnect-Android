package org.deviceconnect.android.manager.setting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.WebSocketInfoManager;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.plugin.MessagingException;


public abstract class BaseSettingActivity extends AppCompatActivity {

    /** マネージャ本体のサービスがBindされているかどうか. */
    private boolean mIsBind = false;

    /**
     * DConnectServiceを操作するクラス.
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

    protected void onManagerBonded() {
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
            mDConnectService = ((DConnectService.LocalBinder) service).getDConnectService();
            onManagerBonded();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
        }
    };

    private synchronized void bindManager() {
        if (isBonded()) {
            return;
        }
        Intent bindIntent = new Intent(getApplicationContext(), DConnectService.class);
        mIsBind = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
