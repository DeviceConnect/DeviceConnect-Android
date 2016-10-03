/*
 EventManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event;

import android.content.ComponentName;
import android.content.Intent;

import org.deviceconnect.android.event.cache.EventCacheController;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;

/**
 * イベント管理クラス. イベントの登録、解除、送信などはこのクラスを通すことで一元管理される。
 * 
 * 
 * @author NTT DOCOMO, INC.
 */
public enum EventManager {

    /**
     * シングルトンなEventManagerのインスタンス.
     */
    INSTANCE;

    /** 
     * キャッシュコントローラー.
     */
    private EventCacheController mController;
    
    /**
     * キャッシュの操作クラスを設定する.
     * スレッドセーフではないので、必要な場合は呼び出しもとで同期処理をすること。
     * 
     * @param controller キャッシュ操作オブジェクト。
     */
    public void setController(final EventCacheController controller) {
        mController = controller;
    }
    
    /**
     * コントローラーの設定状況を確認し、異常な場合は例外を投げる.
     */
    private void checkState() {
        if (mController == null) {
            throw new IllegalStateException("CacheController is not set.");
        }
    }
    
    /**
     * リクエストIntentからEventを生成する.
     * 
     * @param request リクエストデータ
     * @return イベントオブジェクト
     */
    private Event createEvent(final Intent request) {
        
        if (request == null) {
            throw new IllegalArgumentException("Request is null.");
        } 
        
        checkState();
        
        String serviceId = request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        String profile = request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String inter = request.getStringExtra(DConnectMessage.EXTRA_INTERFACE);
        String attribute = request.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
        String accessToken = request.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        ComponentName name = request.getParcelableExtra(DConnectMessage.EXTRA_RECEIVER);
        
        Event event = new Event();
        event.setOrigin(origin);
        event.setAccessToken(accessToken);
        // XXXX パスの大文字小文字を無視
        event.setProfile(profile != null ? profile.toLowerCase() : null);
        event.setInterface(inter != null ? inter.toLowerCase() : null);
        event.setAttribute(attribute != null ? attribute.toLowerCase() : null);
        event.setServiceId(serviceId);
        if (name != null) {
            event.setReceiverName(name.flattenToString());
        }
        
        return event;
    }

    /**
     * 指定されたイベント登録用のリクエストからイベントデータを登録する.
     * 
     * @param request イベント登録リクエスト
     * @return 処理結果
     */
    public EventError addEvent(final Intent request) {
        Event event = createEvent(request);
        return mController.addEvent(event);
    }

    public Event getEvent(final Intent request) {
        checkState();

        ComponentName receiver = request.getParcelableExtra(DConnectMessage.EXTRA_RECEIVER);
        String receiverName = receiver != null ? receiver.flattenToString() : null;
        String profile = request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String inter = request.getStringExtra(DConnectMessage.EXTRA_INTERFACE);
        String attribute = request.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
        // XXXX パスの大文字小文字を無視
        return mController.getEvent(request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID),
                profile != null ? profile.toLowerCase() : null,
                inter != null ? inter.toLowerCase() : null,
                attribute != null ? attribute.toLowerCase() : null,
                request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN),
                receiverName);
    }

    /**
     * 指定されたイベント解除用のリクエストからイベントデータを解除する.
     * 
     * @param request イベント解除リクエスト
     * @return 処理結果
     */
    public EventError removeEvent(final Intent request) {
        return removeEvent(createEvent(request));
    }

    public EventError removeEvent(final Event event) {
        checkState();
        if (event == null) {
            throw new IllegalArgumentException("Event is null.");
        }
        return mController.removeEvent(event);
    }

    /**
     * 指定されたオリジンに紐づくイベント情報を解除する.
     * 
     * @param origin オリジン
     * @return 削除に成功した場合はtrue、その他はfalseを返す。
     */
    public boolean removeEvents(final String origin) {
        checkState();
        return mController.removeEvents(origin);
    }
    
    /**
     * イベントを全て削除する.
     * 
     * @return 成功の場合true、その他はfalseを返す。
     */
    public boolean removeAll() {
        checkState();
        return mController.removeAll();
    }
    
    /**
     * キャッシュデータを書き込む.
     */
    public void flush() {
        checkState();
        mController.flush();
    }

    /**
     * 指定されたイベント用のリクエストからイベント情報の一覧を取得する.
     *
     * @param request イベント解除リクエスト
     * @return イベントの一覧
     */
    public List<Event> getEventList(final Intent request) {
        return getEventList(
                request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID),
                request.getStringExtra(DConnectMessage.EXTRA_PROFILE),
                request.getStringExtra(DConnectMessage.EXTRA_INTERFACE),
                request.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE));
    }

    /**
     * 指定されたサービスIDとAPIに紐づくイベント情報の一覧を取得する.
     * 
     * @param serviceId サービスID
     * @param profile プロファイル名
     * @param inter インターフェース名
     * @param attribute アトリビュート名
     * @return イベントの一覧
     */
    public List<Event> getEventList(final String serviceId, final String profile, 
            final String inter, final String attribute) {
        checkState();
        // XXXX パスの大文字小文字を無視
        return mController.getEvents(serviceId,
            profile != null ? profile.toLowerCase() : null,
            inter != null ? inter.toLowerCase() : null,
            attribute != null ? attribute.toLowerCase() : null);
    }
    
    /**
     * 指定されたAPIに紐づくイベント情報の一覧を取得する.
     * 
     * @param profile プロファイル名
     * @param inter インターフェース名
     * @param attribute アトリビュート名
     * @return イベントの一覧
     */
    public List<Event> getEventList(final String profile, final String inter, final String attribute) {
        checkState();
        return getEventList(null, profile, inter, attribute);
    }
    
    /**
     * 指定されたAPIに紐づくイベント情報の一覧を取得する.
     * 
     * @param profile プロファイル名
     * @param attribute アトリビュート名
     * @return イベントの一覧
     */
    public List<Event> getEventList(final String profile, final String attribute) {
        checkState();
        return getEventList(profile, null, attribute);
    }

    /**
     * 指定されたAPIに紐づくイベント情報の一覧を取得する.
     *
     * @param sessionKey セッションキー
     * @return イベントの一覧
     */
    public List<Event> getEventList(final String sessionKey) {
        checkState();
        return mController.getEvents(sessionKey);
    }

    /**
     * イベントデータからイベントメッセージ用のIntentを生成する.
     * 取得したIntentに適宜イベントオブジェクトを設定し送信すること。
     * 
     * @param event イベントデータ
     * @return イベントメッセージ用Intent
     */
    public static Intent createEventMessage(final Event event) {
        Intent message = MessageUtils.createEventIntent();
        message.putExtra(DConnectMessage.EXTRA_SERVICE_ID, event.getServiceId());
        message.putExtra(DConnectMessage.EXTRA_PROFILE, event.getProfile());
        message.putExtra(DConnectMessage.EXTRA_INTERFACE, event.getInterface());
        message.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, event.getAttribute());
        message.putExtra(DConnectMessage.EXTRA_ACCESS_TOKEN, event.getAccessToken());
        ComponentName cn = ComponentName.unflattenFromString(event.getReceiverName());
        message.setComponent(cn);
        return message;
    }
}
