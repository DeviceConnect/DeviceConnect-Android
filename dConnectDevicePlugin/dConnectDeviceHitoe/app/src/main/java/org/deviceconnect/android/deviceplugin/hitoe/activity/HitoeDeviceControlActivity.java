package org.deviceconnect.android.deviceplugin.hitoe.activity;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileBatteryFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileDeviceOrientationFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileECGFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileHealthFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileListFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfilePoseEstimationFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileStressEstimationFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeProfileWalkStateFragment;

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
    protected final Handler mHandler = new Handler();

    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF) {
                    finish();
                }
            }
        }
    };

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
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mSensorReceiver, filter, null, mHandler);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mSensorReceiver);
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
            case CONTROL_PAGE_BATTERY:
                HitoeProfileBatteryFragment batteryProfile = new HitoeProfileBatteryFragment();
                moveFragment(false, batteryProfile);
                batteryProfile.setArguments(args);
                break;
            case CONTROL_PAGE_DEVICEORIENTATION:
                HitoeProfileDeviceOrientationFragment deviceOrientationProfile = new HitoeProfileDeviceOrientationFragment();
                moveFragment(false, deviceOrientationProfile);
                deviceOrientationProfile.setArguments(args);
                break;
            case CONTROL_PAGE_ECG:
                HitoeProfileECGFragment ecgProfile = new HitoeProfileECGFragment();
                moveFragment(false, ecgProfile);
                ecgProfile.setArguments(args);
                break;
            case CONTROL_PAGE_STRESS:
                HitoeProfileStressEstimationFragment stressProfile = new HitoeProfileStressEstimationFragment();
                moveFragment(false, stressProfile);
                stressProfile.setArguments(args);
                break;
            case CONTROL_PAGE_POSE:
                HitoeProfilePoseEstimationFragment poseProfile = new HitoeProfilePoseEstimationFragment();
                moveFragment(false, poseProfile);
                poseProfile.setArguments(args);
                break;
            case CONTROL_PAGE_WALK:
                HitoeProfileWalkStateFragment walkProfile = new HitoeProfileWalkStateFragment();
                moveFragment(false, walkProfile);
                walkProfile.setArguments(args);
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
