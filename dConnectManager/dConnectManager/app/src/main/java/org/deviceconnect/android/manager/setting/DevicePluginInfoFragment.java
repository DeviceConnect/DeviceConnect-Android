/*
 DevicePluginInfoFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Device plug-in information fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginInfoFragment extends Fragment {

    /** デバイスプラグインをアンインストールする際のリクエストコード. */
    private static final int REQUEST_CODE = 101;

    /** デバイスプラグインのパッケージ名. */
    private String mPackageName;

    /** デバイスプラグインのプラグインID. */
    private String mPluginId;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deviceplugin_info, container, false);

        mPluginId = getArguments().getString(DevicePluginInfoActivity.PLUGIN_ID);

        if (mPluginId == null) {
            getActivity().finish();
            return view;
        }

        String name = null;
        Drawable icon = null;
        String versionName = null;
        DConnectApplication apps = (DConnectApplication) getActivity().getApplication();
        DevicePluginManager manager = apps.getDevicePluginManager();
        for (DevicePlugin plugin : manager.getDevicePlugins()) {
            if (mPluginId.equals(plugin.getPluginId())) {
                mPackageName = plugin.getPackageName();
                name = plugin.getDeviceName();
                icon = plugin.getPluginIcon();
                versionName = plugin.getVersionName();
                break;
            }
        }

        // 指定されたプラグインIDのパッケージが見つからない場合は終了
        if (mPackageName == null) {
            getActivity().finish();
            return view;
        }

        TextView nameView = (TextView) view.findViewById(R.id.plugin_package_name);
        nameView.setText(name);
        if (icon != null) {
            ImageView iconView = (ImageView) view.findViewById(R.id.plugin_icon);
            iconView.setImageDrawable(icon);
        }

        TextView versionView = (TextView) view.findViewById(R.id.plugin_version);
        versionView.setText(getString(R.string.activity_deviceplugin_info_version) + versionName);

        Button settingBtn = (Button) view.findViewById(R.id.plugin_settings_btn);
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
        deleteBtn.setEnabled(!mPackageName.equals(getActivity().getPackageName()));
        Button restartBtn = (Button) view.findViewById(R.id.plugin_restart_btn);
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                restartDevicePlugin();
            }
        });

        LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.plugin_support_profiles);

        Map<String, DevicePluginXmlProfile> profiles = getSupportedProfiles();
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
                tv.setText(" ・ " + profileName);
                mainLayout.addView(tv);
            }
        }
        return view;
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
            if (!existApplicationFromPackageName(mPackageName)) {
                getActivity().finish();
            }
        }
    }

    /**
     * Get supported profiles.
     * @return profiles
     */
    private Map<String, DevicePluginXmlProfile> getSupportedProfiles() {
        return DevicePluginXmlUtil.getSupportProfiles(getActivity(), mPackageName);
    }

    /**
     * Open device plug-in's settings.
     */
    private void openSettings() {
        if (mPackageName == null || mPluginId == null) {
            return;
        }

        DConnectApplication app = (DConnectApplication) getActivity().getApplication();
        List<DevicePlugin> plugins = app.getDevicePluginManager().getDevicePlugins();
        for (DevicePlugin plugin : plugins) {
            if (mPackageName.equals(plugin.getPackageName())
                    && mPluginId.equals(plugin.getPluginId())) {
                Intent request = new Intent();
                request.setComponent(plugin.getComponentName());
                request.setAction(IntentDConnectMessage.ACTION_PUT);
                SystemProfile.setApi(request, "gotapi");
                SystemProfile.setProfile(request, SystemProfile.PROFILE_NAME);
                SystemProfile.setInterface(request, SystemProfile.INTERFACE_DEVICE);
                SystemProfile.setAttribute(request, SystemProfile.ATTRIBUTE_WAKEUP);
                request.putExtra("pluginId", plugin.getPluginId());
                getActivity().sendBroadcast(request);
                break;
            }
        }
    }

    /**
     * Open uninstall dialog.
     */
    private void openUninstall() {
        if (mPackageName == null) {
            return;
        }

        Uri uri = Uri.fromParts("package", mPackageName, null);
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
                DConnectApplication app = (DConnectApplication) getActivity().getApplication();
                List<DevicePlugin> plugins = app.getDevicePluginManager().getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.getPackageName().equals(mPackageName)
                            && plugin.getStartServiceClassName() != null
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
        getActivity().sendBroadcast(request);
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
