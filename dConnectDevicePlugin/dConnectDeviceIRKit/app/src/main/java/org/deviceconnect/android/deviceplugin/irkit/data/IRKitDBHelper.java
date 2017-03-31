package org.deviceconnect.android.deviceplugin.irkit.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Virutal Device 及び Virtual Profile を管理するDBクラス
 */
public class IRKitDBHelper {
    /**
     * DB 名.
     */
    private static final String DB_NAME = "irkit_virtual_device_and_profile.db";

    /**
     * DB Version.
     */
    private static final int DB_VERSION = 1;

    /**
     * Virtual Device テーブル.
     */
    private static final String VIRTUAL_DEVICE_TBL_NAME = "virtual_device_tbl";

    /**
     * Virtual Profile テーブル.
     */
    private static final String VIRTUAL_PROFILE_TBL_NAME = "virtual_profile_tbl";

    /**
     * Virtual Device テーブル: サービスID カラム.
     */
    private static final String VIRTUAL_DEVICE_COL_SERVICE_ID = "service_id";
    /**
     * Virtual Device テーブル: デバイス名 カラム.
     */
    private static final String VIRTUAL_DEVICE_COL_DEVICE_NAME = "device_name";
    /**
     * Virtual Device テーブル: カテゴリ名 カラム.
     */
    private static final String VIRTUAL_DEVICE_COL_CATEGORY_NAME = "category_name";

    /**
     * Virtual Profile テーブル: API 名 カラム.
     */
    private static final String VIRTUAL_PROFILE_COL_NAME = "name";
    /**
     * Virtual Profile テーブル: 赤外線データ カラム.
     */
    private static final String VIRTUAL_PROFILE_COL_IR = "ir";
    /**
     * Virtual Profile テーブル: サービスID カラム.
     */
    private static final String VIRTUAL_PROFILE_COL_SERVICE_ID = "service_id";
    /**
     * Virtual Profile テーブル: Profile カラム.
     */
    private static final String VIRTUAL_PROFILE_COL_PROFILE = "profile";
    /**
     * Virtual Profile テーブル: HTTP Method カラム.
     */
    private static final String VIRTUAL_PROFILE_COL_METHOD = "method";
    /**
     * Virtual Profile テーブル: URI カラム.
     */
    private static final String VIRTUAL_PROFILE_COL_URI = "uri";

    //サポートするプロファイル
    //　ライトのON/OFF
    /**
     * サポートするLight のAPI 名.
     */
    private static final String IRKIT_LIGHT_API_NAMES[] = {"ライト ON", "ライト OFF"};
    /**
     * サポートする Light の URI.
     */
    private static final String IRKIT_LIGHT_API_URIS[] = {"/light", "/light"};

    /**
     * サポートする Light の HTTP Method.
     */
    private static final String IRKIT_LIGHT_API_HTTP_METHODS[] = {"POST", "DELETE"};

    // TV ON/OFF チャンネルUp/Down 音量Up/Down 放送電波切り替え(地デジ/BS/CS)
    /**
     * サポートする TV のAPI 名.
     */
    private static final String IRKIT_TV_API_NAMES[] = {"TV電源ON", "TV電源OFF", "チャンネル+",
                                                            "チャンネル-",
                                                            "1", "2", "3", "4",
                                                            "5", "6", "7", "8","9", "10",
                                                            "11", "12",
                                                            "音量+", "音量-",
                                                             "地デジ", "BS", "CS"};
    /**
     * サポートする TV のURI.
     */
    private static final String IRKIT_TV_API_URIS[] = {"/tv", "/tv", "/tv/channel?control=next",
                                                          "/tv/channel?control=previous",
                                                          "/tv/channel?tuning=1",
                                                          "/tv/channel?tuning=2",
                                                          "/tv/channel?tuning=3",
                                                          "/tv/channel?tuning=4",
                                                          "/tv/channel?tuning=5",
                                                          "/tv/channel?tuning=6",
                                                          "/tv/channel?tuning=7",
                                                          "/tv/channel?tuning=8",
                                                          "/tv/channel?tuning=9",
                                                          "/tv/channel?tuning=10",
                                                          "/tv/channel?tuning=11",
                                                          "/tv/channel?tuning=12",
                                                          "/tv/volume?control=up",
                                                          "/tv/volume?control=down",
                                                          "/tv/broadcastwave?select=DTV",
                                                          "/tv/broadcastwave?select=BS",
                                                          "/tv/broadcastwave?select=CS"};
    /**
     * サポートする TV の HTTP Methods.
     */
    private static final String IRKIT_TV_API_HTTP_METHODS[] = {"PUT", "DELETE", "PUT",
                                                             "PUT", "PUT", "PUT",
                                                             "PUT", "PUT", "PUT",
                                                             "PUT", "PUT", "PUT",
                                                             "PUT", "PUT", "PUT",
                                                             "PUT", "PUT", "PUT",
                                                             "PUT", "PUT", "PUT"};

    /**
     * DB Helper.
     */
    private DBHelper mDBHelper;

    /**
     * コンストラクタ.
     * @param context application context
     */
    public IRKitDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * 仮想デバイスの登録.
     * @param device 登録する仮想デバイス
     * @return 登録の成否
     */
    public synchronized long addVirtualDevice(final VirtualDeviceData device) {
        addVirtualProfiles(device);
        ContentValues values = new ContentValues();
        values.put(VIRTUAL_DEVICE_COL_SERVICE_ID, device.getServiceId());
        values.put(VIRTUAL_DEVICE_COL_DEVICE_NAME, device.getDeviceName());
        values.put(VIRTUAL_DEVICE_COL_CATEGORY_NAME, device.getCategoryName());

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(VIRTUAL_DEVICE_TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * サービスID に関連したProfile を登録する。
     *
     * @return 登録の成否
     */
    private synchronized void addVirtualProfiles(final VirtualDeviceData device) {
        if (device.getCategoryName().equals("ライト")) {
            for (int i = 0; i < IRKIT_LIGHT_API_NAMES.length; i++) {
                ContentValues values = new ContentValues();
                values.put(VIRTUAL_PROFILE_COL_NAME, IRKIT_LIGHT_API_NAMES[i]);
                values.put(VIRTUAL_PROFILE_COL_SERVICE_ID, device.getServiceId());
                values.put(VIRTUAL_PROFILE_COL_PROFILE, "Light");
                values.put(VIRTUAL_PROFILE_COL_METHOD, IRKIT_LIGHT_API_HTTP_METHODS[i]);
                values.put(VIRTUAL_PROFILE_COL_URI, IRKIT_LIGHT_API_URIS[i]);

                SQLiteDatabase db = mDBHelper.getWritableDatabase();
                try {
                    db.insert(VIRTUAL_PROFILE_TBL_NAME, null, values);
                } finally {
                    db.close();
                }
            }
        } else {
            for (int i = 0; i < IRKIT_TV_API_NAMES.length; i++) {
                ContentValues values = new ContentValues();
                values.put(VIRTUAL_PROFILE_COL_NAME, IRKIT_TV_API_NAMES[i]);
                values.put(VIRTUAL_PROFILE_COL_SERVICE_ID, device.getServiceId());
                values.put(VIRTUAL_PROFILE_COL_PROFILE, "TV");
                values.put(VIRTUAL_PROFILE_COL_METHOD, IRKIT_TV_API_HTTP_METHODS[i]);
                values.put(VIRTUAL_PROFILE_COL_URI, IRKIT_TV_API_URIS[i]);

                SQLiteDatabase db = mDBHelper.getWritableDatabase();
                try {
                    db.insert(VIRTUAL_PROFILE_TBL_NAME, null, values);
                } finally {
                    db.close();
                }
            }
        }
    }

    /**
     * Virtual Profile の更新.
     * @param profile profile
     * @return the number of rows updated
     */
    public synchronized int updateVirtualProfile(final VirtualProfileData profile) {
        ContentValues values = new ContentValues();
        values.put(VIRTUAL_PROFILE_COL_NAME, profile.getName());
        values.put(VIRTUAL_PROFILE_COL_SERVICE_ID, profile.getServiceId());
        values.put(VIRTUAL_PROFILE_COL_PROFILE, profile.getProfile());
        values.put(VIRTUAL_PROFILE_COL_METHOD, profile.getMethod());
        values.put(VIRTUAL_PROFILE_COL_URI, profile.getUri());

        values.put(VIRTUAL_PROFILE_COL_IR, profile.getIr());

        String whereClause = VIRTUAL_PROFILE_COL_SERVICE_ID + "=? AND "
                + VIRTUAL_PROFILE_COL_URI + "=? AND "
                + VIRTUAL_PROFILE_COL_METHOD + "=?";
        String[] whereArgs = {
                profile.getServiceId(), profile.getUri(), profile.getMethod()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(VIRTUAL_PROFILE_TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }


    /**
     * 仮想デバイスを削除する.
     * @param serviceId ServiceID
     * @return 成否
     */
    public synchronized boolean removeVirtualDevice(final String serviceId) {
        String whereClause = VIRTUAL_DEVICE_COL_SERVICE_ID + "=?";
        String[] whereArgs = {
                serviceId
        };
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            int isDeleteProfile = db.delete(VIRTUAL_PROFILE_TBL_NAME, whereClause, whereArgs);
            int isDeleteDevice = db.delete(VIRTUAL_DEVICE_TBL_NAME, whereClause, whereArgs);
            if (isDeleteDevice > 0 && isDeleteProfile > 0) {
                return true;
            } else {
                return false;
            }
        } finally {
            db.close();
        }
    }

    /**
     * Virtual Device Listの取得.
     * @param serviceId 検索するサービスID. 全件取得はnull.
     * @return Virtual Device List
     */
    public synchronized List<VirtualDeviceData> getVirtualDevices(final String serviceId) {
        String sql = "SELECT * FROM " + VIRTUAL_DEVICE_TBL_NAME;
        if (serviceId != null) {
            sql += " WHERE " + VIRTUAL_PROFILE_COL_SERVICE_ID + "='" + serviceId + "';";
        }
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<VirtualDeviceData> devices = new ArrayList<VirtualDeviceData>();
        boolean next = cursor.moveToFirst();
        while (next) {
            VirtualDeviceData device = new VirtualDeviceData();
            device.setServiceId(cursor.getString(cursor.getColumnIndex(VIRTUAL_DEVICE_COL_SERVICE_ID)));
            device.setDeviceName(cursor.getString(cursor.getColumnIndex(VIRTUAL_DEVICE_COL_DEVICE_NAME)));
            device.setCategoryName(cursor.getString(cursor.getColumnIndex(VIRTUAL_DEVICE_COL_CATEGORY_NAME)));
            devices.add(device);
            next = cursor.moveToNext();
        }
        cursor.close();
        return devices;
    }


    /**
     * Virtual Device Listの取得.
     * @param serviceId 検索するサービスID
     * @return Virtual Device List
     */
    public synchronized List<VirtualDeviceData> getVirtualDevicesByServiceId(final String serviceId) {
        String sql = "SELECT * FROM " + VIRTUAL_DEVICE_TBL_NAME;
        if (serviceId != null) {
            sql += " WHERE " + VIRTUAL_PROFILE_COL_SERVICE_ID + " LIKE '" + serviceId + "%';";
        }
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<VirtualDeviceData> devices = new ArrayList<VirtualDeviceData>();
        boolean next = cursor.moveToFirst();
        while (next) {
            VirtualDeviceData device = new VirtualDeviceData();
            device.setServiceId(cursor.getString(cursor.getColumnIndex(VIRTUAL_DEVICE_COL_SERVICE_ID)));
            device.setDeviceName(cursor.getString(cursor.getColumnIndex(VIRTUAL_DEVICE_COL_DEVICE_NAME)));
            device.setCategoryName(cursor.getString(cursor.getColumnIndex(VIRTUAL_DEVICE_COL_CATEGORY_NAME)));
            devices.add(device);
            next = cursor.moveToNext();
        }
        cursor.close();
        return devices;
    }

    /**
     * Virtual Profile Listの取得.
     * @param serviceId 検索するサービスID. 全件取得はnull.
     * @param profile Profile
     * @return Virtual Device List
     */
    public synchronized List<VirtualProfileData> getVirtualProfiles(final String serviceId,
                                                                    final String profile) {
        String sql = "SELECT * FROM " + VIRTUAL_PROFILE_TBL_NAME;
        if (serviceId != null && profile != null) {
            sql += " WHERE " + VIRTUAL_PROFILE_COL_SERVICE_ID + "='" + serviceId + "'" +
                    " AND " + VIRTUAL_PROFILE_COL_PROFILE + "='" + profile + "';";
        } else if (serviceId != null && profile == null) {
            sql += " WHERE " + VIRTUAL_PROFILE_COL_SERVICE_ID + "='" + serviceId + "';";

        }

        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<VirtualProfileData> profiles = new ArrayList<VirtualProfileData>();
        boolean next = cursor.moveToFirst();
        while (next) {
            VirtualProfileData p = new VirtualProfileData();
            p.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
            p.setServiceId(cursor.getString(cursor.getColumnIndex(VIRTUAL_PROFILE_COL_SERVICE_ID)));
            p.setName(cursor.getString(cursor.getColumnIndex(VIRTUAL_PROFILE_COL_NAME)));
            p.setProfile(cursor.getString(cursor.getColumnIndex(VIRTUAL_PROFILE_COL_PROFILE)));
            p.setMethod(cursor.getString(cursor.getColumnIndex(VIRTUAL_PROFILE_COL_METHOD)));
            p.setUri(cursor.getString(cursor.getColumnIndex(VIRTUAL_PROFILE_COL_URI)));
            p.setIr(cursor.getString(cursor.getColumnIndex(VIRTUAL_PROFILE_COL_IR)));
            profiles.add(p);
            next = cursor.moveToNext();
        }
        cursor.close();
        return profiles;
    }

    /**
     * Virtual Device and Profile DB Helper.
     */
    private static class DBHelper extends SQLiteOpenHelper {

        /**
         * コンストラクタ.
         * param context application context
         */
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + VIRTUAL_DEVICE_TBL_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + VIRTUAL_PROFILE_TBL_NAME);
            createDB(db);
        }

        /**
         * Virtual Device と Virtual Profile を管理するDB の作成.
         * param db DB
         */
        private void createDB(final SQLiteDatabase db) {
            String virtualDeviceSQL = "CREATE TABLE " + VIRTUAL_DEVICE_TBL_NAME + " ("
                    + VIRTUAL_DEVICE_COL_SERVICE_ID + " TEXT NOT NULL, "
                    + VIRTUAL_DEVICE_COL_DEVICE_NAME + " TEXT NOT NULL, "
                    + VIRTUAL_DEVICE_COL_CATEGORY_NAME + " TEXT NOT NULL"
                    + ");";
            db.execSQL(virtualDeviceSQL);
            String virtualProfileSQL = "CREATE TABLE " + VIRTUAL_PROFILE_TBL_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + VIRTUAL_PROFILE_COL_NAME + " TEXT NOT NULL, "
                    + VIRTUAL_PROFILE_COL_SERVICE_ID + " TEXT NOT NULL, "
                    + VIRTUAL_PROFILE_COL_PROFILE + " TEXT NOT NULL, "
                    + VIRTUAL_PROFILE_COL_METHOD + " TEXT NOT NULL, "
                    + VIRTUAL_PROFILE_COL_URI + " TEXT NOT NULL, "
                    + VIRTUAL_PROFILE_COL_IR + " TEXT "
                    + ");";
            db.execSQL(virtualProfileSQL);
        }
    }
}
