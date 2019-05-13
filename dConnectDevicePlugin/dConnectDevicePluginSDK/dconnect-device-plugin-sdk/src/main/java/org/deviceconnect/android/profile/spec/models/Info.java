/*
 Info.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

/**
 * 提供する API に関するメタデータ.
 *
 * @author NTT DOCOMO, INC.
 */
public class Info extends AbstractSpec {
    /**
     * 提供する API のタイトル.
     */
    private String mTitle;

    /**
     * 提供する API の詳細.
     */
    private String mDescription;

    /**
     * 提供する API の利用規約.
     */
    private String mTermsOfService;

    /**
     * 提供する API の連絡先情報.
     */
    private Contact mContact;

    /**
     * 提供する API のライセンス.
     */
    private License mLicense;

    /**
     * 提供する API のバージョン.
     * <p>
     * Required.
     * </p>
     */
    private String mVersion;

    /**
     * 提供する API のバージョンを取得します.
     *
     * @return 提供する API のバージョン
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * 提供する API のバージョンを設定します.
     *
     * @param version 提供する API のバージョン
     */
    public void setVersion(String version) {
        mVersion = version;
    }

    /**
     * 提供する API の詳細を取得します.
     *
     * @return 提供する API の詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * 提供する API の詳細を設定します.
     *
     * @param description 提供する API の詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * 提供する API のタイトルを取得します.
     *
     * @return 提供する API のタイトル
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * 提供する API のタイトルを設定します.
     *
     * @param title 提供する API のタイトル
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * 提供する API の利用規約を取得します.
     *
     * @return 提供する API の利用規約
     */
    public String getTermsOfService() {
        return mTermsOfService;
    }

    /**
     * 提供する API の利用規約を設定します.
     *
     * @param termsOfService 提供する API の利用規約
     */
    public void setTermsOfService(String termsOfService) {
        mTermsOfService = termsOfService;
    }

    /**
     * 提供する API の連絡先情報を取得します.
     *
     * @return 提供する API の連絡先情報
     */
    public Contact getContact() {
        return mContact;
    }

    /**
     * 提供する API の連絡先情報を設定します.
     *
     * @param contact 提供する API の連絡先情報
     */
    public void setContact(Contact contact) {
        mContact = contact;
    }

    /**
     * 提供する API のライセンスを取得します.
     *
     * @return 提供する API のライセンス
     */
    public License getLicense() {
        return mLicense;
    }

    /**
     * 提供する API のライセンスを設定します.
     *
     * @param license 提供する API のライセンス
     */
    public void setLicense(License license) {
        mLicense = license;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mTitle != null) {
            bundle.putString("title", mTitle);
        }

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mTermsOfService != null) {
            bundle.putString("termsOfService", mTermsOfService);
        }

        if (mContact != null) {
            bundle.putParcelable("contact", mContact.toBundle());
        }

        if (mLicense != null) {
            bundle.putParcelable("license", mLicense.toBundle());
        }

        if (mVersion != null) {
            bundle.putString("version", mVersion);
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
