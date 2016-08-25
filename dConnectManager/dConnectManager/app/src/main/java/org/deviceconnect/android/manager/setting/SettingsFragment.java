/*
 SettingsFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.IDConnectService;
import org.deviceconnect.android.manager.IDConnectWebService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.keepalive.KeepAliveManager;
import org.deviceconnect.android.manager.setting.OpenSourceLicenseFragment.OpenSourceSoftware;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.observer.DConnectObservationService;
import org.deviceconnect.android.observer.receiver.ObserverReceiver;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 設定画面Fragment.
 * 
 * @author NTT DOCOMO, INC.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    /**
     * オープンソースソフトウェア.
     */
    private ArrayList<OpenSourceSoftware> mOpenSourceList;

    /**
     * origin無効時のLocal OAuth、WhiteList設定ダイアログのタグを定義します.
     */
    private static final String TAG_ORIGIN = "origin";

    /**
     * Local OAuth、WhiteList設定時のorigin設定ダイアログのタグを定義します.
     */
    private static final String TAG_REQUIRE_ORIGIN = "require_origin";

    /**
     * Webサーバ起動確認ダイアログのタグを定義します.
     */
    private static final String TAG_WEB_SERVER = "WebServer";

    /** 乱数の最大値. */
    private static final int MAX_NUM = 10000;
    /** キーワードの桁数を定義. */
    private static final int DIGIT = 4;
    /** 10進数の定義. */
    private static final int DECIMAL = 10;
    /** SSL設定チェックボックス. */
    private CheckBoxPreference mCheckBoxSslPreferences;
    /** ポート設定テキストエディッタ. */
    private EditTextPreference mEditPortPreferences;
    /** LocalOAuth設定チェックボックス. */
    private CheckBoxPreference mCheckBoxOauthPreferences;
    /** 外部IP設定チェックボックス. */
    private CheckBoxPreference mCheckBoxExternalPreferences;
    /** オリジン不要フラグ設定チェックボックス. */
    private CheckBoxPreference mCheckBoxRequireOriginPreferences;
    /** Originブロック設定チェックボックス. */
    private CheckBoxPreference mCheckBoxOriginBlockingPreferences;
    /** ポート監視設定チェックボックス。 */
    private CheckBoxPreference mObserverPreferences;
    /** Webサーバのポート設定テキストエディッタ. */
    private EditTextPreference mWebPortPreferences;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // オープソースのリストを準備
        mOpenSourceList = new ArrayList<>();
        mOpenSourceList.add(OpenSourceLicenseFragment.createOpenSourceSoftware(
                "android-support-v4.jar", R.raw.andorid_support_v4));
        mOpenSourceList.add(OpenSourceLicenseFragment.createOpenSourceSoftware(
                "apache-mime4j-0.7.2.jar", R.raw.apache_mime4j));
        mOpenSourceList.add(OpenSourceLicenseFragment.createOpenSourceSoftware(
                "android-support-v4-preferencefragment", R.raw.android_support_v4_preferencefragment));
        mOpenSourceList.add(OpenSourceLicenseFragment.createOpenSourceSoftware(
                "Java WebSocket", R.raw.java_websocket));

        PreferenceScreen versionPreferences = (PreferenceScreen)
                getPreferenceScreen().findPreference(
                        getString(R.string.key_settings_about_appinfo));
        try {
            versionPreferences.setSummary(
                    (getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName));
        } catch (NameNotFoundException e) {
            throw new RuntimeException("could not get my package.");
        }

        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        String keyword = sp.getString(getString(R.string.key_settings_dconn_keyword), null);
        if (keyword == null || keyword.length() <= 0) {
            keyword = createKeyword();
        }

        EditTextPreference editKeywordPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_keyword));
        String docRootPath = sp.getString(getString(R.string.key_settings_web_server_document_root_path), null);
        if (docRootPath == null || docRootPath.length() <= 0) {
            File file = new File(Environment.getExternalStorageDirectory(), getActivity().getPackageName());
            docRootPath = file.getPath();
        }

        editKeywordPreferences.setSummary(keyword);
        editKeywordPreferences.setDefaultValue(keyword);
        editKeywordPreferences.setText(keyword);
        editKeywordPreferences.shouldCommit();

        // SSLのON/OFF
        mCheckBoxSslPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_ssl));

        // ホスト名設定
        EditTextPreference editHostPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_host));
        editHostPreferences.setSummary(editHostPreferences.getText());

        // ポート番号設定
        mEditPortPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_port));
        mEditPortPreferences.setSummary(mEditPortPreferences.getText());

        // Local OAuthのON/OFF
        mCheckBoxOauthPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_local_oauth));

        // グローバル設定のON/OFF
        mCheckBoxExternalPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_allow_external_ip));

        // Origin不要フラグ設定のON/OFF
        mCheckBoxRequireOriginPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_require_origin));

        // Originブロック設定のON/OFF
        mCheckBoxOriginBlockingPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_whitelist_origin_blocking));

        // ポート監視設定のON/OFF
        mObserverPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_observer_on_off));

        // ドキュメントルートパス
        EditTextPreference editDocPreferences = (EditTextPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_web_server_document_root_path));
        editDocPreferences.setSummary(docRootPath);

        EditTextPreference editWebHostPreferences = (EditTextPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_web_server_host));

        mWebPortPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_web_server_port));
        mWebPortPreferences.setSummary(mWebPortPreferences.getText());

        editHostPreferences.setEnabled(false);
        editDocPreferences.setEnabled(false);
        editWebHostPreferences.setEnabled(false);
    }

    @Override
    public void onPause() {
        getActivity().unbindService(mServiceConnection);
        getActivity().unbindService(mWebServiceConnection);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 監視サービスの起動チェック
        mObserverPreferences.setChecked(isObservationServices());
        showIPAddress();

        // サービスとの接続完了まで操作無効
        getPreferenceScreen().setEnabled(false);

        Intent intent = new Intent(IDConnectService.class.getName());
        intent.setPackage(getActivity().getPackageName());
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent2 = new Intent(IDConnectWebService.class.getName());
        intent2.setPackage(getActivity().getPackageName());
        getActivity().bindService(intent2, mWebServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        final String key = preference.getKey();
        if (preference instanceof EditTextPreference) {
            if (getString(R.string.key_settings_dconn_port).equals(key) || 
                    getString(R.string.key_settings_web_server_port).equals(key)) {
                String value = newValue.toString();
                try {
                    // 入力値が整数かチェックする
                    int port = Integer.parseInt(value);
                    if (port < 1024) {
                        return false;
                    }
                    preference.setSummary(value);
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                preference.setSummary(newValue.toString());
            }
        } else if (preference instanceof SwitchPreference) {
            if (getString(R.string.key_settings_dconn_server_on_off).equals(key)) {
                switchDConnectServer((Boolean) newValue);
            } else if (getString(R.string.key_settings_web_server_on_off).equals(key)) {
                switchWebServer((Boolean) newValue);
            } else if (getString(R.string.key_settings_event_keep_alive_on_off).equals(key)) {
                switchEventKeepAlive((Boolean) newValue);
            }
        } else if (preference instanceof CheckBoxPreference) {
            if (getString(R.string.key_settings_dconn_observer_on_off).equals(key)) {
                switchObserver((Boolean) newValue);
            } else if (getString(R.string.key_settings_dconn_require_origin).equals(key)) {
                switchOrigin((Boolean) newValue);
            } else if (getString(R.string.key_settings_dconn_local_oauth).equals(key)
                    || getString(R.string.key_settings_dconn_whitelist_origin_blocking).equals(key)) {
                requiredOrigin((Boolean) newValue);
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference) {
        boolean result = super.onPreferenceTreeClick(preferenceScreen, preference);

        // 各説明をダイアログで表示
        if (getString(R.string.key_settings_open_source_licenses).equals(preference.getKey())) {
            Bundle args = new Bundle();
            args.putParcelableArrayList(OpenSourceLicenseFragment.EXTRA_OSS, mOpenSourceList);
            OpenSourceLicenseFragment fragment = new OpenSourceLicenseFragment();
            fragment.setArguments(args);
            fragment.show(getFragmentManager(), null);
        } else if (getString(R.string.key_settings_about_privacypolicy).equals(preference.getKey())) {
            Bundle policyArgs = new Bundle();
            policyArgs.putInt(Intent.EXTRA_TITLE, R.string.activity_settings_privacy_policy);
            policyArgs.putInt(Intent.EXTRA_TEXT, R.raw.privacypolicy);
            TextDialogFragment fragment = new TextDialogFragment();
            fragment.setArguments(policyArgs);
            fragment.show(getFragmentManager(), null);
        } else if (getString(R.string.key_settings_about_tos).equals(preference.getKey())) {
            Bundle tosArgs = new Bundle();
            tosArgs.putInt(Intent.EXTRA_TITLE, R.string.activity_settings_terms_of_service);
            tosArgs.putInt(Intent.EXTRA_TEXT, R.raw.termsofservice);
            TextDialogFragment fragment = new TextDialogFragment();
            fragment.setArguments(tosArgs);
            fragment.show(getFragmentManager(), null);
        } else if (getString(R.string.key_settings_restart_device_plugin).equals(preference.getKey())) {
            restartDevicePlugins();
        }
        showIPAddress();

        return result;
    }

    /**
     * AlertDialogFragmentでPositiveボタンが押下された時の処理を行う.
     * @param tag タグ
     */
    public void onPositiveButton(final String tag) {
        if (TAG_ORIGIN.equals(tag)) {
            mCheckBoxOauthPreferences.setChecked(false);
            mCheckBoxOriginBlockingPreferences.setChecked(false);
        } else if (TAG_WEB_SERVER.equals(tag)) {
            try {
                mWebService.start();
                setWebUIEnabled(false);
            } catch (RemoteException e) {
                SwitchPreference webPreferences = (SwitchPreference) getPreferenceScreen()
                        .findPreference(getString(R.string.key_settings_web_server_on_off));
                webPreferences.setChecked(false);
            }
        } else if (TAG_REQUIRE_ORIGIN.equals(tag)) {
            mCheckBoxRequireOriginPreferences.setChecked(true);
        }
    }

    /**
     * AlertDialogFragmentでNegativeボタンが押下された時の処理を行う.
     * @param tag タグ
     */
    public void onNegativeButton(final String tag) {
        if (TAG_ORIGIN.equals(tag)) {
            mCheckBoxRequireOriginPreferences.setChecked(true);
        } else if (TAG_WEB_SERVER.equals(tag)) {
            SwitchPreference webPreferences = (SwitchPreference) getPreferenceScreen()
                    .findPreference(getString(R.string.key_settings_web_server_on_off));
            webPreferences.setChecked(false);
        } else if (TAG_REQUIRE_ORIGIN.equals(tag)) {
            mCheckBoxOauthPreferences.setChecked(false);
            mCheckBoxOriginBlockingPreferences.setChecked(false);
        }
    }

    /**
     * DConnectサーバの有効・無効を設定する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchDConnectServer(final boolean checked) {
        setUIEnabled(!checked);
        try {
            if (checked) {
                mDConnectService.start();
            } else {
                mDConnectService.stop();
                notifyManagerTerminate();
            }
        } catch (RemoteException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Webサーバの有効・無効を設定する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchWebServer(final boolean checked) {
        if (checked) {
            String title = getString(R.string.activity_settings_web_server_warning_title);
            String message = getString(R.string.activity_settings_web_server_warning_message);
            String positive = getString(R.string.activity_settings_web_server_warning_positive);
            String negative = getString(R.string.activity_settings_web_server_warning_negative);
            AlertDialogFragment dialog = AlertDialogFragment.create(TAG_WEB_SERVER,
                    title, message, positive, negative);
            dialog.show(getFragmentManager(), TAG_WEB_SERVER);
        } else {
            try {
                mWebService.stop();
            } catch (RemoteException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            setWebUIEnabled(true);
        }
    }

    /**
     * 監視機能の有効・無効を設定する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchObserver(final boolean checked) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), ObserverReceiver.class);
        if (checked) {
            intent.setAction(DConnectObservationService.ACTION_START);
            intent.putExtra(DConnectObservationService.PARAM_RESULT_RECEIVER,
                    new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode != Activity.RESULT_OK) {
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mObserverPreferences.setChecked(false);
                                    }
                                }, 1000);
                            }
                        }
                    });
        } else {
            intent.setAction(DConnectObservationService.ACTION_STOP);
        }
        getActivity().sendBroadcast(intent);
    }

    /**
     * KeepAlive機能の有効・無効を設定する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchEventKeepAlive(final boolean checked) {
        if (checked) {
            KeepAliveManager.getInstance().enableKeepAlive();
        } else {
            KeepAliveManager.getInstance().disableKeepAlive();
        }
    }

    /**
     * Originの要求ダイアログを表示する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchOrigin(final boolean checked) {
        if (!checked) {
            List<String> settings = new ArrayList<>();
            if (mCheckBoxOauthPreferences.isChecked()) {
                settings.add(getString(R.string.activity_settings_local_oauth));
            }
            if (mCheckBoxOriginBlockingPreferences.isChecked()) {
                settings.add(getString(R.string.activity_settings_whitelist_enable));
            }

            if (settings.size() > 0) {
                StringBuilder list = new StringBuilder();
                for (int i = 0; i < settings.size(); i++) {
                    list.append(" - ");
                    list.append(settings.get(i));
                    list.append("\n");
                }
                String title = getString(R.string.activity_settings_warning);
                String message = getString(R.string.activity_settings_warning_require_origin_disabled, list);
                String positive = getString(R.string.activity_settings_yes);
                String negative = getString(R.string.activity_settings_no);
                AlertDialogFragment dialog = AlertDialogFragment.create(TAG_ORIGIN,
                        title, message, positive, negative);
                dialog.show(getFragmentManager(), TAG_ORIGIN);
            }
        }
    }

    /**
     * Originの設定を要求する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void requiredOrigin(final boolean checked) {
        boolean requiredOrigin = mCheckBoxRequireOriginPreferences.isChecked();
        if (checked && !requiredOrigin) {
            StringBuilder list = new StringBuilder();
            list.append(" - ");
            list.append(getString(R.string.activity_settings_require_origin));
            list.append("\n");

            String title = getString(R.string.activity_settings_warning);
            String message = getString(R.string.activity_settings_warning_require_origin_enabled, list);
            String positive = getString(R.string.activity_settings_yes);
            String negative = getString(R.string.activity_settings_no);

            AlertDialogFragment dialog = AlertDialogFragment.create(TAG_REQUIRE_ORIGIN,
                    title, message, positive, negative);
            dialog.show(getFragmentManager(), TAG_REQUIRE_ORIGIN);
        }
    }

    /**
     * UIの有効・無効を設定する.
     * @param enabled trueの場合は有効、falseの場合は無効
     */
    private void setUIEnabled(final boolean enabled) {
        mCheckBoxSslPreferences.setEnabled(enabled);
        mEditPortPreferences.setEnabled(enabled);
        mCheckBoxOauthPreferences.setEnabled(enabled);
        mCheckBoxExternalPreferences.setEnabled(enabled);
        mCheckBoxRequireOriginPreferences.setEnabled(enabled);
        mCheckBoxOriginBlockingPreferences.setEnabled(enabled);
    }

    /**
     * UIの有効・無効を設定する.
     * @param enabled trueの場合は有効、falseの場合は無効
     */
    private void setWebUIEnabled(final boolean enabled) {
        mWebPortPreferences.setEnabled(enabled);
    }

    /**
     * サービスに起動確認を行う.
     *
     * @param c コンテキスト
     * @param cls クラス
     * @return 起動中の場合はtrue、それ以外はfalse
     */
    private boolean isServiceRunning(final Context c, final Class<?> cls) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo i : runningService) {
            if (cls.getName().equals(i.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * キーワードを作成する.
     * 
     * @return キーワード
     */
    private String createKeyword() {
        StringBuilder builder = new StringBuilder();
        builder.append("DCONNECT-");
        int rand = Math.abs(new Random().nextInt() % MAX_NUM);
        for (int i = 0; i < DIGIT; i++) {
            int r = rand % DECIMAL;
            builder.append(r);
            rand /= DECIMAL;
        }
        return builder.toString();
    }

    /**
     * dConnectManagerの監視サービスの起動状態を取得する.
     * 
     * @return 起動している場合はtrue、それ以外はfalse
     */
    private boolean isObservationServices() {
        return isServiceRunning(getActivity(), DConnectObservationService.class);
    }

    /**
     * Start all device plugins.
     */
    private void restartDevicePlugins() {
        final StartingDialogFragment dialog = new StartingDialogFragment();
        dialog.show(getFragmentManager(), null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DevicePluginManager mgr = new DevicePluginManager(getActivity(), null);
                mgr.createDevicePluginList();
                List<DevicePlugin> plugins = mgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.getStartServiceClassName() != null
                            && plugin.getServiceId() != null) {
                        restartDevicePlugin(plugin);
                    }
                }
                if (dialog.isResumed()) {
                    dialog.dismiss();
                }
            }
        }).start();
    }

    /**
     * Start a device plugin.
     * 
     * @param plugin device plugin to be started
     */
    private void restartDevicePlugin(final DevicePlugin plugin) {
        Intent request = new Intent();
        request.setComponent(plugin.getComponentName());
        request.setAction(IntentDConnectMessage.ACTION_DEVICEPLUGIN_RESET);
        request.putExtra("pluginId", plugin.getServiceId());
        getActivity().sendBroadcast(request);
    }

    /**
     * Show a dialog of restart a device plugin.
     */
    public static class StartingDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            String title = getString(R.string.activity_settings_restart_device_plugin_title);
            String msg = getString(R.string.activity_settings_restart_device_plugin_message);
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(title);
            progressDialog.setMessage(msg);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            setCancelable(false);
            return progressDialog;
        }

        @Override
        public void onPause() {
            dismiss();
            super.onPause();
        }
    }

    /**
     * Manager termination notification to all device plug-ins.
     */
    private void notifyManagerTerminate() {
        final ManagerTerminationFragment dialog = new ManagerTerminationFragment();
        dialog.show(getFragmentManager(), null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DevicePluginManager mgr = new DevicePluginManager(getActivity(), null);
                mgr.createDevicePluginList();
                List<DevicePlugin> plugins = mgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.getServiceId() != null) {
                        Intent request = new Intent();
                        request.setComponent(plugin.getComponentName());
                        request.setAction(IntentDConnectMessage.ACTION_MANAGER_TERMINATED);
                        request.putExtra("pluginId", plugin.getServiceId());
                        getActivity().sendBroadcast(request);
                    }
                }
                if (dialog.isResumed()) {
                    dialog.dismiss();
                }
            }
        }).start();
    }

    /**
     * Show a dialog of manager termination processing.
     */
    public  static class ManagerTerminationFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            String title = getString(R.string.activity_settings_manager_terminate_title);
            String msg = getString(R.string.activity_settings_manager_terminate_message);
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(title);
            progressDialog.setMessage(msg);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            setCancelable(false);
            return progressDialog;
        }

        @Override
        public void onPause() {
            dismiss();
            super.onPause();
        }
    }

    /**
     * Show IP Address.
     */
    private void showIPAddress() {
        String ipAddress = DConnectUtil.getIPAddress(getActivity());

        // Set Host IP Address.
        EditTextPreference editHostPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_host));
        editHostPreferences.setSummary(ipAddress);
        
        // Set Host IP Address.
        EditTextPreference webHostPref = (EditTextPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_web_server_host));
        webHostPref.setSummary(ipAddress);
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
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean running = mDConnectService.isRunning();
                            setUIEnabled(!running);
                            SwitchPreference serverPreferences = (SwitchPreference) getPreferenceScreen()
                                    .findPreference(getString(R.string.key_settings_dconn_server_on_off));
                            serverPreferences.setChecked(running);

                            checkServiceConnections();
                        } catch (RemoteException e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
        }
    };

    /**
     * DConnectWebServiceを操作するためのクラス.
     */
    private IDConnectWebService mWebService;

    /**
     * DConnectWebServiceと接続するためのクラス.
     */
    private final ServiceConnection mWebServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mWebService = (IDConnectWebService) service;
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean running = mWebService.isRunning();
                            setWebUIEnabled(!running);
                            SwitchPreference webPreferences = (SwitchPreference) getPreferenceScreen()
                                    .findPreference(getString(R.string.key_settings_web_server_on_off));
                            webPreferences.setChecked(running);

                            checkServiceConnections();
                        } catch (RemoteException e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mWebService = null;
        }
    };

    private synchronized void checkServiceConnections() {
        if (mDConnectService != null && mWebService != null) {
            enablePreference();
        }
    }

    private void enablePreference() {
        // 設定画面の有効化
        getPreferenceScreen().setEnabled(true);

        // 設定変更イベントの受信開始
        mCheckBoxSslPreferences.setOnPreferenceChangeListener(this);
        EditTextPreference editHostPreferences = (EditTextPreference) getPreferenceScreen()
            .findPreference(getString(R.string.key_settings_dconn_host));
        editHostPreferences.setOnPreferenceChangeListener(this);
        EditTextPreference editKeywordPreferences = (EditTextPreference) getPreferenceScreen()
            .findPreference(getString(R.string.key_settings_dconn_keyword));
        editKeywordPreferences.setOnPreferenceChangeListener(this);
        mEditPortPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxOauthPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxExternalPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxRequireOriginPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxOriginBlockingPreferences.setOnPreferenceChangeListener(this);
        mObserverPreferences.setOnPreferenceChangeListener(this);
        mWebPortPreferences.setOnPreferenceChangeListener(this);
        SwitchPreference serverPreferences = (SwitchPreference) getPreferenceScreen()
            .findPreference(getString(R.string.key_settings_dconn_server_on_off));
        serverPreferences.setOnPreferenceChangeListener(this);
        SwitchPreference webPreferences = (SwitchPreference) getPreferenceScreen()
            .findPreference(getString(R.string.key_settings_web_server_on_off));
        webPreferences.setOnPreferenceChangeListener(this);
        SwitchPreference eventKeepAlive = (SwitchPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_event_keep_alive_on_off));
        eventKeepAlive.setOnPreferenceChangeListener(this);
    }

}
