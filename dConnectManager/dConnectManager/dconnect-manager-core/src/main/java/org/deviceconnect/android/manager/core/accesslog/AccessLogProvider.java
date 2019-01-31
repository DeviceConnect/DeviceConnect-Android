/*
 AccessLogProvider.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.accesslog;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.List;

/**
 * アクセスログを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class AccessLogProvider {
    /**
     * アクセスログのDBを管理するクラス.
     */
    private AccessLogHelper mAccessLogHelper;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public AccessLogProvider(final Context context) {
        mAccessLogHelper = new AccessLogHelper(context);
    }

    /**
     * 日付のリストを取得します.
     *
     * @return 日付のリスト
     */
    public List<String> getDateList() {
        return mAccessLogHelper.read(AccessLog::getDateList);
    }

    /**
     * 指定された日付のアクセスログを取得します.
     *
     * @param date 日付
     * @return アクセスログのリスト
     */
    public List<AccessLog> getAccessLogsOfDate(final String date) {
        return mAccessLogHelper.read((db) -> AccessLog.getAccessLogsOfDate(db, date));
    }

    /**
     * アクセスログを追加します.
     *
     * @param accessLog 追加するアクセスログ
     * @return 追加に成功した場合にはtrue、それ以外はfalse
     */
    public boolean add(final AccessLog accessLog) {
        return mAccessLogHelper.write((db) -> AccessLog.add(db, accessLog));
    }

    /**
     * アクセスログを削除します.
     *
     * @param accessLog 削除するアクセスログ
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean remove(final AccessLog accessLog) {
        return mAccessLogHelper.write((db) -> AccessLog.remove(db, accessLog));
    }

    /**
     * 指定された日付のアクセスログを削除します.
     *
     * @param date 日付
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean remove(final String date) {
        return mAccessLogHelper.write((db) -> AccessLog.removeOfDate(db, date));
    }

    /**
     * 全てのアクセスログを削除します.
     */
    public void removeAll() {
        mAccessLogHelper.write(AccessLog::removeTable);
    }

    /**
     * Unix time を日付の文字列にして取得します.
     *
     * @param date Unix time
     * @return 日付の文字列
     */
    public static String dateToString(final long date) {
        return DateFormat.format("yyyy/MM/dd kk:mm:ss", date).toString();
    }

    /**
     * アクセスログを新規に作成します.
     *
     * @return 空のアクセスログ
     */
    public AccessLog createAccessLog() {
        AccessLog accessLog = new AccessLog();
        accessLog.setDate(getToday());
        return accessLog;
    }

    /**
     * 今日の日付の文字列を取得します.
     *
     * @return 今日の日付の文字列
     */
    private String getToday() {
        return DateFormat.format("yyyy/MM/dd", System.currentTimeMillis()).toString();
    }
}
