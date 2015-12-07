/*
 HostDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.camera.CameraOverlay;
import org.deviceconnect.android.deviceplugin.host.camera.MixedReplaceMediaServer;
import org.deviceconnect.android.deviceplugin.host.file.FileDataManager;
import org.deviceconnect.android.deviceplugin.host.manager.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.profile.HostBatteryProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostCanvasProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostConnectProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostFileDescriptorProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostFileProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostKeyEventProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostMediaPlayerProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostMediaStreamingRecordingProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostNotificationProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostPhoneProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostProximityProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostSettingsProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostSystemProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostTouchProfile;
import org.deviceconnect.android.deviceplugin.host.profile.HostVibrationProfile;
import org.deviceconnect.android.deviceplugin.host.video.VideoConst;
import org.deviceconnect.android.deviceplugin.host.video.VideoPlayer;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.PhoneProfileConstants.CallState;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Host Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceService extends DConnectMessageService {
    /** Application class instance. */
    private HostDeviceApplication mApp;

    /** マルチキャスト用のタグ. */
    private static final String HOST_MULTICAST = "deviceplugin.host";

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** ServiceID. */
    private String mServiceId;

    /** バッテリー関連の処理と値処理. */
    private HostBatteryManager mHostBatteryManager;

    /** ミリ秒 - 秒オーダー変換用. */
    private static final int UNIT_SEC = 1000;

    /** Video Current Position response. */
    private Intent mResponse = null;

    /** Intent filter for MediaPlayer (Video). */
    private IntentFilter mIfMediaPlayerVideo;

    /** Intent filter for battery charge event. */
    private IntentFilter mIfBatteryCharge;

    /** Intent filter for battery connect event. */
    private IntentFilter mIfBatteryConnect;

    /** ファイルデータ管理クラス. */
    private FileDataManager mFileDataManager;

    @Override
    public void onCreate() {

        super.onCreate();

        // Get application class instance.
        mApp = (HostDeviceApplication) this.getApplication();

        // EventManagerの初期化
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // ファイル管理クラスの作成
        mFileMgr = new FileManager(this);
        mFileDataManager = new FileDataManager(mFileMgr);

        // add supported profiles
        addProfile(new HostConnectProfile(BluetoothAdapter.getDefaultAdapter()));
        addProfile(new HostNotificationProfile());
        addProfile(new HostDeviceOrientationProfile());
        addProfile(new HostBatteryProfile());
        addProfile(new HostMediaStreamingRecordingProfile());
        addProfile(new HostPhoneProfile());
        addProfile(new HostSettingsProfile());
        addProfile(new HostMediaPlayerProfile());
        addProfile(new HostFileProfile(mFileMgr));
        addProfile(new HostFileDescriptorProfile(mFileDataManager));
        addProfile(new HostVibrationProfile());
        addProfile(new HostProximityProfile());
        addProfile(new HostCanvasProfile());
        addProfile(new HostTouchProfile());
        addProfile(new HostKeyEventProfile());

        // バッテリー関連の処理と値の保持
        mHostBatteryManager = new HostBatteryManager();
        mHostBatteryManager.getBatteryInfo(this.getContext());

        mIfBatteryCharge = new IntentFilter();
        mIfBatteryCharge.addAction(Intent.ACTION_BATTERY_CHANGED);
        mIfBatteryCharge.addAction(Intent.ACTION_BATTERY_LOW);
        mIfBatteryCharge.addAction(Intent.ACTION_BATTERY_OKAY);

        mIfBatteryConnect = new IntentFilter();
        mIfBatteryConnect.addAction(Intent.ACTION_POWER_CONNECTED);
        mIfBatteryConnect.addAction(Intent.ACTION_POWER_DISCONNECTED);

        // オーバーレイ
        mCameraOverlay = new CameraOverlay(this);
        mCameraOverlay.setFileManager(mFileMgr);

        // MediaPlayer (Video) IntentFilter.
        mIfMediaPlayerVideo = new IntentFilter();
        mIfMediaPlayerVideo.addAction(VideoConst.SEND_VIDEOPLAYER_TO_HOSTDP);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (CameraOverlay.DELETE_PREVIEW_ACTION.equals(action)) {
            if (mCameraOverlay.isShow()) {
                stopWebServer();
            }
            return START_STICKY;
        } else if ("android.intent.action.NEW_OUTGOING_CALL".equals(action)) {
            // Phone
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, HostPhoneProfile.PROFILE_NAME, null,
                    HostPhoneProfile.ATTRIBUTE_ON_CONNECT);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostPhoneProfile.setAttribute(mIntent, HostPhoneProfile.ATTRIBUTE_ON_CONNECT);
                Bundle phoneStatus = new Bundle();
                HostPhoneProfile.setPhoneNumber(phoneStatus, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
                HostPhoneProfile.setState(phoneStatus, CallState.START);
                HostPhoneProfile.setPhoneStatus(mIntent, phoneStatus);
                sendEvent(mIntent, event.getAccessToken());
            }
            return START_STICKY;
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)
                || WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            // Wifi
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, HostConnectProfile.PROFILE_NAME, null,
                    HostConnectProfile.ATTRIBUTE_ON_WIFI_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostConnectProfile.setAttribute(mIntent, HostConnectProfile.ATTRIBUTE_ON_WIFI_CHANGE);
                Bundle wifiConnecting = new Bundle();
                WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
                HostConnectProfile.setEnable(wifiConnecting, wifiMgr.isWifiEnabled());
                HostConnectProfile.setConnectStatus(mIntent, wifiConnecting);
                sendEvent(mIntent, event.getAccessToken());
            }
            return START_STICKY;
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, HostConnectProfile.PROFILE_NAME, null,
                    HostConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostConnectProfile.setAttribute(mIntent, HostConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
                Bundle bluetoothConnecting = new Bundle();
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                HostConnectProfile.setEnable(bluetoothConnecting, mBluetoothAdapter.isEnabled());
                HostConnectProfile.setConnectStatus(mIntent, bluetoothConnecting);
                sendEvent(mIntent, event.getAccessToken());
            }
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mFileDataManager.stopTimer();
    }

    /**
     * Get a instance of FileManager.
     *
     * @return FileManager
     */
    public FileManager getFileManager() {
        return mFileMgr;
    }

    /**
     * Register broadcast receiver for battery charge event.
     */
    public void registerBatteryChargeBroadcastReceiver() {
        registerReceiver(mBatteryChargeBR, mIfBatteryCharge);
    }

    /**
     * Unregister broadcast receiver for battery charge event.
     */
    public void unregisterBatteryChargeBroadcastReceiver() {
        unregisterReceiver(mBatteryChargeBR);
    }

    /**
     * Register broadcast receiver for battery connect event.
     */
    public void registerBatteryConnectBroadcastReceiver() {
        registerReceiver(mBatteryConnectBR, mIfBatteryConnect);
    }

    /**
     * Unregister broadcast receiver for battery connect event.
     */
    public void unregisterBatteryConnectBroadcastReceiver() {
        unregisterReceiver(mBatteryConnectBR);
    }

    /**
     * Broadcast receiver for battery charge event.
     */
    private BroadcastReceiver mBatteryChargeBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action) || Intent.ACTION_BATTERY_LOW.equals(action)
                    || Intent.ACTION_BATTERY_OKAY.equals(action)) {
                // バッテリーが変化した時
                mHostBatteryManager.setBatteryRequest(intent);
                List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, HostBatteryProfile.PROFILE_NAME,
                        null, HostBatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);

                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    Intent mIntent = EventManager.createEventMessage(event);
                    HostBatteryProfile.setAttribute(mIntent, HostBatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);
                    Bundle battery = new Bundle();
                    double level = ((double) (mHostBatteryManager.getBatteryLevel())) / ((double) getBatteryScale());
                    HostBatteryProfile.setLevel(battery, level);
                    HostBatteryProfile.setBattery(mIntent, battery);
                    sendEvent(mIntent, event.getAccessToken());
                }
            }
        }
    };

    /**
     * Broadcast receiver for battery connect event.
     */
    private BroadcastReceiver mBatteryConnectBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action) || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                // バッテリーが充電された時
                mHostBatteryManager.setBatteryRequest(intent);
                List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, HostBatteryProfile.PROFILE_NAME,
                        null, HostBatteryProfile.ATTRIBUTE_ON_CHARGING_CHANGE);

                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    Intent mIntent = EventManager.createEventMessage(event);
                    HostBatteryProfile.setAttribute(mIntent, HostBatteryProfile.ATTRIBUTE_ON_CHARGING_CHANGE);
                    Bundle charging = new Bundle();
                    if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                        HostBatteryProfile.setCharging(charging, true);
                    } else {
                        HostBatteryProfile.setCharging(charging, false);
                    }
                    HostBatteryProfile.setBattery(mIntent, charging);
                    sendEvent(mIntent, event.getAccessToken());
                }
            }
        }
    };

    @Override
    protected SystemProfile getSystemProfile() {
        return new HostSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) {
        };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HostServiceDiscoveryProfile(this);
    }

    /**
     * ServiceIDを設定.
     *
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    /**
     * Battery Profile<br>
     * バッテリーレベルを取得.
     *
     * @return バッテリーレベル
     */
    public int getBatteryLevel() {
        mHostBatteryManager.getBatteryInfo(this.getContext());
        return mHostBatteryManager.getBatteryLevel();
    }

    /**
     * Battery Profile<br>
     * バッテリーステータスを取得.
     *
     * @return バッテリーレベル
     */
    public int getBatteryStatus() {
        mHostBatteryManager.getBatteryInfo(this.getContext());
        return mHostBatteryManager.getBatteryStatus();
    }

    /**
     * Battery Profile<br>
     * バッテリーレベルを取得.
     *
     * @return バッテリーレベル
     */
    public int getBatteryScale() {
        mHostBatteryManager.getBatteryInfo(this.getContext());
        return mHostBatteryManager.getBatteryScale();
    }

    //
    // File Descriptor Profile
    //

    /**
     * ファイル操作管理クラスを取得する.
     *
     * @return FileDataManager
     */
    public FileDataManager getFileDataManager() {
        return mFileDataManager;
    }

    // ----------------------------------------------
    // MediaPlayer Profile
    // ----------------------------------------------
    /** MediaPlayerのインスタンス. */
    private MediaPlayer mMediaPlayer = null;
    /** Mediaのステータス. */
    private int mMediaStatus = 0;
    /** Mediaが未設定. */
    private static final int MEDIA_PLAYER_NODATA = 0;
    /** Mediaがセット. */
    private static final int MEDIA_PLAYER_SET = 1;
    /** Mediaが再生中. */
    private static final int MEDIA_PLAYER_PLAY = 2;
    /** Mediaが一時停止中. */
    private static final int MEDIA_PLAYER_PAUSE = 3;
    /** Mediaが停止. */
    private static final int MEDIA_PLAYER_STOP = 4;
    /** Mediaが再生完了. */
    private static final int MEDIA_PLAYER_COMPLETE = 5;
    /** MEDIAタイプ(動画). */
    private static final int MEDIA_TYPE_VIDEO = 1;
    /** MEDIAタイプ(音楽). */
    private static final int MEDIA_TYPE_MUSIC = 2;
    // /** MEDIAタイプ(音声). */
    // private static final int MEDIA_TYPE_AUDIO = 3;
    /** Media Status. */
    private int mSetMediaType = 0;
    /** onStatusChange Eventの状態. */
    private boolean mOnStatusChangeEventFlag = false;
    /** 現在再生中のファイルパス. */
    private String mMyCurrentFilePath = "";
    /** 現在再生中のファイルパス. */
    private String mMyCurrentFileMIMEType = "";
    /** 現在再生中のPosition. */
    private int mMyCurrentMediaPosition = 0;
    /** Backup MediaId. (Used in KITKAT more). */
    String mBackupMediaId;
    /** Media duration. */
    private int mMyCurrentMediaDuration = 0;

    /**
     * サポートしているaudioのタイプ一覧.
     */
    private static final List<String> AUDIO_TYPE_LIST = Arrays.asList("audio/mpeg", "audio/x-wav", "application/ogg",
            "audio/x-ms-wma", "audio/mp3", "audio/ogg", "audio/mp4");

    /**
     * サポートしているvideoのタイプ一覧.
     */
    private static final List<String> VIDEO_TYPE_LIST = Arrays.asList("video/3gpp", "video/mp4", "video/m4v",
            "video/3gpp2", "video/mpeg");

    /**
     * 再生するメディアをセットする(Idから).
     *
     * @param response レスポンス
     * @param mediaId MediaID
     */
    public void putMediaId(final Intent response, final String mediaId) {
        // Check status.
        if (mMediaStatus == MEDIA_PLAYER_PLAY || mMediaStatus == MEDIA_PLAYER_PAUSE) {
            MessageUtils.setIllegalDeviceStateError(response, "Status is playing.");
            sendResponse(response);
            return;
        }

        // Backup MediaId.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mBackupMediaId = mediaId;
        }

        // Videoとしてパスを取得
        Uri mUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));

        String filePath = getPathFromUri(mUri);

        // nullなら、Audioとしてパスを取得
        if (filePath == null) {
            mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));
            filePath = getPathFromUri(mUri);
        }

        String mMineType = getMIMEType(filePath);

        // パス指定の場合
        if (AUDIO_TYPE_LIST.contains(mMineType)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mMediaPlayer = new MediaPlayer();

            try {
                mSetMediaType = MEDIA_TYPE_MUSIC;
                mMyCurrentFilePath = filePath;
                mMyCurrentFileMIMEType = mMineType;
                mMediaStatus = MEDIA_PLAYER_SET;
                mMediaPlayer.setDataSource(filePath);
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(final MediaPlayer arg0) {
                        mMediaStatus = MEDIA_PLAYER_COMPLETE;
                        sendOnStatusChangeEvent("complete");
                    }
                });
                mMediaPlayer.prepareAsync();
                mMyCurrentMediaPosition = 0;
                mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(final MediaPlayer mp) {
                        mMyCurrentMediaDuration = mMediaPlayer.getDuration() / UNIT_SEC;
                    }
                });

                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    response.putExtra(DConnectMessage.EXTRA_VALUE, "regist:" + filePath);
                    sendOnStatusChangeEvent("media");
                    sendResponse(response);
                }
            } catch (IOException e) {
                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                    response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not mount:" + filePath);
                    sendResponse(response);
                }
            }
        } else if (VIDEO_TYPE_LIST.contains(mMineType)) {
            try {

                mSetMediaType = MEDIA_TYPE_VIDEO;
                mMyCurrentFilePath = filePath;
                mMyCurrentFileMIMEType = mMineType;

                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                mMediaPlayer = new MediaPlayer();
                FileInputStream fis = null;
                FileDescriptor mFd = null;

                fis = new FileInputStream(mMyCurrentFilePath);
                mFd = fis.getFD();

                mMediaPlayer.setDataSource(mFd);
                mMediaPlayer.prepare();
                mMyCurrentMediaDuration = mMediaPlayer.getDuration() / UNIT_SEC;
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
                fis.close();

                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    response.putExtra(DConnectMessage.EXTRA_VALUE, "regist:" + filePath);
                    sendOnStatusChangeEvent("media");
                    sendResponse(response);
                }
            } catch (IllegalArgumentException e) {
                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                    response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not mount:" + filePath);
                    sendResponse(response);
                }
            } catch (IllegalStateException e) {
                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                    response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not mount:" + filePath);
                    sendResponse(response);
                }
            } catch (IOException e) {
                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                    response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not mount:" + filePath);
                    sendResponse(response);
                }
            }
        } else {
            if (response != null) {
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                response.putExtra(DConnectMessage.EXTRA_VALUE, "can't not open:" + filePath);
                sendResponse(response);
            }
        }
    }

    /**
     * onStatusChange Eventの登録.
     *
     * @param response レスポンス
     * @param serviceId サービスID
     */
    public void registerOnStatusChange(final Intent response, final String serviceId) {
        mServiceId = serviceId;
        mOnStatusChangeEventFlag = true;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnStatusChange event");
        sendResponse(response);
    }

    /**
     * onStatusChange Eventの解除.
     *
     * @param response レスポンス
     */
    public void unregisterOnStatusChange(final Intent response) {
        mOnStatusChangeEventFlag = false;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister OnStatusChange event");
        sendResponse(response);
    }

    /**
     * 状態変化のイベントを通知.
     *
     * @param status ステータス
     */
    public void sendOnStatusChangeEvent(final String status) {

        if (mOnStatusChangeEventFlag) {
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, MediaPlayerProfile.PROFILE_NAME, null,
                    MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);

            AudioManager manager = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);

            double maxVolume = 1;
            double mVolume = 0;

            mVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            double mVolumeValue = mVolume / maxVolume;

            for (int i = 0; i < events.size(); i++) {

                Event event = events.get(i);
                Intent intent = EventManager.createEventMessage(event);

                MediaPlayerProfile.setAttribute(intent, MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);
                Bundle mediaPlayer = new Bundle();
                MediaPlayerProfile.setStatus(mediaPlayer, status);
                MediaPlayerProfile.setMediaId(mediaPlayer, mMyCurrentFilePath);
                MediaPlayerProfile.setMIMEType(mediaPlayer, mMyCurrentFileMIMEType);
                MediaPlayerProfile.setPos(mediaPlayer, mMyCurrentMediaPosition / UNIT_SEC);
                MediaPlayerProfile.setVolume(mediaPlayer, mVolumeValue);
                MediaPlayerProfile.setMediaPlayer(intent, mediaPlayer);
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    /**
     * URIからパスを取得.
     *
     * @param mUri URI
     * @return パス
     */
    private String getPathFromUri(final Uri mUri) {
        Cursor c = getContentResolver().query(mUri, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            String filename = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            c.close();
            return filename;
        } else {
            c.close();
            return null;
        }
    }

    /**
     * Mediaの再再生.
     *
     * @return SessionID
     */
    public int resumeMedia() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                mMediaPlayer.start();
            } catch (IllegalStateException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("play");
            return mMediaPlayer.getAudioSessionId();
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_PLAY;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_RESUME);
            getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("play");
            return 0;
        }
        return 0;
    }

    /**
     * メディアの再生.
     *
     * @return セッションID
     */
    public int playMedia() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                if (mMediaStatus == MEDIA_PLAYER_STOP) {
                    mMediaPlayer.prepare();
                }
                if (mMediaStatus == MEDIA_PLAYER_STOP || mMediaStatus == MEDIA_PLAYER_PAUSE
                        || mMediaStatus == MEDIA_PLAYER_PLAY) {
                    mMediaPlayer.seekTo(0);
                    mMyCurrentMediaPosition = 0;
                    if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                        return mMediaPlayer.getAudioSessionId();
                    }
                }
                mMediaPlayer.start();
                mMediaStatus = MEDIA_PLAYER_PLAY;
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalStateException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("play");
            return mMediaPlayer.getAudioSessionId();
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            registerReceiver(mMediaPlayerVideoBR, mIfMediaPlayerVideo);
            String className = getClassnameOfTopActivity();

            if (VideoPlayer.class.getName().equals(className)) {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY);
                getContext().sendBroadcast(mIntent);
                sendOnStatusChangeEvent("play");

            } else {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.setClass(getContext(), VideoPlayer.class);
                Uri data = Uri.parse(mMyCurrentFilePath);
                mIntent.setDataAndType(data, mMyCurrentFileMIMEType);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mIntent);
                sendOnStatusChangeEvent("play");
            }

            return 0;
        } else {
            return 0;
        }
    }

    /**
     * メディアの一時停止.
     *
     * @return セッションID
     */
    public int pauseMedia() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC && mMediaStatus != MEDIA_PLAYER_STOP
                && mMediaStatus != MEDIA_PLAYER_SET) {
            try {
                mMediaStatus = MEDIA_PLAYER_PAUSE;
                mMediaPlayer.pause();
            } catch (IllegalStateException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("pause");
            return mMediaPlayer.getAudioSessionId();

        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_PAUSE;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PAUSE);
            getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("pause");
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * ポジションを返す.
     *
     * @return 現在のポジション
     */
    public int getMediaPos() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            return mMediaPlayer.getCurrentPosition() / UNIT_SEC;
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            String className = getClassnameOfTopActivity();
            if (VideoPlayer.class.getName().equals(className)) {
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_GET_POS);
                getContext().sendBroadcast(mIntent);
                return Integer.MAX_VALUE;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Video ポジションを返す為のIntentを設定.
     *
     * @param response 応答用Intent.
     */
    public void setVideoMediaPosRes(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mResponse = response;
        }
    }

    /**
     * VideoPlayer用Broadcast Receiver.
     */
    private BroadcastReceiver mMediaPlayerVideoBR = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(VideoConst.SEND_VIDEOPLAYER_TO_HOSTDP)) {
                String mVideoAction = intent.getStringExtra(VideoConst.EXTRA_NAME);

                if (mVideoAction.equals(VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY_POS)) {
                    mMyCurrentMediaPosition = intent.getIntExtra("pos", 0);
                    mResponse.putExtra("pos", mMyCurrentMediaPosition / UNIT_SEC);
                    mResponse.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    sendResponse(mResponse);
                } else if (mVideoAction.equals(VideoConst.EXTRA_VALUE_VIDEO_PLAYER_STOP)) {
                    unregisterReceiver(mMediaPlayerVideoBR);
                } else if (mVideoAction.equals(VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY_COMPLETION)) {
                    mMediaStatus = MEDIA_PLAYER_COMPLETE;
                    sendOnStatusChangeEvent("complete");
                    unregisterReceiver(mMediaPlayerVideoBR);
                }
            }
        }
    };

    /**
     * ポジションを変える.
     *
     * @param response レスポンス
     * @param pos ポジション
     */
    public void setMediaPos(final Intent response, final int pos) {
        if (pos > mMyCurrentMediaDuration) {
            MessageUtils.setInvalidRequestParameterError(response);
            sendResponse(response);
            return;
        }

        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            mMediaPlayer.seekTo(pos * UNIT_SEC);
            mMyCurrentMediaPosition = pos * UNIT_SEC;
        } else {
            mMediaStatus = MEDIA_PLAYER_PAUSE;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_SEEK);
            mIntent.putExtra("pos", pos * UNIT_SEC);
            getContext().sendBroadcast(mIntent);
            mMyCurrentMediaPosition = pos * UNIT_SEC;
        }
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        sendResponse(response);
    }

    /**
     * メディアの停止.
     *
     * @param response レスポンス
     */
    public void stopMedia(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                mMediaPlayer.stop();
                mMediaStatus = MEDIA_PLAYER_STOP;
                sendOnStatusChangeEvent("stop");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mMediaPlayer.reset();
                    putMediaId(null, mBackupMediaId);
                }
            } catch (IllegalStateException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            sendResponse(response);
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_STOP;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_STOP);
            getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("stop");
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            sendResponse(response);
        }
    }

    /**
     * Play Status.
     *
     * @param response レスポンス
     */
    public void getPlayStatus(final Intent response) {
        String mClassName = getClassnameOfTopActivity();

        // VideoRecorderの場合は、画面から消えている場合
        if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);

            if (!VideoPlayer.class.getName().equals(mClassName)) {
                mMediaStatus = MEDIA_PLAYER_STOP;
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            } else {
                if (mMediaStatus == MEDIA_PLAYER_STOP) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
                } else if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "play");
                } else if (mMediaStatus == MEDIA_PLAYER_PAUSE) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "pause");
                } else if (mMediaStatus == MEDIA_PLAYER_NODATA) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "no data");
                } else {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
                }
            }
            sendResponse(response);
        } else {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            if (mMediaStatus == MEDIA_PLAYER_STOP) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            } else if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "play");
            } else if (mMediaStatus == MEDIA_PLAYER_PAUSE) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "pause");
            } else if (mMediaStatus == MEDIA_PLAYER_NODATA) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "no data");
            } else {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            }
            sendResponse(response);
        }
    }

    // ================================
    // MediaStream_Recording
    // ================================

    /** Lock object. */
    private final Object mLockObj = new Object();

    /** Server for MotionJPEG. */
    private MixedReplaceMediaServer mServer;

    /** カメラを表示するためのオーバーレイ. */
    private CameraOverlay mCameraOverlay;

    /**
     * カメラが使用されているか確認する.
     *
     * @return カメラが使用されている場合はtrue、それ以外はfalse
     */
    public boolean isShowCamera() {
        return mCameraOverlay != null && mCameraOverlay.isShow();
    }

    /**
     * Start a web server.
     *
     * @param callback a callback to return the result.
     */
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpg");
                final String ip = mServer.start();

                if (!mCameraOverlay.isShow()) {
                    mCameraOverlay.show(new CameraOverlay.Callback() {
                        @Override
                        public void onSuccess() {
                            mCameraOverlay.setFinishFlag(false);
                            mCameraOverlay.setServer(mServer);
                            callback.onStart(ip);
                        }

                        @Override
                        public void onFail() {
                            callback.onFail();
                        }
                    });
                } else {
                    mCameraOverlay.setFinishFlag(false);
                    mCameraOverlay.setServer(mServer);
                    callback.onStart(ip);
                }
            } else {
                callback.onStart(mServer.getUrl());
            }
        }
    }

    /**
     * Stop a web server.
     */
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            mCameraOverlay.hide();
        }
    }

    /**
     * 写真撮影を行う.
     *
     * @param listener 写真撮影の結果を通知するリスナー
     */
    public void takePicture(final CameraOverlay.OnTakePhotoListener listener) {
        PermissionUtility.requestPermissions(this, new Handler(Looper.getMainLooper()),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        if (!mCameraOverlay.isShow()) {
                            mCameraOverlay.show(new CameraOverlay.Callback() {
                                @Override
                                public void onSuccess() {
                                    mCameraOverlay.setFinishFlag(true);
                                    mCameraOverlay.takePicture(listener);
                                }

                                @Override
                                public void onFail() {
                                    listener.onFailedTakePhoto();
                                }
                            });
                        } else {
                            mCameraOverlay.takePicture(listener);
                        }
                    }

                    @Override
                    public void onFail(@NonNull String deniedPermission) {
                        listener.onFailedTakePhoto();
                    }
                });
    }

    /**
     * mDNSで端末検索.
     */
    private void searchDeviceByBonjour() {
        // cacheがfalseの場合は、検索開始
        // 初回検索,すでにデバイスがある場合, Wifi接続のBroadcastがある場合は入る
        new Thread(new Runnable() {
            public void run() {

                android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(
                        android.content.Context.WIFI_SERVICE);
                WifiManager.MulticastLock lock = wifi.createMulticastLock(HOST_MULTICAST);
                lock.setReferenceCounted(true);
                lock.acquire();
            }
        }).start();

    }

    /**
     * mDNSで引っかかるように端末を起動.
     */
    private void invokeDeviceByBonjour() {
        // cacheがfalseの場合は、検索開始
        // 初回検索,すでにデバイスがある場合, Wifi接続のBroadcastがある場合は入る
        new Thread(new Runnable() {
            public void run() {

                android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(
                        android.content.Context.WIFI_SERVICE);
                WifiManager.MulticastLock lock = wifi.createMulticastLock(HOST_MULTICAST);
                lock.setReferenceCounted(true);
                lock.acquire();
            }
        }).start();

    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    /**
     * onClickの登録.
     *
     * @param response レスポンス
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     */
    public void registerOnConnect(final Intent response, final String serviceId, final String sessionKey) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register onClick event");
        sendResponse(response);
    }

    /**
     * onClickの削除.
     *
     * @param response レスポンス
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     */
    public void unregisterOnConnect(final Intent response, final String serviceId, final String sessionKey) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Unregister onClick event");
        sendResponse(response);
    }

    /**
     * ファイルからMIME Typeを取得.
     *
     * @param path パス
     * @return MineType
     */
    private String getMIMEType(final String path) {
        // 空文字, 日本語対策, ファイル形式のStringを取得
        String mFilename = new File(path).getName();
        int dotPos = mFilename.lastIndexOf(".");
        String mFormat = mFilename.substring(dotPos, mFilename.length());
        // 拡張子を取得
        String mExt = MimeTypeMap.getFileExtensionFromUrl(mFormat);
        // 小文字に変換
        mExt = mExt.toLowerCase(Locale.getDefault());
        // MIME Typeを返す
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExt);
    }

    /**
     * 画面の一番上にでているActivityのクラス名を取得.
     *
     * @return クラス名
     */
    private String getClassnameOfTopActivity() {
        ActivityManager mActivityManager = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String mClassName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return mClassName;
    }

    /**
     * Get touch cache.
     *
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public Bundle getTouchCache(final String attr) {
        return mApp.getTouchCache(attr);
    }

    /**
     * Get keyevent cache.
     *
     * @param attr Attribute.
     * @return KeyEvent cache data.
     */
    public Bundle getKeyEventCache(final String attr) {
        return mApp.getKeyEventCache(attr);
    }

    /**
     * Callback interface used to receive the result of starting a web server.
     */
    public interface OnWebServerStartCallback {
        /**
         * Called when a web server successfully started.
         *
         * @param uri An ever-updating, static image URI.
         */
        void onStart(@NonNull String uri);

        /**
         * Called when a web server failed to start.
         */
        void onFail();
    }
}
