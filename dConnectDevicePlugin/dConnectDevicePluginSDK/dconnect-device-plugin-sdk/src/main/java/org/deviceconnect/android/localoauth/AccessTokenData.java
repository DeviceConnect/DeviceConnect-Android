/*
 AccessTokenData.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

/**
 * アクセストークンデータ.
 * @author NTT DOCOMO, INC.
 */
public class AccessTokenData {

    /** アクセストークン. */
    private String mAccessToken;

    /** スコープ配列. */
    private AccessTokenScope[] mScopes;

    /** タイムスタンプ. */
    private long mTimestamp;

    /**
     * コンストラクタ.
     * @param accessToken アクセストークン
     * @param timestamp タイムスタンプ
     * @param scopes スコープ毎の有効期限の配列
     */
    public AccessTokenData(final String accessToken, final long timestamp, final AccessTokenScope[] scopes) {
        mAccessToken = accessToken;
        mTimestamp = timestamp;
        mScopes = scopes;
    }

    /**
     * アクセストークン取得.
     * 
     * @return アクセストークン
     */
    public String getAccessToken() {
        return mAccessToken;
    }
    
    /**
     * スコープ配列取得.
     * @return スコープ毎の有効期限の配列 
     */
    public AccessTokenScope[] getScopes() {
        return mScopes;
    }
    
    /**
     * 更新日時取得.
     * @return 更新日時
     */
    public long getTimestamp() {
        return mTimestamp;
    }
}
