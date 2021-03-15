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
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;

public abstract class UVCDevicePluginBindActivity extends AppCompatActivity {
    /**
     * UVC プラグイン.
     */
    private UVCDeviceService mUVCDeviceService;

    /**
     * 接続状態を確認するフラグ.
     */
    private boolean mIsBound = false;

    /**
     * UVC サービスの接続・切断イベントを受信するリスナー.
     */
    private final UVCDeviceService.OnEventListener mOnEventListener = new UVCDeviceService.OnEventListener() {
        @Override
        public void onConnected(UVCService service) {
            onUvcConnected(service);
        }

        @Override
        public void onDisconnected(UVCService service) {
            onUvcDisconnected(service);
        }
    };

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
     * UVCDeviceService との接続状態を確認します.
     *
     * @return 接続されている場合は true、それ以外は false
     */
    public boolean isBound() {
        return mIsBound && mUVCDeviceService != null;
    }

    /**
     * UVCDeviceService に接続されたことを通知します.
     *
     * UVCDeviceService に接続された時に処理を行う場合には、このメソッドをオーバーライドします。
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
     * UVCDeviceService から切断されたことを通知します.
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
     * UVC サービスに接続されたことを通知します.
     *
     * @param service 接続された UVC サービス
     */
    protected void onUvcConnected(UVCService service) {
        for (Fragment f : getSupportFragmentManager ().getFragments()) {
            if (f instanceof NavHostFragment) {
                for (Fragment t : f.getChildFragmentManager().getFragments()) {
                    if (t instanceof OnUVCDevicePluginListener) {
                        ((OnUVCDevicePluginListener) t).onUvcConnected(service);
                    }
                }
            } else  if (f instanceof OnUVCDevicePluginListener) {
                ((OnUVCDevicePluginListener) f).onUvcConnected(service);
            }
        }
    }

    /**
     * UVC サービスから切断されたことを通知します.
     *
     * @param service 切断された UVC サービス
     */
    protected void onUvcDisconnected(UVCService service) {
        for (Fragment f : getSupportFragmentManager ().getFragments()) {
            if (f instanceof NavHostFragment) {
                for (Fragment t : f.getChildFragmentManager().getFragments()) {
                    if (t instanceof OnUVCDevicePluginListener) {
                        ((OnUVCDevicePluginListener) t).onUvcDisconnected(service);
                    }
                }
            } else  if (f instanceof OnUVCDevicePluginListener) {
                ((OnUVCDevicePluginListener) f).onUvcDisconnected(service);
            }
        }
    }

    /**
     * UVCDeviceService に接続します.
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
     * UVCDeviceService から切断します.
     */
    public synchronized void unbindService() {
        if (mIsBound) {
            mIsBound = false;
            if (mUVCDeviceService != null) {
                mUVCDeviceService.removeOnEventListener(mOnEventListener);
            }
            onUnbindService();
            unbindService(mConnection);
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mUVCDeviceService = (UVCDeviceService) ((UVCDeviceService.LocalBinder) binder).getMessageService();
            mUVCDeviceService.addOnEventListener(mOnEventListener);
            onBindService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mUVCDeviceService != null) {
                mUVCDeviceService.removeOnEventListener(mOnEventListener);
            }
            mUVCDeviceService = null;
            mIsBound = false;
            onUnbindService();
        }
    };

    /**
     * UVCDeviceService の接続イベントを通知するリスナー.
     */
    public interface OnUVCDevicePluginListener {
        /**
         * UVCDeviceService に接続されたことを通知します.
         */
        void onBindService();

        /**
         * UVCDeviceService から切断されたことを通知します.
         */
        void onUnbindService();

        /**
         * UVC サービスに接続したことを通知します.
         * 
         * @param service UVC サービス
         */
        void onUvcConnected(UVCService service);

        /**
         * UVC サービスから切断されたことを通知します.
         * 
         * @param service UVC サービス
         */
        void onUvcDisconnected(UVCService service);
    }
}
