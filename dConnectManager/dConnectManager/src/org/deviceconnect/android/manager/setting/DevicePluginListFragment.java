/*
 DevicePluginListFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.manager.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Device plug-in list fragment.
 * 
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginListFragment extends Fragment {

    /** The root view. */
    private View mRootView;

    /** Adapter. */
    private PluginAdapter mPluginAdapter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
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
        return container;
    }

    /**
     * Create a list of plug-in.
     * @return list of plug-in.
     */
    private List<PluginContainer> createPluginContainers() {
        List<PluginContainer> containers = new ArrayList<PluginContainer>();
        String pluginName = "org.deviceconnect.android.deviceplugin";
        PackageManager pm = getActivity().getPackageManager();
        final int flags = PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS;
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(flags);

        for (ApplicationInfo app : installedAppList) {
            if (app.packageName.startsWith(pluginName)) {
                containers.add(createContainer(pm, app));
            }
        }
        return containers;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        mPluginAdapter = new PluginAdapter(getActivity(), createPluginContainers());
        mRootView = inflater.inflate(R.layout.fragment_devicepluginlist, container, false);
        ListView listView = (ListView) mRootView.findViewById(R.id.listview_pluginlist);
        listView.setAdapter(mPluginAdapter);
        return mRootView;
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
     * PluginContainer class.
     */
    private class PluginContainer {
        /** Label. */
        private String mLabel;
        /** Package name. */
        private String mPackageName;

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
            View cv = null;
            if (convertView == null) {
                cv = mInflater.inflate(R.layout.item_deviceplugin_list, parent, false);
            } else {
                cv = convertView;
            }

            final PluginContainer plugin = getItem(position);

            String name = plugin.getLabel();

            TextView nameView = (TextView) cv.findViewById(R.id.devicelist_package_name);
            nameView.setText(name);

            Button infoBtn = (Button) cv.findViewById(R.id.devicelist_info_btn);
            infoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Uri uri = Uri.fromParts("package", plugin.getPackageName(), null);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                    startActivity(intent);
                    updatePluginList();
                }
            });

            Button delBtn = (Button) cv.findViewById(R.id.devicelist_delete_btn);
            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Uri uri = Uri.fromParts("package", plugin.getPackageName(), null);
                    Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                    startActivity(intent);
                    updatePluginList();
                }
            });

            return cv;
        }
    }
}


