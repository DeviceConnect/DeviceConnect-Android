/*
 AWSIotDBHelper.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manage a database.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDBHelper {

    /**
     * データベース名.
     */
    private static final String DB_NAME = "awsiot.db";

    /**
     * データベースバージョン.
     */
    private static final int DB_VERSION = 1;
    private static final String TBL_NAME = "manager_tbl";

    private static final String COL_SERVICE_ID = "service_id";
    private static final String COL_NAME = "name";
    private static final String COL_SUBSCRIBE_FLAG = "subscribe_flag";
    private static final String COL_UPDATEDATE_MILLIS = "update_date_millis";

    private DBHelper mDBHelper;

    public AWSIotDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * Add the manager in the database.
     *
     * @param manager RemoteDeviceConnectManager
     * @return the number of rows added
     */
    public synchronized long addManager(final RemoteDeviceConnectManager manager) {
        ContentValues values = new ContentValues();
        values.put(COL_SERVICE_ID, manager.getServiceId());
        values.put(COL_NAME, manager.getName());
        values.put(COL_SUBSCRIBE_FLAG, manager.isSubscribe() ? 1 : 0);
        values.put(COL_UPDATEDATE_MILLIS, System.currentTimeMillis());

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * Update the manager in the database.
     *
     * @param manager RemoteDeviceConnectManager
     * @return the number of rows updated
     */
    public synchronized int updateManager(final RemoteDeviceConnectManager manager) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, manager.getName());
        values.put(COL_SUBSCRIBE_FLAG, manager.isSubscribe() ? 1 : 0);
        values.put(COL_UPDATEDATE_MILLIS, System.currentTimeMillis());

        String whereClause = COL_SERVICE_ID + "=?";
        String[] whereArgs = {
                manager.getServiceId()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    public synchronized RemoteDeviceConnectManager findManagerById(final String id) {
        RemoteDeviceConnectManager manager;
        String SQL_SELECT = "SELECT * FROM " + TBL_NAME + " WHERE "
                + COL_SERVICE_ID + "=? " + ";";
        String[] whereArgs = {
                id
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT, whereArgs);
        if (cursor.moveToFirst()) {
            manager = new RemoteDeviceConnectManager(
                    cursor.getString(cursor.getColumnIndex(COL_NAME)),
                    cursor.getString(cursor.getColumnIndex(COL_SERVICE_ID)));
            manager.setSubscribeFlag(cursor.getInt(cursor.getColumnIndex(COL_SUBSCRIBE_FLAG)) == 1);
            cursor.close();
            db.close();
            return manager;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    /**
     * Delete the manager in the database.
     *
     * @param manager RemoteDeviceConnectManager
     * @return the number of rows deleted, 0 otherwise
     */
    public synchronized int removeManager(final RemoteDeviceConnectManager manager) {
        String whereClause = COL_SERVICE_ID + "=?";
        String[] whereArgs = {
                manager.getServiceId()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * Get a list of information in the database.
     *
     * @return a list of information
     */
    public synchronized List<RemoteDeviceConnectManager> getManagers() {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<RemoteDeviceConnectManager> managers = new ArrayList<>();
        boolean next = cursor.moveToFirst();
        while (next) {
            RemoteDeviceConnectManager manager =
                    new RemoteDeviceConnectManager(
                            cursor.getString(cursor.getColumnIndex(COL_NAME)),
                            cursor.getString(cursor.getColumnIndex(COL_SERVICE_ID)));
            manager.setSubscribeFlag(cursor.getInt(cursor.getColumnIndex(COL_SUBSCRIBE_FLAG)) == 1);
            managers.add(manager);
            next = cursor.moveToNext();
        }
        cursor.close();
        return managers;
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
                    + COL_SERVICE_ID + " TEXT NOT NULL, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_SUBSCRIBE_FLAG + " INTEGER, "
                    + COL_UPDATEDATE_MILLIS + " INTEGER"
                    + ");";
            db.execSQL(sql);
        }
    }
}
