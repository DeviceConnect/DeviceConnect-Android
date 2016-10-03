/*
 SWNotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;

import com.sonyericsson.extras.liveware.aef.notification.Notification;
import com.sonyericsson.extras.liveware.extension.util.notification.NotificationUtil;

import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * SonySWデバイスプラグインの{@link NotificationProfile}実装.
 * @author NTT DOCOMO, INC.
 */
public class SWNotificationProfile extends NotificationProfile {

    private final DConnectApi mPostNotifyApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            NotificationType type = getType(request);
            String body = getBody(request);

            if (NotificationType.UNKNOWN.equals(type) || type == null) {
                MessageUtils.setInvalidRequestParameterError(response, "type is not specified.");
                return true;
            }
            String uri = request.getStringExtra(PARAM_URI);
            long sourceId = NotificationUtil.getSourceId(getContext(),
                SWConstants.EXTENSION_SPECIFIC_ID);
            ContentValues eventValues = new ContentValues();
            eventValues.put(Notification.EventColumns.EVENT_READ_STATUS, false);
            if (body != null) {
                eventValues.put(Notification.EventColumns.DISPLAY_NAME, body);
                eventValues.put(Notification.EventColumns.MESSAGE, body);
            }
            if (uri != null) {
                String decodedUri = Uri.decode(uri);
                eventValues.put(Notification.EventColumns.IMAGE_URI, decodedUri);
            }
            eventValues.put(Notification.EventColumns.PERSONAL, 1);
            eventValues.put(Notification.EventColumns.PUBLISHED_TIME, System.currentTimeMillis());
            eventValues.put(Notification.EventColumns.SOURCE_ID, sourceId);
            Uri addedEvent = NotificationUtil.addEvent(getContext(), eventValues);
            if (addedEvent != null) {
                setResult(response, DConnectMessage.RESULT_OK);
                setNotificationId(response, addedEvent.getLastPathSegment());
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteNotifyApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String notificationId = getNotificationId(request);

            if (notificationId == null || notificationId.equals("")) {
                MessageUtils.setInvalidRequestParameterError(response, "notificationId is not specified.");
                return true;
            }
            Uri event = Uri.withAppendedPath(Notification.Event.URI, notificationId);
            int num = getContext().getContentResolver().delete(event, null, null);
            if (num > 0) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,
                    "No notification event is found to be deleted: " + event);
            }
            return true;
        }
    };

    public SWNotificationProfile() {
        addApi(mPostNotifyApi);
        addApi(mDeleteNotifyApi);
    }

}
