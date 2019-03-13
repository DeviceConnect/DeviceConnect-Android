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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.plugin.MessagingException;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.util.AnimationUtil;
import org.deviceconnect.android.manager.util.ServiceContainer;
import org.deviceconnect.android.manager.util.ServiceDiscovery;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * サービス一覧を表示するActivity.
 *
 * @author NTT DOCOMO, INC.
 */
public class ServiceListActivity extends BaseSettingActivity implements AlertDialogFragment.OnAlertDialogListener {

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "Manager";

    /**
     * プラグイン設定画面を開くか確認するダイアログのタグ名を定義する.
     */
    private static final String TAG_OPEN_PLUGIN_SETTING = "open_plugin_setting";

    /**
     * ガイド用の設定を保存するファイル名を定義する.
     */
    private static final String FILE_NAME = "__service_list__.dat";

    /**
     * ガイド表示設定用のキーを定義する.
     */
    private static final String KEY_SHOW_GUIDE = "show_guide";
    /**
     * Hostプラグインの検索リトライ回数.
     */
    private static final int RETRY_COUNT = 5;
    /**
     * 検索ダイアログ.
     */
    private WeakReference<DialogFragment> mDialog;

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
     * サービスのリスト表示.
     */
    GridView mServiceListGridView;

    /**
     * サービスリストの再読み込みボタン.
     */
    Button mButtonReloadServiceList;

    /**
     * ハンドラ.
     */
    private Handler mHandler = new Handler();

    /**
     * Device Connect Managerの起動スイッチ.
     */
    private Switch mSwitchAction;

    /**
     * Device Connect Managerの起動スイッチのリスナー
     */
    private final CompoundButton.OnCheckedChangeListener mSwitchActionListener = (buttonView, isChecked) -> switchDConnectServer(isChecked);

    /**
     * ガイドの表示ページ.
     */
    private int mPageIndex;

    /**
     * Device Connect Managerの設定クラス.
     */
    private DConnectSettings mSettings;
    /**
     * Hostデバイスが見つかるまでに行うServiceDiscoveryのリトライ回数が、現在何回目かを保持する.
     */
    private int mRetry;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        mServiceListGridView = findViewById(R.id.activity_service_list_grid_view);
        mButtonReloadServiceList = findViewById(R.id.activity_service_list_search_button);
        mSwitchAction = findViewById(R.id.activity_service_list_manager_switch);

        mSettings = ((DConnectApplication) getApplication()).getSettings();

        if (loadGuideSettings(ServiceListActivity.this)) {
            startGuide();
        }
    }

    @Override
    protected void onManagerDetected(final DConnectService manager, final boolean isRunning) {
        initUI(isRunning);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRetry = 0;
        if (isBonded()) {
            if (!hasDevicePlugins()) {
                showNoDevicePlugin();
            } else {
                initUI(isDConnectServiceRunning());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_service_list, menu);

        MenuItem managerSwitch = menu.findItem(R.id.activity_service_manager_power);
        if (managerSwitch == null) {
            return super.onCreateOptionsMenu(menu);
        }

        Switch switchAction = (Switch) managerSwitch.getActionView();
        if (switchAction != null) {
            mSwitchAction = switchAction;

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mSwitchAction.setPadding((int) (12 * metrics.density), 0, (int) (12 * metrics.density), 0);
            mSwitchAction.setOnCheckedChangeListener(mSwitchActionListener);

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
        if (TAG_OPEN_PLUGIN_SETTING.equals(tag)) {
            openPluginSettings();
        }
    }

    @Override
    public void onNegativeButton(final String tag) {
    }

    private void initUI(boolean isRunning) {
        runOnUiThread(() -> {
            mServiceAdapter = new ServiceAdapter(getPluginManager());

            if (mServiceListGridView != null) {
                mServiceListGridView.setAdapter(mServiceAdapter);
                mServiceListGridView.setOnItemClickListener((parent, view, position, id) -> openServiceInfo(position));
                mServiceListGridView.setOnItemLongClickListener((parent, view, position, id) -> {
                    openPluginSetting(position);
                    return true;
                });
            }

            if (mButtonReloadServiceList != null) {
                mButtonReloadServiceList.setOnClickListener((v) -> reloadServiceList());
            }

            if (mSwitchAction != null) {
                mSwitchAction.setOnCheckedChangeListener(mSwitchActionListener);
                mSwitchAction.setChecked(isRunning);
            }
            setEnableSearchButton(isRunning);
            if (isRunning) {
                reloadServiceList();
            }
        });
    }

    /**
     * Searchボタンの有効無効を切り替える.
     * @param running Managerの実行状態
     */
    private void setEnableSearchButton(final boolean running) {
        Button btn = findViewById(R.id.activity_service_list_search_button);
        FrameLayout fl = findViewById(R.id.activity_service_no_service);
        if (getManagerService() != null) {
            if (btn != null) {
                btn.setEnabled(running);
            }
            if (fl != null) {
                if (running) {
                    fl.setVisibility(View.GONE);
                } else {
                    fl.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * デバイスプラグインを保有しているかを確認する.
     * @return デバイスプラグインを保有している場合はtrue、それ以外はfalse
     */
    private boolean hasDevicePlugins() {
        DevicePluginManager mgr = getPluginManager();
        return mgr != null && mgr.getDevicePlugins().size() > 0;
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
            guideView.setOnClickListener((v) -> nextGuide());
        }

        Button button = findViewById(R.id.activity_service_guide_button);
        if (button != null) {
            button.setOnClickListener((v) -> nextGuide());
        }
    }

    /**
     * ガイドのボタンの有効・無効を設定する.
     * @param enable true: 有効、false: 無効
     */
    private void setGuideClickable(boolean enable) {
        View guideView = findViewById(R.id.activity_service_guide);
        if (guideView != null) {
            guideView.setClickable(enable);
        }

        Button button = findViewById(R.id.activity_service_guide_button);
        if (button != null) {
            button.setEnabled(enable);
        }
    }

    /**
     * 次のガイドに移動する.
     */
    private void nextGuide() {
        if (mPageIndex == GUIDE_ID_LIST.length - 1) {
            endGuide();
        } else {
            setGuideClickable(false);

            View view = findViewById(GUIDE_ID_LIST[mPageIndex]);
            AnimationUtil.animateAlpha(view, new AnimationUtil.AnimationAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    mPageIndex++;
                    visibleGuide();
                }
            });
        }
    }

    /**
     * ガイドを表示する.
     */
    private void visibleGuide() {
        View view = findViewById(GUIDE_ID_LIST[mPageIndex]);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            AnimationUtil.animateAlpha2(view, new AnimationUtil.AnimationAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    setGuideClickable(true);
                }
            });
        }
    }

    /**
     * ガイドの終了処理を行う.
     */
    private void endGuide() {
        boolean result = true;
        CheckBox checkBox = findViewById(R.id.activity_service_guide_checkbox);
        if (checkBox != null) {
            result = !checkBox.isChecked();
        }

        setGuideClickable(false);

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

            // DConnectServiceを起動しておかないとbindが切れた時にサービスが止まってしまう
            Intent intent = new Intent();
            intent.setClass(this, DConnectService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }

            managerService.startInternal();
            mHandler.postDelayed(() -> {
                reloadServiceList();
                mSwitchAction.setEnabled(true);
            }, 500);
        } else {
            managerService.stopInternal();
            runOnUiThread(() -> {
                mServiceAdapter.mServices = new ArrayList<>();
                mServiceAdapter.notifyDataSetInvalidated();
                mSwitchAction.setEnabled(true);
            });

            // サービスを停止しておく
            Intent intent = new Intent();
            intent.setClass(this, DConnectService.class);
            stopService(intent);
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

        mServiceDiscovery = new ServiceDiscovery(this, mSettings, new ServiceDiscovery.Callback() {
            @Override
            public void onPreExecute() {
                try {
                    mDialog = new WeakReference<>(new ServiceDiscoveryDialogFragment());
                    mDialog.get().show(getFragmentManager(), null);
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.w(TAG, "Failed to open the dialog for service discovery.");
                    }
                }
            }

            @Override
            public void onPostExecute(List<ServiceContainer> serviceContainers) {
                try {
                    mDialog.get().dismiss();

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
                    boolean isHost = false;
                    // Hostプラグインが見つからない場合はリトライする
                    for (ServiceContainer service : mServiceAdapter.mServices) {
                        if (service.getName().contains("Host")) {
                            isHost = true;
                            break;
                        }
                    }
                    if (isHost || mRetry == RETRY_COUNT) {
                        mServiceDiscovery = null;
                        mRetry = 0;
                    } else {
                        mServiceDiscovery = null;
                        mRetry++;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        reloadServiceList();
                    }
                }
            }
        });
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
        intent.putExtra(WebViewActivity.EXTRA_SSL, isSSL());
        startActivity(intent);
    }

    /**
     * サービスの確認画面を開く.
     * @param position 開くサービスの紐付いているポジション
     */
    private void openServiceInfo(final int position) {
        mSelectedService = (ServiceContainer) mServiceAdapter.getItem(position);

        DevicePlugin plugin = findDevice(mSelectedService.getId());
        if (plugin == null) {
            return;
        }
        Boolean isSSL = isSSL();
        if (isSSL == null) {
            return;
        }

        String url = BuildConfig.URL_DEMO_HTML + "?serviceId=" + mSelectedService.getId();
        Intent intent = new Intent();
        intent.setClass(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, mSelectedService.getName());
        intent.putExtra(WebViewActivity.EXTRA_SSL, isSSL());
        intent.putExtra(WebViewActivity.EXTRA_SERVICE_ID, mSelectedService.getId());
        intent.putExtra(WebViewActivity.EXTRA_PLUGIN_ID, plugin.getPluginId());
        startActivity(intent);
    }

    /**
     * 指定されたサービスIDに対応したプラグインを取得します
     * @param serviceId サービスID
     * @return プラグイン
     */
    private DevicePlugin findDevice(final String serviceId) {
        DevicePluginManager manager = getPluginManager();
        if (manager != null) {
            for (DevicePlugin plugin : manager.getDevicePlugins()) {
                if (serviceId.contains(plugin.getPluginId())) {
                    return plugin;
                }
            }
        }
        return null;
    }

    /**
     * プラグインの設定画面を開く確認ダイアログを表示する.
     * @param position プラグインの位置
     */
    private void openPluginSetting(final int position) {
        mSelectedService = (ServiceContainer) mServiceAdapter.getItem(position);

        String title = getString(R.string.activity_service_list_plugin_setting_title);
        String message = getString(R.string.activity_service_list_plugin_setting_message);
        String positive = getString(R.string.activity_service_list_plugin_setting_positive);
        String negative = getString(R.string.activity_service_list_plugin_setting_negative);
        AlertDialogFragment dialog = AlertDialogFragment.create(TAG_OPEN_PLUGIN_SETTING, title, message, positive, negative);
        dialog.show(getFragmentManager(), TAG_OPEN_PLUGIN_SETTING);
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
                view = View.inflate(getApplicationContext(), R.layout.item_service_list, null);
            }

            ServiceContainer service = (ServiceContainer) getItem(position);

            TextView textView = view.findViewById(R.id.item_name);
            if (textView != null) {
                textView.setTextColor(Color.BLACK);
                textView.setText(service.getName());
            }

            ImageView typeView = view.findViewById(R.id.item_type);
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

            ImageView imageView = view.findViewById(R.id.item_icon);
            if (imageView != null && mPluginMgr != null) {
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
