/*
 DevicePluginReport.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * デバイスプラグインに関する統計を扱うクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginReport {

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
     * 通信履歴.
     */
    List<BaudRate> mBaudRates = new LinkedList<>();

    /**
     * レスポンスタイムアウト履歴.
     */
    private final List<ResponseTimeout> mDiscoveryTimeoutList = new LinkedList<>();

    /**
     * データを永続化するオブジェクト.
     */
    private final SharedPreferences mPreferences;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param pluginId プラグインID
     */
    DevicePluginReport(final Context context, final String pluginId) {
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

    /**
     * 通信履歴を取得します.
     * @return 通信履歴
     */
    public List<BaudRate> getBaudRates() {
        return new ArrayList<>(mBaudRates);
    }

    /**
     * サービス検索タイムアウトを記録します.
     * @param request サービス検索タイムアウトの発生したリクエストのパス
     */
    void addServiceDiscoveryTimeout(final String request) {
        long timestamp = System.currentTimeMillis();
        synchronized (mDiscoveryTimeoutList) {
            mDiscoveryTimeoutList.add(new ResponseTimeout(request, timestamp));
            if (mDiscoveryTimeoutList.size() > 1) {
                mDiscoveryTimeoutList.remove(0);
            }
        }
    }

    /**
     * サービス検索タイムアウトの履歴を取得します.
     * @return サービス検索タイムアウトの履歴
     */
    List<ResponseTimeout> getServiceDiscoveryTimeoutList() {
        synchronized (mDiscoveryTimeoutList) {
            return new LinkedList<>(mDiscoveryTimeoutList);
        }
    }

    /**
     * サービス検索タイムアウトの履歴を全削除します.
     */
    void clearServiceDiscoveryTimeoutList() {
        synchronized(mDiscoveryTimeoutList) {
            mDiscoveryTimeoutList.clear();
        }
    }

    /**
     * すべてのデータをクリアし、初期状態に戻す.
     */
    public void clear() {
        mPreferences.edit().clear().apply();
        mBaudRates.clear();
        mDiscoveryTimeoutList.clear();
    }

    /**
     * 通信履歴.
     */
    public static class BaudRate extends BaseInfo {
        /**
         * 通信時間.
         */
        final long mBaudRate;

        /**
         * コンストラクタ.
         * @param request リクエストのパス
         * @param baudRate 通信時間
         * @param date 通信日付
         */
        BaudRate(final String request, final long baudRate, final long date) {
            super(request, date);
            mBaudRate = baudRate;
        }

        /**
         * 通信時間を取得します.
         * @return 通信時間
         */
        public long getBaudRate() {
            return mBaudRate;
        }

    }

    /**
     * レスポンスタイムアウト情報.
     */
    private static class ResponseTimeout extends BaseInfo {

        /**
         * コンストラクタ.
         * @param request リクエスト
         * @param date 通信日付
         */
        ResponseTimeout(final String request, final long date) {
            super(request, date);
        }
    }

    /**
     * ベース情報.
     */
    private abstract static class BaseInfo {

        /**
         * リクエストのパス.
         */
        final String mRequest;

        /**
         * 通信日付.
         */
        final long mDate;

        /**
         * コンストラクタ.
         * @param request リクエストのパス
         * @param date 通信日付
         */
        BaseInfo(final String request, final long date) {
            mRequest = request;
            mDate = date;
        }

        /**
         * リクエストのパスを取得します.
         * @return リクエストのパス
         */
        public String getRequest() {
            return mRequest;
        }

        /**
         * 日付を取得します.
         * @return 日付
         */
        public long getDate() {
            return mDate;
        }

        /**
         * 日付の文字列を取得します.
         * @return 日付
         */
        public String getDateString() {
            return DateFormat.format("yyyy/MM/dd kk:mm:ss", mDate).toString();
        }
    }
}
