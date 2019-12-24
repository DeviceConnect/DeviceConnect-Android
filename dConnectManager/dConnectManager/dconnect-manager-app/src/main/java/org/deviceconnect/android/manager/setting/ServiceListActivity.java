/*
 ServiceListActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
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
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.DConnectSDKFactory;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
     * アクセストークンに要求するスコープ.
     */
    private static final String[] SCOPES = {
            "serviceDiscovery"
    };

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
     * 選択されたサービスを一時的に格納する変数.
     */
    private ServiceContainer mSelectedService;

    /**
     * サービスのリスト表示.
     */
    private GridView mServiceListGridView;

    /**
     * サービスリストの再読み込みボタン.
     */
    private Button mButtonReloadServiceList;

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
     * Device Connect Manager の設定クラス.
     */
    private DConnectSettings mSettings;

    /**
     * 保存するデータ.
     */
    private SharedData mSharedData;

    /**
     * Device Connect Manager へのアクセス.
     */
    private DConnectSDK mDConnectSDK;

    /**
     * サービス検索中.
     */
    private boolean mServiceDiscoveryFlag;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        mServiceListGridView = findViewById(R.id.activity_service_list_grid_view);
        mButtonReloadServiceList = findViewById(R.id.activity_service_list_search_button);
        mSwitchAction = findViewById(R.id.activity_service_list_manager_switch);

        mSharedData = new SharedData(getApplicationContext());
        mSettings = ((DConnectApplication) getApplication()).getSettings();
        mDConnectSDK = DConnectSDKFactory.create(getApplicationContext(), DConnectSDKFactory.Type.HTTP);

        if (mSharedData.isGuideSettings()) {
            startGuide();
        }

        if (mSharedData.getAccessToken() != null) {
            mDConnectSDK.setAccessToken(mSharedData.getAccessToken());
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

        switch (id) {
            case R.id.activity_service_menu_item_settings:
                openSettings();
                break;
            case R.id.activity_service_menu_item_help:
                openHelp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SSL設定を反映
        mDConnectSDK.setSSL(mSettings.isSSL());

        if (isBonded()) {
            new Thread(() -> {
                try {
                    initUI(waitForManagerStartup(getManagerService()));
                } catch (InterruptedException e) {
                    // ignore.
                }
            }).start();
        }
    }

    @Override
    protected void onManagerBonded(final DConnectService dConnectService) {
        // 起動時に Device Connect Manager が起動フラグが ON の場合にはダイアログを表示
        if (mSettings.isManagerStartFlag()) {
            showStaringManagerDialog();
        }
    }

    @Override
    protected void onManagerDetected(final DConnectService dConnectService, final boolean isRunning) {
        // 起動中のダイアログが表示されているかもしれないので閉じておく
        dismissStartingManagerDialog();
        initUI(isRunning);
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

    /**
     * サービス一覧画面のUIを初期化します.
     *
     * @param isRunning Device Connect Manager の動作状態
     */
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

        mSharedData.saveGuideSettings(result);
    }

    /**
     * DeviceServiceの起動を切り替える.
     *
     * @param checked trueの場合は起動する、falseの場合は停止する
     */
    private void switchDConnectServer(final boolean checked) {
        DConnectService managerService = getManagerService();
        if (managerService == null) {
            return;
        }

        mSwitchAction.setEnabled(false);

        mSettings.setManagerStartFlag(checked);
        if (checked) {
            showStaringManagerDialog();
            startManager((Boolean running) -> {
                initUI(running);
                dismissStartingManagerDialog();
                mSwitchAction.setEnabled(true);
            });
        } else {
            stopManager();
            runOnUiThread(() -> {
                mServiceAdapter.clear();
                mSwitchAction.setEnabled(true);
            });
        }
        setEnableSearchButton(checked);
    }

    /**
     * サービスの情報をコンテナに格納します.
     *
     * @param message サービス情報が入っているメッセージ
     * @return サービスコンテナ
     */
    private ServiceContainer parseService(DConnectMessage message) {
        ServiceContainer service = new ServiceContainer();
        service.setId(message.getString("id"));
        service.setName(message.getString("name"));
        service.setNetworkType(ServiceDiscoveryProfile.NetworkType.getInstance(message.getString("type")));
        service.setOnline(message.getBoolean("online"));
        return service;
    }

    /**
     * サービス一覧を再読み込みを行う.
     */
    private synchronized void reloadServiceList() {
        if (DEBUG) {
            Log.i(TAG, "reloadServiceList a device plugin.");
        }

        if (mServiceDiscoveryFlag) {
            return;
        }
        mServiceDiscoveryFlag = true;

        DConnectService managerService = getManagerService();
        if (managerService == null || !managerService.isRunning()) {
            // DConnectServiceが動作していない
            return;
        }

        serviceDiscovery();
    }

    /**
     * Local OAuth の処理を行います.
     */
    private void authorization() {
        mDConnectSDK.authorization(getString(R.string.app_name), SCOPES, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(String clientId, String accessToken) {
                mSharedData.saveAutoriztion(clientId, accessToken);
                mDConnectSDK.setAccessToken(accessToken);
                serviceDiscovery();
            }
            @Override
            public void onError(int errorCode, String errorMessage) {
                showErrorMessage(errorMessage);
                mServiceDiscoveryFlag = false;
            }
        });
    }

    /**
     * アクセストークンのリフレッシュ処理を行います.
     */
    private void refreshAccessToken() {
        mDConnectSDK.refreshAccessToken(mSharedData.getClientId(), getString(R.string.app_name), SCOPES, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(String clientId, String accessToken) {
                mSharedData.saveAutoriztion(clientId, accessToken);
                mDConnectSDK.setAccessToken(accessToken);
                serviceDiscovery();
            }
            @Override
            public void onError(int errorCode, String errorMessage) {
                showErrorMessage(errorMessage);
                mServiceDiscoveryFlag = false;
            }
        });
    }

    /**
     * サービスを検索します.
     */
    private void serviceDiscovery() {
        showSearchingService();
        mDConnectSDK.serviceDiscovery((response) -> {
            dismissSearchingService();

            if (response.getResult() == DConnectMessage.RESULT_OK) {
                mServiceDiscoveryFlag = false;
                List<ServiceContainer> serviceContainers = new ArrayList<>();
                List serviceArray = response.getList("services");
                for (int i = 0; i < serviceArray.size(); i++) {
                    serviceContainers.add(parseService((DConnectMessage) serviceArray.get(i)));
                }
                runOnUiThread(() -> mServiceAdapter.setServices(serviceContainers));
            } else {
                switch (DConnectMessage.ErrorCode.getInstance(response.getErrorCode())) {
                    case AUTHORIZATION:
                    case EMPTY_ACCESS_TOKEN:
                    case NOT_FOUND_CLIENT_ID:
                        authorization();
                        break;
                    case SCOPE:
                    case EXPIRED_ACCESS_TOKEN:
                        refreshAccessToken();
                        break;
                    default:
                        showErrorMessage(response.getErrorMessage());
                        mServiceDiscoveryFlag = false;
                        break;
                }
            }
        });
    }

    /**
     * 設定画面を開く.
     */
    private void openSettings() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SettingActivity.class);
        startActivity(intent);
    }

    /**
     * ヘルプ画面を開く.
     */
    private void openHelp() {
        String url = BuildConfig.URL_HELP_HTML;
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), WebViewActivity.class);
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
        private final List<ServiceContainer> mServices = new ArrayList<>();

        /**
         * コンストラクタ.
         * @param pluginMgr プラグイン管理クラス
         */
        ServiceAdapter(final DevicePluginManager pluginMgr) {
            mPluginMgr = pluginMgr;
        }

        /**
         * サービスのリストを設定します.
         *
         * @param services サービスのリスト
         */
        void setServices(List<ServiceContainer> services) {
            synchronized (mServices) {
                Collections.sort(services, mComparator);
                mServices.clear();
                mServices.addAll(services);
            }
            notifyDataSetChanged();
        }

        /**
         * サービスのリストを初期化します.
         */
        void clear() {
            synchronized (mServices) {
                mServices.clear();
            }
            notifyDataSetChanged();
        }

        /**
         * サービスの並びをソートします.
         */
        private Comparator<ServiceContainer> mComparator = (lhs, rhs) -> {
            String name1 = lhs.getName();
            String name2 = rhs.getName();
            if (name1 == null || name2 == null) {
                return 0;
            }
            return name1.compareTo(name2);
        };

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

        /**
         * ネットワークタイプを設定します.
         *
         * @param imageView アイコンを表示するView
         * @param service サービス
         * @param resId リソースID
         */
        private void setNetworkTypeIcon(final ImageView imageView, final ServiceContainer service, final int resId) {
            setIcon(imageView, service, ResourcesCompat.getDrawable(getResources(),resId, null));
        }

        /**
         * サービスのアイコンを設定します.
         *
         * @param imageView アイコンを設定するView
         * @param service サービス
         * @param icon アイコン
         */
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

    /**
     * データを保管するクラス.
     */
    private class SharedData {
        /**
         * ガイド用の設定を保存するファイル名を定義する.
         */
        private static final String FILE_NAME = "__service_list__.dat";

        /**
         * ガイド表示設定用のキーを定義する.
         */
        private static final String KEY_SHOW_GUIDE = "show_guide";

        /**
         * クライアントID用のキーを定義する.
         */
        private static final String KEY_CLIENT_ID = "clientId";

        /**
         * アクセストークン用のキーを定義する.
         */
        private static final String KEY_ACCESS_TOKEN = "accessToken";

        /**
         * ガイドの設定を保持するためのクラス.
         */
        private SharedPreferences mSharedPreferences;

        /**
         * コンストラク.
         * @param context コンテキスト
         */
        SharedData(Context context) {
            mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        }

        /**
         * ガイド設定の読み込みを行う.
         * @return ガイドを表示する場合はtrue、それ以外はfalse
         */
        boolean isGuideSettings() {
            return mSharedPreferences.getBoolean(KEY_SHOW_GUIDE, true);
        }

        /**
         * ガイド設定の設定を行う.
         * @param showGuideFlag ガイドを表示する場合はtrue、それ以外はfalse
         */
        void saveGuideSettings(final boolean showGuideFlag) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(KEY_SHOW_GUIDE, showGuideFlag);
            editor.apply();
        }

        /**
         * クライアントIDを取得します.
         *
         * @return クライアントID
         */
        String getClientId() {
            return mSharedPreferences.getString(KEY_CLIENT_ID, null);
        }

        /**
         * クライアントIDを設定します.
         *
         * @param clientId クライアントID
         */
        void setClientId(String clientId) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(KEY_CLIENT_ID, clientId);
            editor.apply();
        }

        /**
         * アクセストークンを取得します.
         *
         * @return アクセストークン
         */
        String getAccessToken() {
            return mSharedPreferences.getString(KEY_ACCESS_TOKEN, null);
        }

        /**
         * アクセストークンを設定します.
         *
         * @param accessToken アクセストークン
         */
        void setAccessToken(String accessToken) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
            editor.apply();
        }

        void saveAutoriztion(String clientId, String accessToken) {
            setClientId(clientId);
            setAccessToken(accessToken);
        }
    }
}
