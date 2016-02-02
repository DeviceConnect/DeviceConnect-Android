/*
 AttributeDao.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event.cache.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.deviceconnect.android.event.cache.Utils;

/**
 * Attributeテーブル用DAOクラス.
 * 
 *
 * @author NTT DOCOMO, INC.
 */
final class AttributeDao implements AttributeSchema {

    /**
     * Utilityクラスなのでprivate.
     */
    private AttributeDao() {
        
    }
    
    /**
     * アトリビュートを登録する.
     * 
     * @param db データベース操作オブジェクト
     * @param attribute アトリビュート名
     * @param interfaceId インターフェースID
     * @return 登録出来た場合は登録時のIDを返す。重複している場合は登録済みのIDを返す。処理に失敗した場合は-1を返す。
     */
    static long insert(final SQLiteDatabase db, final String attribute, final long interfaceId) {

        String attr = attribute;
        if (attr == null) {
            attr = "";
        }
        
        long result = -1L;
        Cursor cursor = db.query(TABLE_NAME, new String[] {_ID}, NAME + "=? AND " + I_ID + "=?", 
                new String[] {attr, "" + interfaceId}, null, null, null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(NAME, attr);
            values.put(I_ID, interfaceId);
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
