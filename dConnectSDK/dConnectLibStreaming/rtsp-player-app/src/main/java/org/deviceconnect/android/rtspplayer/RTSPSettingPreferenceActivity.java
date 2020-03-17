package org.deviceconnect.android.rtspplayer;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class RTSPSettingPreferenceActivity extends AppCompatActivity {

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
                .replace(R.id.settings_container, new RtspPreferenceFragment())
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

    public static class RtspPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
            setPreferencesFromResource(R.xml.rtsp_settings, rootKey);
        }
    }
}
