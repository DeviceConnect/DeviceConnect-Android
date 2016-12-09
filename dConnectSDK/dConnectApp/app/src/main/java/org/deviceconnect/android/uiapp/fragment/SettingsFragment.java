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
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.deviceconnect.android.uiapp.R;

/**
 * 設定画面フラグメント.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

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
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference) {
        boolean result = super.onPreferenceTreeClick(preferenceScreen, preference);

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

        return result;
    }

}
