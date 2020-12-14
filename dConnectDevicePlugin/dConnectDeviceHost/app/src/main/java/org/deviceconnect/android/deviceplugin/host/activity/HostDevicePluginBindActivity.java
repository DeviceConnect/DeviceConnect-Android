package org.deviceconnect.android.deviceplugin.host.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Surface;

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

    /**
     * 画面の回転固定フラグ.
     */
    private boolean mScreenRotationFixed = false;

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
     *
     * HostDevicePlugin に接続された時に処理を行う場合には、このメソッドをオーバーライドします。
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

    private final ServiceConnection mConnection = new ServiceConnection() {
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

    /**
     * 画面に表示されている状態で回転を固定します.
     *
     * @param fixed 固定する場合はtrue、それ以外はfalse
     */
    public void setDisplayRotation(boolean fixed) {
        mScreenRotationFixed = fixed;
        if (fixed) {
            setRequestedOrientation(getScreenOrientation());
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    /**
     * 画面回転固定状態を確認します.
     *
     * @return 画面が固定されている場合はtrue、それ以外はfalse
     */
    public boolean isScreenRotationFixed() {
        return mScreenRotationFixed;
    }

    /**
     * 画面固定を切り替えます.
     */
    public void toggleScreenRotation() {
        setDisplayRotation(!mScreenRotationFixed);
    }

    /**
     * 画面の向きに合わせた固定フラグを取得します.
     *
     * @return 画面の向きに合わせた固定フラグ
     */
    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                default:
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_180:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
        } else {
            switch(rotation) {
                default:
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_180:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
        }
    }
}
