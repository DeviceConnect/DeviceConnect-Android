/*
 WearNotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import org.deviceconnect.android.deviceplugin.wear.R;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
    private final Random mRandom = new Random(System.currentTimeMillis());

    public WearNotificationProfile() {
        addApi(mPostNotify);
        addApi(mDeleteNotify);
        addApi(mDeleteOnClick);
        addApi(mPutOnClick);
        addApi(mDeleteOnClose);
        addApi(mPutOnClose);
        addApi(mDeleteOnShow);
        addApi(mPutOnShow);
    }

    private final DConnectApi mPostNotify = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final NotificationType type = getType(request);
            final String body = getBody(request);

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

            int iconType = 0;
            String title = "";
            if (type == NotificationType.PHONE) {
                iconType = R.drawable.notification_00_post_lollipop;
                title = "PHONE";
            } else if (type == NotificationType.MAIL) {
                iconType = R.drawable.notification_01_post_lollipop;
                title = "MAIL";
            } else if (type == NotificationType.SMS) {
                iconType = R.drawable.notification_02_post_lollipop;
                title = "SMS";
            } else if (type == NotificationType.EVENT) {
                iconType = R.drawable.notification_03_post_lollipop;
                title = "EVENT";
            } else {
                MessageUtils.setInvalidRequestParameterError(response,
                        "type is invalid.");
                return true;
            }

            String encodeBody = "";
            try {
                if (body != null) {
                    encodeBody = URLDecoder.decode(body, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "body is invalid.");
                return true;
            }


            Notification notification;
            // Get an instance of the NotificationManager service
            NotificationManager mNotification = (NotificationManager) getContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(iconType)
                                .setContentTitle("" + title)
                                .setContentText(encodeBody)
                                .setContentIntent(clickPendingIntent)
                                .setVibrate(new long[]{500})
                                .setDeleteIntent(deletePendingIntent)
                                .extend(new NotificationCompat.WearableExtender());
                notification = notificationBuilder.build();
            } else {
                Notification.Builder notificationBuilder =
                        new Notification.Builder(getContext())
                                .setSmallIcon(Icon.createWithResource(getContext(), iconType))
                                .setContentTitle("" + title)
                                .setContentText(encodeBody)
                                .setContentIntent(clickPendingIntent)
                                .setVibrate(new long[]{500})
                                .setDeleteIntent(deletePendingIntent)
                                .extend(new Notification.WearableExtender());
                ;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String channelId = getContext().getResources().getString(R.string.android_wear_notification_channel_id);
                    NotificationChannel channel = new NotificationChannel(
                            channelId,
                            getContext().getResources().getString(R.string.android_wear_notification_channel_title),
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription(getContext().getResources().getString(R.string.android_wear_notification_channel_desc));
                    mNotification.createNotificationChannel(channel);
                    notificationBuilder.setChannelId(channelId);
                }
                notification = notificationBuilder.build();
            }
            // Build the notification and issues it with notification
            // manager.
            mNotification.notify(myNotificationId, notification);

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
            return true;
        }
    };

    private final DConnectApi mDeleteNotify = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            NotificationManager manager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
            try {
                manager.cancel(Integer.parseInt(getNotificationId(request)));
                setResult(response, DConnectMessage.RESULT_OK);
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response,
                    "notificationId is invalid.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnClick = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLICK;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return unregisterEvent(request, response, getServiceID(request), getSessionKey(request));
        }
    };

    private final DConnectApi mPutOnClick = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLICK;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerEvent(request, response, getServiceID(request), getSessionKey(request));
        }
    };

    private final DConnectApi mDeleteOnClose = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLOSE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return unregisterEvent(request, response, getServiceID(request), getSessionKey(request));
        }
    };

    private final DConnectApi mPutOnClose = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLOSE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerEvent(request, response, getServiceID(request), getSessionKey(request));
        }
    };

    private final DConnectApi mDeleteOnShow = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SHOW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return unregisterEvent(request, response, getServiceID(request), getSessionKey(request));
        }
    };

    private final DConnectApi mPutOnShow = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SHOW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerEvent(request, response, getServiceID(request), getSessionKey(request));
        }
    };

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
