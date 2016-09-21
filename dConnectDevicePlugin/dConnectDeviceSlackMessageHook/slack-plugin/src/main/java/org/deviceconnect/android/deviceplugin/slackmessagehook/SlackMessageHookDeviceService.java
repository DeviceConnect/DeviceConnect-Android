/*
 SlackMessageHookDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.slackmessagehook.profile.SlackMessageHookProfile;
import org.deviceconnect.android.deviceplugin.slackmessagehook.profile.SlackMessageHookSystemProfile;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.Utils;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class SlackMessageHookDeviceService extends DConnectMessageService implements SlackManager.SlackEventListener {

    /** サービスID */
    public static final String SERVICE_ID = "SlackMessageHook";

    /** メッセージ履歴保持時間（秒） */
    private static final int MESSAGE_HOLD_LIMIT = 60;
    /** メッセージ履歴 */
    private List<Bundle> eventHistory = new ArrayList<>();
    /** ユーザーリスト */
    private HashMap<String, SlackManager.ListInfo> userMap = new HashMap<>();


    @Override
    public void onCreate() {
        super.onCreate();

        // Eventの設定.
        EventManager.INSTANCE.setController(new MemoryCacheController());
        SlackManager.INSTANCE.addSlackEventListener(this);

        // Profile追加
        DConnectService service = new DConnectService(SERVICE_ID);
        service.setName(SERVICE_ID);
        service.setNetworkType(ServiceDiscoveryProfileConstants.NetworkType.WIFI);
        service.addProfile(new SlackMessageHookProfile());
        getServiceProvider().addService(service);

        // 接続
        if (Utils.getOnlineStatus(getContext())) {
            final String token = Utils.getAccessToken(this);
            SlackManager.INSTANCE.setApiToken(token, true, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SlackManager.INSTANCE.removeSlackEventListener(this);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SlackMessageHookSystemProfile();
    }

    @Override
    public void OnConnect() {
        // オンライン
        DConnectService service = getServiceProvider().getService(SERVICE_ID);
        if (service != null) {
            service.setOnline(true);
        }
        // ユーザーリスト取得
        if (SlackManager.INSTANCE.isConnected()) {
            fetchUserList();
        }
    }

    @Override
    public void OnConnectLost() {
        // オフライン
        DConnectService service = getServiceProvider().getService(SERVICE_ID);
        if (service != null) {
            service.setOnline(false);
        }
    }

    @Override
    public void OnReceiveSlackMessage(SlackManager.HistoryInfo info) {
        sendMessageEvent(info.text, info.channel, info.user, info.ts, info.file, info.mimetype);
    }


    /**
     * 受信メッセージをEventとして送信
     * @param text テキスト
     * @param channel チャンネル
     * @param user 送信者
     * @param ts 時間
     * @param url リソースURL
     * @param mimeType MimeType
     */
    private void sendMessageEvent(String text, String channel, String user, Double ts, String url, String mimeType) {
        String serviceId = SERVICE_ID;
        String profile = SlackMessageHookProfile.PROFILE_NAME;
        String attribute = SlackMessageHookProfile.ATTRIBUTE_MESSAGE;
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId, profile, null, attribute);

        synchronized (events) {
            synchronized (userMap) {
                // 情報セット
                Bundle message = new Bundle();
                message.putString("messengerType", "slack");
                message.putString("channelId", channel);

                // TimeStampは少数以下切り捨て
                double time = ts;
                message.putLong("timeStamp", (long)time);

                // 送信者情報を変換
                SlackManager.ListInfo info = userMap.get(user);
                if (info != null) {
                    message.putString("from", info.name);
                } else {
                    message.putString("from", user);
                    // 値が無い場合はユーザーリスト再取得
                    fetchUserList();
                }

                // メンション処理
                if (text != null) {
                    boolean isMentioned = false;
                    Pattern p = Pattern.compile("<@(\\w*)>");
                    Matcher m = p.matcher(text);
                    StringBuffer sb = new StringBuffer();
                    while (m.find()) {
                        String uid = m.group(1);
                        info = userMap.get(uid);
                        if (info != null) {
                            m.appendReplacement(sb, "@" + info.name);
                        } else {
                            m.appendReplacement(sb, m.group());
                            // 値が無い場合はユーザーリスト再取得
                            fetchUserList();
                        }
                        if (!isMentioned) {
                            isMentioned = SlackManager.INSTANCE.getBotInfo().id.equals(uid);
                        }
                    }
                    m.appendTail(sb);
                    message.putString("text", sb.toString());
                    //　メッセージタイプ
                    String messageType = null;
                    // Dから始まるChannelIDはDirectMessage
                    if (channel.startsWith("D")) {
                        messageType = "direct";
                    } else {
                        messageType = "normal";
                    }
                    if (isMentioned) {
                        messageType += ",mention";
                    }
                    message.putString("messageType", messageType);
                }

                // リソース
                if (url != null) {
                    message.putString("resource", url);
                }
                if (mimeType != null) {
                    message.putString("mimeType", mimeType);
                }

                // GetMessageのために一定時間イベントを保持する
                eventHistory.add(message);
                // 一定時間経過したメッセージを削除する
                truncateHistory();

                // Eventに値をおくる.
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    intent.putExtra("message", message);
                    sendEvent(intent, event.getAccessToken());
                }
            }
        }
    }

    /**
     * ユーザーリスト取得
     */
    private void fetchUserList() {
        SlackManager.INSTANCE.getUserList(new SlackManager.FinishCallback<ArrayList<SlackManager.ListInfo>>() {
            @Override
            public void onFinish(ArrayList<SlackManager.ListInfo> listInfos, Exception error) {
                if (error == null) {
                    synchronized (userMap) {
                        // UserをHashMapへ
                        userMap = new HashMap<>();
                        for (SlackManager.ListInfo info : listInfos) {
                            userMap.put(info.id, info);
                        }
                    }
                } else {
                    Log.e("slack", "err", error);
                }
            }
        });
    }

    /**
     * メッセージ履歴を取得する
     * @return 履歴
     */
    public List<Bundle> getHistory() {
        // 一定時間経過したメッセージを削除してから返す
        truncateHistory();
        return eventHistory;
    }

    /**
     * 一定時間経過したメッセージを削除する
     */
    private void truncateHistory() {
        Iterator itr = eventHistory.iterator();
        long limit = System.currentTimeMillis() / 1000L - MESSAGE_HOLD_LIMIT;
        while(itr.hasNext()){
            Bundle bundle = (Bundle)itr.next();
            long timeStamp = bundle.getLong("timeStamp");
            if (timeStamp < limit) {
                itr.remove();
            } else {
                break;
            }
        }
    }
}