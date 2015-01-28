/*
 ChromeCastService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastApplication;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastHttpServer;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMediaPlayer;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMessage;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastMediaPlayerProfile;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastNotificationProfile;
import org.deviceconnect.android.deviceplugin.chromecast.profile.ChromeCastSystemProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.db.DBCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.Status;

/**
 * メッセージサービス (Chromecast).
 * <p>
 * Chromecastデバイスプラグインのサービス
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastService extends DConnectMessageService implements
        ChromeCastDiscovery.Callbacks,
        ChromeCastMediaPlayer.Callbacks,
        ChromeCastMessage.Callbacks {
    /**
     * Chromecastのサーバポート.
     */
    private static final int SERVER_PORT = 38088;

    /** Chromecast Discovery. */
    private ChromeCastDiscovery mDiscovery;
    /** Chromecast Application. */
    private ChromeCastApplication mApplication;
    /** Chromecast MediaPlayer. */
    private ChromeCastMediaPlayer mMediaPlayer;
    /** Chromecast Message. */
    private ChromeCastMessage mMessage;
    /** ChromecastHttpServer. */
    private ChromeCastHttpServer mServer;
    /** ChromecastMediaPlayerProfile. */
    private ChromeCastMediaPlayerProfile mMediaPlayerProfile;
    /** StatusChange時のServiceId. */
    private String mServiceIdOnStatusChange = null;
    /** StatusChange時のSessionKey. */
    private String mSessionKeyOnStatusChange = null;
    /** MediaPlayerのステータスアップデートフラグ. */
    private boolean mEnableCastMediaPlayerStatusUpdate = false;

    @Override
    public void onCreate() {
        super.onCreate();
        
        String appId = getString(R.string.application_id);
        String appMsgUrn = getString(R.string.application_message_urn);

        try {
            mServer = new ChromeCastHttpServer("0.0.0.0", SERVER_PORT);
            mServer.start();
        } catch (Exception e) {
            e.getStackTrace();
        }
        
        mDiscovery = new ChromeCastDiscovery(this, appId);
        mDiscovery.setCallbacks(this);
        mDiscovery.registerEvent();
        mApplication = new ChromeCastApplication(this, appId);
        mMediaPlayer = new ChromeCastMediaPlayer(mApplication);
        mMediaPlayer.setCallbacks(this);
        mMessage = new ChromeCastMessage(mApplication, appMsgUrn);
        mMessage.setCallbacks(this);

        EventManager.INSTANCE.setController(new DBCacheController(this));
        addProfile(new ChromeCastServiceDiscoveryProfile());
        addProfile(new ChromeCastNotificationProfile());
        mMediaPlayerProfile = new ChromeCastMediaPlayerProfile();
        addProfile(mMediaPlayerProfile);
    }
	
    @Override
    public void onDestroy() {
        mServer.stop();
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new ChromeCastSystemProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new ChromeCastServiceDiscoveryProfile();
    }

    @Override
    public void onCastDeviceUpdate(final ArrayList<String> devices) { 
        
    }

    @Override
    public void onCastDeviceSelected(final CastDevice selectedDevice) {
        CastDevice currentDevice = mApplication.getSelectedDevice();
        if (currentDevice != null) {
            if (!currentDevice.getDeviceId().equals(selectedDevice.getDeviceId())) {
                mApplication.setSelectedDevice(selectedDevice);
                mApplication.reconnect();
            } else {
                mApplication.connect();
            }
        } else {
            mApplication.setSelectedDevice(selectedDevice);
            mApplication.connect();
        }
    }

    @Override
    public void onCastDeviceUnselected() {
        mApplication.reconnect();
    }
    
    /**
     * ChromeCastDiscoveryを返す.
     * @return  ChromeCastDiscovery
     */
    public ChromeCastDiscovery getChromeCastDiscovery() {
        return mDiscovery;
    }
    /**
     * ChromeCastApplicationを返す.
     * @return  ChromeCastApplication
     */
    public ChromeCastApplication getChromeCastApplication() {
        return mApplication;
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
     * @param sessionKey イベントを識別するKey
     */
    public void registerOnStatusChange(final Intent response, final String serviceId, final String sessionKey) {
        mServiceIdOnStatusChange = serviceId;
        mSessionKeyOnStatusChange = sessionKey;
        mEnableCastMediaPlayerStatusUpdate = true;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnStatusChange event");
        sendBroadcast(response);
    }
	
    /**
     * StatusChange通知を無効にする.
     * 
     * @param response レスポンス
     */
    public void unregisterOnStatusChange(final Intent response) {
        mServiceIdOnStatusChange = null;
        mSessionKeyOnStatusChange = null;
        mEnableCastMediaPlayerStatusUpdate = false;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister OnStatusChange event");
        sendBroadcast(response);
    }

    @Override
    public void onChromeCastMediaPlayerStatusUpdate(final MediaStatus status) {
        MediaInfo info = status.getMediaInfo();
        String playStatusString = mMediaPlayerProfile.getPlayStatus(status.getPlayerState());

        if (mEnableCastMediaPlayerStatusUpdate) {
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceIdOnStatusChange, 
                    MediaPlayerProfile.PROFILE_NAME, null,
                    MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                if (event.getSessionKey().equals(mSessionKeyOnStatusChange)) {
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
                    getContext().sendBroadcast(intent);
                }
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
            sendBroadcast(response);
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
            sendBroadcast(response);
        }
    }
    @Override
    public void onChromeCastMediaPlayerResult(final Intent response,
                        final MediaChannelResult result, final String message) {
        if (result == null) {
            MessageUtils.setIllegalDeviceStateError(response, message);
            sendBroadcast(response);
        } else {
            onChromeCastResult(response, result.getStatus(), message);
        }
    }
    @Override
    public void onChromeCastMessageResult(final Intent response, final Status result, final String message) {
        onChromeCastResult(response, result, message);
    }
}
