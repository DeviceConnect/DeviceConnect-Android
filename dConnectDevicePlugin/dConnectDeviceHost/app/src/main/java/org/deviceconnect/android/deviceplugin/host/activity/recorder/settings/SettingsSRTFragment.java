package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H264Level;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H264Profile;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H265Level;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H265Profile;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingsSRTFragment extends SettingsParameterFragment {
    private HostMediaRecorder mMediaRecorder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getSettingName());
        setPreferencesFromResource(R.xml.settings_host_recorder_srt, rootKey);

        setSummaryOptionAuto(getString(R.string.pref_key_settings_srt_inputbw));
        setSummaryOptionAuto(getString(R.string.pref_key_settings_srt_oheadbw));

        setInputTypeNumber(getString(R.string.pref_key_settings_srt_peerlatency));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_lossmaxttl));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_oheadbw));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_inputbw));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_conntimeo));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_peeridletimeo));
        setInputTypeNumber(getString(R.string.pref_key_settings_srt_packetfilter));
    }

    @Override
    public void onBindService() {
        mMediaRecorder = getRecorder();

        HostMediaRecorder.StreamingSettings settings = getStreamingSetting();

        setPreviewServerPort();
        setPreviewServerUrl(settings.getPort());
        setPreviewSizePreference(settings);
        setPreviewVideoEncoderPreference(settings);
        setPreviewProfileLevelPreference(settings, settings.getPreviewEncoderName(), false);

        setPreviewCutOutReset();

        setInputTypeNumber("preview_framerate");
        setInputTypeNumber("preview_bitrate");
        setInputTypeNumber("preview_i_frame_interval");
        setInputTypeNumber("preview_intra_refresh");

        setPreviewClipPreference("preview_clip_left");
        setPreviewClipPreference("preview_clip_top");
        setPreviewClipPreference("preview_clip_right");
        setPreviewClipPreference("preview_clip_bottom");
    }

    private HostMediaRecorder.StreamingSettings getStreamingSetting() {
        HostMediaRecorder.Settings s = mMediaRecorder.getSettings();
        return s.getPreviewServer(getSettingName());
    }

    private void setSummaryOptionAuto(String name) {
        EditTextPreference inputBwPref = findPreference(name);
        if (inputBwPref != null) {
            inputBwPref.setSummaryProvider(summaryOptionAuto);
        }
    }

    private final Preference.SummaryProvider<EditTextPreference> summaryOptionAuto = (preference) -> {
        String value = preference.getText();
        if (TextUtils.isEmpty(value)) {
            return getString(R.string.host_setting_srt_option_auto);
        }
        return value;
    };

    private void setPreviewServerPort() {
        setInputTypeNumber("port");
        EditTextPreference pref = findPreference("port");
        if (pref != null) {
            pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
        }
    }

    private void setPreviewServerUrl(int port) {
        EditTextPreference pref = findPreference("url");
        if (pref != null) {
            String ipAddress = NetworkUtil.getIPAddress(requireContext());
            pref.setText("srt://" + ipAddress + ":" + port);
        }
    }

    /**
     * 切り抜き範囲の設定にリスナーを設定します.
     *
     * @param key キー
     */
    public void setPreviewClipPreference(String key) {
        EditTextPreference pref = findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
            setInputTypeNumber(key);
        }
    }

    private void setEmptyText(String key) {
        EditTextPreference left = findPreference(key);
        if (left != null) {
            left.setText(null);
        }
    }

    /**
     * 切り抜き範囲のリセットボタンのリスナーを設定します.
     */
    private void setPreviewCutOutReset() {
        PreferenceScreen pref = findPreference("preview_clip_reset");
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                setEmptyText("preview_clip_left");
                setEmptyText("preview_clip_top");
                setEmptyText("preview_clip_right");
                setEmptyText("preview_clip_bottom");
                mMediaRecorder.getSettings().setCropRect(null);
                return false;
            });
        }
    }

    /**
     * プレビューの解像度 Preference を作成します.
     *
     * @param settings レコーダの設定
     */
    private void setPreviewSizePreference(HostMediaRecorder.StreamingSettings settings) {
        ListPreference pref = findPreference("camera_preview_size");
        if (pref != null) {
            List<Size> previewSizes = getSupportedPreviewSizes();
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

    /**
     * エンコーダの設定を行います.
     *
     * @param settings レコーダ設定
     */
    private void setPreviewVideoEncoderPreference(HostMediaRecorder.StreamingSettings settings) {
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
    private void setPreviewProfileLevelPreference(HostMediaRecorder.StreamingSettings settings, HostMediaRecorder.VideoEncoderName encoderName, boolean reset) {
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
     * サイズの小さい方からソートを行うための比較演算子.
     */
    private static final Comparator<Size> SIZE_COMPARATOR = (lhs, rhs) -> {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    };

    /**
     * カメラID に対応したカメラデバイスがサポートしているプレビューサイズのリストを取得します.
     *
     * @return サポートしているプレビューサイズのリスト
     */
    @NonNull
    private List<Size> getSupportedPreviewSizes() {
        HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
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
                        if (p != null && l != null) {
                            return new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                        }
                    }
                    case H265: {
                        H265Profile p = H265Profile.nameOf(profile);
                        H265Level l = H265Level.nameOf(level);
                        if (p != null && l != null) {
                            return new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
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
     * 切り抜き範囲の値を取得します.
     *
     * 未設定の場合には null を返却します。
     *
     * @param key キー
     * @return 切り抜き範囲
     */
    private Integer getDrawingRange(String key) {
        EditTextPreference pref = findPreference(key);
        if (pref != null) {
            try {
                return Integer.parseInt(pref.getText());
            } catch (NumberFormatException e) {
                // ignore.
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

        HostMediaRecorder.StreamingSettings settings = getStreamingSetting();

        String key = preference.getKey();
        if ("camera_preview_size".equals(key)) {
            Size size = getSizeFromValue((String) newValue);
            if (size != null) {
                settings.setPreviewSize(size);
            }
        } else if ("port".equalsIgnoreCase(key)) {
            setPreviewServerUrl(Integer.parseInt((String) newValue));
        } else if ("preview_encoder".equals(key)) {
            // エンコーダが切り替えられたので、プロファイル・レベルは一旦削除しておく
            try {
                settings.setProfileLevel(null);
            } catch (Exception e) {
                return false;
            }
            HostMediaRecorder.VideoEncoderName encoderName =
                    HostMediaRecorder.VideoEncoderName.nameOf((String) newValue);
            setPreviewProfileLevelPreference(settings, encoderName, true);
        } else if ("preview_profile_level".equalsIgnoreCase(key)) {
            try {
                settings.setProfileLevel(getProfileLevel(settings.getPreviewEncoderName(), (String) newValue));
            } catch (Exception e) {
                return false;
            }
        } else if ("preview_clip_left".equalsIgnoreCase(key)) {
            try {
                int clipLeft = Integer.parseInt((String) newValue);
                Integer clipRight = getDrawingRange("preview_clip_right");
                if (clipRight != null && clipRight <= clipLeft) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        } else if ("preview_clip_top".equalsIgnoreCase(key)) {
            try {
                int clipTop = Integer.parseInt((String) newValue);
                Integer clipBottom = getDrawingRange("preview_clip_bottom");
                if (clipBottom != null && clipBottom <= clipTop) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        } else if ("preview_clip_right".equalsIgnoreCase(key)) {
            try {
                int clipRight = Integer.parseInt((String) newValue);
                Integer clipLeft = getDrawingRange("preview_clip_left");
                if (clipLeft != null && clipRight <= clipLeft) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        } else if ("preview_clip_bottom".equalsIgnoreCase(key)) {
            try {
                int clipBottom = Integer.parseInt((String) newValue);
                Integer clipTop = getDrawingRange("preview_clip_top");
                if (clipTop != null && clipBottom <= clipTop) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    };
}