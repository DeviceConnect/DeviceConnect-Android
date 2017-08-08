/*
 KeepAlive.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.event;

import org.deviceconnect.android.manager.plugin.DevicePlugin;

/**
 * Keep Alive.
 * @author NTT DOCOMO, INC.
 */
public class KeepAlive {
    /** デバイスプラグイン */
    private DevicePlugin mPlugin;
    /** イベントカウンター. */
    private int mEventCounter;
    /** レスポンスフラグ. */
    private Boolean mResponseFlag;

    /** コンストラクター. */
    public KeepAlive(final DevicePlugin plugin) {
        setPlugin(plugin);
        setEventCounter(1);
        resetResponseFlag();
    }

    /**
     * デバイスプラグインを取得する.
     * @return デバイスプラグイン.
     */
    public DevicePlugin getPlugin() {
        return mPlugin;
    }

    /**
     * デバイスプラグインIDを取得する.
     * @return デバイスプラグインID.
     */
    public String getServiceId() {
        return mPlugin.getPluginId();
    }

    /**
     * レスポンスフラグを取得する.
     * @return レスポンスフラグ(TRUE or FALSE).
     */
    public Boolean getResponseFlag() {
        return mResponseFlag;
    }

    /**
     * イベントカウンターを取得する.
     * @return イベントカウンター.
     */
    public int getEventCounter() {
        return mEventCounter;
    }

    /**
     * デバイスプラグインを設定する.
     * @param plugin  デバイスプラグイン.
     */
    public void setPlugin(final DevicePlugin plugin) {
        mPlugin = plugin;
    }

    /**
     * レスポンスフラグをセットする.
     */
    public void setResponseFlag() {
        mResponseFlag = true;
    }

    /**
     * レスポンスフラグをリセットする.
     */
    public void resetResponseFlag() {
        mResponseFlag = false;
    }

    /**
     * イベントカウンターを設定する.
     * @param eventCounter イベントカウンター設定値.
     */
    public void setEventCounter(final int eventCounter) {
        mEventCounter = eventCounter;
    }

    /**
     * イベントカウンター加算.
     */
    public void additionEventCounter() {
        mEventCounter++;
    }

    /**
     * イベントカウンター減算.
     */
    public void subtractionEventCounter() {
        mEventCounter--;
    }
}
