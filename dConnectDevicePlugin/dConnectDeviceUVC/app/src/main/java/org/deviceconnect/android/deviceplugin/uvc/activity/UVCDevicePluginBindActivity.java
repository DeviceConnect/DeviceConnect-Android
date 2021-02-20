package org.deviceconnect.android.deviceplugin.uvc.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;

public abstract class UVCDevicePluginBindActivity extends AppCompatActivity {
    /**
     * UVC プラグイン.
     */
    private UVCDeviceService mUVCDeviceService;

    /**
     * 接続状態を確認するフラグ.
     */
    private boolean mIsBound = false;

    @Override
    public void onResume() {
        super.onResume();
        bindService();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    /**
     * UVC プラグインを取得します.
     *
     * 接続されていない場合は null を返却します。
     *
     * @return UVC プラグイン
     */
    public UVCDeviceService getUVCDeviceService() {
        return mUVCDeviceService;
    }

    /**
     * HostDevicePlugin との接続状態を確認します.
     *
     * @return 接続されている場合は true、それ以外は false
     */
    public boolean isBound() {
        return mIsBound && mUVCDeviceService != null;
    }

    /**
     * HostDevicePlugin に接続されたことを通知します.
     *
     * HostDevicePlugin に接続された時に処理を行う場合には、このメソッドをオーバーライドします。
     */
    protected void onBindService() {
        for (Fragment f : getSupportFragmentManager ().getFragments()) {
            if (f instanceof NavHostFragment) {
                for (Fragment t : f.getChildFragmentManager().getFragments()) {
                    if (t instanceof OnUVCDevicePluginListener) {
                        ((OnUVCDevicePluginListener) t).onBindService();
                    }
                }
            } else  if (f instanceof OnUVCDevicePluginListener) {
                ((OnUVCDevicePluginListener) f).onBindService();
            }
        }
    }

    /**
     * HostDevicePlugin から切断されたことを通知します.
     */
    protected void onUnbindService() {
        for (Fragment f : getSupportFragmentManager ().getFragments()) {
            if (f instanceof NavHostFragment) {
                for (Fragment t : f.getChildFragmentManager().getFragments()) {
                    if (t instanceof OnUVCDevicePluginListener) {
                        ((OnUVCDevicePluginListener) t).onUnbindService();
                    }
                }
            } else  if (f instanceof OnUVCDevicePluginListener) {
                ((OnUVCDevicePluginListener) f).onUnbindService();
            }
        }
    }

    /**
     * HostDevicePlugin に接続します.
     */
    public synchronized void bindService() {
        if (mIsBound) {
            return;
        }
        mIsBound = true;

        Intent intent = new Intent(getApplicationContext(), UVCDeviceService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * HostDevicePlugin から切断します.
     */
    public synchronized void unbindService() {
        if (mIsBound) {
            mIsBound = false;
            onUnbindService();
            unbindService(mConnection);
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mUVCDeviceService = (UVCDeviceService) ((UVCDeviceService.LocalBinder) binder).getMessageService();
            onBindService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUVCDeviceService = null;
            mIsBound = false;
            onUnbindService();
        }
    };

    /**
     * UVCDevicePlugin の接続イベントを通知するリスナー.
     */
    public interface OnUVCDevicePluginListener {
        /**
         * UVCDevicePlugin に接続されたことを通知します.
         */
        void onBindService();

        /**
         * UVCDevicePlugin から切断されたことを通知します.
         */
        void onUnbindService();
    }
}
