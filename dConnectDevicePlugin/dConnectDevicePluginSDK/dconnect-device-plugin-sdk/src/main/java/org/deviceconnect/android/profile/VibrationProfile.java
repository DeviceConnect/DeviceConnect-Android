/*
 VibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.profile.VibrationProfileConstants;

import java.util.ArrayList;

/**
 * Vibration プロファイル.
 * 
 * <p>
 * スマートデバイスのバイブレーション操作機能を提供するAPI.<br>
 * スマートデバイスのバイブレーション操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * AndroidManifest.xmlにてVIBRATEパーミッションの指定が必要。
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class VibrationProfile extends DConnectProfile implements VibrationProfileConstants {

    /**
     * 振動パターンで使われる区切り文字.
     * 
     */
    public static final String VIBRATION_PATTERN_DELIM = ",";

    /**
     * デフォルトの最大バイブレーション鳴動時間. {@value} ミリ秒
     */
    public static final long DEFAULT_MAX_VIBRATION_TIME = 500;

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // リクエストゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストから鳴動パターンを取得する.
     * 
     * @param request リクエストパラメータ
     * @return 鳴動パターン文字列。無い場合はnullを返す。
     */
    public static String getPattern(final Intent request) {
        String value = request.getStringExtra(PARAM_PATTERN);
        return value;
    }

    // ------------------------------------
    // ユーティリティメソッド
    // ------------------------------------

    /**
     * 鳴動パターンを文字列から解析し、数値の配列に変換する.<br>
     * 数値の前後の半角のスペースは無視される。その他の半角、全角のスペースは不正なフォーマットとして扱われる。
     * 
     * @param pattern 鳴動パターン文字列。空文字、nullの場合、最大値を返す。
     * @return 鳴動パターンの配列。解析できないフォーマットの場合nullを返す。
     */
    protected final long[] parsePattern(final String pattern) {

        if (pattern == null || pattern.length() == 0) {
            return new long[] {getMaxVibrationTime()};
        }

        long[] result = null;

        if (pattern.contains(VIBRATION_PATTERN_DELIM)) {
            String[] times = pattern.split(VIBRATION_PATTERN_DELIM);
            ArrayList<Long> values = new ArrayList<Long>();
            for (String time : times) {
                try {
                    String valueStr = time.trim();
                    if (valueStr.length() == 0) {
                        if (values.size() != times.length - 1) {
                            // 数値の間にスペースがある場合はフォーマットエラー
                            // ex. 100, , 100
                            values.clear();
                        }
                        break;
                    }
                    long value = Long.parseLong(time.trim());
                    if (value < 0) {
                        // 数値が負の値の場合はフォーマットエラー
                        values.clear();
                        break;
                    }
                    values.add(value);
                } catch (NumberFormatException e) {
                    values.clear();
                    mLogger.warning("Exception in the VibrationProfile#parsePattern() method. " + e.toString());
                    break;
                }
            }

            if (values.size() != 0) {
                result = new long[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    result[i] = values.get(i);
                }
            }
        } else {
            try {
                long time = Long.parseLong(pattern);
                if (time >= 0) {
                    result = new long[] {time};
                }
            } catch (NumberFormatException e) {
                mLogger.warning("Exception in the VibrationProfile#parsePattern() method. " + e.toString());
            }
        }

        return result;
    }

    /**
     * バイブレーションの最大鳴動時間を取得する.<br>
     * 実装クラス毎にオーバーライドし、適切な数値を返すこと。
     * 
     * @return 最大バイブレーション鳴動時間。単位はミリ秒。
     */
    protected long getMaxVibrationTime() {
        return DEFAULT_MAX_VIBRATION_TIME;
    }
}
