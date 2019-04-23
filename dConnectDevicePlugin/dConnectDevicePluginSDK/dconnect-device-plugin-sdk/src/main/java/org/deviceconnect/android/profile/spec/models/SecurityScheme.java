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
     */
    private String mIn;

    /**
     * OAuth2 で使用されるフロー.
     * <p>
     * implicit, password, application, accessCode
     * </p>
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

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getIn() {
        return mIn;
    }

    public void setIn(String in) {
        mIn = in;
    }

    public String getFlow() {
        return mFlow;
    }

    public void setFlow(String flow) {
        mFlow = flow;
    }

    public String getAuthorizationUrl() {
        return mAuthorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        mAuthorizationUrl = authorizationUrl;
    }

    public String getTokenUrl() {
        return mTokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        mTokenUrl = tokenUrl;
    }

    public SecurityScopes getSecurityScopes() {
        return mSecurityScopes;
    }

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
