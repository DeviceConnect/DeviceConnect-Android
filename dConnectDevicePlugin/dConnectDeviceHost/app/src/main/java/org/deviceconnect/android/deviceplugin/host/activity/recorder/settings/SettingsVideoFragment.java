package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;
import android.util.Range;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.SeekBarDialogPreference;
import org.deviceconnect.android.deviceplugin.host.profile.utils.AutoExposure;
import org.deviceconnect.android.deviceplugin.host.profile.utils.AutoFocus;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H264Level;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H264Profile;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H265Level;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H265Profile;
import org.deviceconnect.android.deviceplugin.host.profile.utils.NoiseReduction;
import org.deviceconnect.android.deviceplugin.host.profile.utils.OpticalStabilization;
import org.deviceconnect.android.deviceplugin.host.profile.utils.Stabilization;
import org.deviceconnect.android.deviceplugin.host.profile.utils.WhiteBalance;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;

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
        setPreviewProfileLevelPreference(settings, settings.getPreviewEncoderName(), false);
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
        setPreviewJpegQuality(settings);
        setPreviewCutOutReset();

        setInputTypeNumber("preview_framerate");
        setInputTypeNumber("preview_bitrate");
        setInputTypeNumber("preview_i_frame_interval");
        setInputTypeNumber("preview_intra_refresh");
        setInputTypeNumber("preview_clip_left");
        setInputTypeNumber("preview_clip_top");
        setInputTypeNumber("preview_clip_right");
        setInputTypeNumber("preview_clip_bottom");
    }

    private void setPreviewCutOutReset() {
        PreferenceScreen pref = findPreference("preview_clip_reset");
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                EditTextPreference left = findPreference("preview_clip_left");
                left.setText(null);
                EditTextPreference top = findPreference("preview_clip_top");
                top.setText(null);
                EditTextPreference right = findPreference("preview_clip_right");
                right.setText(null);
                EditTextPreference bottom = findPreference("preview_clip_bottom");
                bottom.setText(null);

                mMediaRecorder.getSettings().setDrawingRange(null);

                return false;
            });
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
            List<Size> pictureSizes = getSupportedPictureSizes(settings);
            if (!pictureSizes.isEmpty()) {
                List<String> entryValues = new ArrayList<>();
                for (Size preview : pictureSizes) {
                    entryValues.add(getValueFromSize(preview));
                }
                pref.setEntries(entryValues.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

                String value = pref.getValue();
                if (value == null) {
                    Size pictureSize = settings.getPictureSize();
                    if (pictureSize != null) {
                        pref.setValue(getValueFromSize(pictureSize));
                    }
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

                String value = pref.getValue();
                if (value == null) {
                    Size previewSize = settings.getPreviewSize();
                    if (previewSize != null) {
                        pref.setValue(getValueFromSize(previewSize));
                    }
                }
                pref.setVisible(true);
            } else {
                pref.setEnabled(false);
            }
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
            List<String> list = settings.getSupportedVideoEncoders();
            if (!list.isEmpty()) {
                List<String> entryValues = new ArrayList<>(list);
                pref.setEntries(entryValues.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
                pref.setVisible(true);
            } else {
                pref.setEnabled(false);
            }
        }
    }

    /**
     * エンコーダのプロファイルとレベルを設定します.
     *
     * @param settings レコーダ設定
     * @param encoderName エンコーダ
     * @param reset リセットフラグ
     */
    private void setPreviewProfileLevelPreference(HostMediaRecorder.Settings settings, HostMediaRecorder.VideoEncoderName encoderName, boolean reset) {
        ListPreference pref = findPreference("preview_profile_level");
        if (pref != null) {
            List<HostMediaRecorder.ProfileLevel> list = CapabilityUtil.getSupportedProfileLevel(encoderName.getMimeType());
            if (!list.isEmpty()) {
                List<String> entryValues = new ArrayList<>();
                entryValues.add("none");

                for (HostMediaRecorder.ProfileLevel pl : list) {
                    String value = getProfileLevel(encoderName, pl);
                    if (value != null) {
                        entryValues.add(value);
                    }
                }

                pref.setEntries(entryValues.toArray(new String[0]));
                pref.setEntryValues(entryValues.toArray(new String[0]));
                pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);

                if (reset) {
                    pref.setValue("none");
                } else {
                    HostMediaRecorder.ProfileLevel pl = settings.getProfileLevel();
                    if (pl != null) {
                        pref.setValue(getProfileLevel(encoderName, pl));
                    }
                }

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

    private void setPreviewJpegQuality(HostMediaRecorder.Settings settings) {
        SeekBarDialogPreference pref = findPreference("preview_jpeg_quality");
        if (pref != null) {
            pref.setMinValue(0);
            pref.setMaxValue(100);
            pref.setEnabled(true);
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

    /**
     * プロファイルとレベルを文字列に変換します.
     *
     * @param encoderName エンコーダ
     * @param pl プロファイルとレベル
     * @return 文字列
     */
    private String getProfileLevel(HostMediaRecorder.VideoEncoderName encoderName, HostMediaRecorder.ProfileLevel pl) {
        switch (encoderName) {
            case H264: {
                H264Profile p = H264Profile.valueOf(pl.getProfile());
                H264Level l = H264Level.valueOf(pl.getLevel());
                if (p != null && l != null) {
                    return p.getName() + " - " + l.getName();
                }
            }
            case H265: {
                H265Profile p = H265Profile.valueOf(pl.getProfile());
                H265Level l = H265Level.valueOf(pl.getLevel());
                if (p != null && l != null) {
                    return p.getName() + " - " + l.getName();
                }
            }
        }
        return null;
    }

    /**
     * 文字列をプロファイルとレベルに変換します.
     *
     * プロファイルとレベルに変換できなかった場合には、null を返却します。
     *
     * @param encoderName エンコーダ
     * @param value 変換する文字列
     * @return プロファイルとレベル
     */
    private HostMediaRecorder.ProfileLevel getProfileLevel(HostMediaRecorder.VideoEncoderName encoderName, String value) {
        String[] t = value.split("-");
        if (t.length == 2) {
            try {
                String profile = t[0].trim();
                String level = t[1].trim();
                switch (encoderName) {
                    case H264: {
                        H264Profile p = H264Profile.nameOf(profile);
                        H264Level l = H264Level.nameOf(level);
                        return new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                    }
                    case H265: {
                        H265Profile p = H265Profile.nameOf(profile);
                        H265Level l = H265Level.nameOf(level);
                        return new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                    }
                }
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
                mMediaRecorder.getSettings().setProfileLevel(null);
                HostMediaRecorder.VideoEncoderName encoderName =
                        HostMediaRecorder.VideoEncoderName.nameOf((String) newValue);
                setPreviewProfileLevelPreference(mMediaRecorder.getSettings(), encoderName, true);
            }
        } else if ("preview_profile_level".equalsIgnoreCase(key)) {
            if (mMediaRecorder != null) {
                HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
                settings.setProfileLevel(getProfileLevel(settings.getPreviewEncoderName(), (String) newValue));
            }
        }
        return true;
    };
}
