package org.deviceconnect.android.manager.event;


import android.content.ComponentName;
import android.content.Intent;

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.VersionName;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.UUID;

abstract class EventProtocol {

    private static final VersionName V100 = VersionName.parse("1.0.0");

    static EventProtocol getInstance(final DConnectMessageService context,
                                     final Intent request) {
        final String appType = request.getStringExtra(DConnectService.EXTRA_INNER_TYPE);
        if (DConnectService.INNER_TYPE_HTTP.equals(appType)) {
            return new EventProtocol(context) {
                @Override
                protected EventSession createSession(final Intent request,
                                                     final String receiverId,
                                                     final String pluginId) {
                    final String accessToken = DConnectProfile.getAccessToken(request);
                    final String serviceId = DConnectProfile.getServiceID(request);
                    final String profileName = DConnectProfile.getProfile(request);
                    final String interfaceName = DConnectProfile.getInterface(request);
                    final String attributeName = DConnectProfile.getAttribute(request);

                    WebSocketEventSession session = new WebSocketEventSession();
                    session.setAccessToken(accessToken);
                    session.setReceiverId(receiverId);
                    session.setServiceId(serviceId);
                    session.setPluginId(pluginId);
                    session.setProfileName(profileName);
                    session.setInterfaceName(interfaceName);
                    session.setAttributeName(attributeName);
                    session.setContext(context);
                    return session;
                }
            };
        } else {
            return new EventProtocol(context) {
                @Override
                protected EventSession createSession(final Intent request,
                                                     final String receiverId,
                                                     final String pluginId) {
                    final String accessToken = DConnectProfile.getAccessToken(request);
                    final String serviceId = DConnectProfile.getServiceID(request);
                    final String profileName = DConnectProfile.getProfile(request);
                    final String interfaceName = DConnectProfile.getInterface(request);
                    final String attributeName = DConnectProfile.getAttribute(request);

                    final ComponentName receiver = request.getParcelableExtra(DConnectMessage.EXTRA_RECEIVER);
                    IntentEventSession session = new IntentEventSession();
                    session.setAccessToken(accessToken);
                    session.setReceiverId(receiverId);
                    session.setServiceId(serviceId);
                    session.setPluginId(pluginId);
                    session.setProfileName(profileName);
                    session.setInterfaceName(interfaceName);
                    session.setAttributeName(attributeName);
                    session.setContext(context);
                    session.setBroadcastReceiver(receiver);
                    return session;
                }
            };
        }
    }

    private DConnectMessageService mMessageService;

    public EventProtocol(final DConnectMessageService messageService) {
        mMessageService = messageService;
    }

    boolean removeSession(final EventSessionTable table, final Intent request, final DevicePlugin plugin) {
        String receiverId = createReceiverId(request);
        if (receiverId == null) {
            return false;
        }
        EventSession query = createSession(request, receiverId, plugin.getServiceId());
        for (EventSession session : table.getAll()) {
            if (isSameSession(query, session)) {
                table.remove(session);

                if (plugin.getPluginSdkVersionName().compareTo(V100) == 0) {
                    DConnectProfile.setSessionKey(request, createSessionKeyForPlugin(session));
                }
                return true;
            }
        }
        return false;
    }

    boolean addSession(final EventSessionTable table, final Intent request, final DevicePlugin plugin) {
        String accessToken = request.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        if (accessToken == null) {
            DConnectProfile.setAccessToken(request, UUID.randomUUID().toString());
        }

        String receiverId = createReceiverId(request);
        if (receiverId == null) {
            return false;
        }
        EventSession session = createSession(request, receiverId, plugin.getServiceId());
        table.add(session);

        if (plugin.getPluginSdkVersionName().compareTo(V100) == 0) {
            DConnectProfile.setSessionKey(request, createSessionKeyForPlugin(session));
        }
        return true;
    }

    private String createSessionKeyForPlugin(final EventSession session) {
        StringBuilder result = new StringBuilder();
        result.append(session.getReceiverId())
            .append(DConnectMessageService.SEPARATOR)
            .append(session.getPluginId());
        if (session instanceof IntentEventSession) {
            result.append(DConnectMessageService.SEPARATOR_SESSION)
                .append(((IntentEventSession) session).getBroadcastReceiver());
        }
        return result.toString();
    }

    protected abstract EventSession createSession(final Intent request,
                                                  final String receiverId,
                                                  final String pluginId);

    private String createReceiverId(final Intent request) {
        String origin;
        if (getOrigin(request) == null && !mMessageService.requiresOrigin()) {
            origin = DConnectService.ANONYMOUS_ORIGIN;
        } else {
            origin = getOrigin(request);
        }

        final String receiverId;
        final String sessionKey = DConnectProfile.getSessionKey(request);
        if (sessionKey != null) {
            receiverId = convertSessionKey2Key(sessionKey);
        } else {
            receiverId = md5(origin);
        }
        return receiverId;
    }

    private static String getOrigin(Intent intent) {
        return intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
    }

    private static String md5(final String s) {
        try {
            return DConnectUtil.toMD5(s);
        } catch (Exception e) {
            // NOP.
        }
        return null;
    }

    public boolean isSameSession(final EventSession a, final EventSession b) {
        return isSame(a.getReceiverId(), b.getReceiverId())
            && isSame(a.getServiceId(), b.getServiceId())
            && isSame(a.getPluginId(), b.getPluginId())
            && isSameIgnoreCase(a.getProfileName(), b.getProfileName()) // MEMO パスの大文字小文字を無視
            && isSameIgnoreCase(a.getInterfaceName(), b.getInterfaceName())
            && isSameIgnoreCase(a.getAttributeName(), b.getAttributeName());
    }

    private boolean isSame(final String a, final  String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null) {
            return a.equals(b);
        } else {
            return b.equals(a);
        }
    }

    private boolean isSameIgnoreCase(final String a, final  String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null) {
            return a.equalsIgnoreCase(b);
        } else {
            return b.equalsIgnoreCase(a);
        }
    }

    /**
     * セッションキーからプラグインIDに変換する.
     *
     * @param sessionKey セッションキー
     * @return プラグインID
     */
    static String convertSessionKey2PluginId(final String sessionKey) {
        int index = sessionKey.lastIndexOf(DConnectService.SEPARATOR);
        if (index > 0) {
            return sessionKey.substring(index + 1);
        }
        return sessionKey;
    }

    /**
     * デバイスプラグインからのセッションキーから前半分のクライアントのセッションキーに変換する.
     * @param sessionKey セッションキー
     * @return クライアント用のセッションキー
     */
    static String convertSessionKey2Key(final String sessionKey) {
        int index = sessionKey.lastIndexOf(DConnectService.SEPARATOR);
        if (index > 0) {
            return sessionKey.substring(0, index);
        }
        return sessionKey;
    }
}
