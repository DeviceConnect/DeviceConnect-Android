/*
 LinkingNotificationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingNotification;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.message.DConnectMessage;

public class LinkingNotificationProfile extends NotificationProfile {

    @Override
    protected boolean onPostNotify(final Intent request, final Intent response, final String serviceId,
                                   final NotificationType type, final Direction dir, final String lang,
                                   final String body, final String tag, final byte[] iconData) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        String title = "通知";
        switch (type) {
            case PHONE:
                title = "着信通知";
                break;
            case MAIL:
                title = "メール通知";
                break;
            case SMS:
                title = "SMS通知";
                break;
            case EVENT:
                title = "イベント通知";
                break;
            case UNKNOWN:
            default:
                MessageUtils.setInvalidRequestParameterError(response, "type is invalid.");
                return true;
        }
        String detail = body == null ? "通知が来ています。" : body;
        LinkingDeviceManager manager = getLinkingDeviceManager();
        manager.sendNotification(device, new LinkingNotification(title, detail));
        setNotificationId(response, "0");
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private LinkingDevice getDevice(final String serviceId, final Intent response) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return null;
        }
        LinkingDevice device = getLinkingDeviceManager().findDeviceByBdAddress(serviceId);
        if (device == null) {
            MessageUtils.setIllegalDeviceStateError(response, "device not found");
            return null;
        }
        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }
        return device;
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
