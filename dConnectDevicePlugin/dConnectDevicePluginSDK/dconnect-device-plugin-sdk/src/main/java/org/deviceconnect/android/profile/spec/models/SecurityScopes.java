/*
 SecurityScopes.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 で使用されるスコープ.
 *
 * @author NTT DOCOMO, INC.
 */
public class SecurityScopes extends AbstractSpec {
    /**
     * スコープのリスト.
     */
    private Map<String, String> mScopes;

    /**
     * スコープのリストを取得します.
     *
     * @return スコープのリスト
     */
    public Map<String, String> getScopes() {
        return mScopes;
    }

    /**
     * スコープのリストを設定します.
     *
     * @param scopes スコープのリスト
     */
    public void setScopes(Map<String, String> scopes) {
        mScopes = scopes;
    }

    /**
     * スコープを追加します.
     *
     * @param key キー
     * @param value 値
     */
    public void addScope(String key, String value) {
        if (mScopes == null) {
            mScopes = new HashMap<>();
        }
        mScopes.put(key, value);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mScopes != null && !mScopes.isEmpty()) {
            for (Map.Entry<String, String> entry : mScopes.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
