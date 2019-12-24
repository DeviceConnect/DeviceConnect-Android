/*
 SettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.setting;

import android.content.BroadcastReceiver;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.sphero.BuildConfig;
import org.deviceconnect.android.deviceplugin.sphero.setting.fragment.PairingFragment;
import org.deviceconnect.android.deviceplugin.sphero.setting.fragment.WakeupFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * 設定画面用アクティビティ.
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends DConnectSettingPageFragmentActivity {
    /** Action NameSpace. */
    private static final String ACTION_NAMESPACE = SettingActivity.class.getPackage().getName() + ".action";

    /** 
     * 新規に検知したデバイスを追加するアクション.
     */
    public static final String ACTION_ADD_DEVICE = ACTION_NAMESPACE + ".ADD_DEVICE";

    /** 
     * 接続済みのデバイスを追加するアクション.
     */
    public static final String ACTION_ADD_CONNECTED_DEVICE = ACTION_NAMESPACE + ".ADD_CONNECTED_DEVICE";
    /**
     * 接続されていないデバイスを追加するアクション.
     */
    public static final String ACTION_ADD_FOUNDED_DEVICE = ACTION_NAMESPACE + ".ADD_FOUNDED_DEVICE";

    /** 
     * デバイスを削除するアクション.
     */
    public static final String ACTION_REMOVE_DEVICE = ACTION_NAMESPACE + ".REMOVE_DEVICE";
    
    /** 
     * すべてのデバイスを削除するアクション.
     */
    public static final String ACTION_REMOVE_DEVICE_ALL = ACTION_NAMESPACE + ".REMOVE_DEVICE_ALL";
    
    /** 
     * デバイスを削除するアクション.
     */
    public static final String ACTION_CONNECTED = ACTION_NAMESPACE + ".ACTION_CONNECTED";
    /**
     * デバイスを切断するアクション.
     */
    public static final String ACTION_DISCONNECTED = ACTION_NAMESPACE + ".ACTION_DISCONNECTED";
    /**
     * デバイスを削除するアクション.
     */
    public static final String ACTION_DELETED = ACTION_NAMESPACE + ".ACTION_DELETED";

    /**
     * Extraキー : {@value} .
     */
    public static final String EXTRA_DEVICE = "device";
    /**
     * Extraキー : {@value} .
     */
    public static final String EXTRA_IS_CONNECTED = "is_connected";

    /**
     * Extraキー : {@value} .
     */
    public static final String EXTRA_DEVICES = "devices";
    
    /** 
     * サービスから通知を受けるブロードキャストレシーバ.
     */
    private BroadcastReceiver mReceiver;
    
   /**
     * ページのクラスリスト.
     */
    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
        WakeupFragment.class,
        PairingFragment.class,
    };
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getPageCount() {
        return PAGES.length;
    }

    @Override
    public Fragment createPage(final int position) {
        Fragment page;
        try {
            page = (Fragment) PAGES[position].newInstance();
        } catch (InstantiationException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            page = null;
        } catch (IllegalAccessException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            page = null;
        }
        
        return page;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(mReceiver);
    }
 }
