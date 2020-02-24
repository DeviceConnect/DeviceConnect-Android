package org.deviceconnect.android.rtspserver;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Size;
import android.view.MenuItem;

import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2WrapperManager;
import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;

import java.net.InetAddress;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class RtspPreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

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
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            EditTextPreference url = findPreference(getString(R.string.key_server_url));
            if (url != null) {
                url.setText(getServerUrl());
            }

            ListPreference videoResolution = findPreference(getString(R.string.key_video_resolution));
            if (videoResolution != null) {
                String[] entries = createCameraResolutions(getContext());
                videoResolution.setEntries(entries);
                videoResolution.setEntryValues(entries);
            }

            setInputTypeNumber(R.string.key_server_port);
            setInputTypeNumber(R.string.key_video_bit_rate);
            setInputTypeNumber(R.string.key_video_frame_rate);
            setInputTypeNumber(R.string.key_video_iframe_interval);
            setInputTypeNumber(R.string.key_audio_bit_rate);
            setInputTypeNumber(R.string.key_audio_sampling_rate);
        }

        private String[] createCameraResolutions(Context context) {
            Camera2Wrapper camera = Camera2WrapperManager.createCamera(context);
            List<Size> sizes = camera.getSettings().getSupportedPreviewSizes();
            String[] resolutions = new String[sizes.size()];
            for (int i = 0; i < resolutions.length; i++) {
                Size size = sizes.get(i);
                resolutions[i] = size.getWidth() + "x" + size.getHeight();
            }
            return resolutions;
        }

        private String getServerUrl() {
            IpAddressManager ipAddressManager = new IpAddressManager();
            ipAddressManager.storeIPAddress();

            InetAddress ipAddress = ipAddressManager.getWifiIPv4Address();
            if (ipAddress == null) {
                ipAddress = ipAddressManager.getIPv4Address();
            }

            if (ipAddress != null) {
                return "rtsp://" + ipAddress.getHostAddress() + ":" + getStringFromEditText(R.string.key_server_port, R.string.settings_server_port);
            }

            return "null";
        }

        private String getStringFromEditText(int resId, int defaultId) {
            EditTextPreference editTextPreference = findPreference(getString(resId));
            if (editTextPreference != null) {
                return editTextPreference.getText();
            }
            return getString(defaultId);
        }

        private void setInputTypeNumber(int resId) {
            EditTextPreference editTextPreference = findPreference(getString(resId));
            if (editTextPreference != null) {
                editTextPreference.setOnBindEditTextListener((editText) ->
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
                editTextPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            }
        }

        private Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
            String key = preference.getKey();

            if (key.equals(getString(R.string.key_server_port))) {

            }

            return true;
        };
    }
}
