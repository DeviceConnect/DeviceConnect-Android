/*
 DConnectServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server;

import org.deviceconnect.server.logger.LogHandler;
import org.deviceconnect.server.websocket.DConnectWebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Device Connect用HTTPサーバー.
 * 
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectServer {

    /** デバッグフラグ. */
    private static final boolean DEBUG = false;

    /** サーバーイベントの通知を受けるリスナークラス. */
    protected DConnectServerEventListener mListener;

    /** ロガー. */
    protected final Logger mLogger = Logger.getLogger("dconnect.server");

    /** WebSocketのセッション. */
    protected final Map<String, DConnectWebSocket> mSockets;

    /** サーバー設定情報. */
    protected final DConnectServerConfig mConfig;

    /**
     * コンストラクタ. サーバーを生成します
     * 
     * @param config サーバー設定情報。
     */
    public DConnectServer(final DConnectServerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null.");
        }
        mConfig = config;
        mSockets = new ConcurrentHashMap<String, DConnectWebSocket>();

        if (DEBUG) {
            LogHandler handler = new LogHandler("dconnect.server");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.WARNING);
        }
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
     *     指定されたIDのWebSocketが存在しない場合にはnullを返却します。
     * </p>
     * @param webSocketId webSocketのID
     * @return DConnectWebSocketのインスタンス
     */
    public DConnectWebSocket getWebSocket(final String webSocketId) {
        return mSockets.get(webSocketId);
    }
}
