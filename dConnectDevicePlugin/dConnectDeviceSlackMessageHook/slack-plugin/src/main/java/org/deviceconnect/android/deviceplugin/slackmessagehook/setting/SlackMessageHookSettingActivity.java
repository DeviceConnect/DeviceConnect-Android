/*
 SlackMessageHookSettingActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.SettingFragment;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.SettingTokenFragment;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.Utils;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;
import org.deviceconnect.android.ui.activity.DConnectSettingPageActivity;

/**
 * 設定用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class SlackMessageHookSettingActivity extends Activity {

    private Menu mainMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // CLOSEボタン作成
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            getActionBar().setTitle(DConnectSettingPageActivity.DEFAULT_TITLE);
        }
        // アクセストークンチェック
        boolean needsConnect = Utils.getOnlineStatus(this);
        final String token = Utils.getAccessToken(this);
        // プログレスダイアログを表示
        ProgressDialog dialog = null;
        if (needsConnect) {
            dialog = Utils.showProgressDialog(this);
        }
        final ProgressDialog finalDialog = dialog;
        SlackManager.INSTANCE.setApiToken(token, needsConnect, new SlackManager.FinishCallback<Void>() {
            @Override
            public void onFinish(Void aVoid, Exception error) {
                if (finalDialog != null) finalDialog.dismiss();
                Fragment fragment;
                if (token == null) {
                    fragment = new SettingTokenFragment();
                } else {
                    fragment = new SettingFragment();
                }
                // 画面遷移
                Utils.transition(fragment, getFragmentManager(), false);
            }
        });
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // Token設定画面の時はToken設定メニューを隠す
        Fragment fragment = getFragmentManager().findFragmentByTag(SettingTokenFragment.class.getName());
        if (fragment != null && fragment.isVisible()) {
            menu.getItem(0).setVisible(false);
        } else {
            menu.getItem(0).setVisible(true);
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 閉じる
                finish();
                return true;
            case R.id.menu_change_token:
                // 画面遷移
                Utils.transition(new SettingTokenFragment(), getFragmentManager(), true);
                return true;
            case R.id.menu_open_slack:
                // Slackを開く
                Uri uri = Uri.parse("https://slack.com/messages");
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
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