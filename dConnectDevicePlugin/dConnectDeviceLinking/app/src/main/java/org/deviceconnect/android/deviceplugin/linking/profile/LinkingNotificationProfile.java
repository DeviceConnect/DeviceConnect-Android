/*
 LinkingNotificationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingNotification;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.Random;

public class LinkingNotificationProfile extends NotificationProfile {

    private static final int RANDOM_SEED = 1000000;
    private final Random mRandom = new Random();

    @Override
    protected boolean onPostNotify(Intent request, Intent response, String serviceId, NotificationType type, Direction dir, String lang, String body, String tag, byte[] iconData) {
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
                MessageUtils.setInvalidRequestParameterError(response,
                        "type is invalid.");
                return true;
            default:
                break;
        }
        String detail = body == null ? "通知が来ています。" : body;
        LinkingManager manager = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        manager.sendNotification(device, new LinkingNotification(title, detail));
        int notifyId = mRandom.nextInt(RANDOM_SEED);
        response.putExtra(NotificationProfile.PARAM_NOTIFICATION_ID, notifyId);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private LinkingDevice getDevice(String serviceId, Intent response) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return null;
        }
        LinkingDevice device = LinkingUtil.getLinkingDevice(getContext(), serviceId);
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

}
