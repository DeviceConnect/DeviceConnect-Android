package org.deviceconnect.android.manager;

import android.content.Context;
import android.content.Intent;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketInfoManager {

    private Map<String, WebSocketInfo> mWebSocketInfoMap = new HashMap<>();

    private Context mContext;
    private DevicePluginManager mDevicePluginManager;

    public WebSocketInfoManager(final Context context) {
        mContext = context;
        mDevicePluginManager = ((DConnectApplication) mContext).getDevicePluginManager();
    }

    public void addWebSocketInfo(final String eventKey, final String uri) {
        WebSocketInfo info = new WebSocketInfo();
        info.setUri(uri);
        info.setEventKey(eventKey);
        info.setConnectTime(System.currentTimeMillis());
        mWebSocketInfoMap.put(eventKey, info);
    }

    public void removeWebSocketInfo(final String eventKey) {
        WebSocketInfo info = mWebSocketInfoMap.remove(eventKey);
        if (info != null) {
            notifyDisconnectWebSocket(info.getEventKey());
        }
    }

    public WebSocketInfo getWebSocketInfo(final String eventKey) {
        return mWebSocketInfoMap.get(eventKey);
    }

    public List<WebSocketInfo> getWebSocketInfos() {
        return new ArrayList<>(mWebSocketInfoMap.values());
    }

    private void notifyDisconnectWebSocket(final String sessionKey) {
        List<DevicePlugin> plugins = mDevicePluginManager.getDevicePlugins();
        for (DevicePlugin plugin : plugins) {
            String serviceId = plugin.getServiceId();
            Intent request = new Intent();
            request.setComponent(plugin.getComponentName());
            request.setAction(IntentDConnectMessage.ACTION_EVENT_TRANSMIT_DISCONNECT);
            request.putExtra("pluginId", serviceId);
            request.putExtra(IntentDConnectMessage.EXTRA_SESSION_KEY, sessionKey + DConnectMessageService.SEPARATOR + serviceId);
            mContext.sendBroadcast(request);
        }
    }
}
