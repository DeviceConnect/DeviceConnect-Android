/*
 ChromeCastMessage.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;

import android.content.Intent;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.deviceconnect.android.deviceplugin.chromecast.BuildConfig;

/**
 * Chromecast Message クラス.
 * 
 * <p>
 * メッセージ機能を提供する
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastMessage implements ChromeCastController.Callbacks {

    /** Message Channel. */
    private MessageChannel mMessageChannel;
    /** Chromecast Application. */
    private ChromeCastController mApplication;
    /** メッセージの宛先. */
    private String mUrn = null;
    /** メッセージのコールバック. */
    private Callbacks mCallbacks;

    /**
     * Chromecastでのメッセージの受信状況を通知する.
     *
     * @author NTT DOCOMO, INC.
     *
     */
    private class MessageChannel implements MessageReceivedCallback {

        /**
         * メッセージの宛先.
         */
        private String mUrn = null;

        /**
         * コンストラクタ.
         * 
         * @param urn メッセージの宛先(名前空間)
         */
        public MessageChannel(final String urn) {
            this.mUrn = urn;
        }

        /**
         * メッセージの宛先(名前空間)を取得する.
         * 
         * @return 名前空間
         */
        public String getNamespace() {
            return mUrn;
        }

        @Override
        public void onMessageReceived(final CastDevice castDevice, final String namespace, final String message) {
        }
    }

    /**
     * Chromecastでのメッセージ処理の結果を通知するコールバックのインターフェース.
     * 
     * @author NTT DOCOMO, INC.
     */
    public interface Callbacks {
        /**
         * メッセージ処理の結果を通知する.
         * 
         * @param response レスポンス
         * @param result メッセージ処理結果
         * @param message メッセージ処理のステータス
         */
        void onChromeCastMessageResult(final Intent response, final Status result, final String message);
    }

    /**
     * コールバックを登録する.
     * 
     * @param   callbacks   コールバック
     */
    public void setCallbacks(final Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    /**
     * コンストラクタ.
     * 
     * @param   application     ChromeCastApplication
     * @param   urn             メッセージの宛先(名前空間)
     */
    public ChromeCastMessage(final ChromeCastController application, final String urn) {
        this.mApplication = application;
        this.mApplication.addCallbacks(this);
        this.mUrn = urn;
    }

    /**
     * デバイスが有効か否かを返す.
     * 
     * @return  デバイスが有効か否か（有効: true, 無効: false）
     */
    public boolean isDeviceEnable() {
        try {
            return (mApplication.getGoogleApiClient() != null);
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
    }
    @Override
    public void onAttach() {
        mMessageChannel = new MessageChannel(this.mUrn);
        try {
            Cast.CastApi.setMessageReceivedCallbacks(mApplication.getGoogleApiClient(),
                    mMessageChannel.getNamespace(), mMessageChannel);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDetach() {
        if (mMessageChannel != null) {
            try {
                Cast.CastApi.removeMessageReceivedCallbacks(mApplication.getGoogleApiClient(),
                        mMessageChannel.getNamespace());
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            mMessageChannel = null;
        }
    }

    /**
     * メッセージを送信する.
     * 
     * @param   response    レスポンス
     * @param   message     メッセージ
     */
    public void sendMessage(final Intent response, final String message) {
        try {
            if (mApplication.getGoogleApiClient() != null && mMessageChannel != null) {
                Cast.CastApi.sendMessage(mApplication.getGoogleApiClient(),
                        mMessageChannel.getNamespace(), message)
                        .setResultCallback((result) -> {
                            mCallbacks.onChromeCastMessageResult(response, result, null);
                        });
            }
        } catch (Exception e) {
            mCallbacks.onChromeCastMessageResult(response, null, null);
        }
    }

}
