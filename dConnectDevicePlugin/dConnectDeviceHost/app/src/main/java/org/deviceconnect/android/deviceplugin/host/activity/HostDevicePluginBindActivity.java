package org.deviceconnect.android.deviceplugin.host.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;

public class HostDevicePluginBindActivity extends AppCompatActivity {
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

        if (isLaunchManager()) {
            bindService();
        } else {
            startManager();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isLaunchManager()) {
            bindService();
        }
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
    public boolean isLaunchManager() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.deviceconnect.android.manager".equals(serviceInfo.service.getPackageName())) {
                return true;
            }
        }
        return false;
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
        if (mIsBound) {
            return;
        }

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
            onUnbindService();
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
            mIsBound = false;
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

    public void hideSystemUI() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        decorView.requestLayout();
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    public void showSystemUI() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        decorView.requestLayout();
    }


    public interface OnHostDevicePluginListener {
        /**
         * HostDevicePlugin に接続されたことを通知します.
         */
        void onBindService();

        /**
         * HostDevicePlugin から切断されたことを通知します.
         */
        void onUnbindService();
    }
}
