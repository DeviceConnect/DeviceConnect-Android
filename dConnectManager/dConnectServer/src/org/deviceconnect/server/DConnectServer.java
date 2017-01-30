/*
 DConnectServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server;

import org.deviceconnect.server.websocket.DConnectWebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Device Connect用HTTPサーバー.
 * 
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectServer {
    /** サーバーイベントの通知を受けるリスナークラス. */
    protected DConnectServerEventListener mListener;

    /** WebSocketのセッション. */
    protected final Map<String, DConnectWebSocket> mSockets;

    /** サーバー設定情報. */
    protected final DConnectServerConfig mConfig;

    /**
     * コンストラクタ. サーバーを生成します
     * 
     * @param config サーバー設定情報。
     * @throws NullPointerException configに{@code null}が設定された場合に発生
     */
    public DConnectServer(final DConnectServerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null.");
        }
        mConfig = config;
        mSockets = new ConcurrentHashMap<String, DConnectWebSocket>();
    }

    /**
     * サーバーを非同期で起動する. オーバーライドするクラスで非同期処理を実装すること。
     */
    public abstract void start();

    /**
     * サーバーを終了させる.
     */
    public abstract void shutdown();

    /**
     * サーバーが起動しているか調査する.
     * 
     * @return 起動中の場合true、その他はfalseを返す
     */
    public abstract boolean isRunning();

    /**
     * サーバーのバージョンを返す.
     * 
     * @return サーバーバージョン
     */
    public abstract String getVersion();

    /**
     * 設定されたコンフィグ情報を取得します.
     * @return DConnectServerConfigのインスタンス
     */
    public DConnectServerConfig getConfig() {
        return mConfig;
    }

    /**
     * イベントリスナーを設定します.
     * 
     * @param listener リスナーオブジェクト
     */
    public void setServerEventListener(final DConnectServerEventListener listener) {
        mListener = listener;
    }

    /**
     * 指定したIDのWebSocketを取得します.
     * <p>
     *     指定されたIDのWebSocketが存在しない場合には{@code null}を返却します。
     * </p>
     * @param webSocketId webSocketのID
     * @return DConnectWebSocketのインスタンス
     */
    public DConnectWebSocket getWebSocket(final String webSocketId) {
        return mSockets.get(webSocketId);
    }

    /**
     * 接続されているWebSocketの一覧を取得します.
     * @return DConnectWebSocketのマップ
     */
    public Map<String, DConnectWebSocket> getWebSockets() {
        return mSockets;
    }
}
