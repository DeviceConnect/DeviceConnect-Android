/*
 RestartingDialogFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.plugin.MessagingException;
import org.deviceconnect.android.manager.core.plugin.PluginDetectionException;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;

import static org.deviceconnect.android.manager.core.plugin.PluginDetectionException.Reason.TOO_MANY_PACKAGES;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_progress, null);
        TextView titleView = v.findViewById(R.id.title);
        TextView messageView = v.findViewById(R.id.message);
        titleView.setText(title);
        messageView.setText(msg);
        builder.setView(v);

        return builder.create();
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
                if (mgr == null) {
                    return null;
                }
                List<DevicePlugin> plugins = mgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.isEnabled() && plugin.getPluginId() != null) {
                        if (packageName == null || packageName.equals(plugin.getPackageName())) {
                            Intent request = new Intent();
                            request.setComponent(plugin.getComponentName());
                            request.setAction(IntentDConnectMessage.ACTION_DEVICEPLUGIN_RESET);
                            request.putExtra("pluginId", plugin.getPluginId());
                            try {
                                plugin.send(request);
                            } catch (MessagingException e) {
                                showMessagingErrorDialog(activity, e);
                            }
                        }
                    }
                }
                return null;
            }

            private void showMessagingErrorDialog(final BaseSettingActivity activity,
                                                  final MessagingException e) {
                if (activity != null) {
                    activity.showMessagingErrorDialog(e);
                }
            }

            private void showPluginDetectionErrorDialog(final BaseSettingActivity activity,
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
