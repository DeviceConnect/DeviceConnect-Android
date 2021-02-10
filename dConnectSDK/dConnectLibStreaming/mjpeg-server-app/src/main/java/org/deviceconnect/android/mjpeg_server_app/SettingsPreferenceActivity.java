package org.deviceconnect.android.mjpeg_server_app;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.text.InputType;
import android.util.Size;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingsPreferenceActivity extends AppCompatActivity {

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

        private static final String PREF_CAMERA_PREVIEW_SIZE_BASE = "camera_preview_size_";

        private Settings mSettings;

        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
            setPreferencesFromResource(R.xml.mjpeg_server_settings, rootKey);

            Context context = getContext();
            if (context != null) {
                mSettings = new Settings(context);
                createMJPEGServerPreference();
                createCameraSettingsPreference(context);
                createEncoderFrameRatePreference();
                createEncoderQualityPreference();
            }

            setInputTypeNumber("encoder_frame_rate");
            setInputTypeNumber("encoder_quality");
        }

        private String getIpAddress() {
            IpAddressManager addressManager = new IpAddressManager();
            addressManager.storeIPAddress();
            InetAddress address = addressManager.getIPv4Address();
            if (address == null) {
                address = addressManager.getWifiIPv4Address();
            }
            if (address == null) {
                address = addressManager.getVpnIPv4Address();
            }
            if (address == null) {
                address = addressManager.getBluetoothIPv4Address();
            }
            if (address != null) {
                return address.getHostAddress();
            }
            return null;
        }

        private void setInputTypeNumber(String key) {
            EditTextPreference editTextPreference = findPreference(key);
            if (editTextPreference != null) {
                editTextPreference.setOnBindEditTextListener((editText) ->
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
                editTextPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            }
        }

        private void createMJPEGServerPreference() {
            EditTextPreference pref = findPreference("server_url");
            if (pref != null) {
                pref.setText("http://" + getIpAddress() + ":12345/mjpeg");
            }
        }

        private void createCameraSettingsPreference(final Context context) {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            ListPreference facingPref = findPreference("camera_facing");
            if (facingPref != null) {
                if (cameraManager != null) {
                    final int facing;
                    String value = facingPref.getValue();
                    if (value == null) {
                        facing = Settings.DEFAULT_CAMERA_FACING;
                        facingPref.setValueIndex(Settings.DEFAULT_CAMERA_FACING);
                    } else {
                        facing = Integer.parseInt(value);
                    }

                    facingPref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                        if (newValue instanceof String) {
                            int newFacing = Integer.parseInt((String) newValue);
                            createCameraPreviewPreference(cameraManager, newFacing);
                        }
                        return true;
                    });

                    createCameraPreviewPreference(cameraManager, facing);
                } else {
                    facingPref.setEnabled(false);
                }
            }
        }

        private void createCameraPreviewPreference(final CameraManager cameraManager, final int facing) {
            // 一旦すべて隠す
            for (int i = 0; i <= 1; i++) {
                ListPreference previewSizePref = findPreference(PREF_CAMERA_PREVIEW_SIZE_BASE + i);
                if (previewSizePref != null) {
                    previewSizePref.setVisible(false);
                }
            }

            ListPreference previewSizePref = findPreference(PREF_CAMERA_PREVIEW_SIZE_BASE + facing);
            if (previewSizePref != null) {
                String value = previewSizePref.getValue();
                if (value == null) {
                    Size preview = mSettings.getCameraPreviewSize(facing);
                    previewSizePref.setValue(getPreviewSizeSettingValue(preview));
                }

                previewSizePref.setVisible(true);
                if (cameraManager != null) {
                    try {
                        String cameraId = getCameraId(cameraManager, facing);
                        List<Size> previewSizes = getSupportedPreviewSizes(cameraManager, cameraId);
                        List<String> entryValues = new ArrayList<>();
                        for (Size preview : previewSizes) {
                            entryValues.add(getPreviewSizeSettingValue(preview));
                        }

                        previewSizePref.setEntries(entryValues.toArray(new String[0]));
                        previewSizePref.setEntryValues(entryValues.toArray(new String[0]));
                    } catch (CameraAccessException ignored) {}
                } else {
                    previewSizePref.setEnabled(false);
                }
            }
        }

        private String getPreviewSizeSettingValue(final Size previewSize) {
            return previewSize.getWidth() + " x " + previewSize.getHeight();
        }

        private void createEncoderFrameRatePreference() {
            EditTextPreference pref = findPreference("encoder_frame_rate");
            if (pref != null) {
                pref.setSummaryProvider((Preference preference) ->
                        pref.getText() + " fps"
                );
                // 0 以上の整数値以外の入力を禁止する
                pref.setOnBindEditTextListener((@NonNull EditText editText) ->
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER));
                String value = pref.getText();
                if (value == null) {
                    pref.setText(Settings.DEFAULT_ENCODER_FPS);
                }
            }
        }

        private void createEncoderQualityPreference() {
            EditTextPreference pref = findPreference("encoder_quality");
            if (pref != null) {
                pref.setSummaryProvider((Preference preference) ->
                        pref.getText() + ""
                );
                // 0 以上の実数値以外の入力を禁止する
                pref.setOnBindEditTextListener((@NonNull EditText editText) ->
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER
                                | InputType.TYPE_NUMBER_FLAG_DECIMAL));
                String value = pref.getText();
                if (value == null) {
                    pref.setText(Settings.DEFAULT_ENCODER_QUALITY);
                }
            }
        }

        /**
         * 指定された facing に対応するカメラIDを取得します.
         * <p>
         * facing に対応したカメラが発見できない場合には null を返却します。
         * </p>
         * @param cameraManager カメラマネージャ
         * @param facing カメラの向き
         * @return カメラID
         * @throws CameraAccessException カメラの操作に失敗した場合に発生
         */
        private static String getCameraId(final CameraManager cameraManager, final int facing) throws CameraAccessException {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer supportFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (supportFacing != null && supportFacing == facing) {
                    return cameraId;
                }
            }
            return null;
        }

        /**
         * カメラID に対応したカメラデバイスがサポートしているプレビューサイズのリストを取得します.
         *
         * @param cameraManager カメラマネージャ
         * @param cameraId カメラID
         * @return サポートしているプレビューサイズのリスト
         */
        @NonNull
        private static List<Size> getSupportedPreviewSizes(final CameraManager cameraManager, final String cameraId) {
            List<Size> previewSizes = new ArrayList<>();
            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map != null) {
                    previewSizes = Arrays.asList(map.getOutputSizes(SurfaceTexture.class));
                    Collections.sort(previewSizes, SIZE_COMPARATOR);
                }
            } catch (CameraAccessException e) {
                // ignore.
            }
            return previewSizes;
        }

        /**
         * サイズの小さい方からソートを行うための比較演算子.
         */
        private static final Comparator<Size> SIZE_COMPARATOR = (lhs, rhs) -> {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        };

        private Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
            String key = preference.getKey();
            return true;
        };
    }
}
