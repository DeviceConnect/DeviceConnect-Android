/*
 IRKitDeviceListActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.irkit.IRKitApplication;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.settings.fragment.IRKitRegisterIRFragment;
import org.deviceconnect.android.deviceplugin.irkit.settings.fragment.IRKitVirtualDeviceFragment;
import org.deviceconnect.android.deviceplugin.irkit.settings.fragment.IRKitVirtualProfileListFragment;

/**
 * Device Connect Manager device plug-in list Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class IRKitVirtualDeviceListActivity extends Activity
    implements FragmentManager.OnBackStackChangedListener {

    /**
     * エクストラ: 対象のサービスID.
     */
    public static final String EXTRA_SERVICE_ID = "serviceId";

    /**
     * Virtual Device 管理ページ.
     */
    public static final int MANAGE_VIRTUAL_DEVICE_PAGE = 0;

    /**
     * Virtual Profile 管理ページ.
     */
    public static final int MANAGE_VIRTUAL_PROFILE_PAGE = 1;

    /**
     * Ir 登録ページ.
     */
    public static final int REGISTER_IR_PAGE = 2;

    /**
     * 今表示されているページ番号
     */
    private int currentPage = MANAGE_VIRTUAL_DEVICE_PAGE;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            String serviceId = getIntent().getStringExtra(EXTRA_SERVICE_ID);
            startApp(currentPage, serviceId);
            setTitleForPage(currentPage);

            getFragmentManager().addOnBackStackChangedListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getFragmentManager().removeOnBackStackChangedListener(this);
        getIRKitApplication().removeAllListViewPostion();
    }

    @Override
    public void onBackStackChanged() {
        setTitleForPage(currentPage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (currentPage) {
            case REGISTER_IR_PAGE:
            case MANAGE_VIRTUAL_PROFILE_PAGE:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                currentPage--;
                break;
            default:
                finish();

        }
        return true;
    }
    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    FragmentManager fm = getFragmentManager();
                    int cnt = fm.getBackStackEntryCount();
                    if (cnt <= 1) {
                        finish();
                        return false;
                    } else {
                        currentPage--;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void setTitleForPage(final int pageId) {
        int resId;
        switch (pageId) {
            case MANAGE_VIRTUAL_DEVICE_PAGE:
                resId = R.string.virtual_device_list;
                break;
            case MANAGE_VIRTUAL_PROFILE_PAGE:
                resId = R.string.ir_registration;
                break;
            case REGISTER_IR_PAGE:
                resId = R.string.ir_registration;
                break;
            default:
                return;
        }
        getActionBar().setTitle(resId);
    }

    /**
     * ページを遷移する.
     * @param pageId pageId
     * @param serviceId サービスID
     */
    public void startApp(final int pageId, final String serviceId) {
        currentPage = pageId;
        setTitle(R.string.activity_devicelist_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
        if (pageId == MANAGE_VIRTUAL_PROFILE_PAGE) {
            IRKitVirtualProfileListFragment f = new IRKitVirtualProfileListFragment();
            f.setServiceId(serviceId);
            moveFragment(f);
        } else if (pageId == MANAGE_VIRTUAL_DEVICE_PAGE) {
            IRKitVirtualDeviceFragment f = new IRKitVirtualDeviceFragment();
            f.setServiceId(serviceId);
            moveFragment(f);
        }
    }

    /**
     * IRを登録するページを開く.
     * @param virtualProfile Virtual Profile
     */
    public void startRegisterPageApp(final VirtualProfileData virtualProfile) {
        currentPage = REGISTER_IR_PAGE;
        setTitle(R.string.activity_devicelist_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);

        IRKitRegisterIRFragment f = new IRKitRegisterIRFragment();
        f.setProfile(virtualProfile);
        moveFragment(f);
    }

    /**
     * Fragment の遷移.
     * @param f Fragment
     */
    private void moveFragment(final Fragment f) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction t = fm.beginTransaction();
        t.setTransition(FragmentTransaction.TRANSIT_NONE);
        t.replace(android.R.id.content, f);
        t.addToBackStack(null);
        t.commit();
    }

    /**
     * IRKitApplicationを返す.
     * @return IRKitApplication
     */
    public IRKitApplication getIRKitApplication() {
        return (IRKitApplication) getApplication();
    }

}
