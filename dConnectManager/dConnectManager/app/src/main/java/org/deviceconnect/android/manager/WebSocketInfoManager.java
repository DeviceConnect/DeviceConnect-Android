/*
 WebSocketInfoManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
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
    private Map<String, WebSocketInfo> mWebSocketInfoMap = new HashMap<>();

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * プラグイン管理クラス.
     */
    private DevicePluginManager mDevicePluginManager;

    /**
     * WebSocketのイベントリスナー.
     */
    private OnWebSocketEventListener mOnWebSocketEventListener;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public WebSocketInfoManager(final Context context) {
        mContext = context;
        mDevicePluginManager = ((DConnectApplication) mContext).getDevicePluginManager();
    }

    /**
     * WebSocketのイベントを通知するリスナーを設定する.
     *
     * @param onWebSocketEventListener リスナー
     */
    public void setOnWebSocketEventListener(final OnWebSocketEventListener onWebSocketEventListener) {
        mOnWebSocketEventListener = onWebSocketEventListener;
    }

    /**
     * WebSocketを追加する.
     *
     * @param origin WebSocketのオリジン、もしくはセッションキー
     * @param uri URI
     * @param webSocketId WebSocketの識別子.
     */
    public void addWebSocketInfo(final String origin, final String uri, final String webSocketId) {
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
    public void removeWebSocketInfo(final String origin) {
        WebSocketInfo info = mWebSocketInfoMap.remove(origin);
        if (info != null) {
            notifyDisconnectWebSocket(info.getOrigin());

            if (mOnWebSocketEventListener != null) {
                mOnWebSocketEventListener.onDisconnect(origin);
            }
        }
    }

    /**
     * 指定されたオリジン、もしくはセッションキーに対応するWebSocketを取得する.
     *
     * @param origin WebSocketのオリジン、もしくはセッションキー
     * @return WebSocketの情報
     */
    public WebSocketInfo getWebSocketInfo(final String origin) {
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
     * 全プラグインにWebSocketが切断されたことを通知する.
     * @param origin オリジン
     */
    private void notifyDisconnectWebSocket(final String origin) {
        List<DevicePlugin> plugins = mDevicePluginManager.getDevicePlugins();
        for (DevicePlugin plugin : plugins) {
            String serviceId = plugin.getPluginId();
            Intent request = new Intent();
            request.setComponent(plugin.getComponentName());
            request.setAction(IntentDConnectMessage.ACTION_EVENT_TRANSMIT_DISCONNECT);
            request.putExtra(SystemProfileConstants.PARAM_PLUGIN_ID, serviceId);
            request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, origin);
            mContext.sendBroadcast(request);
        }
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
