/*
 ManagerTerminationFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
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

/**
 * Device Connect Manger終了要求中ダイアログ.
 *
 * @author NTT DOCOMO, INC.
 */
public class ManagerTerminationFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String title = getString(R.string.activity_settings_manager_terminate_title);
        String msg = getString(R.string.activity_settings_manager_terminate_message);
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
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private ManagerTerminationFragment mDialog;
            @Override
            protected void onPreExecute() {
                mDialog = new ManagerTerminationFragment();
                mDialog.show(activity.getFragmentManager(), null);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mDialog.isResumed()) {
                    mDialog.dismiss();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                DConnectApplication app = (DConnectApplication) activity.getApplication();
                DevicePluginManager mgr = app.getDevicePluginManager();
                List<DevicePlugin> plugins = mgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.getPluginId() != null) {
                        Intent request = new Intent();
                        request.setComponent(plugin.getComponentName());
                        request.setAction(IntentDConnectMessage.ACTION_MANAGER_TERMINATED);
                        request.putExtra("pluginId", plugin.getPluginId());
                        activity.sendBroadcast(request);
                    }
                }
                return null;
            }
        };
        task.execute();
    }
}
