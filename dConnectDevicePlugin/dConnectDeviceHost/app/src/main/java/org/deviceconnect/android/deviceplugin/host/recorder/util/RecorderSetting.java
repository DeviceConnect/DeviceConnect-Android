package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.libsrt.SRT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recorderの設定データ管理クラス.
 */
public class RecorderSetting {

    /**
     * シングルトン用のインスタンス.
     */
    private static RecorderSetting mInstance;

    /**
     * データを保存するプリファレンス.
     */
    private SharedPreferences mSharedPreferences;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * 設定画面でサポートする SRT オプションの定義.
     */
    private final List<SRTOptionItem> mSRTOptionItems = new ArrayList<>();
    {
        mSRTOptionItems.add(new SRTOptionItem(SRT.SRTO_PEERLATENCY, Integer.class, R.string.pref_key_settings_srt_peerlatency));
        mSRTOptionItems.add(new SRTOptionItem(SRT.SRTO_LOSSMAXTTL, Integer.class, R.string.pref_key_settings_srt_lossmaxttl));
        mSRTOptionItems.add(new SRTOptionItem(SRT.SRTO_INPUTBW, Long.class, R.string.pref_key_settings_srt_inputbw));
        mSRTOptionItems.add(new SRTOptionItem(SRT.SRTO_OHEADBW, Integer.class, R.string.pref_key_settings_srt_oheadbw));
        mSRTOptionItems.add(new SRTOptionItem(SRT.SRTO_CONNTIMEO, Integer.class, R.string.pref_key_settings_srt_conntimeo));
        mSRTOptionItems.add(new SRTOptionItem(SRT.SRTO_PEERIDLETIMEO, Integer.class, R.string.pref_key_settings_srt_peeridletimeo));
        mSRTOptionItems.add(new SRTOptionItem(SRT.SRTO_PACKETFILTER, String.class, R.string.pref_key_settings_srt_packetfilter));
    }

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
        if (mInstance == null) {
            mInstance = new RecorderSetting(context);
        }
        return mInstance;
    }

    /**
     * 指定されたキーの値を整数にして取得します.
     *
     * @param key 格納されているキー
     * @param defaultValue 値が格納されていない場合に返却する値
     * @return 整数値
     */
    private int getInt(String key, int defaultValue) {
        String value = mSharedPreferences.getString(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * MediaRecorder プレビューの声が有効か確認します.
     *
     * @return 有効の場合はtrue、それ以外はfalse
     */
    public boolean isAudioEnabled() {
        return mSharedPreferences.getBoolean(mContext.getString(R.string.pref_key_settings_audio_enabled), false);
    }

    /**
     * プレビュー音声のビットレートを取得します.
     *
     * @return プレビュー音声のビットレート
     */
    public int getPreviewAudioBitRate() {
        return getInt(mContext.getString(R.string.pref_key_settings_audio_bit_rate), 64 * 1000);
    }

    /**
     * プレビュー音声のサンプルレートを取得します.
     *
     * @return プレビュー音声のサンプルレート
     */
    public int getPreviewSampleRate() {
        return getInt(mContext.getString(R.string.pref_key_settings_audio_sample_rate), 8000);
    }

    /**
     * プレビュー音声のチャンネル数を取得します.
     *
     * @return プレビュー音声のチャンネル数
     */
    public int getPreviewChannel() {
        return getInt(mContext.getString(R.string.pref_key_settings_audio_channel), 1);
    }

    /**
     * プレビュー音声のエコーキャンセラー設定を取得します.
     *
     * @return プレビュー音声のエコーキャンセラーが有効の場合はtrue、それ以外はfalse
     */
    public boolean isUseAEC() {
        return mSharedPreferences.getBoolean(mContext.getString(R.string.pref_key_settings_audio_use_aec), true);
    }

    /**
     * SRT サーバに対して設定するオプションの一覧を作成します.
     *
     * @return オプションの一覧
     */
    public Map<Integer, Object> loadSRTSocketOptions() {
        Map<Integer, Object> options = new HashMap<>();
        for (SRTOptionItem item : mSRTOptionItems) {
            options.put(item.getOptionEnum(), item.getValue());
        }
        return options;
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

    /**
     * カメラのターゲット.
     */
    public static class Target {
        /**
         * ターゲットの ID.
         */
        private String mTarget;

        /**
         * ターゲットの名前.
         */
        private String mName;

        /**
         * ターゲットのマイムタイプ.
         *
         * <p>
         * 音声と映像を区別するのに使用します。
         * </p>
         */
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

    /**
     * SRT オプション設定項目の定義.
     *
     * SRT オプションの列挙子 ({@link SRT} で定義されているもの) に対して、値の型とプリファレンスキーを対応づける.
     */
    private class SRTOptionItem {
        final int mOptionEnum;
        final Class<?> mValueClass;
        final int mPrefKey;

        public SRTOptionItem(int optionEnum, Class<?> valueClass, int prefKey) {
            mOptionEnum = optionEnum;
            mValueClass = valueClass;
            mPrefKey = prefKey;
        }

        public int getOptionEnum() {
            return mOptionEnum;
        }

        Object getValue() {
            String key = mContext.getString(mPrefKey);
            String value = mSharedPreferences.getString(key, null);
            if (value == null || "".equals(value)) {
                return null;
            }
            try {
                if (mValueClass == Long.class) {
                    return Long.parseLong(value);
                } else if (mValueClass == Integer.class) {
                    return Integer.parseInt(value);
                }
            } catch (Exception ignored) {}
            return value;
        }
    }
}
