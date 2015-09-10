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
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Notification;
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

    @Override
    protected boolean onPostNotify(final Intent request, final Intent response, final String serviceId,
            final NotificationType type, final Direction dir, final String lang, final String body, final String tag,
            final byte[] iconData) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            if (body == null) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "body is invalid.");
                return true;
            } else {
                if (mNotificationStatusReceiver == null) {
                    mNotificationStatusReceiver = new NotificationStatusReceiver();
                    getContext().getApplicationContext().registerReceiver(mNotificationStatusReceiver,
                            new IntentFilter(ACTON_NOTIFICATION));
                }

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
                    encodeBody = URLDecoder.decode(body, "UTF-8");
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
                            notifyId, notifyIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

                    Notification notification;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(this.getContext())
                                        .setSmallIcon(iconType)
                                        .setContentTitle("" + title)
                                        .setContentText(encodeBody)
                                        .setContentIntent(mPendingIntent);
                        notification = notificationBuilder.build();
                    } else {
                        Notification.Builder notificationBuilder =
                                new Notification.Builder(this.getContext())
                                        .setSmallIcon(Icon.createWithResource(getContext(), iconType))
                                        .setContentTitle("" + title)
                                        .setContentText(encodeBody)
                                        .setContentIntent(mPendingIntent);
                        notification = notificationBuilder.build();
                    }

                    // Get an instance of the NotificationManager service
                    NotificationManager mNotification = (NotificationManager) getContext()
                            .getSystemService(Context.NOTIFICATION_SERVICE);

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
        }
        return true;
    }

    /**
     * デバイスへのノーティフィケーション消去リクエストハンドラー.<br/>
     * ノーティフィケーションを消去し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param notificationId 通知ID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteNotify(final Intent request, final Intent response, final String serviceId,
            final String notificationId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
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
        }
        return true;
    }

    @Override
    protected boolean onPutOnClick(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            mNotificationStatusReceiver = new NotificationStatusReceiver();
            IntentFilter intentFilter = new IntentFilter(ACTON_NOTIFICATION);
            this.getContext().registerReceiver(mNotificationStatusReceiver, intentFilter);

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
                return true;
            }

        }
        return true;
    }

    @Override
    protected boolean onPutOnClose(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
                return true;
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnShow(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnClick(final Intent request,
            final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnClose(final Intent request,
            final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnShow(final Intent request,
            final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
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
        }
        return true;
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * セッションキーが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response, "SessionKey not found");
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = HostServiceDiscoveryProfile.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);

        return match.find();
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
                    Intent mIntent = EventManager.createEventMessage(event);
                    mIntent.putExtra(HostNotificationProfile.PARAM_NOTIFICATION_ID, "" + notificationId);
                    getContext().sendBroadcast(mIntent);
                }

                // 状態更新の為のmutexロックを外して、待っていた処理に通知する。
                notifyAll();
            }
        }
    }
}
