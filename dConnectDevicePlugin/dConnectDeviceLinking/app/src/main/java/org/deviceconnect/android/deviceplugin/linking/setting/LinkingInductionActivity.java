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
