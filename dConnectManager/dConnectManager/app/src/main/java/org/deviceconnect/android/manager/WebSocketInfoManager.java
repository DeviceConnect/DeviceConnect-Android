/*
 WebSocketInfoManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * イベント送信経路(WebSocket)管理クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebSocketInfoManager {
    /**
     * WebSocketを格納しておくMap.
     */
    private final Map<String, WebSocketInfo> mWebSocketInfoMap = new HashMap<>();

    /**
     * WebSocketのイベントリスナー.
     */
    private final List<OnWebSocketEventListener> mOnWebSocketEventListeners = new ArrayList<>();

    /**
     * WebSocketのイベントを通知するリスナーを登録する.
     *
     * @param listener リスナー
     */
    public void addOnWebSocketEventListener(final OnWebSocketEventListener listener) {
        synchronized (mOnWebSocketEventListeners) {
            for (OnWebSocketEventListener cache : mOnWebSocketEventListeners) {
                if (cache == null) {
                    return;
                }
            }
            mOnWebSocketEventListeners.add(listener);
        }
    }

    /**
     * WebSocketのイベントを通知するリスナーを削除する.
     *
     * @param listener リスナー
     */
    public void removeOnWebSocketEventListener(final OnWebSocketEventListener listener) {
        synchronized (mOnWebSocketEventListeners) {
            for (Iterator<OnWebSocketEventListener> it = mOnWebSocketEventListeners.iterator(); it.hasNext(); ) {
                OnWebSocketEventListener cache = it.next();
                if (cache == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    private void notifyOnDisconnect(final String origin) {
        synchronized (mOnWebSocketEventListeners) {
            for (OnWebSocketEventListener listener : mOnWebSocketEventListeners) {
                listener.onDisconnect(origin);
            }
        }
    }

    /**
     * WebSocketを追加する.
     *
     * @param origin WebSocketのオリジン、もしくはセッションキー
     * @param uri URI
     * @param webSocketId WebSocketの識別子.
     */
    void addWebSocketInfo(final String origin, final String uri, final String webSocketId) {
        WebSocketInfo info = new WebSocketInfo();
        info.setRawId(webSocketId);
        info.setUri(uri);
        info.setOrigin(origin);
        info.setConnectTime(System.currentTimeMillis());
        mWebSocketInfoMap.put(origin, info);
    }

    /**
     * WebSocketを削除する.
     *
     * @param origin WebSocketのオリジン、もしくはセッションキー
     */
    void removeWebSocketInfo(final String origin) {
        WebSocketInfo info = mWebSocketInfoMap.remove(origin);
        if (info != null) {
            notifyOnDisconnect(origin);
        }
    }

    /**
     * 指定されたオリジン、もしくはセッションキーに対応するWebSocketを取得する.
     *
     * @param origin WebSocketのオリジン、もしくはセッションキー
     * @return WebSocketの情報
     */
    WebSocketInfo getWebSocketInfo(final String origin) {
        return mWebSocketInfoMap.get(origin);
    }

    /**
     * 登録されているWebSocketの一覧を取得する.
     *
     * @return WebSocketの一覧
     */
    public List<WebSocketInfo> getWebSocketInfos() {
        return new ArrayList<>(mWebSocketInfoMap.values());
    }

    /**
     * WebSocketのイベントを通知するリスナー.
     *
     * @author NTT DOCOMO, INC.
     */
    public interface OnWebSocketEventListener {
        /**
         * WebSocketが切断されたことを通知する.
         * @param origin WebSocketのオリジン、もしくはセッションキー
         */
        void onDisconnect(String origin);
    }
}
