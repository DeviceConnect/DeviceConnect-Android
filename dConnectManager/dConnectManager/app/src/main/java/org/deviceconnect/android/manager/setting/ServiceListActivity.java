package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.IDConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.util.ServiceContainer;
import org.deviceconnect.android.manager.util.ServiceDiscovery;

import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends Activity {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Manager";

    private ServiceAdapter mServiceAdapter;
    private DevicePluginManager mDevicePluginManager;

    private Switch mSwitchAction;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        mServiceAdapter = new ServiceAdapter();

        GridView gridView = (GridView) findViewById(R.id.activity_service_list_grid_view);
        if (gridView != null) {
            gridView.setAdapter(mServiceAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                    shiftService(position);
                }
            });
        }

        Button btn = (Button) findViewById(R.id.activity_service_list_search_button);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    reload();
                }
            });
        }

        DConnectApplication app = (DConnectApplication) getApplication();
        mDevicePluginManager = app.getDevicePluginManager();
    }

    @Override
    public void onPause() {
        unbindService(mServiceConnection);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = new Intent(IDConnectService.class.getName());
        intent.setPackage(getPackageName());
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (mDevicePluginManager.getDevicePlugins().size() == 0) {
            showNoDevicePlugin();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_service_list, menu);

        mSwitchAction = (Switch) menu.findItem(R.id.activity_service_manager_power).getActionView();
        mSwitchAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                switchDConnectServer(isChecked);
            }
        });

        if (mDConnectService != null) {
            try {
                mSwitchAction.setChecked(mDConnectService.isRunning());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.activity_service_menu_item_settings) {
            shiftSettings();
        } else if (id == R.id.activity_service_menu_item_help) {
            shiftHelp();
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchDConnectServer(final boolean checked) {
        try {
            if (checked) {
                mDConnectService.start();
            } else {
                mDConnectService.stop();
                notifyManagerTerminate();
            }
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }
    }

    /**
     * Manager termination notification to all device plug-ins.
     */
    private void notifyManagerTerminate() {
        ManagerTerminationFragment.show(this);
    }

    private void reload() {
        try {
            if (mDConnectService != null && !mDConnectService.isRunning()) {
                return;
            }
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }

        ServiceDiscovery discovery = new ServiceDiscovery(this) {
            @Override
            protected void onPreExecute() {
                // TODO ダイアログ表示
            }

            @Override
            protected void onPostExecute(final List<ServiceContainer> serviceContainers) {
                // TODO ダイアログ非表示
                mServiceAdapter.mServices = serviceContainers;
                mServiceAdapter.notifyDataSetInvalidated();
            }
        };
        discovery.execute();
    }

    private void shiftSettings() {
        Intent intent = new Intent();
        intent.setClass(this, SettingActivity.class);
        startActivity(intent);
    }

    private void shiftHelp() {
        String url = "file:///android_asset/html/help/index.html";
        Intent intent = new Intent();
        intent.setClass(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        startActivity(intent);
    }

    private void shiftService(final int position) {
        ServiceContainer service = (ServiceContainer) mServiceAdapter.getItem(position);
        String url = "file:///android_asset/html/demo/index.html?serviceId=" + service.getId();
        Intent intent = new Intent();
        intent.setClass(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        startActivity(intent);
    }

    private void showNoDevicePlugin() {
        AlertDialogFragment dialog = AlertDialogFragment.create("no", "", "");
        dialog.show(getFragmentManager(), "");
    }

    private String getPackageName(final String serviceId) {
        List<DevicePlugin> list = mDevicePluginManager.getDevicePlugins();
        for (DevicePlugin plugin : list) {
            if (serviceId.contains(plugin.getServiceId())) {
                return plugin.getPackageName();
            }
        }
        return null;
    }

    /**
     * DConnectServiceを操作するクラス.
     */
    private IDConnectService mDConnectService;

    /**
     * DConnectServiceと接続するためのクラス.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mDConnectService = (IDConnectService) service;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSwitchAction != null) {
                        try {
                            mSwitchAction.setChecked(mDConnectService.isRunning());
                            if (mDConnectService.isRunning()) {
                                reload();
                            }
                        } catch (RemoteException e) {
                            if (DEBUG) {
                                Log.e(TAG, "", e);
                            }
                        }
                    }
                }
            });
        }
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
        }
    };

    private class ServiceAdapter extends BaseAdapter {
        private List<ServiceContainer> mServices = new ArrayList<>();

        @Override
        public int getCount() {
            return mServices.size();
        }

        @Override
        public Object getItem(final int position) {
            return mServices.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_service_list, null);
            }

            ServiceContainer service = (ServiceContainer) getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.item_name);
            textView.setText(service.getName());

            ImageView imageView = (ImageView) view.findViewById(R.id.item_icon);
            if (imageView != null) {
                String packageName = getPackageName(service.getId());
                if (packageName != null) {
                    try {
                        PackageManager pm = getPackageManager();
                        ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
                        imageView.setImageDrawable(pm.getApplicationIcon(app.packageName));
                    } catch (PackageManager.NameNotFoundException e) {
                        if (DEBUG) {
                            Log.e(TAG, "", e);
                        }
                    }
                }
            }
            return view;
        }
    }
}
