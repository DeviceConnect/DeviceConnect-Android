/*
 MemoryCacheController.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;

/**
 * イベントデータをメモリにキャッシュし、キャッシュの操作機能を提供する.
 * 
 * 
 * @author NTT DOCOMO, INC.
 */
public class MemoryCacheController extends BaseCacheController {

    /**
     * イベントマップ. serviceId毎にイベントの種類をキーにイベント情報を管理する。
     * 
     */
    private Map<String, Map<String, List<Event>>> mEventMap;
    
    /** 
     * 空のサービスID用キー.
     */
    private static final String NULL_SERVICE_ID = "__null";
    
    /** 
     * 空のレシーバー用キー.
     */
    private static final String NULL_RECEIVER_NAME = "";
    
    /**
     * メモリキャッシュコントローラーを生成する.
     */
    public MemoryCacheController() {
        mEventMap = new HashMap<String, Map<String, List<Event>>>();
    }
    
    /**
     * イベント情報からサービスIDを取得する.
     * サービスIDが無い場合はnullを示す特殊な文字列を返す。
     * 
     * @param event イベントデータ
     * @return サービスID
     */
    private String getServiceId(final Event event) {
        String serviceId = event.getServiceId();
        if (serviceId == null) {
            serviceId = NULL_SERVICE_ID;
        }
        return serviceId;
    }
    
    /**
     * イベント情報からレシーバー名を取得する.
     * レシーバーが無い場合は空文字を返す。
     * 
     * @param event イベント情報
     * @return レシーバー名
     */
    private String getReceiverName(final Event event) {
        String receiver = event.getReceiverName();
        if (receiver == null) {
            receiver = NULL_RECEIVER_NAME;
        }
        return receiver;
    }
    
    @Override
    public synchronized EventError addEvent(final Event event) {
        
        if (!checkParameter(event)) {
            return EventError.INVALID_PARAMETER;
        }
        
        String serviceId = getServiceId(event);
        Map<String, List<Event>> events = mEventMap.get(serviceId);
        
        if (events == null) {
            events = new HashMap<String, List<Event>>();
            mEventMap.put(serviceId, events);
        }
        
        String path = event.getProfile();
        if (event.getInterface() != null) {
            path += event.getInterface();
        }
        path += event.getAttribute();
        
        List<Event> eventList = events.get(path);
        if (eventList == null) {
            eventList = new CopyOnWriteArrayList<Event>();
            events.put(path, eventList);
        }
        
        String sessionKey = event.getSessionKey();
        String receiver = getReceiverName(event);
        for (Event e : eventList) {
            if (e.getSessionKey().equals(sessionKey) && e.getReceiverName().equals(receiver)) {
                // 登録済みの場合はアクセストークンを上書きする
                e.setAccessToken(event.getAccessToken());
                e.setUpdateDate(Utils.getCurreTimestamp());
                return EventError.NONE;
            }
        }
        event.setCreateDate(Utils.getCurreTimestamp());
        event.setUpdateDate(Utils.getCurreTimestamp());
        eventList.add(event);
        
        return EventError.NONE;
    }

    @Override
    public synchronized EventError removeEvent(final Event event) {
        
        if (!checkParameter(event)) {
            return EventError.INVALID_PARAMETER;
        }
        
        String serviceId = getServiceId(event);
        Map<String, List<Event>> events = mEventMap.get(serviceId);
        
        if (events == null) {
            return EventError.NOT_FOUND;
        }

        String path = event.getProfile();
        if (event.getInterface() != null) {
            path += event.getInterface();
        }
        path += event.getAttribute();
        
        List<Event> eventList = events.get(path);
        if (eventList == null) {
            return EventError.NOT_FOUND;
        }
        
        String sessionKey = event.getSessionKey();
        String receiver = getReceiverName(event);
        for (Event e : eventList) {
            if (e.getSessionKey().equals(sessionKey) && e.getReceiverName().equals(receiver)) {
                eventList.remove(e);
                if (eventList.size() == 0) {
                    events.remove(path);
                }
                return EventError.NONE;
            }
        }
        
        return EventError.NOT_FOUND;
    }

    @Override
    public synchronized Event getEvent(final String serviceId, final String profile, final String inter, 
            final String attribute, final String sessionKey, final String receiver) {
        Event event = null;
        String tmpReceiver = receiver;
        if (tmpReceiver == null) {
            tmpReceiver = NULL_RECEIVER_NAME;
        }
        
        do {
            List<Event> eventList = getEvents(serviceId, profile, inter, attribute);
            if (eventList == null) {
                break;
            }
            
            for (Event e : eventList) {
                if (e.getSessionKey().equals(sessionKey) && e.getReceiverName().equals(tmpReceiver)) {
                    event = e;
                    break;
                }
            }            
        } while (false);
        
        return event;
    }

    @Override
    public synchronized List<Event> getEvents(final String serviceId, final String profile, 
            final String inter, final String attribute) {
        
        String tmpServiceId = serviceId;
        if (serviceId == null) {
            tmpServiceId = NULL_SERVICE_ID;
        }
        Map<String, List<Event>> events = mEventMap.get(tmpServiceId);
        
        if (events == null) {
            return new ArrayList<Event>();
        }

        String path = profile;
        if (inter != null) {
            path += inter;
        }
        path += attribute;
        List<Event> res = events.get(path);
        if (res == null) {
            return new ArrayList<Event>();
        }
        
        return res;
    }

    @Override
    public void flush() {
        // do nothing.
    }

    @Override
    public synchronized boolean removeAll() {
        mEventMap.clear();
        return mEventMap.size() == 0;
    }
    
    /**
     * イベントデータのキャッシュオブジェクトを取得する.
     * Map&lt;serviceId, Map&lt;profile+interface+attribute, List&lt;Event&gt;&gt;&gt;。
     * 
     * @return キャッシュ
     */
    protected synchronized Map<String, Map<String, List<Event>>> getCache() {
        return mEventMap;
    }
    
    /**
     * キャッシュを設定する.
     * nullの場合設定されない。
     * 
     * @param cache キャッシュ
     */
    protected synchronized void setCache(final Map<String, Map<String, List<Event>>> cache) {
        if (cache != null) {
            mEventMap = cache;
        }
    }

    @Override
    public synchronized boolean removeEvents(final String sessionKey) {
        
        if (sessionKey == null) {
            throw new IllegalArgumentException("SessionKey is null.");
        }
        
        for (Entry<String, Map<String, List<Event>>> entry : mEventMap.entrySet()) {
            for (Entry<String, List<Event>> events : entry.getValue().entrySet()) {
                List<Event> removes = new ArrayList<Event>();
                for (Event event : events.getValue()) {
                    if (sessionKey.equals(event.getSessionKey())) {
                        removes.add(event);
                    }
                }
                if (removes.size() != 0) {
                    events.getValue().removeAll(removes);
                }
            }
        }
        
        return true;
    }
}
