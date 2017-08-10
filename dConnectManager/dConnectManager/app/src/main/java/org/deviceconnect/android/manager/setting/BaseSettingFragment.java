package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;


public abstract class BaseSettingFragment extends Fragment {

    /** マネージャ本体のサービスがBindされているかどうか. */
    private boolean mIsBind = false;

    /**
     * DConnectServiceを操作するクラス.
     */
    private DConnectService mDConnectService;

    @Override
    public void onResume() {
        super.onResume();
        bindManager();
    }

    @Override
    public void onPause() {
        unbindManager();
        super.onPause();
    }

    protected void onManagerBonded() {
        // NOP.
    }

    protected boolean isManagerBonded() {
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
        if (isManagerBonded()) {
            return;
        }
        Activity activity = getActivity();
        if (activity != null) {
            Intent bindIntent = new Intent(activity, DConnectService.class);
            mIsBind = activity.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private synchronized void unbindManager() {
        if (!isManagerBonded()) {
            return;
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.unbindService(mServiceConnection);
            mDConnectService = null;
        }
    }

    protected DevicePluginManager getPluginManager() {
        if (mDConnectService == null) {
            return null;
        }
        return mDConnectService.getPluginManager();
    }
}
