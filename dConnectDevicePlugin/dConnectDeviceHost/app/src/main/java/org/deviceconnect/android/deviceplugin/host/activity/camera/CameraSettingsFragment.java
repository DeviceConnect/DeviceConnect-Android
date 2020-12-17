package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.app.Activity;
import android.hardware.camera2.CameraMetadata;
import android.os.Bundle;
import android.text.InputType;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraSettingsFragment extends PreferenceFragmentCompat implements CameraActivity.OnHostDevicePluginListener {
    private HostMediaRecorderManager mMediaRecorderManager;
    private HostMediaRecorder mMediaRecorder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        HostDevicePlugin plugin = getHostDevicePlugin();

        mMediaRecorderManager = plugin.getHostMediaRecorderManager();
        mMediaRecorder = mMediaRecorderManager.getRecorder(getRecorderId());
        HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();

        getPreferenceManager().setSharedPreferencesName(mMediaRecorder.getId());

        setPreferencesFromResource(R.xml.settings_host_camera, rootKey);

        setPictureSizePreference(settings);
        setPreviewSizePreference(settings);
        setPreviewVideoEncoderPreference(settings);
        setPreviewWhiteBalancePreference(settings);

        setInputTypeNumber("preview_framerate");
        setInputTypeNumber("preview_bitrate");
        setInputTypeNumber("preview_i_frame_interval");
        setInputTypeNumber("encoder_intra_refresh");
        setInputTypeNumber("preview_audio_bitrate");
        setInputTypeNumber("preview_audio_channel");
        setInputTypeNumber("mjpeg_port");
        setInputTypeNumber("preview_quality");
        setInputTypeNumber("rtsp_port");
        setInputTypeNumber("srt_port");
    }

    @Override
    public void onBindService() {
    }

    @Override
    public void onUnbindService() {
    }

    /**
     * レコード ID を取得します.
     *
     * @return レコード ID
     */
    private String getRecorderId() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getString("recorder_id");
        }
        return null;
    }

    /**
     * 接続されている HostDevicePlugin のインスタンスを取得します.
     *
     * 接続されていない場合には null を返却します。
     *
     * @return HostDevicePlugin のインスタンス
     */
    public HostDevicePlugin getHostDevicePlugin() {
        Activity activity = getActivity();
        if (activity instanceof CameraActivity) {
            return ((CameraActivity) activity).getHostDevicePlugin();
        }
        return null;
    }

    /**
     * 指定されたキーに対応する入力フォームを数値のみ入力可能に設定します.
     *
     * @param key キー
     */
    private void setInputTypeNumber(String key) {
        EditTextPreference editTextPreference = findPreference(key);
        if (editTextPreference != null) {
            editTextPreference.setOnBindEditTextListener((editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
            editTextPreference.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        }
    }

    /**
     * 静止画の解像度の Preference を作成します.
     *
     * @param settings レコーダの設定
     */
    private void setPictureSizePreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("camera_picture_size");
        if (pref != null) {
            String value = pref.getValue();
            if (value == null) {
                Size preview = settings.getPreviewSize();
                pref.setValue(getValueFromSize(preview));
            }
            pref.setVisible(true);

            List<Size> previewSizes = getSupportedPictureSizes(settings);
            List<String> entryValues = new ArrayList<>();
            for (Size preview : previewSizes) {
                entryValues.add(getValueFromSize(preview));
            }

            pref.setEntries(entryValues.toArray(new String[0]));
            pref.setEntryValues(entryValues.toArray(new String[0]));
            pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        }
    }

    /**
     * プレビューの解像度 Preference を作成します.
     *
     * @param settings レコーダの設定
     */
    private void setPreviewSizePreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("camera_preview_size");
        if (pref != null) {
            String value = pref.getValue();
            if (value == null) {
                Size preview = settings.getPreviewSize();
                pref.setValue(getValueFromSize(preview));
            }
            pref.setVisible(true);

            List<Size> previewSizes = getSupportedPreviewSizes(settings);
            List<String> entryValues = new ArrayList<>();
            for (Size preview : previewSizes) {
                entryValues.add(getValueFromSize(preview));
            }

            pref.setEntries(entryValues.toArray(new String[0]));
            pref.setEntryValues(entryValues.toArray(new String[0]));
            pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        }
    }

    private void setPreviewVideoEncoderPreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_mime_type");
        if (pref != null) {
            pref.setValue(settings.getPreviewMimeType());
        }
    }

    private void setPreviewWhiteBalancePreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_white_balance");
        if (pref != null) {
            List<String> entryNames = new ArrayList<>();
            List<String> entryValues = new ArrayList<>();
            for (Integer mode : settings.getSupportedWhiteBalances()) {
                switch (mode) {
                    case CameraMetadata.CONTROL_AWB_MODE_OFF:
                        entryNames.add("OFF");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_AUTO:
                        entryNames.add("AUTO");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT:
                        entryNames.add("INCANDESCENT");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT:
                        entryNames.add("FLUORESCENT");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT:
                        entryNames.add("WARM_FLUORESCENT");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT:
                        entryNames.add("DAYLIGHT");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT:
                        entryNames.add("CLOUDY_DAYLIGHT");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_TWILIGHT:
                        entryNames.add("TWILIGHT");
                        break;
                    case CameraMetadata.CONTROL_AWB_MODE_SHADE:
                        entryNames.add("SHADE");
                        break;
                }
                entryValues.add(String.valueOf(mode));
            }
            pref.setEntries(entryNames.toArray(new String[0]));
            pref.setEntryValues(entryValues.toArray(new String[0]));
            pref.setValue(String.valueOf(settings.getPreviewWhiteBalance()));
        }
    }

    /**
     * サイズの小さい方からソートを行うための比較演算子.
     */
    private static final Comparator<Size> SIZE_COMPARATOR = (lhs, rhs) -> {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    };

    /**
     * カメラID に対応したカメラデバイスがサポートしている写真サイズのリストを取得します.
     *
     * @param settings レコーダ
     * @return サポートしているプレビューサイズのリスト
     */
    @NonNull
    private static List<Size> getSupportedPictureSizes(HostMediaRecorder.Settings settings) {
        List<Size> previewSizes = new ArrayList<>();
        if (settings != null) {
            previewSizes.addAll(settings.getSupportedPictureSizes());
            Collections.sort(previewSizes, SIZE_COMPARATOR);
        }
        return previewSizes;
    }

    /**
     * カメラID に対応したカメラデバイスがサポートしているプレビューサイズのリストを取得します.
     *
     * @param settings レコーダ
     * @return サポートしているプレビューサイズのリスト
     */
    @NonNull
    private static List<Size> getSupportedPreviewSizes(HostMediaRecorder.Settings settings) {
        List<Size> previewSizes = new ArrayList<>();
        if (settings != null) {
            previewSizes.addAll(settings.getSupportedPreviewSizes());
            Collections.sort(previewSizes, SIZE_COMPARATOR);
        }
        return previewSizes;
    }

    /**
     * プレビューのサイズを文字列に変換します.
     *
     * @param previewSize プレビューサイズ
     * @return 文字列
     */
    private String getValueFromSize(Size previewSize) {
        return previewSize.getWidth() + " x " + previewSize.getHeight();
    }

    private Size getSizeFromValue(String value) {
        String[] t = value.split("x");
        if (t.length == 2) {
            try {
                int w = Integer.parseInt(t[0].trim());
                int h = Integer.parseInt(t[1].trim());
                return new Size(w, h);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }


    private Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
        String key = preference.getKey();
        if ("camera_picture_size".equals(key)) {
            Size size = getSizeFromValue((String) newValue);
            if (size != null) {
                mMediaRecorder.getSettings().setPictureSize(size);
            }
        } else if ("camera_preview_size".equals(key)) {
            Size size = getSizeFromValue((String) newValue);
            if (size != null) {
                mMediaRecorder.getSettings().setPreviewSize(size);
            }
        }
        return true;
    };
}
