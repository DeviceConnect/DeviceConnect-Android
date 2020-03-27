/*
 SwitchBotDeviceProvider.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.switchbot.device;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;

import java.util.ArrayList;

/**
 * SwitchBotデバイスの情報を保存するためのクラス
 */
public class SwitchBotDeviceProvider extends SQLiteOpenHelper {
    private static final String TAG = "SwitchBotDeviceProvider";
    private static final Boolean DEBUG = BuildConfig.DEBUG;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "switchbot.db";
    private static final String TABLE_NAME = "sb_devices";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_MODE = "mode";
    private final Context mContext;
    private final SwitchBotDevice.EventListener mEventListener;
    private SQLiteDatabase mSQLiteDatabase = null;

    public SwitchBotDeviceProvider(final Context context, final SwitchBotDevice.EventListener eventListener) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (DEBUG) {
            Log.d(TAG, "SwitchBotDeviceProvider()");
            Log.d(TAG, "context : " + context);
        }
        mContext = context;
        mEventListener = eventListener;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        if (DEBUG) {
            Log.d(TAG, "onCreate()");
            Log.d(TAG, "mSQLiteDatabase : " + sqLiteDatabase);
        }
        sqLiteDatabase.execSQL(
                "CREATE TABLE " + TABLE_NAME + " ( " + COLUMN_NAME + " TEXT UNIQUE, " + COLUMN_ADDRESS + " TEXT UNIQUE, " + COLUMN_MODE
                        + " INTEGER, PRIMARY KEY(" + COLUMN_NAME + "," + COLUMN_ADDRESS + "))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (DEBUG) {
            Log.d(TAG, "onUpgrade()");
            Log.d(TAG, "mSQLiteDatabase : " + sqLiteDatabase);
            Log.d(TAG, "oldVersion : " + oldVersion);
            Log.d(TAG, "newVersion : " + newVersion);
        }
        sqLiteDatabase.execSQL(
                "DROP TABLE IF EXISTS " + TABLE_NAME
        );
        onCreate(sqLiteDatabase);
    }

    /**
     * デバイス追加処理
     *
     * @param switchBotDevice 追加対象デバイス
     * @return 処理結果。true:成功, false:失敗
     */
    public boolean insert(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "insert()");
            Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
            Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
        }
        if (mSQLiteDatabase == null) {
            mSQLiteDatabase = getReadableDatabase();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, switchBotDevice.getDeviceName());
        contentValues.put(COLUMN_ADDRESS, switchBotDevice.getDeviceAddress());
        contentValues.put(COLUMN_MODE, switchBotDevice.getDeviceMode().getValue());
        return (-1L != mSQLiteDatabase.insert(TABLE_NAME, null, contentValues));
    }

    /**
     * デバイス削除処理
     *
     * @param switchBotDevices 削除対象デバイスリスト
     */
    public void delete(final ArrayList<SwitchBotDevice> switchBotDevices) {
        if (DEBUG) {
            Log.d(TAG, "delete()");
            for (SwitchBotDevice switchBotDevice : switchBotDevices) {
                Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            }
        }
        if (mSQLiteDatabase == null) {
            mSQLiteDatabase = getReadableDatabase();
        }
        String[] deviceNames = new String[switchBotDevices.size()];
        for (int i = 0; i < switchBotDevices.size(); i++) {
            deviceNames[i] = switchBotDevices.get(i).getDeviceName();
        }
        mSQLiteDatabase.delete(TABLE_NAME, generateWherePhrase(deviceNames), deviceNames);
    }

    private String generateWherePhrase(String[] deviceNames) {
        StringBuilder sb = new StringBuilder();
        for(String ignored : deviceNames) {
            sb.append(COLUMN_NAME + " = ? OR ");
        }
        sb.setLength(sb.length() - 4);
        return sb.toString();
    }

    /**
     * デバイスリスト読み出し処理
     *
     * @return デバイスリスト
     */
    public ArrayList<SwitchBotDevice> getDevices() {
        if (DEBUG) {
            Log.d(TAG, "getDevices()");
        }
        ArrayList<SwitchBotDevice> ret = new ArrayList<>();
        if (mSQLiteDatabase == null) {
            mSQLiteDatabase = getReadableDatabase();
        }
        try {
            Cursor cursor = mSQLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String deviceName = null;
                String deviceAddress = null;
                SwitchBotDevice.Mode deviceMode = null;
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    String columnName = cursor.getColumnName(i);
                    if (DEBUG) {
                        Log.d(TAG, "column name : " + columnName);
                    }
                    switch (columnName) {
                        case COLUMN_NAME:
                            deviceName = cursor.getString(i);
                            if (DEBUG) {
                                Log.d(TAG, "device name : " + deviceName);
                            }
                            break;
                        case COLUMN_ADDRESS:
                            deviceAddress = cursor.getString(i);
                            if (DEBUG) {
                                Log.d(TAG, "device address : " + deviceAddress);
                            }
                            break;
                        case COLUMN_MODE:
                            deviceMode = SwitchBotDevice.Mode.getInstance(cursor.getInt(i));
                            if (DEBUG) {
                                Log.d(TAG, "device mode : " + deviceMode);
                            }
                            break;
                        default:
                            Log.e(TAG, "unexpected column name");
                            throw new RuntimeException();
                    }
                }
                if (deviceName != null && deviceAddress != null && deviceMode != null) {
                    SwitchBotDevice switchBotDevice = new SwitchBotDevice(mContext, deviceName, deviceAddress, deviceMode, mEventListener);
                    ret.add(switchBotDevice);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    /**
     * デバイス情報更新処理
     *
     * @param oldDevice 旧デバイス情報
     * @param newDevice 新デバイス情報
     * @return 更新結果。true:成功, false:失敗
     */
    public boolean update(SwitchBotDevice oldDevice, SwitchBotDevice newDevice) {
        if (DEBUG) {
            Log.d(TAG, "update()");
            Log.d(TAG, "device name(old) : " + oldDevice.getDeviceName());
            Log.d(TAG, "device name(new) : " + newDevice.getDeviceName());
            Log.d(TAG, "device mode(old) : " + oldDevice.getDeviceMode());
            Log.d(TAG, "device mode(new) : " + newDevice.getDeviceMode());
        }
        if (mSQLiteDatabase == null) {
            mSQLiteDatabase = getReadableDatabase();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, newDevice.getDeviceName());
        contentValues.put(COLUMN_MODE, newDevice.getDeviceMode().getValue());
        return (-1L != mSQLiteDatabase.update(TABLE_NAME, contentValues, COLUMN_NAME + " =? ", new String[]{oldDevice.getDeviceName()}));
    }
}
