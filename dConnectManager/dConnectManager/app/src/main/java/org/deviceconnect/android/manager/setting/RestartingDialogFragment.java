/*
 RestartingDialogFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.plugin.PluginDetectionException;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;

/**
 * デバイスプラグインの再起動要求中ダイアログ.
 *
 * @author NTT DOCOMO, INC.
 */
public class RestartingDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String title = getString(R.string.activity_settings_restart_device_plugin_title);
        String msg = getString(R.string.activity_settings_restart_device_plugin_message);
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);
        return progressDialog;
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }


    public static void show(final BaseSettingActivity activity) {
        show(activity, null);
    }

    public static void show(final BaseSettingActivity activity, final String packageName) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private RestartingDialogFragment mDialog;
            @Override
            protected void onPreExecute() {
                mDialog = new RestartingDialogFragment();
                mDialog.show(activity.getFragmentManager(), null);
            }

            @Override
            protected void onPostExecute(final Void aVoid) {
                if (mDialog.isResumed()) {
                    mDialog.dismiss();
                }
            }

            @Override
            protected Void doInBackground(final Void... params) {
                DevicePluginManager mgr = activity.getPluginManager();
                try {
                    mgr.createDevicePluginList();
                } catch (PluginDetectionException e) {
                    showErrorMessage(activity, e);
                    return null;
                }

                List<DevicePlugin> plugins = mgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.getStartServiceClassName() != null && plugin.getPluginId() != null) {
                        if (packageName == null || packageName.equals(plugin.getPackageName())) {
                            Intent request = new Intent();
                            request.setComponent(plugin.getComponentName());
                            request.setAction(IntentDConnectMessage.ACTION_DEVICEPLUGIN_RESET);
                            request.putExtra("pluginId", plugin.getPluginId());
                            activity.sendBroadcast(request);
                        }
                    }
                }
                return null;
            }

            private void showErrorMessage(final BaseSettingActivity activity,
                                          final PluginDetectionException e) {
                // エラーメッセージ初期化
                int messageId;
                switch (e.getReason()) {
                    case TOO_MANY_PACKAGES:
                        messageId = R.string.dconnect_error_plugin_not_detected_due_to_too_many_packages;
                        break;
                    default:
                        messageId = R.string.dconnect_error_plugin_not_detected_due_to_unknown_error;
                        break;
                }
                final String message = activity.getString(messageId);
                final String title = activity.getString(R.string.dconnect_error_plugin_not_detected_title);

                // エラーダイアログ表示
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog.isResumed()) {
                            Bundle args = new Bundle();
                            args.putString(ErrorDialogFragment.EXTRA_TITLE, title);
                            args.putString(ErrorDialogFragment.EXTRA_MESSAGE, message);
                            ErrorDialogFragment f = new ErrorDialogFragment();
                            f.setArguments(args);
                            f.show(activity.getSupportFragmentManager(), "error");
                        }
                    }
                });
            }
        };
        task.execute();
    }
}
