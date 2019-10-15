/*
 LinkingNotificationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.lib.R;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingNotification;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

public class LinkingNotificationProfile extends NotificationProfile {

    public LinkingNotificationProfile() {
        addApi(mPostNotify);
    }


    private final DConnectApi mPostNotify = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            NotificationType type = getType(request);
            String body = getBody(request);
            String title;
            switch (type) {
                case PHONE:
                    title = getContext().getString(R.string.linking_notification_profile_type_phone);
                    break;
                case MAIL:
                    title = getContext().getString(R.string.linking_notification_profile_type_mail);
                    break;
                case SMS:
                    title = getContext().getString(R.string.linking_notification_profile_type_sms);
                    break;
                case EVENT:
                    title = getContext().getString(R.string.linking_notification_profile_type_event);
                    break;
                case UNKNOWN:
                default:
                    MessageUtils.setInvalidRequestParameterError(response, "type is invalid.");
                    return true;
            }

            String detail = body == null ? getContext().getString(R.string.linking_notification_profile_body) : body;
            getLinkingDeviceManager().sendNotification(device, new LinkingNotification(title, detail));
            setNotificationId(response, "0");
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = ((LinkingDeviceService) getService()).getLinkingDevice();

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
