/*
 DeviceSchema.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event.cache.db;


/**
 * Deviceテーブル スキーマ.
 * 
 *
 * @author NTT DOCOMO, INC.
 */
interface DeviceSchema extends BaseSchema {
    
    /** 
     * テーブル名 : {@value}.
     */
    String TABLE_NAME = "Device";

    /** 
     * サービスID.
     */
    String SERVICE_ID = "service_id";
    
    /** 
     * テーブルcreate文.
     */
    String CREATE = "CREATE TABLE " + TABLE_NAME + " (" 
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
            + SERVICE_ID + " TEXT NOT NULL, "
            + CREATE_DATE + " INTEGER NOT NULL, "
            + UPDATE_DATE + " INTEGER NOT NULL, UNIQUE(" + SERVICE_ID + "));";
    /** 
     * テーブルdrop文.
     */
    String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
