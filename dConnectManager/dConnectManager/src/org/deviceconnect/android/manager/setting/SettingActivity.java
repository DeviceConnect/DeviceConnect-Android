/*
 SettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.localoauth.activity.AccessTokenListActivity;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.hmac.HmacManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Device Connect Manager設定管理用Activity.
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends Activity {

    /** 起動用URIスキーム名. */
    private static final String SCHEME_LAUNCH = "dconnect";

    /** HMAC管理クラス. */
    private HmacManager mHmacManager;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_dconnect_settings);
        mHmacManager = new HmacManager(this);
        LocalOAuth2Main.initialize(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null && SCHEME_LAUNCH.equals(intent.getScheme())) {
            String key = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEY);
            String origin = intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            try {
                if (origin != null) {
                    origin = URLDecoder.decode(origin, "UTF-8");
                    if (key != null && !TextUtils.isEmpty(origin)) {
                        mHmacManager.updateKey(origin, key);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                // nothing to do.
                return;
            }
            startService(new Intent(this, DConnectService.class));

            ComponentName receiver = intent.getParcelableExtra(DConnectMessage.EXTRA_RECEIVER);
            if (receiver != null) {
                Intent response = new Intent(IntentDConnectMessage.ACTION_MANAGER_LAUNCHED);
                response.setComponent(receiver);
                sendBroadcast(response);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalOAuth2Main.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dconnect_settings, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, AccessTokenListActivity.class);
            startActivity(intent);
        } else if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
