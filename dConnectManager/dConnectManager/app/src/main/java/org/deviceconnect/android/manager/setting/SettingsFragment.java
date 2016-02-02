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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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

import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.IDConnectService;
import org.deviceconnect.android.manager.IDConnectWebService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.setting.OpenSourceLicenseFragment.OpenSourceSoftware;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.observer.DConnectObservationService;
import org.deviceconnect.android.observer.receiver.ObserverReceiver;

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

        // SharedPreferenceをマルチプロセスでも動作する設定にする
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

        // オープソースのリストを準備
        mOpenSourceList = new ArrayList<OpenSourceSoftware>();
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

        editKeywordPreferences.setOnPreferenceChangeListener(this);
        editKeywordPreferences.setSummary(keyword);
        editKeywordPreferences.setDefaultValue(keyword);
        editKeywordPreferences.setText(keyword);
        editKeywordPreferences.shouldCommit();

        // SSLのON/OFF
        mCheckBoxSslPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_ssl));
        mCheckBoxSslPreferences.setOnPreferenceChangeListener(this);

        // ホスト名設定
        EditTextPreference editHostPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_host));
        editHostPreferences.setOnPreferenceChangeListener(this);
        editHostPreferences.setSummary(editHostPreferences.getText());

        // ポート番号設定
        mEditPortPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_port));
        mEditPortPreferences.setOnPreferenceChangeListener(this);
        mEditPortPreferences.setSummary(mEditPortPreferences.getText());

        // Local OAuthのON/OFF
        mCheckBoxOauthPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_local_oauth));
        mCheckBoxOauthPreferences.setOnPreferenceChangeListener(this);

        // グローバル設定のON/OFF
        mCheckBoxExternalPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_allow_external_ip));
        mCheckBoxExternalPreferences.setOnPreferenceChangeListener(this);

        // Origin不要フラグ設定のON/OFF
        mCheckBoxRequireOriginPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_require_origin));
        mCheckBoxRequireOriginPreferences.setOnPreferenceChangeListener(this);

        // Originブロック設定のON/OFF
        mCheckBoxOriginBlockingPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_whitelist_origin_blocking));
        mCheckBoxOriginBlockingPreferences.setOnPreferenceChangeListener(this);

        // ポート監視設定のON/OFF
        mObserverPreferences = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_observer_on_off));
        mObserverPreferences.setOnPreferenceChangeListener(this);

        // Webサーバ

        // ドキュメントルートパス
        EditTextPreference editDocPreferences = (EditTextPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_web_server_document_root_path));
        editDocPreferences.setSummary(docRootPath);

        EditTextPreference editWebHostPreferences = (EditTextPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_web_server_host));

        mWebPortPreferences = (EditTextPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_web_server_port));
        mWebPortPreferences.setOnPreferenceChangeListener(this);
        mWebPortPreferences.setSummary(mWebPortPreferences.getText());

        SwitchPreference serverPreferences = (SwitchPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_dconn_server_on_off));
        serverPreferences.setOnPreferenceChangeListener(this);

        SwitchPreference webPreferences = (SwitchPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_web_server_on_off));
        webPreferences.setOnPreferenceChangeListener(this);

        editHostPreferences.setEnabled(false);
        editDocPreferences.setEnabled(false);
        editWebHostPreferences.setEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showIPAddress();
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
                    Integer.parseInt(value);
                    preference.setSummary(value);
                } catch (NumberFormatException e) {
                    return true;
                }
            } else {
                preference.setSummary(newValue.toString());
            }
        } else if (preference instanceof SwitchPreference) {
            if (getString(R.string.key_settings_dconn_server_on_off).equals(key)) {
                boolean checked = ((Boolean) newValue).booleanValue();
                setEnabled(!checked);
                try {
                    // dConnectManagerのON/OFF
                    if (checked) {
                        mDConnectService.start();
                    } else {
                        mDConnectService.stop();
                    }
                } catch (RemoteException e) {
                }
            } else if (getString(R.string.key_settings_web_server_on_off).equals(key)) {
                boolean checked = ((Boolean) newValue).booleanValue();
                try {
                    setWebUIEnabled(!checked);
                    if (checked) {
                        mWebService.start();
                    } else {
                        mWebService.stop();
                    }
                } catch (RemoteException e) {
                }
            }
        } else if (preference instanceof CheckBoxPreference) {
            if (getString(R.string.key_settings_dconn_observer_on_off).equals(key)) {
                boolean checked = ((Boolean) newValue).booleanValue();
                // 監視サービスのON/OFF
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
            } else if (getString(R.string.key_settings_dconn_require_origin).equals(key)) {
                boolean checked = ((Boolean) newValue).booleanValue();
                if (!checked) {
                    List<String> settings = new ArrayList<String>();
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.activity_settings_warning));
                        String message = getString(R.string.activity_settings_warning_require_origin_disabled);
                        message = message.replace("%1", list);
                        builder.setMessage(message);
                        builder.setCancelable(false);
                        String yes = getString(R.string.activity_settings_yes);
                        String no = getString(R.string.activity_settings_no);
                        builder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                mCheckBoxOauthPreferences.setChecked(false);
                                mCheckBoxOriginBlockingPreferences.setChecked(false);
                            }
                        });
                        builder.setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                mCheckBoxRequireOriginPreferences.setChecked(true);
                            }
                        });
                        builder.create().show();
                    }
                }
            } else if (getString(R.string.key_settings_dconn_local_oauth).equals(key)
                    || getString(R.string.key_settings_dconn_whitelist_origin_blocking).equals(key)) {
                boolean checked = ((Boolean) newValue).booleanValue();
                boolean requiredOrigin = mCheckBoxRequireOriginPreferences.isChecked();
                if (checked && !requiredOrigin) {
                    StringBuilder list = new StringBuilder();
                    list.append(" - ");
                    list.append(getString(R.string.activity_settings_require_origin));
                    list.append("\n");

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.activity_settings_warning));
                    String message = getString(R.string.activity_settings_warning_require_origin_enabled);
                    message = message.replace("%1", list);
                    builder.setMessage(message);
                    builder.setCancelable(false);
                    String yes = getString(R.string.activity_settings_yes);
                    String no = getString(R.string.activity_settings_no);
                    builder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            mCheckBoxRequireOriginPreferences.setChecked(true);
                        }
                    });
                    builder.setNegativeButton(no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            ((CheckBoxPreference) preference).setChecked(false);
                        }
                    });
                    builder.create().show();
                }
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
     * UIの有効・無効を設定する.
     * @param enabled trueの場合は有効、falseの場合は無効
     */
    private void setEnabled(final boolean enabled) {
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
        dialog.show(getFragmentManager(), "dialog");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DevicePluginManager mgr = new DevicePluginManager(getActivity(), null);
                mgr.createDevicePluginList();
                List<DevicePlugin> plugins = mgr.getDevicePlugins();
                for (DevicePlugin plugin : plugins) {
                    if (plugin.getStartServiceClassName() != null) {
                        restartDevicePlugin(plugin);
                    }
                }
                dialog.dismiss();
            }
        }).start();

    }

    /**
     * Start a device plugin.
     * 
     * @param plugin device plugin to be started
     */
    private void restartDevicePlugin(final DevicePlugin plugin) {
        Intent service = new Intent();
        service.setClassName(plugin.getPackageName(), plugin.getStartServiceClassName());
        getActivity().startService(service);
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean running = mDConnectService.isRunning();
                        setEnabled(!running);
                        SwitchPreference serverPreferences = (SwitchPreference) getPreferenceScreen()
                                .findPreference(getString(R.string.key_settings_dconn_server_on_off));
                        serverPreferences.setChecked(running);
                    } catch (RemoteException e) {
                        return;
                    }
                }
            });
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean running = mWebService.isRunning();
                        setWebUIEnabled(!running);
                        SwitchPreference webPreferences = (SwitchPreference) getPreferenceScreen()
                                .findPreference(getString(R.string.key_settings_web_server_on_off));
                        webPreferences.setChecked(running);
                    } catch (RemoteException e) {
                        return;
                    }
                }
            });
        }
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mWebService = null;
        }
    };
}
