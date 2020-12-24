package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.libsrt.SRT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SRTSettings {

    public static final String FILE_NAME = "srt_properties";

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

    private PropertyUtil mPref;
    private Context mContext;

    public SRTSettings(Context context) {
        mContext = context;
        mPref = new PropertyUtil(context, FILE_NAME);
    }

    /**
     * 指定されたキーの値を整数にして取得します.
     *
     * @param key 格納されているキー
     * @param defaultValue 値が格納されていない場合に返却する値
     * @return 整数値
     */
    private int getInt(String key, int defaultValue) {
        String value = mPref.getString(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
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
     * SRT オプション設定項目の定義.
     *
     * SRT オプションの列挙子 ({@link SRT} で定義されているもの) に対して、値の型とプリファレンスキーを対応づける.
     */
    private class SRTOptionItem {
        final int mOptionEnum;
        final Class<?> mValueClass;
        final int mPrefKey;

        SRTOptionItem(int optionEnum, Class<?> valueClass, int prefKey) {
            mOptionEnum = optionEnum;
            mValueClass = valueClass;
            mPrefKey = prefKey;
        }

        int getOptionEnum() {
            return mOptionEnum;
        }

        Object getValue() {
            String key = mContext.getString(mPrefKey);
            String value = mPref.getString(key, null);
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
