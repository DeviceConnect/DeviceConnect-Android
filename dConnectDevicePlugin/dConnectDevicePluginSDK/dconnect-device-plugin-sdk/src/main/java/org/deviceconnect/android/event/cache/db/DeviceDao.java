/*
 DeviceDao.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event.cache.db;

import org.deviceconnect.android.event.cache.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Deviceテーブル DAOクラス.
 * 
 * 
 * @author NTT DOCOMO, INC.
 */
final class DeviceDao implements DeviceSchema {

    /**
     * インスタンスを生成する.
     */
    private DeviceDao() {
    }

    /**
     * サービスIDを登録する.
     * 
     * @param db DB操作オブジェクト
     * @param serviceId サービスID
     * @return 登録出来た場合は登録時のIDを返す。重複している場合は登録済みのIDを返す。処理に失敗した場合は-1を返す。
     */
    static long insert(final SQLiteDatabase db, final String serviceId) {

        String did = (serviceId != null) ? serviceId : "";
        long result = -1L;
        Cursor cursor = db.query(TABLE_NAME, new String[] {_ID}, SERVICE_ID + "=?", new String[] {did}, null, null,
                null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(SERVICE_ID, did);
            values.put(CREATE_DATE, Utils.getCurreTimestamp().getTime());
            values.put(UPDATE_DATE, Utils.getCurreTimestamp().getTime());
            result = db.insert(TABLE_NAME, null, values);
        } else if (cursor.moveToFirst()) {
            if (cursor.getColumnIndex(_ID) != -1) {
                result = cursor.getLong(0);
            }
        }
        cursor.close();
        
        return result;
    }

}
