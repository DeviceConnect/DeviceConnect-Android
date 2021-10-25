package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.activity.fragment.HostDevicePluginBindPreferenceFragment;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public abstract class SettingsBaseFragment extends HostDevicePluginBindPreferenceFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
        return view;
    }

    /**
     * 選択されているレコーダを取得します.
     *
     * プラグインに接続されていない場合は null を返却します。
     *
     * @return HostMediaRecorder のインスタンス
     */
    public HostMediaRecorder getRecorder() {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            return ((SettingsActivity) activity).getRecorder();
        }
        return null;
    }

    /**
     * レコーダ ID を取得します.
     *
     * @return レコーダ ID
     */
    public String getRecorderId() {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            return ((SettingsActivity) activity).getRecorderId();
        }
        return null;
    }

    /**
     * プレビューサーバ、配信設定ファイル名を取得します.
     *
     * @return 設定のファイル名
     */
    public String getEncoderId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString("encoder_id");
        }
        return null;
    }
}
