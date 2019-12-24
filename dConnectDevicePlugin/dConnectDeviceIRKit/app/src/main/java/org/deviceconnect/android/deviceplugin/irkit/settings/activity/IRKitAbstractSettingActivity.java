/*
 IRKitAbstractSettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.ui.adapter.DConnectFragmentPagerAdapter;
import org.deviceconnect.android.ui.adapter.DConnectPageCreater;

/**
 * ViewPagerを独自拡張するためのActivity.
 * @author NTT DOCOMO, INC.
 */
abstract class IRKitAbstractSettingActivity extends FragmentActivity implements
        DConnectPageCreater<Fragment> {

    /**
     * ページ用のビューページャー.
     */
    private ViewPager mViewPager;

    /**
     * ViewPagerを持つレイアウトを自動的に設定する. サブクラスでオーバーライドする場合は setContentView
     * を<strong>実行しないこと</strong>。
     * 
     * @param savedInstanceState パラメータ
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.irkit_activity_setting_page);

        mViewPager = (ViewPager) findViewById(R.id.setting_pager);
        DConnectFragmentPagerAdapter adapter = new DConnectFragmentPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(adapter);


        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(R.string.setting_page_title);
        }
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
