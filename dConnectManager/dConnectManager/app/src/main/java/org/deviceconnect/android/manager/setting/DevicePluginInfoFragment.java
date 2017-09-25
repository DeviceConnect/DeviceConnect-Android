/*
 DevicePluginInfoFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.deviceconnect.android.localoauth.DevicePluginXmlProfile;
import org.deviceconnect.android.localoauth.DevicePluginXmlProfileLocale;
import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.plugin.ConnectionError;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.plugin.CommunicationHistory;
import org.deviceconnect.android.manager.plugin.MessagingException;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Device plug-in information fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginInfoFragment extends BaseSettingFragment {

    /** デバイスプラグインをアンインストールする際のリクエストコード. */
    private static final int REQUEST_CODE = 101;

    /** デバイスプラグイン情報. */
    private DevicePlugin.Info mPluginInfo;

    /** プラグイン接続エラー表示. */
    private ConnectionErrorView mErrorView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deviceplugin_info, container, false);
    }

    /**
     * プラグイン情報を更新する.
     * @param plugin 更新するプラグイン
     */
    public void updateInfo(final DevicePlugin plugin) {
        View view = getView();
        if (view == null) {
            return;
        }

        mPluginInfo = plugin.getInfo();

        boolean isEnabled = plugin.isEnabled();

        String packageName = mPluginInfo.getPackageName();
        Integer iconId = mPluginInfo.getPluginIconId();
        String name = mPluginInfo.getDeviceName();
        Drawable icon = DConnectUtil.loadPluginIcon(getActivity(), packageName, iconId);
        String versionName = mPluginInfo.getVersionName();
        String managerPackageName = getActivity().getPackageName();

        TextView nameView = (TextView) view.findViewById(R.id.plugin_package_name);
        nameView.setText(name);
        if (icon != null) {
            ImageView iconView = (ImageView) view.findViewById(R.id.plugin_icon);
            iconView.setImageDrawable(icon);
        }

        TextView versionView = (TextView) view.findViewById(R.id.plugin_version);
        versionView.setText(getString(R.string.activity_deviceplugin_info_version, versionName));

        Button settingBtn = (Button) view.findViewById(R.id.plugin_settings_btn);
        settingBtn.setEnabled(isEnabled);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                openSettings();
            }
        });

        Button deleteBtn = (Button) view.findViewById(R.id.plugin_delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                openUninstall();
            }
        });
        deleteBtn.setEnabled(!packageName.equals(managerPackageName));

        Button restartBtn = (Button) view.findViewById(R.id.plugin_restart_btn);
        restartBtn.setEnabled(isEnabled);
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                restartDevicePlugin();
            }
        });

        TextView connectionTypeView = (TextView) view.findViewById(R.id.plugin_connection_type);
        int resId;
        switch (mPluginInfo.getConnectionType()) {
            case BINDER:
                resId = R.string.activity_deviceplugin_info_connection_type_binder;
                break;
            case BROADCAST:
                resId = R.string.activity_deviceplugin_info_connection_type_broadcast;
                break;
            case INTERNAL:
                resId = R.string.activity_deviceplugin_info_connection_type_included_with_manager;
                break;
            default:
                resId = R.string.activity_deviceplugin_info_connection_type_unknown;
                break;
        }
        connectionTypeView.setText(getString(resId));

        LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.plugin_support_profiles);

        Map<String, DevicePluginXmlProfile> profiles = mPluginInfo.getSupportedProfiles();
        if (profiles != null) {
            String locale = Locale.getDefault().getLanguage();
            for (String key : profiles.keySet()) {
                String profileName = key;
                DevicePluginXmlProfile p = profiles.get(key);
                if (p != null) {
                    Map<String, DevicePluginXmlProfileLocale> locales = p.getXmlProfileLocales();
                    if (locales != null) {
                        DevicePluginXmlProfileLocale xmlLocale = locales.get(locale);
                        if (xmlLocale != null) {
                            profileName = xmlLocale.getName();
                        }
                    }
                }
                TextView tv = new TextView(getActivity());
                tv.setText(getString(R.string.activity_deviceplugin_info_profile_name, profileName));
                mainLayout.addView(tv);
            }
        }

        mErrorView = (ConnectionErrorView) view.findViewById(R.id.plugin_connection_error_view);
        mErrorView.showErrorMessage(plugin);

        if (BuildConfig.DEBUG) {
            CommunicationHistory history = plugin.getHistory();

            View baud = view.findViewById(R.id.activity_deviceplugin_info_baud_rate);
            baud.setVisibility(View.VISIBLE);

            TextView average = (TextView) baud.findViewById(R.id.activity_deviceplugin_info_average_baud_rate);
            average.setText(getString(R.string.activity_deviceplugin_info_baud_rate_unit, history.getAverageBaudRate()));

            TextView request = (TextView) baud.findViewById(R.id.activity_deviceplugin_info_worst_request);
            request.setText(history.getWorstBaudRateRequest());

            TextView worst = (TextView) baud.findViewById(R.id.activity_deviceplugin_info_worst_baud_rate);
            worst.setText(getString(R.string.activity_deviceplugin_info_baud_rate_unit, history.getWorstBaudRate()));

            LayoutInflater inflater = getLayoutInflater(null);

            LinearLayout baudRateListLayout = (LinearLayout) baud.findViewById(R.id.activity_deviceplugin_info_baud_rate_list);
            baudRateListLayout.removeAllViews();
            List<CommunicationHistory.Info> baudRateList = history.getRespondedCommunications();
            for (int i = baudRateList.size() - 1; i >= 0 ; i--) {
                CommunicationHistory.Info b = baudRateList.get(i);

                View v = inflater.inflate(R.layout.item_baud_rate_list, null);
                TextView br = (TextView) v.findViewById(R.id.activity_deviceplugin_info_request);
                br.setText(b.getRequestPath());

                TextView bb = (TextView) v.findViewById(R.id.activity_deviceplugin_info_baud_rate);
                bb.setText(getString(R.string.activity_deviceplugin_info_baud_rate_unit, b.getRoundTripTime()));

                TextView d = (TextView) v.findViewById(R.id.activity_deviceplugin_info_date);
                d.setText(b.getDateString());

                baudRateListLayout.addView(v);
            }

            LinearLayout timeoutListLayout = (LinearLayout) baud.findViewById(R.id.activity_deviceplugin_info_timeout_list);
            timeoutListLayout.removeAllViews();
            List<CommunicationHistory.Info> timeoutList = history.getNotRespondedCommunications();
            for (int i = timeoutList.size() - 1; i >= 0 ; i--) {
                CommunicationHistory.Info t = timeoutList.get(i);

                View v = inflater.inflate(R.layout.item_timeout_list, null);
                TextView br = (TextView) v.findViewById(R.id.activity_deviceplugin_info_request);
                br.setText(t.getRequestPath());

                TextView d = (TextView) v.findViewById(R.id.activity_deviceplugin_info_date);
                d.setText(t.getDateString());

                timeoutListLayout.addView(v);
            }
        }
    }

    /**
     * 接続のエラー状態を更新する.
     * @param error エラー
     */
    public void updateErrorState(final ConnectionError error) {
        mErrorView.showErrorMessage(error);
    }

    /**
     * 指定されたRunnableをUIスレッドで動作させる.
     * @param r 実行するRunnable
     */
    private void runOnUiThread(final Runnable r) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(r);
        }
    }

    /**
     * ボタンの有効・無効を設定する.
     * @param isEnabled 有効の場合はtrue、それ以外はfalse
     */
    void onEnabled(final boolean isEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = getView();
                if (view != null) {
                    Button settingBtn = (Button) view.findViewById(R.id.plugin_settings_btn);
                    settingBtn.setEnabled(isEnabled);
                    Button restartBtn = (Button) view.findViewById(R.id.plugin_restart_btn);
                    restartBtn.setEnabled(isEnabled);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (!existApplicationFromPackageName(mPluginInfo.getPackageName())) {
                getActivity().finish();
            }
        }
    }

    @Override
    protected void onManagerBonded() {
        DevicePluginManager mgr = getPluginManager();
        if (mgr != null) {
            String pluginId = getArguments().getString(DevicePluginInfoActivity.EXTRA_PLUGIN_ID);
            if (pluginId != null) {
                DevicePlugin plugin = mgr.getDevicePlugin(pluginId);
                if (plugin != null) {
                    updateInfo(plugin);
                }
            }
        }
    }

    /**
     * Open device plug-in's settings.
     */
    private void openSettings() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent request = new Intent(activity, DConnectService.class);
            request.setAction(DConnectService.ACTION_OPEN_SETTINGS);
            request.putExtra(DConnectService.EXTRA_PLUGIN_ID, mPluginInfo.getPluginId());
            activity.startService(request);
        }
    }

    /**
     * エラーメッセージを表示する.
     * @param e エラーを格納した例外
     */
    private void showMessagingErrorDialog(final MessagingException e) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSettingActivity) {
            ((BaseSettingActivity) activity).showMessagingErrorDialog(e);
        }
    }

    /**
     * Open uninstall dialog.
     */
    private void openUninstall() {
        Uri uri = Uri.fromParts("package", mPluginInfo.getPackageName(), null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * Restart device plug-in.
     */
    private void restartDevicePlugin() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            private StartingDialogFragment mDialog;

            @Override
            protected void onPreExecute() {
                if (getActivity() != null) {
                    mDialog = new StartingDialogFragment();
                    mDialog.show(getFragmentManager(), "dialog");
                }
            }

            @Override
            protected Void doInBackground(final Void... params) {
                DevicePluginManager mgr = getPluginManager();
                if (mgr == null) {
                    return null;
                }
                List<DevicePlugin> plugins = mgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.isEnabled() && plugin.getPackageName().equals(mPluginInfo.getPackageName())
                            && plugin.getPluginId() != null) {
                        restartDevicePlugin(plugin);
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(final Void o) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        };
        task.execute();
    }

    /**
     * Start a device plugin.
     *
     * @param plugin device plugin to be started
     */
    private void restartDevicePlugin(final DevicePlugin plugin) {
        Intent request = new Intent();
        request.setComponent(plugin.getComponentName());
        request.setAction(IntentDConnectMessage.ACTION_DEVICEPLUGIN_RESET);
        request.putExtra("pluginId", plugin.getPluginId());
        try {
            plugin.send(request);
        } catch (MessagingException e) {
            showMessagingErrorDialog(e);
        }
    }

    /**
     * Tests whether packageName exists in application list.
     *
     * @param packageName package name
     * @return true if packageName exists, false otherwise
     */
    private boolean existApplicationFromPackageName(final String packageName) {
        if (packageName == null) {
            return false;
        }

        final PackageManager pm = getActivity().getPackageManager();
        final int flags = PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS;
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(flags);
        for (ApplicationInfo app : installedAppList) {
            if (app.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show a dialog of restart a device plugin.
     */
    public static class StartingDialogFragment extends DialogFragment {
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
    }
}
