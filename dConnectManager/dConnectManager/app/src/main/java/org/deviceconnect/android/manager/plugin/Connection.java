/*
 Connection.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


import android.content.Intent;

/**
 * プラグインとの通信機能を提供するインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface Connection {

    /**
     * 接続先のプラグインIDを取得する.
     * @return プラグインID
     */
    String getPluginId();

    /**
     * プラグインとの接続タイプを取得する.
     * @return 接続タイプ
     */
    ConnectionType getType();

    /**
     * プラグインとの接続状態を取得する.
     * @return 接続状態
     */
    ConnectionState getState();

    /**
     * プラグインとの接続を確立する.
     * なお、本メソッドは同期的に処理される.
     *
     * @throws ConnectingException 接続に失敗した場合
     */
    void connect() throws ConnectingException;

    /**
     * プラグインとの接続を切断する.
     * なお、本メソッドは同期的に処理される.
     */
    void disconnect();

    /**
     * 接続状態変更通知のリスナーを追加する.
     *
     * @param listener リスナー
     */
    void addConnectionStateListener(ConnectionStateListener listener);

    /**
     * 接続状態変更通知のリスナーを解除する.
     *
     * @param listener リスナー
     */
    void removeConnectionStateListener(ConnectionStateListener listener);

    /**
     * プラグインに対してインテント形式のメッセージを送信する.
     *
     * @param message メッセージ
     * @throws MessagingException 送信に失敗した場合
     */
    void send(Intent message) throws MessagingException;

}
