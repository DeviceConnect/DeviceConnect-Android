package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;

public class CameraBaseFragment extends Fragment implements CameraActivity.OnHostDevicePluginListener {
    /**
     * UI 操作用の Handler.
     */
    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onBindService() {
    }

    @Override
    public void onUnbindService() {
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

    public String getRecorderId() {
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            return ((CameraActivity) activity).getRecorderId();
        }
        return null;
    }

    public void toggleScreenRotation() {
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            ((CameraActivity) activity).toggleScreenRotation();
        }
    }

    public boolean isScreenRotationFixed() {
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            return ((CameraActivity) activity).isScreenRotationFixed();
        }
        return false;
    }

    /**
     * UI スレッドで Runnable を実行します.
     *
     * @param run 実行する Runnable
     */
    public void runOnUiThread(Runnable run) {
        mUIHandler.post(run);
    }
}
