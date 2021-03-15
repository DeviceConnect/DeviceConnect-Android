package org.deviceconnect.android.deviceplugin.uvc.fragment;

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
import androidx.appcompat.app.ActionBar;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCSettingsActivity;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public abstract class UVCDevicePluginBindPreferenceFragment extends PreferenceFragmentCompat implements UVCDevicePluginBindActivity.OnUVCDevicePluginListener {

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

    @Override
    public void onUvcConnected(UVCService service) {
    }

    @Override
    public void onUvcDisconnected(UVCService service) {
    }

    /**
     * 前の画面に戻ります.
     *
     * 前の画面がない場合には Activity を終了します。
     */
    public void popBackFragment() {
        int entryCount = getParentFragmentManager().getBackStackEntryCount();
        if (entryCount == 0) {
            getActivity().finish();
        } else {
            findNavController(this).popBackStack();
        }
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
     * UVCDeviceService との接続を確認します.
     *
     * @return 接続されている場合はtrue、それ以外はfalse
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

    /**
     * UI スレッドで Runnable を実行します.
     *
     * @param run 実行する Runnable
     */
    public void runOnUiThread(Runnable run) {
        mUIHandler.post(run);
    }
}