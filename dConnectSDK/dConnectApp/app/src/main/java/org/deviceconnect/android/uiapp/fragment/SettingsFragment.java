/*
 SettingsFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;

import org.deviceconnect.android.uiapp.DConnectApplication;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.utils.Settings;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 設定画面フラグメント.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private DConnectSDK mDConnectSDK;
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mFuture;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        PreferenceScreen versionPreferences = (PreferenceScreen)
                getPreferenceScreen().findPreference(
                        getString(R.string.key_settings_about_appinfo));
        try {
            versionPreferences.setSummary((getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        CheckBoxPreference checkBoxSslPreferences = (CheckBoxPreference)
                getPreferenceScreen().findPreference(
                        getString(R.string.key_settings_dconn_ssl));
        checkBoxSslPreferences.setOnPreferenceChangeListener(this);

        EditTextPreference editHostPreferences = (EditTextPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_dconn_host));
        editHostPreferences.setOnPreferenceChangeListener(this);
        editHostPreferences.setSummary(editHostPreferences.getText());

        EditTextPreference editPortPreferences = (EditTextPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_dconn_port));
        editPortPreferences.setOnPreferenceChangeListener(this);
        editPortPreferences.setSummary(editPortPreferences.getText());

        ListPreference listPreference = (ListPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_dconn_sdk));
        listPreference.setSummary(Settings.getInstance().getSDKType());
        listPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDConnectSDK = ((DConnectApplication) getActivity().getApplication()).getDConnectSK();
        startMonitoring();
    }

    @Override
    public void onPause() {
        mDConnectSDK = null;
        stopMonitoring();
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if (getString(R.string.key_settings_dconn_launch).equals(preference.getKey())) {
            if (mDConnectSDK == null) {
                return false;
            }

            Log.e("ABC", "startManager aaaaa:");
            Boolean result = (Boolean) newValue;
            if (result) {
                Log.e("ABC", "startManager aaaaa bbb:");
                mDConnectSDK.startManager(getActivity());
            } else {
                Log.e("ABC", "startManager aaaaa ccc:");
                mDConnectSDK.stopManager(getActivity());
            }
            return false;
        } else if (getString(R.string.key_settings_dconn_sdk).equals(preference.getKey())) {
            if (newValue != null) {
                preference.setSummary((CharSequence) newValue);
                DConnectApplication app = (DConnectApplication) getActivity().getApplication();
                app.initDConnectSDK((String) newValue);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference) {
        if (getString(R.string.key_settings_about_oss).equals(preference.getKey())) {
//            mOssFragment.show(getFragmentManager(), null);
        } else if (getString(R.string.key_settings_about_privacypolicy).equals(preference.getKey())) {
            Bundle policyArgs = new Bundle();
            policyArgs.putInt(Intent.EXTRA_TITLE, R.string.privacy_policy);
            policyArgs.putInt(Intent.EXTRA_TEXT, R.raw.privacypolicy);
            TextDialogFragment fragment = new TextDialogFragment();
            fragment.setArguments(policyArgs);
            fragment.show(getFragmentManager(), null);
        } else if (getString(R.string.key_settings_about_tos).equals(preference.getKey())) {
            Bundle tosArgs = new Bundle();
            tosArgs.putInt(Intent.EXTRA_TITLE, R.string.terms_of_service);
            tosArgs.putInt(Intent.EXTRA_TEXT, R.raw.termsofservice);
            TextDialogFragment fragment = new TextDialogFragment();
            fragment.setArguments(tosArgs);
            fragment.show(getFragmentManager(), null);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void startMonitoring() {
        mFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final boolean result = checkManager();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            setManagerLaunch(result);
                        }
                    }
                });
            }
        }, 1, 3, TimeUnit.SECONDS);
    }

    private void stopMonitoring() {
        if (mFuture != null) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    private void setManagerLaunch(final boolean flag) {
        SwitchPreference sw = (SwitchPreference)
                getPreferenceScreen().findPreference(getString(R.string.key_settings_dconn_launch));
        sw.setOnPreferenceChangeListener(null);
        sw.setChecked(flag);
        sw.setOnPreferenceChangeListener(this);
    }

    private boolean checkManager() {
        if (mDConnectSDK == null) {
            return false;
        }

        DConnectResponseMessage response = mDConnectSDK.availability();
        return response != null && response.getResult() == DConnectMessage.RESULT_OK;
    }
}
