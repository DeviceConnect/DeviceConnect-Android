package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;

public class CameraBaseFragment extends Fragment implements CameraActivity.OnHostDevicePluginListener {
    /**
     * UI 操作用の Handler.
     */
    private Handler mUIHandler = new Handler(Looper.getMainLooper());

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
     * HostDevicePlugin との接続状態を確認します.
     *
     * @return 接続中の場合はtrue、それ以外はfalse
     */
    public boolean isBound() {
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            return ((CameraActivity) activity).isBound();
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
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            return ((CameraActivity) activity).getHostDevicePlugin();
        }
        return null;
    }

    /**
     * 画面回転固定の設定を切り替えます.
     */
    public void toggleDisplayRotation() {
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            ((CameraActivity) activity).toggleDisplayRotation();
        }
    }

    /**
     * 画面回転固定状態を確認します.
     *
     * @return 画面回転を固定している場合は true、それ以外は false
     */
    public boolean isDisplayRotationFixed() {
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            return ((CameraActivity) activity).isDisplayRotationFixed();
        }
        return false;
    }

    public int getDisplayOrientation() {
        Activity activity = getActivity();
        if (activity != null) {
            return ((CameraActivity) activity).getRequestedOrientation();
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
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
     * @param run 実行する Runnable
     * @param delay 遅延する時間(ミリ秒)
     */
    public void postDelay(Runnable run, long delay) {
        mUIHandler.postDelayed(run, delay);
    }
}
