/*
 DConnectParameterSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

/**
 * Device Connect APIのリクエストパラメータの仕様定義.
 *
 * @param <T> DConnectDataSpecの子クラス
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectParameterSpec<T extends DConnectDataSpec> implements DConnectSpecConstants {

    protected final T mDataSpec;
    String mName;
    Boolean mIsRequired;

    /**
     * コンストラクタ.
     *
     * @param dataSpec リクエストパラメータとして受け付けるデータの仕様
     */
    protected DConnectParameterSpec(final T dataSpec) {
        mDataSpec = dataSpec;
    }

    /**
     * データの種類を取得する.
     * @return データの種類
     */
    public DataType getDataType() {
        return mDataSpec.getDataType();
    }

    /**
     * リクエストパラメータの名前を取得する.
     * @return リクエストパラメータの名前
     */
    public String getName() {
        return mName;
    }

    /**
     * リクエストパラメータの名前を設定する.
     * @param name リクエストパラメータの名前
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * 必須パラメータフラグを設定する.
     * @param isRequired 必須パラメータである場合は<code>true</code>. そうでない場合は<code>false</code>
     */
    void setRequired(final boolean isRequired) {
        mIsRequired = isRequired;
    }

    /**
     * 必須パラメータフラグを取得する.
     * @return 必須パラメータである場合は<code>true</code>. そうでない場合は<code>false</code>
     */
    public boolean isRequired() {
        return mIsRequired != null ? mIsRequired : false;
    }

    /**
     * リクエストパラメータとして入力された値が仕様に反していないことを確認する.
     * @param param リクエストパラメータとして入力された値
     * @return 仕様に反していない場合は<code>true</code>. そうでない場合は<code>false</code>
     */
    public final boolean validate(final Object param) {
        if (param == null) {
            return !isRequired();
        }
        return mDataSpec.validate(param);
    }

    abstract static class BaseBuilder<T extends BaseBuilder<T>> {

        protected String mName;
        protected Boolean mIsRequired;

        protected abstract T getThis();

        /**
         * リクエストパラメータの名前を設定する.
         * @param name リクエストパラメータの名前
         * @return ビルダー自身のインスタンス
         */
        public T setName(final String name) {
            mName = name;
            return getThis();
        }

        /**
         * 必須パラメータフラグを設定する.
         * @param isRequired 必須パラメータである場合は<code>true</code>. そうでない場合は<code>false</code>
         * @return ビルダー自身のインスタンス
         */
        public T setRequired(final boolean isRequired) {
            mIsRequired = isRequired;
            return getThis();
        }

    }
}
