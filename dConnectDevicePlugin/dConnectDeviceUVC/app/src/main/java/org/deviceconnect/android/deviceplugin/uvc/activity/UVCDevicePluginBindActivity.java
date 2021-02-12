package org.deviceconnect.android.deviceplugin.uvc.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;

public class UVCDevicePluginBindActivity extends AppCompatActivity {
    /**
     * UVC プラグイン.
     */
    private UVCDeviceService mUVCDeviceService;

    /**
     * 接続状態を確認するフラグ.
     */
    private boolean mIsBound = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isManagerStarted()) {
        }
        bindService();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    /**
     * Manager を起動します.
     */
    public void startManager() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("gotapi://start/server"));
        intent.setPackage("org.deviceconnect.android.manager");
        startActivity(intent);
    }

    /**
     * Manager の起動を確認します.
     *
     * @return 起動している場合は true、それ以外は false
     */
    public boolean isManagerStarted() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.deviceconnect.android.manager".equals(serviceInfo.service.getPackageName())) {
                return true;
            }
        }
        return false;
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
    public void bindService() {
        if (mIsBound) {
            return;
        }
        mIsBound = true;

        Intent intent = new Intent(this, UVCDeviceService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * HostDevicePlugin から切断します.
     */
    public void unbindService() {
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
