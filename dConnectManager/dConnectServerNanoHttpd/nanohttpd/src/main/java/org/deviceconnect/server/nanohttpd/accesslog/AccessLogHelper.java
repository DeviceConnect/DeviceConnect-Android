/*
 AccessLogHelper.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd.accesslog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * アクセスログのDBを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class AccessLogHelper extends SQLiteOpenHelper {
    /**
     * DBのファイル名を定義.
     */
    private static final String DATABASE_NAME = "DeviceConnectAccessLog.db";

    /**
     * DBのバージョンを定義.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    AccessLogHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AccessLog.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * 読み込み用のDBを開き、ハンドラを呼び出します.
     * <p>
     * 指定されたハンドラーから抜けた時点で、SQLiteDatabaseを閉じます。
     * </p>
     * @param handler DBの処理を行うハンドラ
     * @param <T> 処理結果
     * @return 処理結果
     */
    <T> T read(final DBHandler<T> handler) {
        T obj = null;
        try (SQLiteDatabase db = getReadableDatabase()) {
            obj = handler.process(db);
        } catch (Exception e) {
            // ignore.
        }
        return obj;
    }

    /**
     * 書き込みのDBを開き、ハンドラを呼び出します.
     * <p>
     * 指定されたハンドラーから抜けた時点で、SQLiteDatabaseを閉じます。
     * </p>
     * @param handler DBの処理を行うハンドラ
     * @return 処理結果
     */
    Boolean write(final DBHandler<Boolean> handler) {
        Boolean obj = Boolean.FALSE;
        try (SQLiteDatabase db = getWritableDatabase()) {
            obj = handler.process(db);
        } catch (Exception e) {
            // ignore.
        }
        return obj;
    }

    /**
     * DB の処理を行うインターフェース.
     *
     * @param <T>
     */
    interface DBHandler<T> {
        /**
         * SQLiteDatabase がオープンされた場合に処理を行う.
         *
         * @param db SQLiteDatabase
         * @return
         */
        T process(SQLiteDatabase db);
    }
}
