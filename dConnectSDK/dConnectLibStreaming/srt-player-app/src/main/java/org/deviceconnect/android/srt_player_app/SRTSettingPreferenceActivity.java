package org.deviceconnect.android.srt_player_app;

import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SRTSettingPreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(R.string.settings_name);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SRTPreferenceFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SRTPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
            setPreferencesFromResource(R.xml.srt_settings, rootKey);

            setInputTypeNumber("settings_srt_rcvlatency");
            setInputTypeNumber("settings_srt_conntimeo");
            setInputTypeNumber("settings_srt_peeridletimeo");
        }

        private void setInputTypeNumber(String key) {
            EditTextPreference editTextPreference = findPreference(key);
            if (editTextPreference != null) {
                editTextPreference.setOnBindEditTextListener((editText) ->
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
                editTextPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            }
        }

        private Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
            String key = preference.getKey();
            return true;
        };
    }
}
