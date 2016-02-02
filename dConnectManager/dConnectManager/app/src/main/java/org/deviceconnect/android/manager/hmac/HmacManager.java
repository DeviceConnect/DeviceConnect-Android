/*
 HmacManager.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.hmac;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * HMAC Manager.
 * @author NTT DOCOMO, INC.
 */
public final class HmacManager {

    /** 
     * The hash algorithm.
     */
    private static final String HASH_ALGORITHM = "HmacSHA256";

    /**
     * The empty string.
     */
    private static final String EMPTY = "";

    /**
     * The HMAC key database.
     */
    private final HmacKeyDB mCache;

    /**
     * Constructor.
     * @param context Context
     */
    public HmacManager(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null.");
        }
        mCache = new HmacKeyDB(context);
    }

    /**
     * Updates HMAC key by the key included in the specified request.
     * 
     * @param origin Origin of application
     * @param key HMAC key
     */
    public void updateKey(final String origin, final String key) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }
        if (origin.equals(EMPTY)) {
            throw new IllegalArgumentException("origin is an empty string.");
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (key.equals(EMPTY)) {
            mCache.removeKey(origin);
        } else {
            mCache.addKey(origin, key);
        }
    }

    /**
     * Returns whether the client uses HMAC or not.
     * 
     * @param origin Origin of application
     * @return true if the client uses HMAC, otherwise false
     */
    public boolean usesHmac(final String origin) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }
        return mCache.hasKey(origin);
    }

    /**
     * Generates HMAC.
     * 
     * @param origin Origin of application
     * @param nonce Nonce
     * @return HMAC
     */
    public String generateHmac(final String origin, final String nonce) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }
        if (nonce == null) {
            throw new IllegalArgumentException("nonce is null.");
        }
        HmacKey hmacKey = mCache.getKey(origin);
        if (hmacKey == null) {
            return null;
        }
        // HMAC generation with key and nonce.
        try {
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(toByteArray(hmacKey.getKey()), HASH_ALGORITHM);
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(toByteArray(nonce));
            return toHexString(hmac);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(HASH_ALGORITHM + " is not supported.");
        } catch (InvalidKeyException e) {
            throw new RuntimeException("keySpec is null.");
        }
    }

    /**
     * Parse a hex string expression of a byte array to raw.
     * @param b a hex string expression of a byte array
     * @return A raw byte array
     */
    private static byte[] toByteArray(final String b) {
        String c = b;
        if (c.length() % 2 != 0) {
            c = "0" + c;
        }
        byte[] array = new byte[b.length() / 2];
        for (int i = 0; i < b.length() / 2; i++) {
            String hex = b.substring(2 * i, 2 * i + 2);
            array[i] = (byte) Integer.parseInt(hex, 16);
        }
        return array;
    }

    /**
     * Returns a hex string expression of a byte array.
     * 
     * @param b A byte array
     * @return A string expression of a byte array
     */
    private static String toHexString(final byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException("b is null.");
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String substr = Integer.toHexString(b[i] & 0xff);
            if (substr.length() < 2) {
                str.append("0");
            }
            str.append(substr);
        }
        return str.toString();
    }

    /**
     * HMAC key database.
     */
    private static class HmacKeyDB extends SQLiteOpenHelper {

        /** 
         * The DB file name.
         */
        private static final String DB_NAME = "__device_connect_hmac.db";

        /** 
         * The DB Version.
         */
        private static final int DB_VERSION = 1;

        /**
         * The table name.
         */
        private static final String TABLE_NAME = "HmacKey";

        /**
         * The unique ID for a row.
         * <p>Type: INTEGER (long)</p>
         */
        private static final String ID = BaseColumns._ID;

        /**
         * The origin.
         * <p>Type: TEXT</p>
         */
        private static final String ORIGIN = "origin";

        /**
         * The HMAC key.
         * <p>Type: TEXT</p>
         */
        private static final String HMAC_KEY = "hmac_key";

        /**
         * CREATE TABLE statement.
         */
        private static final String CREATE = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ORIGIN + " TEXT NOT NULL, "
                + HMAC_KEY + " TEXT NOT NULL, UNIQUE(" + ORIGIN + "));";

        /**
         * DROP TABLE statement.
         */
        private static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * Constructor.
         * @param context Context
         */
        public HmacKeyDB(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(CREATE);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL(DROP);
        }

        /**
         * Gets HMAC key for origin.
         * @param origin Origin
         * @return HMAC key
         */
        synchronized HmacKey getKey(final String origin) {
            SQLiteDatabase db = openDB();
            if (db == null) {
                return null;
            }
            Cursor c = db.query(TABLE_NAME, null, ORIGIN + "=?", new String[]{origin}, null, null, null);
            HmacKey key = null;
            if (c.moveToFirst()) {
                key = new HmacKey(c.getString(1), c.getString(2));
            }
 
            c.close();
            db.close();
            return key;
        }

        /**
         * Checks whether HMAC key exists for the specified origin.
         * @param origin Origin
         * @return true if HMAC key exists for the specified origin, otherwise false.
         */
        boolean hasKey(final String origin) {
            return getKey(origin) != null;
        }

        /**
         * Adds a HMAC key for the specified origin.
         * @param origin Origin
         * @param key HMAC key
         * @return Result
         */
        synchronized HmacKeyError addKey(final String origin, final String key) {
            if (origin == null) {
                throw new IllegalArgumentException("origin is null");
            }
            if (origin.equals(EMPTY)) {
                throw new IllegalArgumentException("origin is an empty string.");
            }
            if (key == null) {
                throw new IllegalArgumentException("key is null");
            }
            if (key.equals(EMPTY)) {
                throw new IllegalArgumentException("key is an empty string.");
            }

            SQLiteDatabase db = openDB();
            if (db == null) {
                return HmacKeyError.FAILED;
            }
            Cursor c = null;
            try {
                db.beginTransaction();
                c = db.query(TABLE_NAME, null, ORIGIN + "=?", new String[]{origin}, null, null, null);
                if (c.getCount() == 0) {
                    ContentValues values = new ContentValues();
                    values.put(ORIGIN, origin);
                    values.put(HMAC_KEY, key);
                    long id = db.insert(TABLE_NAME, null, values);
                    if (id == -1) {
                        return HmacKeyError.FAILED;
                    }
                } else if (c.moveToFirst()) {
                    long id = c.getLong(0);
                    ContentValues values = new ContentValues();
                    values.put(HMAC_KEY, key);
                    int count = db.update(TABLE_NAME, values, ID + "=?", new String[]{"" + id});
                    if (count != 1) {
                        return HmacKeyError.FAILED;
                    }
                } else {
                    return HmacKeyError.FAILED;
                }
                db.setTransactionSuccessful();
            } finally {
                if (c != null) {
                    c.close();
                }
                db.endTransaction();
                db.close();
            }
            return HmacKeyError.NONE;
        }

        /**
         * Removes a HMAC key for the specified origin.
         * @param origin Origin
         * @return Result
         */
        synchronized HmacKeyError removeKey(final String origin) {
            if (origin == null) {
                throw new IllegalArgumentException("origin is null");
            }
            if (origin.equals(EMPTY)) {
                throw new IllegalArgumentException("origin is an empty string.");
            }

            SQLiteDatabase db = openDB();
            if (db == null) {
                return HmacKeyError.FAILED;
            }
            try {
                int count = db.delete(TABLE_NAME, ORIGIN + "=?", new String[]{origin});
                if (count != 1) {
                    return HmacKeyError.FAILED;
                }
            } finally {
                db.close();
            }
            return HmacKeyError.NONE;
        }

        /**
         * Opens the database.
         * 
         * @return An instance of the database
         */
        SQLiteDatabase openDB() {
            SQLiteDatabase db;
            try {
                db = getWritableDatabase();
            } catch (SQLiteException e) {
                db = null;
            }
            return db;
        }
    }
}
