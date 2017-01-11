/*
 PluginListActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.uiapp.DConnectApplication;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.data.DCDevicePlugin;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * デバイスプラグイン一覧を表示するためのActivity.
 */
public class PluginListActivity extends Activity {

    private PluginAdapter mPluginAdapter = new PluginAdapter();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_list);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mPluginAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                openDevicePluginSettings(mPluginAdapter.mPluginList.get(position));
            }
        });

        getSystem();
    }

    private void getSystem() {
        DConnectSDK.URIBuilder builder = getSDK().createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);

        getSDK().get(builder.build(), new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPluginAdapter.mPluginList = getDevicePlugin(response);
                        mPluginAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void openDevicePluginSettings(final DCDevicePlugin plugin) {
        DConnectSDK.URIBuilder builder = getSDK().createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.setInterface(SystemProfileConstants.INTERFACE_DEVICE);
        builder.setAttribute(SystemProfileConstants.ATTRIBUTE_WAKEUP);

        MultipartEntity data = new MultipartEntity();
        data.add(SystemProfileConstants.PARAM_PLUGIN_ID, new StringEntity(plugin.getId()));

        getSDK().put(builder.build(), data, new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPluginAdapter.mPluginList = getDevicePlugin(response);
                        mPluginAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private DConnectSDK getSDK() {
        DConnectApplication app = (DConnectApplication) getApplication();
        return app.getDConnectSK();
    }

    private List<DCDevicePlugin> getDevicePlugin(final DConnectResponseMessage response) {
        List<DCDevicePlugin> list = new ArrayList<>();

        List<Object> plugins = response.getList(SystemProfileConstants.PARAM_PLUGINS);
        if (plugins != null) {
            for (Object o : plugins) {
                DConnectMessage plugin = (DConnectMessage) o;
                String name = plugin.getString(SystemProfileConstants.PARAM_NAME);
                String id = plugin.getString(SystemProfileConstants.PARAM_ID);
                String pn = plugin.getString(SystemProfileConstants.PARAM_PACKAGE_NAME);

                DCDevicePlugin p = new DCDevicePlugin(name, id);
                p.setPackageName(pn);

                list.add(p);
            }
        }

        return list;
    }

    private class PluginAdapter extends BaseAdapter {

        private List<DCDevicePlugin> mPluginList = new ArrayList<>();

        @Override
        public int getCount() {
            return mPluginList.size();
        }

        @Override
        public Object getItem(final int position) {
            return mPluginList.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_profile_list, null);
            }

           DCDevicePlugin plugin = mPluginList.get(position);

            TextView textView = (TextView) view.findViewById(R.id.item_name);
            if (textView != null) {
                textView.setText(plugin.getName());
            }

            return view;
        }
    }
}
