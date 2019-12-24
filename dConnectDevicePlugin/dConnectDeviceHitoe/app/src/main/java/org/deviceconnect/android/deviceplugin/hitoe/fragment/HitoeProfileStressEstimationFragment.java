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
import org.deviceconnect.android.deviceplugin.hitoe.data.StressEstimationData;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;


/**
 * This fragment do setting of the control health profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileStressEstimationFragment extends Fragment implements HitoeScheduler.OnRegularNotify {

    /**
     * Current Hitoe Device object.
     */
    private HitoeDevice mCurrentDevice;

    /**
     * HeartRate TextView.
     */
    private TextView mLFHF;
    /**
     * Hitoe scheduler.
     */
    private HitoeScheduler mScheduler;
    /** LFHF view. */
    private GradientDrawable mLFHFGradientDrawable;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stress_instructions, null);
        mScheduler = new HitoeScheduler(this, HitoeConstants.LFHF_TEXT_UPDATE_CYCLE_TIME,
                                                HitoeConstants.LFHF_TEXT_UPDATE_CYCLE_TIME);
        rootView.findViewById(R.id.button_register).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(true);
        });
        rootView.findViewById(R.id.button_unregister).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(false);
        });
        TextView title = rootView.findViewById(R.id.view_title);
        mLFHF = rootView.findViewById(R.id.lfhf_value);
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

            StressEstimationData stress = manager.getStressEstimationData(mCurrentDevice.getId());
            if (stress != null) {
                updateView(stress.getTimeStamp(), stress.getLFHFValue());
            }
        });
    }

    /**
     * Update view.
     * @param timestamp timestamp
     * @param lfhf stress estimation
     */
    public void updateView(final long timestamp, final double lfhf) {

        final int scoreRGB;
        final int scoreR;
        final int scoreG;
        final int scoreB;

        if (timestamp == -1) {

            return;
        }
        scoreRGB = (int) (150 * (lfhf / 5));
        if(105 + scoreRGB < 255) {
            scoreR = 105 + scoreRGB;
        } else {
            scoreR = 255;
        }
        if(255 - scoreRGB > 0) {
            scoreG = 255 - scoreRGB;
            scoreB = 255 - scoreRGB;
        } else {
            scoreG = 0;
            scoreB = 0;
        }

        mLFHF.setText("LF/HF:" + String.valueOf(lfhf));
        mLFHFGradientDrawable.setColors(new int[]{0xFFCDFFFF, Color.rgb(scoreR, scoreG, scoreB)});
    }
}
