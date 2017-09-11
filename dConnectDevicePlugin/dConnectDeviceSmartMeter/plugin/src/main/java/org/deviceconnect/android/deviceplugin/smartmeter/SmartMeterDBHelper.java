/*
 SmartMeterDBHelper.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.deviceconnect.android.deviceplugin.smartmeter.device.UsbSerialDevice;

import java.util.ArrayList;

/**
 * SmartMeterアクセス情報を格納するDBヘルパークラス.
 * @author NTT DOCOMO, INC.
 */
class SmartMeterDBHelper {
    /** Define the name of the database. */
    private static final String DB_NAME = "smartmeter_device.db";
    /** Define the version of the database. */
    private static final int DB_VERSION = 1;
    /** SmartMeterの情報を格納するテーブル名. */
    private static final String TBL_NAME = "smartmeter_device_tbl";
    /** ServiceIDを格納するカラム名. */
    private static final String COL_SERVICE_ID = "service_id";
    /** Nameを格納するカラム名. */
    private static final String COL_NAME = "name";
    /** Device Typeを格納するカラム名. */
    private static final String COL_DEVICE_TYPE = "device_type";
    /** MAC Addressを格納するカラム名. */
    private static final String COL_MAC_ADDRESS = "mac_address";
    /** 起動フラグを格納するカラム名. */
    private static final String COL_START_UP = "start_up";

    /** DB管理ヘルパー. */
    private DBHelper mDBHelper;

    /**
     * コンストラクター.
     * @param context context.
     */
    SmartMeterDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * DBにUSBシリアルデバイス情報を追加.
     * @param device 追加するUSBシリアルデバイス情報.
     * @return 追加されてインデックス番号.
     */
    synchronized long addDevice(final UsbSerialDevice device) {
        ContentValues values = new ContentValues();
        values.put(COL_SERVICE_ID, device.getServiceId());
        values.put(COL_NAME, device.getName());
        values.put(COL_DEVICE_TYPE, device.getDeviceType());
        values.put(COL_MAC_ADDRESS, device.getMacAddr());
        values.put(COL_START_UP, device.getOnline() ? 1 : 0);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * DB格納のUSBシリアルデバイスリストを取得.
     * @return USBシリアルデバイスリスト.
     */
    synchronized ArrayList<UsbSerialDevice> getDeviceList() {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            ArrayList<UsbSerialDevice> devices = new ArrayList<>();
            boolean next = cursor.moveToFirst();
            while (next) {
                UsbSerialDevice device = new UsbSerialDevice();
                device.setServiceId(cursor.getString(cursor.getColumnIndex(COL_SERVICE_ID)));
                device.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                device.setDeviceType(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                device.setDeviceType(cursor.getString(cursor.getColumnIndex(COL_DEVICE_TYPE)));
                device.setMacAddr(cursor.getString(cursor.getColumnIndex(COL_MAC_ADDRESS)));
                device.setOnline(cursor.getInt(cursor.getColumnIndex(COL_START_UP)) == 1);
                device.setSerialPort(null);
                device.setSerialInputOutputManager(null);
                devices.add(device);
                next = cursor.moveToNext();
            }
            return devices;
        } finally {
            cursor.close();
        }
    }

    /**
     * USBシリアルデバイス情報更新.
     * @param device 更新するUSBシリアルデバイス情報.
     * @return 更新されたUSBシリアルデバイスの個数
     */
    synchronized long updateDevice(final UsbSerialDevice device) {
        ContentValues values = new ContentValues();
        values.put(COL_SERVICE_ID, device.getServiceId());
        values.put(COL_NAME, device.getName());
        values.put(COL_DEVICE_TYPE, device.getDeviceType());
        values.put(COL_MAC_ADDRESS, device.getMacAddr());
        values.put(COL_START_UP, device.getOnline() ? 1 : 0);

        String whereClause = COL_MAC_ADDRESS + "=?";
        String[] whereArgs = {
                device.getMacAddr()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたMACアドレスと同じUSBシリアルデバイス情報を取得.
     * @param macAddress 取得するDeviceのMACアドレス.
     * @return USBシリアルデバイス情報.
     */
    synchronized UsbSerialDevice getDeviceByMacAddress(final String macAddress) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_MAC_ADDRESS + "=?";
        String[] whereArgs = {
                macAddress
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, whereArgs);
        try {
            if (cursor.moveToFirst()) {
                UsbSerialDevice device = new UsbSerialDevice();
                device.setServiceId(cursor.getString(cursor.getColumnIndex(COL_SERVICE_ID)));
                device.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                device.setDeviceType(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                device.setDeviceType(cursor.getString(cursor.getColumnIndex(COL_DEVICE_TYPE)));
                device.setMacAddr(cursor.getString(cursor.getColumnIndex(COL_MAC_ADDRESS)));
                device.setOnline(cursor.getInt(cursor.getColumnIndex(COL_START_UP)) == 1);
                device.setSerialPort(null);
                device.setSerialInputOutputManager(null);
                return device;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * 指定されたServiceIDと同じUSBシリアルデバイス情報を取得.
     * @param serviceId 取得するDeviceのServiceID.
     * @return USBシリアルデバイス情報.
     */
    synchronized UsbSerialDevice getDeviceByServiceId(final String serviceId) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_SERVICE_ID + "=?";
        String[] whereArgs = {
                serviceId
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, whereArgs);
        try {
            if (cursor.moveToFirst()) {
                UsbSerialDevice device = new UsbSerialDevice();
                device.setServiceId(cursor.getString(cursor.getColumnIndex(COL_SERVICE_ID)));
                device.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                device.setDeviceType(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                device.setDeviceType(cursor.getString(cursor.getColumnIndex(COL_DEVICE_TYPE)));
                device.setMacAddr(cursor.getString(cursor.getColumnIndex(COL_MAC_ADDRESS)));
                device.setOnline(cursor.getInt(cursor.getColumnIndex(COL_START_UP)) == 1);
                device.setSerialPort(null);
                device.setSerialInputOutputManager(null);
                return device;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * 指定されたMACアドレスと同じUSBシリアルデバイス情報を削除.
     * @param macAddress 削除するDeviceのMACアドレス.
     * @return 削除された個数
     */
    synchronized int removeDeviceByMacAddress(final String macAddress) {
        String whereClause = COL_MAC_ADDRESS + "=?";
        String[] whereArgs = {
                macAddress
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたデバイスが格納されているかを確認します.
     * @param device 存在確認をするデバイス.
     * @return 存在する場合にはtrue、それ以外はfalse
     */
    synchronized boolean hasDevice(final UsbSerialDevice device) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_MAC_ADDRESS + "=?";
        String[] whereArgs = {
                device.getMacAddr()
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, whereArgs);
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    /**
     * DBHelper.
     */
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
                    + COL_DEVICE_TYPE + " TEXT NOT NULL, "
                    + COL_MAC_ADDRESS + " TEXT NOT NULL, "
                    + COL_START_UP + " INTEGER NOT NULL "
                    + ");";
            db.execSQL(sql);
        }
    }
}
