/*
 HitoeProfileHealthFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceControlActivity;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.WalkStateData;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;


/**
 * This fragment do setting of the control health profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileWalkStateFragment extends Fragment implements HitoeScheduler.OnRegularNotify {

    /**
     * Current Hitoe Device object.
     */
    private HitoeDevice mCurrentDevice;

    /**
     * Step TextView.
     */
    private TextView mStep;
    /**
     * State TextView.
     */
    private TextView mState;

    /**
     * Speed TextView.
     */
    private TextView mSpeed;
    /**
     * Distance TextView.
     */
    private TextView mDistance;
    /**
     * Balance TextView.
     */
    private TextView mBalance;

    /** Hitoe Scheduler. */
    private HitoeScheduler mScheduler;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_walk_instructions, null);
        mScheduler = new HitoeScheduler(this, HitoeConstants.HR_TEXT_UPDATE_CYCLE_TIME,
                                                HitoeConstants.HR_TEXT_UPDATE_CYCLE_TIME);
        rootView.findViewById(R.id.button_register).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(true);
        });
        rootView.findViewById(R.id.button_unregister).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(false);
        });
        TextView title = rootView.findViewById(R.id.view_title);
        mStep = rootView.findViewById(R.id.walk_step);
        mState = rootView.findViewById(R.id.walk_state);
        mSpeed = rootView.findViewById(R.id.walk_speed);
        mDistance =  rootView.findViewById(R.id.walk_distance);
        mBalance = rootView.findViewById(R.id.walk_balance);

        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                String[] profiles = getResources().getStringArray(R.array.support_profiles);
                title.setText(profiles[6] + getString(R.string.title_control));
            }
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScheduler.scanHitoeDevice(false);
    }

    @Override
    public void onRegularNotify() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            WalkStateData walk = manager.getWalkStateData(mCurrentDevice.getId());
            if (walk != null) {
                mStep.setText("" + walk.getStep());
                mState.setText(walk.getState().getState());
                mSpeed.setText("" + walk.getSpeed());
                mDistance.setText("" + walk.getDistance());
                mBalance.setText("" + walk.getBalance());
            }
        });
    }
}
