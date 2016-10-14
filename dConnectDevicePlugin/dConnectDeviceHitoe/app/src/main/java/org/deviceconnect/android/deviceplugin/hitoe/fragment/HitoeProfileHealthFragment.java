/*
 HitoeProfileHealthFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceControlActivity;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;


/**
 * This fragment do setting of the control health profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileHealthFragment extends Fragment  implements HitoeScheduler.OnRegularNotify {

    /**
     * Current Hitoe Device object.
     */
    private HitoeDevice mCurrentDevice;

    /**
     * HeartRate TextView.
     */
    private TextView mHeartRate;
    /**
     * Hitoe Scheduler.
     */
    private HitoeScheduler mScheduler;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_heartrate_instructions, null);
        mScheduler = new HitoeScheduler(this, HitoeConstants.HR_TEXT_UPDATE_CYCLE_TIME,
                                                HitoeConstants.HR_TEXT_UPDATE_CYCLE_TIME);
        rootView.findViewById(R.id.button_register).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                mScheduler.scanHitoeDevice(true);
            }
        });
        rootView.findViewById(R.id.button_unregister).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                mScheduler.scanHitoeDevice(false);
            }
        });
        TextView title = (TextView) rootView.findViewById(R.id.view_title);
        mHeartRate = (TextView) rootView.findViewById(R.id.heartrate_value);
        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                String[] profiles = getResources().getStringArray(R.array.support_profiles);
                title.setText(profiles[0] + getString(R.string.title_control));
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HitoeApplication app = (HitoeApplication) getActivity().getApplication();
                HitoeManager manager = app.getHitoeManager();
                HeartRateData heart = manager.getHeartRateData(mCurrentDevice.getId());
                if (heart != null) {
                    HeartData rate = heart.getHeartRate();
                    if (rate != null) {
                        mHeartRate.setText("" + rate.getValue());
                    }
                }

            }
        });
    }
}
