package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class SecurityScopes extends AbstractSpec {

    private Map<String, String> mScopes;

    public Map<String, String> getScopes() {
        return mScopes;
    }

    public void setScopes(Map<String, String> scopes) {
        mScopes = scopes;
    }

    public void putScope(String key, String value) {
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
