/**
  HostNotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * ホストデバイスプラグイン, Notification プロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostNotificationProfile extends NotificationProfile {

    /** Random Seed. */
    private static final int RANDOM_SEED = 1000000;

    /**
     * Notificationテータスレシーバー.
     */
    private NotificationStatusReceiver mNotificationStatusReceiver;

    /**
     * Notification Flag.
     */
    private static final String ACTON_NOTIFICATION = "org.deviceconnect.android.intent.action.notifiy";

    /** ランダムシード. */
    private final Random mRandom = new Random();

    private final DConnectApi mPostNotifyApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (mNotificationStatusReceiver == null) {
                mNotificationStatusReceiver = new NotificationStatusReceiver();
                getContext().getApplicationContext().registerReceiver(mNotificationStatusReceiver,
                    new IntentFilter(ACTON_NOTIFICATION));
            }
            String serviceId = getServiceID(request);
            NotificationType type = getType(request);
            String body = getBody(request);

            int iconType = 0;
            String title = "";
            if (type == NotificationType.PHONE) {
                iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.notification_00 : R.drawable.notification_00_post_lollipop;
                title = "PHONE";
            } else if (type == NotificationType.MAIL) {
                iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.notification_01 : R.drawable.notification_01_post_lollipop;
                title = "MAIL";
            } else if (type == NotificationType.SMS) {
                iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.notification_02 : R.drawable.notification_02_post_lollipop;
                title = "SMS";
            } else if (type == NotificationType.EVENT) {
                iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.notification_03 : R.drawable.notification_03_post_lollipop;
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

            int notifyId = mRandom.nextInt(RANDOM_SEED);
            if (Build.MODEL.endsWith("M100")) {
                Toast.makeText(getContext(), encodeBody, Toast.LENGTH_SHORT).show();
                response.putExtra(NotificationProfile.PARAM_NOTIFICATION_ID, notifyId);
                setResult(response, IntentDConnectMessage.RESULT_OK);
            } else {
                // Build intent for notification content
                Intent notifyIntent = new Intent(ACTON_NOTIFICATION);
                notifyIntent.putExtra("notificationId", notifyId);
                notifyIntent.putExtra("serviceId", serviceId);

                PendingIntent mPendingIntent = PendingIntent.getBroadcast(getContext(),
                    notifyId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                            .setContentIntent(mPendingIntent);
                    notification = notificationBuilder.build();
                } else {
                    Notification.Builder notificationBuilder =
                        new Notification.Builder(getContext())
                            .setSmallIcon(Icon.createWithResource(getContext(), iconType))
                            .setContentTitle("" + title)
                            .setContentText(encodeBody)
                            .setContentIntent(mPendingIntent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String channelId = getContext().getResources().getString(R.string.host_notification_channel_id);
                        NotificationChannel channel = new NotificationChannel(
                                channelId,
                                getContext().getResources().getString(R.string.host_notification_channel_title),
                                NotificationManager.IMPORTANCE_DEFAULT);
                        channel.setDescription(getContext().getResources().getString(R.string.host_notification_channel_desc));
                        mNotification.createNotificationChannel(channel);
                        notificationBuilder.setChannelId(channelId);
                    }
                    notification = notificationBuilder.build();
                }
                // Build the notification and issues it with notification
                // manager.
                mNotification.notify(notifyId, notification);

                response.putExtra(NotificationProfile.PARAM_NOTIFICATION_ID, notifyId);
                setResult(response, IntentDConnectMessage.RESULT_OK);
            }

            List<Event> events = EventManager.INSTANCE.getEventList(
                serviceId,
                HostNotificationProfile.PROFILE_NAME,
                null,
                HostNotificationProfile.ATTRIBUTE_ON_SHOW);
            HostDeviceService service = (HostDeviceService) getContext();
            synchronized (events) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    setNotificationId(intent, String.valueOf(notifyId));
                    service.sendEvent(intent, event.getAccessToken());
                }
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
            String serviceId = getServiceID(request);
            String notificationId = getNotificationId(request);

            int notifyId = 0;
            try {
                notifyId = Integer.parseInt(notificationId);
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response,
                    "notificationId is invalid.");
                return true;
            }

            NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(
                    Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(notifyId);
            setResult(response, IntentDConnectMessage.RESULT_OK);

            List<Event> events = EventManager.INSTANCE.getEventList(
                serviceId,
                HostNotificationProfile.PROFILE_NAME,
                null,
                HostNotificationProfile.ATTRIBUTE_ON_CLOSE);
            HostDeviceService service = (HostDeviceService) getContext();
            synchronized (events) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    intent.putExtra(HostNotificationProfile.PARAM_NOTIFICATION_ID, notificationId);
                    service.sendEvent(intent, event.getAccessToken());
                }
            }
            return true;
        }
    };

    private final DConnectApi mPutOnClickApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLICK;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mNotificationStatusReceiver = new NotificationStatusReceiver();
            IntentFilter intentFilter = new IntentFilter(ACTON_NOTIFICATION);
            getContext().registerReceiver(mNotificationStatusReceiver, intentFilter);

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnCloseApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLOSE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnShowApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SHOW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnClickApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLICK;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                switch (error) {
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
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnCloseApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CLOSE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                switch (error) {
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
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnShowApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SHOW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                switch (error) {
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
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    public HostNotificationProfile() {
        addApi(mPostNotifyApi);
        addApi(mDeleteNotifyApi);
        addApi(mPutOnClickApi);
        addApi(mPutOnCloseApi);
        addApi(mPutOnShowApi);
        addApi(mDeleteOnClickApi);
        addApi(mDeleteOnCloseApi);
        addApi(mDeleteOnShowApi);
    }

    /**
     * ノーティフィケーション.
     */
    private class NotificationStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            // クリティカルセクション（バッテリーステータスの状態更新etc）にmutexロックを掛ける。
            synchronized (this) {

                int notificationId = intent.getIntExtra("notificationId", -1);
                String mServiceId = intent.getStringExtra("serviceId");

                List<Event> events = EventManager.INSTANCE.getEventList(
                        mServiceId,
                        HostNotificationProfile.PROFILE_NAME,
                        null,
                        HostNotificationProfile.ATTRIBUTE_ON_CLICK);

                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    Intent evtIntent = EventManager.createEventMessage(event);
                    evtIntent.putExtra(HostNotificationProfile.PARAM_NOTIFICATION_ID, "" + notificationId);
                    sendEvent(evtIntent, event.getAccessToken());
                }

                // 状態更新の為のmutexロックを外して、待っていた処理に通知する。
                notifyAll();
            }
        }
    }
}
