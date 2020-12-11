package org.deviceconnect.android.deviceplugin.host.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;

public class HostDevicePluginBindActivity extends Activity {
    /**
     * 接続している HostDevicePlugin のインスタンス.
     */
    private HostDevicePlugin mHostDevicePlugin;

    /**
     * 接続状態を確認するフラグ.
     */
    private boolean mIsBound = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    /**
     * 接続されている HostDevicePlugin のインスタンスを取得します.
     *
     * 接続されていない場合には null を返却します。
     *
     * @return HostDevicePlugin のインスタンス
     */
    public HostDevicePlugin getHostDevicePlugin() {
        return mHostDevicePlugin;
    }

    /**
     * HostDevicePlugin との接続状態を確認します.
     *
     * @return 接続されている場合は true、それ以外は false
     */
    public boolean isBound() {
        return mIsBound;
    }

    /**
     * HostDevicePlugin に接続されたことを通知します.
     */
    protected void onBindService() {
    }

    /**
     * HostDevicePlugin から切断されたことを通知します.
     */
    protected void onUnbindService() {
    }

    /**
     * HostDevicePlugin に接続します.
     */
    public void bindService() {
        Intent intent = new Intent(this, HostDevicePlugin.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * HostDevicePlugin から切断します.
     */
    public void unbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mHostDevicePlugin = (HostDevicePlugin) ((HostDevicePlugin.LocalBinder) binder).getMessageService();
            onBindService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHostDevicePlugin = null;
            onUnbindService();
        }
    };
}
