/*
 ChromeCastSettingFragmentActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.IntroductoryOverlay;

import org.deviceconnect.android.deviceplugin.chromecast.R;

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
    /**
     * コンストラクタ.
     */
    public ChromeCastSettingFragmentActivity() {
        mFragments = new ArrayList<>();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(this, menu,
                R.id.media_route_menu_item);
        showIntroductoryOverlay();
        return true;
    }


    /**
     * 説明を表示.
     */
    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        new Handler().postDelayed(() -> {
            if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
                mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                        ChromeCastSettingFragmentActivity.this, mediaRouteMenuItem)
                        .setTitleText(getString(R.string.introducing_cast))
                        .setOverlayColor(R.color.primary)
                        .setSingleTime()
                        .setOnOverlayDismissedListener(
                                () -> {
                                    mIntroductoryOverlay = null;
                                })
                        .build();
                mIntroductoryOverlay.show();
            }
        }, 500);
    }

}
