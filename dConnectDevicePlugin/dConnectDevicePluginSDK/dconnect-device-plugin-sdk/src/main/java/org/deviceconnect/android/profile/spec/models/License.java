/*
 License.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

/**
 * 提供する API に関するライセンス.
 *
 * @author NTT DOCOMO, INC.
 */
public class License extends AbstractSpec {
    /**
     * API に適用されるライセンス名.
     * <p>
     * Required
     * </p>
     */
    private String mName;

    /**
     * API に適用されるライセンスへのURL.
     */
    private String mUrl;

    /**
     * API に適用されるライセンス名を取得します.
     *
     * @return  API に適用されるライセンス名
     */
    public String getName() {
        return mName;
    }

    /**
     * API に適用されるライセンス名を設定します.
     *
     * @param name API に適用されるライセンス名
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * API に適用されるライセンスへのURLを取得します.
     *
     * @return API に適用されるライセンスへのURL.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * API に適用されるライセンスへのURLを設定します.
     *
     * @param url API に適用されるライセンスへのURL
     */
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mName != null) {
            bundle.putString("name", mName);
        }

        if (mUrl != null) {
            bundle.putString("url", mUrl);
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
