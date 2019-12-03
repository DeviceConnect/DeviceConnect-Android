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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceControlActivity;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.PoseEstimationData;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;
import org.deviceconnect.profile.PoseEstimationProfileConstants;


/**
 * This fragment do setting of the control health profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfilePoseEstimationFragment extends Fragment implements HitoeScheduler.OnRegularNotify {

    /**
     * Current Hitoe Device object.
     */
    private HitoeDevice mCurrentDevice;

    /**
     * Pose ImageView.
     */
    private ImageView mPoseView;
    /**
     * Hitoe scheduler.
     */
    private HitoeScheduler mScheduler;

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pose_instructions, null);
        mScheduler = new HitoeScheduler(this, HitoeConstants.HR_TEXT_UPDATE_CYCLE_TIME,
                                                HitoeConstants.HR_TEXT_UPDATE_CYCLE_TIME);
        rootView.findViewById(R.id.button_register).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(true);
        });
        rootView.findViewById(R.id.button_unregister).setOnClickListener((view) -> {
            mScheduler.scanHitoeDevice(false);
        });
        TextView title = rootView.findViewById(R.id.view_title);
        mPoseView = rootView.findViewById(R.id.pose_image);
        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                String[] profiles = getResources().getStringArray(R.array.support_profiles);
                title.setText(profiles[5] + getString(R.string.title_control));
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

            PoseEstimationData pose = manager.getPoseEstimationData(mCurrentDevice.getId());
            if (pose != null) {
                int poseResource = R.drawable.pose_default;
                if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.Backward) {
                    poseResource = R.drawable.pose_backward;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.FaceDown) {
                    poseResource = R.drawable.pose_facedown;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.FaceLeft) {
                    poseResource = R.drawable.pose_faceleft;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.FaceRight) {
                    poseResource = R.drawable.pose_faceright;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.FaceUp) {
                    poseResource = R.drawable.pose_faceup;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.Forward) {
                    poseResource = R.drawable.pose_forward;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.Leftside) {
                    poseResource = R.drawable.pose_leftside;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.Rightside) {
                    poseResource = R.drawable.pose_rightside;
                } else if (pose.getPoseState() == PoseEstimationProfileConstants.PoseState.Standing) {
                    poseResource = R.drawable.pose_standing;
                }
                mPoseView.setImageResource(poseResource);
            }
        });
    }
}
