package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCSettingsActivity;

public class UVCDevicePluginBindFragment extends Fragment implements UVCDevicePluginBindActivity.OnUVCDevicePluginListener {
    /**
     * UI 操作用の Handler.
     */
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onResume() {
        super.onResume();

        if (isBound()) {
            onBindService();
        }
    }

    @Override
    public void onBindService() {
    }

    @Override
    public void onUnbindService() {
    }

    /**
     * ActionBar にタイトルを設定します.
     *
     * @param title タイトル
     */
    public void setTitle(String title) {
        Activity activity = getActivity();
        if (activity instanceof UVCDevicePluginBindActivity) {
            ActionBar actionBar = ((UVCDevicePluginBindActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(title);
            }
        }
    }

    /**
     * HostDevicePlugin との接続状態を確認します.
     *
     * @return 接続中の場合はtrue、それ以外はfalse
     */
    public boolean isBound() {
        Activity activity = getActivity();
        if (activity instanceof UVCDevicePluginBindActivity) {
            return ((UVCDevicePluginBindActivity) activity).isBound();
        }
        return false;
    }

    /**
     * 接続されている UVCDeviceService のインスタンスを取得します.
     * <p>
     * 接続されていない場合には null を返却します。
     *
     * @return UVCDeviceService のインスタンス
     */
    public UVCDeviceService getUVCDeviceService() {
        Activity activity = getActivity();
        if (activity instanceof UVCDevicePluginBindActivity) {
            return ((UVCDevicePluginBindActivity) activity).getUVCDeviceService();
        }
        return null;
    }

    /**
     * 画面の向き設定を取得します.
     *
     * @return 画面の向き設定
     */
    public int getDisplayOrientation() {
        Activity activity = getActivity();
        if (activity != null) {
            return activity.getRequestedOrientation();
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /**
     * トーストを表示します.
     *
     * @param resId リソースID
     */
    public void showToast(int resId) {
        runOnUiThread(() -> {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * UI スレッドで Runnable を実行します.
     *
     * @param run 実行する Runnable
     */
    public void runOnUiThread(Runnable run) {
        mUIHandler.post(run);
    }

    /**
     * Runnable を指定された delay の分だけ後に実行します.
     *
     * @param run   実行する Runnable
     * @param delay 遅延する時間(ミリ秒)
     */
    public void postDelay(Runnable run, long delay) {
        mUIHandler.postDelayed(run, delay);
    }
}