/*
 SecurityScheme.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

/**
 * セキュリティスキーム.
 *
 * @author NTT DOCOMO, INC.
 */
public class SecurityScheme extends AbstractSpec {
    /**
     * セキュリティスキームのタイプ.
     * <p>
     * basic, apiKey, oauth2
     * </p>
     *
     * TODO enum で宣言した方が良いか？
     */
    private String mType;

    /**
     * セキュリティスキームの詳細.
     */
    private String mDescription;

    /**
     * API Key の header または query の変数名.
     */
    private String mName;

    /**
     * API Key の格納場所.
     * <p>
     * query or header
     * </p>
     * TODO enum で宣言した方が良いか？
     */
    private String mIn;

    /**
     * OAuth2 で使用されるフロー.
     * <p>
     * implicit, password, application, accessCode
     * </p>
     * TODO enum で宣言した方が良いか？
     */
    private String mFlow;

    /**
     * OAuth2 のフローで使用される認証用URL.
     */
    private String mAuthorizationUrl;

    /**
     * OAuth2 で使用されるトークン用URL.
     */
    private String mTokenUrl;

    /**
     * OAuth2 で使用されるスコープのリスト.
     */
    private SecurityScopes mSecurityScopes;

    /**
     * セキュリティタイプを取得します.
     *
     * <p>
     * basic, apiKey, oauth2
     * </p>
     *
     * @return セキュリティタイプ
     */
    public String getType() {
        return mType;
    }

    /**
     * セキュリティタイプを設定します.
     *
     * @param type セキュリティタイプ
     */
    public void setType(String type) {
        mType = type;
    }

    /**
     * セキュリティの詳細を取得します.
     *
     * @return セキュリティの詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * セキュリティの詳細を設定します.
     *
     * @param description セキュリティの詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * API Key の header または query の変数名を取得します.
     *
     * @return API Key の header または query の変数名.
     */
    public String getName() {
        return mName;
    }

    /**
     * API Key の header または query の変数名を設定します.
     *
     * @param name API Key の header または query の変数名.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * API Key の格納場所を取得します.
     *
     * @return API Key の格納場所
     */
    public String getIn() {
        return mIn;
    }

    /**
     * API Key の格納場所を設定します.
     *
     * @param in API Key の格納場所.
     */
    public void setIn(String in) {
        mIn = in;
    }

    /**
     * OAuth2 で使用されるフローを取得します.
     *
     * @return OAuth2 で使用されるフロー
     */
    public String getFlow() {
        return mFlow;
    }

    /**
     * OAuth2 で使用されるフロー設定します.
     *
     * @param flow OAuth2 で使用されるフロー
     */
    public void setFlow(String flow) {
        mFlow = flow;
    }

    /**
     * OAuth2 のフローで使用される認証用URLを取得します.
     *
     * @return OAuth2 のフローで使用される認証用URL
     */
    public String getAuthorizationUrl() {
        return mAuthorizationUrl;
    }

    /**
     * OAuth2 のフローで使用される認証用URLを設定します.
     *
     * @param authorizationUrl OAuth2 のフローで使用される認証用URL
     */
    public void setAuthorizationUrl(String authorizationUrl) {
        mAuthorizationUrl = authorizationUrl;
    }

    /**
     * OAuth2 で使用されるトークン用URLを取得します.
     *
     * @return OAuth2 で使用されるトークン用URL
     */
    public String getTokenUrl() {
        return mTokenUrl;
    }

    /**
     * OAuth2 で使用されるトークン用URLを設定します.
     *
     * @param tokenUrl OAuth2 で使用されるトークン用URL
     */
    public void setTokenUrl(String tokenUrl) {
        mTokenUrl = tokenUrl;
    }

    /**
     * OAuth2 で使用されるスコープのリストを取得します.
     *
     * @return OAuth2 で使用されるスコープのリスト
     */
    public SecurityScopes getSecurityScopes() {
        return mSecurityScopes;
    }

    /**
     * OAuth2 で使用されるスコープのリストを設定します.
     *
     * @param securityScopes OAuth2 で使用されるスコープのリスト
     */
    public void setSecurityScopes(SecurityScopes securityScopes) {
        mSecurityScopes = securityScopes;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mType != null) {
            bundle.putString("type", mType);
        }

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mName != null) {
            bundle.putString("name", mName);
        }

        if (mIn != null) {
            bundle.putString("in", mIn);
        }

        if (mFlow != null) {
            bundle.putString("flow", mFlow);
        }

        if (mAuthorizationUrl != null) {
            bundle.putString("authorizationUrl", mAuthorizationUrl);
        }

        if (mTokenUrl != null) {
            bundle.putString("tokenUrl", mTokenUrl);
        }

        if (mSecurityScopes != null) {
            bundle.putParcelable("scopes", mSecurityScopes.toBundle());
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
