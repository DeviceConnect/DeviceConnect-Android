package org.deviceconnect.android.manager.setting;

import android.animation.Animator;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import org.deviceconnect.android.manager.util.AnimationUtil;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.ServiceContainer;
import org.deviceconnect.android.manager.util.ServiceDiscovery;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends Activity implements AlertDialogFragment.OnAlertDialogListener {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Manager";

    private static final String TAG_OFFLINE = "offline";

    private static final String FILE_NAME = "__service_list__.dat";
    private static final String KEY_SHOW_GUIDE = "show_guide";

    private static final int[] GUIDE_ID_LIST = {
            R.id.activity_service_guide_1,
            R.id.activity_service_guide_2,
    };

    private ServiceAdapter mServiceAdapter;
    private DevicePluginManager mDevicePluginManager;
    private SharedPreferences mSharedPreferences;
    private ServiceDiscovery mServiceDiscovery;
    private ServiceContainer mSelectedService;

    private Handler mHandler = new Handler();

    private Switch mSwitchAction;
    private int mPageIndex;

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
                    openServiceInfo(position);
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

        if (load(this)) {
            showGuide();
        }
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

        if (!hasDevicePlugins()) {
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
            openSettings();
        } else if (id == R.id.activity_service_menu_item_help) {
            openHelp();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButton(final String tag) {
        if (TAG_OFFLINE.equals(tag)) {
            openPluginSettings();
        }
    }

    @Override
    public void onNegativeButton(final String tag) {

    }

    private void setEnableSearchButton(final boolean running) {
        Button btn = (Button) findViewById(R.id.activity_service_list_search_button);
        if (btn != null) {
            if (mDConnectService != null) {
                btn.setEnabled(running);
            }
        }
    }

    private boolean hasDevicePlugins() {
        return mDevicePluginManager.getDevicePlugins().size() > 0;
    }

    private boolean load(final Context context) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_SHOW_GUIDE, true);
    }

    private void save(final boolean showGuideFlag) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_SHOW_GUIDE, showGuideFlag);
        editor.apply();
    }

    private void showGuide() {
        View guideView = findViewById(R.id.activity_service_guide);
        if (guideView != null) {
            guideView.setVisibility(View.VISIBLE);
            guideView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    nextGuide();
                }
            });
        }

        Button button = (Button) findViewById(R.id.activity_service_guide_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextGuide();
            }
        });
    }

    private void nextGuide() {
        if (mPageIndex == GUIDE_ID_LIST.length - 1) {
            endGuide();
        } else {
            animateGuide(new AnimationUtil.AnimationAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPageIndex++;
                    visibleGuide();
                }
            });
        }
    }

    private void animateGuide(final AnimationUtil.AnimationAdapter listener) {
        for (int i = 0; i < GUIDE_ID_LIST.length; i++) {
            View view = findViewById(GUIDE_ID_LIST[i]);
            if (i == mPageIndex) {
                AnimationUtil.animateAlpha(view, listener);
            }
        }
    }

    private void visibleGuide() {
        for (int i = 0; i < GUIDE_ID_LIST.length; i++) {
            View view = findViewById(GUIDE_ID_LIST[i]);
            if (i == mPageIndex) {
                view.setVisibility(View.VISIBLE);
                AnimationUtil.animateAlpha2(view, new AnimationUtil.AnimationAdapter() {
                    @Override
                    public void onAnimationEnd(final Animator animation) {
                    }
                });
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    private void endGuide() {
        boolean result = true;
        CheckBox checkBox = (CheckBox) findViewById(R.id.activity_service_guide_checkbox);
        if (checkBox != null) {
            result = !checkBox.isChecked();
        }

        final View guideView = findViewById(R.id.activity_service_guide);
        if (guideView != null) {
            AnimationUtil.animateAlpha(guideView, new AnimationUtil.AnimationAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    guideView.setVisibility(View.GONE);
                }
            });
        }

        save(result);
    }

    private void switchDConnectServer(final boolean checked) {
        if (mDConnectService == null) {
            return;
        }

        try {
            if (checked) {
                mDConnectService.start();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reload();
                    }
                }, 400);
            } else {
                mDConnectService.stop();
                notifyManagerTerminate();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mServiceAdapter.mServices = new ArrayList<>();
                        mServiceAdapter.notifyDataSetInvalidated();
                    }
                });
            }
            setEnableSearchButton(checked);
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }
    }

    private void notifyManagerTerminate() {
        ManagerTerminationFragment.show(this);
    }

    private void reload() {
        if (DEBUG) {
            Log.i(TAG, "reload a device plugin.");
        }

        if (mDConnectService == null) {
            return;
        }

        try {
            if (!mDConnectService.isRunning()) {
                return;
            }
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }

        if (mServiceDiscovery != null) {
            return;
        }

        mServiceDiscovery = new ServiceDiscovery(this) {
            private DialogFragment mDialog;

            @Override
            protected void onPreExecute() {
                mDialog = new ServiceDiscoveryDialogFragment();
                mDialog.show(getFragmentManager(), null);
            }

            @Override
            protected void onPostExecute(final List<ServiceContainer> serviceContainers) {
                mDialog.dismiss();

                View view = findViewById(R.id.activity_service_no_service);
                if (view != null) {
                    view.setVisibility(serviceContainers.size() == 0 ? View.VISIBLE : View.GONE);
                }

                mServiceAdapter.mServices = serviceContainers;
                mServiceAdapter.notifyDataSetInvalidated();
                mServiceDiscovery = null;
            }
        };
        mServiceDiscovery.execute();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setClass(this, SettingActivity.class);
        startActivity(intent);
    }

    private void openHelp() {
        String url = "file:///android_asset/html/help/index.html";
        Intent intent = new Intent();
        intent.setClass(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.activity_help_title));
        startActivity(intent);
    }

    private void openServiceInfo(final int position) {
        mSelectedService = (ServiceContainer) mServiceAdapter.getItem(position);
        if (mSelectedService.isOnline()) {
            String url = "file:///android_asset/html/demo/index.html?serviceId=" + mSelectedService.getId();
            Intent intent = new Intent();
            intent.setClass(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, url);
            intent.putExtra(WebViewActivity.EXTRA_TITLE, mSelectedService.getName());
            startActivity(intent);
        } else {
            String title = getString(R.string.activity_service_list_offline_title);
            String message = getString(R.string.activity_service_list_offline_message, mSelectedService.getName());
            String positive = getString(R.string.activity_service_list_offline_positive);
            String negative = getString(R.string.activity_service_list_offline_negative);
            AlertDialogFragment dialog = AlertDialogFragment.create(TAG_OFFLINE, title, message, positive, negative);
            dialog.show(getFragmentManager(), TAG_OFFLINE);
        }
    }

    private void showNoDevicePlugin() {
        String title = getString(R.string.activity_service_list_no_plugin_title);
        String message = getString(R.string.activity_service_list_no_plugin_message);
        String positive = getString(R.string.activity_service_list_no_plugin_positive);
        AlertDialogFragment dialog = AlertDialogFragment.create("no", title, message, positive);
        dialog.show(getFragmentManager(), "no");
    }

    private void openPluginSettings() {
        DConnectApplication app = (DConnectApplication) getApplication();
        DevicePluginManager mgr = app.getDevicePluginManager();
        List<DevicePlugin> plugins = mgr.getDevicePlugins();
        for (DevicePlugin plugin : plugins) {
            if (mSelectedService.getId().contains(plugin.getPluginId())) {
                Intent request = new Intent();
                request.setComponent(plugin.getComponentName());
                request.setAction(IntentDConnectMessage.ACTION_PUT);
                SystemProfile.setApi(request, "gotapi");
                SystemProfile.setProfile(request, SystemProfile.PROFILE_NAME);
                SystemProfile.setInterface(request, SystemProfile.INTERFACE_DEVICE);
                SystemProfile.setAttribute(request, SystemProfile.ATTRIBUTE_WAKEUP);
                request.putExtra("pluginId", plugin.getPluginId());
                sendBroadcast(request);
                break;
            }
        }
    }

    private String getPackageName(final String serviceId) {
        List<DevicePlugin> list = mDevicePluginManager.getDevicePlugins();
        for (DevicePlugin plugin : list) {
            if (serviceId.contains(plugin.getPluginId())) {
                return plugin.getPackageName();
            }
        }
        return null;
    }

    private IDConnectService mDConnectService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mDConnectService = (IDConnectService) service;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mSwitchAction != null) {
                            mSwitchAction.setChecked(mDConnectService.isRunning());
                        }
                        if (mDConnectService.isRunning()) {
                            reload();
                        }
                        setEnableSearchButton(mDConnectService.isRunning());
                    } catch (RemoteException e) {
                        if (DEBUG) {
                            Log.e(TAG, "", e);
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
            if (textView != null) {
                textView.setText(service.getName());
            }

            ImageView typeView = (ImageView) view.findViewById(R.id.item_type);
            if (typeView != null) {
                switch(service.getNetworkType()) {
                    case BLE:
                    case BLUETOOTH:
                        setNetworkTypeIcon(typeView, service, R.drawable.bluetooth_on);
                        break;
                    case WIFI:
                        setNetworkTypeIcon(typeView, service, R.drawable.wifi_on);
                        break;
                    case NFC:
                        setNetworkTypeIcon(typeView, service, R.drawable.nfc_on);
                        break;
                    default:
                        typeView.setVisibility(View.GONE);
                        break;
                }
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.item_icon);
            if (imageView != null) {
                String packageName = getPackageName(service.getId());
                if (packageName != null) {
                    try {
                        PackageManager pm = getPackageManager();
                        ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
                        setIcon(imageView, service, pm.getApplicationIcon(app.packageName));
                    } catch (PackageManager.NameNotFoundException e) {
                        if (DEBUG) {
                            Log.e(TAG, "", e);
                        }
                    }
                }
            }
            return view;
        }

        private void setNetworkTypeIcon(final ImageView imageView, final ServiceContainer service, final int resId) {
            setIcon(imageView, service, ResourcesCompat.getDrawable(getResources(),resId, null));
        }

        private void setIcon(final ImageView imageView, final ServiceContainer service, final Drawable icon) {
            if (icon == null) {
                imageView.setVisibility(View.GONE);
                return;
            }

            Drawable newIcon = icon;
            if (!service.isOnline()) {
                newIcon = DConnectUtil.convertToGrayScale(icon);
            } else {
                newIcon.setColorFilter(null);
            }
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(newIcon);
        }
    }
}
