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
import org.deviceconnect.android.deviceplugin.slackmessagehook.profile.SlackMessageHookServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.slackmessagehook.profile.SlackMessageHookSystemProfile;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 * @author NTT DOCOMO, INC.
 */
public class SlackMessageHookDeviceService extends DConnectMessageService implements SlackManager.SlackEventListener {

    /** ユーザーリスト */
    private HashMap<String, SlackManager.ListInfo> userMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Eventの設定.
        EventManager.INSTANCE.setController(new MemoryCacheController());
        SlackManager.INSTANCE.setSlackEventListener(this);

        // Profile追加
        addProfile(new SlackMessageHookProfile());

        // ユーザーリスト取得
        // TODO: 接続時に取りに行く
        if (SlackManager.INSTANCE.isConnected()) {
            fetchUserList();
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SlackMessageHookSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this){};
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new SlackMessageHookServiceDiscoveryProfile(this);
    }

    @Override
    public void OnReceiveSlackMessage(String text, String channel, String user, String ts) {
        sendMessageEvent(text, channel, user, ts, null, null);
    }

    @Override
    public void OnReceiveSlackFile(String comment, String channel, String user, String ts, String url, String mimeType) {
        sendMessageEvent(comment, channel, user, ts, url, mimeType);
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
    private void sendMessageEvent(String text, String channel, String user, String ts, String url, String mimeType) {
        String serviceId = SlackMessageHookServiceDiscoveryProfile.SERVICE_ID;
        String profile = SlackMessageHookProfile.PROFILE_NAME;
        String attribute = SlackMessageHookProfile.ATTRIBUTE_MESSAGE;
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId, profile, null, attribute);

        if (events.size() == 0) return;

        synchronized (events) {
            synchronized (userMap) {
                // 情報セット
                Bundle message = new Bundle();
                message.putString("messengerType", "slack");
                message.putString("channelId", channel);

                // TimeStampは少数以下切り捨て
                float time = Float.parseFloat(ts);
                message.putInt("timeStamp", (int)time);

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

                // Eventに値をおくる.
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    intent.putExtra("message", message);
                    sendEvent(intent, event.getAccessToken());
                }
            }
        }
    }

    // ユーザーリスト取得
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
}