package org.deviceconnect.android.manager.event;


import android.content.Intent;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.util.VersionName;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.io.IOException;
import java.util.logging.Logger;

public class EventHandler {

    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    private final EventSessionTable mTable;

    private final DConnectService mContext;

    private final KeepAliveManager mKeepAliveManager;

    public EventHandler(final DConnectService context) {
        mTable = new EventSessionTable();
        mContext = context;
        mKeepAliveManager = new KeepAliveManager(context, mTable);
    }

    public void onRequest(final Intent request, final DevicePlugin dest) {
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

        if (isSupportedKeepAlive(dest)) {
            mKeepAliveManager.setManagementTable(dest);
        }
    }

    private void onUnregistrationRequest(final Intent request, final DevicePlugin dest) {
        EventProtocol protocol = EventProtocol.getInstance(mContext, request);
        if (protocol == null) {
            mLogger.warning("Failed to identify a event receiver.");
            return;
        }
        protocol.removeSession(mTable, request, dest);

        if (isSupportedKeepAlive(dest)) {
            mKeepAliveManager.removeManagementTable(dest);
        }
    }

    private boolean isSupportedKeepAlive(final DevicePlugin plugin) {
        VersionName version = plugin.getPluginSdkVersionName();
        VersionName match = VersionName.parse("1.1.0");
        return !(version.compareTo(match) == -1);
    }

    public void onEvent(final Intent event) {
        String pluginAccessToken = event.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        String serviceId = DConnectProfile.getServiceID(event);
        String profileName = DConnectProfile.getProfile(event);
        String interfaceName = DConnectProfile.getInterface(event);
        String attributeName = DConnectProfile.getAttribute(event);
        if (pluginAccessToken != null) {
            for (EventSession session : mTable.getAll()) {
                if (isSameName(pluginAccessToken, session.getAccessToken()) &&
                    isSameName(serviceId, session.getServiceId()) &&
                    isSameName(profileName, session.getProfileName()) &&
                    isSameName(interfaceName, session.getInterfaceName()) &&
                    isSameName(attributeName, session.getAttributeName())) {
                    try {
                        event.putExtra(IntentDConnectMessage.EXTRA_SESSION_KEY, session.getReceiverId());
                        session.sendEvent(event);
                    } catch (IOException e) {
                        error("Failed to send event.");
                    }
                    return;
                }
            }
        } else {
            String sessionKey = DConnectProfile.getSessionKey(event);
            if (sessionKey != null) {
                String pluginId = EventProtocol.convertSessionKey2PluginId(sessionKey);
                String receiverId = EventProtocol.convertSessionKey2Key(sessionKey);
                for (EventSession session : mTable.getAll()) {
                    if (isSameName(pluginId, session.getPluginId()) &&
                        isSameName(receiverId, session.getReceiverId()) &&
                        isSameName(serviceId, session.getServiceId()) &&
                        isSameName(profileName, session.getProfileName()) &&
                        isSameName(interfaceName, session.getInterfaceName()) &&
                        isSameName(attributeName, session.getAttributeName())) {
                        try {
                            event.putExtra(IntentDConnectMessage.EXTRA_SESSION_KEY, session.getReceiverId());
                            session.sendEvent(event);
                        } catch (IOException e) {
                            error("Failed to send event.");
                        }
                        return;
                    }
                }
            }
        }
    }

    public void enableKeepAlive() {
        mKeepAliveManager.enableKeepAlive();
    }

    public void disableKeepAlive() {
        mKeepAliveManager.disableKeepAlive();
    }

    public void onKeepAliveCommand(final Intent intent) {
        String status = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEEPALIVE_STATUS);
        if (status.equals("RESPONSE")) {
            String serviceId = intent.getStringExtra("serviceId");
            if (serviceId != null) {
                KeepAlive keepAlive = mKeepAliveManager.getKeepAlive(serviceId);
                if (keepAlive != null) {
                    keepAlive.setResponseFlag();
                }
            }
        } else if (status.equals("DISCONNECT")) {
            String sessionKey = intent.getStringExtra(IntentDConnectMessage.EXTRA_SESSION_KEY);
            if (sessionKey != null) {
                mContext.sendDisconnectWebSocket(sessionKey);
            }
        }
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
}
