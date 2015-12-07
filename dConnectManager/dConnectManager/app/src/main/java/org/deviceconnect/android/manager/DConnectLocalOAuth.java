/*
 DConnectLocalOAuth.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.deviceconnect.android.manager.profile.DConnectFilesProfile;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * デバイスプラグインとのLocal OAuthの認可を行うためのクラス.
 * @author NTT DOCOMO, INC.
 */
public class DConnectLocalOAuth {
    /** DBのファイル名を定義. */
    private static final String DATABASE_NAME = "local_oauth_deviceplugin.db";
    /** DBのバージョンを定義. */
    private static final int DATABASE_VERSION = 3;
    /** OAuthデータ用のテーブル名を定義. */
    private static final String OAUTH_DATA_TABLE_NAME = "oauth_data_tbl";
    /** アクセストークン用のテーブル名を定義. */
    private static final String ACCESS_TOKEN_TABLE_NAME = "access_token_tbl";

    /**
     * Local OAuthを無視するプロファイル名一覧を定義.
     */
    public static final String[] IGNORE_PROFILE = {
        AuthorizationProfileConstants.PROFILE_NAME,
        AvailabilityProfileConstants.PROFILE_NAME,
        SystemProfileConstants.PROFILE_NAME,
        DConnectFilesProfile.PROFILE_NAME,
    };

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** DBアクセスヘルパークラス. */
    private LocalOAuthSQLiteOpenHelper mDBHelper;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public DConnectLocalOAuth(final Context context) {
        mContext = context;
        mDBHelper = new LocalOAuthSQLiteOpenHelper(context, DATABASE_NAME);
    }
    
    /**
     * コンストラクタ.
     * @param context コンテキスト
     * @param filename ファイル名
     */
    public DConnectLocalOAuth(final Context context, final String filename) {
        mContext = context;
        mDBHelper = new LocalOAuthSQLiteOpenHelper(context, filename);
    }

    /**
     * Local OAuthを無視するプロファイルをチェックする.
     * @param profileName プロファイル名
     * @return 無視する場合はtrue、それ以外はfalse
     */
    public boolean checkProfile(final String profileName) {
        return Arrays.asList(IGNORE_PROFILE).contains(profileName);
    }

    /**
     * コンテキストを取得する.
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Local OAuthデータを追加する.
     * @param serviceId サービスID
     * @param clientId クライアントID
     * @param clientSecret クライアントシークレット
     */
    public synchronized void setOAuthData(final String serviceId, final String clientId, final String clientSecret) {
        mLogger.fine("setOAuthData[serviceId: " + serviceId + ", clinetId: " 
                + clientId + ", clientSecret: " + clientSecret + "]");

        ContentValues values = new ContentValues();
        values.put(OAuthDataColumns.SERVICE_ID, serviceId);
        values.put(OAuthDataColumns.CLIENT_ID, clientId);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            db.insertOrThrow(OAUTH_DATA_TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたサービスIDのLocal OAuthデータを取得する.
     * 指定されたサービスIDに対応するデータが存在しない場合はnullを返却する.
     * @param serviceId サービスID
     * @return Local OAuthデータ
     */
    public synchronized OAuthData getOAuthData(final String serviceId) {
        String select = OAuthDataColumns.SERVICE_ID + "=?";
        String[] selectArgs = {serviceId};
        OAuthData client =  null;
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cs = db.query(OAUTH_DATA_TABLE_NAME, null, select, selectArgs, null, null, null);
        try {
            if (cs.moveToFirst()) {
                client = getOAuthData(cs);
            }
        } finally {
            cs.close();
            db.close();
        }
        return client;
    }

    /**
     * 指定されたサービスIDのLocal OAuthのデータを削除する.
     * @param serviceId サービスID
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean deleteOAuthData(final String serviceId) {
        OAuthData oauth = getOAuthData(serviceId);
        if (oauth == null) {
            return false;
        }
        return deleteOAuthData(oauth);
    }

    /**
     * 指定されたOAuthのデータをDBから削除する.
     * @param oauth LocalOAuthデータ
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    private synchronized boolean deleteOAuthData(final OAuthData oauth) {
        if (oauth == null) {
            throw new IllegalArgumentException("oauth is null.");
        }

        mLogger.fine("deleteOAuthData[serviceId: " + oauth.getServiceId() 
                + ", clientId: " + oauth.getClientId() + "]");

        boolean result = deleteAccessToken(oauth.getId());
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            String select = OAuthDataColumns._ID + "=" + oauth.getId();
            result = db.delete(OAUTH_DATA_TABLE_NAME, select, null) > 0;
        } finally {
            db.close();
        }
        return result;
    }

    /**
     * 指定されたプラグインIDを含むserviceIdを持つLocal OAuthデータをすべて削除する.
     * @param pluginId プラグインID
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean deleteOAuthDatas(final String pluginId) {
        List<OAuthData> datas = getOAuthDatas(pluginId);
        for (OAuthData auth : datas) {
            deleteOAuthData(auth);
        }
        return true;
    }

    /**
     * 指定されたプラグインIDを含むserviceIdを持つLocal OAuthデータ一覧を取得する.
     * 見つからない場合には、サイズが0のリストを返却する。
     * @param pluginId プラグインID
     * @return LocalOAuthデータ一覧
     */
    public synchronized List<OAuthData> getOAuthDatas(final String pluginId) {
        List<OAuthData> datas = new ArrayList<OAuthData>();

        String select = OAuthDataColumns.SERVICE_ID + " LIKE ?";
        String[] selectArgs = {"%" + pluginId + "%"};
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cs = db.query(OAUTH_DATA_TABLE_NAME, null, select, selectArgs, null, null, null);
        try {
            if (cs.moveToFirst()) {
                do {
                    OAuthData client = new OAuthData();
                    client.mId = cs.getInt(cs.getColumnIndex(OAuthDataColumns._ID));
                    client.mServiceId = cs.getString(cs.getColumnIndex(OAuthDataColumns.SERVICE_ID));
                    client.mClientId = cs.getString(cs.getColumnIndex(OAuthDataColumns.CLIENT_ID));
                    datas.add(client);
                } while (cs.moveToNext());
            }
        } finally {
            cs.close();
            db.close();
        }
        return datas;
    }

    /**
     * アクセストークンを削除します.
     * @param oauthId OAuthDataを識別するID
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public synchronized boolean deleteAccessToken(final int oauthId) {
        mLogger.fine("deleteAccessToken[oauthId]: " + oauthId);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            String select = AccessTokenColumns.OAUTH_ID + "=" + oauthId + "";
            return db.delete(ACCESS_TOKEN_TABLE_NAME, select, null) > 0;
        } finally {
            db.close();
        }
    }

    /**
     * 指定されたアクセストークンを削除する.
     * 
     * @param token アクセストークン
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public synchronized boolean deleteAccessToken(final String token) {
        mLogger.fine("deleteAccessToken[token]: " + token);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            String select = AccessTokenColumns.ACCESS_TOKEN + "=?";
            String[] selectArgs = {token};
            return db.delete(ACCESS_TOKEN_TABLE_NAME, select, selectArgs) > 0;
        } finally {
            db.close();
        }
    }
    /**
     * サービスIDに対応したアクセストークンを取得する.
     * 
     * アクセストークンが見つからない場合にはnullを返却する.
     * 
     * @param oauthId サービスID
     * @return アクセストークン
     */
    public synchronized String getAccessToken(final int oauthId) {
        String select = AccessTokenColumns.OAUTH_ID + "=" + oauthId;
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cs = db.query(ACCESS_TOKEN_TABLE_NAME, null, select, null, null, null, null);
        try {
            if (cs.moveToFirst()) {
                return cs.getString(cs.getColumnIndex(AccessTokenColumns.ACCESS_TOKEN));
            }
        } finally {
            cs.close();
            db.close();
        }
        return null;
    }

    /**
     * アクセストークンを設定する.
     * @param oauthId サービスID
     * @param accessToken アクセストークン
     */
    public synchronized void setAccessToken(final int oauthId, final String accessToken) {
        String select = AccessTokenColumns.OAUTH_ID + "=" + oauthId + "";

        ContentValues values = new ContentValues();
        values.put(AccessTokenColumns.OAUTH_ID, oauthId);
        values.put(AccessTokenColumns.ACCESS_TOKEN, accessToken);

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cs = db.query(ACCESS_TOKEN_TABLE_NAME, null, select, null, null, null, null);
        try {
            if (cs.moveToFirst()) {
                mDBHelper.getWritableDatabase().update(ACCESS_TOKEN_TABLE_NAME, values, select, null);
            } else {
                mDBHelper.getWritableDatabase().insertOrThrow(ACCESS_TOKEN_TABLE_NAME, null, values);
            }
        } finally {
            cs.close();
            db.close();
        }
    }

    /**
     * 保存しているLocal OAuthのデータ一覧を取得する.
     * @return Local OAuthのデータ一覧
     */
    public synchronized List<OAuthData> getOAuthDataList() {
        List<OAuthData> clients = new ArrayList<OAuthData>();
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cs = db.query(OAUTH_DATA_TABLE_NAME, null, null, null, null, null, null);
        try {
            if (cs.moveToFirst()) {
                do {
                    clients.add(getOAuthData(cs));
                } while (cs.moveToNext());
            }
        } finally {
            cs.close();
            db.close();
        }
        return clients;
    }

    /**
     * DBからOAuthDataを作成する.
     * @param cs DBのカーソル
     * @return OAuthDataのインスタンス
     */
    private OAuthData getOAuthData(final Cursor cs) {
        OAuthData cd = new OAuthData();
        cd.mId = cs.getInt(cs.getColumnIndex(OAuthDataColumns._ID));
        cd.mServiceId = cs.getString(cs.getColumnIndex(OAuthDataColumns.SERVICE_ID));
        cd.mClientId = cs.getString(cs.getColumnIndex(OAuthDataColumns.CLIENT_ID));
        return cd;
    }
    /**
     * LocalOAuth用のデータ.
     * @author NTT DOCOMO, INC.
     */
    public class OAuthData {
        /** 識別子. */
        private int mId;
        /** クライアントID. */
        private String mClientId;
        /** クライアントシークレット. */
        private String mClientSecret;
        /** サービスID. */
        private String mServiceId;
        /**
         * 識別子を取得する.
         * @return 識別子
         */
        public int getId() {
            return mId;
        }
        /**
         * 識別子を設定する.
         * @param id 識別子
         */
        public void setId(final int id) {
            this.mId = id;
        }
        /**
         * クライアントIDを取得する.
         * @return クライアントID
         */
        public String getClientId() {
            return mClientId;
        }
        /**
         * クライアントIDを設定する.
         * @param clientId クライアントID
         */
        public void setClientId(final String clientId) {
            this.mClientId = clientId;
        }
        /**
         * クライアントシークレットを取得する.
         * @return クライアントシークレット
         */
        public String getClientSecret() {
            return mClientSecret;
        }
        /**
         * クライアントシークレットを設定する.
         * @param clientSecret クライアントシークレット
         */
        public void setClientSecret(final String clientSecret) {
            this.mClientSecret = clientSecret;
        }
        /**
         * サービスIDを取得する.
         * @return サービスID
         */
        public String getServiceId() {
            return mServiceId;
        }
        /**
         * サービスIDを設定する.
         * @param serviceId サービスID
         */
        public void setServiceId(final String serviceId) {
            this.mServiceId = serviceId;
        }
    }

    /**
     * LocalOAuthのデータを保持するDBにアクセスするためのヘルパークラス.
     * @author NTT DOCOMO, INC.
     */
    private class LocalOAuthSQLiteOpenHelper extends SQLiteOpenHelper {
        /**
         * コンストラクタ.
         * @param context コンテキスト
         * @param filename ファイル名
         */
        public LocalOAuthSQLiteOpenHelper(final Context context, final String filename) {
            super(context, filename, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createAllTables(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            // 既にテーブルが存在する場合には、削除して、再度テーブルを作成する
            db.execSQL("DROP TABLE IF EXISTS " + OAUTH_DATA_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ACCESS_TOKEN_TABLE_NAME);
            createAllTables(db);
        }

        /**
         * 必要なテーブルをすべて作成する.
         * @param db データベース
         */
        private void createAllTables(final SQLiteDatabase db) {
            createOAuthDataTable(db);
            createAccessTokenTable(db);
        }

        /**
         * OAuthデータ用のテーブルを作成する.
         * @param db DBアクセス用クラス
         */
        private void createOAuthDataTable(final SQLiteDatabase db) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE " + OAUTH_DATA_TABLE_NAME);
            sql.append("(_id INTEGER PRIMARY KEY, ");
            sql.append(OAuthDataColumns.SERVICE_ID + " TEXT NOT NULL,");
            sql.append(OAuthDataColumns.CLIENT_ID + " TEXT NOT NULL");
            sql.append(");");
            db.execSQL(sql.toString());
        }

        /**
         * アクセストークンデータ用のテーブルを作成する.
         * @param db DBアクセス用クラス
         */
        private void createAccessTokenTable(final SQLiteDatabase db) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE " + ACCESS_TOKEN_TABLE_NAME + " (");
            sql.append(AccessTokenColumns.OAUTH_ID + " INTEGER,");
            sql.append(AccessTokenColumns.ACCESS_TOKEN + " TEXT NOT NULL");
            sql.append(");");
            db.execSQL(sql.toString());
        }
    }

    /**
     * カラム名を定義するためのクラス.
     * @author NTT DOCOMO, INC.
     */
    private static final class OAuthDataColumns implements BaseColumns {
        /**
         * コンストラクタ.
         * インスタンスは作成させないのでprivate.
         */
        private OAuthDataColumns() {
        }
        
        /**
         * クライアントID.
         */
        public static final String CLIENT_ID = "client_id";
        
        /**
         * サービスID.
         */
        public static final String SERVICE_ID = "service_id";
    }

    /**
     * アクセストークン用データカラム名.
     * @author NTT DOCOMO, INC.
     */
    private static final class AccessTokenColumns implements BaseColumns {
        /**
         * コンストラクタ.
         * インスタンスは作成させないのでprivate.
         */
        private AccessTokenColumns() {
        }

        /**
         * OAuthDataのID.
         */
        public static final String OAUTH_ID = "oauth_id";

        /**
         * アクセストークン.
         */
        public static final String ACCESS_TOKEN = "access_token";
    }
}
