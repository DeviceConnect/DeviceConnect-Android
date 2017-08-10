/*
 EventBroker.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.event;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.manager.DConnectLocalOAuth;
import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.request.DiscoveryDeviceRequest;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * イベントブローカー.
 *
 * @author NTT DOCOMO, INC.
 */
public class EventBroker {

    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    private final EventSessionTable mTable;

    private final DConnectMessageService mContext;

    private DConnectLocalOAuth mLocalOAuth;

    private DevicePluginManager mPluginManager;

    private RegistrationListener mListener;

    public EventBroker(final DConnectMessageService context,
                       final EventSessionTable table,
                       final DConnectLocalOAuth localOAuth,
                       final DevicePluginManager pluginManager) {
        mTable = table;
        mContext = context;
        mLocalOAuth = localOAuth;
        mPluginManager = pluginManager;
    }

    public void setRegistrationListener(final RegistrationListener listener) {
        mListener = listener;
    }

    public void removeEventSession(final String receiverId) {
        mTable.removeForReceiverId(receiverId);
    }

    public void onRequest(final Intent request, final DevicePlugin dest) {
        String serviceId = DConnectProfile.getServiceID(request);
        String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        if (serviceId == null) {
            return;
        }
        String accessToken = getAccessToken(origin, serviceId);
        if (accessToken != null) {
            request.putExtra(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        } else {
            request.removeExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        }

        if (isRegistrationRequest(request)) {
            onRegistrationRequest(request, dest);
        } else if (isUnregistrationRequest(request)) {
            onUnregistrationRequest(request, dest);
        }
    }

    private void onRegistrationRequest(final Intent request, final DevicePlugin dest) {
        EventProtocol protocol = EventProtocol.getInstance(mContext, request);
        if (protocol == null) {
            mLogger.warning("Failed to identify a event receiver.");
            return;
        }
        protocol.addSession(mTable, request, dest);

        if (mListener != null) {
            mListener.onPutEventSession(request, dest);
        }
    }

    private void onUnregistrationRequest(final Intent request, final DevicePlugin dest) {
        EventProtocol protocol = EventProtocol.getInstance(mContext, request);
        if (protocol == null) {
            mLogger.warning("Failed to identify a event receiver.");
            return;
        }
        protocol.removeSession(mTable, request, dest);

        if (mListener != null) {
            mListener.onDeleteEventSession(request, dest);
        }
    }

    private String getAccessToken(final String origin, final String serviceId) {
        DConnectLocalOAuth.OAuthData oauth = mLocalOAuth.getOAuthData(origin, serviceId);
        if (oauth != null) {
            return mLocalOAuth.getAccessToken(oauth.getId());
        }
        return null;
    }

    public void updateAccessTokenForPlugin(final String pluginId, final String newAccessToken) {
        mTable.updateAccessTokenForPlugin(pluginId, newAccessToken);
    }

    public void removeSessionForPlugin(final String pluginId) {
        mTable.removeForPlugin(pluginId);
    }

    public void onEvent(final Intent event) {
        if (isServiceChangeEvent(event)) {
            onServiceChangeEvent(event);
            return;
        }

        String pluginAccessToken = event.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        String serviceId = DConnectProfile.getServiceID(event);
        String profileName = DConnectProfile.getProfile(event);
        String interfaceName = DConnectProfile.getInterface(event);
        String attributeName = DConnectProfile.getAttribute(event);

        EventSession targetSession = null;
        if (pluginAccessToken != null) {
            for (EventSession session : mTable.getAll()) {
                if (isSameNameCaseSensitive(pluginAccessToken, session.getAccessToken()) &&
                    isSameNameCaseSensitive(serviceId, session.getServiceId()) &&
                    isSameName(profileName, session.getProfileName()) &&
                    isSameName(interfaceName, session.getInterfaceName()) &&
                    isSameName(attributeName, session.getAttributeName())) {
                    targetSession = session;
                    break;
                }
            }
        } else {
            // 旧バージョンのイベントAPIとの互換性保持
            String sessionKey = DConnectProfile.getSessionKey(event);
            if (sessionKey != null) {
                sessionKey = trimReceiverName(sessionKey);
                String pluginId = EventProtocol.convertSessionKey2PluginId(sessionKey);
                String receiverId = EventProtocol.convertSessionKey2Key(sessionKey);
                for (EventSession session : mTable.getAll()) {
                    if (isSameNameCaseSensitive(pluginId, session.getPluginId()) &&
                        isSameNameCaseSensitive(receiverId, session.getReceiverId()) &&
                        isSameNameCaseSensitive(serviceId, session.getServiceId()) &&
                        isSameName(profileName, session.getProfileName()) &&
                        isSameName(interfaceName, session.getInterfaceName()) &&
                        isSameName(attributeName, session.getAttributeName())) {
                        targetSession = session;
                        break;
                    }
                }
            }
        }
        if (targetSession != null) {
            try {
                DevicePlugin plugin = mPluginManager.getDevicePlugin(targetSession.getPluginId());
                if (plugin != null) {
                    event.putExtra(IntentDConnectMessage.EXTRA_SESSION_KEY, targetSession.getReceiverId());
                    event.putExtra(DConnectMessage.EXTRA_SERVICE_ID, mPluginManager.appendServiceId(plugin, serviceId));
                    targetSession.sendEvent(event);
                } else {
                    mLogger.warning("onEvent: Plugin is not found: id = " + targetSession.getPluginId());
                }
            } catch (IOException e) {
                error("Failed to send event.");
            }
        }
    }

    private String trimReceiverName(final String sessionKey) {
        int index = sessionKey.lastIndexOf(DConnectMessageService.SEPARATOR_SESSION);
        if (index == -1) {
            // HTTP経由でイベントを登録した場合
            return sessionKey;
        }
        // Intent経由でイベントを登録した場合
        return sessionKey.substring(0, index);
    }

    private boolean isServiceChangeEvent(final Intent event) {
        String profileName = DConnectProfile.getProfile(event);
        String attributeName = DConnectProfile.getAttribute(event);
        // MEMO パスの大文字小文字を無視
        return ServiceDiscoveryProfile.PROFILE_NAME.equalsIgnoreCase(profileName)
            && ServiceDiscoveryProfile.ATTRIBUTE_ON_SERVICE_CHANGE.equalsIgnoreCase(attributeName);
    }

    private void onServiceChangeEvent(final Intent event) {
        DevicePlugin plugin = findPluginForServiceChange(event);
        if (plugin == null) {
            warn("onServiceChangeEvent: plugin is not found");
            return;
        }

        // network service discoveryの場合には、networkServiceのオブジェクトの中にデータが含まれる
        Bundle service = event.getParcelableExtra(ServiceDiscoveryProfile.PARAM_NETWORK_SERVICE);
        String id = service.getString(ServiceDiscoveryProfile.PARAM_ID);

        // サービスIDを変更
        replaceServiceId(event, plugin);

        // 送信先のセッションを取得
        List<Event> evts = EventManager.INSTANCE.getEventList(
            ServiceDiscoveryProfile.PROFILE_NAME,
            ServiceDiscoveryProfile.ATTRIBUTE_ON_SERVICE_CHANGE);
        for (int i = 0; i < evts.size(); i++) {
            Event evt = evts.get(i);
            mContext.sendEvent(evt.getReceiverName(), event);
        }
    }

    private DevicePlugin findPluginForServiceChange(final Intent event) {
        String pluginAccessToken = DConnectProfile.getAccessToken(event);
        if (pluginAccessToken != null) {
            return mPluginManager.getDevicePlugin(pluginAccessToken);
        } else {
            String sessionKey = DConnectProfile.getSessionKey(event);
            if (sessionKey != null) {
                String pluginId = EventProtocol.convertSessionKey2PluginId(sessionKey);
                return mPluginManager.getDevicePlugin(pluginId);
            }
        }
        return null;
    }

    /**
     * デバイスプラグインのクライアントを作成する.
     * @param plugin クライアントを作成するデバイスプラグイン
     * @param serviceId サービスID
     * @param event 送信するイベント
     */
    private void createClientOfDevicePlugin(final DevicePlugin plugin, final String serviceId, final Intent event) {
        Intent intent = new Intent(IntentDConnectMessage.ACTION_GET);
        intent.setComponent(plugin.getComponentName());
        intent.putExtra(DConnectMessage.EXTRA_PROFILE,
            ServiceDiscoveryProfileConstants.PROFILE_NAME);
        intent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
        intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, mContext.getPackageName());

        DiscoveryDeviceRequest request = new DiscoveryDeviceRequest();
        request.setContext(mContext);
        request.setLocalOAuth(mLocalOAuth);
        request.setUseAccessToken(true);
        request.setRequireOrigin(true);
        request.setDestination(plugin);
        request.setRequest(intent);
        request.setEvent(event);
        request.setDevicePluginManager(mPluginManager);
        mContext.addRequest(request);
    }

    /**
     * イベント用メッセージのサービスIDを置換する.
     * <br>
     *
     * デバイスプラグインから送られてくるサービスIDは、デバイスプラグインの中でIDになっている。
     * dConnect ManagerでデバイスプラグインのIDをサービスIDに付加することでDNSっぽい動きを実現する。
     *
     * @param event イベントメッセージ用Intent
     * @param plugin 送信元のデバイスプラグイン
     */
    private void replaceServiceId(final Intent event, final DevicePlugin plugin) {
        String serviceId = event.getStringExtra(IntentDConnectMessage.EXTRA_SERVICE_ID);
        event.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID,
            mPluginManager.appendServiceId(plugin, serviceId));
    }

    private boolean isSameName(final String a, final  String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null) {
            return a.equalsIgnoreCase(b);
        } else {
            return b.equalsIgnoreCase(a);
        }
    }

    private boolean isSameNameCaseSensitive(final String a, final  String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null) {
            return a.equals(b);
        } else {
            return b.equals(a);
        }
    }

    private boolean isRegistrationRequest(final Intent request) {
        String action = request.getAction();
        return IntentDConnectMessage.ACTION_PUT.equals(action);
    }

    private boolean isUnregistrationRequest(final Intent request) {
        String action = request.getAction();
        return IntentDConnectMessage.ACTION_DELETE.equals(action);
    }

    private void warn(final String message) {
        mLogger.warning(message);
    }

    private void error(final String message) {
        mLogger.severe(message);
    }

    public interface RegistrationListener {
        void onPutEventSession(final Intent request, final DevicePlugin plugin);
        void onDeleteEventSession(final Intent request, final DevicePlugin plugin);
    }
}
