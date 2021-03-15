package org.deviceconnect.android.deviceplugin.host.activity.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;

public abstract class HostDevicePluginBindPreferenceFragment extends PreferenceFragmentCompat implements HostDevicePluginBindActivity.OnHostDevicePluginListener {

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
        return view;
    }

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
     * HostDevicePlugin との接続を確認します.
     *
     * @return 接続されている場合はtrue、それ以外はfalse
     */
    public boolean isBound() {
        Activity activity = getActivity();
        if (activity instanceof HostDevicePluginBindActivity) {
            return ((HostDevicePluginBindActivity) activity).isBound();
        }
        return false;
    }

    /**
     * 接続されている HostDevicePlugin のインスタンスを取得します.
     * <p>
     * 接続されていない場合には null を返却します。
     *
     * @return HostDevicePlugin のインスタンス
     */
    public HostDevicePlugin getHostDevicePlugin() {
        Activity activity = getActivity();
        if (activity instanceof HostDevicePluginBindActivity) {
            return ((HostDevicePluginBindActivity) activity).getHostDevicePlugin();
        }
        return null;
    }

    /**
     * 指定されたキーに対応する入力フォームを数値のみ入力可能に設定します.
     *
     * @param key キー
     */
    public void setInputTypeNumber(String key) {
        EditTextPreference editTextPreference = findPreference(key);
        if (editTextPreference != null) {
            editTextPreference.setOnBindEditTextListener((editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
        }
    }

    /**
     * トーストを表示します.
     *
     * @param resId リソースID
     */
    public void showToast(int resId) {
        runOnUiThread(() -> Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show());
    }

    public void runOnUiThread(Runnable run) {
        mUIHandler.post(run);
    }
}