/*
 DConnectServerConfig.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server;

import java.util.List;

/**
 * サーバーの設定情報.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class DConnectServerConfig {

    // サーバーの設定値は起動後などに変更されるのを防ぐため Builder でパラメータを設定させsetterは本体には置かない。

    /**
     * Assets をドキュメントルートにする場合の定義.
     */
    public static final String DOC_ASSETS = "file:///android_asset/";

    /** 最大コネクション数. */
    private int mMaxConnectionSize;

    /** WebSocket最大コネクション数. */
    private int mMaxWebSocketConnectionSize;

    /** ドキュメントルートのパス. */
    private String mDocumentRootPath;

    /** SSLを使うかのフラグ. */
    private boolean mIsSsl;

    /** サーバーのポート番号. */
    private int mPort;

    /** サーバーのホスト名. */
    private String mHost;

    /** IPのホワイトリスト. */
    private List<String> mIpWhiteList;

    /** ファイルなどのキャッシュをおくフォルダへのパス. */
    private String mCachePath;

    /** 文字コード. */
    private String mCharset = "UTF-8";

    /** アクセスログ機能の有効. */
    private boolean mEnableAccessLog;

    /**
     * 最大コネクション数を取得する.
     * 
     * @return 最大コネクション数
     */
    public int getMaxConnectionSize() {
        return mMaxConnectionSize;
    }

    /**
     * WebSocketの最大コネクション数を取得する.
     * 
     * @return WebSocketの最大コネクション数。
     */
    public int getMaxWebSocketConnectionSize() {
        return mMaxWebSocketConnectionSize;
    }

    /**
     * ドキュメントルートのパスを取得する.
     * 
     * @return ドキュメントルートのパス
     */
    public String getDocumentRootPath() {
        return mDocumentRootPath;
    }

    /**
     * キャッシュ置き場へのパスを取得する.
     * @return キャッシュ置き場へのパス
     */
    public String getCachePath() {
        return mCachePath;
    }

    /**
     * ポート番号を取得する.
     * 
     * @return ポート番号
     */
    public int getPort() {
        return mPort;
    }

    /**
     * ホスト名を取得する.
     * 
     * @return ホスト名
     */
    public String getHost() {
        return mHost;
    }

    /**
     * SSL通信を行うかをチェックする.
     * 
     * @return SSL通信をする場合true、しない場合はfalseを返す。
     */
    public boolean isSsl() {
        return mIsSsl;
    }

    /**
     * 文字コードを取得する.
     * @return 文字コード
     */
    public String getCharset() {
        return mCharset;
    }

    /**
     * IPのホワイトリストを取得する.
     * 
     * @return IPのホワイトリスト。
     */
    public List<String> getIPWhiteList() {
        return mIpWhiteList;
    }

    /**
     * アクセスログが有効になっているか確認する.
     *
     * @return アクセスログが有効の場合にはtrue、それ以外はfalse
     */
    public boolean isEnableAccessLog() {
        return mEnableAccessLog;
    }

    /**
     * コンストラクタ.
     * 
     * @param builder ビルダー。
     */
    private DConnectServerConfig(final Builder builder) {
        // Builderを用いるためprivateに設定。
        this.mDocumentRootPath = builder.mDocumentRootPath;
        this.mMaxConnectionSize = builder.mMaxConnectionSize;
        this.mMaxWebSocketConnectionSize = builder.mMaxWebSocketConnectionSize;
        this.mIsSsl = builder.mIsSsl;
        this.mPort = builder.mPort;
        this.mHost = builder.mHost;
        this.mCachePath = builder.mCachePath;
        this.mIpWhiteList = builder.mIpWhiteList;
        this.mCharset = builder.mCharset;
        this.mEnableAccessLog = builder.mEnableAccessLog;
    }

    /**
     * DConnectServerConfigのビルダークラス.
     * 
     * @author NTT DOCOMO, INC.
     * 
     */
    public static final class Builder {

        /** 最大コネクション数. */
        private int mMaxConnectionSize = 64;

        /** WebSocket最大コネクション数. */
        private int mMaxWebSocketConnectionSize = 32;

        /** ドキュメントルートのパス. */
        private String mDocumentRootPath;

        /** ファイルなどのキャッシュをおくフォルダへのパス. */
        private String mCachePath;

        /** SSLを使うかのフラグ. */
        private boolean mIsSsl;

        /** サーバーのポート番号. */
        private int mPort = -1;

        /** サーバーのホスト名. */
        private String mHost;

        /** IPのホワイトリスト. */
        private List<String> mIpWhiteList;

        /** 文字コード. */
        private String mCharset = "UTF-8";

        /** アクセスログ機能の設定. **/
        private boolean mEnableAccessLog;

        /**
         * DConnectServerConfigのインスタンスを設定された設定値で生成する.
         * 
         * @return DConnectServerConfigのインスタンス。
         * @throws IllegalArgumentException Port番号が設定されていない場合
         */
        public DConnectServerConfig build() {

            if (mPort < 0) {
                throw new IllegalArgumentException("Port must be larger than 0.");
            }

            return new DConnectServerConfig(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "mMaxConnectionSize=" + mMaxConnectionSize +
                    ", mMaxWebSocketConnectionSize=" + mMaxWebSocketConnectionSize +
                    ", mDocumentRootPath='" + mDocumentRootPath + '\'' +
                    ", mCachePath='" + mCachePath + '\'' +
                    ", mIsSsl=" + mIsSsl +
                    ", mPort=" + mPort +
                    ", mHost='" + mHost + '\'' +
                    ", mIpWhiteList=" + mIpWhiteList +
                    ", mCharset='" + mCharset + '\'' +
                    ", mEnableAccessLog='" + mEnableAccessLog + '\'' +
                    '}';
        }

        /**
         * 最大コネクション数を設定する.
         * 
         * @param maxConnectionSize 最大コネクション数。1以上を指定すること。
         * 
         * @return ビルダー。
         */
        public Builder maxConnectionSize(final int maxConnectionSize) {

            if (maxConnectionSize <= 0) {
                throw new IllegalArgumentException("MaxConnectionSize must be larger than 0.");
            }

            this.mMaxConnectionSize = maxConnectionSize;
            return this;
        }

        /**
         * WebSocketの最大コネクション数を設定する.
         * 
         * @param maxWebSocketConnectionSize WebSocketの最大コネクション数。1以上に設定すること。
         * 
         * @return ビルダー
         */
        public Builder maxWebSocketConnectionSize(final int maxWebSocketConnectionSize) {

            if (maxWebSocketConnectionSize <= 0) {
                throw new IllegalArgumentException("MaxWebSocketConnectionSize must be larger than 0.");
            }

            this.mMaxWebSocketConnectionSize = maxWebSocketConnectionSize;
            return this;
        }

        /**
         * SSLの利用設定を行う.
         * 
         * @param isSsl trueの場合SSL通信を行う。falseの場合はSSL通信を行わない。
         * @return ビルダー。
         */
        public Builder isSsl(final boolean isSsl) {
            this.mIsSsl = isSsl;
            return this;
        }

        /**
         * ポートを設定する.
         * 
         * @param port サーバーのポート番号。
         * @return ビルダー
         */
        public Builder port(final int port) {
            if (port < 0) {
                throw new IllegalArgumentException("Port must be larger than 0.");
            }
            this.mPort = port;
            return this;
        }

        /**
         * ホスト名を設定する.
         * 
         * @param host ホスト名。
         * @return ビルダー。
         */
        public Builder host(final String host) {
            this.mHost = host;
            return this;
        }

        /**
         * ドキュメントルートのパスを設定する.
         * 
         * @param documentRootPath ドキュメントルートのパス。
         * @return ビルダー。
         */
        public Builder documentRootPath(final String documentRootPath) {

            if (documentRootPath == null) {
                throw new IllegalArgumentException("Document root must be not null.");
            }

            this.mDocumentRootPath = documentRootPath;
            return this;
        }

        /**
         * 一時的なキャッシュ置き場へのパスを設定する.
         * @param cachePath キャッシュ置き場へのパス
         * @return ビルダー。
         */
        public Builder cachePath(final String cachePath) {
            if (cachePath == null) {
                throw new IllegalArgumentException("cachePath root must be not null.");
            }
            mCachePath = cachePath;
            return this;
        }

        /**
         * IPのホワイトリストを設定する.
         * <p>
         * 空のリストが設定された場合には、ホワイトリストは無視します。
         * </p>
         * @param ipWhiteList IPのホワイトリスト。
         * @return ビルダー。
         */
        public Builder ipWhiteList(final List<String> ipWhiteList) {
            this.mIpWhiteList = ipWhiteList;
            return this;
        }

        /**
         * 文字コードを設定する.
         * <p>
         * デフォルトでは、UTF-8が設定してあります。
         * </p>
         * @param charset 文字コード
         * @return ビルダー。
         */
        public Builder charset(final String charset) {
            mCharset = charset;
            return this;
        }

        /**
         * アクセスログを設定する.
         *
         * @param enable 有効にする場合はtrue、それ以外はfalse
         * @return ビルダー。
         */
        public Builder accessLog(final boolean enable) {
            mEnableAccessLog = enable;
            return this;
        }
    }
}
