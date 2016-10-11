/*
 ChromeCastSettingFragmentActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.IntroductoryOverlay;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastApplication;
import org.deviceconnect.android.deviceplugin.chromecast.R;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;

import java.util.ArrayList;

/**
 * チュートリアル画面（ステップ）.
 * <p>
 * 画面を作成する
 * </p>
 * 
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastSettingFragmentActivity extends DConnectSettingCompatPageFragmentActivity {

    /** ページUI用Fragment. */
    private ArrayList<Fragment> mFragments;
    private MenuItem mediaRouteMenuItem;
    private IntroductoryOverlay mIntroductoryOverlay;
    /** ChromeCast接続用Button. */
    private MediaRouteButton mMediaRouteButton;
    /** ChromeCastを管理するApplication. */
    private ChromeCastApplication mApp;

    /**
     * コンストラクタ.
     */
    public ChromeCastSettingFragmentActivity() {
        mFragments = new ArrayList<Fragment>();
        mFragments.add(new ChromeCastSettingFragmentPage1());
        mFragments.add(new ChromeCastSettingFragmentPage2());
        mFragments.add(new ChromeCastSettingFragmentPage3());
    }
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public int getPageCount() {
        return mFragments.size();
    }

    @Override
    public Fragment createPage(final int position) {
        return mFragments.get(position);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mApp != null) {
            ChromeCastDiscovery disocovery = mApp.getDiscovery();
            if (disocovery != null) {
                disocovery.registerEvent();
            }
        }
    }

    @Override
    public void onPause() {
        if (mApp != null) {
            ChromeCastDiscovery disocovery = mApp.getDiscovery();
            if (disocovery != null) {
                disocovery.unregisterEvent();
            }
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(this, menu,
                R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider
                = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);
        mApp = (ChromeCastApplication) getApplication();
        if (mApp != null) {
            mApp.initialize();
            MediaRouteSelector selector = mApp.getDiscovery().getMediaRouteSelector();
            // Set the MediaRouteActionProvider selector for device discovery.
            mediaRouteActionProvider.setRouteSelector(selector);
            showIntroductoryOverlay();
        }
        return true;
    }


    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
                        mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                                ChromeCastSettingFragmentActivity.this, mediaRouteMenuItem)
                                .setTitleText(getString(R.string.introducing_cast))
                                .setOverlayColor(R.color.primary)
                                .setSingleTime()
                                .setOnOverlayDismissedListener(
                                        new IntroductoryOverlay.OnOverlayDismissedListener() {
                                            @Override
                                            public void onOverlayDismissed() {
                                                mIntroductoryOverlay = null;
                                            }
                                        })
                                .build();
                        mIntroductoryOverlay.show();
                    }
            }
        }, 500);
    }

}
