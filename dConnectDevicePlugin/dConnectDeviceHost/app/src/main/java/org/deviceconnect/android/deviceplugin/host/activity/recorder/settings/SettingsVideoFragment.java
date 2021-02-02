package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.content.res.Resources;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Range;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.SeekBarDialogPreference;
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
        setPreviewAutoFocusPreference(settings);
        setPreviewWhiteBalancePreference(settings);
        setPreviewWhiteBalanceTemperaturePreference(settings);
        setPreviewAutoExposurePreference(settings);
        setPreviewProfilePreference(settings.getPreviewEncoderName(), false);
        setPreviewLevelPreference(settings.getPreviewEncoderName(), false);
        setPreviewSensorExposureTime(settings);
        setPreviewSensorSensitivity(settings);
        setPreviewSensorFrameDuration(settings);
        setPreviewStabilization(settings);
        setPreviewOpticalStabilization(settings);
        setPreviewNoiseReduction(settings);
        setPreviewFocalLength(settings);

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
                pref.setValue("none");
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
                pref.setValue("none");
            }
        }
    }

    private void setPreviewAutoFocusPreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_auto_focus");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedAutoFocusModeList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    switch (mode) {
                        case CameraMetadata.CONTROL_AF_MODE_OFF:
                            entryNames.add("OFF");
                            break;
                        case CameraMetadata.CONTROL_AF_MODE_AUTO:
                            entryNames.add("AUTO");
                            break;
                        case CameraMetadata.CONTROL_AF_MODE_MACRO:
                            entryNames.add("MACRO");
                            break;
                        case CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO:
                            entryNames.add("CONTINUOUS_VIDEO");
                            break;
                        case CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE:
                            entryNames.add("CONTINUOUS_PICTURE");
                            break;
                        case CameraMetadata.CONTROL_AF_MODE_EDOF:
                            entryNames.add("EDOF");
                            break;
                    }
                    entryValues.add(String.valueOf(mode));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getPreviewAutoFocusMode()));
            } else {
                pref.setEnabled(false);
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
            List<Integer> modeList = settings.getSupportedWhiteBalanceModeList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (Integer mode : modeList) {
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
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewWhiteBalanceTemperaturePreference(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_white_balance_temperature");
        if (pref != null) {
            Range<Integer> range = settings.getSupportedWhiteBalanceTemperature();
            if (range != null) {
                pref.setMinValue(range.getLower());
                pref.setMaxValue(range.getUpper());
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしているホワイトバランスのリストを設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewAutoExposurePreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_auto_exposure_mode");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedAutoExposureModeList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    switch (mode) {
                        case CameraMetadata.CONTROL_AE_MODE_OFF:
                            entryNames.add("OFF");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON:
                            entryNames.add("ON");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH:
                            entryNames.add("ON_AUTO_FLASH");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH:
                            entryNames.add("ON_ALWAYS_FLASH");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE:
                            entryNames.add("ON_AUTO_FLASH_REDEYE");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON_EXTERNAL_FLASH:
                            entryNames.add("ON_EXTERNAL_FLASH");
                            break;
                    }
                    entryValues.add(String.valueOf(mode));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getPreviewAutoExposureMode()));
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewSensorExposureTime(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_exposure_time");
        if (pref != null) {
            Range<Long> range = settings.getSupportedSensorExposureTime();
            if (range != null) {
                pref.setMinValue(range.getLower().intValue());
                pref.setMaxValue(range.getUpper().intValue());
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewSensorSensitivity(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_sensitivity");
        if (pref != null) {
            Range<Integer> range = settings.getSupportedSensorSensitivity();
            if (range != null) {
                pref.setMinValue(range.getLower());
                pref.setMaxValue(range.getUpper());
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewSensorFrameDuration(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_frame_duration");
        if (pref != null) {
            Long max = settings.getMaxSensorFrameDuration();
            if (max != null) {
                pref.setMinValue(0);
                pref.setMaxValue(max.intValue());
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewStabilization(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_stabilization_mode");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedStabilizationList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    switch (mode) {
                        case CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON:
                            entryNames.add("ON");
                            break;
                        case CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF:
                            entryNames.add("OFF");
                            break;
                    }
                    entryValues.add(String.valueOf(mode));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getStabilizationMode()));
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewOpticalStabilization(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_optical_stabilization_mode");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedOpticalStabilizationList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    switch (mode) {
                        case CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON:
                            entryNames.add("ON");
                            break;
                        case CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF:
                            entryNames.add("OFF");
                            break;
                    }
                    entryValues.add(String.valueOf(mode));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getOpticalStabilizationMode()));
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewNoiseReduction(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_reduction_noise");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedNoiseReductionList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    switch (mode) {
                        case CameraMetadata.NOISE_REDUCTION_MODE_FAST:
                            entryNames.add("MODE_FAST");
                            break;
                        case CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY:
                            entryNames.add("HIGH_QUALITY");
                            break;
                        case CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL:
                            entryNames.add("MODE_MINIMAL");
                            break;
                        case CameraMetadata.NOISE_REDUCTION_MODE_OFF:
                            entryNames.add("OFF");
                            break;
                        case CameraMetadata.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG:
                            entryNames.add("ZERO_SHUTTER_LAG");
                            break;
                    }
                    entryValues.add(String.valueOf(mode));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getNoiseReduction()));
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewFocalLength(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_focal_length");
        if (pref != null) {
            List<Float> modeList = settings.getSupportedFocalLengthList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("None");
                entryValues.add("none");
                for (Float mode : modeList) {
                    entryNames.add(String.valueOf(mode));
                    entryValues.add(String.valueOf(mode));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getFocalLength()));
            } else {
                pref.setEnabled(false);
            }
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
