package org.deviceconnect.android.manager.event;


import android.content.Intent;

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.io.IOException;
import java.util.logging.Logger;

public class EventHandler {

    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    private final EventSessionTable mTable;

    private final DConnectMessageService mContext;

    public EventHandler(final DConnectMessageService context) {
        mTable = new EventSessionTable();
        mContext = context;
    }

    public void onRequest(final Intent request, final DevicePlugin dest) {
        if (isRegistrationRequest(request)) {
            EventProtocol protocol = EventProtocol.getInstance(mContext, request);
            if (protocol == null) {
                mLogger.warning("Failed to identify a event receiver.");
                return;
            }
            protocol.addSession(mTable, request, dest);
        } else if (isUnregistrationRequest(request)) {
            EventProtocol protocol = EventProtocol.getInstance(mContext, request);
            if (protocol == null) {
                mLogger.warning("Failed to identify a event receiver.");
                return;
            }
            protocol.removeSession(mTable, request, dest);
        }
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
