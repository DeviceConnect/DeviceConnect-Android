/*
 HeartRateDBHelper
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.data;

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
public class HeartRateDBHelper {

    /**
     * Define the name of the database.
     */
    private static final String DB_NAME = "heart_rate.db";

    /**
     * Define the version of the database.
     */
    private static final int DB_VERSION = 1;
    private static final String TBL_NAME = "device_tbl";

    private static final String COL_NAME = "name";
    private static final String COL_ADDRESS = "address";
    private static final String COL_LOCATION = "location";
    private static final String COL_REGISTER_FLAG = "register_flag";

    private DBHelper mDBHelper;

    /**
     * Constructor.
     * @param context application context
     */
    public HeartRateDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * Add the device to database.
     * @param device device
     * @return the row ID of the newly added row, or -1 if an error occurred
     */
    public synchronized long addHeartRateDevice(final HeartRateDevice device) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, device.getName());
        values.put(COL_ADDRESS, device.getAddress());
        values.put(COL_LOCATION, device.getSensorLocation());
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
    public synchronized int updateHeartRateDevice(final HeartRateDevice device) {
        ContentValues values = new ContentValues();
        values.put(COL_REGISTER_FLAG, device.isRegisterFlag() ? 1 : 0);

        String whereClause = COL_ADDRESS + "=?";
        String[] whereArgs = {
                device.getAddress()
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
    public synchronized int removeHeartRateDevice(final HeartRateDevice device) {
        String whereClause = COL_ADDRESS + "=?";
        String[] whereArgs = {
                device.getAddress()
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
    public synchronized List<HeartRateDevice> getHeartRateDevices() {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<HeartRateDevice> devices = new ArrayList<>();
        boolean next = cursor.moveToFirst();
        while (next) {
            HeartRateDevice device = new HeartRateDevice();
            device.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
            device.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
            device.setAddress(cursor.getString(cursor.getColumnIndex(COL_ADDRESS)));
            device.setSensorLocation(cursor.getInt(cursor.getColumnIndex(COL_LOCATION)));
            device.setRegisterFlag(cursor.getInt(cursor.getColumnIndex(COL_REGISTER_FLAG)) == 1);
            devices.add(device);
            next = cursor.moveToNext();
        }
        return devices;
    }

    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
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
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_ADDRESS + " TEXT NOT NULL, "
                    + COL_LOCATION + " INTEGER, "
                    + COL_REGISTER_FLAG + " INTEGER"
                    + ");";
            db.execSQL(sql);
        }
    }
}
