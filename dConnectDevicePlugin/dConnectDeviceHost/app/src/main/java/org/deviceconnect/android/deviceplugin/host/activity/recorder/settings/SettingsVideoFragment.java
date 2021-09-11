package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;
import android.util.Range;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.SeekBarDialogPreference;
import org.deviceconnect.android.deviceplugin.host.profile.utils.AutoExposure;
import org.deviceconnect.android.deviceplugin.host.profile.utils.AutoFocus;
import org.deviceconnect.android.deviceplugin.host.profile.utils.NoiseReduction;
import org.deviceconnect.android.deviceplugin.host.profile.utils.OpticalStabilization;
import org.deviceconnect.android.deviceplugin.host.profile.utils.Stabilization;
import org.deviceconnect.android.deviceplugin.host.profile.utils.WhiteBalance;
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
        setPreviewFps(settings);
        setPreviewAutoFocusPreference(settings);
        setPreviewWhiteBalancePreference(settings);
        setPreviewWhiteBalanceTemperaturePreference(settings);
        setPreviewAutoExposurePreference(settings);
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
    }

    /**
     * 静止画の解像度の Preference を作成します.
     *
     * @param settings レコーダの設定
     */
    private void setPictureSizePreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("camera_picture_size");
        if (pref != null) {
            List<Size> pictureSizes = getSupportedPictureSizes(settings);
            if (!pictureSizes.isEmpty()) {
                List<String> entryValues = new ArrayList<>();
                for (Size preview : pictureSizes) {
                    entryValues.add(getValueFromSize(preview));
                }
                pref.setEntries(entryValues.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

                Size pictureSize = settings.getPictureSize();
                if (pictureSize != null) {
                    pref.setValue(getValueFromSize(pictureSize));
                }
                pref.setVisible(true);
            } else {
                pref.setEnabled(false);
            }
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
            List<Size> previewSizes = getSupportedPreviewSizes(settings);
            if (!previewSizes.isEmpty()) {
                List<String> entryValues = new ArrayList<>();
                for (Size preview : previewSizes) {
                    entryValues.add(getValueFromSize(preview));
                }

                pref.setEntries(entryValues.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

                Size previewSize = settings.getPreviewSize();
                if (previewSize != null) {
                    pref.setValue(getValueFromSize(previewSize));
                }
                pref.setVisible(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    private void setPreviewFps(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("camera_fps");
        if (pref != null) {
            List<Range<Integer>> fpsList = settings.getSupportedFps();
            if (!fpsList.isEmpty()) {
                List<String> entryValues = new ArrayList<>();
                for (Range<Integer> fps : fpsList) {
                    entryValues.add(fps.getLower() + "-" + fps.getUpper());
                }

                pref.setEntries(entryValues.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

                Range<Integer> previewFps = settings.getPreviewFps();
                if (previewFps != null) {
                    pref.setValue(previewFps.getLower() + "-" + previewFps.getUpper());
                }
//                int previewSize = settings.getPreviewMaxFrameRate();
//                if (previewSize != null) {
//                    pref.setValue(getValueFromSize(previewSize));
//                }
                pref.setVisible(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * 自動フォーカスモードの設定を行います.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewAutoFocusPreference(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_auto_focus");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedAutoFocusModeList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("none");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    AutoFocus af = AutoFocus.valueOf(mode);
                    if (af != null) {
                        entryNames.add(af.getName());
                        entryValues.add(String.valueOf(mode));
                    }
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getPreviewAutoFocusMode()));
                pref.setEnabled(true);
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
                entryNames.add("none");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    WhiteBalance wb = WhiteBalance.valueOf(mode);
                    if (wb != null) {
                        entryNames.add(wb.getName());
                        entryValues.add(String.valueOf(mode));
                    }
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getPreviewWhiteBalance()));
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * ホワイトバランスの色温度を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewWhiteBalanceTemperaturePreference(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_white_balance_temperature");
        if (pref != null) {
            Range<Integer> range = settings.getSupportedWhiteBalanceTemperature();
            if (range != null) {
                pref.setMinValue(range.getLower());
                pref.setMaxValue(range.getUpper());
                pref.setEnabled(true);
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
                entryNames.add("none");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    AutoExposure ae = AutoExposure.valueOf(mode);
                    if (ae != null) {
                        entryNames.add(ae.getName());
                        entryValues.add(String.valueOf(mode));
                    }
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getPreviewAutoExposureMode()));
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしている露出時間の範囲を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewSensorExposureTime(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_exposure_time");
        if (pref != null) {
            Range<Long> range = settings.getSupportedSensorExposureTime();
            if (range != null) {
                pref.setMinValue(range.getLower().intValue());
                pref.setMaxValue(range.getUpper().intValue());
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしている ISO 感度の範囲を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewSensorSensitivity(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_sensitivity");
        if (pref != null) {
            Range<Integer> range = settings.getSupportedSensorSensitivity();
            if (range != null) {
                pref.setMinValue(range.getLower());
                pref.setMaxValue(range.getUpper());
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしているフレーム期間の範囲を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewSensorFrameDuration(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_sensor_frame_duration");
        if (pref != null) {
            Long max = settings.getMaxSensorFrameDuration();
            if (max != null) {
                pref.setMinValue(0);
                pref.setMaxValue(max.intValue());
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしている手ぶれ補正を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewStabilization(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_stabilization_mode");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedStabilizationList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("none");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    Stabilization s = Stabilization.valueOf(mode);
                    if (s != null) {
                        entryNames.add(s.getName());
                        entryValues.add(String.valueOf(mode));
                    }
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getStabilizationMode()));
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしている光学手ぶれ補正を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewOpticalStabilization(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_optical_stabilization_mode");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedOpticalStabilizationList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("none");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    OpticalStabilization ost = OpticalStabilization.valueOf(mode);
                    if (ost != null) {
                        entryNames.add(ost.getName());
                        entryValues.add(String.valueOf(mode));
                    }
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getOpticalStabilizationMode()));
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしているノイズ低減モードを設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewNoiseReduction(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_reduction_noise");
        if (pref != null) {
            List<Integer> modeList = settings.getSupportedNoiseReductionList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("none");
                entryValues.add("none");
                for (Integer mode : modeList) {
                    NoiseReduction nr = NoiseReduction.valueOf(mode);
                    if (nr != null) {
                        entryNames.add(nr.getName());
                        entryValues.add(String.valueOf(mode));
                    }
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getNoiseReduction()));
                pref.setEnabled(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * レコーダでサポートしている焦点距離を設定します.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewFocalLength(HostMediaRecorder.Settings settings) {
        ListPreference pref = findPreference("preview_focal_length");
        if (pref != null) {
            List<Float> modeList = settings.getSupportedFocalLengthList();
            if (modeList != null && !modeList.isEmpty()) {
                List<String> entryNames = new ArrayList<>();
                List<String> entryValues = new ArrayList<>();
                entryNames.add("none");
                entryValues.add("none");
                for (Float mode : modeList) {
                    entryNames.add(String.valueOf(mode));
                    entryValues.add(String.valueOf(mode));
                }
                pref.setEntries(entryNames.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setValue(String.valueOf(settings.getFocalLength()));
                pref.setEnabled(true);
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

    /**
     * 文字列を Size に変換します.
     *
     * Size に変換できなかった場合には null を返却します。
     *
     * @param value 文字列のサイズ
     * @return サイズ
     */
    private Size getSizeFromValue(String value) {
        String[] t = value.split("x");
        if (t.length == 2) {
            try {
                int w = Integer.parseInt(t[0].trim());
                int h = Integer.parseInt(t[1].trim());
                return new Size(w, h);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Range<Integer> getRangeFromValue(String value) {
        String[] t = value.split("-");
        if (t.length == 2) {
            try {
                int w = Integer.parseInt(t[0].trim());
                int h = Integer.parseInt(t[1].trim());
                return new Range<>(w, h);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 設定が変更された時に呼び出されるリスナー.
     */
    private final Preference.OnPreferenceChangeListener mOnPreferenceChangeListener = (preference, newValue) -> {
        if (mMediaRecorder == null) {
            return false;
        }

        HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();

        String key = preference.getKey();
        if ("camera_picture_size".equals(key)) {
            Size size = getSizeFromValue((String) newValue);
            if (size != null) {
                settings.setPictureSize(size);
            }
        } else if ("camera_preview_size".equals(key)) {
            Size size = getSizeFromValue((String) newValue);
            if (size != null) {
                settings.setPreviewSize(size);
            }
        } else if ("camera_fps".equals(key)) {
            Range<Integer> fps = getRangeFromValue((String) newValue);
            if (fps != null) {
                settings.setPreviewFps(fps);
            }
        }
        return true;
    };
}
