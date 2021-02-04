/*
 HostSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.HostDevicePluginBindPreferenceFragment;
import org.deviceconnect.android.deviceplugin.host.activity.recorder.camera.CameraActivity;
import org.deviceconnect.android.deviceplugin.host.activity.recorder.screencast.ScreencastActivity;
import org.deviceconnect.android.deviceplugin.host.activity.recorder.settings.SettingsActivity;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.util.ArrayList;
import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

/**
 * Host プラグインの設定全体を管理するフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostSettingFragment extends HostDevicePluginBindPreferenceFragment {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.settings_host_plugin);
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        boolean result = super.onPreferenceTreeClick(preference);

        Activity activity = getActivity();
        if (activity == null) {
            return result;
        }
        Context context = activity.getApplicationContext();

        // 各説明をダイアログで表示
        Intent intent = null;
        if (getString(R.string.pref_key_settings_gps).equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_settings_to_gps);
        } else if (getString(R.string.pref_key_settings_app_camera).equals(preference.getKey())) {
            intent = new Intent(context, CameraActivity.class);
        } else if (getString(R.string.pref_key_settings_app_screen_capture).equals(preference.getKey())) {
            intent = new Intent(context, ScreencastActivity.class);
        } else if (getString(R.string.pref_key_settings_demo_page).equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_settings_to_demo);
        }
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }

        return result;
    }

    @Override
    public void onBindService() {
        PreferenceCategory pref = findPreference("host_recorder_list");
        if (pref != null && pref.getPreferenceCount() == 0) {
            List<HostMediaRecorder> recorderList = getRecorderList();
            for (HostMediaRecorder recorder : recorderList) {
                PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getContext());
                screen.setTitle(recorder.getName());
                screen.setIconSpaceReserved(false);
                screen.setOnPreferenceClickListener(preference -> {
                    Context context = getContext();
                    if (context != null) {
                        SettingsActivity.startActivity(context, recorder.getId(), null);
                    }
                    return false;
                });
                pref.addPreference(screen);
            }
        }
    }

    /**
     * サポートしているレコーダのリストを取得します。
     *
     * プラグインに接続されていない場合は空のリストを返却します。
     *
     * @return HostMediaRecorder のリスト
     */
    private List<HostMediaRecorder> getRecorderList() {
        Activity activity = getActivity();
        if (activity instanceof HostSettingActivity) {
            return ((HostSettingActivity) activity).getRecorderList();
        }
        return new ArrayList<>();
    }
}
