/*
 ChromeCastSettingFragmentPage3.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;

import org.deviceconnect.android.deviceplugin.chromecast.BuildConfig;
import org.deviceconnect.android.deviceplugin.chromecast.R;

/**
 * チュートリアル画面.
 * <p>
 * 画面を作成する
 * </p>
 * Chromecastの設定
 * 
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastSettingFragmentPage3 extends Fragment {
    /** Chromecastアプリのパッケージ名. */
    private static final String PACKAGE_NAME = "com.google.android.apps.chromecast.app";
    /** バッジの横サイズ. */
    private int mBadgeWidth = 0;
    /** バッジの縦サイズ. */
    private int mBadgeHeight = 0;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private MediaRouteButton mMediaRouteButton;
    private CastDevice mSelectedDevice;
    private int mRouteCount = 0;

    /**
     * Chromecast App (Google) のインストール状態を調べる.
     * 
     * @param   context         コンテキスト
     * @return  インストール状態    （true: インストールされている, false: インストールされていない）
     */
    private boolean isApplicationInstalled(final Context context) {
        boolean installed = false;
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, PackageManager.GET_META_DATA);
            installed = true;
        } catch (NameNotFoundException e) {
            if (BuildConfig.DEBUG) { 
                e.printStackTrace();
            }
        }
        return installed;
    }

    /**
     * Chromecast App (Google) のインストール状態に応じて、Buttonの背景を変更する.
     * 
     * @param   button  ボタン
     */
    private void changeButtonBackground(final Button button) {
        if (isApplicationInstalled(button.getContext())) {
            button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            button.setBackgroundResource(R.drawable.button_blue);
            button.setText(getResources().getString(R.string.chromecast_settings_step_3_button));
        } else {
            button.setLayoutParams(new LayoutParams(mBadgeWidth, mBadgeHeight));
            button.setBackgroundResource(R.drawable.button_google_play);
            button.setText("");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        changeButtonBackground((Button) getActivity().findViewById(R.id.buttonChromecastSettingApp));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chromecast_settings_step_3, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_setting_page_title));

        toolbar.setNavigationIcon(R.drawable.ic_close_light);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        Button button = (Button) rootView.findViewById(R.id.buttonChromecastSettingApp);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent;
                if (isApplicationInstalled(v.getContext())) {
                    intent = v.getContext().getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                } else {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE_NAME));
                }
                startActivity(intent);
            }
        });

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.button_google_play);
        mBadgeWidth = image.getWidth();
        mBadgeHeight = image.getHeight();
        image.recycle();

//        button = (Button) rootView.findViewById(R.id.buttonChromecastSettingWifiRestart);
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
//                wifi.setWifiEnabled(false);
//                wifi.setWifiEnabled(true);
//            }
//        });
        mMediaRouter = MediaRouter.getInstance(getActivity());
        // Create a MediaRouteSelector for the type of routes your app supports
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getResources()
                                .getString(R.string.application_id))).build();
        // Create a MediaRouter callback for discovery events
        mMediaRouterCallback = new MyMediaRouterCallback();

        // Set the MediaRouteButton selector for device discovery.
        mMediaRouteButton = (MediaRouteButton) rootView.findViewById(R.id.media_route_button);
        mMediaRouteButton.setRouteSelector(mMediaRouteSelector);
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();

        // Add the callback to start device discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    public void onPause() {
        // Remove the callback to stop device discovery
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onPause();
    }
    private class MyMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d("TEST", "onRouteAdded");
            if (++mRouteCount == 1) {
                // Show the button when a device is discovered.
                mMediaRouteButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d("TEST", "onRouteRemoved");
            if (--mRouteCount == 0) {
                // Hide the button if there are no devices discovered.
                mMediaRouteButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d("TEST", "onRouteSelected");
            // Handle route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d("TEST", "onRouteUnselected: info=" + info);
            mSelectedDevice = null;
        }
    }
}
