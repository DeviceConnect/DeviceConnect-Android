/*
 HitoeDBHelper
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

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
 * @author NTT DOCOMO, INC.
 */
public class HitoeDBHelper {

    /**
     * Define the name of the database.
     */
    private static final String DB_NAME = "hitoe_device.db";

    /**
     * Define the version of the database.
     */
    private static final int DB_VERSION = 1;
    /** DB Name. */
    private static final String TBL_NAME = "device_tbl";

    /** DB column  {@value} . */
    private static final String COL_TYPE = "type";
    /** DB column  {@value} . */
    private static final String COL_NAME = "name";
    /** DB column  {@value} . */
    private static final String COL_ID = "device_id";
    /** DB column  {@value} . */
    private static final String COL_CONNECT_MODE = "connect_mode";
    /** DB column  {@value} . */
    private static final String COL_PIN_CODE = "pin_code";
    /** DB column  {@value} . */
    private static final String COL_REGISTER_FLAG = "register_flag";

    /** DB Helper. */
    private DBHelper mDBHelper;

    /**
     * Constructor.
     * @param context application context
     */
    public HitoeDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * Add the device to database.
     * @param device device
     * @return the row ID of the newly added row, or -1 if an error occurred
     */
    public synchronized long addHitoeDevice(final HitoeDevice device) {
        List<HitoeDevice> exist = getHitoeDevices(device.getId());
        if (exist.size() > 0) {
            updateHitoeDevice(device);
            return -2;
        }
        ContentValues values = new ContentValues();
        values.put(COL_TYPE, device.getType());
        values.put(COL_NAME, device.getName());
        values.put(COL_ID, device.getId());
        values.put(COL_CONNECT_MODE, device.getConnectMode());
        values.put(COL_PIN_CODE, device.getPinCode());
        values.put(COL_REGISTER_FLAG, device.isRegisterFlag() ? 1 : 0);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * Update the device in the database.
     * @param device device
     * @return the number of rows updated
     */
    public synchronized int updateHitoeDevice(final HitoeDevice device) {
        ContentValues values = new ContentValues();
        values.put(COL_REGISTER_FLAG, device.isRegisterFlag() ? 1 : 0);

        String whereClause = COL_ID + "=?";
        String[] whereArgs = {
                device.getId()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * Delete the device in the database.
     * @param device device
     * @return the number of rows deleted, 0 otherwise
     */
    public synchronized int removeHitoeDevice(final HitoeDevice device) {
        String whereClause = COL_ID + "=?";
        String[] whereArgs = {
                device.getId()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * Get a list of device in the database.
     * @return a list of device
     */
    public synchronized List<HitoeDevice> getHitoeDevices(final String id) {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};
        if (id != null) {
            sql += " WHERE " + COL_ID + "='" + id + "' ";
        }
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<HitoeDevice> devices = new ArrayList<>();
        boolean next = cursor.moveToFirst();
        while (next) {
            HitoeDevice device = new HitoeDevice(null);
            device.setType(cursor.getString(cursor.getColumnIndex(COL_TYPE)));
            device.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
            device.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
            device.setConnectMode(cursor.getString(cursor.getColumnIndex(COL_CONNECT_MODE)));
            device.setPinCode(cursor.getString(cursor.getColumnIndex(COL_PIN_CODE)));
            device.setRegisterFlag(cursor.getInt(cursor.getColumnIndex(COL_REGISTER_FLAG)) == 1);
            devices.add(device);
            next = cursor.moveToNext();
        }
        return devices;
    }

    /**
     * SQL DB Helper.
     */
    private static class DBHelper extends SQLiteOpenHelper {
        /**
         * Constructor.
         * @param context Context
         */
        public DBHelper(final Context context) {
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

        /**
         * Create DB's sql.
         * @param db DB
         */
        private void createDB(final SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TBL_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                    + COL_TYPE + " TEXT, "
                    + COL_NAME + " TEXT, "
                    + COL_ID + " TEXT, "
                    + COL_CONNECT_MODE + " TEXT, "
                    + COL_PIN_CODE + " TEXT, "
                    + COL_REGISTER_FLAG + " INTEGER"
                    + ");";
            db.execSQL(sql);
        }
    }
}
