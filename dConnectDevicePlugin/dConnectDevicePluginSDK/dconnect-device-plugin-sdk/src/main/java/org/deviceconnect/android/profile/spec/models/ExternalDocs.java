/*
 Example.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

/**
 * 外部文書情報.
 *
 * @author NTT DOCOMO, INC.
 */
public class ExternalDocs extends AbstractSpec {
    /**
     * 外部文書の詳細.
     */
    private String mDescription;

    /**
     * 外部文書へのURL.
     */
    private String mUrl;

    /**
     * 外部文書の詳細を取得します.
     *
     * @return 外部文書の詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * 外部文書の詳細を設定します.
     *
     * @param description 外部文書の詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * 外部文書へのURLを取得します.
     *
     * @return 外部文書への URL
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 外部文書へのURLを設定します.
     *
     * @param url 外部文書へのURL
     */
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mUrl != null) {
            bundle.putString("url", mUrl);
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
