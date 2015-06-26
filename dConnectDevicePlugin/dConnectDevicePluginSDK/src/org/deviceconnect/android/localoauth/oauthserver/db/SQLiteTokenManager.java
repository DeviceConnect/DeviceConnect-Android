/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.deviceconnect.android.localoauth.oauthserver.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deviceconnect.android.localoauth.LocalOAuth2Settings;
import org.deviceconnect.android.localoauth.oauthserver.SampleUser;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.OAuthResourceDefs;
import org.restlet.ext.oauth.internal.AbstractTokenManager;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Scope;
import org.restlet.ext.oauth.internal.Token;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class SQLiteTokenManager extends AbstractTokenManager {

    /** セッションマップ. */
    private final Map<String, AuthSession> mSessions = new ConcurrentHashMap<String, AuthSession>();

    /**
     * DBオブジェクト.
     */
    private SQLiteDatabase mDb = null;

    /**
     * DBオブジェクトを設定する.
     * 
     * @param db DBオブジェクト
     */
    public void setDb(final SQLiteDatabase db) {
        mDb = db;
    }

    /**
     * トークン生成.
     * 
     * @param client クライアント
     * @param username ユーザー名
     * @param scopes スコープ(timestampは内部で現在時刻に更新される)
     * @param applicationName アプリケーション名
     * @return トークン
     * @throws OAuthException OAuthの例外
     * 
     */
    public Token generateToken(final Client client, final String username, final Scope[] scopes,
            final String applicationName) throws OAuthException {
    
        /* scopesのtimestampを現在時刻に設定する */
        long currentTime = System.currentTimeMillis();
        for (Scope s : scopes) {
            s.setTimestamp(currentTime);
        }
        
        /* すでに発行されているトークンが存在するか？ */
        SQLiteToken token = (SQLiteToken) findToken(client, username);

        /* DBのprofilesテーブルに登録されていないprofilesがscopeに指定されていればそれを追加する */
        List<SQLiteProfile> profiles = dbLoadProfiles(mDb);
        for (Scope s : scopes) {
            if (findProfileByProfileName(profiles, s.getScope()) == null) {
                SQLiteProfile profile = new SQLiteProfile();
                profile.setProfileName(s.getScope());

                /* DBに1件追加(失敗したらSQLiteException発生) */
                profile.dbInsert(mDb);
                profiles.add(profile);
            }
        }

        /* 発行されているtokenがなければ登録する(失敗したらSQLiteException発生) */
        if (token == null) {
            
            /* トークンデータを登録 */
            token = new SQLiteToken();
            token.setClientId(client.getClientId());
            token.setUsername(username);
            token.setScope(scopes);
            token.setTokenType(OAuthResourceDefs.TOKEN_TYPE_BEARER);
            token.setAccessToken(generateRawToken());
            token.setRefreshToken(generateRawToken());
            token.setApplicationName(applicationName);
            token.dbInsert(mDb);
            
            /* スコープデータを登録 */
            long tokensTokenId = token.getId();
            for (Scope scope : scopes) {
                SQLiteProfile profile = findProfileByProfileName(profiles, scope.getScope());
                SQLiteScopeDb sc = new SQLiteScopeDb(tokensTokenId, profile.getId(), scope.getTimestamp(),
                        scope.getExpirePeriod());
                sc.dbInsert(mDb);
            }
            
        } else { /* 既存のtokenにscopeを追加し、トークンの有効期限を更新する */
            
            /* DBのscopesテーブルに登録されているデータを読み込む */
            List<SQLiteScopeInfo> grantScopeInfos = dbLoadScopes(mDb, token.getId());
            
            /* DB未登録のスコープデータを取得する */
            List<SQLiteScopeInfo> addScopes = new ArrayList<SQLiteScopeInfo>();
            List<SQLiteScopeInfo> existScopes = new ArrayList<SQLiteScopeInfo>();
            for (Scope scope : scopes) {
                
                /* grantScopesに登録されているか？ */
                SQLiteScopeInfo scopeInfo = findScopeInfoByProfileName(grantScopeInfos, scope.getScope());
                SQLiteProfile profile = findProfileByProfileName(profiles, scope.getScope());
                if (profile != null) {
                    if (scopeInfo == null) {
                        /* 登録されていなければ追加スコープ配列に登録する */
                        addScopes.add(new SQLiteScopeInfo(token.getId(), profile.getId(), scope.getTimestamp(), scope
                                .getExpirePeriod(), scope.getScope()));
                    } else {
                        /* 登録されていれば既存スコープ配列に登録する */
                        existScopes.add(new SQLiteScopeInfo(token.getId(), profile.getId(), scope.getTimestamp(), scope
                                .getExpirePeriod(), scope.getScope()));
                    }
                }
            }
            
            /* 追加スコープがあれば、DBにレコード追加する */
            if (addScopes.size() > 0) {
                
                /* token.getScope()とaddScopesのスコープ名を連結したnewScopesを作成する */
                Scope[] newScopes = new Scope[token.getScope().length + addScopes.size()];
                System.arraycopy(token.getScope(), 0, newScopes, 0, token.getScope().length);
                int storeIndex = token.getScope().length;
                int addScopeCount = addScopes.size();
                for (int i = 0; i < addScopeCount; i++) {
                    SQLiteScopeInfo scopeInfo = addScopes.get(i);
                    String scopeName = scopeInfo.getProfileName();
                    long timestamp = scopeInfo.getTimestamp();
                    long expirePeriod = scopeInfo.getExpirePeriod();
                    Scope s = new Scope(scopeName, timestamp, expirePeriod);
                    newScopes[storeIndex++] = s;
                }
                
                /* 追加分のscopeをDBに登録する */
                for (SQLiteScopeInfo addScope : addScopes) {
                    addScope.dbInsert(mDb);
                }
                
                /* tokenのスコープを更新する */
                token.setScope(newScopes);
            }
            
            /* 既存スコープがあれば、DBのtimestampを更新する */
            for (SQLiteScopeInfo existScope : existScopes) {
                existScope.dbReplaceTimestamp(mDb);
            }
            
            /* DB更新(内部でアクセス時間を更新、失敗したらSQLiteException発生) */
            token.dbUpdate(mDb);
            
        }

        return token;
    }

    /**
     * リフレッシュトークン（本APIは使用禁止です）.
     * 
     * @param client クライアント
     * @param refreshToken リフレッシュトークン
     * @param scope スコープ
     * @return トークン
     * @throws OAuthException OAuth例外
     */
    public Token refreshToken(final Client client, final String refreshToken, final String[] scope)
            throws OAuthException {
        throw new OAuthException(OAuthError.invalid_grant, "本APIは使用できません.", null);
    }

    /**
     * セッション保存.
     * @param session セッション
     * @return セッションコード
     * @throws OAuthException OAuth関連の例外 
     */
    public String storeSession(final AuthSession session) throws OAuthException {
        String code = generateRawCode();
        mSessions.put(code, session);
        return code;
    }

    /**
     * セッション復元.
     * @param code セッションコード
     * @return セッションデータ
     * @throws OAuthException OAuth関連の例外 
     */
    public AuthSession restoreSession(final String code) throws OAuthException {
        AuthSession session = mSessions.remove(code);
        if (session == null) {
            throw new OAuthException(OAuthError.invalid_grant, "Invalid code.", null);
        }
        return session;
    }

    /**
     * クライアントIDとユーザー名が一致するトークンを取得する.
     * 
     * @param client クライアントID
     * @param username ユーザー名
     * @return トークン(該当なければnullを返す)
     * 
     */
    public Token findToken(final Client client, final String username)  {
        if (mDb != null) {
            String selection = SQLiteToken.CLIENTID_FIELD + "=? and " 
                    + SQLiteToken.USERS_USERID_FIELD + "=?";
            String[] selectionArgs = {
                client.getClientId(),
                String.valueOf(LocalOAuthOpenHelper.USERS_USER_ID)
            };
            SQLiteToken[] tokens = dbLoadTokens(mDb, selection, selectionArgs);
            if (tokens == null) {
                return null;
            } else if (tokens.length == 1) {
                return tokens[0];
            } else {
                throw new SQLiteException("アクセストークンに該当するトークンが2件以上存在しています。");
            }
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }

    /**
     * ユーザー名が一致するトークンをDBから読み込む.
     * 
     * @param username ユーザー名
     * @return トークン配列
     */
    public Token[] findTokens(final String username) {
        if (mDb != null) {
            String selection = SQLiteToken.USERS_USERID_FIELD + "=?";
            String[] selectionArgs = { String.valueOf(LocalOAuthOpenHelper.USERS_USER_ID) };
            SQLiteToken[] tokens = dbLoadTokens(mDb, selection, selectionArgs);
            return tokens;
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }

    /**
     * クライアントIDが一致するトークンをDBから読み込む.
     * 
     * @param client クライアント
     * @return トークン配列
     */
    public Token[] findTokens(final Client client) {
        if (mDb != null) {
            String selection = SQLiteToken.CLIENTID_FIELD + "=?";
            String[] selectionArgs = { client.getClientId() };
            SQLiteToken[] tokens = dbLoadTokens(mDb, selection, selectionArgs);
            return tokens;
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }

    }

    /**
     * クライアントIDとユーザー名を指定してトークンを削除する.
     * 
     * @param client クライアント
     * @param username ユーザー名
     */
    public void revokeToken(final Client client, final String username) {
        if (mDb != null) {
            String whereClause = SQLiteToken.CLIENTID_FIELD + "=? and "
                    + SQLiteToken.USERS_USERID_FIELD + "=?";
            String[] whereArgs = {
                client.getClientId(),
                String.valueOf(LocalOAuthOpenHelper.USERS_USER_ID)
            };
            dbDeleteTokens(mDb, whereClause, whereArgs);
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }
    
    /**
     * トークンIDを指定してトークンを削除する.
     * 
     * @param tokenId トークンID
     */
    public void revokeToken(final long tokenId) {
        if (mDb != null) {
            String whereClause = SQLiteToken.ID_FIELD + "=?";
            String[] whereArgs = { String.valueOf(tokenId) };
            dbDeleteTokens(mDb, whereClause, whereArgs);
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }

    /**
     * ユーザー名を指定してトークンを削除する.
     * 
     * @param username ユーザー名
     */
    public void revokeAllTokens(final String username) {
        if (mDb != null) {
            String whereClause = SQLiteToken.USERS_USERID_FIELD + "=?";
            String[] whereArgs = { String.valueOf(LocalOAuthOpenHelper.USERS_USER_ID) };
            dbDeleteTokens(mDb, whereClause, whereArgs);
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }

    /**
     * クライアントIDが一致するアクセストークンを削除する.
     * 
     * @param client クライアント
     */
    public void revokeAllTokens(final Client client)  {
        if (mDb != null) {
            String whereClause = SQLiteToken.CLIENTID_FIELD + "=?";
            String[] whereArgs = { client.getClientId() };
            dbDeleteTokens(mDb, whereClause, whereArgs);
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }

    /**
     * アクセストークンが一致するトークンを探す.
     * 
     * @param accessToken アクセストークン
     * @return not null: アクセストークンが一致するトークンデータ / null:
     *         アクセストークンが一致するトークンデータが見つからない
     */
    @Override
    public Token findTokenByAccessToken(final String accessToken) {
        if (mDb != null) {
            String selection = SQLiteToken.ACCCESS_TOKEN_FIELD + "=?";
            String[] selectionArgs = { accessToken };
            SQLiteToken[] tokens = dbLoadTokens(mDb, selection, selectionArgs);

            if (tokens == null || tokens.length == 0) { /* 該当データなし */
                return null;
            } else if (tokens.length == 1) { /* 該当データあり（1件のみ...正常） */
                return tokens[0];
            } else { /* 該当データあり（2件以上...異常） */
                throw new SQLiteException("アクセストークンに該当するトークンが2件以上存在しています。");
            }
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }

    /**
     * 古い無効なトークン(クライアントIDが削除されて残っていたトークン)をクリーンアップする.
     */
    public void cleanup() {
        if (mDb != null) {
            final long deleteDate = System.currentTimeMillis() - LocalOAuth2Settings.TOKEN_CLEANUP_TIME
                    * LocalOAuth2Settings.MSEC;
            String sql = "delete from tokens "
                    + "where not exists (select * from clients where tokens.client_id = clients.id) "
                    + "and tokens.access_date < " + deleteDate;
            mDb.execSQL(sql);
        } else {
            throw new SQLiteException("DBがオープンされていません。");
        }
    }
    
    
    /**
     * 指定条件でtokenデータをDBから読み込む.
     * 
     * @param db DBオブジェクト
     * @param selection selection条件(key = value)。複数あるときはAnd条件になる。
     * @param selectionArgs ?にバインドする値。
     * @return 条件に一致したtokenデータ配列。該当データが0件ならnull。
     */
    private SQLiteToken[] dbLoadTokens(final SQLiteDatabase db, final String selection, final String[] selectionArgs) {

        SQLiteToken[] result = null;

        /* tokensテーブル読み込み */
        String tables = LocalOAuthOpenHelper.TOKENS_TABLE;
        String[] columns = SQLiteToken.TOKEN_ALL_FIELIDS;

        Cursor c = null;
        try {
            c = db.query(tables, columns, selection, selectionArgs, null, null, null);
            if (c.moveToFirst()) {
                int count = c.getCount();
                if (count > 0) {
                    result = new SQLiteToken[count];
                    for (int i = 0; i < count; i++) {

                        SQLiteToken token = new SQLiteToken();

                        final int idColumnIndex = c.getColumnIndex(SQLiteToken.ID_FIELD);
                        if (!c.isNull(idColumnIndex)) {
                            token.setId(c.getLong(idColumnIndex));
                        }

                        final int accessTokenColumnIndex = c.getColumnIndex(SQLiteToken.ACCCESS_TOKEN_FIELD);
                        if (!c.isNull(accessTokenColumnIndex)) {
                            token.setAccessToken(c.getString(accessTokenColumnIndex));
                        }

                        final int tokenTypeColumnIndex = c.getColumnIndex(SQLiteToken.TOKEN_TYPE_FIELD);
                        if (!c.isNull(tokenTypeColumnIndex)) {
                            token.setTokenType(c.getString(tokenTypeColumnIndex));
                        }

                        final int clientIdColumnIndex = c.getColumnIndex(SQLiteToken.CLIENTID_FIELD);
                        if (!c.isNull(clientIdColumnIndex)) {
                            token.setClientId(c.getString(clientIdColumnIndex));
                        }

                        final int usersUserIdColumnindex = c.getColumnIndex(SQLiteToken.USERS_USERID_FIELD);
                        if (!c.isNull(usersUserIdColumnindex)) {
                            // long userId = c.getLong(usersUserIdColumnindex);
                            token.setUsername(SampleUser.USERNAME);
                        }

                        final int registrationDateColumnIndex = c.getColumnIndex(SQLiteToken.REGISTRATION_DATE_FIELD);
                        if (!c.isNull(registrationDateColumnIndex)) {
                            token.setRegistrationDate(c.getLong(registrationDateColumnIndex));
                        }

                        final int accessDateColumnIndex = c.getColumnIndex(SQLiteToken.ACCESS_DATE_FIELD);
                        if (!c.isNull(accessDateColumnIndex)) {
                            token.setAccessDate(c.getLong(accessDateColumnIndex));
                        }

                        final int applicationNameColumnIndex = c.getColumnIndex(SQLiteToken.APPLICATION_NAME_FIELD);
                        if (!c.isNull(applicationNameColumnIndex)) {
                            token.setApplicationName(c.getString(applicationNameColumnIndex));
                        }

                        result[i] = token;

                        c.moveToNext();
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        /* 1件以上データが存在する */
        if (result != null && result.length > 0) {
            /* tokenごとにscopesを読み込み、tokensのscopeに格納する */
            dbLoadScopesStoreToToken(db, result);
        }

        return result;
    }
    
    /**
     * 指定条件でtokenデータをDBから削除する.
     * 
     * @param db DBオブジェクト
     * @param whereClause where条件(key = value)。複数あるときはAnd条件になる。
     * @param whereArgs ?の値
     */
    private void dbDeleteTokens(final SQLiteDatabase db, final String whereClause, final String[] whereArgs) {
        String table = LocalOAuthOpenHelper.TOKENS_TABLE;
        db.delete(table, whereClause, whereArgs);
    }

    /**
     * profileデータを全てdbから読み込む.
     * 
     * @param db DBオブジェクト
     * @return SQLiteProfile配列
     */
    private List<SQLiteProfile> dbLoadProfiles(final SQLiteDatabase db) {

        List<SQLiteProfile> profiles = new ArrayList<SQLiteProfile>();

        String tables = LocalOAuthOpenHelper.PROFILES_TABLE;
        String[] columns = SQLiteProfile.PROFILE_ALL_FIELIDS;

        Cursor c = null;
        try {
            c = db.query(tables, columns, null, null, null, null, null);
            if (c.moveToFirst()) {
                int count = c.getCount();
                if (count > 0) {

                    for (int i = 0; i < count; i++) {

                        SQLiteProfile profile = new SQLiteProfile();

                        final int idColumnIndex = c.getColumnIndex(SQLiteProfile.ID_FIELD);
                        if (!c.isNull(idColumnIndex)) {
                            profile.setId(c.getLong(idColumnIndex));
                        }

                        final int profileNameColumnIndex = c.getColumnIndex(SQLiteProfile.PROFILE_NAME_FIELD);
                        if (!c.isNull(profileNameColumnIndex)) {
                            profile.setProfileName(c.getString(profileNameColumnIndex));
                        }

                        final int descriptionColumnIndex = c.getColumnIndex(SQLiteProfile.DESCRIPTION_FIELD);
                        if (!c.isNull(descriptionColumnIndex)) {
                            profile.setDescription(c.getString(descriptionColumnIndex));
                        }

                        profiles.add(profile);

                        c.moveToNext();
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return profiles;
    }

    /**
     * プロファイル配列に指定されたプロファイル名のデータが存在すればそのデータを返す.
     * 
     * @param profiles プロファイルデータ配列
     * @param profileName プロファイル名
     * @return not null: プロファイル名が一致するプロファイルデータが存在すればそのポインタを返す。 null: 存在しない。
     */
    private SQLiteProfile findProfileByProfileName(final List<SQLiteProfile> profiles, final String profileName) {

        for (SQLiteProfile profile : profiles) {
            if (profileName.equals(profile.getProfileName())) {
                return profile;
            }
        }
        return null;
    }

    /**
     * tokenごとにscopesを読み込み、tokensのscopeに格納する.
     * 
     * @param db DBオブジェクト
     * @param tokens トークン配列
     */
    private void dbLoadScopesStoreToToken(final SQLiteDatabase db, final SQLiteToken[] tokens) {

        final String tables = LocalOAuthOpenHelper.SCOPES_TABLE + "," + LocalOAuthOpenHelper.PROFILES_TABLE;
        final String fieldProfilesProfileName = LocalOAuthOpenHelper.PROFILES_TABLE + "."
                + SQLiteProfile.PROFILE_NAME_FIELD;
        final String fieldScopesTimestamp = LocalOAuthOpenHelper.SCOPES_TABLE + "."
                + SQLiteScopeDb.TIMESTAMP_FIELD;
        final String fieldScopesExpirePeriod = LocalOAuthOpenHelper.SCOPES_TABLE + "."
                + SQLiteScopeDb.EXPIRE_PERIOD_FIELD;
        
        final String fieldScopesTokenId = LocalOAuthOpenHelper.SCOPES_TABLE + "." + SQLiteScopeDb.TOKENS_TOKENID_FIELD;
        final String fieldScopesProfileId = LocalOAuthOpenHelper.SCOPES_TABLE + "."
                + SQLiteScopeDb.PROFILES_PROFILEID_FIELD;
        final String fieldProfilesId = LocalOAuthOpenHelper.PROFILES_TABLE + "." + SQLiteProfile.ID_FIELD;
        final String[] columns = {fieldProfilesProfileName, fieldScopesTimestamp, fieldScopesExpirePeriod};

        for (SQLiteToken token : tokens) {
            String where = fieldScopesTokenId + " = " + token.getId() + " and " + fieldScopesProfileId + " = "
                    + fieldProfilesId;

            Scope[] scopes = null;

            Cursor c = null;
            try {
                c = db.query(tables, columns, where, null, null, null, null);
                if (c.moveToFirst()) {
                    int count = c.getCount();
                    if (count > 0) {
                        scopes = new Scope[count];
                        final int profileNameColumnIndex = c.getColumnIndex(SQLiteProfile.PROFILE_NAME_FIELD);
                        final int timestampColumnIndex = c.getColumnIndex(SQLiteScopeDb.TIMESTAMP_FIELD);
                        final int expirePeriodColumnIndex = c.getColumnIndex(SQLiteScopeDb.EXPIRE_PERIOD_FIELD);
                        for (int i = 0; i < count; i++) {
                            String scopeName = null;
                            if (!c.isNull(profileNameColumnIndex)) {
                                scopeName = c.getString(profileNameColumnIndex);
                            }
                            long timestamp = 0;
                            if (!c.isNull(timestampColumnIndex)) {
                                timestamp = c.getLong(timestampColumnIndex);
                            }
                            long expirePeriod = 0;
                            if (!c.isNull(expirePeriodColumnIndex)) {
                                expirePeriod = c.getInt(expirePeriodColumnIndex);
                            }
                            
                            scopes[i] = new Scope(scopeName, timestamp, expirePeriod);
                            
                            c.moveToNext();
                        }
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            token.setScope(scopes);
        }
    }
    
    
    /**
     * tokenIdが一致するscopesデータをdbから読み込む.
     * 
     * @param db DBオブジェクト
     * @param tokenId トークンID
     * @return SQLiteScopeInfo配列
     */
    private List<SQLiteScopeInfo> dbLoadScopes(final SQLiteDatabase db, final long tokenId) {
        
        final String tables = LocalOAuthOpenHelper.SCOPES_TABLE + "," + LocalOAuthOpenHelper.PROFILES_TABLE;
        
        final String fieldScopesTokenId = LocalOAuthOpenHelper.SCOPES_TABLE + "." + SQLiteScopeDb.TOKENS_TOKENID_FIELD;
        final String fieldScopesProfileId = LocalOAuthOpenHelper.SCOPES_TABLE + "."
                + SQLiteScopeDb.PROFILES_PROFILEID_FIELD;
        final String fieldTimestampProfileId = LocalOAuthOpenHelper.SCOPES_TABLE + "."
                + SQLiteScopeDb.TIMESTAMP_FIELD;
        final String fieldExpirePeriodProfileId = LocalOAuthOpenHelper.SCOPES_TABLE + "."
                + SQLiteScopeDb.EXPIRE_PERIOD_FIELD;
        final String fieldProfilesProfileName = LocalOAuthOpenHelper.PROFILES_TABLE + "."
                + SQLiteProfile.PROFILE_NAME_FIELD;
        final String fieldProfilesId = LocalOAuthOpenHelper.PROFILES_TABLE + "." + SQLiteProfile.ID_FIELD;

        final String[] columns = {fieldScopesTokenId, fieldScopesProfileId, fieldTimestampProfileId,
                fieldExpirePeriodProfileId, fieldProfilesProfileName};

        String where = fieldScopesTokenId + " = " + tokenId + " and " + fieldScopesProfileId + " = "
                + fieldProfilesId;

        List<SQLiteScopeInfo> scopeInfos = new ArrayList<SQLiteScopeInfo>();

        Cursor c = null;
        try {
            c = db.query(tables, columns, where, null, null, null, null);
            if (c.moveToFirst()) {
                int count = c.getCount();
                if (count > 0) {
                    
                    final int tokensTokenIdColumnIndex = c.getColumnIndex(SQLiteScopeDb.TOKENS_TOKENID_FIELD);
                    final int profilesProfileIdColumnIndex = c.getColumnIndex(SQLiteScopeDb.PROFILES_PROFILEID_FIELD);
                    final int timestampColumnIndex = c.getColumnIndex(SQLiteScopeDb.TIMESTAMP_FIELD);
                    final int expirePeriodColumnIndex = c.getColumnIndex(SQLiteScopeDb.EXPIRE_PERIOD_FIELD);
                    final int profileNameColumnIndex = c.getColumnIndex(SQLiteProfile.PROFILE_NAME_FIELD);
                    
                    for (int i = 0; i < count; i++) {
                        
                        if (!c.isNull(tokensTokenIdColumnIndex)
                                &&  !c.isNull(profilesProfileIdColumnIndex)
                                &&  !c.isNull(timestampColumnIndex)
                                &&  !c.isNull(expirePeriodColumnIndex)
                                &&  !c.isNull(profileNameColumnIndex)) {
                            
                            long tokensTokenId = c.getLong(tokensTokenIdColumnIndex);
                            long profilesProfileId = c.getLong(profilesProfileIdColumnIndex);
                            long timestamp = c.getLong(timestampColumnIndex);
                            long expirePeriod = c.getLong(expirePeriodColumnIndex);
                            String profileName = c.getString(profileNameColumnIndex);
                            scopeInfos.add(new SQLiteScopeInfo(tokensTokenId, profilesProfileId, 
                                    timestamp, expirePeriod, profileName));
                            
                        }

                        c.moveToNext();
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return scopeInfos;
    }
    
    /**
     * SQLiteScopeInfo配列からプロファイル名が一致するデータを返す.
     * @param scopeInfos SQLiteScopeInfo配列
     * @param profileName プロファイル名
     * @return not null: プロファイル名が一致したSQLiteScopeInfoデータ / null: 該当なし
     */
    private SQLiteScopeInfo findScopeInfoByProfileName(final List<SQLiteScopeInfo> scopeInfos,
            final String profileName) {
        for (SQLiteScopeInfo scopeInfo : scopeInfos) {
            if (scopeInfo.getProfileName().equals(profileName)) {
                return scopeInfo;
            }
        }
        return null;
    }
    
    
    /**
     * SQLiteScopeにProfile名を付けたクラス.
     */
    class SQLiteScopeInfo extends SQLiteScopeDb {
        
        /** プロファイル名.*/
        private String mProfileName;
        
        /**
         * コンストラクタ.
         * @param tokensTokenId tokensテーブルのトークンID
         * @param profilesProfileId profilesテーブルのプロファイルID
         * @param timestamp タイムスタンプ
         * @param expirePeriod 有効期限
         * @param profileName プロファイル名
         */
        public SQLiteScopeInfo(final long tokensTokenId, final long profilesProfileId, final long timestamp,
                final long expirePeriod, final String profileName) {
            super(tokensTokenId, profilesProfileId, timestamp, expirePeriod);
            mProfileName = profileName;
        }
        
        /**
         * プロファイル名取得.
         * @return プロファイル名
         */
        public String getProfileName() {
            return mProfileName;
        }
        
        /**
         * プロファイル名設定.
         * @param profileName プロファイル名
         */
        public void setProfileName(final String profileName) {
            mProfileName = profileName;
        }
        
    }
}
