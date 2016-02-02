/*
 EventDeviceDao.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event.cache.db;

import org.deviceconnect.android.event.cache.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * EventDeviceテーブル用DAOクラス.
 * 
 *
 * @author NTT DOCOMO, INC.
 */
final class EventDeviceDao implements EventDeviceSchema {

    /**
     * Utilityクラスなのでprivate.
     */
    private EventDeviceDao() {
    }

    /**
     * イベントとデバイスのマッチング情報を登録する.
     * 
     * @param db データベース操作オブジェクト
     * @param attributeId アトリビュートID
     * @param serviceId サービスID
     * @return 登録出来た場合は登録時のIDを返す。重複している場合は登録済みのIDを返す。処理に失敗した場合は-1を返す。
     */
    static long insert(final SQLiteDatabase db, final long attributeId, final long serviceId) {
        
        long result = -1L;
        Cursor cursor = db.query(TABLE_NAME, new String[] {_ID}, A_ID + "=? AND " + D_ID + "=?", 
                new String[] {"" + attributeId, "" + serviceId}, null, null, null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(A_ID, attributeId);
            values.put(D_ID, serviceId);
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
    
    /**
     * 指定されたIDの行を削除.
     * 
     * @param db データベース操作クラス
     * @param id ID
     * @return 削除された行の数。無い場合は0を返す。
     */
    static int deleteById(final SQLiteDatabase db, final long id) {
        
        int result = 0;
        try {
            return db.delete(TABLE_NAME, _ID + "=?", new String[] {"" + id});
        } catch (SQLiteException e) {
            result = 0;
        }
        
        return result;
    }
}
