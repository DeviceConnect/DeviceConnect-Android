/*
 DConnectSettingPageFragmentActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.R;
import org.deviceconnect.android.ui.adapter.DConnectFragmentPagerAdapter;
import org.deviceconnect.android.ui.adapter.DConnectPageCreater;

/**
 * デバイスプラグイン設定画面用 ベースフラグメントアクティビティ.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectSettingCompatPageFragmentActivity extends AppCompatActivity implements
        DConnectPageCreater<Fragment> {
    
    /**
     * ページ用のビューページャー.
     */
    private ViewPager mViewPager;

    /**
     * ViewPagerを持つレイアウトを自動的に設定する.
     * サブクラスでオーバーライドする場合は setContentView を<strong>実行しないこと</strong>。
     * 
     * @param savedInstanceState パラメータ
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);

        mViewPager = (ViewPager) findViewById(R.id.setting_pager);
        mViewPager.setAdapter(new DConnectFragmentPagerAdapter(getSupportFragmentManager(), this));
        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle(org.deviceconnect.android.deviceplugin.chromecast.R.string.activity_setting_page_title);
        toolbar.setBackgroundColor(Color.parseColor("#00a0e9"));
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        addContentView(toolbar, new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        toolbar.setNavigationIcon(org.deviceconnect.android.deviceplugin.chromecast.R.drawable.close_icon);
        toolbar.setNavigationOnClickListener((view) -> {
            finish();
        });
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * ViewPagerを取得する.
     * 
     * @return ViewPagerのインスタンス
     */
    protected ViewPager getViewPager() {
        return mViewPager;
    }
}
