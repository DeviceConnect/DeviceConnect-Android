package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * 仮想サービスのデータを管理します.
 */
public class VirtualServiceDBHelper {
    /**
     * データベースを保持するファイル名.
     */
    private static final String DB_NAME = "virtual_service.db";

    /**
     * データベールのバージョン.
     */
    private static final int DB_VERSION = 1;

    private static final String TBL_SERVICE_NAME = "virtual_service_tbl";
    private static final String COL_SERVICE_ID = "serviceId";
    private static final String COL_SERVICE_NAME = "name";

    private static final String TBL_PROFILE_NAME = "virtual_profile_tbl";
    private static final String COL_PROFILE_SERVICE_ID = "serviceId";
    private static final String COL_PROFILE_TYPE = "type";
    private static final String COL_PROFILE_PIN = "pin";

    /**
     * pinを区切る文字列.
     */
    private static final String SEPARATOR = ",";

    /**
     * DBを操作するためのヘルパークラス.
     */
    private DBHelper mDBHelper;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public VirtualServiceDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * サービスIDを作成します.
     * @return サービスID
     */
    public String createServiceId() {
        return "fabo_" + getServiceBaseId() + "_service_id";
    }

    /**
     * DBに仮想サービスデータを追加します.
     * @param serviceData 追加を行うサービスデータ
     * @return 挿入したデータのrow情報
     */
    public long addServiceData(final ServiceData serviceData) {
        ContentValues values = new ContentValues();
        values.put(COL_SERVICE_ID, serviceData.getServiceId());
        values.put(COL_SERVICE_NAME, serviceData.getName());

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            long row = db.insert(TBL_SERVICE_NAME, null, values);
            if (row > 0) {
                for (ProfileData p : serviceData.getProfileDataList()) {
                    addProfileData(p);
                }
            }
            return row;
        } finally {
            db.close();
        }
    }

    /**
     * DBから仮想サービスデータを削除します.
     * @param serviceData 削除を行うサービスデータ
     */
    public long removeServiceData(final ServiceData serviceData) {
        String whereClause = COL_SERVICE_ID + "=?";
        String[] whereArgs = {
                serviceData.getServiceId()
        };

        for (ProfileData p : serviceData.getProfileDataList()) {
            removeProfileData(p);
        }

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_SERVICE_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * 仮想サービスデータを更新します.
     * @param serviceData 更新する仮想サービスデータ
     * @return 更新したDBのカラム数
     */
    public long updateServiceData(final ServiceData serviceData) {
        ContentValues values = new ContentValues();
        values.put(COL_SERVICE_NAME, serviceData.getName());

        String whereClause = COL_SERVICE_ID + "=?";
        String[] whereArgs = {
                serviceData.getServiceId()
        };


        List<ProfileData> old = getProfileDataList(serviceData.getServiceId());

        for (ProfileData p : serviceData.getProfileDataList()) {
            // アップデートに失敗した場合には、プロファイルが存在しなかった
            // 可能性があるので、追加処理を行っておく。
            if (updateProfileData(p) <= 0) {
                addProfileData(p);
            }
            old.remove(p);
        }

        // 削除されたプロファイルはDBからも削除しておく
        for (ProfileData p : old) {
            removeProfileData(p);
        }

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_SERVICE_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * 指定された仮想サービスのIDに対応するサービスデータを取得します.
     * @param vid 仮想サービスのID
     * @return ServiceDataのインスタンス
     */
    public ServiceData getServiceData(final String vid) {
        String sql = "SELECT * FROM " + TBL_SERVICE_NAME + " WHERE " + COL_SERVICE_ID + "=?";
        String[] selectionArgs = {
                vid
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            boolean next = cursor.moveToFirst();
            if (next) {
                ServiceData service = new ServiceData();
                service.setServiceId(cursor.getString(cursor.getColumnIndex(COL_SERVICE_ID)));
                service.setName(cursor.getString(cursor.getColumnIndex(COL_SERVICE_NAME)));
                service.setProfileDataList(getProfileDataList(service.getServiceId()));
                return service;
            }
            return null;
        } finally {
            cursor.close();
            db.close();
        }
    }

    /**
     * DBから仮想サービスデータのリストを取得します.
     * <p>
     * 一つも登録されていない場合には、空のListを返却します。
     * </p>
     * @return 仮想サービスデータのリスト
     */
    public List<ServiceData> getServiceDataList() {
        String sql = "SELECT * FROM " + TBL_SERVICE_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            List<ServiceData> serviceDatas = new ArrayList<>();
            boolean next = cursor.moveToFirst();
            while (next) {
                ServiceData service = new ServiceData();
                service.setServiceId(cursor.getString(cursor.getColumnIndex(COL_SERVICE_ID)));
                service.setName(cursor.getString(cursor.getColumnIndex(COL_SERVICE_NAME)));
                service.setProfileDataList(getProfileDataList(service.getServiceId()));
                serviceDatas.add(service);
                next = cursor.moveToNext();
            }
            return serviceDatas;
        } finally {
            cursor.close();
            db.close();
        }
    }

    /**
     * プロファイルデータを追加します.
     * @param profileData 追加するプロファイルデータ
     */
    public long addProfileData(final ProfileData profileData) {
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_SERVICE_ID, profileData.getServiceId());
        values.put(COL_PROFILE_TYPE, profileData.getType().getValue());
        values.put(COL_PROFILE_PIN, convertList2String(profileData.getPinList()));

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_PROFILE_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * プロファイルデータを削除します.
     * @param profileData 削除するプロファイルデータ
     */
    public long removeProfileData(final ProfileData profileData) {
        String whereClause = COL_PROFILE_SERVICE_ID + "=? AND " + COL_PROFILE_TYPE + "=?";
        String[] whereArgs = {
                profileData.getServiceId(),
                "" + profileData.getType().getValue()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_PROFILE_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたプロファイルデータの情報を更新します.
     * @param profileData 更新するプロファイルデータ
     * @return 更新したDBのカラム数
     */
    public long updateProfileData(final ProfileData profileData) {
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_PIN, convertList2String(profileData.getPinList()));

        String whereClause = COL_PROFILE_SERVICE_ID + "=? AND " + COL_PROFILE_TYPE + "=?";
        String[] whereArgs = {
                profileData.getServiceId(),
                String.valueOf(profileData.getType().getValue())
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_PROFILE_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたサービスIDに対応するDBからプロファイルデータのリストを取得します.
     * <p>
     * 一つも登録されていない場合には、空のListを返却します。
     * </p>
     * @param serviceId サービスID
     * @return プロファイルデータのリスト
     */
    public List<ProfileData> getProfileDataList(final String serviceId) {
        String sql = "SELECT * FROM " + TBL_PROFILE_NAME + " WHERE " + COL_PROFILE_SERVICE_ID + "=?";
        String[] selectionArgs = { serviceId };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            List<ProfileData> profileDatas = new ArrayList<>();
            boolean next = cursor.moveToFirst();
            while (next) {
                ProfileData profile = new ProfileData();
                profile.setServiceId(cursor.getString(cursor.getColumnIndex(COL_PROFILE_SERVICE_ID)));
                profile.setType(ProfileData.Type.getType(cursor.getInt(cursor.getColumnIndex(COL_PROFILE_TYPE))));
                profile.setPinList(convertString2List(cursor.getString(cursor.getColumnIndex(COL_PROFILE_PIN))));
                profileDatas.add(profile);
                next = cursor.moveToNext();
            }
            return profileDatas;
        } finally {
            cursor.close();
            db.close();
        }
    }

    /**
     * サービスデータのテーブルにあるBaseColumns._IDの最大値を取得します.
     * @return テーブルにあるBaseColumns._IDの最大値
     */
    private int getServiceBaseId() {
        String sql = "SELECT * FROM " + TBL_SERVICE_NAME + " ORDER BY " + BaseColumns._ID + " DESC";
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)) + 1;
            }
        } finally {
            cursor.close();
            db.close();
        }
        return 0;
    }

    private String convertList2String(final List<Integer> pins) {
        StringBuilder sb = new StringBuilder();
        for (Integer str : pins) {
            if (sb.length() > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    private List<Integer> convertString2List(final String str) {
        List<Integer> list = new ArrayList<>();
        for (String s : str.split(SEPARATOR)) {
            try {
                list.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // do noting.
            }
        }
        return list;
    }

    /**
     * DB操作を行うためのヘルパークラス.
     */
    private class DBHelper extends SQLiteOpenHelper {
        /**
         * コンストラクタ.
         * @param context コンテキスト
         */
        DBHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL_SERVICE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_PROFILE_NAME);
            createDB(db);
        }

        private void createDB(final SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TBL_SERVICE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                    + COL_SERVICE_ID + " TEXT NOT NULL, "
                    + COL_SERVICE_NAME + " TEXT NOT NULL"
                    + ");";
            db.execSQL(sql);

            String sql2 = "CREATE TABLE " + TBL_PROFILE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                    + COL_PROFILE_SERVICE_ID + " TEXT NOT NULL, "
                    + COL_PROFILE_TYPE + " INTEGER, "
                    + COL_PROFILE_PIN + " TEXT NOT NULL"
                    + ");";
            db.execSQL(sql2);
        }
    }
}
