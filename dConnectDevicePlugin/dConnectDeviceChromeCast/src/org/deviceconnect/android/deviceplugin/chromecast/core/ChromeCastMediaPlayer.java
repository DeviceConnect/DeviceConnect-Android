/*
 ChromeCastMediaPlayer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;

import android.content.Intent;
import android.webkit.MimeTypeMap;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.ResultCallback;

import org.deviceconnect.android.deviceplugin.chromecast.BuildConfig;

import java.io.IOException;
import java.util.Locale;

/**
 * Chromecast MediaPlayer クラス.
 * 
 * <p>
 * MediaPlayer機能を提供する
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastMediaPlayer implements ChromeCastApplication.Callbacks {

    /** Chromecast Application. */
    private ChromeCastApplication mApplication;
    /** RemoteMediaPlayer. */
    private RemoteMediaPlayer mRemoteMediaPlayer;
    /** Chromecastの再生状態を受け取るコールバック. */
    private Callbacks mCallbacks;
    /** Chromecastのファイルロード有効フラグ. */
    private boolean mIsLoadEnable = false;

    /**
     * Chromecastの再生状態を通知するためのコールバックのインターフェース.
     * @author NTT DOCOMO, INC.
     */
    public interface Callbacks {
        /**
         * 再生状態を通知する.
         * 
         * @param status メディアのステータス
         */
        void onChromeCastMediaPlayerStatusUpdate(final MediaStatus status);
        /**
         * 再生処理の結果を通知する.
         * 
         * @param   response レスポンス
         * @param   result 再生処理結果
         * @param   message 再生処理のステータス
         */
        void onChromeCastMediaPlayerResult(final Intent response,
                final MediaChannelResult result, final String message);
    }

    /**
     * コールバックを登録する.
     * 
     * @param callbacks コールバック
     */
    public void setCallbacks(final Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    /**
     * コンストラクタ.
     * 
     * @param application ChromeCastApplication
     */
    public ChromeCastMediaPlayer(final ChromeCastApplication application) {
        this.mApplication = application;
        this.mApplication.addCallbacks(this);
    }

    /**
     * デバイスが有効か否かを返す.
     * @return デバイスが有効か否か（有効: true, 無効: false）
     */
    public boolean isDeviceEnable() {
        return (mApplication.getGoogleApiClient() != null);
    }

    @Override
    public void onAttach() {
        mRemoteMediaPlayer = new RemoteMediaPlayer();

        mRemoteMediaPlayer
                .setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
                    @Override
                    public void onStatusUpdated() {
                        if (mRemoteMediaPlayer.getMediaStatus() == null) {
                            return;
                        }
                        mCallbacks.onChromeCastMediaPlayerStatusUpdate(mRemoteMediaPlayer.getMediaStatus());
                    }
                });

        mRemoteMediaPlayer
                .setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
                    @Override
                    public void onMetadataUpdated() {
                        
                    }
                });

        try {
            Cast.CastApi.setMessageReceivedCallbacks(mApplication.getGoogleApiClient(),
                    mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDetach() {
        if (mRemoteMediaPlayer != null) {
            try {
                Cast.CastApi.removeMessageReceivedCallbacks(mApplication.getGoogleApiClient(),
                        mRemoteMediaPlayer.getNamespace());
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            mRemoteMediaPlayer = null;
        }
    }

    /**
     * メディアをロードする.
     * 
     * @param   response    レスポンス
     * @param   url         メディアのURL
     * @param   title       メディアのタイトル
     */
    public void load(final Intent response, final String url, final String title) {
        MediaInfo mediaInfo;
        
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        String ext = MimeTypeMap.getFileExtensionFromUrl(url).toLowerCase(Locale.getDefault());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        if (mimeType == null || (mimeType != null && mimeType.isEmpty())) {
            mimeType = "application/octet-stream";
        }
        mediaInfo = new MediaInfo.Builder(url).setContentType(mimeType)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata).build();
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer
                .load(mApplication.getGoogleApiClient(), mediaInfo, false)
                .setResultCallback(
                        new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(final MediaChannelResult result) {
                                if (result.getStatus().isSuccess()) {
                                    mIsLoadEnable = true;
                                } else {
                                    mIsLoadEnable = false;
                                }
                                mCallbacks.onChromeCastMediaPlayerResult(response, result, "load");
                            }
                        });
        }
    }

    /**
     * メディアをプレイする.
     * 
     * @param   response    レスポンス
     */
    public void play(final Intent response) {
        MediaInfo mediaInfo = null;
        if (mIsLoadEnable) {
            mediaInfo = mRemoteMediaPlayer.getMediaInfo();
        }
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer.load(mApplication.getGoogleApiClient(), mediaInfo, true).setResultCallback(
                new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                    @Override
                    public void onResult(final MediaChannelResult result) {
                        mCallbacks.onChromeCastMediaPlayerResult(response, result, "load");
                    }
                });
        }
    }

    /**
     * メディアをレジュームする.
     * 
     * @param   response    レスポンス
     */
    public void resume(final Intent response) {
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer.play(mApplication.getGoogleApiClient()).setResultCallback(
                    new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(final MediaChannelResult result) {
                            mCallbacks.onChromeCastMediaPlayerResult(response, result,
                                    null);
                        }
                    });
        }
    }

    /**
     * メディアを停止する.
     * 
     * @param   response    レスポンス
     */
    public void stop(final Intent response) {
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer.stop(mApplication.getGoogleApiClient()).setResultCallback(
                    new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(final MediaChannelResult result) {
                            mCallbacks.onChromeCastMediaPlayerResult(response, result,
                                    null);
                        }
                    });
        }
    }

    /**
     * メディアを一時停止する.
     * 
     * @param   response    レスポンス
     */
    public void pause(final Intent response) {
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer.pause(mApplication.getGoogleApiClient()).setResultCallback(
                    new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(final MediaChannelResult result) {
                            mCallbacks.onChromeCastMediaPlayerResult(response, result,
                                    null);
                        }
                    });
        }
     }

    /**
     * メディアをミュートする.
     * 
     * @param   response    レスポンス
     * @param   mute        ミュートするか否か (true: ミュートON, false: ミュートOFF)
     */
    public void setMute(final Intent response, final boolean mute) {
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer
                .setStreamMute(mApplication.getGoogleApiClient(), mute)
                .setResultCallback(
                        new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(final MediaChannelResult result) {
                                mCallbacks.onChromeCastMediaPlayerResult(response, result,
                                        null);
                            }
                        });
        }
    }

    /**
     * メディアのミュートの状態を取得する.
     * 
     * @param   response    レスポンス
     * @return  ミュート状態 (1:ミュートON, 0: ミュートOFF)
     */
    public int getMute(final Intent response) {
        if (mRemoteMediaPlayer == null) {
            return -1;
        }
        if (mRemoteMediaPlayer.getMediaStatus().isMute()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * メディアのボリュームを設定する.
     * 
     * @param   response    レスポンス
     * @param   volume      ボリューム (0.0 <= volume <= 1.0)
     */
    public void setVolume(final Intent response, final double volume) {
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer
                .setStreamVolume(mApplication.getGoogleApiClient(), volume)
                .setResultCallback(
                        new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(final MediaChannelResult result) {
                                mCallbacks.onChromeCastMediaPlayerResult(response, result,
                                        null);
                            }
                        });
        }
    }
	
    /**
     * メディアのボリュームを取得する.
     * 
     * @param   response    レスポンス
     * @return  volume      ボリューム (0 <= volume <= 1.0, -1: エラー)
     */
    public double getVolume(final Intent response) {
        if (mRemoteMediaPlayer != null) {
            return mRemoteMediaPlayer.getMediaStatus().getStreamVolume();
        } else {
            return -1;
        }
    }

    /**
     * メディアのシークを設定する.
     * 
     * @param   response    レスポンス
     * @param   pos         ポジション
     */
    public void setSeek(final Intent response, final long pos) {
        if (mRemoteMediaPlayer != null) {
            mRemoteMediaPlayer
                .seek(mApplication.getGoogleApiClient(), pos)
                .setResultCallback(
                        new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(final MediaChannelResult result) {
                                mCallbacks.onChromeCastMediaPlayerResult(response, result,
                                        null);
                            }
                        });
        }
    }

    /**
     * メディアのシークを取得する.
     * 
     * @param   response    レスポンス
     * @return  pos         ポジション (-1: エラー)
     */
    public long getSeek(final Intent response) {
        if (mRemoteMediaPlayer != null) {
            return  mRemoteMediaPlayer.getApproximateStreamPosition();
        } else {
            return -1;
        }
    }

    /**
     * メディアの状態を取得する.
     * 
     * @return  status  メディアの状態
     */
    public MediaStatus getMediaStatus() {
        if (mRemoteMediaPlayer != null) {
            return mRemoteMediaPlayer.getMediaStatus();
        } else {
            return null;
        }
    }
}
