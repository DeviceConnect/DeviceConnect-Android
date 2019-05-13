/*
 Contact.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

/**
 * API 提供者の連絡先情報.
 *
 * @author NTT DOCOMO, INC.
 */
public class Contact extends AbstractSpec {
    /**
     * API 提供者の連絡先の個人・組織の名前.
     */
    private String mName;

    /**
     * API 提供者の連絡先のURL.
     */
    private String mUrl;

    /**
     * API 提供者の連絡先のメールアドレス.
     */
    private String mEMail;

    /**
     * API 提供者の連絡先の個人・組織の名前を取得します.
     *
     * @return  API 提供者の連絡先の個人・組織の名前
     */
    public String getName() {
        return mName;
    }

    /**
     * API 提供者の連絡先の個人・組織の名前を設定します.
     *
     * @param name API 提供者の連絡先の個人・組織の名前
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * API 提供者の連絡先のURLを設定します.
     *
     * @return API 提供者の連絡先のURL
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * API 提供者の連絡先のURLを設定します.
     *
     * @param url API 提供者の連絡先のURL
     */
    public void setUrl(String url) {
        mUrl = url;
    }

    /**
     * API 提供者の連絡先のメールアドレスを取得します.
     *
     * @return API 提供者の連絡先のメールアドレス
     */
    public String getEMail() {
        return mEMail;
    }

    /**
     * API 提供者の連絡先のメールアドレスを設定します.
     *
     * @param EMail API 提供者の連絡先のメールアドレス
     */
    public void setEMail(String EMail) {
        mEMail = EMail;
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

        if (mEMail != null) {
            bundle.putString("email", mEMail);
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
