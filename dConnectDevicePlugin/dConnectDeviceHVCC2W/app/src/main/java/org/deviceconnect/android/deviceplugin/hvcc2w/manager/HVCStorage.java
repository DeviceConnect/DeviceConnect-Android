/*
 HVCStorage
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcc2w.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.FaceRecognitionDataModel;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.FaceRecognitionObject;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.UserDataModel;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.UserDataObject;

import java.util.ArrayList;
import java.util.List;

/**
 * HVC Storage.
 * @author NTT DOCOMO, INC.
 */
public enum HVCStorage {
    /**
     * Singleton instance.
     */
    INSTANCE;
    /**
     * DB Name.
     */
    private static final String DB_NAME = "hvc_c2w_deviceconnect.db";

    /**
     * DB Version.
     */
    private static final int DB_VERSION = 1;

    /**
     * User Data Table.
     */
    private static final String USER_DATA_TBL_NAME = "user_data_tbl";

    /**
     * Face Recognition Table.
     */
    private static final String FACE_RECOGNITION_TBL_NAME = "face_recognition_tbl";

    /**
     * User Data param: {@value} .
     */
    private static final String USER_EMAIL = "email";

    /**
     * User Data param: {@value}.
     */
    private static final String USER_PASSWORD = "password";

    /**
     * User Data Param: {@value}.
     */
    private static final String USER_ACCESS_TOKEN = "access_token";


    /**
     * Face Recognition Param: {@value}.
     */
    private static final String FACE_RECOGNITION_NAME = "name";
    /**
     * Face Recognition Param: {@value}.
     */
    private static final String FACE_RECOGNITION_SERVICE_ID = "service_id";
    /**
     * Face Recognition Param: {@value}.
     */
    private static final String FACE_RECOGNITION_USER_ID = "user_id";
    /**
     * Face Recognition Param: {@value}.
     */
    private static final String FACE_RECOGNITION_DATA_ID = "data_id";
    /**
     * DB Helper.
     */
    private HVCDBHelper mHVCDBHelper;


    /**
     * Constructor.
     */
    private HVCStorage() {
    }

    /**
     * Initialize.
     *
     * @param context Context。
     */
    public void init(final Context context) {
        mHVCDBHelper = new HVCDBHelper(context);
    }


    /**
     * Register HVC User Data.
     * @param object User Data Object
     * @return Success or failure
     */
    public synchronized long registerUserData(final UserDataObject object) {
        ContentValues values = makeUserDataContentValue(object);
        SQLiteDatabase db = mHVCDBHelper.getWritableDatabase();
        long result = -1;
        try {
            result = db.insert(USER_DATA_TBL_NAME, null, values);
        } finally {
            db.close();
            return result;
        }
    }

    /**
     * Register Face Recognition Data.
     * @param object Face Recognition Data Object
     * @return Success or failure
     */
    public synchronized long registerFaceRecognitionData(final FaceRecognitionObject object) {
        ContentValues values = makeFaceRecognitionDataContentValue(object);
        SQLiteDatabase db = mHVCDBHelper.getWritableDatabase();
        long result = -1;
        try {
            result = db.insert(FACE_RECOGNITION_TBL_NAME, null, values);
        } finally {
            db.close();
            return result;
        }
    }


    /**
     * Remove HVC User Data Object.
     * @param email User Data Address
     * @return Success or failure
     */
    public synchronized long removeUserData(final String email) {
        String whereClause = USER_EMAIL + "=?";
        String[] whereArgs = {
                email
        };
        SQLiteDatabase db = mHVCDBHelper.getWritableDatabase();
        int isDelete = -1;
        try {
            isDelete = db.delete(USER_DATA_TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
            return isDelete;
        }
    }

    /**
     * Remove Face Recognition Object.
     * @param name Face Recognition name
     * @return Success or failure
     */
    public synchronized long removeFaceRecognitionData(final String name) {
        String whereClause = FACE_RECOGNITION_NAME + "=?";
        String[] whereArgs = {
                name
        };
        SQLiteDatabase db = mHVCDBHelper.getWritableDatabase();
        int isDelete = -1;
        try {
            isDelete = db.delete(FACE_RECOGNITION_TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
            return isDelete;
        }
    }

    /**
     * Get User Data List.
     * @return UserDataList
     */
    public synchronized List<UserDataObject> getUserDatas(final String email) {
        String sql = "SELECT * FROM " + USER_DATA_TBL_NAME;
        if (email != null) {
            sql += " WHERE " + USER_EMAIL + "='" + email + "' ";
        }

        String[] selectionArgs = {};
        SQLiteDatabase db = null;
        List<UserDataObject> objects = new ArrayList<UserDataObject>();
        try {
            db = mHVCDBHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, selectionArgs);

            boolean next = cursor.moveToFirst();
            while (next) {
                String emailAddress = cursor.getString(cursor.getColumnIndex(USER_EMAIL));
                String password = cursor.getString(cursor.getColumnIndex(USER_PASSWORD));
                String accessToken = cursor.getString(cursor.getColumnIndex(USER_ACCESS_TOKEN));
                UserDataObject object = new UserDataModel(emailAddress, password, accessToken);
                objects.add(object);
                next = cursor.moveToNext();
            }
        } finally {
            db.close();
        }
        return objects;
    }

    /**
     * Get Face Recognition Data List.
     * @return Face Recognition Data List
     */
    public synchronized List<FaceRecognitionObject> getFaceRecognitionDatas(final String name) {
        String sql = "SELECT * FROM " + FACE_RECOGNITION_TBL_NAME;
        if (name != null) {
            sql += " WHERE " + FACE_RECOGNITION_NAME + "='" + name + "' ";
        }
        sql += " ORDER BY " + FACE_RECOGNITION_USER_ID + " ASC;";

        String[] selectionArgs = {};
        SQLiteDatabase db = null;
        List<FaceRecognitionObject> objects = new ArrayList<FaceRecognitionObject>();
        try {
            db = mHVCDBHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, selectionArgs);

            boolean next = cursor.moveToFirst();
            while (next) {
                String n = cursor.getString(cursor.getColumnIndex(FACE_RECOGNITION_NAME));
                String deviceId = cursor.getString(cursor.getColumnIndex(FACE_RECOGNITION_SERVICE_ID));
                int userId = cursor.getInt(cursor.getColumnIndex(FACE_RECOGNITION_USER_ID));
                int dataId = cursor.getInt(cursor.getColumnIndex(FACE_RECOGNITION_DATA_ID));
                FaceRecognitionObject object = new FaceRecognitionDataModel(n, deviceId, userId, dataId);
                objects.add(object);
                next = cursor.moveToNext();
            }
        } finally {
            db.close();
        }
        return objects;
    }
    /**
     * Get Face Recognition Data List For UserId.
     * @param id UserId
     * @return Face Recognition Data List
     */
    public synchronized List<FaceRecognitionObject> getFaceRecognitionDatasForUserId(final int id) {
        String sql = "SELECT * FROM " + FACE_RECOGNITION_TBL_NAME;
        if (id > -1) {
            sql += " WHERE " + FACE_RECOGNITION_USER_ID + "=" + id + "";
        }
        sql += " ORDER BY " + FACE_RECOGNITION_USER_ID + " ASC;";

        String[] selectionArgs = {};
        SQLiteDatabase db = null;
        List<FaceRecognitionObject> objects = new ArrayList<FaceRecognitionObject>();
        try {
            db = mHVCDBHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, selectionArgs);

            boolean next = cursor.moveToFirst();
            while (next) {
                String n = cursor.getString(cursor.getColumnIndex(FACE_RECOGNITION_NAME));
                String deviceId = cursor.getString(cursor.getColumnIndex(FACE_RECOGNITION_SERVICE_ID));
                int userId = cursor.getInt(cursor.getColumnIndex(FACE_RECOGNITION_USER_ID));
                int dataId = cursor.getInt(cursor.getColumnIndex(FACE_RECOGNITION_DATA_ID));
                FaceRecognitionObject object = new FaceRecognitionDataModel(n, deviceId, userId, dataId);
                objects.add(object);
                next = cursor.moveToNext();
            }
        } finally {
            db.close();
        }
        return objects;
    }
    /**
     * Get Face Recognition Data List For UserId.
     * @param id UserId
     * @return Face Recognition Data List
     */
    public synchronized List<FaceRecognitionObject> getFaceRecognitionDatasForDeviceId(final String id) {
        String sql = "SELECT * FROM " + FACE_RECOGNITION_TBL_NAME;
        if (id != null) {
            sql += " WHERE " + FACE_RECOGNITION_SERVICE_ID + "='" + id + "'";
        }
        sql += " ORDER BY " + FACE_RECOGNITION_USER_ID + " ASC;";

        String[] selectionArgs = {};
        SQLiteDatabase db = null;
        List<FaceRecognitionObject> objects = new ArrayList<FaceRecognitionObject>();
        try {
            db = mHVCDBHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, selectionArgs);

            boolean next = cursor.moveToFirst();
            while (next) {
                String n = cursor.getString(cursor.getColumnIndex(FACE_RECOGNITION_NAME));
                String deviceId = cursor.getString(cursor.getColumnIndex(FACE_RECOGNITION_SERVICE_ID));
                int userId = cursor.getInt(cursor.getColumnIndex(FACE_RECOGNITION_USER_ID));
                int dataId = cursor.getInt(cursor.getColumnIndex(FACE_RECOGNITION_DATA_ID));
                FaceRecognitionObject object = new FaceRecognitionDataModel(n, deviceId, userId, dataId);
                objects.add(object);
                next = cursor.moveToNext();
            }
        } finally {
            db.close();
        }
        return objects;
    }
    /** Make User Data Content Value. */
    private ContentValues makeUserDataContentValue(final UserDataObject object) {
        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, object.getEmail());
        values.put(USER_PASSWORD, object.getPassword());
        values.put(USER_ACCESS_TOKEN, object.getAccessToken());
        return values;
    }

    /** Make Face Recognition Data Content Value. */
    private ContentValues makeFaceRecognitionDataContentValue(final FaceRecognitionObject object) {
        ContentValues values = new ContentValues();
        values.put(FACE_RECOGNITION_NAME, object.getName());
        values.put(FACE_RECOGNITION_SERVICE_ID, object.getDeviceId());
        values.put(FACE_RECOGNITION_USER_ID, object.getUserId());
        values.put(FACE_RECOGNITION_DATA_ID, object.getDataId());
        return values;
    }


    /**
     * DB Helper to store the HVC storage
     */
    private class HVCDBHelper extends SQLiteOpenHelper {

        /**
         * Constructor.
         * @param context application context
         */
        public HVCDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + USER_DATA_TBL_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + FACE_RECOGNITION_TBL_NAME);
            createDB(db);
        }

        /**
         * DB to store the HVC storage
         * @param db DB
         */
        private void createDB(final SQLiteDatabase db) {
            String userdataSQL = "CREATE TABLE " + USER_DATA_TBL_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USER_EMAIL + " TEXT NOT NULL,"
                    + USER_PASSWORD + " TEXT,"
                    + USER_ACCESS_TOKEN + " TEXT"
                    + ");";
            db.execSQL(userdataSQL);
            String faceRecognitionSQL = "CREATE TABLE " + FACE_RECOGNITION_TBL_NAME + " ("
                    + FACE_RECOGNITION_NAME + " TEXT NOT NULL PRIMARY KEY,"
                    + FACE_RECOGNITION_SERVICE_ID + " TEXT,"
                    + FACE_RECOGNITION_USER_ID + " INTEGER,"
                    + FACE_RECOGNITION_DATA_ID + " INTEGER"
                    + ");";
            db.execSQL(faceRecognitionSQL);
        }
    }
}
