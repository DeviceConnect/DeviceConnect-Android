/*
 KeepAlive.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.keepalive;

import android.content.ComponentName;
import android.content.Intent;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DConnectBroadcastReceiver;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * Keep Alive Manager.
 * @author NTT DOCOMO, INC.
 */
public class KeepAliveManager {
    /** インスタンス. */
    private static KeepAliveManager sInstance;
    /** Applicationインスタンス. */
    private DConnectApplication mApp;
    /** イベント Keep Alive 管理テーブル. */
    private final LinkedList<KeepAlive> mManagementList = new LinkedList<>();
    /** 機能有効フラグ 初期起動時は有効. */
    private Boolean mEnableKeepAlive = true;
    /** タイマーハンドラー. */
    private Timer mTimer;
    /** 定期処理中フラグ. */
    private Boolean mRunningPeriodicProcess = false;

    /**
     * コンストラクター.
     *
     * @param app DConnectApplicationのインスタンス
     */
    public KeepAliveManager(final DConnectApplication app) {
        mApp = app;
    }

    /**
     * KeepAlive機能有効化チェック.
     * @return 有効時はtrue、無効時はfalse.
     */
    public Boolean isEnableKeepAlive() {
        return mEnableKeepAlive;
    }

    /**
     * KeepAlive機能無効.
     * @return 正常終了はtrue、それ以外はfalse.
     */
    public Boolean disableKeepAlive() {
        setKeepAliveFunction(false);
        stopPeriodicProcess();
        if (!mManagementList.isEmpty()) {
            for (int i = 0; i < mManagementList.size(); i++) {
                KeepAlive data = mManagementList.get(i);
                sendKeepAlive(data.getPlugin(), "STOP");
            }
        }
        return true;
    }

    /**
     * KeepAlive機能有効.
     * @return 正常終了はtrue、それ以外はfalse.
     */
    public Boolean enableKeepAlive() {
        setKeepAliveFunction(true);
        if (!mManagementList.isEmpty()) {
            if (!mManagementList.isEmpty()) {
                for (int i = 0; i < mManagementList.size(); i++) {
                    KeepAlive data = mManagementList.get(i);
                    sendKeepAlive(data.getPlugin(), "START");
                }
            }
            startPeriodicProcess();
        }
        return true;
    }

    /**
     * KeepAlive機能設定.
     * @param flag true設定で有効、false設定で無効.
     */
    public void setKeepAliveFunction(final Boolean flag) {
        mEnableKeepAlive = flag;
    }

    /**
     * 該当デバイスプラグインを管理テーブルに設定する.
     * @param plugin デバイスプラグイン.
     */
    public void setManagementTable(DevicePlugin plugin) {
        KeepAlive data = getKeepAlive(plugin);
        if (data == null) {
            mManagementList.add(new KeepAlive(plugin));
            sendKeepAlive(plugin, "START");
        } else {
            data.additionEventCounter();
        }
        if (isEnableKeepAlive() && !mRunningPeriodicProcess) {
            startPeriodicProcess();
        }
    }

    /**
     * 該当するデバイスプラグインを持つKeepAlive要素を削除する.
     * @param plugin デバイスプラグイン.
     */
    public void removeManagementTable(final DevicePlugin plugin) {
        if (!(mManagementList.isEmpty())) {
            for (int i = 0; i < mManagementList.size(); i++) {
                KeepAlive data = mManagementList.get(i);
                if (data.getServiceId().equals(plugin.getServiceId())) {
                    data.subtractionEventCounter();
                    if (data.getEventCounter() <= 0) {
                        sendKeepAlive(plugin, "STOP");
                        mManagementList.remove(i);
                    }
                    break;
                }
            }
            if (isEnableKeepAlive() && mManagementList.isEmpty() && mRunningPeriodicProcess) {
                stopPeriodicProcess();
            }
        } else if (isEnableKeepAlive() && mRunningPeriodicProcess) {
            stopPeriodicProcess();
        }
    }

    /**
     * 該当するデバイスプラグインのKeepAlive要素を取得する.
     * @param plugin デバイスプラグイン.
     * @return KeepAliveデータ.
     */
    public KeepAlive getKeepAlive(final DevicePlugin plugin) {
        if (!(mManagementList.isEmpty())) {
            /** 要素数分ループ. */
            for (KeepAlive data : mManagementList) {
                if (data.getServiceId().equals(plugin.getServiceId())) {
                    return data;
                }
            }
        }
        /** 要素0または該当なしならnull. */
        return null;
    }

    /**
     * 該当するプラグインIDのKeepAlive要素を取得する.
     * @param serviceId プラグインID.
     * @return KeepAliveデータ.
     */
    public KeepAlive getKeepAlive(final String serviceId) {
        if (!(mManagementList.isEmpty())) {
            /** 要素数分ループ. */
            for (KeepAlive data : mManagementList) {
                if (data.getServiceId().equals(serviceId)) {
                    return data;
                }
            }
        }
        /** 要素0または該当なしならnull. */
        return null;
    }

    /**
     * KeepAlive送信.
     * @param plugin デバイスプラグイン.
     * @param status ステータス.
     */
    private void sendKeepAlive(final DevicePlugin plugin, final String status) {
        Intent request = new Intent();
        request.setComponent(plugin.getComponentName());
        request.setAction(IntentDConnectMessage.ACTION_KEEPALIVE);
        request.putExtra(IntentDConnectMessage.EXTRA_KEEPALIVE_STATUS, status);
        request.putExtra(IntentDConnectMessage.EXTRA_RECEIVER, new ComponentName(mApp.getApplicationContext(), DConnectBroadcastReceiver.class));
        request.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID, plugin.getServiceId());
        mApp.getApplicationContext().sendBroadcast(request);
    }

    /**
     * DisconnectWebSocket送信.
     * @param sessionKey セッションキー.
     */
    private void sendDisconnectWebSeocket(final String sessionKey) {
        String[] key = sessionKey.split(Pattern.quote("."), -1);
        Intent request = new Intent();
        request.setComponent(new ComponentName(mApp.getApplicationContext(), DConnectBroadcastReceiver.class));
        request.setAction(IntentDConnectMessage.ACTION_KEEPALIVE);
        request.putExtra(IntentDConnectMessage.EXTRA_KEEPALIVE_STATUS, "DISCONNECT");
        request.putExtra(IntentDConnectMessage.EXTRA_SESSION_KEY, key[0]);
        mApp.getApplicationContext().sendBroadcast(request);
    }

    /**
     * 定期処理
     */
    private void periodicProcess() {
        if (isEnableKeepAlive()) {
            for (KeepAlive data : mManagementList) {
                if (data.getResponseFlag()) {
                    data.resetResponseFlag();
                    sendKeepAlive(data.getPlugin(), "CHECK");
                } else {
                    DevicePlugin plugin = data.getPlugin();
                    removeManagementTable(plugin);
                    // 該当プラグインIDに紐付くWebSocketの切断処理
                    String sessionKey;
                    while ((sessionKey = mApp.getIdentifySessionKey(plugin))!= null) {
                        sendDisconnectWebSeocket(sessionKey);
                        mApp.removeDevicePluginIdentifyKey(sessionKey);
                    }
                }
            }
        }
    }

    /**
     * 定期処理開始.
     */
    private void startPeriodicProcess() {
        if (mTimer == null) {
            mTimer = new Timer(true);
            /* 定期処理間隔(mSec). */
            int PROCESS_INTERVAL = 30000;
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    periodicProcess();
                }
            }, PROCESS_INTERVAL, PROCESS_INTERVAL);
            mRunningPeriodicProcess = true;
        }
    }

    /** 定期処理停止. */
    private void stopPeriodicProcess() {
        mRunningPeriodicProcess = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
