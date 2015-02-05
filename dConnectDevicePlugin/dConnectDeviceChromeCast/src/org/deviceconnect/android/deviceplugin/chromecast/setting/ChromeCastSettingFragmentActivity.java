/*
 ChromeCastSettingFragmentActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.setting;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * チュートリアル画面（ステップ）.
 * <p>
 * 画面を作成する
 * </p>
 * 
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastSettingFragmentActivity extends DConnectSettingPageFragmentActivity {

    /** ページUI用Fragment. */
    private ArrayList<Fragment> mFragments;
    
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
    public int getPageCount() {
        return mFragments.size();
    }

    @Override
    public Fragment createPage(final int position) {
        return mFragments.get(position);
    }
}
