package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.os.Bundle;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.fragment.preference.SeekBarDialogPreference;
import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H264Level;
import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H264Profile;
import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H265Level;
import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H265Profile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.util.CapabilityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UVCSettingsVideoFragment extends UVCSettingsBaseFragment {
    private MediaRecorder mMediaRecorder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getSettingsName());
        setPreferencesFromResource(R.xml.settings_uvc_recorder_video, rootKey);
    }

    @Override
    public void onBindService() {
        super.onBindService();

        mMediaRecorder = getRecorder();

        if (mMediaRecorder == null) {
            return;
        }

        MediaRecorder.Settings settings = mMediaRecorder.getSettings();

        setPictureSizePreference(settings);
        setPreviewSizePreference(settings);
        setPreviewVideoEncoderPreference(settings);
        setPreviewProfileLevelPreference(settings);
        setPreviewJpegQuality();

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
    private void setPictureSizePreference(MediaRecorder.Settings settings) {
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
    private void setPreviewSizePreference(MediaRecorder.Settings settings) {
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
    private void setPreviewVideoEncoderPreference(MediaRecorder.Settings settings) {
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
     */
    private void setPreviewProfileLevelPreference(MediaRecorder.Settings settings) {
        setPreviewProfileLevelPreference(settings, settings.getPreviewEncoderName(), false);
    }

    /**
     * エンコーダのプロファイルとレベルを設定します.
     *
     * @param settings レコーダ設定
     * @param encoderName エンコーダ
     * @param reset リセットフラグ
     */
    private void setPreviewProfileLevelPreference(MediaRecorder.Settings settings, MediaRecorder.VideoEncoderName encoderName, boolean reset) {
        ListPreference pref = findPreference("preview_profile_level");
        if (pref != null) {
            List<MediaRecorder.ProfileLevel> list = CapabilityUtil.getSupportedProfileLevel(encoderName.getMimeType());
            if (!list.isEmpty()) {
                List<String> entryValues = new ArrayList<>();
                entryValues.add("none");

                for (MediaRecorder.ProfileLevel pl : list) {
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
                    MediaRecorder.ProfileLevel pl = settings.getProfileLevel();
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
     * JPEG クオリティを設定します.
     */
    private void setPreviewJpegQuality() {
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
    private static List<Size> getSupportedPictureSizes(MediaRecorder.Settings settings) {
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
    private static List<Size> getSupportedPreviewSizes(MediaRecorder.Settings settings) {
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
    private String getProfileLevel(MediaRecorder.VideoEncoderName encoderName, MediaRecorder.ProfileLevel pl) {
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
    private MediaRecorder.ProfileLevel getProfileLevel(MediaRecorder.VideoEncoderName encoderName, String value) {
        String[] t = value.split("-");
        if (t.length == 2) {
            try {
                String profile = t[0].trim();
                String level = t[1].trim();
                switch (encoderName) {
                    case H264: {
                        H264Profile p = H264Profile.nameOf(profile);
                        H264Level l = H264Level.nameOf(level);
                        if (p != null && l != null) {
                            return new MediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                        }
                    }
                    case H265: {
                        H265Profile p = H265Profile.nameOf(profile);
                        H265Level l = H265Level.nameOf(level);
                        if (p != null && l != null) {
                            return new MediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                        }
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
        if (mMediaRecorder == null) {
            return false;
        }

        MediaRecorder.Settings settings = mMediaRecorder.getSettings();

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
        } else if ("preview_encoder".equals(key)) {
            // エンコーダが切り替えられたので、プロファイル・レベルは一旦削除しておく
            settings.setProfileLevel(null);
            MediaRecorder.VideoEncoderName encoderName =
                    MediaRecorder.VideoEncoderName.nameOf((String) newValue);
            setPreviewProfileLevelPreference(settings, encoderName, true);
        } else if ("preview_profile_level".equalsIgnoreCase(key)) {
            settings.setProfileLevel(getProfileLevel(settings.getPreviewEncoderName(), (String) newValue));
        }
        return true;
    };
}
