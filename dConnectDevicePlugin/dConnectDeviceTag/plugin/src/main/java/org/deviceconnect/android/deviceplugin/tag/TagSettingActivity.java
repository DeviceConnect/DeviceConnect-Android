/*
 TagSetting.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.tag.ui.SwitchPreference;

/**
 * プラグインの設定画面を表示する Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class TagSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 設定用のフラグメント.
     */
    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(TagSetting.FILE_NAME);

            addPreferencesFromResource(R.xml.activity_setting_pref_oauth);

            SwitchPreference serverPreferences = (SwitchPreference) getPreferenceScreen()
                    .findPreference(getString(R.string.key_settings_ouath_on_off));
            serverPreferences.setOnPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public boolean onPreferenceChange(final Preference preference, final Object newValue) {
            final String key = preference.getKey();
            if (preference instanceof SwitchPreference) {
                if (getString(R.string.key_settings_ouath_on_off).equals(key)) {
                }
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(newValue.toString());
            }
            return true;
        }

        /**
         * EditTextPreference の summary に値を表示するように設定します.
         *
         * @param resId EditTextPreference キーのリソースID
         */
        private void setEditTextPreferenceSummary(int resId) {
            setEditTextPreferenceSummary(getString(resId));
        }

        /**
         * EditTextPreference の summary に値を表示するように設定します.
         *
         * @param key EditTextPreference のキー
         */
        private void setEditTextPreferenceSummary(String key) {
            EditTextPreference pref = (EditTextPreference) getPreferenceScreen().findPreference(key);
            pref.setSummary(pref.getText());
            pref.setOnPreferenceChangeListener(this);
        }
    }
}