/*
 DConnectApplication.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Device Connect Manager Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectApplication extends Application {

    /** デバイスプラグインに紐付くイベント判断用キー格納領域 */
    private final Map<String, String> mEventKeys = new ConcurrentHashMap<>();

    /** インスタンス */
    private static DConnectApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    /**
     * Applicationインスタンス取得.
     * @return インスタンス
     */
    public static synchronized DConnectApplication getInstance() {
        return sInstance;
    }

    /**
     * セッションキーとデバイスプラグインの紐付けを行う.
     * @param identifyKey appendPluginIdToSessionKey()加工後のセッションキー
     * @param serviceId プラグインID
     */
    public void setDevicePluginIdentifyKey(final String identifyKey, final String serviceId) {
        mEventKeys.put(identifyKey, serviceId);
    }

    /**
     * セッションキーに紐付いているデバイスプラグインIDを取得する.
     * @param identifyKey セッションキー
     * @return プラグインID、該当無しの場合はnull
     */
    public String getDevicePluginIdentifyKey(final String identifyKey) {
        if (mEventKeys.containsKey(identifyKey)) {
            return mEventKeys.get(identifyKey);
        } else {
            return null;
        }
    }

    /**
     * セッションキーに紐付いているデバイスプラグインIDを削除する.
     * @param identifyKey セッションキー
     * @return 削除成功でtrue, 該当無しの場合はfalse
     */
    public boolean removeDevicePluginIdentifyKey(final String identifyKey) {
        if (mEventKeys.containsKey(identifyKey)) {
            mEventKeys.remove(identifyKey);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Map登録されているKey取得.
     * @param sessionKey セッションキー
     * @return Map登録されているKey, 存在しない場合はnull.
     */
    public String getIdentifySessionKey(final String sessionKey) {
        String matchKey = null;
        for (Map.Entry<String, String> entry : mEventKeys.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(sessionKey)) {
                matchKey = key;
                break;
            }
        }
        return matchKey;
    }
}
