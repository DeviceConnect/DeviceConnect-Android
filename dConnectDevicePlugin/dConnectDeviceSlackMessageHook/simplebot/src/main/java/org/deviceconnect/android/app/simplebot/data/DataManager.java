/*
 DataManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.deviceconnect.android.app.simplebot.BuildConfig;

/**
 * データ管理クラス
 */
public class DataManager {

    /** デバッグタグ */
    private static final String TAG = "DataManager";

    /** DBのバージョン */
    private static final int DB_VERSION = 4;
    /** DBファイル名 */
    public static final String DB_NAME = "simplebot.db";
    /** テーブル名 */
    public static final String TABLE_NAME = "command";

    /** idのカラム名 */
    public static final String COLUMN_ID = "_id";
    /** keywordのカラム名 */
    public static final String COLUMN_KEYWORD = "keyword";
    /** serviceIdのカラム名 */
    public static final String COLUMN_SERVICE_ID = "serviceId";
    /** serviceNameのカラム名 */
    public static final String COLUMN_SERVICE_NAME = "serviceName";
    /** apiのカラム名 */
    public static final String COLUMN_API = "api";
    /** methodのカラム名 */
    public static final String COLUMN_METHOD = "method";
    /** pathのカラム名 */
    public static final String COLUMN_PATH = "path";
    /** bodyのカラム名 */
    public static final String COLUMN_BODY = "body";
    /** acceptのカラム名 */
    public static final String COLUMN_ACCEPT = "accept";
    /** accept_uriのカラム名 */
    public static final String COLUMN_ACCEPT_URI = "accept_uri";
    /** successのカラム名 */
    public static final String COLUMN_SUCCESS = "success";
    /** success_uriのカラム名 */
    public static final String COLUMN_SUCCESS_URI = "success_uri";
    /** errorのカラム名 */
    public static final String COLUMN_ERROR = "error";
    /** error_uriのカラム名 */
    public static final String COLUMN_ERROR_URI = "error_uri";

    /** 全カラム */
    public static final String[] COLUMNS = {
            COLUMN_ID,
            COLUMN_KEYWORD, // キーワード
            COLUMN_SERVICE_ID, // サービスID
            COLUMN_SERVICE_NAME, // サービス名
            COLUMN_API, // API
            COLUMN_METHOD, // メソッド
            COLUMN_PATH, // パス
            COLUMN_BODY, // ボディ
            COLUMN_ACCEPT, // 受付レスポンス
            COLUMN_ACCEPT_URI, // 受付レスポンスリソースURI
            COLUMN_SUCCESS, // 成功レスポンス
            COLUMN_SUCCESS_URI, // 成功レスポンスリソースURI
            COLUMN_ERROR, // 失敗レスポンス
            COLUMN_ERROR_URI // 失敗レスポンスリソースURI
    };

    /** SQLiteHelper */
    private SQLiteHelper sql;

    /**
     * データクラス
     */
    public static class Data {
        public long id = -1;
        public String keyword;
        public String serviceId;
        public String serviceName;
        public String api;
        public String method;
        public String path;
        public String body;
        public String accept;
        public String acceptUri;
        public String success;
        public String successUri;
        public String error;
        public String errorUri;

        @Override
        public String toString() {
            return "Data{" +
                    "id=" + id +
                    ", keyword='" + keyword + '\'' +
                    ", serviceId='" + serviceId + '\'' +
                    ", serviceName='" + serviceName + '\'' +
                    ", api='" + api + '\'' +
                    ", method='" + method + '\'' +
                    ", path='" + path + '\'' +
                    ", body='" + body + '\'' +
                    ", accept='" + accept + '\'' +
                    ", acceptUri='" + acceptUri + '\'' +
                    ", success='" + success + '\'' +
                    ", successUri='" + successUri + '\'' +
                    ", error='" + error + '\'' +
                    ", errorUri='" + errorUri + '\'' +
                    '}';
        }
    }

    /**
     * SQLHelper
     */
    private class SQLiteHelper extends SQLiteOpenHelper {

        /**
         * context指定で初期化
         * @param context コンテキスト
         */
        public SQLiteHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder sb = new StringBuilder();
            sb.append("create table ");
            sb.append(TABLE_NAME);
            sb.append(" (");
            sb.append(COLUMN_ID);
            sb.append(" integer PRIMARY KEY AUTOINCREMENT");
            for (String col: COLUMNS) {
                if (col.equals(COLUMN_ID)) {
                    continue;
                } else {
                    sb.append(", ");
                }
                sb.append(col);
                sb.append(" text");
            }
            sb.append(");");
            if (BuildConfig.DEBUG) Log.d(TAG,"onCreate:" + sb.toString());
            db.execSQL(sb.toString());
            addSampleData(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    /**
     * context指定で初期化
     * @param context コンテキスト
     */
    public DataManager(Context context) {
        sql = new SQLiteHelper(context);
    }

    /**
     * データを登録
     * @param db DB
     * @param data データ
     * @return trueで成功
     */
    public boolean upsert(SQLiteDatabase db, Data data) {
        if (BuildConfig.DEBUG) Log.d(TAG,"upsert:" + data.toString());
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEYWORD, data.keyword);
        values.put(COLUMN_SERVICE_ID, data.serviceId);
        values.put(COLUMN_SERVICE_NAME, data.serviceName);
        values.put(COLUMN_API, data.api);
        values.put(COLUMN_METHOD, data.method);
        values.put(COLUMN_PATH, data.path);
        values.put(COLUMN_BODY, data.body);
        values.put(COLUMN_ACCEPT, data.accept);
        values.put(COLUMN_ACCEPT_URI, data.acceptUri);
        values.put(COLUMN_SUCCESS, data.success);
        values.put(COLUMN_SUCCESS_URI, data.successUri);
        values.put(COLUMN_ERROR, data.error);
        values.put(COLUMN_ERROR_URI, data.errorUri);

        long ret;
        if (data.id < 0) {
            ret = db.insert(TABLE_NAME, null, values);
        } else {
            ret = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(data.id)});
        }
        return ret > 0;
    }

    /**
     * データを登録
     * @param data データ
     * @return trueで成功
     */
    public boolean upsert(Data data) {
        SQLiteDatabase db = sql.getWritableDatabase();
        return upsert(db, data);
    }

    /**
     * データを削除
     * @param id ID
     * @return trueで成功
     */
    public boolean delete(long id) {
        if (BuildConfig.DEBUG) Log.d(TAG,"upsert:" + String.valueOf(id));
        SQLiteDatabase db = sql.getWritableDatabase();
        long ret = db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return ret > 0;
    }

    /**
     * 全てのデータを返すカーソル取得
     * @return カーソル
     */
    public Cursor getAll() {
        if (BuildConfig.DEBUG) Log.d(TAG,"getAll");
        SQLiteDatabase db = sql.getReadableDatabase();
        return db.query(TABLE_NAME, COLUMNS, null, null, null, null, COLUMN_ID + " desc");
    }

    /**
     * ID指定でデータを取得
     * @param id ID
     * @return データ
     */
    public Data getData(long id) {
        if (BuildConfig.DEBUG) Log.d(TAG,"getData:" + id);
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, COLUMN_ID + " desc");
        if (cursor.moveToFirst()) {
            Data data = convertData(cursor);
            cursor.close();
            return data;
        } else {
            cursor.close();
            return null;
        }
    }

    /**
     * SQLiteのカーソルから情報を取得
     * @param cursor カーソル
     * @return データ
     */
    public Data convertData(Cursor cursor) {
        Data data = new Data();
        data.id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        data.keyword = cursor.getString(cursor.getColumnIndex(COLUMN_KEYWORD));
        data.serviceId = cursor.getString(cursor.getColumnIndex(COLUMN_SERVICE_ID));
        data.serviceName = cursor.getString(cursor.getColumnIndex(COLUMN_SERVICE_NAME));
        data.api = cursor.getString(cursor.getColumnIndex(COLUMN_API));
        data.method = cursor.getString(cursor.getColumnIndex(COLUMN_METHOD));
        data.path = cursor.getString(cursor.getColumnIndex(COLUMN_PATH));
        data.body = cursor.getString(cursor.getColumnIndex(COLUMN_BODY));
        data.accept = cursor.getString(cursor.getColumnIndex(COLUMN_ACCEPT));
        data.acceptUri = cursor.getString(cursor.getColumnIndex(COLUMN_ACCEPT_URI));
        data.success = cursor.getString(cursor.getColumnIndex(COLUMN_SUCCESS));
        data.successUri = cursor.getString(cursor.getColumnIndex(COLUMN_SUCCESS_URI));
        data.error = cursor.getString(cursor.getColumnIndex(COLUMN_ERROR));
        data.errorUri = cursor.getString(cursor.getColumnIndex(COLUMN_ERROR_URI));
        return data;
    }

    /**
     * CSVのデータから情報を取得
     * @param csv 文字列
     * @return データ
     */
    public Data convertData(String[] csv) {
        if (csv == null || csv.length != 13) {
            return null;
        }
        Data data = new Data();
        int index = 0;
        data.keyword = csv[index++];
        data.serviceId = csv[index++];
        data.serviceName = csv[index++];
        data.api = csv[index++];
        data.method = csv[index++];
        data.path = csv[index++];
        data.body = csv[index++];
        data.accept = csv[index++];
        data.acceptUri = csv[index++];
        data.success = csv[index++];
        data.successUri = csv[index++];
        data.error = csv[index++];
        data.errorUri = csv[index];
        return data;
    }

    /**
     * サンプルデータを追加
     */
    private void addSampleData(SQLiteDatabase db) {
        // 音楽リスト
        Data data = new Data();
        data.keyword = "音楽(リスト|一覧)";
        data.path = "/gotapi/mediaPlayer/mediaList";
        data.api = "mediaPlayer/mediaList [GET]";
        data.method = "GET";
        data.body = null;
        data.success = "{%loop in $media as $m}[{$m.mediaId}:{$m.title}]{% endloop %}";
        data.error = "エラーです。 {$errorMessage}";
        data.serviceId = "Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org";
        data.serviceName = "Host";
        upsert(db, data);
        // 音楽設定
        data.keyword = "(\\d+)を設定";
        data.path = "/gotapi/mediaPlayer/media";
        data.api = "mediaPlayer/media [PUT]";
        data.method = "PUT";
        data.body = "{\"mediaId\":\"$1\"}";
        data.success = "設定しました。";
        upsert(db, data);
        // 再生
        data.keyword = "再生";
        data.path = "/gotapi/mediaPlayer/play";
        data.api = "mediaPlayer/play [PUT]";
        data.method = "PUT";
        data.body = null;
        data.success = "再生しました。";
        upsert(db, data);
        // 停止
        data.keyword = "停止";
        data.path = "/gotapi/mediaPlayer/stop";
        data.api = "mediaPlayer/stop [PUT]";
        data.method = "PUT";
        data.success = "停止しました。";
        upsert(db, data);
        // バッテリー残量
        data.keyword = "バッテリー|電池";
        data.path = "/gotapi/battery/level";
        data.api = "battery/level [GET]";
        data.method = "GET";
        data.success = "残り{$level|calc(*100)}%です。";
        upsert(db, data);
        // 写真
        data.keyword = "写真";
        data.path = "/gotapi/mediaStreamRecording/takePhoto";
        data.api = "mediaStreamRecording/takePhoto [POST]";
        data.method = "POST";
        data.accept = "撮影します、しばらくお待ちください。";
        data.success = "撮影しました。";
        data.successUri = "{$uri}";
        upsert(db, data);
    }
}
