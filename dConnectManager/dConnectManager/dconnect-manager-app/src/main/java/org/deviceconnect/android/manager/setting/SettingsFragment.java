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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DConnectWebService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.setting.OpenSourceLicenseFragment.OpenSourceSoftware;
import org.deviceconnect.android.manager.util.PauseHandler;
import org.deviceconnect.android.observer.DConnectObservationService;
import org.deviceconnect.android.observer.receiver.ObserverReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Availabilityの表示のOn/OFF設定ダイアログのタグを定義します.
     */
    private static final String TAG_AVAILABILITY = "availability";

    /**
     * Webサーバの起動に失敗ダイアログのタグを定義します.
     */
    private static final String TAG_ERROR_WEB_SERVER = "error_web_server";

    /**
     * Webサーバ起動失敗エラーを表示するためのメッセージを定義します.
     */
    private static final int MSG_SHOW_ERROR_DIALOG = 1;

    /** SSL設定チェックボックス. */
    private CheckBoxPreference mCheckBoxSslPreferences;
    /** ポート設定テキストエディッタ. */
    private EditTextPreference mEditPortPreferences;
    /** LocalOAuth設定チェックボックス. */
    private CheckBoxPreference mCheckBoxOauthPreferences;
    /** 外部IP設定チェックボックス. */
    private CheckBoxPreference mCheckBoxExternalIpPreferences;
    /** 外部起動/終了設定チェックボックス. */
    private CheckBoxPreference mCheckBoxExternalStartAndStartPreferences;
    /** オリジン不要フラグ設定チェックボックス. */
    private CheckBoxPreference mCheckBoxRequireOriginPreferences;
    /** Originブロック設定チェックボックス. */
    private CheckBoxPreference mCheckBoxOriginBlockingPreferences;
    /** Manager名の表示オンオフのチェックボックス. */
    private CheckBoxPreference mCheckBoxManagerNameVisiblePreferences;

    /** ポート監視設定チェックボックス。 */
    private CheckBoxPreference mObserverPreferences;
    /** Webサーバのポート設定テキストエディッタ. */
    private EditTextPreference mWebPortPreferences;

    /** アクセスログ設定用チェックボックス */
    private CheckBoxPreference mCheckBoxAccessLogPreferences;

    /**
     * 一時中断用ハンドラー.
     * <p>
     * パーミッションのリクエストを行うと一時中断が発生しますので、
     * 画面が戻ってきた時に即時にダイアログを表示すると例外が発生してしまう
     * のを防ぐためのハンドラー.
     * </p>
     */
    private PauseHandlerImpl mPauseHandler;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_dconnect_manager);
        addPreferencesFromResource(R.xml.settings_dconnect_device_plugin);
        addPreferencesFromResource(R.xml.settings_dconnect_settings);
        addPreferencesFromResource(R.xml.settings_dconnect_security);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPreferencesFromResource(R.xml.settings_power_saving_doze_mode);
        } else {
            addPreferencesFromResource(R.xml.settings_power_saving);
        }
        addPreferencesFromResource(R.xml.settings_web_server);
        addPreferencesFromResource(R.xml.settings_dconnect_about);
        setHasOptionsMenu(true);

        // オープソースのリストを準備
        mOpenSourceList = new ArrayList<>();
        mOpenSourceList.add(OpenSourceLicenseFragment.createOpenSourceSoftware(
                "android-support-v4.jar", R.raw.andorid_support_v4));
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
        // サーバーのON/OFF
        boolean power = sp.getBoolean(getString(R.string.key_settings_dconn_server_on_off), false);

        // キーワード
        String keyword = sp.getString(getString(R.string.key_settings_dconn_keyword), DConnectSettings.DEFAULT_KEYWORD);
        if (keyword.length() <= 0) {
            keyword = DConnectUtil.createKeyword();
        }

        // ドキュメントルート
        String docRootPath = sp.getString(getString(R.string.key_settings_web_server_document_root_path), null);
        if (docRootPath == null || docRootPath.length() <= 0) {
            File file = new File(Environment.getExternalStorageDirectory(), getActivity().getPackageName());
            docRootPath = file.getPath();
        }

        // Managerの名前
        String name = sp.getString(getString(R.string.key_settings_dconn_name), null);
        if (name == null || name.length() <= 0) {
            name = DConnectUtil.createName();
        }

        // ManagerのUUID
        String uuid = sp.getString(getString(R.string.key_settings_dconn_uuid), null);
        if (uuid == null || uuid.length() <= 0) {
            uuid = DConnectUtil.createUuid();
        }


        EditTextPreference editKeywordPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_keyword));
        editKeywordPreferences.setSummary(keyword);
        editKeywordPreferences.setDefaultValue(keyword);
        editKeywordPreferences.setText(keyword);
        editKeywordPreferences.shouldCommit();


        EditTextPreference editNamePreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_name));
        editNamePreferences.setSummary(name);
        editNamePreferences.setDefaultValue(name);
        editNamePreferences.setText(name);
        editNamePreferences.shouldCommit();


        PreferenceScreen editUuidPreferences = (PreferenceScreen) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_uuid));
        editUuidPreferences.setSummary(uuid);
        editUuidPreferences.setDefaultValue(uuid);
        editUuidPreferences.shouldCommit();

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
        mCheckBoxExternalIpPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_allow_external_ip));

        // 外部起動/終了設定のON/OFF
        mCheckBoxExternalStartAndStartPreferences = (CheckBoxPreference) getPreferenceScreen()
            .findPreference(getString(R.string.key_settings_dconn_allow_external_start_and_stop));

        // Origin不要フラグ設定のON/OFF
        mCheckBoxRequireOriginPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_require_origin));

        // Originブロック設定のON/OFF
        mCheckBoxOriginBlockingPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_whitelist_origin_blocking));

        // ポート監視設定のON/OFF
        mObserverPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_observer_on_off));

        // Manager名の表示オンオフのチェックボックス.
        mCheckBoxManagerNameVisiblePreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_availability_visible_name));

        // アクセスログ設定のON/OFF
        mCheckBoxAccessLogPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_accesslog));

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

        setUIEnabled(power);

        mPauseHandler = new PauseHandlerImpl();
    }

    @Override
    public void onPause() {
        mPauseHandler.pause();
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

        // サービスとの接続完了まで操作無効にする
        getPreferenceScreen().setEnabled(false);

        // サービスとの接続
        bindDConnectService();
        bindDConnectWebService();

        // Dozeモード
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CheckBoxPreference dozeModePreference = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference(getString(R.string.key_settings_doze_mode));
            if (dozeModePreference != null) {
                dozeModePreference.setChecked(!DConnectUtil.isDozeMode(getActivity()));
                if (DConnectUtil.isDozeMode(getActivity())) {
                    dozeModePreference.setSummary(R.string.activity_settings_doze_mode_summary_off);
                } else {
                    dozeModePreference.setSummary(R.string.activity_settings_doze_mode_summary_on);
                }
                dozeModePreference.setOnPreferenceChangeListener(this);
            }
        }

        // WakeLock
        CheckBoxPreference wakeLockPreference = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_wake_lock));
        if (wakeLockPreference != null) {
            if (wakeLockPreference.isChecked()) {
                wakeLockPreference.setSummary(R.string.activity_settings_wake_lock_summary_on);
            } else {
                wakeLockPreference.setSummary(R.string.activity_settings_wake_lock_summary_off);
            }
            wakeLockPreference.setOnPreferenceChangeListener(this);
        }

        mPauseHandler.setFragment(this);
        mPauseHandler.resume();
    }

    @Override
    public void onDestroy() {
        // メモリリークしないようにフラグメントを削除しておく
        mPauseHandler.setFragment(null);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            } else if (getString(R.string.key_settings_dconn_availability_visible_name).equals(key)) {
                switchVisibleManagerName((Boolean) newValue);
            } else if (getString(R.string.key_settings_doze_mode).equals(key)) {
                switchDozeMode((Boolean) newValue);
            } else if (getString(R.string.key_settings_wake_lock).equals(key)) {
                switchWakeLock((Boolean) newValue);
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
        } else if (getString(R.string.key_settings_export_server_certificate).equals(preference.getKey())) {
            ExportCertificateDialogFragment newFragment = new ExportCertificateDialogFragment();
            newFragment.show(((SettingActivity) getActivity()).getSupportFragmentManager(),null);
        } else if (getString(R.string.key_settings_install_server_certificate).equals(preference.getKey())) {
            mDConnectService.installRootCertificate();
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
            startWebServer();
        } else if (TAG_REQUIRE_ORIGIN.equals(tag)) {
            mCheckBoxRequireOriginPreferences.setChecked(true);
        } else if (TAG_AVAILABILITY.equals(tag)) {
            mCheckBoxManagerNameVisiblePreferences.setChecked(true);
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
            setWebServerSwitchUI(false);
        } else if (TAG_REQUIRE_ORIGIN.equals(tag)) {
            mCheckBoxOauthPreferences.setChecked(false);
            mCheckBoxOriginBlockingPreferences.setChecked(false);
        } else if (TAG_AVAILABILITY.equals(tag)) {
            mCheckBoxManagerNameVisiblePreferences.setChecked(false);
        }
    }

    /**
     * WebサーバのトグルスイッチのOn/Offを切り替えます.
     *
     * @param checked trueの場合はON、それ以外の場合はOff
     */
    private void setWebServerSwitchUI(final boolean checked) {
        SwitchPreference webPreferences = (SwitchPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_web_server_on_off));
        webPreferences.setChecked(checked);
    }

    /**
     * DConnectServiceにバインドを行う.
     */
    private void bindDConnectService() {
        Intent intent = new Intent(getActivity(), DConnectService.class);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * DConnectWebServiceにバインドを行う.
     */
    private void bindDConnectWebService() {
        Intent intent = new Intent(getActivity(), DConnectWebService.class);
        getActivity().bindService(intent, mWebServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * DConnectサーバの有効・無効を設定する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchDConnectServer(final boolean checked) {
        setUIEnabled(!checked);
        if (checked) {

            Intent intent = new Intent();
            intent.setClass(getActivity(), DConnectService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().startForegroundService(intent);
            } else {
                getActivity().startService(intent);
            }

            mDConnectService.startInternal();
        } else {
            mDConnectService.stopInternal();

            Intent intent = new Intent();
            intent.setClass(getActivity(), DConnectService.class);
            getActivity().stopService(intent);
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
            mWebService.stopWebServer();
            setWebUIEnabled(true);

            Intent intent = new Intent();
            intent.setClass(getActivity(), DConnectWebService.class);
            getActivity().stopService(intent);
        }
    }

    private void startWebServerInternal() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), DConnectWebService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().startService(intent);
        }
        mWebService.startWebServer();
    }

    /**
     * Webサーバを起動します.
     */
    private void startWebServer() {
        if (DConnectUtil.isPermission(getActivity().getApplicationContext())) {
            startWebServerInternal();
            setWebUIEnabled(false);
        } else {
            DConnectUtil.requestPermission(getActivity().getApplicationContext(),
                    mPauseHandler,
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            startWebServerInternal();
                            setWebUIEnabled(false);
                        }
                        @Override
                        public void onFail(@NonNull final String deniedPermission) {
                            setWebServerSwitchUI(false);
                            mPauseHandler.sendMessage(mPauseHandler.obtainMessage(MSG_SHOW_ERROR_DIALOG));
                        }
                    });
        }
    }

    /**
     * Webサーバの起動に失敗したことを通知するダイアログを表示します.
     */
    private void showErrorWebServer() {
        String title = getString(R.string.activity_settings_web_server_warning_title);
        String message = getString(R.string.activity_settings_web_server_error_message);
        String positive = getString(R.string.activity_settings_web_server_warning_positive);
        AlertDialogFragment dialog = AlertDialogFragment.create(TAG_ERROR_WEB_SERVER,
                title, message, positive);
        dialog.show(getFragmentManager(), TAG_ERROR_WEB_SERVER);
    }

    /**
     * 監視機能の有効・無効を設定する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchObserver(final boolean checked) {
        Intent intent = new Intent();
        DConnectSettings settings = ((DConnectApplication) getActivity().getApplication()).getSettings();
        intent.setClass(getActivity(), ObserverReceiver.class);
        if (checked) {
            intent.setAction(DConnectObservationService.ACTION_START);
            intent.putExtra(DConnectObservationService.PARAM_PORT, settings.getPort());
            intent.putExtra(DConnectObservationService.PARAM_OBSERVATION_INTERVAL, settings.getObservationInterval());
            intent.putExtra(DConnectObservationService.PARAM_RESULT_RECEIVER,
                    new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode != Activity.RESULT_OK) {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    mObserverPreferences.setChecked(false);
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
        BaseSettingActivity activity = (BaseSettingActivity) getActivity();
        if (activity == null) {
            return;
        }

        DConnectService service = activity.getManagerService();
        if (service != null) {
            service.setEnableKeepAlive(checked);
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
     * AvailabilityにManagerの名前を表示することを警告するダイアログを表示する.
     * @param checked trueの場合は有効、falseの場合は無効
     */
    private void switchVisibleManagerName(final boolean checked) {
        if (checked) {
            String title = getString(R.string.activity_settings_warning);
            String message = getString(R.string.activity_settings_availability_warning_message);
            String positive = getString(R.string.activity_settings_yes);
            String negative = getString(R.string.activity_settings_no);
            AlertDialogFragment dialog = AlertDialogFragment.create(TAG_AVAILABILITY,
                    title, message, positive, negative);
            dialog.show(getFragmentManager(), TAG_AVAILABILITY);
        }
    }
    /**
     * Dozeモードの切り替えを行います.
     * @param checked 有効にするの場合はtrue、無効にする場合はfalse
     */
    private void switchDozeMode(final boolean checked) {
        if (!checked) {
            DConnectUtil.startConfirmIgnoreDozeMode(getActivity());
        } else {
            DConnectUtil.startDozeModeSettingActivity(getActivity());
        }
    }

    /**
     * WakeLockの切り替えを行います.
     * @param checked 有効にするの場合はtrue、無効にする場合はfalse
     */
    private void switchWakeLock(final boolean checked) {
        CheckBoxPreference pref = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_wake_lock));
        if (pref != null) {
            if (checked) {
                pref.setSummary(R.string.activity_settings_wake_lock_summary_on);
                if (mDConnectService != null) {
                    mDConnectService.acquireWakeLock();
                }
            } else {
                pref.setSummary(R.string.activity_settings_wake_lock_summary_off);
                if (mDConnectService != null) {
                    mDConnectService.releaseWakeLock();
                }
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
        mCheckBoxExternalIpPreferences.setEnabled(enabled);
        mCheckBoxRequireOriginPreferences.setEnabled(enabled);
        mCheckBoxOriginBlockingPreferences.setEnabled(enabled);
        mCheckBoxAccessLogPreferences.setEnabled(enabled);
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
        RestartingDialogFragment.show((BaseSettingActivity) getActivity());
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
    private DConnectService mDConnectService;

    /**
     * DConnectServiceと接続するためのクラス.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mDConnectService = ((DConnectService.LocalBinder) service).getDConnectService();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    boolean running = mDConnectService.isRunning();
                    setUIEnabled(!running);

                    SwitchPreference serverPreferences = (SwitchPreference) getPreferenceScreen()
                            .findPreference(getString(R.string.key_settings_dconn_server_on_off));
                    serverPreferences.setChecked(running);

                    checkServiceConnections();
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
    private DConnectWebService mWebService;

    /**
     * DConnectWebServiceと接続するためのクラス.
     */
    private final ServiceConnection mWebServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mWebService = ((DConnectWebService.LocalBinder) service).getDConnectWebService();
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean running = mWebService.isRunning();
                        setWebUIEnabled(!running);
                        setWebServerSwitchUI(running);
                        checkServiceConnections();
                    }
                });
            }
        }
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mWebService = null;
        }
    };

    /**
     * Device Connect Managerのサービスへのbind状態を確認して、Preferenceを有効にします。
     */
    private synchronized void checkServiceConnections() {
        if (mDConnectService != null && mWebService != null) {
            enablePreference();
        }
    }

    /**
     * Preferenceの設定を有効にします.
     */
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
        EditTextPreference editNamePreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_name));
        editNamePreferences.setOnPreferenceChangeListener(this);
        mEditPortPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxOauthPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxExternalIpPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxExternalStartAndStartPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxRequireOriginPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxOriginBlockingPreferences.setOnPreferenceChangeListener(this);
        mCheckBoxManagerNameVisiblePreferences.setOnPreferenceChangeListener(this);
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

    /**
     * 一時中断用ハンドラ.
     */
    private static class PauseHandlerImpl extends PauseHandler {
        /**
         * 処理を行うフラグメント.
         */
        private SettingsFragment mFragment;

        /**
         * フラグメントを設定します.
         * @param fragment フラグメント
         */
        public void setFragment(final SettingsFragment fragment) {
            mFragment = fragment;
        }

        @Override
        protected boolean storeMessage(final Message message) {
            return true;
        }

        @Override
        protected void processMessage(final Message message) {
            switch (message.what) {
                case MSG_SHOW_ERROR_DIALOG:
                    if (mFragment != null) {
                        mFragment.showErrorWebServer();
                    }
                    break;
            }
        }
    }
}
