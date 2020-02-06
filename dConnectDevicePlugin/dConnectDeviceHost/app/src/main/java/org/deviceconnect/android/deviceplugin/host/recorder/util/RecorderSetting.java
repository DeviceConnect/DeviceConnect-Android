package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recorderの設定データ管理クラス.
 */
public class RecorderSetting {
    public static final String PREVIEW_JPEG_MIME_TYPE = "video/x-mjpeg";

    /** Context */
    private Context mContext;

    /** シングルトン用 */
    private static RecorderSetting instance;

    private SharedPreferences mSharedPreferences;

    /**
     * コンストラクタ.
     * シングルトンにするためにprivateとしてある.
     *
     * @param context Context
     */
    private RecorderSetting(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 共通のインスタンスを返す.
     *
     * @param context Context
     * @return インスタンス
     */
    public static RecorderSetting getInstance(Context context) {
        if (instance == null) {
            instance = new RecorderSetting(context);
        }
        return instance;
    }

    /**
     * MediaRecorder のターゲットのリストを取得します.
     *
     * @return MediaRecorder のターゲットのリスト
     */
    public List<Target> getTargets() {
        Set<String> sets = mSharedPreferences.getStringSet("targets", new HashSet<>());
        List<Target> list = new ArrayList<>();
        for (String t : sets) {
            try {
                list.add(new Target(t));
            } catch (Exception e) {
                // ignore
            }
        }
        return list;
    }

    /**
     * MediaRecorder のターゲットを保存します.
     *
     * @param targets ターゲットのリスト
     */
    public void saveTargets(List<Target> targets) {
        if (targets == null) {
            return;
        }

        HashSet<String> t = new HashSet<>();
        for (Target target : targets) {
            t.add(target.to());
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet("targets", t);
        editor.apply();
    }

    /**
     * 各 MediaRecorder のプレビューサーバのポート番号を設定します.
     *
     * @param target ターゲット
     * @param port ポート番号
     */
    public void setPort(String target, String mimeType, int port) {
        mSharedPreferences.edit().putInt(target + "-" + mimeType + "-port", port).apply();
    }

    /**
     * 各 MediaRecorder のプレビューサーバのポート番号を取得します.
     *
     * @param target ターゲット
     * @param defaultPort 保存されていなかった時のデフォルトの値
     * @return ポート番号
     */
    public int getPort(String target, String mimeType, int defaultPort) {
        return mSharedPreferences.getInt(target + "-" + mimeType + "-port", defaultPort);
    }

    /**
     * 各 MediaRecorder の JPEG のクオリティを設定します.
     *
     * @param target ターゲット
     * @param quality クオリティ (1 - 100)
     */
    public void setJpegQuality(String target, int quality) {
        mSharedPreferences.edit().putInt(target + "-jpeg-quality", quality).apply();
    }

    /**
     * 各 MediaRecorder の JPEG のクオリティを取得します.
     *
     * @param target ターゲット
     * @param defaultQuality 保存されていなかった時のデフォルトの値
     * @return JPEG のクオリティ
     */
    public int getJpegQuality(String target, int defaultQuality) {
        return mSharedPreferences.getInt(target + "-jpeg-quality", defaultQuality);
    }

    public static class Target {
        private String mTarget;
        private String mName;
        private String mMimeType;

        public Target(String target, String name, String mimeType) {
            mTarget = target;
            mName = name;
            mMimeType = mimeType;
        }

        private Target(String t) {
            String[] split = t.split("##");
            mTarget = split[0];
            mName = split[1];
            mMimeType = split[2];
        }

        public String getTarget() {
            return mTarget;
        }

        public String getName() {
            return mName;
        }

        public String getMimeType() {
            return mMimeType;
        }

        private String to() {
            return mTarget + "##" + mName + "##" + mMimeType;
        }
    }
}
