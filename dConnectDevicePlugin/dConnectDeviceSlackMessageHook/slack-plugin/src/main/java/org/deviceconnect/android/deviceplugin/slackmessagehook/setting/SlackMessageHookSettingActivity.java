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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.ChannelListFragment;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.SettingFragment;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.SettingTokenFragment;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.ShowMenuFragment;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.Utils;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;
import org.deviceconnect.android.ui.activity.DConnectSettingPageActivity;

import java.io.File;

/**
 * 設定用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class SlackMessageHookSettingActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final boolean launchAsApp = Intent.ACTION_MAIN.equals(getIntent().getAction());

        // CLOSEボタン作成
        if (getActionBar() != null && !launchAsApp) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            getActionBar().setTitle(R.string.slack_settings);
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
                if (token == null || error instanceof SlackManager.SlackAuthException) {
                    // Token未設定の場合はToken設定画面へ
                    fragment = new SettingTokenFragment();
                    if (launchAsApp) {
                        Utils.transition(new ChannelListFragment(), getFragmentManager(), false);
                        Utils.transition(fragment, getFragmentManager(), true);
                        return;
                    }
                } else {
                    if (launchAsApp || error != null) {
                        // ラウンチャーから起動の場合はChannelリスト画面へ
                        fragment = new ChannelListFragment();
                    } else {
                        // Managerからの起動の場合は設定画面へ
                        fragment = new SettingFragment();
                    }
                }
                // 画面遷移
                Utils.transition(fragment, getFragmentManager(), false);
            }
        });
    }

    /** メニューを表示するクラス */
    private static final Class[] MenuFragments = {
            ChannelListFragment.class,
            SettingFragment.class
    };

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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final Context context = SlackMessageHookSettingActivity.this;
        switch (item.getItemId()) {
            case android.R.id.home:
                // 閉じる
                finish();
                return true;
            case R.id.menu_change_token:
                // 画面遷移
                Utils.transition(new SettingTokenFragment(), getFragmentManager(), true);
                return true;
            case R.id.menu_setting:
                // 画面遷移
                Fragment fragment = new SettingFragment();
                Utils.transition(fragment, getFragmentManager(), true);
                break;
            case R.id.menu_clear_cache:
                // キャッシュクリア
                Utils.showConfirmDialog(context, context.getString(R.string.menu_clear_cache), context.getString(R.string.clear_cache), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File cacheDir = Utils.getCacheDir(context);
                        Utils.deleteDir(cacheDir);
                    }
                });
                break;
            case R.id.menu_open_slack:
                // Slackを開く
                Uri uri = Uri.parse("https://slack.com/messages");
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}