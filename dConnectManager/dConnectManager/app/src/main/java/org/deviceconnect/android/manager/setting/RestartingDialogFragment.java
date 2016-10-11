package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;

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

    public static void show(final Activity activity) {
        show(activity, null);
    }

    public static void show(final Activity activity, final String packageName) {
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
                DConnectApplication app = (DConnectApplication) activity.getApplication();
                DevicePluginManager mgr = app.getDevicePluginManager();
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
        };
        task.execute();
    }
}
