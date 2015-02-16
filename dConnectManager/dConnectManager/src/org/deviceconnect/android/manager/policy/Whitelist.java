/*
 Whitelist.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

import java.util.ArrayList;
import java.util.Iterator;
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

    /** The origin pattern database. */
    private final OriginPatternDB mCache;

    /**
     * Constructor.
     * 
     * @param context Context
     */
    public Whitelist(final Context context) {
        mCache = new OriginPatternDB(context);
    }

    /**
     * Returns whether requests from the specified origin are allowed.
     * 
     * @param origin Origin of requests
     * @return <code>true</code> if requests from the specified origin are allowed, 
     *      otherwise <code>false</code>.
     */
    public boolean allows(final String origin) {
        List<OriginPattern> patterns = getPatterns();
        for (OriginPattern p : patterns) {
            if (p.matches(origin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns origin patterns.
     * 
     * @return Origin patterns
     */
    public synchronized List<OriginPattern> getPatterns() {
        return mCache.getPatterns();
    }

    /**
     * Adds an origin pattern to this whitelist.
     * 
     * @param pattern An origin pattern
     * @return An instance of {@link OriginPattern}
     */
    public synchronized OriginPattern addPattern(final String pattern) {
        try {
            long id = mCache.addPattern(pattern);
            return new OriginPattern(id, pattern);
        } catch (OriginPatternDBException e) {
            return null;
        }
    }

    /**
     * Removes an origin pattern from this whitelist.
     * 
     * @param pattern An origin pattern
     * @return <code>true</code> if the pattern is removed successfully,
     *      otherwise <code>false</code>.
     */
    public synchronized boolean removePattern(final OriginPattern pattern) {
        try {
            mCache.removePattern(pattern.mId);
            for (Iterator<OriginPattern> it = getPatterns().iterator(); it.hasNext();) {
                OriginPattern p = it.next();
                if (p.equals(pattern)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        } catch (OriginPatternDBException e) {
            return false;
        }
    }

    /**
     * Origin pattern database.
     */
    private static class OriginPatternDB extends SQLiteOpenHelper {

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
        private static final String TABLE_NAME = "OriginPatterns";

        /**
         * The unique ID for a row.
         * <p>Type: INTEGER (long)</p>
         */
        private static final String ID = BaseColumns._ID;

        /**
         * The pattern of origin.
         * <p>Type: TEXT</p>
         */
        private static final String PATTERN = "pattern";

        /**
         * CREATE TABLE statement.
         */
        private static final String CREATE = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PATTERN + " TEXT NOT NULL);";

        /**
         * DROP TABLE statement.
         */
        private static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * Constructor.
         * @param context Context
         */
        public OriginPatternDB(final Context context) {
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
         * Gets all origin patterns in the database.
         * @return Origin patterns
         */
        List<OriginPattern> getPatterns() {
            SQLiteDatabase db = openDB();
            if (db == null) {
                return null;
            }
            Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
            List<OriginPattern> patterns = new ArrayList<OriginPattern>();
            if (c.moveToFirst()) {
                do {
                    patterns.add(new OriginPattern(c.getLong(0), c.getString(1)));
                } while (c.moveToNext());
            }
            c.close();
            db.close();
            return patterns;
        }

        /**
         * Adds a origin pattern to this database.
         * @param pattern Origin pattern
         * @return row ID
         * @throws OriginPatternDBException throws if database error occurred.
         */
        long addPattern(final String pattern) throws OriginPatternDBException {
            SQLiteDatabase db = openDB();
            if (db == null) {
                throw new OriginPatternDBException("Failed to open the database.");
            }
            Cursor c = null;
            long id;
            try {
                db.beginTransaction();
                ContentValues values = new ContentValues();
                values.put(PATTERN, pattern);
                id = db.insert(TABLE_NAME, null, values);
                if (id == -1) {
                    throw new OriginPatternDBException("Failed to store origin pattern.");
                }
                db.setTransactionSuccessful();
            } finally {
                if (c != null) {
                    c.close();
                }
                db.endTransaction();
                db.close();
            }
            return id;
        }

        /**
         * Removes a origin pattern from this database.
         * @param id row ID
         * @throws OriginPatternDBException throws if database error occurred.
         */
        void removePattern(final long id) throws OriginPatternDBException {
            if (id < 0) {
                throw new IllegalArgumentException("id is a negative number.");
            }

            SQLiteDatabase db = openDB();
            if (db == null) {
                throw new OriginPatternDBException("Failed to open the database.");
            }
            try {
                int count = db.delete(TABLE_NAME, ID + "=" + id, null);
                if (count != 1) {
                    throw new OriginPatternDBException("Failed to store origin pattern.");
                }
            } finally {
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
     * Exception of origin pattern database management.
     */
    private static class OriginPatternDBException extends Exception {

        /** Serial Version UID. */
        private static final long serialVersionUID = 1L;
        
        /**
         * Constructor.
         * @param message an error message
         */
        public OriginPatternDBException(final String message) {
            super(message);
        }
    }
}

