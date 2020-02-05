package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Recorderの設定データ管理クラス.
 */
public class RecorderSettingData {
    public static final String PREVIEW_JPEG_MIME_TYPE = "video/x-mjpeg";

    public Set<String> targets = null;

    /** Context */
    private Context context;

    /** シングルトン用 */
    private static RecorderSettingData instance;

    /**
     * コンストラクタ.
     * シングルトンにするためにprivateとしてある.
     *
     * @param context Context
     */
    private RecorderSettingData(Context context) {
        this.context = context;
        loadTargetIds();
    }

    /**
     * 共通のインスタンスを返す.
     *
     * @param context Context
     * @return インスタンス
     */
    public static RecorderSettingData getInstance(Context context) {
        if (instance == null) {
            instance = new RecorderSettingData(context);
        }
        return instance;
    }

    /**
     * データ保存.
     */
    public void saveTargets(final String[] targets) {
        if (targets == null) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("targets", new HashSet<>(Arrays.asList(targets)));
        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void storePreviewQuality(final String target, int quality) {
        getSharedPreferences().edit().putInt(getPreviewQualityKey(target), quality).apply();
    }

    public int readPreviewQuality(final String target) {
        return getSharedPreferences().getInt(getPreviewQualityKey(target), 40);
    }

    public void storePreviewName(final String target, final String name) {
        getSharedPreferences().edit().putString(getPreviewNameKey(target), name).apply();
    }

    public String readPreviewName(final String target) {
        return getSharedPreferences().getString(getPreviewNameKey(target), null);
    }

    public void loadTargetIds() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        targets = preferences.getStringSet("targets", new HashSet<>());
    }

    public String[] getTargets() {
        return targets.toArray(new String[targets.size()]);
    }


    private String getPreviewQualityKey(final String target) {
        return target + "-" + PREVIEW_JPEG_MIME_TYPE + "-preview-quality";
    }

    private String getPreviewNameKey(final String target) {
        return target + "-" + PREVIEW_JPEG_MIME_TYPE + "-preview-name";
    }

}
