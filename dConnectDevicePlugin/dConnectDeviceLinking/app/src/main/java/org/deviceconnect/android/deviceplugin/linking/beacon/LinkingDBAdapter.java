/*
 LinkingDBAdapter.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.AtmosphericPressureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.BatteryData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.GattData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.HumidityData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.RawData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.TemperatureData;

import java.util.ArrayList;
import java.util.List;

public class LinkingDBAdapter {

    private static final String TAG = "LinkingDB";

    private static final String DB_FILE_NAME = "linking_beacon.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_BEACON = "table_beacon";
    private static final String TABLE_GATT = "table_gatt";
    private static final String TABLE_TEMPERATURE = "table_temperature";
    private static final String TABLE_HUMIDITY = "table_humidity";
    private static final String TABLE_ATMOSPHERIC_PRESSURE = "table_atmospheric_pressure";
    private static final String TABLE_BATTERY = "table_battery";
    private static final String TABLE_RAW_DATA = "table_raw_data";

    private LinkingDatabaseHelper mHelper;

    public LinkingDBAdapter(Context context) {
        mHelper = new LinkingDatabaseHelper(context);
    }

    public void delete(LinkingBeacon beacon) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "LinkingDBAdapter#delete");
            Log.d(TAG, beacon.toString());
        }
        deleteGatt(beacon);
        deleteTemperature(beacon);
        deleteHumidity(beacon);
        deleteAtmosphericPressure(beacon);
        deleteBattery(beacon);
        deleteRawData(beacon);
        deleteBeacon(beacon);
    }

    public void deleteAll() {
        delete(TABLE_BEACON, null, null);
        delete(TABLE_GATT, null, null);
        delete(TABLE_TEMPERATURE, null, null);
        delete(TABLE_HUMIDITY, null, null);
        delete(TABLE_ATMOSPHERIC_PRESSURE, null, null);
        delete(TABLE_BATTERY, null, null);
        delete(TABLE_RAW_DATA, null, null);
    }

    public boolean insertBeacon(LinkingBeacon beacon) {
        ContentValues values = new ContentValues();
        values.put(BeaconColumns.VENDOR_ID, beacon.getVendorId());
        values.put(BeaconColumns.EXTRA_ID, beacon.getExtraId());
        values.put(BeaconColumns.VERSION, beacon.getVersion());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long ret;
        try {
            ret = db.insert(TABLE_BEACON, null, values);
        } finally {
            db.close();
        }
        return ret > 0;
    }

    public List<LinkingBeacon> queryBeacons() {
        List<LinkingBeacon> list = new ArrayList<>();
        Cursor cursor = query(
                TABLE_BEACON,
                null,
                null,
                null,
                BaseColumns._ID + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        LinkingBeacon beacon = new LinkingBeacon();
                        beacon.setVendorId(cursor.getInt(cursor.getColumnIndex(BeaconColumns.VENDOR_ID)));
                        beacon.setExtraId(cursor.getInt(cursor.getColumnIndex(BeaconColumns.EXTRA_ID)));
                        beacon.setVersion(cursor.getInt(cursor.getColumnIndex(BeaconColumns.VERSION)));
                        beacon.setGattData(queryGatt(beacon));
                        beacon.setTemperatureData(queryTemperature(beacon));
                        beacon.setAtmosphericPressureData(queryAtmosphericPressure(beacon));
                        beacon.setHumidityData(queryHumidity(beacon));
                        beacon.setBatteryData(queryBattery(beacon));
                        beacon.setRawData(queryRawData(beacon.getVendorId(), beacon.getExtraId()));
                        list.add(beacon);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    public boolean deleteBeacon(LinkingBeacon beacon) {
        String selection = LinkingBaseColumns.VENDOR_ID + "=? AND " + LinkingBaseColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(beacon.getVendorId()),
                String.valueOf(beacon.getExtraId())
        };
        try {
            int row = delete(TABLE_BEACON, selection, selectionArgs);
            return row > 0;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return false;
    }

    public boolean insertGatt(LinkingBeacon beacon, GattData gatt) {
        return insertGatt(beacon.getVendorId(), beacon.getExtraId(), gatt);
    }

    public boolean insertGatt(int vendorId, int extraId, GattData gatt) {
        ContentValues values = new ContentValues();
        values.put(GattColumns.VENDOR_ID, vendorId);
        values.put(GattColumns.EXTRA_ID, extraId);
        values.put(GattColumns.RSSI, gatt.getRssi());
        values.put(GattColumns.TX_POWER, gatt.getTxPower());
        values.put(GattColumns.DISTANCE, gatt.getDistance());
        values.put(GattColumns.TIME_STAMP, gatt.getTimeStamp());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long ret;
        try {
            ret = db.insert(TABLE_GATT, null, values);
        } finally {
            db.close();
        }
        return ret > 0;
    }

    public GattData queryGatt(LinkingBeacon beacon) {
        return queryGatt(beacon.getVendorId(), beacon.getExtraId());
    }

    public GattData queryGatt(int vendorId, int extraId) {
        String selection = GattColumns.VENDOR_ID + "=? AND " + GattColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        Cursor cursor = query(
                TABLE_GATT,
                null,
                selection,
                selectionArgs,
                GattColumns.TIME_STAMP + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    GattData gatt = new GattData();
                    gatt.setRssi(cursor.getInt(cursor.getColumnIndex(GattColumns.RSSI)));
                    gatt.setTxPower(cursor.getInt(cursor.getColumnIndex(GattColumns.TX_POWER)));
                    gatt.setDistance(cursor.getInt(cursor.getColumnIndex(GattColumns.DISTANCE)));
                    gatt.setTimeStamp(cursor.getLong(cursor.getColumnIndex(GattColumns.TIME_STAMP)));
                    return gatt;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public boolean deleteGatt(LinkingBeacon beacon) {
        return deleteGatt(beacon.getVendorId(), beacon.getExtraId());
    }

    public boolean deleteGatt(int vendorId, int extraId) {
        String selection = LinkingBaseColumns.VENDOR_ID + "=? AND " + LinkingBaseColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        try {
            int row = delete(TABLE_GATT, selection, selectionArgs);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "LinkingDBAdapter#deleteGatt: " + row);
            }
            return row > 0;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return false;
    }

    public boolean insertTemperature(LinkingBeacon beacon, TemperatureData temp) {
        return insertTemperature(beacon.getVendorId(), beacon.getExtraId(), temp);
    }

    public boolean insertTemperature(int vendorId, int extraId, TemperatureData temp) {
        ContentValues values = new ContentValues();
        values.put(TemperatureColumns.VENDOR_ID, vendorId);
        values.put(TemperatureColumns.EXTRA_ID, extraId);
        values.put(TemperatureColumns.TEMPERATURE, temp.getValue());
        values.put(TemperatureColumns.TIME_STAMP, temp.getTimeStamp());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long ret;
        try {
            ret = db.insert(TABLE_TEMPERATURE, null, values);
        } finally {
            db.close();
        }
        return ret > 0;
    }

    public TemperatureData queryTemperature(LinkingBeacon beacon) {
        return queryTemperature(beacon.getVendorId(), beacon.getExtraId());
    }

    public TemperatureData queryTemperature(int vendorId, int extraId) {
        String selection = GattColumns.VENDOR_ID + "=? AND " + GattColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        Cursor cursor = query(
                TABLE_TEMPERATURE,
                null,
                selection,
                selectionArgs,
                GattColumns.TIME_STAMP + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    TemperatureData temp = new TemperatureData();
                    temp.setValue(cursor.getFloat(cursor.getColumnIndex(TemperatureColumns.TEMPERATURE)));
                    temp.setTimeStamp(cursor.getLong(cursor.getColumnIndex(TemperatureColumns.TIME_STAMP)));
                    return temp;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public boolean deleteTemperature(LinkingBeacon beacon) {
        return deleteTemperature(beacon.getVendorId(), beacon.getExtraId());
    }

    public boolean deleteTemperature(int vendorId, int extraId) {
        String selection = LinkingBaseColumns.VENDOR_ID + "=? AND " + LinkingBaseColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        try {
            int row = delete(TABLE_TEMPERATURE, selection, selectionArgs);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "LinkingDBAdapter#deleteTemperature: " + row);
            }
            return row > 0;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return false;
    }

    public boolean insertHumidity(LinkingBeacon beacon, HumidityData humidity) {
        return insertHumidity(beacon.getVendorId(), beacon.getExtraId(), humidity);
    }

    public boolean insertHumidity(int vendorId, int extraId, HumidityData humidity) {
        ContentValues values = new ContentValues();
        values.put(HumidityColumns.VENDOR_ID, vendorId);
        values.put(HumidityColumns.EXTRA_ID, extraId);
        values.put(HumidityColumns.HUMIDITY, humidity.getValue());
        values.put(HumidityColumns.TIME_STAMP, humidity.getTimeStamp());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long ret;
        try {
            ret = db.insert(TABLE_HUMIDITY, null, values);
        } finally {
            db.close();
        }
        return ret > 0;
    }

    public HumidityData queryHumidity(LinkingBeacon beacon) {
        return queryHumidity(beacon.getVendorId(), beacon.getExtraId());
    }

    public HumidityData queryHumidity(int vendorId, int extraId) {
        String selection = HumidityColumns.VENDOR_ID + "=? AND " + HumidityColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        Cursor cursor = query(
                TABLE_HUMIDITY,
                null,
                selection,
                selectionArgs,
                GattColumns.TIME_STAMP + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    HumidityData humidity = new HumidityData();
                    humidity.setValue(cursor.getFloat(cursor.getColumnIndex(HumidityColumns.HUMIDITY)));
                    humidity.setTimeStamp(cursor.getLong(cursor.getColumnIndex(HumidityColumns.TIME_STAMP)));
                    return humidity;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public boolean deleteHumidity(LinkingBeacon beacon) {
        return deleteHumidity(beacon.getVendorId(), beacon.getExtraId());
    }

    public boolean deleteHumidity(int vendorId, int extraId) {
        String selection = LinkingBaseColumns.VENDOR_ID + "=? AND " + LinkingBaseColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        try {
            int row = delete(TABLE_HUMIDITY, selection, selectionArgs);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "LinkingDBAdapter#deleteHumidity: " + row);
            }
            return row > 0;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return false;
    }

    public boolean insertAtmosphericPressure(LinkingBeacon beacon, AtmosphericPressureData ap) {
        return insertAtmosphericPressure(beacon.getVendorId(), beacon.getExtraId(), ap);
    }

    public boolean insertAtmosphericPressure(int vendorId, int extraId, AtmosphericPressureData ap) {
        ContentValues values = new ContentValues();
        values.put(AtmosphericPressure.VENDOR_ID, vendorId);
        values.put(AtmosphericPressure.EXTRA_ID, extraId);
        values.put(AtmosphericPressure.ATMOSPHERIC_PRESSURE, ap.getValue());
        values.put(AtmosphericPressure.TIME_STAMP, ap.getTimeStamp());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long ret;
        try {
            ret = db.insert(TABLE_ATMOSPHERIC_PRESSURE, null, values);
        } finally {
            db.close();
        }
        return ret > 0;
    }

    public AtmosphericPressureData queryAtmosphericPressure(LinkingBeacon beacon) {
        return queryAtmosphericPressure(beacon.getVendorId(), beacon.getExtraId());
    }

    public AtmosphericPressureData queryAtmosphericPressure(int vendorId, int extraId) {
        String selection = AtmosphericPressure.VENDOR_ID + "=? AND " + AtmosphericPressure.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        Cursor cursor = query(
                TABLE_ATMOSPHERIC_PRESSURE,
                null,
                selection,
                selectionArgs,
                GattColumns.TIME_STAMP + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    AtmosphericPressureData ap = new AtmosphericPressureData();
                    ap.setValue(cursor.getFloat(cursor.getColumnIndex(AtmosphericPressure.ATMOSPHERIC_PRESSURE)));
                    ap.setTimeStamp(cursor.getLong(cursor.getColumnIndex(AtmosphericPressure.TIME_STAMP)));
                    return ap;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public boolean deleteAtmosphericPressure(LinkingBeacon beacon) {
        return deleteAtmosphericPressure(beacon.getVendorId(), beacon.getExtraId());
    }

    public boolean deleteAtmosphericPressure(int vendorId, int extraId) {
        String selection = LinkingBaseColumns.VENDOR_ID + "=? AND " + LinkingBaseColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        try {
            int row = delete(TABLE_ATMOSPHERIC_PRESSURE, selection, selectionArgs);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "LinkingDBAdapter#deleteAtmosphericPressure: " + row);
            }
            return row > 0;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return false;
    }

    public boolean insertBattery(LinkingBeacon beacon, BatteryData battery) {
        return insertBattery(beacon.getVendorId(), beacon.getExtraId(), battery);
    }

    public boolean insertBattery(int vendorId, int extraId, BatteryData battery) {
        ContentValues values = new ContentValues();
        values.put(BatteryColumns.VENDOR_ID, vendorId);
        values.put(BatteryColumns.EXTRA_ID, extraId);
        values.put(BatteryColumns.LEVEL, battery.getLevel());
        values.put(BatteryColumns.LOW_BATTERY, battery.isLowBatteryFlag() ? 1 : 0);
        values.put(BatteryColumns.TIME_STAMP, battery.getTimeStamp());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long ret;
        try {
            ret = db.insert(TABLE_BATTERY, null, values);
        } finally {
            db.close();
        }
        return ret > 0;
    }

    public BatteryData queryBattery(LinkingBeacon beacon) {
        return queryBattery(beacon.getVendorId(), beacon.getExtraId());
    }

    public BatteryData queryBattery(int vendorId, int extraId) {
        String selection = BatteryColumns.VENDOR_ID + "=? AND " + BatteryColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        Cursor cursor = query(
                TABLE_BATTERY,
                null,
                selection,
                selectionArgs,
                GattColumns.TIME_STAMP + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    BatteryData battery = new BatteryData();
                    battery.setLevel(cursor.getFloat(cursor.getColumnIndex(BatteryColumns.LEVEL)));
                    battery.setLowBatteryFlag(cursor.getInt(cursor.getColumnIndex(BatteryColumns.LOW_BATTERY)) == 1);
                    battery.setTimeStamp(cursor.getLong(cursor.getColumnIndex(BatteryColumns.TIME_STAMP)));
                    return battery;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public boolean deleteBattery(LinkingBeacon beacon) {
        return  deleteBattery(beacon.getVendorId(), beacon.getExtraId());
    }

    public boolean deleteBattery(int vendorId, int extraId) {
        String selection = LinkingBaseColumns.VENDOR_ID + "=? AND " + LinkingBaseColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        try {
            int row = delete(TABLE_BATTERY, selection, selectionArgs);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "LinkingDBAdapter#deleteBattery: " + row);
            }
            return row > 0;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return false;
    }

    public boolean insertRawData(LinkingBeacon beacon, RawData raw) {
        return insertRawData(beacon.getVendorId(), beacon.getExtraId(), raw);
    }

    public boolean insertRawData(int vendorId, int extraId, RawData raw) {
        ContentValues values = new ContentValues();
        values.put(RawDataColumns.VENDOR_ID, vendorId);
        values.put(RawDataColumns.EXTRA_ID, extraId);
        values.put(RawDataColumns.RAW_DATA, raw.getValue());
        values.put(RawDataColumns.TIME_STAMP, raw.getTimeStamp());

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long ret;
        try {
            ret = db.insert(TABLE_RAW_DATA, null, values);
        } finally {
            db.close();
        }
        return ret > 0;
    }

    public RawData queryRawData(LinkingBeacon beacon) {
        return queryRawData(beacon.getVendorId(), beacon.getExtraId());
    }

    public RawData queryRawData(int vendorId, int extraId) {
        String selection = RawDataColumns.VENDOR_ID + "=? AND " + RawDataColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        Cursor cursor = query(
                TABLE_RAW_DATA,
                null,
                selection,
                selectionArgs,
                GattColumns.TIME_STAMP + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    RawData raw = new RawData();
                    raw.setValue(cursor.getInt(cursor.getColumnIndex(RawDataColumns.RAW_DATA)));
                    raw.setTimeStamp(cursor.getLong(cursor.getColumnIndex(RawDataColumns.TIME_STAMP)));
                    return raw;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public boolean deleteRawData(LinkingBeacon beacon) {
        return deleteRawData(beacon.getVendorId(), beacon.getExtraId());
    }

    public boolean deleteRawData(int vendorId, int extraId) {
        String selection = LinkingBaseColumns.VENDOR_ID + "=? AND " + LinkingBaseColumns.EXTRA_ID + "=?";
        String[] selectionArgs = {
                String.valueOf(vendorId),
                String.valueOf(extraId)
        };
        try {
            int row = delete(TABLE_RAW_DATA, selection, selectionArgs);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "LinkingDBAdapter#deleteRawData: " + row);
            }
            return row > 0;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return false;
    }

    private Cursor query(String tbl, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(tbl);

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = null;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase mdb = mHelper.getReadableDatabase();
        return qb.query(mdb, projection, selection, selectionArgs, null, null, orderBy);
    }

    private int delete(String tbl, String selection, String[] selectionArgs) {
        SQLiteDatabase mdb = mHelper.getWritableDatabase();
        return mdb.delete(tbl, selection, selectionArgs);
    }

    private class LinkingDatabaseHelper extends SQLiteOpenHelper {

        public LinkingDatabaseHelper(Context context) {
            super(context, DB_FILE_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createBeaconTable(db);
            createGattTable(db);
            createTemperatureTable(db);
            createHumidityTable(db);
            createAtmosphericPressureTable(db);
            createBatteryTable(db);
            createRawDataTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onDowngrade(db, oldVersion, newVersion);
        }

        private void createBeaconTable(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_BEACON + " (_id INTEGER PRIMARY KEY,"
                    + BeaconColumns.VENDOR_ID + " INTEGER,"
                    + BeaconColumns.EXTRA_ID + " INTEGER,"
                    + BeaconColumns.VERSION + " INTEGER,"
                    + BeaconColumns.TIME_STAMP + " INTEGER);";
            db.execSQL(sql);
        }

        private void createGattTable(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_GATT + " (_id INTEGER PRIMARY KEY,"
                    + GattColumns.VENDOR_ID + " INTEGER,"
                    + GattColumns.EXTRA_ID + " INTEGER,"
                    + GattColumns.TIME_STAMP + " INTEGER,"
                    + GattColumns.RSSI + " INTEGER,"
                    + GattColumns.TX_POWER + " INTEGER,"
                    + GattColumns.DISTANCE + " INTEGER"
                    + ");";
            db.execSQL(sql);
        }

        private void createTemperatureTable(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_TEMPERATURE + " (_id INTEGER PRIMARY KEY,"
                    + TemperatureColumns.VENDOR_ID + " INTEGER,"
                    + TemperatureColumns.EXTRA_ID + " INTEGER,"
                    + TemperatureColumns.TIME_STAMP + " INTEGER,"
                    + TemperatureColumns.TEMPERATURE + " REAL"
                    + ");";
            db.execSQL(sql);
        }

        private void createHumidityTable(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_HUMIDITY + " (_id INTEGER PRIMARY KEY,"
                    + HumidityColumns.VENDOR_ID + " INTEGER,"
                    + HumidityColumns.EXTRA_ID + " INTEGER,"
                    + HumidityColumns.TIME_STAMP + " INTEGER,"
                    + HumidityColumns.HUMIDITY + " REAL"
                    + ");";
            db.execSQL(sql);
        }

        private void createAtmosphericPressureTable(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_ATMOSPHERIC_PRESSURE + " (_id INTEGER PRIMARY KEY,"
                    + AtmosphericPressure.VENDOR_ID + " INTEGER,"
                    + AtmosphericPressure.EXTRA_ID + " INTEGER,"
                    + AtmosphericPressure.TIME_STAMP + " INTEGER,"
                    + AtmosphericPressure.ATMOSPHERIC_PRESSURE + " REAL"
                    + ");";
            db.execSQL(sql);
        }

        private void createBatteryTable(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_BATTERY + " (_id INTEGER PRIMARY KEY,"
                    + BatteryColumns.VENDOR_ID + " INTEGER,"
                    + BatteryColumns.EXTRA_ID + " INTEGER,"
                    + BatteryColumns.TIME_STAMP + " INTEGER,"
                    + BatteryColumns.LOW_BATTERY + " INTEGER,"
                    + BatteryColumns.LEVEL + " REAL"
                    + ");";
            db.execSQL(sql);
        }

        private void createRawDataTable(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_RAW_DATA + " (_id INTEGER PRIMARY KEY,"
                    + RawDataColumns.VENDOR_ID + " INTEGER,"
                    + RawDataColumns.EXTRA_ID + " INTEGER,"
                    + RawDataColumns.TIME_STAMP + " INTEGER,"
                    + RawDataColumns.RAW_DATA + " INTEGER"
                    + ");";
            db.execSQL(sql);
        }
    }

    public interface LinkingBaseColumns extends BaseColumns {
        String VENDOR_ID = "vendor_id";
        String EXTRA_ID = "extra_id";
        String TIME_STAMP = "time_stamp";
    }

    public interface BeaconColumns extends LinkingBaseColumns {
        String VERSION = "version";
    }

    public interface GattColumns extends LinkingBaseColumns {
        String RSSI = "rssi";
        String TX_POWER = "tx_power";
        String DISTANCE = "distance";
    }

    public interface TemperatureColumns extends LinkingBaseColumns {
        String TEMPERATURE = "temperature";
    }

    public interface HumidityColumns extends LinkingBaseColumns {
        String HUMIDITY = "humidity";
    }

    public interface AtmosphericPressure extends LinkingBaseColumns {
        String ATMOSPHERIC_PRESSURE = "atmospheric_pressure";
    }

    public interface BatteryColumns extends LinkingBaseColumns {
        String LOW_BATTERY = "low_battery";
        String LEVEL = "level";
    }

    public interface RawDataColumns extends LinkingBaseColumns {
        String RAW_DATA = "raw_data";
    }
}
