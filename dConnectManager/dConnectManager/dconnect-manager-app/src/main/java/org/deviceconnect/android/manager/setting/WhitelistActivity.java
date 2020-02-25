/*
 WhitelistActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.deviceconnect.android.manager.R;

/**
 * Whitelist Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WhitelistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.activity_whitelist_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        if (savedInstanceState == null) {
            Fragment f = new WhitelistFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction t = fm.beginTransaction();
            t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            t.add(android.R.id.content, f, "continar");
            t.commit();
        }
    }

}
