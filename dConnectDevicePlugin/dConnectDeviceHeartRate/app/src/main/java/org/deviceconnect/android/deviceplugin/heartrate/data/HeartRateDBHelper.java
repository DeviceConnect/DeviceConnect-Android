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
 */
public class HeartRateDBHelper {

    private static final String DB_NAME = "heart_rate.db";
    private static final int DB_VERSION = 1;
    private static final String TBL_NAME = "device_tbl";

    private static final String COL_NAME = "name";
    private static final String COL_ADDRESS = "address";
    private static final String COL_LOCATION = "location";
    private static final String COL_REGISTER_FLAG = "register_flag";

    private DBHelper mDBHelper;

    public HeartRateDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    public synchronized long addHeartRateDevice(HeartRateDevice device) {
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

    public synchronized int updateHeartRateDevice(HeartRateDevice device) {
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

    public synchronized int removeHeartRateDevice(HeartRateDevice device) {
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
        public void onCreate(SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
            createDB(db);
        }

        private void createDB(SQLiteDatabase db) {
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
