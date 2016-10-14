/*
 MainActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import org.deviceconnect.android.app.simplebot.data.SettingData;
import org.deviceconnect.android.app.simplebot.fragment.CommandDetailsFragment;
import org.deviceconnect.android.app.simplebot.fragment.CommandListFragment;
import org.deviceconnect.android.app.simplebot.fragment.ShowMenuFragment;
import org.deviceconnect.android.app.simplebot.fragment.SettingFragment;
import org.deviceconnect.android.app.simplebot.utils.Utils;


/**
 * メインアクティビティ
 */
public class MainActivity extends Activity {

    /** メニューを表示するクラス */
    private static final Class[] MenuFragments = {
            SettingFragment.class,
            CommandListFragment.class,
            CommandDetailsFragment.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // サービス開始
        if (SettingData.getInstance(this).active) {
            Intent serviceIntent = new Intent(MainActivity.this, SimpleBotService.class);
            MainActivity.this.startService(serviceIntent);
        }

        // 画面遷移
        Utils.transition(new SettingFragment(), getFragmentManager(), false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        if (action == KeyEvent.ACTION_UP) {
            // メニュー表示
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                for (Class cls: MenuFragments) {
                    ShowMenuFragment fragment = (ShowMenuFragment) getFragmentManager().findFragmentByTag(cls.getName());
                    if (fragment != null && fragment.isVisible()) {
                        fragment.showMenu();
                    }
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
