package org.deviceconnect.android.deviceplugin.host.activity.settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.activity.camera.CameraActivity;

public abstract class SettingsBaseFragment extends PreferenceFragmentCompat implements CameraActivity.OnHostDevicePluginListener {

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
        if (activity instanceof SettingsActivity) {
            return ((SettingsActivity) activity).isBound();
        }
        return false;
    }

    /**
     * レコード ID を取得します.
     *
     * @return レコード ID
     */
    public String getRecorderId() {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            return ((SettingsActivity) activity).getRecorderId();
        }
        return null;
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
        if (activity instanceof SettingsActivity) {
            return ((SettingsActivity) activity).getHostDevicePlugin();
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
}
