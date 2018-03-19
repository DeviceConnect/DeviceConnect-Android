/*
 BaseSettingFragment.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
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

/**
 * 設定画面用フラグメントのベースクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BaseSettingFragment extends Fragment {

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
     * マネージャ本体と明示的にアンバインドする直前であることを通知.
     */
    protected void beforeManagerDisconnected() {
        // NOP.
    }

    /**
     * マネージャ本体とのバインドが切断されたことを通知.
     */
    protected void onManagerLost() {
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
            DConnectService manager = ((DConnectService.LocalBinder) service).getDConnectService();
            mDConnectService = manager;
            onManagerBonded(manager);
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
            boolean canBind = activity.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            if (canBind) {
                onManagerBinding();
            } else {
                onCannotManagerBonded();
            }
        }
    }

    private synchronized void unbindManager() {
        if (!isManagerBonded()) {
            return;
        }
        Activity activity = getActivity();
        if (activity != null) {
            beforeManagerDisconnected();
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
