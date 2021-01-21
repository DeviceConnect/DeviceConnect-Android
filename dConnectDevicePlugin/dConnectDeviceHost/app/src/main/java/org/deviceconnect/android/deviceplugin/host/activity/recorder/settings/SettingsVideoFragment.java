package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.content.res.Resources;
import android.hardware.camera2.CameraMetadata;
import android.os.Bundle;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingsVideoFragment extends SettingsParameterFragment {
    private HostMediaRecorder mMediaRecorder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getRecorderId());
        setPreferencesFromResource(R.xml.settings_host_recorder_video, rootKey);
    }

    @Override
    public void onBindService() {
        mMediaRecorder = getRecorder();

        HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();

        setPictureSizePreference(settings);
        setPreviewSizePreference(settings);
        setPreviewVideoEncoderPreference(settings);
        setPreviewWhiteBalancePreference(settings);
        setPreviewProfilePreference(settings.getPreviewEncoderName(), false);
        setPreviewLevelPreference(settings.getPreviewEncoderName(), false);

        setInputTypeNumber("preview_framerate");
        setInputTypeNumber("preview_bitrate");
        setInputTypeNumber("preview_i_frame_interval");
        setInputTypeNumber("preview_intra_refresh");
        setInputTypeNumber("preview_quality");
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

    /**
     * エンコーダの設定を行います.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewVideoEncoderPreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_encoder");
        if (pref != null) {
            pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        }
    }

    private void setPreviewProfilePreference(HostMediaRecorder.VideoEncoderName encoderName, boolean reset) {
        ListPreference pref = findPreference("preview_profile");
        if (pref != null) {
            Resources res = getContext().getResources();
            switch (encoderName) {
                case H264:
                    pref.setEntries(res.getStringArray(R.array.h264_profile_names));
                    pref.setEntryValues(res.getStringArray(R.array.h264_profile_values));
                    break;
                case H265:
                    pref.setEntries(res.getStringArray(R.array.h265_profile_names));
                    pref.setEntryValues(res.getStringArray(R.array.h265_profile_values));
                    break;
            }
            if (reset) {
                pref.setValue("default");
            }
        }
    }

    private void setPreviewLevelPreference(HostMediaRecorder.VideoEncoderName encoderName, boolean reset) {
        ListPreference pref = findPreference("preview_level");
        if (pref != null) {
            Resources res = getContext().getResources();
            switch (encoderName) {
                case H264:
                    pref.setEntries(res.getStringArray(R.array.h264_level_names));
                    pref.setEntryValues(res.getStringArray(R.array.h264_level_values));
                    break;
                case H265:
                    pref.setEntries(res.getStringArray(R.array.h265_level_names));
                    pref.setEntryValues(res.getStringArray(R.array.h265_level_values));
                    break;
            }
            if (reset) {
                pref.setValue("default");
            }
        }
    }



    /**
     * レコーダでサポートしているホワイトバランスのリストを設定します.
     *
     * @param settings レコーダ設定
     */
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

    private final Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
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
        } else if ("preview_encoder".equals(key)) {
            if (mMediaRecorder != null) {
                HostMediaRecorder.VideoEncoderName encoderName =
                        HostMediaRecorder.VideoEncoderName.nameOf((String) newValue);
                setPreviewProfilePreference(encoderName, true);
                setPreviewLevelPreference(encoderName, true);
            }
        }
        return true;
    };
}
