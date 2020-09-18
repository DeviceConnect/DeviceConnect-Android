/*
 EventBroker.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.event;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.manager.core.DConnectConst;
import org.deviceconnect.android.manager.core.DConnectLocalOAuth;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

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

    private final DConnectLocalOAuth mLocalOAuth;

    private final DevicePluginManager mPluginManager;

    private EventProtocol mProtocol;

    private RegistrationListener mListener;

    /**
     * コンストラクタ.
     *
     * @param settings Device Connec の設定を保持するクラス
     * @param table イベントセッションを保持するクラス
     * @param localOAuth 認可処理を行うクラス
     * @param pluginManager プラグイン管理クラス
     * @param factory イベントセッションファクトリー
     */
    public EventBroker(final DConnectSettings settings, final EventSessionTable table,
                       final DConnectLocalOAuth localOAuth, final DevicePluginManager pluginManager,
                       final AbstractEventSessionFactory factory) {
        mTable = table;
        mLocalOAuth = localOAuth;
        mPluginManager = pluginManager;
        mProtocol = new EventProtocol(settings, factory);
    }

    /**
     * リスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setRegistrationListener(final RegistrationListener listener) {
        mListener = listener;
    }

    /**
     * 指定されたレシーバーIDのセッションを削除します.
     *
     * @param receiverId レシーバーID
     */
    public void removeEventSession(final String receiverId) {
        mTable.removeForReceiverId(receiverId);
    }

    /**
     * 指定されたリクエストからイベントセッションの登録・解除の処理を行います.
     *
     * @param request リクエスト
     * @param dest 送信先のプラグイン
     */
    public void parseEventSession(final Intent request, final DevicePlugin dest) {
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
            registerRequest(request, dest);
        } else if (isUnregistrationRequest(request)) {
            unregisterRequest(request, dest);
        }
    }

    /**
     * 指定されたリクエストからイベントセッションの登録・解除の処理を行います.
     * このメソッドでは Device Connect Manager 自身のサポートするイベント API へのリクエストを処理します.
     *
     * @param request リクエスト
     */
    public void parseEventSessionForSelf(final Intent request) {
        if (isRegistrationRequest(request)) {
            registerRequest(request, null);
        } else if (isUnregistrationRequest(request)) {
            unregisterRequest(request, null);
        }
    }

    /**
     * セッションテーブルのアクセストークンを更新します.
     *
     * @param pluginId プラグインID
     * @param newAccessToken アクセストークン
     */
    public void updateAccessTokenForPlugin(final String pluginId, final String newAccessToken) {
        mTable.updateAccessTokenForPlugin(pluginId, newAccessToken);
    }

    /**
     * 指定されたプラグインIDのセッションを全て削除します.
     *
     * @param pluginId プラグインID
     */
    public void removeSessionForPlugin(final String pluginId) {
        mTable.removeForPlugin(pluginId);
    }

    /**
     * プラグインからのイベントを受け取り処理を行います.
     *
     * @param event イベント
     */
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

    /**
     * プラグインからのイベントを受け取り処理を行います.
     *  Device Connect Manager 自身のイベントを処理します.
     *
     * @param event イベント
     */
    public void onEventForSelf(final Intent event) {
        String profileName = DConnectProfile.getProfile(event);
        String interfaceName = DConnectProfile.getInterface(event);
        String attributeName = DConnectProfile.getAttribute(event);

        EventSession targetSession = null;
        for (EventSession session : mTable.getAll()) {
            if (isSameName(profileName, session.getProfileName()) &&
                isSameName(interfaceName, session.getInterfaceName()) &&
                isSameName(attributeName, session.getAttributeName())) {
                targetSession = session;
                break;
            }
        }

        if (targetSession != null) {
            try {
                event.putExtra(IntentDConnectMessage.EXTRA_SESSION_KEY, targetSession.getReceiverId());
                targetSession.sendEvent(event);
            } catch (IOException e) {
                error("Failed to send event.");
            }
        }
    }

    /**
     * 登録イベントを処理します.
     *
     * @param request リクエスト
     * @param dest 送信先のプラグイン. Device Connect Manager 自身のイベントの場合は <code>null</code>
     */
    private void registerRequest(final Intent request, final DevicePlugin dest) {
        mProtocol.addSession(mTable, request, dest);
        if (mListener != null) {
            mListener.onPutEventSession(request, dest);
        }
    }

    /**
     * 解除イベントを処理します.
     *
     * @param request リクエスト
     * @param dest 送信先のプラグイン. Device Connect Manager 自身のイベントの場合は <code>null</code>
     */
    private void unregisterRequest(final Intent request, final DevicePlugin dest) {
        mProtocol.removeSession(mTable, request, dest);
        if (mListener != null) {
            mListener.onDeleteEventSession(request, dest);
        }
    }

    /**
     * 指定されたサービスのアクセストークンを取得します.
     *
     * @param origin オリジン
     * @param serviceId サービスID
     * @return アクセストークン
     */
    private String getAccessToken(final String origin, final String serviceId) {
        DConnectLocalOAuth.OAuthData oauth = mLocalOAuth.getOAuthData(origin, serviceId);
        if (oauth != null) {
            return mLocalOAuth.getAccessToken(oauth.getId());
        }
        return null;
    }

    /**
     * セッションキーからレシーバー名を切り抜きます.
     *
     * @param sessionKey セッションキー
     * @return レシーバー名
     */
    private String trimReceiverName(final String sessionKey) {
        int index = sessionKey.lastIndexOf(DConnectConst.SEPARATOR_SESSION);
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
//            mContext.sendEvent(evt.getReceiverName(), event);
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

    /**
     * 大文字小文字を無視して文字列が一致するか確認します.
     * @param a
     * @param b
     * @return
     */
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

    /**
     * 大文字小文字を区別して文字列が一致するか確認します.
     * @param a
     * @param b
     * @return
     */
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

    /**
     * 指定されたリクエストがイベント登録のリクエストか確認します.
     * <p>
     * リクエストの action が PUT の場合にはイベント登録とする。
     * </p>
     * @param request リクエスト
     * @return イベント登録のリクエストの場合にはtrue、それ以外はfalse
     */
    private boolean isRegistrationRequest(final Intent request) {
        return IntentDConnectMessage.ACTION_PUT.equals(request.getAction());
    }

    /**
     * 指定されたリクエストがイベント解除のリクエストか確認します.
     * <p>
     * リクエストの action が DELETE の場合にはイベント解除とする。
     * </p>
     * @param request リクエスト
     * @return イベント解除のリクエストの場合にはtrue、それ以外はfalse
     */
    private boolean isUnregistrationRequest(final Intent request) {
        return IntentDConnectMessage.ACTION_DELETE.equals(request.getAction());
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
