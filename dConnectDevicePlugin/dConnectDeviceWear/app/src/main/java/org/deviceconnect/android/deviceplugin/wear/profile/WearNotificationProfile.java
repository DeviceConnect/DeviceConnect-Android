/*
 WearNotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.deviceconnect.android.deviceplugin.wear.R;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;
import java.util.Random;

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
            Bitmap myBitmap;
            Resources myRes = getContext().getResources();
            NotificationCompat.Builder myNotificationBuilder;
            NotificationManagerCompat myNotificationManager;
            int myNotificationId = mRandom.nextInt(Integer.MAX_VALUE);

            Intent clickIntent = new Intent(getContext(),
                    org.deviceconnect.android.deviceplugin.wear.WearDeviceService.class);
            clickIntent.setAction(WearConst.DEVICE_TO_WEAR_NOTIFICATION_OPEN);
            clickIntent.putExtra(WearConst.PARAM_DEVICEID, serviceId);
            clickIntent.putExtra(WearConst.PARAM_NOTIFICATIONID, myNotificationId);
            PendingIntent clickPendingIntent = PendingIntent.getService(getContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Intent deleteIntent = new Intent(getContext(),
                    org.deviceconnect.android.deviceplugin.wear.WearDeviceService.class);
            deleteIntent.setAction(WearConst.DEVICE_TO_WEAR_NOTIFICATION_CLOSED);
            deleteIntent.putExtra(WearConst.PARAM_DEVICEID, serviceId);
            deleteIntent.putExtra(WearConst.PARAM_NOTIFICATIONID, myNotificationId);
            PendingIntent deletePendingIntent = PendingIntent.getService(getContext(), 0, deleteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            switch (type) {
            case PHONE:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_00);
                myNotificationBuilder = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.notification_00).setContentTitle("Phone").setContentText(body)
                        .setContentIntent(clickPendingIntent).setLargeIcon(myBitmap)
                        .setVibrate(new long[]{500}).setDeleteIntent(deletePendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .extend(new NotificationCompat.WearableExtender());
                break;
            case MAIL:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_01);
                myNotificationBuilder = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.notification_01).setContentTitle("Mail").setContentText(body)
                        .setContentIntent(clickPendingIntent).setLargeIcon(myBitmap)
                        .setVibrate(new long[]{500}).setDeleteIntent(deletePendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .extend(new NotificationCompat.WearableExtender());
                break;
            case SMS:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_02);
                myNotificationBuilder = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.notification_02).setContentTitle("SMS").setContentText(body)
                        .setContentIntent(clickPendingIntent).setLargeIcon(myBitmap)
                        .setVibrate(new long[]{500}).setDeleteIntent(deletePendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .extend(new NotificationCompat.WearableExtender());
                break;
            case EVENT:
                myBitmap = BitmapFactory.decodeResource(myRes, R.drawable.notification_03);
                myNotificationBuilder = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.notification_03).setContentTitle("Event").setContentText(body)
                        .setContentIntent(clickPendingIntent).setLargeIcon(myBitmap)
                        .setVibrate(new long[]{500}).setDeleteIntent(deletePendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .extend(new NotificationCompat.WearableExtender());
                break;
            case UNKNOWN:
            default:
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }

            // Send Notification.
            myNotificationManager = NotificationManagerCompat.from(getContext());
            myNotificationManager.notify(myNotificationId, myNotificationBuilder.build());
            response.putExtra(NotificationProfile.PARAM_NOTIFICATION_ID, myNotificationId);
            setResult(response, IntentDConnectMessage.RESULT_OK);
            
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId, WearNotificationProfile.PROFILE_NAME,
                    null, WearNotificationProfile.ATTRIBUTE_ON_SHOW);
            synchronized (events) {
                for (Event event : events) {
                    Intent msg = EventManager.createEventMessage(event);
                    msg.putExtra(WearNotificationProfile.PARAM_NOTIFICATION_ID, myNotificationId);
                    ((DConnectMessageService) getContext()).sendEvent(msg, event.getAccessToken());
                }
            }
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
    protected boolean onDeleteOnClick(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        return unregisterEvent(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onPutOnClick(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        return registerEvent(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnClose(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        return unregisterEvent(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onPutOnClose(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        return registerEvent(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnShow(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        return unregisterEvent(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onPutOnShow(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
        return registerEvent(request, response, serviceId, sessionKey);
    }

    /**
     * イベントを登録します.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return 同期なのでtrueを返却
     */
    private boolean registerEvent(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
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

    /**
     * イベントを解除します.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return 同期なのでtrueを返却
     */
    private boolean unregisterEvent(final Intent request, final Intent response, final String serviceId, final String sessionKey) {
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
