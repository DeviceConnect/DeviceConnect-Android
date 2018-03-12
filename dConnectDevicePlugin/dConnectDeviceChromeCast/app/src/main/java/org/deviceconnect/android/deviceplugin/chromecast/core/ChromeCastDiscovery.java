/*
 ChromeCastDiscovery.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import org.deviceconnect.android.deviceplugin.chromecast.BuildConfig;

import java.util.ArrayList;

/**
 * Chromecast Discovery クラス.
 * 
 * <p>
 * ReceiverアプリのアプリケーションIDに対応した、Chromecastデバイスを探索する
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastDiscovery {

    /** 出力するログのタグ名. */
    private static final String TAG = ChromeCastDiscovery.class.getSimpleName();
    /** 選択されたデバイス. */
    private CastDevice mSelectedDevice;
    /** Chromecastとの接続状態を通知するコールバック. */
    private Callbacks mCallbacks;
    /** Route名のリスト. */
    private ArrayList<CastDevice> mRouteNames;

    /**
     * Chromecastとの接続状態を通知するコールバックのインターフェース.
     * @author NTT DOCOMO, INC.
     */
    public interface Callbacks {
        /**
         * Chromecastデバイスの探索状態（発見or消失した場合）を通知する.
         * @param   devices デバイス名のリスト
         */
        void onCastDeviceUpdate(final ArrayList<CastDevice> devices);
        /**
         * Chromecastデバイスが選択されたときに通知する.
         * 
         * @param selectedDevice 選択されたデバイス
         */
        void onCastDeviceSelected(final CastDevice selectedDevice);
        /**
         * Chromecastデバイスが非選択されたときに通知する.
         * @param unselectedDevice 選択されたデバイス
         */
        void onCastDeviceUnselected(final CastDevice unselectedDevice);
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
     * ChromeCastとの接続を管理する.
     */
    private CastContext mCastContext;
    /**
     * onEndingを通るかどうか.
     */
    private boolean mIsEnding;

    private SessionManagerListener<CastSession> mSessionListener = new SessionManagerListener<CastSession>() {

        @Override
        public void onSessionStarting(CastSession castSession) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionStarting");
                Log.d(TAG, "<================================");
            }
            synchronized (this) {
                CastDevice device = castSession.getCastDevice();
                if (device != null) {
                    mRouteNames.add(device);
                }
            }
            mCallbacks.onCastDeviceUpdate(mRouteNames);
        }

        @Override
        public void onSessionStarted(CastSession castSession, String s) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionStarted");
                Log.d(TAG, "<================================");
            }
            mSelectedDevice = castSession.getCastDevice();
            mCallbacks.onCastDeviceSelected(mSelectedDevice);
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int i) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionStartFailed");
                Log.d(TAG, "<================================");
            }
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionEnding");
                Log.d(TAG, "<================================");
            }
            mCallbacks.onCastDeviceUpdate(mRouteNames);
            mIsEnding = true;
        }

        @Override
        public void onSessionEnded(CastSession castSession, int i) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionEnded");
                Log.d(TAG, "<================================");
            }
            // WiFiをOFFにしたのちにChromeCastへリクエストを送ると、onSessionEndingが呼ばれずにこのメソッドが呼ばれる。
            if (mIsEnding) {
                mCallbacks.onCastDeviceUnselected(mSelectedDevice);
                mSelectedDevice = null;
            }
            mIsEnding = false;
        }

        @Override
        public void onSessionResuming(CastSession castSession, String s) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionResuming");
                Log.d(TAG, "<================================");
            }
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean b) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionResumed: " + b);
                Log.d(TAG, "<================================");
            }
            mSelectedDevice = castSession.getCastDevice();
            mCallbacks.onCastDeviceSelected(mSelectedDevice);
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int i) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionResumeFailed");
                Log.d(TAG, "<================================");
            }
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int i) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "================================>");
                Log.d(TAG, "SessionManagerListener.onSessionSuspended");
                Log.d(TAG, "<================================");
            }
            mCallbacks.onCastDeviceUnselected(mSelectedDevice);
        }
    };

    /**
     * コンストラクタ.
     * <p>
     * ReceiverアプリのアプリケーションIDに対応した、Chromecastデバイスの探索を開始する.
     * </p>
     * 
     * @param context コンテキスト
     */
    public ChromeCastDiscovery(final Context context) {
        mCastContext = CastContext.getSharedInstance(context);
        mIsEnding = false;
        setListener();
        mRouteNames = new ArrayList<>();
    }

    /**
     * Listenerを登録する.
     */
    private void setListener() {
        mCastContext.getSessionManager().removeSessionManagerListener(mSessionListener, CastSession.class);
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionListener, CastSession.class);
    }


    /**
     * 選択されたChromecastデバイスを返す.
     * 
     * @return 選択されたデバイス
     */
    public CastDevice getSelectedDevice() {
        return mSelectedDevice;
    }

    /**
     * 接続可能なChromecastデバイスのデバイス名のリストを返す.
     * 
     * @return デバイス名のリスト
     */
    public ArrayList<CastDevice> getDeviceNames() {
        return mRouteNames;
    }
}
