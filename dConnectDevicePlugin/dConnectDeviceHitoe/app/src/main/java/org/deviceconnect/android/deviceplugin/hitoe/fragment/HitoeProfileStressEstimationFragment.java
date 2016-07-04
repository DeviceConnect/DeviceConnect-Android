/*
 HitoeProfileHealthFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.StressEstimationData;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;


/**
 * This fragment do setting of the control health profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileStressEstimationFragment extends Fragment  implements HitoeScheduler.OnRegularNotify {

    /**
     * Current Hitoe Device object.
     */
    private HitoeDevice mCurrentDevice;

    /**
     * HeartRate TextView.
     */
    private TextView mLFHF;

    private HitoeScheduler mScheduler;
    private GradientDrawable mLFHFGradientDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stress_instructions, null);
        mScheduler = new HitoeScheduler(getActivity(), this, HitoeConstants.LFHF_TEXT_UPDATE_CYCLE_TIME,
                                                HitoeConstants.LFHF_TEXT_UPDATE_CYCLE_TIME);
        rootView.findViewById(R.id.button_register).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mScheduler.scanHitoeDevice(true);
            }
        });
        rootView.findViewById(R.id.button_unregister).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mScheduler.scanHitoeDevice(false);
            }
        });
        TextView title = (TextView) rootView.findViewById(R.id.view_title);
        mLFHF = (TextView) rootView.findViewById(R.id.lfhf_value);
        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                String[] profiles = getResources().getStringArray(R.array.support_profiles);
                title.setText(profiles[4] + getString(R.string.title_control));
            }
        }
        mLFHFGradientDrawable = (GradientDrawable) mLFHF.getBackground();
        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
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

                StressEstimationData stress = manager.getStressEstimationData(mCurrentDevice.getId());
                if (stress != null) {
                    updateView(stress.getTimeStamp(), stress.getLFHFValue());
                }

            }
        });
    }

    public void updateView(final long timestamp, final double lfhf) {

        final int score_RGB;
        final int score_R;
        final int score_G;
        final int score_B;

        if (timestamp == -1) {

            return;
        }
        score_RGB = (int) (150 * (lfhf / 5));
        if(105 + score_RGB < 255) {
            score_R = 105 + score_RGB;
        } else {
            score_R = 255;
        }
        if(255 - score_RGB > 0) {
            score_G = 255 - score_RGB;
            score_B = 255 - score_RGB;
        } else {
            score_G = 0;
            score_B = 0;
        }

        mLFHF.setText("LF/HF:" + String.valueOf(lfhf));
        mLFHFGradientDrawable.setColors(new int[]{0xFFCDFFFF, Color.rgb(score_R, score_G, score_B)});
    }
}
