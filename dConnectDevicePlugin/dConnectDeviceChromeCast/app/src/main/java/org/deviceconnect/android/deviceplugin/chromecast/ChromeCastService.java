/*
 ChromeCastService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastController;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastHttpServer;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMediaPlayer;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMessage;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastSystemProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.db.DBCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * メッセージサービス (Chromecast).
 * <p>
 * Chromecastデバイスプラグインのサービス
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastService extends DConnectMessageService implements
        ChromeCastMediaPlayer.Callbacks,
        ChromeCastMessage.Callbacks,
        ChromeCastDiscovery.Callbacks,
        ChromeCastController.Result,
        DConnectServiceListener {

    /**
     * Chromecastのサーバポート.
     */
    private static final int SERVER_PORT = 38088;

    /** Chromecast MediaPlayer. */
    private ChromeCastMediaPlayer mMediaPlayer;
    /** Chromecast Message. */
    private ChromeCastMessage mMessage;
    /** ChromecastHttpServer. */
    private ChromeCastHttpServer mServer;
    /** StatusChange時のServiceId. */
    private String mServiceIdOnStatusChange = null;
    /** MediaPlayerのステータスアップデートフラグ. */
    private boolean mEnableCastMediaPlayerStatusUpdate = false;
    /**
     * ChromeCastが接続完了してからレスポンスを返すためのCallbackを返す.
     * @author NTT DOCOMO, INC.
     */
    public interface Callback {
        /**
         * レスポンス.
         * @param connected true : 接続されている, false : 接続されていない
         */
        void onResponse(final boolean connected);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ChromeCastApplication app = (ChromeCastApplication) getApplication();
        app.initialize();
        app.getDiscovery().setCallbacks(this);
        app.getController().setResult(this);
        String appMsgUrn = getString(R.string.application_message_urn);

        int portCount = 0;
        while (portCount < 500) { // Portを決定する
            try {
                mServer = new ChromeCastHttpServer(this, "0.0.0.0", SERVER_PORT + portCount);
                mServer.start();
                break;
            } catch (IOException e) {
                portCount++;
            }
        }
        
        mMediaPlayer = new ChromeCastMediaPlayer(app.getController());
        mMediaPlayer.setCallbacks(this);
        mMessage = new ChromeCastMessage(app.getController(), appMsgUrn);
        mMessage.setCallbacks(this);

        EventManager.INSTANCE.setController(new DBCacheController(this));
        addProfile(new ChromeCastServiceDiscoveryProfile(getServiceProvider()));

    }

    @Override
    public void onDestroy() {
        mServer.stop();

        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new ChromeCastSystemProfile();
    }

    @Override
    public void onServiceAdded(final DConnectService service) {
        if (BuildConfig.DEBUG) {
            Log.i("TEST", "onServiceAdded: " + service.getName());
        }
        // NOP.
    }

    @Override
    public void onServiceRemoved(final DConnectService service) {
        if (BuildConfig.DEBUG) {
            Log.i("TEST", "onServiceRemoved: " + service.getName());
        }
        ChromeCastApplication app = (ChromeCastApplication) getApplication();
        if (app != null) {
            app.getController().teardown();
        }
    }

    @Override
    public void onStatusChange(final DConnectService service) {
        if (BuildConfig.DEBUG) {
            Log.i("TEST", "onStatusChange: " + service.getName());
        }
        // NOP.
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            Log.i("TEST", "Plug-in : onManagerUninstalled");
        }
        ChromeCastApplication app = (ChromeCastApplication) getApplication();
        if (app != null) {
            app.getController().teardown();
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.i("TEST", "Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.i("TEST", "Plug-in : onManagerEventTransmitDisconnected");
        }
        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }

    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            Log.i("TEST", "Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        /* 全イベント削除. */
        EventManager.INSTANCE.removeAll();
        onCastDeviceUnselected(null);
    }


    /**
     * ChromeCastDiscoveryを返す.
     * @return  ChromeCastDiscovery
     */
    public ChromeCastDiscovery getChromeCastDiscovery() {
        ChromeCastApplication app = (ChromeCastApplication) getApplication();
        if (app == null) {
            return null;
        }
        return app.getDiscovery();
    }

    /**
     * ChromeCastMediaPlayerを返す.
     * @return  ChromeCastMediaPlayer
     */
    public ChromeCastMediaPlayer getChromeCastMediaPlayer() {
        return mMediaPlayer;
    }
    /**
     * ChromeCastMessageを返す.
     * @return  ChromeCastMessage
     */
    public ChromeCastMessage getChromeCastMessage() {
        return mMessage;
    }
    /**
     * ChromeCastHttpServerを返す.
     * @return  ChromeCastHttpServer
     */
    public ChromeCastHttpServer getChromeCastHttpServer() {
        return mServer;
    }

    /**
     * StatusChange通知を有効にする.
     * 
     * @param response レスポンス
     * @param serviceId デバイスを識別するID
     */
    public void registerOnStatusChange(final Intent response, final String serviceId) {
        mServiceIdOnStatusChange = serviceId;
        mEnableCastMediaPlayerStatusUpdate = true;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnStatusChange event");
        sendResponse(response);
    }

    /**
     * StatusChange通知を無効にする.
     * 
     * @param response レスポンス
     */
    public void unregisterOnStatusChange(final Intent response) {
        mServiceIdOnStatusChange = null;
        mEnableCastMediaPlayerStatusUpdate = false;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister OnStatusChange event");
        sendResponse(response);
    }

    @Override
    public void onChromeCastMediaPlayerStatusUpdate(final MediaStatus status) {

        MediaInfo info = status.getMediaInfo();
        ChromeCastDeviceService service = (ChromeCastDeviceService) getServiceProvider().getService(mServiceIdOnStatusChange);
        if (service == null) {
            return;
        }
        String playStatusString = service.getMediaPlayerProfile().getPlayStatus(status.getPlayerState());

        if (mEnableCastMediaPlayerStatusUpdate) {
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceIdOnStatusChange, 
                    MediaPlayerProfile.PROFILE_NAME, null,
                    MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);

                Intent intent = EventManager.createEventMessage(event);
                MediaPlayerProfile.setAttribute(intent,
                    MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);
                Bundle mediaPlayer = new Bundle();
                MediaPlayerProfile.setStatus(mediaPlayer, playStatusString);
                if (info != null) {
                    MediaPlayerProfile.setMediaId(mediaPlayer, info.getContentId());
                    MediaPlayerProfile.setMIMEType(mediaPlayer, info.getContentType());
                } else {
                    MediaPlayerProfile.setMediaId(mediaPlayer, "");
                    MediaPlayerProfile.setMIMEType(mediaPlayer, "");
                }
                MediaPlayerProfile.setPos(mediaPlayer, (int) status.getStreamPosition() / 1000);
                MediaPlayerProfile.setVolume(mediaPlayer, status.getStreamVolume());
                MediaPlayerProfile.setMediaPlayer(intent, mediaPlayer);
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    /**
     * ステータスに基づいて、レスポンスする.
     * @param response レスポンス
     * @param result Chromecastからの状態
     * @param message Chromecastの状態
     */
    private void onChromeCastResult(final Intent response, final Status result, final String message) {
        if (result == null) {
            MessageUtils.setIllegalDeviceStateError(response, message);
        } else {
            if (result.isSuccess()) {
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            } else {
                if (message == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, message + " is error");
                }
            }
        }
        sendResponse(response);
    }
    @Override
    public void onChromeCastMediaPlayerResult(final Intent response,
                        final MediaChannelResult result, final String message) {
        if (result == null) {
            MessageUtils.setIllegalDeviceStateError(response, message);
            sendResponse(response);
        } else {
            onChromeCastResult(response, result.getStatus(), message);
        }
    }
    @Override
    public void onChromeCastMessageResult(final Intent response, final Status result, final String message) {
        onChromeCastResult(response, result, message);
    }

    @Override
    public void onCastDeviceUpdate(final ArrayList<CastDevice> devices) {
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "onCastDeviceUpdate#");
        }
        if (devices.size() == 0) {
            if (BuildConfig.DEBUG) {
                Log.d("TEST", "size:0");
            }
            ChromeCastApplication app = (ChromeCastApplication) getApplication();

            if (app.getController() != null) {
                app.getController().teardown();
            }
        }
    }


    @Override
    public void onCastDeviceSelected(final CastDevice selectedDevice) {
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "onCastDeviceSelected#" + selectedDevice.getDeviceId());
        }
        ChromeCastApplication app = (ChromeCastApplication) getApplication();
        if (app == null) {
            return;
        }

        CastDevice currentDevice = app.getController().getSelectedDevice();
        if (currentDevice != null) {
            DConnectService castService = getServiceProvider().getService(currentDevice.getDeviceId());
            if (castService == null) {
                castService = new ChromeCastDeviceService(currentDevice);
                getServiceProvider().addService(castService);
            }
            castService.setOnline(true);
            if (!currentDevice.getDeviceId().equals(selectedDevice.getDeviceId())) {
                app.getController().setSelectedDevice(selectedDevice);
                app.getController().reconnect();
            } else {
                app.getController().connect();
            }
        } else {
            app.getController().setSelectedDevice(selectedDevice);
            app.getController().connect();
        }
    }

    @Override
    public void onCastDeviceUnselected(final CastDevice unselectedDevice) {
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "onCastDeviceUnselected#start");
        }
        ChromeCastApplication app = (ChromeCastApplication) getApplication();

        if (app == null) {
            return;
        }
        CastDevice currentDevice = unselectedDevice;
        if (currentDevice == null) {
            if (app.getController().getSelectedDevice() == null) {
                return;
            }
            currentDevice = app.getController().getSelectedDevice();
        }
        if (BuildConfig.DEBUG) {
            Log.d("TEST", "onCastDeviceUnselected#start+ " + currentDevice.getDeviceId());
        }

        DConnectService castService = getServiceProvider().getService(currentDevice.getDeviceId());
        if (castService != null) {
            castService.setOnline(false);
        }
        app.getController().teardown();
    }


    @Override
    public synchronized void onChromeCastConnected() {
        ChromeCastApplication app = (ChromeCastApplication) getApplication();
        if (app != null) {
            CastDevice currentDevice = app.getController().getSelectedDevice();
            DConnectService castService = getServiceProvider().getService(currentDevice.getDeviceId());
            if (castService == null) {
                castService = new ChromeCastDeviceService(currentDevice);
                getServiceProvider().addService(castService);
            }
            castService.setOnline(true);
        }
    }



    /**
     * 選択されたChromeCastと接続する.
     * @param serviceId サービスID
     * @param callback 非同期処理用Callback
     */
    public synchronized void connectChromeCast(final String serviceId,
                                               final Callback callback) {
        ChromeCastApplication app = (ChromeCastApplication) getApplication();
        if (app == null) {
            callback.onResponse(false);
            return;
        }

        if (app.getDiscovery().getSelectedDevice() != null) {
            // Whether application that had been started before whether other apps
            try {
                GoogleApiClient client = app.getController().getGoogleApiClient();
                if (client == null || (!client.isConnected())) {
                    // Request in connection queuing
                    callback.onResponse(false);
                    return;
                }
                callback.onResponse(true);
            } catch (IllegalStateException e) {
                callback.onResponse(false);
            }
        }
    }
}
