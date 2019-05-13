/*
 DConnectApiSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Device Connect APIの仕様を保持するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectApiSpec implements DConnectSpecConstants {

    private Type mType;
    private Method mMethod;
    private String mApiName;
    private String mProfileName;
    private String mInterfaceName;
    private String mAttributeName;
    private DConnectParameterSpec[] mRequestParamList;

    private DConnectApiSpec() {}

    /**
     * APIの種類を設定する.
     * @param type APIの種類
     */
    void setType(final Type type) {
        mType = type;
    }

    /**
     * APIの種類を取得する.
     * @return APIの種類
     */
    public Type getType() {
        return mType;
    }

    /**
     * APIのメソッドを設定する.
     * @param method APIのメソッド
     */
    void setMethod(final Method method) {
        mMethod = method;
    }

    /**
     * APIのメソッドを取得する.
     * @return APIのメソッド
     */
    public Method getMethod() {
        return mMethod;
    }

    void setApiName(final String apiName) {
        mApiName = apiName;
    }

    void setProfileName(final String profileName) {
        mProfileName = profileName;
    }

    void setInterfaceName(final String interfaceName) {
        mInterfaceName = interfaceName;
    }

    void setAttributeName(final String attributeName) {
        mAttributeName = attributeName;
    }

    /**
     * API名を取得する.
     * @return API名
     */
    public String getApiName() {
        return mApiName;
    }

    /**
     * プロファイル名を取得する.
     * @return プロファイル名
     */
    public String getProfileName() {
        return mProfileName;
    }

    /**
     * インターフェース名を取得する.
     * @return インターフェース名
     */
    public String getInterfaceName() {
        return mInterfaceName;
    }

    /**
     * アトリビュート名を取得する.
     * @return アトリビュート名
     */
    public String getAttributeName() {
        return mAttributeName;
    }

    /**
     * リクエストパラメータ仕様の一覧を設定する.
     * @param paramList リクエストパラメータ仕様の配列
     */
    void setRequestParamList(final DConnectParameterSpec[] paramList) {
        mRequestParamList = paramList;
    }

    /**
     * リクエストパラメータ仕様の一覧を取得する.
     * @return リクエストパラメータ仕様の配列
     */
    public DConnectParameterSpec[] getRequestParamList() {
        return mRequestParamList;
    }

    /**
     * リクエストの内容が仕様に反していないことを確認する.
     *
     * @param request リクエスト
     * @return 仕様に反していない場合は<code>true</code>. そうでない場合は<code>false</code>
     */
    public boolean validate(final Intent request) {
        Bundle extras = request.getExtras();
        for (DConnectParameterSpec paramSpec : getRequestParamList()) {
            Object paramValue = extras.get(paramSpec.getName());
            if (!paramSpec.validate(paramValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@link DConnectApiSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder {
        private Type mType;
        private Method mMethod;
        private List<DConnectParameterSpec> mParamList;

        /**
         * APIの種類を設定する.
         * @param type APIの種類
         */
        public Builder setType(final Type type) {
            mType = type;
            return this;
        }

        /**
         * APIのメソッドを設定する.
         * @param method APIのメソッド
         * @return ビルダー自身のインスタンス
         */
        public Builder setMethod(final Method method) {
            mMethod = method;
            return this;
        }

        /**
         * リクエストパラメータ仕様の一覧を設定する.
         * @param paramList リクエストパラメータ仕様のリスト
         * @return ビルダー自身のインスタンス
         */
        public Builder setRequestParamList(final List<DConnectParameterSpec> paramList) {
            mParamList = paramList;
            return this;
        }

        /**
         * {@link DConnectApiSpec}のインスタンスを生成する.
         *
         * @return {@link DConnectApiSpec}のインスタンス
         */
        public DConnectApiSpec build() {
            if (mParamList == null) {
                mParamList = new ArrayList<DConnectParameterSpec>();
            }
            DConnectApiSpec spec = new DConnectApiSpec();
            spec.setType(mType);
            spec.setMethod(mMethod);
            spec.setRequestParamList(
                mParamList.toArray(new DConnectParameterSpec[mParamList.size()]));
            return spec;
        }
    }

}
