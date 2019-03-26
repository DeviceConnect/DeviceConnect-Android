/*
 AccessLogProvider.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd.accesslog;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     * DB処理を非同期に行うためのスレッド.
     */
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

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
     * 日付のリストを非同期に取得して、コールバックに通知します.
     *
     * @param callback 日付のリストを通知するコールバック
     */
    public void getDateList(final Callback<List<String>> callback) {
        mExecutorService.execute(() -> callback.onComplete(getDateList()));
    }

    /**
     * 指定された日付のアクセスログのリストを取得します.
     * <p>
     * 指定された日付のアクセスログが存在しない場合には空のリストを返却します。
     * </p>
     * @param date 日付
     * @return アクセスログのリスト
     */
    public List<AccessLog> getAccessLogsOfDate(final String date) {
        return mAccessLogHelper.read((db) -> AccessLog.getAccessLogsOfDate(db, date));
    }

    /**
     * 指定された日付のアクセスログのリストを非同期に取得して、コールバックに通知します.
     * <p>
     * 指定された日付のアクセスログが存在しない場合には空のリストを返却します。
     * </p>
     * @param date 日付
     * @param callback アクセスログのリストを通知するコールバック
     */
    public void getAccessLogsOfDate(final String date, final Callback<List<AccessLog>> callback) {
        mExecutorService.execute(() -> callback.onComplete(getAccessLogsOfDate(date)));
    }

    /**
     * 指定された日付の中から指定された IP アドレスもしくはパスが一致するアクセスログのリストを取得します.
     * <p>
     * 条件に合うアクセスログが存在しない場合には空のリストを返却します。
     * </p>
     * @param date 日付
     * @param condition 条件
     * @return アクセスログのリスト
     */
    public List<AccessLog> getAccessLogsFromCondition(final String date, final String condition) {
        return mAccessLogHelper.read((db) -> AccessLog.getAccessLogsFromCondition(db, date, condition));
    }

    /**
     * 指定された日付の中から指定された IP アドレスもしくはパスが一致するアクセスログのリストを非同期で取得して、コールバックに通知します.
     * <p>
     * 条件に合うアクセスログが存在しない場合には空のリストを返却します。
     * </p>
     * @param date 日付
     * @param condition 条件
     * @param callback アクセスログのリストを通知するコールバック
     */
    public void getAccessLogsFromCondition(final String date, final String condition, final Callback<List<AccessLog>> callback) {
        mExecutorService.execute(() -> callback.onComplete(getAccessLogsFromCondition(date, condition)));
    }

    /**
     * 指定された ID のアクセスログを取得します.
     * <p>
     * 指定された ID が存在しない場合は null を返却します。
     * </p>
     * @param id アクセスログのID
     * @return アクセスログ
     */
    public AccessLog getAccessLog(final long id) {
        return mAccessLogHelper.read((db) -> AccessLog.getAccessLog(db, id));
    }

    /**
     * 指定された ID のアクセスログを非同期に取得して、コールバックに通知します.
     * <p>
     * 指定された ID が存在しない場合は null を返却します。
     * </p>
     * @param id アクセスログのID
     * @param callback アクセスログを通知するコールバック
     */
    public void getAccessLog(final long id, final Callback<AccessLog> callback) {
        mExecutorService.execute(() -> callback.onComplete(getAccessLog(id)));
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
     * 非同期にアクセスログを追加して、結果をコールバックに通知します.
     *
     * @param accessLog 追加するアクセスログ
     * @param callback 追加に成功した場合にはtrue、それ以外はfalse
     */
    public void add(final AccessLog accessLog, final Callback<Boolean> callback) {
        mExecutorService.execute(() -> callback.onComplete(add(accessLog)));
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
     * 非同期にアクセスログを削除して、結果をコールバックに通知します.
     *
     * @param accessLog 削除するアクセスログ
     * @param callback 削除に成功した場合はtrue、それ以外はfalse
     */
    public void remove(final AccessLog accessLog, final Callback<Boolean> callback) {
        mExecutorService.execute(() -> callback.onComplete(remove(accessLog)));
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
     * 非同期に指定された日付のアクセスログを削除して、結果をコールバックに通知します.
     *
     * @param date 日付
     * @param callback 削除に成功した場合はtrue、それ以外はfalse
     */
    public void remove(final String date, final Callback<Boolean> callback) {
        mExecutorService.execute(() -> callback.onComplete(remove(date)));
    }

    /**
     * 全てのアクセスログを削除します.
     */
    public void removeAll() {
        mAccessLogHelper.write(AccessLog::removeTable);
    }

    /**
     * 非同期に全てのアクセスログを削除して、結果をコールバックに通知します.
     *
     * @param callback 結果を通知するコールバック
     */
    public void removeAll(final Callback<Boolean> callback) {
        mExecutorService.execute(() -> {
            removeAll();
            callback.onComplete(true);
        });
    }

    /**
     * Unix time を日付の文字列にして取得します.
     *
     * @param date Unix time
     * @return 日付の文字列
     */
    public static String dateToString(final long date) {
        // MEMO Android DateFormat ではミリ秒が表示できない
        return new java.text.SimpleDateFormat("yyyy/MM/dd kk:mm:ss.SSS", Locale.getDefault())
                .format(new Date(date));
//        return DateFormat.format("yyyy/MM/dd kk:mm:ss.SSS", date).toString();
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

    /**
     * アクセスログの処理結果を受け取るためのコールバック.
     *
     * @param <T>
     */
    public interface Callback<T> {
        /**
         * 処理の完了を通知します.
         *
         * @param value 処理結果
         */
        void onComplete(T value);
    }
}
