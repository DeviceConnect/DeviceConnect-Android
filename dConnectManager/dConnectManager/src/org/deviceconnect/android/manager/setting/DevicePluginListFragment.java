/*
 DevicePluginListFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Device plug-in list fragment.
 * 
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginListFragment extends Fragment {

    /** Adapter. */
    private PluginAdapter mPluginAdapter;

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

    /**
     * Create a PluginContainer from Device Plug-in.
     * @param pm PackageManager.
     * @param app ApplicationInfo.
     * @return Instance of DeviceContainer
     */
    private PluginContainer createContainer(final PackageManager pm, final ApplicationInfo app) {
        PluginContainer container = new PluginContainer();
        container.setLabel(app.loadLabel(pm).toString());
        container.setPackageName(app.packageName);
        try {
            container.setIcon(pm.getApplicationIcon(app.packageName));
        } catch (PackageManager.NameNotFoundException e) {
            // do nothing.
            if (BuildConfig.DEBUG) {
                Log.d("Manager", "Icon is not found.");
            }
        }
        try {
            PackageInfo info = pm.getPackageInfo(app.packageName, PackageManager.GET_META_DATA);
            container.setVersion(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // do nothing.
            if (BuildConfig.DEBUG) {
                Log.d("Manager", "VersionName is not found.");
            }
        }
        return container;
    }

    /**
     * Create a list of plug-in.
     * @return list of plug-in.
     */
    private List<PluginContainer> createPluginContainers() {
        List<PluginContainer> containers = new ArrayList<PluginContainer>();
        PackageManager pm = getActivity().getPackageManager();
        DevicePluginManager manager = new DevicePluginManager(getActivity(), "");
        manager.createDevicePluginList();
        for (DevicePlugin plugin : manager.getDevicePlugins()) {
            try {
                ApplicationInfo app = pm.getApplicationInfo(plugin.getPackageName(), 0);
                containers.add(createContainer(pm, app));
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }
        }
        return containers;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        mPluginAdapter = new PluginAdapter(getActivity(), createPluginContainers());
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
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
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
        intent.putExtra(DevicePluginInfoActivity.PACKAGE_NAME, container.getPackageName());
        startActivity(intent);
    }

    /**
     * PluginContainer class.
     */
    static class PluginContainer {
        /** Label. */
        private String mLabel;
        /** Package name. */
        private String mPackageName;
        /** Version. */
        private String mVersion;
        /** Icon. */
        private Drawable mIcon;

        /**
         * Get plug-in Label.
         * 
         * @return Plug-in label.
         */
        public String getLabel() {
            return mLabel;
        }

        /**
         * Set plug-in label.
         * 
         * @param label Plug-in label.
         */
        public void setLabel(final String label) {
            if (label == null) {
                mLabel = "Unknown";
            } else {
                mLabel = label;
            }
        }

        /**
         * Get plug-in package name.
         * 
         * @return Plug-in package name.
         */
        public String getPackageName() {
            return mPackageName;
        }

        /**
         * Set plug-in package name.
         * 
         * @param name Plug-in package name.
         */
        public void setPackageName(final String name) {
            mPackageName = name;
        }

        /**
         * Get plug-in version.
         *
         * @return Plug-in version
         */
        public String getVersion() {
            return mVersion;
        }

        /**
         * Set plug-in version.
         *
         * @param version Plug-in version
         */
        public void setVersion(final String version) {
            mVersion = version;
        }

        /**
         * Get plug-in icon.
         * @return icon
         */
        public Drawable getIcon() {
            return mIcon;
        }

        /**
         * Set plug-in icon.
         * @param icon plug-in icon
         */
        public void setIcon(final Drawable icon) {
            mIcon = icon;
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
         * @param objects Plug-in list object.
         */
        public PluginAdapter(final Context context, final List<PluginContainer> objects) {
            super(context, 0, objects);
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

            final PluginContainer plugin = getItem(position);

            String name = plugin.getLabel();

            TextView nameView = (TextView) cv.findViewById(R.id.devicelist_package_name);
            nameView.setText(name);

            Drawable icon = plugin.getIcon();
            if (icon != null) {
                ImageView iconView = (ImageView) cv.findViewById(R.id.devicelist_icon);
                iconView.setImageDrawable(icon);
            }

            String version = plugin.getVersion();
            TextView versionView = (TextView) cv.findViewById(R.id.devicelist_version);
            versionView.setText(getString(R.string.activity_devicepluginlist_version) + version);
            return cv;
        }
    }
}


