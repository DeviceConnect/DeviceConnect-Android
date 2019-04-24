/*
 Parameter.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models.parameters;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.AbstractSpec;

/**
 * API 操作で使用されるパラメータ情報.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class Parameter extends AbstractSpec {
    /**
     * パラメータ名.
     * <p>
     * Required
     * </p>
     */
    private String mName;

    /**
     * パラメータの入力箇所.
     * <p>
     * Required
     * </p>
     * <p>
     * query、header、path、formData、body が設定可能。
     * </p>
     *
     * TODO enum で宣言した方が良いか？
     */
    private String mIn;

    /**
     * パラメータの詳細.
     */
    private String mDescription;

    /**
     * パラメータの必須宣言.
     */
    private Boolean mRequired;

    /**
     * パラメータ名を取得します.
     *
     * @return パラメータ名
     */
    public String getName() {
        return mName;
    }

    /**
     * パラメータ名を設定します.
     *
     * @param name パラメータ名
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * パラメータの入力箇所を取得します.
     *
     * @return ラメータの入力箇所
     */
    public String getIn() {
        return mIn;
    }

    /**
     * パラメータの入力箇所を設定します.
     *
     * @param in パラメータの入力箇所
     */
    public void setIn(String in) {
        mIn = in;
    }

    /**
     * パラメータの詳細を取得します.
     *
     * @return パラメータの詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * パラメータの詳細を設定します.
     *
     * @param description パラメータの詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * パラメータの必須宣言を取得します.
     *
     * @return パラメータの必須宣言
     */
    public boolean isRequired() {
        return mRequired != null ? mRequired : false;
    }

    /**
     * パラメータの必須宣言を設定します.
     *
     * @param required パラメータの必須宣言
     */
    public void setRequired(boolean required) {
        mRequired = required;
    }

    void copyParameter(Bundle bundle) {
        if (mName != null) {
            bundle.putString("name", mName);
        }

        if (mIn != null) {
            bundle.putString("in", mIn);
        }

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mRequired != null) {
            bundle.putBoolean("required", mRequired);
        }
    }
}
