/*
 LinkingHelpActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.linking.lib.R;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingHelpFragment;

public class LinkingHelpActivity extends AppCompatActivity {

    public static final String EXTRA_SCREEN_ID = "screenId";

    private static final int[][] HELP_RES_ID = {
            {
                    R.layout.fragment_linking_help_1,
                    R.layout.fragment_linking_help_2,
                    R.layout.fragment_linking_help_3,
                    R.layout.fragment_linking_help_4,
                    R.layout.fragment_linking_help_5,
                    R.layout.fragment_linking_help_6,
            },
            {
                    R.layout.fragment_linking_help_7,
                    R.layout.fragment_linking_help_4,
                    R.layout.fragment_linking_help_5,
                    R.layout.fragment_linking_help_6,
            }
    };

    private int mScreenId;

    @Override
    protected void onCreate(final  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_help);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle args = intent.getExtras();
            if (args != null) {
                mScreenId = args.getInt(EXTRA_SCREEN_ID);
            }
        }

        FragmentManager manager = getSupportFragmentManager();
        final ViewPager viewPager =  findViewById(R.id.viewPager);
        if (viewPager != null) {
            viewPager.setAdapter(new MyFragmentPagerAdapter(manager));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }
                @Override
                public void onPageSelected(int position) {
                    set();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        Button linkingAppBtn = findViewById(R.id.fragment_linking_app);
        if (linkingAppBtn != null) {
            linkingAppBtn.setOnClickListener((v) -> {
                LinkingUtil.startLinkingApp(getApplicationContext());
            });
        }

        Button nextBtn = (Button) findViewById(R.id.fragment_linking_help_next);
        if (nextBtn != null) {
            nextBtn.setOnClickListener((v) -> {
                if (viewPager != null) {
                    int pos = viewPager.getCurrentItem() + 1;
                    if (pos > HELP_RES_ID[mScreenId].length - 1) {
                        pos = HELP_RES_ID[mScreenId].length - 1;
                    }
                    viewPager.setCurrentItem(pos);
                }
            });
        }

        Button preBtn = findViewById(R.id.fragment_linking_help_pre);
        if (preBtn != null) {
            preBtn.setOnClickListener((v) -> {
                if (viewPager != null) {
                    int pos = viewPager.getCurrentItem() - 1;
                    if (pos < 0) {
                        pos = 0;
                    }
                    viewPager.setCurrentItem(pos);
                }
            });
        }

        set();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void set() {
        Button nextBtn = findViewById(R.id.fragment_linking_help_next);
        Button preBtn = findViewById(R.id.fragment_linking_help_pre);
        ViewPager viewPager =  findViewById(R.id.viewPager);
        if (viewPager != null && nextBtn != null && preBtn != null) {
            int position = viewPager.getCurrentItem();
            if (position == 0) {
                preBtn.setVisibility(View.GONE);
            } else {
                preBtn.setVisibility(View.VISIBLE);
            }
            if (position == HELP_RES_ID[mScreenId].length - 1) {
                nextBtn.setVisibility(View.GONE);
            } else {
                nextBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            return LinkingHelpFragment.newInstance(HELP_RES_ID[mScreenId][position]);
        }

        @Override
        public int getCount() {
            return HELP_RES_ID[mScreenId].length;
        }
    }
}
