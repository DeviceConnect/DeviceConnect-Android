/*
 MainActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import org.deviceconnect.android.app.simplebot.data.SettingData;
import org.deviceconnect.android.app.simplebot.fragment.CommandDetailsFragment;
import org.deviceconnect.android.app.simplebot.fragment.CommandListFragment;
import org.deviceconnect.android.app.simplebot.fragment.SettingFragment;
import org.deviceconnect.android.app.simplebot.utils.Utils;


/**
 * メインアクティビティ
 */
public class MainActivity extends Activity {

    private Menu mainMenu;

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
        Utils.transition(new CommandListFragment(), getFragmentManager(), false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                Utils.transition(new SettingFragment(), getFragmentManager(), true);
                break;
            case R.id.menu_add_command:
                Fragment fragment = new CommandDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("mode", "add");
                fragment.setArguments(bundle);
                Utils.transition(fragment, getFragmentManager(), true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        if (action == KeyEvent.ACTION_UP) {
            // メニュー表示
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (mainMenu != null) {
                    mainMenu.performIdentifierAction(R.id.overflow_options, 0);
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
