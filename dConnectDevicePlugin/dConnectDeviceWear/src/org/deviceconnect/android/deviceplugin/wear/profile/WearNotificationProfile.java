/*
 WearNotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import java.util.Random;

import org.deviceconnect.android.deviceplugin.wear.R;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Notification Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearNotificationProfile extends NotificationProfile {

    /**
     * ランダムを生成するクラス.
     */
    private Random mRandom = new Random(System.currentTimeMillis());

    @Override
    protected boolean onPostNotify(final Intent request, final Intent response, final String serviceId,
            final NotificationType type, final Direction dir, final String lang, final String body, final String tag,
            final byte[] iconData) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (type == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            Bitmap myBitmap = null;
            Resources myRes = getContext().getResources();
            NotificationCompat.Builder myNotificationBuilder = null;
            NotificationManagerCompat myNotificationManager = null;
            int myNotificationId = mRandom.nextInt(Integer.MAX_VALUE);

            Intent intent = new Intent(getContext(),
                    org.deviceconnect.android.deviceplugin.wear.WearDeviceService.class);
            intent.setAction(WearConst.DEVICE_TO_WEAR_NOTIFICATION_OPEN);
            intent.putExtra(WearConst.PARAM_DEVICEID, serviceId);
            intent.putExtra(WearConst.PARAM_NOTIFICATIONID, myNotificationId);
            PendingIntent pendingIntent = PendingIntent.getService(getContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            switch (type) {
            case PHONE:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_00);
                myNotificationBuilder = new NotificationCompat.Builder(this.getContext())
                        .setSmallIcon(R.drawable.notification_00).setContentTitle("Phone").setContentText(body)
                        .setContentIntent(pendingIntent).setLargeIcon(myBitmap);
                break;
            case MAIL:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_01);
                myNotificationBuilder = new NotificationCompat.Builder(this.getContext())
                        .setSmallIcon(R.drawable.notification_01).setContentTitle("Mail").setContentText(body)
                        .setContentIntent(pendingIntent).setLargeIcon(myBitmap);
                break;
            case SMS:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_02);
                myNotificationBuilder = new NotificationCompat.Builder(this.getContext())
                        .setSmallIcon(R.drawable.notification_02).setContentTitle("SMS").setContentText(body)
                        .setContentIntent(pendingIntent).setLargeIcon(myBitmap);
                break;
            case EVENT:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_03);
                myNotificationBuilder = new NotificationCompat.Builder(this.getContext())
                        .setSmallIcon(R.drawable.notification_03).setContentTitle("Event").setContentText(body)
                        .setContentIntent(pendingIntent).setLargeIcon(myBitmap);
                break;
            case UNKNOWN:
            default:
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }
            // Send Notification.
            myNotificationManager = NotificationManagerCompat.from(this.getContext());
            myNotificationManager.notify(myNotificationId, myNotificationBuilder.build());
            response.putExtra(NotificationProfile.PARAM_NOTIFICATION_ID, myNotificationId);
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteNotify(final Intent request, final Intent response, final String serviceId,
            final String notificationId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (notificationId == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            NotificationManager manager = (NotificationManager) getContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            try {
                manager.cancel(Integer.parseInt(notificationId));
                setResult(response, DConnectMessage.RESULT_OK);
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "notificationId is invalid.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnClick(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            switch (error) {
            case NONE:
                setResult(response, DConnectMessage.RESULT_OK);
                break;
            case FAILED:
                MessageUtils.setUnknownError(response, "Do not register event.");
                break;
            case INVALID_PARAMETER:
                MessageUtils.setInvalidRequestParameterError(response);
                break;
            case NOT_FOUND:
                MessageUtils.setUnknownError(response, "Event not found.");
                break;
            default:
                MessageUtils.setUnknownError(response);
                break;
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnClick(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            switch (error) {
            case NONE:
                setResult(response, DConnectMessage.RESULT_OK);
                break;
            case FAILED:
                MessageUtils.setUnknownError(response, "Do not unregister event.");
                break;
            case INVALID_PARAMETER:
                MessageUtils.setInvalidRequestParameterError(response);
                break;
            case NOT_FOUND:
                MessageUtils.setUnknownError(response, "Event not found.");
                break;
            default:
                MessageUtils.setUnknownError(response);
                break;
            }
        }
        return true;
    }
}
