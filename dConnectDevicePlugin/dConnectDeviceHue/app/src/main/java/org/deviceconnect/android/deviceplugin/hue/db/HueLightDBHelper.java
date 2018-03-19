/*
HueLightDBHelper
Copyright (c) 2018 NTT DOCOMO,INC.
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
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.List;

/**
 * Hueのライト情報を格納するDBヘルパークラス.
 * @author NTT DOCOMO, INC.
 */
public class HueLightDBHelper {

    /**
     * Define the name of the database.
     */
    private static final String DB_NAME = "hue_light.db";

    /**
     * Define the version of the database.
     */
    private static final int DB_VERSION = 1;

    /**
     * ライトの情報を格納するテーブル名.
     */
    private static final String TBL_NAME = "light_info_tbl";

    /**
     * IPアドレスを格納するカラム名.
     */
    private static final String COL_BRIDGE_IP = "bridge_ip";

    /**
     * Light Idを格納するカラム.
     */
    private static final String COL_LIGHT_ID = "light_id";

    /**
     * ユーザ名を格納するカラム名.
     */
    private static final String COL_LIGHT_NAME = "light_name";
    /**
     * バージョンNOを格納するカラム名.
     */
    private static final String COL_VERSION_NUMBER = "version_number";

    /**
     * モデルNOを格納するカラム名.
     */
    private static final String COL_MODEL_NUMBER = "model_number";


    /**
     * DB管理ヘルパー.
     */
    private DBHelper mDBHelper;

    HueLightDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * ライトを追加します.
     * @param bridgeIp 追加するライト
     * @param light 追加するライト
     * @return 追加した行番号
     */
    synchronized long addLight(final String bridgeIp, final PHLight light) {
        ContentValues values = new ContentValues();
        values.put(COL_LIGHT_ID, light.getIdentifier());
        values.put(COL_LIGHT_NAME, light.getName());
        values.put(COL_VERSION_NUMBER, light.getVersionNumber());
        values.put(COL_MODEL_NUMBER, light.getModelNumber());
        values.put(COL_BRIDGE_IP, bridgeIp);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたLightIDと同じライトを削除します.
     * @param ipAddress 削除するライトのIPアドレス
     * @return 削除した個数
     */
    synchronized int removeLightByIpAddress(final String ipAddress) {
        String whereClause = COL_BRIDGE_IP + "=?";
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
     * 指定されたLightIdと同じライトを削除します.
     * @param lightId 削除するライトのLightId
     * @return 削除した個数
     */
    synchronized int removeLightByLightId(final String lightId) {
        String whereClause = COL_LIGHT_ID + "=?";
        String[] whereArgs = {
                lightId
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }
    /**
     * ライト一覧を取得します.
     * @param ip 取得するブリッジのIP
     * @return ライト
     */
    synchronized List<PHLight> getLightsForIp(final String ip) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_BRIDGE_IP + "=?";
        String[] selectionArgs = {ip};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            List<PHLight> lights = new ArrayList<>();
            boolean next = cursor.moveToFirst();
            while (next) {
                PHLight light = new PHLight(cursor.getString(cursor.getColumnIndex(COL_LIGHT_NAME)),
                        cursor.getString(cursor.getColumnIndex(COL_LIGHT_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_VERSION_NUMBER)),
                        cursor.getString(cursor.getColumnIndex(COL_MODEL_NUMBER)));
                lights.add(light);
                next = cursor.moveToNext();
            }
            return lights;
        } finally {
            cursor.close();
        }
    }

    /**
     * ライト情報を更新します.
     * @param light 更新するライト情報
     * @return 更新したライトの個数
     */
    synchronized long updateLight(final String bridgeIp, final PHLight light) {
        ContentValues values = new ContentValues();
        values.put(COL_LIGHT_ID, light.getIdentifier());
        values.put(COL_LIGHT_NAME, light.getName());
        values.put(COL_VERSION_NUMBER, light.getVersionNumber());
        values.put(COL_MODEL_NUMBER, light.getModelNumber());
        values.put(COL_BRIDGE_IP, bridgeIp);

        String whereClause = COL_LIGHT_ID + "=?";
        String[] whereArgs = {
                light.getIdentifier()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    synchronized PHLight getLightByLightId(final String lightId) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_LIGHT_ID + "=?";
        String[] whereArgs = {
                lightId
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, whereArgs);
        try {
            if (cursor.moveToFirst()) {
                PHLight light = new PHLight(cursor.getString(cursor.getColumnIndex(COL_LIGHT_NAME)),
                        cursor.getString(cursor.getColumnIndex(COL_LIGHT_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_VERSION_NUMBER)),
                        cursor.getString(cursor.getColumnIndex(COL_MODEL_NUMBER)));
                return light;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * 指定されたライトが格納されているかを確認します.
     * @param lightId 存在確認をするライトID
     * @return 存在する場合にはtrue、それ以外はfalse
     */
    synchronized boolean hasLight(final String bridgeIp, final String lightId) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_BRIDGE_IP + "=? AND " + COL_LIGHT_ID + "=?";
        String[] whereArgs = {
                bridgeIp, lightId
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
                    + COL_LIGHT_ID + " TEXT NOT NULL, "
                    + COL_LIGHT_NAME + " TEXT NOT NULL, "
                    + COL_VERSION_NUMBER + " TEXT NOT NULL, "
                    + COL_MODEL_NUMBER + " TEXT NOT NULL, "
                    + COL_BRIDGE_IP + " TEXT NOT NULL "
                    + ");";
            db.execSQL(sql);
        }
    }
}
