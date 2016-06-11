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
import android.os.Bundle;
import android.support.annotation.Nullable;
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
        final String token = Utils.getAccessToken(this);
        SlackManager.INSTANCE.setApiToken(token, false, new SlackManager.FinishCallback<Void>() {
            @Override
            public void onFinish(Void aVoid, Exception error) {
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}