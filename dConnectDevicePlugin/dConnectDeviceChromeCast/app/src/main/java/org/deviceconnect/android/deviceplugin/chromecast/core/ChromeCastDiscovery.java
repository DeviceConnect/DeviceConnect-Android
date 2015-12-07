/*
 ChromeCastDiscovery.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;

import android.content.Context;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;

import org.deviceconnect.android.deviceplugin.chromecast.BuildConfig;

import java.util.ArrayList;
import java.util.List;

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
    /** MediaRouter. */
    private MediaRouter mMediaRouter;
    /** MediaRouteSelector. */
    private MediaRouteSelector mMediaRouteSelector;
    /** MediaRouterのコールバック. */
    private MediaRouter.Callback mMediaRouterCallback;
    /** 選択されたデバイス. */
    private CastDevice mSelectedDevice;
    /** Chromecastとの接続状態を通知するコールバック. */
    private Callbacks mCallbacks;
    /** Route名のリスト. */
    private ArrayList<String> mRouteNames;
    /** Routeの情報のリスト. */
    private ArrayList<MediaRouter.RouteInfo> mRouteInfos;

    /**
     * Chromecastとの接続状態を通知するコールバックのインターフェース.
     * @author NTT DOCOMO, INC.
     */
    public interface Callbacks {
        /**
         * Chromecastデバイスの探索状態（発見or消失した場合）を通知する.
         * @param   devices デバイス名のリスト
         */
        void onCastDeviceUpdate(final ArrayList<String> devices);
        /**
         * Chromecastデバイスが選択されたときに通知する.
         * 
         * @param selectedDevice 選択されたデバイス
         */
        void onCastDeviceSelected(final CastDevice selectedDevice);
        /**
         * Chromecastデバイスが非選択されたときに通知する.
         * 
         */
        void onCastDeviceUnselected();
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
     * <p>
     * ReceiverアプリのアプリケーションIDに対応した、Chromecastデバイスの探索を開始する.
     * </p>
     * 
     * @param context コンテキスト
     * @param appId   ReceiverアプリのアプリケーションID
     */
    public ChromeCastDiscovery(final Context context, final String appId) {

        mRouteNames = new ArrayList<String>();
        mRouteInfos = new ArrayList<MediaRouter.RouteInfo>();

        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(appId)).build();

        mMediaRouterCallback = new MediaRouter.Callback() {
            @Override
            public void onRouteAdded(final MediaRouter router, final MediaRouter.RouteInfo info) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MediaRouter.Callback$onRouteAdded");
                    Log.d(TAG, "MediaRouter.Callback$onRouteAdded: " + info.toString());
                }
                synchronized (this) {
                    CastDevice device = CastDevice.getFromBundle(info.getExtras());
                    if (device != null && device.isOnLocalNetwork()) {
                        mRouteInfos.add(info);
                        mRouteNames.add(info.getName());
                    }
                }
                mCallbacks.onCastDeviceUpdate(mRouteNames);
            }

            @Override
            public void onRouteRemoved(final MediaRouter router, final MediaRouter.RouteInfo info) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MediaRouter.Callback$onRouteRemoved");
                    Log.d(TAG, "MediaRouter.Callback$onRouteRemoved: " + info.toString());
                }
                for (int i = 0; i < mRouteInfos.size(); i++) {
                    MediaRouter.RouteInfo routeInfo = mRouteInfos.get(i);
                    if (routeInfo.equals(info)) {
                        synchronized (this) {
                            mRouteInfos.remove(i);
                            mRouteNames.remove(i);
                        }
                        mCallbacks.onCastDeviceUpdate(mRouteNames);
                        break;
                    }
                }
            }

            @Override
            public void onRouteSelected(final MediaRouter router, final MediaRouter.RouteInfo info) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MediaRouter.Callback$onRouteSelected");
                    Log.d(TAG, "MediaRouter.Callback$onRouteSelected: " + info.toString());
                }
                mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
                mCallbacks.onCastDeviceSelected(mSelectedDevice);
            }

            @Override
            public void onRouteUnselected(final MediaRouter router, final MediaRouter.RouteInfo info) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MediaRouter.Callback$onRouteUnselected");
                    Log.d(TAG, "MediaRouter.Callback$onRouteUnselected: " + info.toString());
                }
                mCallbacks.onCastDeviceUnselected();
                mSelectedDevice = null;
            }

            @Override
            public void onRouteChanged(final MediaRouter router, final MediaRouter.RouteInfo info) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MediaRouter.onRouteChanged");
                    Log.d(TAG, "MediaRouter.onRouteChanged: " + info.toString());
                }
            }
            @Override
            public void onRoutePresentationDisplayChanged(final MediaRouter router, final MediaRouter.RouteInfo info) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MediaRouter.onRoutePresentationDisplayChanged");
                    Log.d(TAG, "MediaRouter.onRoutePresentationDisplayChanged: " + info.toString());
                }
            }
            @Override
            public void onRouteVolumeChanged(final MediaRouter router, final MediaRouter.RouteInfo info) {
            }
            @Override
            public void onProviderAdded(final MediaRouter router, final MediaRouter.ProviderInfo info) {
            }
            @Override
            public void onProviderChanged(final MediaRouter router, final MediaRouter.ProviderInfo info) {
            }
            @Override
            public void onProviderRemoved(final MediaRouter router, final MediaRouter.ProviderInfo info) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MediaRouter.onProviderRemoved");
                    Log.d(TAG, "MediaRouter.onProviderRemoved: " + info.toString());
                }

            }
        };
    }

    /**
     * MediaRouterのイベントを登録する.
     */
    public void registerEvent() {
        update();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    /**
     * MediaRouterのイベントの登録を解除する.
     */
    public void unregisterEvent() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    /**
     * Chromecastデバイス情報を更新する.
     */
    private void update() {
        List<MediaRouter.RouteInfo> rInfos = mMediaRouter.getRoutes();

        mRouteInfos.clear();
        mRouteNames.clear();
        for (int i = 0; i < rInfos.size(); i++) {
            MediaRouter.RouteInfo info = rInfos.get(i);

            if (info.getDescription() != null) {
                CastDevice device = CastDevice.getFromBundle(info.getExtras());
                if (device != null && device.isOnLocalNetwork()) {
                    mRouteInfos.add(info);
                    mRouteNames.add(info.getName());
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "name:" + info.getName());
                        Log.d(TAG, "desc:" + info.getDescription());
                        Log.d(TAG, "Is Connecting:" + info.isConnecting());
                        Log.d(TAG, "Is Enabled:" + info.isEnabled());
                        Log.d(TAG, "Is Selected:" + info.isSelected());
                    }
                }
            }
        }
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
    public ArrayList<String> getDeviceNames() {
        return mRouteNames;
    }

    /**
     * Chromecastデバイスを選択する.
     * 
     * @param name Chromecastデバイス名
     */
    public void setRouteName(final String name) {
        for (int i = 0; i < mRouteInfos.size(); i++) {
            MediaRouter.RouteInfo routeInfo = mRouteInfos.get(i);
            if (routeInfo.getName().equals(name)) {
                mMediaRouter.selectRoute(routeInfo);
                break;
            }
        }
    }
}
