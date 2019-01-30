package org.deviceconnect.android.manager.core.accesslog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class AccessLogHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DeviceConnectAccessLog.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "accessLog";
    private static final String COLUMN_KEY = "name";

    AccessLogHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_KEY + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    <T> T read(Process handler) {
        T obj = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            obj = handler.process(db);
        } finally {
            db.close();
        }
        return obj;
    }

    <Boolean> Boolean write(Process<Boolean> handler) {
        Boolean obj = false;
        SQLiteDatabase db = getReadableDatabase();
        try {
            obj = handler.process(db);
        } finally {
            db.close();
        }
        return obj;
    }

    interface Process<T> {
        T process(SQLiteDatabase db);
    }
}
