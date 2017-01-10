package org.deviceconnect.android.uiapp.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.deviceconnect.android.uiapp.DConnectApplication;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.data.DCDevicePlugin;
import org.deviceconnect.android.uiapp.data.DCProfile;
import org.deviceconnect.android.uiapp.data.DCService;
import org.deviceconnect.android.uiapp.utils.Utils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends BasicActivity {

    /**
     * サービスリスト.
     */
    private ServiceAdapter mServiceAdapter;

    /**
     * プラグインリスト.
     */
    private List<DCDevicePlugin> mPluginList = new ArrayList<>();

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
                    openServiceFragment(mServiceAdapter.mServices.get(position));
                }
            });
        }

        getSystem(new OnReceivedDevicePluginListener() {
            @Override
            public void onReceived(final List<DCDevicePlugin> pluginList) {
                mPluginList = pluginList;
                discoverServices();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            discoverServices();
        } else if (id == R.id.action_settings) {
            openSettings();
        } else if (id == R.id.action_plugins) {
            openPluginListActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * サービス一覧を取得する.
     */
    private void discoverServices() {
        getSDK().serviceDiscovery(new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_OK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mServiceAdapter.mServices = generateServiceList(response);
                            mServiceAdapter.notifyDataSetInvalidated();
                        }
                    });
                } else {
                    int errorCode = response.getErrorCode();
                    switch (DConnectMessage.ErrorCode.getInstance(errorCode)) {
                        case SCOPE:
                        case AUTHORIZATION:
                        case EXPIRED_ACCESS_TOKEN:
                        case EMPTY_ACCESS_TOKEN:
                        case NOT_FOUND_CLIENT_ID:
                            String[] profiles = new String[ DConnectApplication.SCOPES.size()];
                            DConnectApplication.SCOPES.toArray(profiles);
                            String appName = getString(R.string.app_name);
                            getSDK().authorization(appName, profiles, new DConnectSDK.OnAuthorizationListener() {
                                @Override
                                public void onResponse(final String clientId, final String accessToken) {
                                    getSDK().setAccessToken(accessToken);
                                    discoverServices();
                                }

                                @Override
                                public void onError(final int errorCode, final String errorMessage) {
                                }
                            });
                            break;
                        default:
                            // TODO: エラー処理
                            break;
                    }
                }
            }
        });
    }

    /**
     * Device Connect Managerのレスポンスからサービスリストを生成します.
     * @param response Device Connect Managerからのレスポンス
     * @return サービスリスト
     */
    private List<DCService> generateServiceList(final DConnectResponseMessage response) {
        List<Object> services = response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES);
        if (services == null) {
            return new ArrayList<>();
        }

        List<DCService> tempServices = new ArrayList<>();
        for (Object obj : services) {
            DConnectMessage service = (DConnectMessage) obj;
            String id = service.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
            String name = service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
            boolean online = service.getBoolean(ServiceDiscoveryProfileConstants.PARAM_ONLINE);
            List<Object> scopes = service.getList(ServiceDiscoveryProfileConstants.PARAM_SCOPES);
            String type = service.getString(ServiceDiscoveryProfileConstants.PARAM_TYPE);
            DCService tmp = new DCService(id, name);
            tmp.setOnline(online);
            if (scopes != null) {
                for (Object o : scopes) {
                    tmp.addService(new DCProfile((String) o));
                }
            }
            tmp.setType(type);
            tempServices.add(tmp);
        }
        return tempServices;
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openServiceFragment(final DCService service) {
        Intent intent = new Intent();
        intent.setClass(this, ServiceActivity.class);
        intent.putExtra("serviceId", service.getId());
        intent.putExtra("name", service.getName());
        startActivity(intent);
    }

    private void openPluginListActivity() {
        Intent intent = new Intent();
        intent.setClass(this, PluginListActivity.class);
        startActivity(intent);
    }

    private String getPackageName(final String serviceId) {
        for (DCDevicePlugin plugin : mPluginList) {
            if (serviceId.contains(plugin.getId())) {
                return plugin.getPackageName();
            }
        }
        return null;
    }

    private class ServiceAdapter extends BaseAdapter {
        private List<DCService> mServices = new ArrayList<>();

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

            DCService service = (DCService) getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.item_name);
            if (textView != null) {
                textView.setText(service.getName());
            }

            ImageView typeView = (ImageView) view.findViewById(R.id.item_type);
            if (typeView != null) {
                String type = service.getType();
                switch (ServiceDiscoveryProfileConstants.NetworkType.getInstance(type)) {
                    case WIFI:
                        setNetworkTypeIcon(typeView, service, R.drawable.wifi_on);
                        break;
                    case BLUETOOTH:
                    case BLE:
                        setNetworkTypeIcon(typeView, service, R.drawable.bluetooth_on);
                        break;
                    case NFC:
                        setNetworkTypeIcon(typeView, service, R.drawable.nfc_on);
                        break;
                }
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.item_icon);
            if (imageView != null) {
//                String packageName = getPackageName(service.getId());
//                if (packageName != null) {
//                    try {
//                        PackageManager pm = getPackageManager();
//                        ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
//                        setIcon(imageView, service, pm.getApplicationIcon(app.packageName));
//                    } catch (PackageManager.NameNotFoundException e) {
//                    }
//                }
                setIcon(imageView, service, getResources().getDrawable(R.drawable.ic_launcher));
            }
            return view;
        }

        private void setNetworkTypeIcon(final ImageView imageView, final DCService service, final int resId) {
            setIcon(imageView, service, getResources().getDrawable(resId));
        }

        private void setIcon(final ImageView imageView, final DCService service, final Drawable icon) {
            if (icon == null) {
                imageView.setVisibility(View.GONE);
                return;
            }

            Drawable newIcon = icon;
            if (!service.isOnline()) {
                newIcon = Utils.convertToGrayScale(icon);
            } else {
                newIcon.setColorFilter(null);
            }
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(newIcon);
        }
    }
}
