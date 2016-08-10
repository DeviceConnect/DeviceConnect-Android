/*
 LinkingInductionActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;

public class LinkingInductionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_induction);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }

        Button linkingBtn = (Button) findViewById(R.id.fragment_linking_app);
        if (linkingBtn != null) {
            linkingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkingUtil.startLinkingApp(getApplicationContext());
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

        Button updateBtn = (Button) findViewById(R.id.fragment_linking_setting_update_btn);
        if (updateBtn != null) {
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showGooglePlay() {
        View playView = findViewById(R.id.fragment_linking_setting_google_play);
        View updateView = findViewById(R.id.fragment_linking_setting_update_app);
        if (playView != null && updateView != null) {
            if (LinkingUtil.isApplicationInstalled(getApplicationContext())) {
                if (LinkingUtil.getVersionCode(this) < LinkingUtil.LINKING_APP_VERSION) {
                    updateView.setVisibility(View.VISIBLE);
                    playView.setVisibility(View.GONE);
                } else {
                    updateView.setVisibility(View.GONE);
                    playView.setVisibility(View.GONE);
                }
            } else {
                updateView.setVisibility(View.GONE);
                playView.setVisibility(View.VISIBLE);
            }
        }
    }
}
