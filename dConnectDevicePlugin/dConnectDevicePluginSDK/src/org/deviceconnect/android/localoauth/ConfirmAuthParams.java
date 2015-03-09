/*
 ConfirmAuthParams.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

/**
 * 承認確認画面起動パラメータ.<br>
 * <table>
 * <tr>
 * <td></td>
 * <td>デバイスプラグイン用</td>
 * <td>アプリ用</td>
 * </tr>
 * <tr>
 * <td>mCcontext</td>
 * <td>○設定必要</td>
 * <td>○設定必要</td>
 * </tr>
 * <tr>
 * <td>mApplicationName</td>
 * <td>○設定必要</td>
 * <td>○設定必要</td>
 * </tr>
 * <tr>
 * <td>mClientId</td>
 * <td>○設定必要</td>
 * <td>○設定必要</td>
 * </tr>
 * <tr>
 * <td>mServiceId</td>
 * <td>○設定必要</td>
 * <td>×設定不要</td>
 * </tr>
 * <tr>
 * <td>mScopes</td>
 * <td>○設定必要</td>
 * <td>○設定必要</td>
 * </tr>
 * <tr>
 * <td>mIsForDevicePlugin</td>
 * <td>○trueを設定</td>
 * <td>○falseを設定</td>
 * </tr>
 * <tr>
 * <td>mServicePackageName</td>
 * <td>○設定必要</td>
 * <td>○設定必要</td>
 * </tr>
 * <tr>
 * <td>mPublishAccessTokenListener</td>
 * <td>○設定必要</td>
 * <td>○設定必要</td>
 * </tr>
 * </table>
 * @author NTT DOCOMO, INC.
 */
public class ConfirmAuthParams {

    /** コンテキスト. */
    private android.content.Context mContext;

    /** アプリケーション名. */
    private String mApplicationName;

    /** クライアントID. */
    private String mClientId;

    /** サービスID(デバイスプラグイン用の場合のみ設定する). */
    private String mServiceId;

    /** スコープ名. */
    private String[] mScope;

    /** デバイスプラグイン用の承認確認画面か.　 */
    private boolean mIsForDevicePlugin;

    /**
     * コンストラクタ.
     */
    public ConfirmAuthParams() {
        mContext = null;
        mApplicationName = null;
        mClientId = null;
        mServiceId = null;
        mScope = null;
        mIsForDevicePlugin = true;
    }

    /**
     * コンストラクタ.
     * 
     * @param builder ビルダー
     */
    private ConfirmAuthParams(final Builder builder) {
        // Builderを用いるためprivateに設定。
        this.mContext = builder.mContext;
        this.mApplicationName = builder.mApplicationName;
        this.mClientId = builder.mClientId;
        this.mServiceId = builder.mServiceId;
        this.mScope = builder.mScope;
        this.mIsForDevicePlugin = builder.mIsForDevicePlugin;
    }

    /**
     * Contextを取得.
     * 
     * @return Context
     */
    public android.content.Context getContext() {
        return mContext;
    }

    /**
     * Contextを設定.
     * 
     * @param context Context
     */
    public void setContext(final android.content.Context context) {
        this.mContext = context;
    }

    /**
     * アプリケーション名を取得.
     * 
     * @return アプリケーション名
     */
    public String getApplicationName() {
        return mApplicationName;
    }

    /**
     * アプリケーション名を設定.
     * 
     * @param applicationName アプリケーション名
     */
    public void setApplicationName(final String applicationName) {
        this.mApplicationName = applicationName;
    }

    /**
     * クライアントIDを取得.
     * 
     * @return クライアントID
     */
    public String getClientId() {
        return mClientId;
    }

    /**
     * クライアントIDを設定.
     * 
     * @param clientId クライアントID
     */
    public void setClientId(final String clientId) {
        this.mClientId = clientId;
    }

    /**
     * サービスIDを取得.
     * 
     * @return サービスID
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * サービスIDを設定.
     * 
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        this.mServiceId = serviceId;
    }

    /**
     * スコープを取得.
     * 
     * @return スコープ
     */
    public String[] getScopes() {
        return mScope;
    }
    
    /**
     * スコープを設定.
     * 
     * @param scopes スコープ
     */
    public void setScopes(final String[] scopes) {
        this.mScope = scopes;
    }

    /**
     * デバイスプラグイン向け承認画面フラグを取得.
     * 
     * @return デバイスプラグイン向け承認画面フラグ
     */
    public boolean isForDevicePlugin() {
        return mIsForDevicePlugin;
    }

    /**
     * デバイスプラグイン向け承認画面フラグを設定.
     * 
     * @param isForDevicePlugin デバイスプラグイン向け承認画面フラグ
     */
    public void setForDevicePlugin(final boolean isForDevicePlugin) {
        this.mIsForDevicePlugin = isForDevicePlugin;
    }

    /**
     * ConfirmAuthParamsのビルダークラス.
     */
    public static final class Builder {
        
        /** コンテキスト. */
        private android.content.Context mContext;

        /** アプリケーション名. */
        private String mApplicationName;

        /** クライアントID. */
        private String mClientId;

        /** サービスID(デバイスプラグイン用の場合のみ設定する). */
        private String mServiceId;

        /** スコープ. */
        private String[] mScope;

        /** デバイスプラグイン用の承認確認画面か.　 */
        private boolean mIsForDevicePlugin;

        /**
         * ConfirmAuthParamsのインスタンスを設定された設定値で生成する.
         * 
         * @return ConfirmAuthParamsのインスタンス。
         */
        public ConfirmAuthParams build() {

            if (mContext == null) {
                throw new IllegalArgumentException("mCcontext must be not null.");
            } else if (mApplicationName == null) {
                throw new IllegalArgumentException("mApplicationName must be not null.");
            } else if (mClientId == null) {
                throw new IllegalArgumentException("mClientId must be not null.");
            } else if (mScope == null) {
                throw new IllegalArgumentException("mScopes must be not null.");
            }

            return new ConfirmAuthParams(this);
        }
        
        /**
         * コンテキストを設定する.
         * @param context コンテキスト
         * @return ビルダー。
         */
        public Builder context(final android.content.Context context) {
            mContext = context;
            return this;
        }
        
        /**
         * アプリケーション名を設定する.
         * @param applicationName アプリケーション名
         * @return ビルダー。
         */
        public Builder applicationName(final String applicationName) {
            mApplicationName = applicationName;
            return this;
        }

        /**
         * クライアントIDを設定する.
         * @param clientId クライアントID
         * @return ビルダー。
         */
        public Builder clientId(final String clientId) {
            mClientId = clientId;
            return this;
        }

        /**
         * サービスIDを設定する(デバイスプラグイン用の場合のみ設定する).
         * @param serviceId サービスID
         * @return ビルダー。
         */
        public Builder serviceId(final String serviceId) {
            mServiceId = serviceId;
            return this;
        }

        /**
         * スコープを設定する.
         * @param scopes   スコープ
         * @return ビルダー。
         */
        public Builder scopes(final String[] scopes) {
            mScope = scopes;
            return this;
        }

        /**
         * デバイスプラグイン用の承認確認画面か.
         * @param isForDevicePlugin デバイスプラグイン用の承認確認画面ならtrueを、アプリ用ならfalseを設定する。
         * @return ビルダー。
         */
        public Builder isForDevicePlugin(final boolean isForDevicePlugin) {
            mIsForDevicePlugin = isForDevicePlugin;
            return this;
        }
    }
}
