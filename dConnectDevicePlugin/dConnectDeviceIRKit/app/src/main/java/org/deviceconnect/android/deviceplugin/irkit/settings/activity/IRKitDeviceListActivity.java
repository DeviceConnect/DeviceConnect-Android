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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.irkit.IRKitApplication;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.settings.fragment.IRKitDeviceListFragment;
import org.deviceconnect.android.deviceplugin.irkit.settings.fragment.IRKitRegisterIRFragment;
import org.deviceconnect.android.deviceplugin.irkit.settings.fragment.IRKitVirtualDeviceFragment;
import org.deviceconnect.android.deviceplugin.irkit.settings.fragment.IRKitVirtualProfileListFragment;

/**
 * Device Connect Manager device plug-in list Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class IRKitDeviceListActivity extends Activity {

    /**
     * トップページ.
     */
    public static final int TOP_PAGE = 0;

    /**
     * Virtual Device 管理ページ.
     */
    public static final int MANAGE_VIRTUAL_DEVICE_PAGE = 1;

    /**
     * Virtual Profile 管理ページ.
     */
    public static final int MANAGE_VIRTUAL_PROFILE_PAGE = 2;

    /**
     * Ir 登録ページ.
     */
    public static final int REGISTER_IR_PAGE = 3;

    /**
     * 今表示されているページ番号
     */
    private int currentPage = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            startApp(TOP_PAGE, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getIRKitApplication().removeAllListViewPostion();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (currentPage) {
            case REGISTER_IR_PAGE:
            case MANAGE_VIRTUAL_PROFILE_PAGE:
            case MANAGE_VIRTUAL_DEVICE_PAGE:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                currentPage--;
                if (currentPage == TOP_PAGE) {
                    getActionBar().setTitle("CLOSE");
                }
                break;
            default:
                finish();

        }
        return true;
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
                        if (currentPage == TOP_PAGE) {
                            getActionBar().setTitle("CLOSE");
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
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
            getActionBar().setTitle("一覧");

            IRKitVirtualProfileListFragment f = new IRKitVirtualProfileListFragment();
            f.setServiceId(serviceId);
            moveFragment(f);
        } else if (pageId == MANAGE_VIRTUAL_DEVICE_PAGE) {
            getActionBar().setTitle("一覧");

            IRKitVirtualDeviceFragment f = new IRKitVirtualDeviceFragment();
            f.setServiceId(serviceId);
            moveFragment(f);
        } else if (pageId == TOP_PAGE) {
            getActionBar().setTitle("CLOSE");

            IRKitDeviceListFragment f = new IRKitDeviceListFragment();
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
        getActionBar().setTitle("一覧");

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
