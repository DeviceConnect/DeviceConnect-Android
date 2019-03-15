/*
 AccessLog.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd.accesslog;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * アクセスログ.
 *
 * @author NTT DOCOMO, INC.
 */
public class AccessLog {
    /**
     * アクセスログのテーブル名.
     */
    private static final String TABLE_NAME = "accessLog";

    private long mId = -1;
    private String mDate;
    private String mRemoteIpAddress;
    private String mRemoteHostName;
    private long mRequestReceivedTime;
    private String mRequestHeader;
    private String mRequestMethod;
    private String mRequestPath;
    private String mRequestBody;
    private long mResponseSendTime;
    private int mResponseStatusCode;
    private String mResponseContentType;
    private String mResponseBody;

    AccessLog() {
    }

    /**
     * アクセスログのIDを取得します.
     *
     * @return アクセスログのID
     */
    public long getId() {
        return mId;
    }

    /**
     * ログの日付の文字列を取得します.
     *
     * @return ログの日付
     */
    public String getDate() {
        return mDate;
    }

    /**
     * リモートの IP アドレスを取得します.
     *
     * @return リモートのIPアドレス
     */
    public String getRemoteIpAddress() {
        return mRemoteIpAddress;
    }

    /**
     * リモートのホスト名を取得します.
     *
     * @return リモートのホスト名
     */
    public String getRemoteHostName() {
        return mRemoteHostName;
    }

    /**
     * リクエストの受信時間を取得します.
     *
     * @return リクエストの受信時間
     */
    public long getRequestReceivedTime() {
        return mRequestReceivedTime;
    }

    /**
     * リクエストのヘッダーを取得します.
     *
     * @return リクエストのヘッダー
     */
    public Map<String, String> getRequestHeader() {
        return stringToHeader(mRequestHeader);
    }

    /**
     * リクエストのメソッドを取得します.
     *
     * @return リクエストのメソッド
     */
    public String getRequestMethod() {
        return mRequestMethod;
    }

    /**
     * リクエストのパスを取得します.
     *
     * @return リクエストのパス
     */
    public String getRequestPath() {
        return mRequestPath;
    }

    /**
     * リクエストのボディを取得します.
     *
     * @return リクエストのボディ
     */
    public String getRequestBody() {
        return mRequestBody;
    }

    /**
     * レスポンスの送信時間を取得します.
     *
     * @return レスポンスの送信時間
     */
    public long getResponseSendTime() {
        return mResponseSendTime;
    }

    /**
     * レスポンスのステータスコードを取得します.
     *
     * @return レスポンスのステータスコード
     */
    public int getResponseStatusCode() {
        return mResponseStatusCode;
    }

    /**
     * レスポンスのコンテンツタイプを取得します.
     *
     * @return レスポンスのコンテンツタイプ
     */
    public String getResponseContentType() {
        return mResponseContentType;
    }

    /**
     * レスポンスのボディを取得します.
     *
     * @return レスポンスのボディ
     */
    public String getResponseBody() {
        return mResponseBody;
    }

    /**
     * アクセスログが属する日付を設定します.
     *
     * @param date 日付
     */
    public void setDate(String date) {
        mDate = date;
    }

    /**
     * リクエスト先の IP アドレスを設定します.
     *
     * @param remoteIpAddress IDアドレス
     */
    public void setRemoteIpAddress(String remoteIpAddress) {
        mRemoteIpAddress = remoteIpAddress;
    }

    /**
     * リクエスト先のホスト名を設定します.
     *
     * @param remoteHostName ホスト名
     */
    public void setRemoteHostName(String remoteHostName) {
        mRemoteHostName = remoteHostName;
    }

    /**
     * リクエストの受信時間を設定します.
     *
     * @param requestReceivedTime リクエストの受信時間(Unix time)
     */
    public void setRequestReceivedTime(long requestReceivedTime) {
        mRequestReceivedTime = requestReceivedTime;
    }

    /**
     * リクエストのヘッダーを設定します.
     *
     * @param header ヘッダー
     */
    public void setRequestHeader(Map<String, String> header) {
        mRequestHeader = headerToString(header);
    }

    /**
     * リクエストのメソッドを設定します.
     *
     * @param requestMethod リクエストのメソッド
     */
    public void setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
    }

    /**
     * リクエストのパスを設定します.
     *
     * @param requestPath リクエストのパス
     */
    public void setRequestPath(String requestPath) {
        mRequestPath = requestPath;
    }

    /**
     * リクエストのボディを設定します.
     *
     * @param requestBody リクエストのボディ
     */
    public void setRequestBody(String requestBody) {
        mRequestBody = requestBody;
    }

    /**
     * レスポンスの送信時間を設定します.
     *
     * @param responseSendTime レスポンスの送信時間
     */
    public void setResponseSendTime(long responseSendTime) {
        mResponseSendTime = responseSendTime;
    }

    /**
     * レスポンスのステータスコードを設定します.
     *
     * @param responseStatusCode レスポンスのステータスコード
     */
    public void setResponseStatusCode(int responseStatusCode) {
        mResponseStatusCode = responseStatusCode;
    }

    /**
     * レスポンスのコンテントタイプを設定します.
     *
     * @param responseContentType レスポンスのコンテントタイプ
     */
    public void setResponseContentType(String responseContentType) {
        mResponseContentType = responseContentType;
    }

    /**
     * レスポンスのボディを設定します.
     *
     * @param responseBody レスポンスのボディ
     */
    public void setResponseBody(String responseBody) {
        mResponseBody = responseBody;
    }

    @Override
    public String toString() {
        return "AccessLog{" +
                "mId=" + mId +
                ", mDate='" + mDate + '\'' +
                ", mRemoteIpAddress='" + mRemoteIpAddress + '\'' +
                ", mRemoteHostName='" + mRemoteHostName + '\'' +
                ", mRequestReceivedTime=" + mRequestReceivedTime +
                ", mRequestHeader='" + mRequestHeader + '\'' +
                ", mRequestMethod='" + mRequestMethod + '\'' +
                ", mRequestPath='" + mRequestPath + '\'' +
                ", mRequestBody='" + mRequestBody + '\'' +
                ", mResponseSendTime=" + mResponseSendTime +
                ", mResponseStatusCode=" + mResponseStatusCode +
                ", mResponseContentType='" + mResponseContentType + '\'' +
                ", mResponseBody='" + mResponseBody + '\'' +
                '}';
    }

    /**
     * アクセスログ用のテーブルを作成します.
     *
     * @param db SQLiteDatabaseのインスタンス
     */
    static void createTable(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + AccessLogColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AccessLogColumns.DATE + " TEXT,"
                + AccessLogColumns.REQUEST_IP_ADDRESS + " TEXT,"
                + AccessLogColumns.REQUEST_HOST_NAME + " TEXT,"
                + AccessLogColumns.REQUEST_RECEIVED_TIME + " INTEGER,"
                + AccessLogColumns.REQUEST_HEADER + " TEXT,"
                + AccessLogColumns.REQUEST_METHOD + " TEXT,"
                + AccessLogColumns.REQUEST_PATH + " TEXT,"
                + AccessLogColumns.REQUEST_BODY + " TEXT,"
                + AccessLogColumns.RESPONSE_SEND_TIME + " INTEGER,"
                + AccessLogColumns.RESPONSE_STATUS_CODE + " INTEGER,"
                + AccessLogColumns.RESPONSE_CONTENT_TYPE + " TEXT,"
                + AccessLogColumns.RESPONSE_BODY + " TEXT"
                + ");");
    }

    /**
     * アクセスログのテーブルを削除します.
     *
     * @param db SQLiteDatabaseのインスタンス
     * @return 常にtrueを返します.
     */
    static boolean removeTable(final SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        createTable(db);
        return true;
    }

    /**
     * アクセスログをテーブルに追加します.
     *
     * @param db SQLiteDatabaseのインスタンス
     * @param accessLog 追加するアクセスログ
     * @return 追加に成功した場合はtrue、それ以外はfalse
     */
    static boolean add(final SQLiteDatabase db, final AccessLog accessLog) {
        ContentValues values = new ContentValues();
        values.put(AccessLogColumns.DATE, accessLog.mDate);
        values.put(AccessLogColumns.REQUEST_IP_ADDRESS, accessLog.mRemoteIpAddress);
        values.put(AccessLogColumns.REQUEST_HOST_NAME, accessLog.mRemoteHostName);
        values.put(AccessLogColumns.REQUEST_RECEIVED_TIME, accessLog.mRequestReceivedTime);
        values.put(AccessLogColumns.REQUEST_HEADER, accessLog.mRequestHeader);
        values.put(AccessLogColumns.REQUEST_METHOD, accessLog.mRequestMethod);
        values.put(AccessLogColumns.REQUEST_PATH, accessLog.mRequestPath);
        values.put(AccessLogColumns.REQUEST_BODY, accessLog.mRequestBody);
        values.put(AccessLogColumns.RESPONSE_SEND_TIME, accessLog.mResponseSendTime);
        values.put(AccessLogColumns.RESPONSE_STATUS_CODE, accessLog.mResponseStatusCode);
        values.put(AccessLogColumns.RESPONSE_CONTENT_TYPE, accessLog.mResponseContentType);
        values.put(AccessLogColumns.RESPONSE_BODY, accessLog.mResponseBody);

        try {
            return db.insertOrThrow(TABLE_NAME, null, values) != -1;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 指定されたアクセスログを削除します.
     *
     * @param db SQLiteDatabaseのインスタンス
     * @param accessLog 削除するアクセスログ
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    static boolean remove(final SQLiteDatabase db, final AccessLog accessLog) {
        if (accessLog.getId() == -1) {
            return false;
        }
        String select = AccessLogColumns._ID + "=" + accessLog.getId();
        return db.delete(TABLE_NAME, select, null) > 0;
    }

    /**
     * 指定された日付のアクセスログを削除します.
     *
     * @param db SQLiteDatabaseのインスタンス
     * @param date 日付
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    static boolean removeOfDate(final SQLiteDatabase db, final String date) {
        String whereClause = AccessLogColumns.DATE + "=?";
        String[] whereArgs = {date};
        return db.delete(TABLE_NAME, whereClause, whereArgs) > 0;
    }

    /**
     * 日付のリストを取得します.
     *
     * @param db SQLiteDatabaseのインスタンス
     * @return 日付のリスト
     */
    static List<String> getDateList(final SQLiteDatabase db) {
        List<String> list = new ArrayList<>();

        String[] columns = {AccessLogColumns.DATE};
        String orderBy = AccessLogColumns.DATE + " DESC";

        Cursor cs = db.query(true, TABLE_NAME, columns,
                null, null, AccessLogColumns.DATE,
                null, orderBy, null) ;
        if (cs != null) {
            try {
                while (cs.moveToNext()) {
                    list.add(cs.getString(cs.getColumnIndex(AccessLogColumns.DATE)));
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                cs.close();
            }
        }
        return list;
    }

    /**
     * アクセスログのリストを取得します.
     * <p>
     * アクセスログがない場合には空のリストを返却します。
     * </p>
     * @param db SQLiteDatabaseのインスタンス
     * @param date 取得する日付
     * @return アクセスログのリスト。
     */
    static List<AccessLog> getAccessLogsOfDate(final SQLiteDatabase db, final String date) {
        List<AccessLog> list = new ArrayList<>();

        String selection = AccessLogColumns.DATE + "=?";
        String[] selectionArgs = {date};
        String orderBy = AccessLogColumns.REQUEST_RECEIVED_TIME + " DESC";

        Cursor cs = db.query(TABLE_NAME, null, selection, selectionArgs,
                null, null, orderBy);
        if (cs != null) {
            try {
                while (cs.moveToNext()) {
                    list.add(createAccessLog(cs));
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                cs.close();
            }
        }
        return list;
    }

    /**
     * 指定された IP アドレスもしくはパスが一致するアクセスログのリストを取得します.
     * <p>
     * 条件にアクセスログがない場合には空のリストを返却します。
     * </p>
     * @param db SQLiteDatabaseのインスタンス
     * @param date 取得する日付
     * @param condition 条件
     * @return アクセスログのリスト。
     */
    static List<AccessLog> getAccessLogsFromCondition(final SQLiteDatabase db, final String date, final String condition) {
        List<AccessLog> list = new ArrayList<>();

        String selection = AccessLogColumns.DATE + "=? AND (" + AccessLogColumns.REQUEST_IP_ADDRESS + " LIKE ? OR " + AccessLogColumns.REQUEST_PATH + " LIKE ?)";
        String[] selectionArgs = {date, "%" + condition + "%", "%" + condition + "%"};
        String orderBy = AccessLogColumns.REQUEST_RECEIVED_TIME + " DESC";

        Cursor cs = db.query(TABLE_NAME, null, selection, selectionArgs,
                null, null, orderBy);
        if (cs != null) {
            try {
                while (cs.moveToNext()) {
                    list.add(createAccessLog(cs));
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                cs.close();
            }
        }
        return list;
    }

    /**
     * アクセスログを取得します.
     * <p>
     * アクセスログがない場合には空のListを返却します。
     * </p>
     * @param db SQLiteDatabaseのインスタンス
     * @param id 取得するアクセスログのID
     * @return アクセスログ。
     */
    static AccessLog getAccessLog(final SQLiteDatabase db, final long id) {
        String selection = AccessLogColumns._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cs = db.query(TABLE_NAME, null, selection, selectionArgs,
                null, null, null);
        if (cs != null) {
            try {
                if (cs.moveToNext()) {
                    return createAccessLog(cs);
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                cs.close();
            }
        }
        return null;
    }

    /**
     * Cursor から AccessLog を作成します.
     *
     * @param cs Cursor
     * @return AccessLog
     */
    private static AccessLog createAccessLog(final Cursor cs) {
        AccessLog accessLog = new AccessLog();
        accessLog.mId = cs.getLong(cs.getColumnIndex(AccessLogColumns._ID));
        accessLog.mDate = cs.getString(cs.getColumnIndex(AccessLogColumns.DATE));
        accessLog.mRemoteIpAddress = cs.getString(cs.getColumnIndex(AccessLogColumns.REQUEST_IP_ADDRESS));
        accessLog.mRemoteHostName = cs.getString(cs.getColumnIndex(AccessLogColumns.REQUEST_HOST_NAME));
        accessLog.mRequestReceivedTime = cs.getLong(cs.getColumnIndex(AccessLogColumns.REQUEST_RECEIVED_TIME));
        accessLog.mRequestHeader = cs.getString(cs.getColumnIndex(AccessLogColumns.REQUEST_HEADER));
        accessLog.mRequestMethod = cs.getString(cs.getColumnIndex(AccessLogColumns.REQUEST_METHOD));
        accessLog.mRequestPath = cs.getString(cs.getColumnIndex(AccessLogColumns.REQUEST_PATH));
        accessLog.mRequestBody = cs.getString(cs.getColumnIndex(AccessLogColumns.REQUEST_BODY));
        accessLog.mResponseSendTime = cs.getLong(cs.getColumnIndex(AccessLogColumns.RESPONSE_SEND_TIME));
        accessLog.mResponseStatusCode = cs.getInt(cs.getColumnIndex(AccessLogColumns.RESPONSE_STATUS_CODE));
        accessLog.mResponseContentType = cs.getString(cs.getColumnIndex(AccessLogColumns.RESPONSE_CONTENT_TYPE));
        accessLog.mResponseBody = cs.getString(cs.getColumnIndex(AccessLogColumns.RESPONSE_BODY));
        return accessLog;
    }

    /**
     * Header の文字列を Map に変換します.
     *
     * @param string Header の文字列
     * @return Map
     */
    private static Map<String, String> stringToHeader(String string) {
        Map<String, String> header = new HashMap<>();
        if (string != null) {
            String[] p = string.split("\t");
            for (String a : p) {
                String[] keyValue = a.split("=");
                header.put(keyValue[0], keyValue[1]);
            }
        }
        return header;
    }

    /**
     * Header の Map を文字列に変換します.
     *
     * @param header HeaderのMap
     * @return 文字列
     */
    private static String headerToString(Map<String, String> header) {
        StringBuilder h = new StringBuilder();
        for (String key : header.keySet()) {
            String value = header.get(key);
            if (h.length() > 0) {
                h.append("\t");
            }
            h.append(key).append("=").append(value);
        }
        return h.toString();
    }

    /**
     * カラム名を定義するためのクラス.
     *
     * @author NTT DOCOMO, INC.
     */
    private interface AccessLogColumns extends BaseColumns {
        /**
         * アクセスログの日付.
         */
        String DATE = "date";

        /**
         * リクエスト元のIPアドレス.
         */
        String REQUEST_IP_ADDRESS = "request_ip_address";

        /**
         * リクエスト元のホスト名.
         */
        String REQUEST_HOST_NAME = "request_host_name";

        /**
         * リクエスト受信時刻.
         */
        String REQUEST_RECEIVED_TIME = "request_received_time";

        /**
         * リクエストのHTTPヘッダー.
         */
        String REQUEST_HEADER = "request_header";

        /**
         * リクエストのHTTPメソッド.
         */
        String REQUEST_METHOD = "request_method";

        /**
         * リクエストのパス.
         */
        String REQUEST_PATH = "request_path";

        /**
         * リクエストのボディ.
         */
        String REQUEST_BODY = "request_body";

        /**
         * レスポンス送信時刻.
         */
        String RESPONSE_SEND_TIME = "response_send_time";

        /**
         * レスポンスのステータスコード.
         */
        String RESPONSE_STATUS_CODE = "response_status_code";

        /**
         * レスポンスのコンテントタイプ.
         */
        String RESPONSE_CONTENT_TYPE = "response_content_type";

        /**
         * レスポンスのボディ.
         */
        String RESPONSE_BODY = "response_body";
    }
}
