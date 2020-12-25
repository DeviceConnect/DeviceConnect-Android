package org.deviceconnect.android.deviceplugin.host.activity.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.HostDevicePluginBindFragment;
import org.deviceconnect.android.deviceplugin.host.databinding.FragmentHostEventMainBinding;

public class HostEventProfileFragment extends HostDevicePluginBindFragment {

    private GestureDetector mGestureDetector;

    private final GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(final MotionEvent event) {
            return super.onDoubleTap(event);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentHostEventMainBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_host_event_main, container, false);

        mGestureDetector = new GestureDetector(getContext(), mSimpleOnGestureListener);

        View root = binding.getRoot();
        root.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: // 1st touch only.
                case MotionEvent.ACTION_POINTER_DOWN: // Others touch.
                case MotionEvent.ACTION_UP: // Last touch remove only.
                case MotionEvent.ACTION_POINTER_UP: // Others touch move.
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                default:
                    break;
            }
            return mGestureDetector.onTouchEvent(event);
        });

        return root;
    }

    @Override
    public void onBindService() {
    }

    @Override
    public void onUnbindService() {
    }

    private void postTouchEvent() {

    }
}
