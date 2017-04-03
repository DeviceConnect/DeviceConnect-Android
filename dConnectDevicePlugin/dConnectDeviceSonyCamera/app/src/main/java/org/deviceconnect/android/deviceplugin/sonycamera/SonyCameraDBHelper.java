/*
SonyCameraDBHelper
Copyright (c) 2017 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.deviceconnect.android.deviceplugin.sonycamera.service.SonyCameraService;

import java.util.ArrayList;
import java.util.List;

/**
 * Sonyカメラを管理するDBヘルパークラス.
 * @author NTT DOCOMO, INC.
 */
class SonyCameraDBHelper {

    /**
     * Define the name of the database.
     */
    private static final String DB_NAME = "sony_camera.db";

    /**
     * Define the version of the database.
     */
    private static final int DB_VERSION = 1;

    /**
     * アクセスポイントの情報を格納するテーブル名.
     */
    private static final String TBL_NAME = "ssid_tbl";

    /**
     * ユーザ名を格納するカラム名.
     */
    private static final String COL_WIFI_SSID = "wifi_ssid";

    /**
     * ユーザ名を格納するカラム名.
     */
    private static final String COL_DEVICE_NAME = "device_name";

    /**
     * DB管理ヘルパー.
     */
    private DBHelper mDBHelper;

    SonyCameraDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    long addSonyCameraService(final SonyCameraService service) {
        ContentValues values = new ContentValues();
        values.put(COL_WIFI_SSID, service.getId());
        values.put(COL_DEVICE_NAME, service.getName());

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    long removeSonyCameraService(final SonyCameraService service) {
        String whereClause = COL_WIFI_SSID + "=?";
        String[] whereArgs = {
                service.getId()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    List<SonyCameraService> getSonyCameraServices() {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            List<SonyCameraService> services = new ArrayList<>();
            boolean next = cursor.moveToFirst();
            while (next) {
                String id = cursor.getString(cursor.getColumnIndex(COL_WIFI_SSID));
                String name = cursor.getString(cursor.getColumnIndex(COL_DEVICE_NAME));
                SonyCameraService service = new SonyCameraService(id);
                service.setName(name);
                services.add(service);
                next = cursor.moveToNext();
            }
            return services;
        } finally {
            cursor.close();
        }
    }

    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
            createDB(db);
        }

        private void createDB(final SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TBL_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                    + COL_WIFI_SSID + " TEXT NOT NULL, "
                    + COL_DEVICE_NAME + " TEXT NOT NULL "
                    + ");";
            db.execSQL(sql);
        }
    }
}
