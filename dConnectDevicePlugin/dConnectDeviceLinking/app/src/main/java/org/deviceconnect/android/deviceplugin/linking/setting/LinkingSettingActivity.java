/*
 org.deviceconnect.android.deviceplugin.linking
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;

public class LinkingSettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_setting);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button connectBtn = (Button) findViewById(R.id.fragment_linking_setting_connect);
        if (connectBtn != null) {
            connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLinkingHelpActivity(0);
                }
            });
        }

        Button setBtn = (Button) findViewById(R.id.fragment_linking_setting_set);
        if (setBtn != null) {
            setBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLinkingHelpActivity(1);
                }
            });
        }

        Button linkingBtn = (Button) findViewById(R.id.fragment_linking_app);
        if (linkingBtn != null) {
            linkingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkingUtil.startLinakingApp(getApplicationContext());
                }
            });
        }

        Button googleBtn = (Button) findViewById(R.id.fragment_linking_setting_google_play_btn);
        if (googleBtn != null) {
            googleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkingUtil.startGooglePlay(getApplicationContext());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showGooglePlay();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLinkingHelpActivity(int screenId) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), LinkingHelpActivity.class);
        intent.putExtra(LinkingHelpActivity.EXTRA_SCREEN_ID, screenId);
        startActivity(intent);
    }

    private void showGooglePlay() {
        View view = findViewById(R.id.fragment_linking_setting_google_play);
        if (view != null) {
            if (LinkingUtil.isApplicationInstalled(getApplicationContext())) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
        }
    }
}
