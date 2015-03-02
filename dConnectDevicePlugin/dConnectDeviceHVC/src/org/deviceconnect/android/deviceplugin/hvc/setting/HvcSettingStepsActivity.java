/*
 HvcSettingStepsActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.hvc.R;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * HVC setting activity.
 */
public class HvcSettingStepsActivity extends DConnectSettingPageFragmentActivity {
    
    private static final int TUTORIAL_PAGE_NNMBER = 3;
    
    private static final String LOGGER_NAME = "HvcSettingStepsActivity";
    
    /**
     * ホストアプリケーションタイトルテキスト.
     */
    private static TextView sHostApplicationTitleText;

    /** フラグメント一覧. */
    private List<Fragment> mFragments = new ArrayList<Fragment>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onSaveInstanceState(final Bundle outState) {

    }

    @Override
    public int getPageCount() {
        return TUTORIAL_PAGE_NNMBER;
    }

    @Override
    public Fragment createPage(final int position) {
        if (mFragments.size() == 0) {
            BaseFragment f1 = new AboutFragment();
            f1.setActivity(this);
            BaseFragment f2 = new HvcConnectFragment();
            f2.setActivity(this);
            BaseFragment f3 = new BluetoothSettingPromptFragment();
            f3.setActivity(this);
            mFragments.add(f1);
            mFragments.add(f2);
            mFragments.add(f3);
        }
        return mFragments.get(position);
    }
    /**
     * チュートリアルページの取得.
     * @param position position
     */
    public void setCurrentPage(final int position) {
        getViewPager().setCurrentItem(position, true);
    }

    /**
     * Base Fragment.
     *
     */
    public static class BaseFragment extends Fragment {
        
        /** ロガー. */
        protected Logger mLogger = Logger.getLogger(LOGGER_NAME);
        /**
         * チュートリアルページアクティビティ.
         */
        HvcSettingStepsActivity mActivity;
        /**
         * アクティビティを設定する.
         * @param activity activity
         */
        public void setActivity(final HvcSettingStepsActivity activity) {
            mActivity = activity;
        }
    }

    /**
     * step1. About HVC Device Plugin.
     */
    public static class AboutFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, 
                final ViewGroup container, final Bundle savedInstanceState) {
            final int layoutId = R.layout.hvc_setting_0;
            View root = inflater.inflate(layoutId, container, false);
            
            return root;
        }
        @Override
        public void onResume() {
            super.onResume();
        }
    }

    /**
     * 手順2 HVC-Cの準備.
     */
    public static class HvcConnectFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, 
                final ViewGroup container, final Bundle savedInstanceState) {
            final int layoutId = R.layout.hvc_setting_1;
            View root = inflater.inflate(layoutId, container, false);
            return root;
        }
    }

    /**
     * 手順3 Bluetooth設定.
     */
    public static class BluetoothSettingPromptFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, 
                final ViewGroup container, final Bundle savedInstanceState) {
            final int layoutId = R.layout.hvc_setting_2;
            View root = inflater.inflate(layoutId, container, false);
            Button button = (Button) root.findViewById(R.id.button_launch_bluetooth_setting);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                }
            });
            return root;
        }
    }
}
