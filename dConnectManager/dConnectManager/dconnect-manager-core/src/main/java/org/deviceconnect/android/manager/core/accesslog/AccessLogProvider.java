package org.deviceconnect.android.manager.core.accesslog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class AccessLogProvider {
    private AccessLogHelper mAccessLogHelper;

    public AccessLogProvider(Context context) {
        mAccessLogHelper = new AccessLogHelper(context);
    }

    public List<AccessLog> query(final String date) {
        String selection = "";
        String[] selectionArgs = {};
        return mAccessLogHelper.read((db) -> {
            List<AccessLog> list = new ArrayList<>();
            Cursor cs = db.query(TABLE_NAME, null, selection, selectionArgs,
                    null, null, null);
            if (cs != null) {
                try {
                    while (cs.moveToNext()) {
                        list.add(createAccessLog(cs));
                    }
                } finally {
                    cs.close();
                }
            }
            return list;
        });
    }

    public void add(AccessLog accessLog) {
    }

    public void remove(AccessLog accessLog) {
    }

    public void remove(String date) {
    }

    public void removeAll() {
    }

    private AccessLog createAccessLog(Cursor c) {
        return new AccessLog();
    }
}
