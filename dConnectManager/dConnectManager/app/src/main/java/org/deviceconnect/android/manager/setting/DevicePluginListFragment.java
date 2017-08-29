/*
 DevicePluginListFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.plugin.ConnectionState;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.deviceconnect.android.manager.plugin.DevicePluginManager.DevicePluginEventListener;

/**
 * Device plug-in list fragment.
 * 
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginListFragment extends BaseSettingFragment {

    /** Adapter. */
    private PluginAdapter mPluginAdapter;

    /** デバイスプラグインとの接続状態の変更通知を受信するリスナー. */
    private final DevicePluginEventListener mEventListener = new DevicePluginEventListener() {
        @Override
        public void onConnectionStateChanged(final DevicePlugin plugin, final ConnectionState state) {
            updatePluginList();
        }

        @Override
        public void onDeviceFound(final DevicePlugin plugin) {
            updatePluginList();
        }

        @Override
        public void onDeviceLost(final DevicePlugin plugin) {
            updatePluginList();
        }
    };

    private void runOnUiThread(final Runnable r) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(r);
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePluginList();
    }

    @Override
    public void onPause() {
        DevicePluginManager mgr = getPluginManager();
        if (mgr != null) {
            mgr.removeEventListener(mEventListener);
        }
        super.onPause();
    }

    @Override
    protected void onManagerBonded() {
        DevicePluginManager mgr = getPluginManager();
        if (mgr != null) {
            mgr.addEventListener(mEventListener);
            updatePluginList();
        }
    }

    /**
     * Create a list of plug-in.
     * @return list of plug-in.
     */
    private List<PluginContainer> createPluginContainers() {
        List<PluginContainer> containers = new ArrayList<>();
        if (isManagerBonded()) {
            PackageManager pm = getActivity().getPackageManager();
            DevicePluginManager manager = getPluginManager();
            for (DevicePlugin plugin : manager.getDevicePlugins()) {
                try {
                    pm.getApplicationInfo(plugin.getPackageName(), 0);
                    containers.add(new PluginContainer(getActivity(), plugin));
                } catch (PackageManager.NameNotFoundException e) {
                    // NOP.
                }
            }
            Collections.sort(containers, new Comparator<PluginContainer>() {
                @Override
                public int compare(final PluginContainer o1, final PluginContainer o2) {
                    String a = o1.getLabel();
                    String b = o2.getLabel();
                    return a.compareTo(b);
                }
            });
        }
        return containers;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        mPluginAdapter = new PluginAdapter(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_devicepluginlist, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_pluginlist);
        listView.setAdapter(mPluginAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                openDevicePluginInformation(mPluginAdapter.getItem(position));
            }
        });
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Update plug-in list.
     */
    public void updatePluginList() {
        if (mPluginAdapter == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPluginAdapter.clear();
                mPluginAdapter.addAll(createPluginContainers());
                mPluginAdapter.notifyDataSetChanged();
            }
       });
    }

    /**
     * Open device plug-in information activity.
     *
     * @param container plug-in container
     */
    private void openDevicePluginInformation(final PluginContainer container) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), DevicePluginInfoActivity.class);
        DevicePlugin plugin = container.getPluginEntity();
        intent.putExtra(DevicePluginInfoActivity.PLUGIN_INFO, plugin.getInfo());
        intent.putExtra(DevicePluginInfoActivity.PLUGIN_ENABLED, plugin.isEnabled());
        intent.putExtra(DevicePluginInfoActivity.CONNECTION_ERROR, plugin.getCurrentConnectionError());
        startActivity(intent);
    }

    /**
     * PluginContainer class.
     */
    static class PluginContainer {
        /** Label. */
        private final String mLabel;
        /** Plug-in. */
        private final DevicePlugin mPlugin;
        /** Context. */
        private final Context mContext;
        /** Connecting state. */
        private boolean mIsConnecting;

        PluginContainer(final Context context, final DevicePlugin plugin) {
            mContext = context;
            mPlugin = plugin;
            String label = plugin.getDeviceName();
            if (label == null) {
                mLabel = "Unknown";
            } else {
                mLabel = label;
            }
        }

        /**
         * Get plug-in Label.
         * 
         * @return Plug-in label.
         */
        public String getLabel() {
            return mLabel;
        }

        /**
         * Get plug-in id.
         *
         * @return Plug-in id.
         */
        public String getPluginId() {
            return mPlugin.getPluginId();
        }

        /**
         * Get plug-in version.
         *
         * @return Plug-in version
         */
        public String getVersion() {
            return mPlugin.getVersionName();
        }

        /**
         * Get plug-in icon.
         * @return icon
         */
        public Drawable getIcon() {
            return mPlugin.getPluginIcon(mContext);
        }

        /**
         * Get plug-in entity.
         * @return plug-in
         */
        public DevicePlugin getPluginEntity() {
            return mPlugin;
        }

        public boolean isConnecting() {
            return mIsConnecting;
        }

        public void setConnecting(final boolean isConnecting) {
            mIsConnecting = isConnecting;
        }

        public boolean hasSamePlugin(final String pluginId) {
            return mPlugin.getPluginId().equals(pluginId);
        }
    }

    /**
     * PluginAdapter class.
     */
    private class PluginAdapter extends ArrayAdapter<PluginContainer> {
        /** LayoutInflater. */
        private LayoutInflater mInflater;

        /**
         * Constructor.
         * 
         * @param context Context.
         */
        PluginAdapter(final Context context) {
            super(context, 0, new ArrayList<PluginContainer>());
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View cv;
            if (convertView == null) {
                cv = mInflater.inflate(R.layout.item_deviceplugin_list, parent, false);
            } else {
                cv = convertView;
            }

            final PluginContainer container = getItem(position);
            final DevicePlugin plugin = container.getPluginEntity();

            String name = container.getLabel();

            TextView nameView = (TextView) cv.findViewById(R.id.devicelist_package_name);
            nameView.setText(name);

            Drawable icon = container.getIcon();
            if (icon != null) {
                ImageView iconView = (ImageView) cv.findViewById(R.id.devicelist_icon);
                iconView.setImageDrawable(icon);
            }

            String version = container.getVersion();
            TextView versionView = (TextView) cv.findViewById(R.id.devicelist_version);
            versionView.setText(getString(R.string.activity_devicepluginlist_version) + version);

            SwitchCompat switchCompat = (SwitchCompat) cv.findViewById(R.id.switch_plugin_enable_status);
            switchCompat.setOnCheckedChangeListener(null);
            switchCompat.setChecked(plugin.isEnabled());
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton button, final boolean isOn) {
                    requestPluginStateChange(plugin.getPluginId(), isOn);
                }
            });

            View progressCircle = cv.findViewById(R.id.progress_plugin_enable_status);
            if (container.isConnecting()) {
                progressCircle.setVisibility(View.VISIBLE);
            } else {
                progressCircle.setVisibility(View.INVISIBLE);
            }

            ConnectionErrorView errorView = (ConnectionErrorView) cv.findViewById(R.id.plugin_connection_error_view);
            errorView.showErrorMessage(plugin.getCurrentConnectionError());

            if (plugin.canCommunicate()) {
                cv.setBackgroundResource(R.color.plugin_list_row_background_color_connected);
            } else {
                cv.setBackgroundResource(R.color.plugin_list_row_background_color_not_connected);
            }

            return cv;
        }

        void onStateChange(final String pluginId, final ConnectionState state) {
            int cnt = getCount();
            for (int i = 0; i < cnt; i++) {
                PluginContainer container = getItem(i);
                if (container != null && container.hasSamePlugin(pluginId)) {
                    container.setConnecting(state == ConnectionState.CONNECTING);
                    notifyDataSetChanged();
                    break;
                }
            }
        }

        private void requestPluginStateChange(final String pluginId, final boolean isOn) {
            Activity activity = getActivity();
            if (activity != null) {
                String action = isOn ?
                        DConnectMessageService.ACTION_ENABLE_PLUGIN :
                        DConnectMessageService.ACTION_DISABLE_PLUGIN;
                Intent request = new Intent(activity.getApplicationContext(), DConnectService.class);
                request.setAction(action);
                request.putExtra(DConnectMessageService.EXTRA_PLUGIN_ID, pluginId);
                activity.startService(request);
            }
        }
    }
}


