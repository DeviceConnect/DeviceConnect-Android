package org.deviceconnect.android.deviceplugin.hitoe.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileHealthFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileListFragment;

/**
 * This activity is hitoe debug screen.
 * @author NTT DOCOMO, INC.
 */
public class HitoeDeviceControlActivity extends FragmentActivity {
    /**
     * Feature serviceId.
     */
    public static final String FEATURE_SERVICE_ID = "org.deviceconnect.android.hitoe.SERVICEID";

    /**
     * Default title.
     */
    public static final String DEFAULT_TITLE = "CLOSE";


    public static final int CONTROL_PAGE_MAIN = 0;
    public static final int CONTROL_PAGE_HEARTRATE = 1;
    public static final int CONTROL_PAGE_BATTERY = 2;
    public static final int CONTROL_PAGE_DEVICEORIENTATION = 3;
    public static final int CONTROL_PAGE_ECG = 4;
    public static final int CONTROL_PAGE_STRESS = 5;
    public static final int CONTROL_PAGE_POSE = 6;
    public static final int CONTROL_PAGE_WALK = 7;


    private ListView mProfileListView;
    private ArrayAdapter<String> mAdapter;
    private HitoeManager mManager;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HitoeApplication app = (HitoeApplication) getApplication();
        app.initialize();
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            getActionBar().setTitle(DEFAULT_TITLE);
        }
        if (savedInstanceState == null) {
            movePage(CONTROL_PAGE_MAIN);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void movePage(final int page) {
        Bundle args = new Bundle();
        Intent device = getIntent();
        if (device == null) {
            return;
        }
        String serviceId = device.getStringExtra(FEATURE_SERVICE_ID);
        args.putString(FEATURE_SERVICE_ID, serviceId);
        switch (page) {
            case CONTROL_PAGE_MAIN:
                HitoeProfileListFragment profile = new HitoeProfileListFragment();
                moveFragment(true, profile);
                profile.setArguments(args);
                break;
            case CONTROL_PAGE_HEARTRATE:
                HitoeProfileHealthFragment heartRateProfile = new HitoeProfileHealthFragment();
                moveFragment(false, heartRateProfile);
                heartRateProfile.setArguments(args);
                break;

        }

    }
    /**
     * Gets a instance of HitoeManager.
     *
     * @return HitoeManager
     */
    public HitoeManager getManager() {
        HitoeApplication application =
                (HitoeApplication) super.getApplication();
        return application.getHitoeManager();
    }

    private void moveFragment(final boolean isFirst, final Fragment f) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.setTransition(FragmentTransaction.TRANSIT_NONE);
        t.replace(android.R.id.content, f);
        if (!isFirst) {
            t.addToBackStack(null);
        }
        t.commit();

    }

}
