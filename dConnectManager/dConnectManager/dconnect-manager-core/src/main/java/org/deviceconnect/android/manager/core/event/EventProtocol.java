/*
 EventProtocol.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.event;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.manager.core.DConnectConst;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.util.VersionName;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * イベント登録手順.
 *
 * @author NTT DOCOMO, INC.
 */
public class EventProtocol {
    /**
     * SDKバージョン 1.0.0 を定義.
     */
    private static final VersionName V100 = VersionName.parse("1.0.0");

    /**
     * SDKバージョン 1.1.0 を定義.
     */
    private static final VersionName V110 = VersionName.parse("1.1.0");

    private DConnectSettings mSettings;
    private AbstractEventSessionFactory mEventSessionFactory;

    EventProtocol(final DConnectSettings settings, final AbstractEventSessionFactory factory) {
        mSettings = settings;
        mEventSessionFactory = factory;
    }

    /**
     * セッションを追加します.
     *
     * @param table セッションを保持するテーブル
     * @param request 追加リクエスト
     * @param plugin プラグイン. Device Connect Manager 自身のイベントの場合は <code>null</code>
     * @return 追加に成功した場合はtrue、それ以外はfalse
     */
    boolean addSession(final EventSessionTable table, final Intent request, final DevicePlugin plugin) {
        String receiverId = createReceiverId(request, mSettings.requireOrigin());
        if (receiverId == null) {
            return false;
        }
        String serviceId = null;
        String pluginId = null;
        if (plugin != null) {
            serviceId = DevicePluginManager.splitServiceId(plugin, DConnectProfile.getServiceID(request));
            pluginId = plugin.getPluginId();
        }

        String accessToken = request.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        if (accessToken == null) {
            DConnectProfile.setAccessToken(request, receiverId);
        }
        EventSession session = mEventSessionFactory.createSession(request, serviceId, receiverId, pluginId);
        table.add(session);

        if (plugin != null && plugin.getPluginSdkVersionName().compareTo(V100) == 0) {
            DConnectProfile.setSessionKey(request, session.createKey());
        }
        return true;
    }

    /**
     * セッションを削除します.
     *
     * @param table セッションを保持するテーブル
     * @param request 削除リクエスト
     * @param plugin プラグイン
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    boolean removeSession(final EventSessionTable table, final Intent request, final DevicePlugin plugin) {
        String receiverId = createReceiverId(request, mSettings.requireOrigin());
        if (receiverId == null) {
            return false;
        }
        String serviceId = null;
        String pluginId = null;
        if (plugin != null) {
            serviceId = DevicePluginManager.splitServiceId(plugin, DConnectProfile.getServiceID(request));
            pluginId = plugin.getPluginId();
        }

        String accessToken = request.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        if (accessToken == null) {
            DConnectProfile.setAccessToken(request, receiverId);
        }

        EventSession query = mEventSessionFactory.createSession(request, serviceId, receiverId, pluginId);
        for (EventSession session : table.getAll()) {
            if (isSameSession(query, session)) {
                table.remove(session);

                if (plugin != null && plugin.getPluginSdkVersionName().compareTo(V100) == 0) {
                    DConnectProfile.setSessionKey(request, session.createKey());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * リクエストからレシーバーIDを作成します.
     *
     * @param request リクエスト
     * @param requiresOrigin origin要求の有無
     * @return レシーバーID
     */
    private static String createReceiverId(final Intent request, final boolean requiresOrigin) {
        String origin = getOrigin(request, requiresOrigin);
        String receiverId;
        String sessionKey = DConnectProfile.getSessionKey(request);
        if (sessionKey != null) {
            receiverId = sessionKey;
        } else {
            receiverId = origin;
        }
        return receiverId;
    }

    /**
     * リクエストからオリジンを取得します.
     *
     * @param request リクエスト
     * @param requiresOrigin origin要求の有無
     * @return オリジン
     */
    private static String getOrigin(final Intent request, final boolean requiresOrigin) {
        if (getOrigin(request) == null && !requiresOrigin) {
            return DConnectConst.ANONYMOUS_ORIGIN;
        } else {
            return getOrigin(request);
        }
    }

    /**
     * 指定されたセッションが同一か確認します.
     *
     * @param a 確認するセッション1
     * @param b 確認するセッション2
     * @return 同じセッションの場合はtrue、それ以外はfalse
     */
    private static boolean isSameSession(final EventSession a, final EventSession b) {
        return isSame(a.getReceiverId(), b.getReceiverId())
            && isSame(a.getServiceId(), b.getServiceId())
            && isSame(a.getPluginId(), b.getPluginId())
            && isSameIgnoreCase(a.getProfileName(), b.getProfileName()) // MEMO パスの大文字小文字を無視
            && isSameIgnoreCase(a.getInterfaceName(), b.getInterfaceName())
            && isSameIgnoreCase(a.getAttributeName(), b.getAttributeName());
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

    public static Intent createRegistrationRequestForServiceChange(final Context context, final DevicePlugin plugin,
                                                                   final Class<? extends BroadcastReceiver> dConnectBroadcastReceiver) {
        String profileName = ServiceDiscoveryProfile.PROFILE_NAME;
        String attributeName = ServiceDiscoveryProfile.ATTRIBUTE_ON_SERVICE_CHANGE;
        Intent request = createRegistrationRequest(context,
            plugin,
            profileName,
            null,
            attributeName, dConnectBroadcastReceiver);
        if (plugin.getPluginSdkVersionName().compareTo(V110) >= 0) {
            // NOTE: イベントハンドラーがあとでプラグインを特定するための情報
            request.putExtra(DConnectMessage.EXTRA_ACCESS_TOKEN, plugin.getPluginId());
        }
        return request;
    }

    static Intent createRegistrationRequest(final Context context, final DevicePlugin plugin,
                                            final String profileName, final String interfaceName,
                                            final String attributeName, final Class<? extends BroadcastReceiver> dConnectBroadcastReceiver) {
        return createEventRequest(context, IntentDConnectMessage.ACTION_PUT,
            plugin, profileName, interfaceName, attributeName, dConnectBroadcastReceiver);
    }

    static Intent createUnregistrationRequest(final Context context, final DevicePlugin plugin,
                                              final String profileName, final String interfaceName,
                                              final String attributeName, final Class<? extends BroadcastReceiver> dConnectBroadcastReceiver) {
        return createEventRequest(context, IntentDConnectMessage.ACTION_DELETE,
            plugin, profileName, interfaceName, attributeName, dConnectBroadcastReceiver);
    }

    private static String getOrigin(Intent intent) {
        return intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
    }

    private static boolean isSame(final String a, final  String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null) {
            return a.equals(b);
        } else {
            return b.equals(a);
        }
    }

    private static boolean isSameIgnoreCase(final String a, final  String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null) {
            return a.equalsIgnoreCase(b);
        } else {
            return b.equalsIgnoreCase(a);
        }
    }

    private static Intent createEventRequest(final Context context, final String action,
                                             final DevicePlugin plugin, final String profileName,
                                             final String interfaceName, final String attributeName,
                                             final Class<? extends BroadcastReceiver> dConnectBroadcastReceiver) {
        Intent request = new Intent(action);
        request.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        request.setComponent(plugin.getComponentName());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, profileName);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, interfaceName);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, attributeName);
        request.putExtra(DConnectMessage.EXTRA_RECEIVER,
            new ComponentName(context, dConnectBroadcastReceiver));
        if (plugin.getPluginSdkVersionName().compareTo(V100) == 0) {
            request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, plugin.getPluginId());
        }
        return request;
    }
}
