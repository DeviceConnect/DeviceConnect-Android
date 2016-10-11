/*
 EventCacheController.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event.cache;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;

import java.util.List;

/**
 * イベントデータ操作インターフェース.
 * イベントデータの追加、削除、保存、検索の機能を提供する。
 * 
 *
 * @author NTT DOCOMO, INC.
 */
public interface EventCacheController {
    
    /**
     * イベントデータをキャッシュに追加する.
     * 
     * @param event イベントデータ
     * @return 処理結果
     */
    EventError addEvent(Event event);
    
    /**
     * イベントデータをキャッシュから削除する.
     * 
     * @param event イベントデータ
     * @return 処理結果
     */
    EventError removeEvent(Event event);
    
    /**
     * 指定されたオリジンに紐づくイベント情報を全て削除する.
     * 
     * @param origin オリジン
     * @return 成功の場合true、その他はfalseを返す
     */
    boolean removeEvents(String origin);
    
    /**
     * キャッシュからデータを全て削除する.
     * @return 成功の場合true、その他はfalseを返す
     */
    boolean removeAll();
    
    /**
     * キャッシュから指定された条件に合うイベントデータを取得する.
     * 
     * @param serviceId サービスID
     * @param profile プロファイル名
     * @param inter インターフェース名
     * @param attribute 属性名
     * @param origin オリジン
     * @param receiver レシーバー名
     * @return イベントデータ。条件に合うものが無い場合はnullを返す。
     */
    Event getEvent(String serviceId, String profile, String inter, 
            String attribute, String origin, String receiver);
    
    /**
     * キャッシュから条件にあうイベントデータの一覧を取得する.
     * 
     * @param serviceId サービスID
     * @param profile プロファイル名
     * @param inter インターフェース名
     * @param attribute 属性名
     * @return イベントデータの一覧。無い場合は空のリストを返す。
     */
    List<Event> getEvents(String serviceId, String profile, String inter, String attribute);

    List<Event> getEvents(String sessionKey);

    /**
     * キャッシュデータをフラッシュする.
     */
    void flush();
    
}
