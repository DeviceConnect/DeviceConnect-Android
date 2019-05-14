/*
 Tag.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

/**
 * タグ情報.
 *
 * @author NTT DOCOMO, INC.
 */
public class Tag extends AbstractSpec {
    /**
     * タグの名前.
     * <p>
     *  Required
     * </p>
     */
    private String mName;

    /**
     * タグの詳細.
     */
    private String mDescription;

    /**
     * タグに関する追加の外部文書.
     */
    private ExternalDocs mExternalDocs;

    /**
     * タグの名前を取得します.
     *
     * @return タグの名前
     */
    public String getName() {
        return mName;
    }

    /**
     * タグの名前を設定します.
     *
     * @param name タグの名前
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * タグの詳細を取得します.
     *
     * @return タグの詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * タグの詳細を設定します.
     *
     * @param description タグの詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * タグに関する追加の外部文書を取得します.
     *
     * @return タグに関する追加の外部文書
     */
    public ExternalDocs getExternalDocs() {
        return mExternalDocs;
    }

    /**
     * タグに関する追加の外部文書を設定します.
     *
     * @param externalDocs タグに関する追加の外部文書
     */
    public void setExternalDocs(ExternalDocs externalDocs) {
        mExternalDocs = externalDocs;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mName != null) {
            bundle.putString("name", mName);
        }

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mExternalDocs != null) {
            bundle.putParcelable("externalDocs", mExternalDocs.toBundle());
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
