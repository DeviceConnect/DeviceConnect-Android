package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.SeekBarDialogPreference;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingsMJPEGFragment extends SettingsParameterFragment {
    private HostMediaRecorder mMediaRecorder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getSettingName());
        setPreferencesFromResource(R.xml.settings_host_recorder_mjpeg, rootKey);
    }

    @Override
    public void onBindService() {
        mMediaRecorder = getRecorder();

        HostMediaRecorder.StreamingSettings settings = getStreamingSetting();

        setPreviewServerPort();
        setPreviewServerUrl(settings.getPort());
        setPreviewSizePreference(settings);

        setPreviewJpegQuality();
        setPreviewCutOutReset();

        setInputTypeNumber("preview_framerate");

        setPreviewClipPreference("preview_clip_left");
        setPreviewClipPreference("preview_clip_top");
        setPreviewClipPreference("preview_clip_right");
        setPreviewClipPreference("preview_clip_bottom");
    }

    private HostMediaRecorder.StreamingSettings getStreamingSetting() {
        HostMediaRecorder.Settings s = mMediaRecorder.getSettings();
        return s.getPreviewServer(getSettingName());
    }

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
            pref.setText("http://" + ipAddress + ":" + port + "/mjpeg");
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

    /**
     * 切り抜き範囲のリセットボタンのリスナーを設定します.
     */
    private void setPreviewCutOutReset() {
        PreferenceScreen pref = findPreference("preview_clip_reset");
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                EditTextPreference left = findPreference("preview_clip_left");
                if (left != null) {
                    left.setText(null);
                }
                EditTextPreference top = findPreference("preview_clip_top");
                if (top != null) {
                    top.setText(null);
                }
                EditTextPreference right = findPreference("preview_clip_right");
                if (right != null) {
                    right.setText(null);
                }
                EditTextPreference bottom = findPreference("preview_clip_bottom");
                if (bottom != null) {
                    bottom.setText(null);
                }
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
