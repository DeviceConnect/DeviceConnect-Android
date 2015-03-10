/*
 Whitelist.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Whitelist of origins.
 * 
 * @author NTT DOCOMO, INC.
 */
public class Whitelist {

    /** The origin database. */
    private final OriginDB mCache;

    /**
     * Constructor.
     * 
     * @param context Context
     */
    public Whitelist(final Context context) {
        mCache = new OriginDB(context);
    }

    /**
     * Returns whether requests from the specified origin are allowed.
     * 
     * @param origin Origin of requests
     * @return <code>true</code> if requests from the specified origin are allowed, 
     *      otherwise <code>false</code>.
     */
    public boolean allows(final Origin origin) {
        List<OriginInfo> patterns = getOrigins();
        for (OriginInfo p : patterns) {
            if (p.matches(origin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns origin.
     * 
     * @return Origin
     */
    public synchronized List<OriginInfo> getOrigins() {
        return mCache.getOrigins();
    }

    /**
     * Returns whether the specified origin is included in this whitelist.
     * @param originExp a string expression of origin
     * @return <code>true</code> if origin is included, otherwise <code>false</code>
     */
    public synchronized boolean hasOrigin(final String originExp) {
        for (OriginInfo info : mCache.getOrigins()) {
            if (info.matches(OriginParser.parse(originExp))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an origin to this whitelist.
     * 
     * @param origin an origin to be added.
     * @param title the title of origin.
     * @return An instance of {@link OriginInfo}
     * @throws WhitelistException if origin can not be stored.
     */
    public synchronized OriginInfo addOrigin(final Origin origin, final String title)
            throws WhitelistException {
        try {
            long date = System.currentTimeMillis();
            long id = mCache.addOrigin(origin, title, date);
            return new OriginInfo(id, origin, title, date);
        } catch (OriginDBException e) {
            throw new WhitelistException("Failed to store origin: " + origin, e);
        }
    }

    /**
     * Update an origin on this whitelist.
     * 
     * @param info an origin to be updated.
     * @throws WhitelistException if origin can not be updated.
     */
    public synchronized void updateOrigin(final OriginInfo info) throws WhitelistException {
        try {
            mCache.updateOrigin(info);
        } catch (OriginDBException e) {
            throw new WhitelistException("Failed to store origin: " + info.mOrigin, e);
        }
    }

    /**
     * Removes an origin from this whitelist.
     * 
     * @param info an origin to be removed.
     * @throws WhitelistException if origin can not be removed.
     */
    public synchronized void removeOrigin(final OriginInfo info) throws WhitelistException {
        try {
            mCache.removeOrigin(info);
        } catch (OriginDBException e) {
            throw new WhitelistException("Failed to remove origin: " + info.mOrigin, e);
        }
    }

    /**
     * Origin database.
     */
    private static class OriginDB extends SQLiteOpenHelper {

        /** 
         * The DB file name.
         */
        private static final String DB_NAME = "__device_connect_whitelist.db";

        /** 
         * The DB Version.
         */
        private static final int DB_VERSION = 1;

        /**
         * The table name.
         */
        private static final String TABLE_NAME = "Origins";

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
         * The title of origin.
         * <p>Type: TEXT</p>
         */
        private static final String TITLE = "title";

        /**
         * The registration date.
         * <p>Type: INTEGER</p>
         */
        private static final String DATE = "date";

        /**
         * CREATE TABLE statement.
         */
        private static final String CREATE = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ORIGIN + " TEXT NOT NULL, "
                + TITLE + " TEXT NOT NULL, "
                + DATE + " INTEGER);";

        /**
         * DROP TABLE statement.
         */
        private static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * Constructor.
         * @param context Context
         */
        public OriginDB(final Context context) {
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
         * Gets all origins in the database.
         * @return all origins in the database
         */
        List<OriginInfo> getOrigins() {
            List<OriginInfo> patterns = new ArrayList<OriginInfo>();
            SQLiteDatabase db = openDB();
            if (db == null) {
                return patterns;
            }

            Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    String originExp = c.getString(1);
                    Origin origin = OriginParser.parse(originExp);
                    String title = c.getString(2);
                    long date = c.getLong(3);
                    patterns.add(new OriginInfo(id, origin, title, date));
                } while (c.moveToNext());
            }
            c.close();
            db.close();
            return patterns;
        }

        /**
         * Adds a origin to this database.
         * @param origin the origin
         * @param title the title of origin
         * @param date the registration date
         * @return row ID
         * @throws OriginDBException throws if database error occurred
         */
        long addOrigin(final Origin origin, final String title, final long date) throws OriginDBException {
            SQLiteDatabase db = openDB();
            if (db == null) {
                throw new OriginDBException("Failed to open the database.");
            }
            long id;
            try {
                db.beginTransaction();
                ContentValues values = new ContentValues();
                values.put(ORIGIN, origin.toString());
                values.put(TITLE, title);
                values.put(DATE, date);
                id = db.insert(TABLE_NAME, null, values);
                if (id == -1) {
                    throw new OriginDBException("Failed to store origin.");
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
            return id;
        }

        /**
         * Updates a origin to this database.
         * @param origin the origin to be updated
         * @throws OriginDBException throws if database error occurred
         */
        void updateOrigin(final OriginInfo origin) throws OriginDBException {
            SQLiteDatabase db = openDB();
            if (db == null) {
                throw new OriginDBException("Failed to open the database.");
            }
            try {
                db.beginTransaction();
                ContentValues values = new ContentValues();
                values.put(ORIGIN, origin.mOrigin.toString());
                values.put(TITLE, origin.mTitle);
                int count = db.update(TABLE_NAME, values, ID + "=" + origin.mId, null);
                if (count != 1) {
                    throw new OriginDBException("Failed to store origin.");
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
        }

        /**
         * Removes a origin from this database.
         * @param origin the origin to be removed
         * @throws OriginDBException if the origin cannot be removed
         * @throws IllegalArgumentException if <code>id</code> equals -1.
         */
        void removeOrigin(final OriginInfo origin) throws OriginDBException {
            if (origin.mId == -1) {
                throw new IllegalArgumentException("The row ID is illegal.");
            }

            SQLiteDatabase db = openDB();
            if (db == null) {
                throw new OriginDBException("Failed to open origin whitelist database.");
            }
            try {
                db.beginTransaction();
                int count = db.delete(TABLE_NAME, ID + "=" + origin.mId, null);
                if (count != 1) {
                    throw new OriginDBException("Failed to remove origin: " + origin.mOrigin);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
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

    /**
     * Exception of origin database management.
     */
    private static class OriginDBException extends Exception {

        /** Serial Version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         * @param message the detail message for this exception.
         */
        public OriginDBException(final String message) {
            super(message);
        }
    }
}

