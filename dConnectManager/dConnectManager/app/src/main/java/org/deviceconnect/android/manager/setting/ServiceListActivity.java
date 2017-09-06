/*
 ServiceListActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.animation.Animator;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.plugin.MessagingException;
import org.deviceconnect.android.manager.util.AnimationUtil;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.ServiceContainer;
import org.deviceconnect.android.manager.util.ServiceDiscovery;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * サービス一覧を表示するActivity.
 *
 * @author NTT DOCOMO, INC.
 */
public class ServiceListActivity extends BaseSettingActivity implements AlertDialogFragment.OnAlertDialogListener {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Manager";

    /**
     * サービスがオフラインの時に表示するダイアログのタグ名を定義する.
     */
    private static final String TAG_OFFLINE = "offline";

    /**
     * ガイド用の設定を保存するファイル名を定義する.
     */
    private static final String FILE_NAME = "__service_list__.dat";

    /**
     * ガイド表示設定用のキーを定義する.
     */
    private static final String KEY_SHOW_GUIDE = "show_guide";

    /**
     * ガイド用のレイアウト一覧.
     */
    private static final int[] GUIDE_ID_LIST = {
            R.id.activity_service_guide_1,
            R.id.activity_service_guide_2,
    };

    /**
     * サービスアダプタ.
     */
    private ServiceAdapter mServiceAdapter;

    /**
     * ガイドの設定を保持するためのクラス.
     */
    private SharedPreferences mSharedPreferences;

    /**
     * serviceDiscoveryを実行するクラス.
     */
    private ServiceDiscovery mServiceDiscovery;

    /**
     * 選択されたサービスを一時的に格納する変数.
     */
    private ServiceContainer mSelectedService;

    /**
     * ハンドラ.
     */
    private Handler mHandler = new Handler();

    /**
     * Device Connect Managerの起動スイッチ.
     */
    private Switch mSwitchAction;

    /**
     * ガイドの表示ページ.
     */
    private int mPageIndex;

    /**
     * Device Connect Managerの設定クラス.
     */
    private DConnectSettings mSettings;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        mSettings = ((DConnectApplication) getApplication()).getSettings();

        if (loadGuideSettings(this)) {
            startGuide();
        }
    }

    @Override
    protected void onManagerBonded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mServiceAdapter = new ServiceAdapter(getPluginManager());

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
                            reloadServiceList();
                        }
                    });
                }

                DConnectService managerService = getManagerService();
                if (mSwitchAction != null) {
                    mSwitchAction.setChecked(managerService.isRunning());
                }
                if (managerService.isRunning()) {
                    reloadServiceList();
                }
                setEnableSearchButton(managerService.isRunning());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isBonded() && !hasDevicePlugins()) {
            showNoDevicePlugin();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_service_list, menu);

        mSwitchAction = (Switch) menu.findItem(R.id.activity_service_manager_power).getActionView();
        if (mSwitchAction != null) {
            mSwitchAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    switchDConnectServer(isChecked);
                }
            });

            DConnectService managerService = getManagerService();
            if (managerService != null) {
                mSwitchAction.setChecked(managerService.isRunning());
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
            if (getManagerService() != null) {
                btn.setEnabled(running);
            }
        }
    }

    /**
     * デバイスプラグインを保有しているかを確認する.
     * @return デバイスプラグインを保有している場合はtrue、それ以外はfalse
     */
    private boolean hasDevicePlugins() {
        return getPluginManager().getDevicePlugins().size() > 0;
    }

    /**
     * ガイド設定の読み込みを行う.
     * @param context コンテキスト
     * @return ガイドを表示する場合はtrue、それ以外はfalse
     */
    private boolean loadGuideSettings(final Context context) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_SHOW_GUIDE, true);
    }

    /**
     * ガイド設定の設定を行う.
     * @param showGuideFlag ガイドを表示する場合はtrue、それ以外はfalse
     */
    private void saveGuideSettings(final boolean showGuideFlag) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_SHOW_GUIDE, showGuideFlag);
        editor.apply();
    }

    /**
     * ガイドを表示を開始する.
     */
    private void startGuide() {
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

    /**
     * 次のガイドに移動する.
     */
    private void nextGuide() {
        if (mPageIndex == GUIDE_ID_LIST.length - 1) {
            endGuide();
        } else {
            animateGuide(new AnimationUtil.AnimationAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    mPageIndex++;
                    visibleGuide();
                }
            });
        }
    }

    /**
     * ガイドをアニメーションする.
     * @param listener アニメーション通知リスナー
     */
    private void animateGuide(final AnimationUtil.AnimationAdapter listener) {
        for (int i = 0; i < GUIDE_ID_LIST.length; i++) {
            View view = findViewById(GUIDE_ID_LIST[i]);
            if (i == mPageIndex) {
                AnimationUtil.animateAlpha(view, listener);
            }
        }
    }

    /**
     * ガイドを表示する.
     */
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

    /**
     * ガイドの終了処理を行う.
     */
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
                public void onAnimationEnd(final Animator animation) {
                    guideView.setVisibility(View.GONE);
                }
            });
        }

        saveGuideSettings(result);
    }

    /**
     * DeviceServiceの起動を切り替える.
     * @param checked trueの場合は起動する、falseの場合は停止する
     */
    private void switchDConnectServer(final boolean checked) {
        DConnectService managerService = getManagerService();
        if (managerService == null) {
            return;
        }

        if (mServiceDiscovery != null) {
            return;
        }

        mSwitchAction.setEnabled(false);

        mSettings.setManagerStartFlag(checked);
        if (checked) {
            managerService.startInternal();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    reloadServiceList();
                    mSwitchAction.setEnabled(true);
                }
            }, 500);
        } else {
            managerService.stopInternal();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mServiceAdapter.mServices = new ArrayList<>();
                    mServiceAdapter.notifyDataSetInvalidated();
                    mSwitchAction.setEnabled(true);
                }
            });
        }
        setEnableSearchButton(checked);
    }

    /**
     * サービス一覧を再読み込みを行う.
     */
    private void reloadServiceList() {
        if (DEBUG) {
            Log.i(TAG, "reloadServiceList a device plugin.");
        }

        // DConnectServiceが動作していない
        DConnectService managerService = getManagerService();
        if (managerService == null || !managerService.isRunning()) {
            return;
        }

        // 既にserviceDiscoveryが実行されている場合
        if (mServiceDiscovery != null) {
            return;
        }

        mServiceDiscovery = new ServiceDiscovery(this, mSettings) {
            private DialogFragment mDialog;

            @Override
            protected void onPreExecute() {
                try {
                    mDialog = new ServiceDiscoveryDialogFragment();
                    mDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.w(TAG, "Failed to open the dialog for service discovery.");
                    }
                }
            }

            @Override
            protected void onPostExecute(final List<ServiceContainer> serviceContainers) {
                try {
                    mDialog.dismiss();

                    View view = findViewById(R.id.activity_service_no_service);
                    if (view != null) {
                        view.setVisibility(serviceContainers.size() == 0 ? View.VISIBLE : View.GONE);
                    }

                    mServiceAdapter.mServices = serviceContainers;
                    mServiceAdapter.notifyDataSetInvalidated();
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.w(TAG, "Failed to dismiss the dialog for service discovery.");
                    }
                } finally {
                    mServiceDiscovery = null;
                }
            }
        };
        mServiceDiscovery.execute();
    }

    /**
     * 設定画面を開く.
     */
    private void openSettings() {
        Intent intent = new Intent();
        intent.setClass(this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * ヘルプ画面を開く.
     */
    private void openHelp() {
        String url = BuildConfig.URL_HELP_HTML;
        Intent intent = new Intent();
        intent.setClass(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.activity_help_title));
        startActivity(intent);
    }

    /**
     * サービスの確認画面を開く.
     * @param position 開くサービスの紐付いているポジション
     */
    private void openServiceInfo(final int position) {
        mSelectedService = (ServiceContainer) mServiceAdapter.getItem(position);
        String url = BuildConfig.URL_DEMO_HTML + "?serviceId=" + mSelectedService.getId();
        Intent intent = new Intent();
        intent.setClass(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, mSelectedService.getName());
        startActivity(intent);
    }

    /**
     * デバイスプラグインが一つもない場合のダイアログを表示する.
     */
    private void showNoDevicePlugin() {
        String title = getString(R.string.activity_service_list_no_plugin_title);
        String message = getString(R.string.activity_service_list_no_plugin_message);
        String positive = getString(R.string.activity_service_list_no_plugin_positive);
        AlertDialogFragment dialog = AlertDialogFragment.create("no-device-plugin", title, message, positive);
        dialog.show(getFragmentManager(), "no-device-plugin");
    }

    /**
     * デバイスプラグインの設定画面を開く.
     * <p>
     * {@link #mSelectedService}に格納されているデバイスプラグインの設定画面を開く。
     * </p>
     */
    private void openPluginSettings() {
        DevicePluginManager mgr = getPluginManager();
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
                try {
                    plugin.send(request);
                } catch (MessagingException e) {
                    showMessagingErrorDialog(e);
                }
                break;
            }
        }
    }

    /**
     * 指定されたサービスのパッケージ名を取得する.
     * @param serviceId サービスID
     * @return パッケージ名
     */
    private String getPackageName(final String serviceId) {
        List<DevicePlugin> list = getPluginManager().getDevicePlugins();
        for (DevicePlugin plugin : list) {
            if (serviceId.contains(plugin.getPluginId())) {
                return plugin.getPackageName();
            }
        }
        return null;
    }

    /**
     * Device Connect Managerに接続されているサービスを表示するためのアダプタークラス.
     */
    private class ServiceAdapter extends BaseAdapter {
        /**
         * プラグイン管理クラス.
         */
        private final DevicePluginManager mPluginMgr;
        /**
         * サービス一覧.
         */
        private List<ServiceContainer> mServices = new ArrayList<>();

        ServiceAdapter(final DevicePluginManager pluginMgr) {
            mPluginMgr = pluginMgr;
        }

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
                List<DevicePlugin> plugins = mPluginMgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (service.getId().contains(plugin.getPluginId())) {
                        setIcon(imageView, service, plugin.getPluginIcon(ServiceListActivity.this));
                        break;
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
