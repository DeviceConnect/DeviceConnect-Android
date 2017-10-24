/*
 CommunicationHistory.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import java.util.LinkedList;
import java.util.List;

/**
 * デバイスプラグインとの通信履歴を保持するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class CommunicationHistory {

    /** デフォルトの最大履歴サイズ. */
    private static final int DEFAULT_HISTORY_MAX_SIZE = 10;

    /** ファイル名のプレフィクス. */
    private static final String PREFIX_PREFERENCES = "plugin_report_";

    /**
     * 設定キー: 平均通信時間.
     */
    private static final String KEY_AVERAGE_BAUD_RATE = "average_baud_rate";

    /**
     * 設定キー: 最遅通信時間.
     */
    private static final String KEY_WORST_BAUD_RATE = "worst_baud_rate";

    /**
     * 設定キー: 最遅通信時間のリクエスト.
     */
    private static final String KEY_WORST_REQUEST = "worst_request";

    /**
     * データを永続化するオブジェクト.
     */
    private final SharedPreferences mPreferences;

    /**
     * 応答タイムアウトの履歴を保持するリスト.
     */
    private final List<Info> mNotRespondedList = new LinkedList<>();

    /**
     * 通信履歴を保持するリスト.
     */
    private final List<Info> mRespondedList = new LinkedList<>();

    /**
     * 最大履歴サイズ設定.
     */
    private int mHistoryMaxSize = DEFAULT_HISTORY_MAX_SIZE;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param pluginId プラグインID
     */
    CommunicationHistory(final Context context, final String pluginId) {
        String prefName = PREFIX_PREFERENCES + pluginId;
        mPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    /**
     * 平均通信時間を保存します.
     * @param baudRate 保存する平均通信時間
     */
    void setAverageBaudRate(final long baudRate) {
        mPreferences.edit().putLong(KEY_AVERAGE_BAUD_RATE, baudRate).apply();
    }

    /**
     * 平均通信時間を取得します.
     * @return 平均通信時間
     */
    public long getAverageBaudRate() {
        return mPreferences.getLong(KEY_AVERAGE_BAUD_RATE, 0);
    }

    /**
     * 最遅通信時間を保存します.
     * @param baudRate 最遅通信時間
     */
    void setWorstBaudRate(final long baudRate) {
        mPreferences.edit().putLong(KEY_WORST_BAUD_RATE, baudRate).apply();
    }

    /**
     * 最遅通信時間を取得します.
     * @return 最遅通信時間
     */
    public long getWorstBaudRate() {
        return mPreferences.getLong(KEY_WORST_BAUD_RATE, 0);
    }

    /**
     * 最遅通信時間のリクエストを保存します.
     * @param request 最遅通信時間のリクエスト
     */
    void setWorstBaudRateRequest(final String request) {
        mPreferences.edit().putString(KEY_WORST_REQUEST, request).apply();
    }

    /**
     * 最遅通信時間のリクエストを取得します.
     * @return 最遅通信時間のリクエスト
     */
    public String getWorstBaudRateRequest() {
        return mPreferences.getString(KEY_WORST_REQUEST, "None");
    }

    public void add(final Info info) {
        if (!info.isTimeout()) {
            add(mRespondedList, info);
        } else {
            add(mNotRespondedList, info);
        }
    }

    private void add(final List<Info> list, final Info info) {
        synchronized (list) {
            list.add(info);
            if (list.size() > getMaxSize()) {
                list.remove(0);
            }
        }
    }

    private int getMaxSize() {
        return mHistoryMaxSize;
    }

    public List<Info> getNotRespondedCommunications() {
        synchronized (mNotRespondedList) {
            return new LinkedList<>(mNotRespondedList);
        }
    }

    public List<Info> getRespondedCommunications() {
        synchronized (mRespondedList) {
            return new LinkedList<>(mRespondedList);
        }
    }

    /**
     * 保持していたすべてのデータをクリアし、初期状態に戻す.
     */
    void clear() {
        synchronized (this) {
            mPreferences.edit().clear().apply();
            mRespondedList.clear();
            mNotRespondedList.clear();
        }
    }

    /**
     * 1リクエスト当たりの通信についての情報.
     */
    public static class Info {

        /**
         * サービスID.
         *
         * NOTE: プラグイン自体へのリクエストの場合は <code>null</code> となる.
         */
        final String mServiceId;

        /**
         * リクエストのパス.
         */
        final String mRequestPath;

        /**
         * プラグインへのリクエスト送信時刻を示すUNIX時間.
         */
        final long mStart;

        /**
         * プラグインからのレスポンス受信時刻を示すUNIX時間.
         * 負の値が設定されている場合、レスポンスタイムアウトが発生したことを示す.
         */
        final long mEnd;

        Info(final String serviceId, final String path, final long start, final long end) {
            if (end > 0 && start > end){
                throw new IllegalArgumentException("`end` is must be larger than `start` if no timeout.");
            }
            mServiceId= serviceId;
            mRequestPath = path;
            mStart = start;
            mEnd = end;
        }

        Info(final String serviceId, final String path, final long start) {
            this(serviceId, path, start, -1);
        }

        public String getServiceId() {
            return mServiceId;
        }

        public String getRequestPath() {
            return mRequestPath;
        }

        public long getStartTime() {
            return mStart;
        }

        public long getEndTime() {
            return mEnd;
        }

        /**
         * ラウンドトリップ時間を取得する.
         * 単位はミリ秒. タイムアウトが発生していた場合は負の値を返す.
         * @return
         */
        public long getRoundTripTime() {
            if (isTimeout()) {
                return -1;
            }
            return mEnd - mStart;
        }

        public boolean isTimeout() {
            return mEnd < 0;
        }

        /**
         * リクエストを送信した時刻の文字列を取得します.
         * @return リクエストを送信した時刻
         */
        public String getDateString() {
            return DateFormat.format("yyyy/MM/dd kk:mm:ss", mStart).toString();
        }

    }
}
