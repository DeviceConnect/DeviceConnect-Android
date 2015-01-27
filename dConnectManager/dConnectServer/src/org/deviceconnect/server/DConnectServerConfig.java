/*
 DConnectServerConfig.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server;

import java.util.ArrayList;

/**
 * サーバーの設定情報.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class DConnectServerConfig {

    // サーバーの設定値は起動後などに変更されるのを防ぐためBuilderでパラメータを設定させ
    // setterは本体には置かない。

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
    private ArrayList<String> mIpWhiteList;

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
     * IPのホワイトリストを取得する.
     * 
     * @return IPのホワイトリスト。
     */
    public ArrayList<String> getIPWhiteList() {
        return mIpWhiteList;
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
        this.mIpWhiteList = builder.mIpWhiteList;
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

        /** SSLを使うかのフラグ. */
        private boolean mIsSsl;

        /** サーバーのポート番号. */
        private int mPort = -1;

        /** サーバーのホスト名. */
        private String mHost;

        /** IPのホワイトリスト. */
        private ArrayList<String> mIpWhiteList;

        /**
         * DConnectServerConfigのインスタンスを設定された設定値で生成する.
         * 
         * @return DConnectServerConfigのインスタンス。
         */
        public DConnectServerConfig build() {

            if (mDocumentRootPath == null) {
                throw new IllegalStateException("Document root must be not null.");
            } else if (mPort < 0) {
                throw new IllegalStateException("Port must be larger than 0.");
            }

            return new DConnectServerConfig(this);
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
         * IPのホワイトリストを設定する.
         * 
         * @param ipWhiteList IPのホワイトリスト。
         * @return ビルダー。
         */
        public Builder ipWhiteList(final ArrayList<String> ipWhiteList) {
            this.mIpWhiteList = ipWhiteList;
            return this;
        }
    }
}
