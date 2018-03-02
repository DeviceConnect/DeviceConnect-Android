/*
HueDBHelper
Copyright (c) 2017 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.hue.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.philips.lighting.hue.sdk.PHAccessPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Hueアクセスポイント情報を格納するDBヘルパークラス.
 * @author NTT DOCOMO, INC.
 */
public class HueDBHelper {

    /**
     * Define the name of the database.
     */
    private static final String DB_NAME = "hue_bridge.db";

    /**
     * Define the version of the database.
     */
    private static final int DB_VERSION = 1;

    /**
     * アクセスポイントの情報を格納するテーブル名.
     */
    private static final String TBL_NAME = "access_point_tbl";

    /**
     * ユーザ名を格納するカラム名.
     */
    private static final String COL_USER_NAME = "user_name";

    /**
     * IPアドレスを格納するカラム名.
     */
    private static final String COL_IP_ADDRESS = "ip_address";

    /**
     * Macアドレスを格納するカラム.
     */
    private static final String COL_MAC_ADDRESS = "mac_address";

    /**
     * DB管理ヘルパー.
     */
    private DBHelper mDBHelper;

    HueDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * アクセスポイントを追加します.
     * @param accessPoint 追加するアクセスポイント
     * @return 追加した行番号
     */
    synchronized long addAccessPoint(final PHAccessPoint accessPoint) {
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, accessPoint.getUsername());
        values.put(COL_IP_ADDRESS, accessPoint.getIpAddress());
        values.put(COL_MAC_ADDRESS, accessPoint.getMacAddress());

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたIPアドレスと同じアクセスポイントを削除します.
     * @param ipAddress 削除するアクセスポイントのIPアドレス
     * @return 削除した個数
     */
    synchronized int removeAccessPointByIpAddress(final String ipAddress) {
        String whereClause = COL_IP_ADDRESS + "=?";
        String[] whereArgs = {
                ipAddress
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * アクセスポイント一覧を取得します.
     * @return アクセスポイント
     */
    synchronized List<PHAccessPoint> getAccessPoints() {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            List<PHAccessPoint> accessPoints = new ArrayList<PHAccessPoint>();
            boolean next = cursor.moveToFirst();
            while (next) {
                PHAccessPoint accessPoint = new PHAccessPoint();
                accessPoint.setUsername(cursor.getString(cursor.getColumnIndex(COL_USER_NAME)));
                accessPoint.setIpAddress(cursor.getString(cursor.getColumnIndex(COL_IP_ADDRESS)));
                accessPoint.setMacAddress(cursor.getString(cursor.getColumnIndex(COL_MAC_ADDRESS)));
                accessPoints.add(accessPoint);
                next = cursor.moveToNext();
            }
            return accessPoints;
        } finally {
            cursor.close();
        }
    }

    /**
     * アクセスポイント情報を更新します.
     * @param accessPoint 更新するアクセスポイント情報
     * @return 更新したアクセスポイントの個数
     */
    synchronized long updateAccessPoint(final PHAccessPoint accessPoint) {
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, accessPoint.getUsername());
        values.put(COL_IP_ADDRESS, accessPoint.getIpAddress());

        String whereClause = COL_MAC_ADDRESS + "=?";
        String[] whereArgs = {
                accessPoint.getMacAddress()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    synchronized PHAccessPoint getAccessPointByMacAddress(final String macAddress) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_MAC_ADDRESS + "=?";
        String[] whereArgs = {
                macAddress
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, whereArgs);
        try {
            if (cursor.moveToFirst()) {
                PHAccessPoint accessPoint = new PHAccessPoint();
                accessPoint.setUsername(cursor.getString(cursor.getColumnIndex(COL_USER_NAME)));
                accessPoint.setIpAddress(cursor.getString(cursor.getColumnIndex(COL_IP_ADDRESS)));
                accessPoint.setMacAddress(cursor.getString(cursor.getColumnIndex(COL_MAC_ADDRESS)));
                return accessPoint;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * 指定されたアクセスポイントが格納されているかを確認します.
     * @param accessPoint 存在確認をするアクセスポイント
     * @return 存在する場合にはtrue、それ以外はfalse
     */
    synchronized boolean hasAccessPoint(final PHAccessPoint accessPoint) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_MAC_ADDRESS + "=?";
        String[] whereArgs = {
                accessPoint.getMacAddress()
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, whereArgs);
        try {
            return cursor.moveToFirst();
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
                    + COL_USER_NAME + " TEXT NOT NULL, "
                    + COL_IP_ADDRESS + " TEXT NOT NULL, "
                    + COL_MAC_ADDRESS + " TEXT NOT NULL "
                    + ");";
            db.execSQL(sql);
        }
    }
}
