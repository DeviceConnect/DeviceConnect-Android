/*
 HitoeDeviceSettingsFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceControlActivity;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.TargetDeviceData;


/**
 * This fragment do setting of the control battery profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileBatteryFragment extends Fragment {

    /**
     * Current Hitoe Device object.
     */
    private HitoeDevice mCurrentDevice;

    /**
     * HeartRate TextView.
     */
    private TextView mLevel;
    /**
     * Battery imageview.
     */
    private ImageView mBatteryImage;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_battery_instructions, null);
        rootView.findViewById(R.id.button_register).setOnClickListener((view) -> {
            setBattery();
        });
        TextView title = rootView.findViewById(R.id.view_title);
        mLevel = rootView.findViewById(R.id.battery_value);
        mBatteryImage = rootView.findViewById(R.id.image_battery);
        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                String[] profiles = getResources().getStringArray(R.array.support_profiles);
                title.setText(profiles[1] + getString(R.string.title_control));
            }
        }

        return rootView;
    }

    /**
     * Set Battery value.
     */
    public void setBattery() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            HeartRateData heart = manager.getHeartRateData(mCurrentDevice.getId());
            TargetDeviceData device = heart.getDevice();
            if (device != null) {
                float level = device.getBatteryLevel();
                if (level == 1.0) {
                    mBatteryImage.setImageResource(R.drawable.mark_battery01);
                } else if (level == 0.75) {
                    mBatteryImage.setImageResource(R.drawable.mark_battery02);
                } else if (level == 0.5) {
                    mBatteryImage.setImageResource(R.drawable.mark_battery03);
                } else if (level == 0.25) {
                    mBatteryImage.setImageResource(R.drawable.mark_battery04);
                } else {
                    mBatteryImage.setImageResource(R.drawable.mark_battery05);
                }
                mLevel.setText("" + (level * 100));
            }
        });

    }

}
