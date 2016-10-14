/*
 WebSocketInfoManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.content.Context;
import android.content.Intent;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;

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

    private Map<String, WebSocketInfo> mWebSocketInfoMap = new HashMap<>();

    private Context mContext;
    private DevicePluginManager mDevicePluginManager;

    private OnWebSocketEventListener mOnWebSocketEventListener;

    public WebSocketInfoManager(final Context context) {
        mContext = context;
        mDevicePluginManager = ((DConnectApplication) mContext).getDevicePluginManager();
    }

    public void setOnWebSocketEventListener(final OnWebSocketEventListener onWebSocketEventListener) {
        mOnWebSocketEventListener = onWebSocketEventListener;
    }

    public void addWebSocketInfo(final String receiverId, final String uri, final String webSocketId) {
        WebSocketInfo info = new WebSocketInfo();
        info.setRawId(webSocketId);
        info.setUri(uri);
        info.setReceiverId(receiverId);
        info.setConnectTime(System.currentTimeMillis());
        mWebSocketInfoMap.put(receiverId, info);
    }

    public void removeWebSocketInfo(final String eventKey) {
        WebSocketInfo info = mWebSocketInfoMap.remove(eventKey);
        if (info != null) {
            notifyDisconnectWebSocket(info.getReceiverId());

            if (mOnWebSocketEventListener != null) {
                mOnWebSocketEventListener.onDisconnect(eventKey);
            }
        }
    }

    public WebSocketInfo getWebSocketInfo(final String receiverId) {
        return mWebSocketInfoMap.get(receiverId);
    }

    public List<WebSocketInfo> getWebSocketInfos() {
        return new ArrayList<>(mWebSocketInfoMap.values());
    }

    private void notifyDisconnectWebSocket(final String origin) {
        List<DevicePlugin> plugins = mDevicePluginManager.getDevicePlugins();
        for (DevicePlugin plugin : plugins) {
            String serviceId = plugin.getPluginId();
            Intent request = new Intent();
            request.setComponent(plugin.getComponentName());
            request.setAction(IntentDConnectMessage.ACTION_EVENT_TRANSMIT_DISCONNECT);
            request.putExtra("pluginId", serviceId);
            request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, origin);
            mContext.sendBroadcast(request);
        }
    }

    public interface OnWebSocketEventListener {
        void onDisconnect(String sessionKey);
    }
}
