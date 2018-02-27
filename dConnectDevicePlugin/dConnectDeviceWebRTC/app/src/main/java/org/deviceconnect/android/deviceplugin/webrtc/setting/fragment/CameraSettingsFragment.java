/*
 CameraSettingsFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.setting.fragment;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingUtil;
import org.deviceconnect.android.deviceplugin.webrtc.util.CameraUtils;
import org.deviceconnect.android.deviceplugin.webrtc.util.CapabilityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * カメラ設定画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class CameraSettingsFragment extends Fragment {

    /**
     * Settings Adapter.
     */
    private SettingsAdapter mAdapter;

    /**
     * The format of the current camera.
     */
    private CameraUtils.CameraFormat mCameraFormat;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        String cameraText = SettingUtil.getCameraParam(getActivity());
        setCameraFormat(cameraText);
        mAdapter = new SettingsAdapter();

        View root = inflater.inflate(R.layout.settings_camera, null);
        ListView listView = (ListView) root.findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (position == 0) {
                    openCameraFacing();
                } else if (position == 1) {
                    openCameraSize();
                }
            }
        });
        return root;
    }

    private void setCameraFormat(final String cameraText) {
        mCameraFormat = CameraUtils.textToFormat(cameraText);
        if (mCameraFormat == null) {
            mCameraFormat = CameraUtils.getDefaultFormat();
        }
    }
    /**
     * Shows the dialog of camera facing.
     */
    private void openCameraFacing() {
        String title = getString(R.string.settings_camera_facing);

        int selected = 0;
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.settings_camera_facing_back));
        if (CameraUtils.hasFrontFacingDevice()) {
            list.add(getString(R.string.settings_camera_facing_front));
            if (mCameraFormat.getFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                selected = 1;
            }
        }

        ItemSelectDialogFragment fragment = ItemSelectDialogFragment.create(title, list, selected);
        fragment.setOnSelectItemListener(new ItemSelectDialogFragment.OnSelectItemListener() {
            @Override
            public void onSelected(final String text, final int which) {
                if (which == 0) {
                    mCameraFormat.setFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else if (which == 1) {
                    mCameraFormat.setFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
                SettingUtil.setCameraParam(getActivity(), CameraUtils.formatToText(mCameraFormat));
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCanceled() {
            }
        });
        fragment.show(getActivity().getSupportFragmentManager(), "");
    }

    /**
     * Shows the dialog of camera resolution.
     */
    private void openCameraSize() {
        String title = getString(R.string.settings_camera_size);

        int selected = 0;
        List<String> list = new ArrayList<>();
        List<CameraUtils.CameraFormat> formats = CameraUtils.getSupportedFormats(mCameraFormat.getFacing());
        for (CameraUtils.CameraFormat format : formats) {
            list.add(format.getWidth() + "x" + format.getHeight());
            if (format.getWidth() == mCameraFormat.getWidth() &&
                    format.getHeight() == mCameraFormat.getHeight()) {
                selected = list.size() - 1;
            }
        }

        ItemSelectDialogFragment fragment = ItemSelectDialogFragment.create(title, list, selected);
        fragment.setOnSelectItemListener(new ItemSelectDialogFragment.OnSelectItemListener() {
            @Override
            public void onSelected(final String text, final int which) {
                String[] size = text.split("x");
                if (size.length == 2) {
                    mCameraFormat.setWidth(Integer.parseInt(size[0]));
                    mCameraFormat.setHeight(Integer.parseInt(size[1]));
                }
                SettingUtil.setCameraParam(getActivity(), CameraUtils.formatToText(mCameraFormat));
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCanceled() {
            }
        });
        fragment.show(getActivity().getSupportFragmentManager(), "");
    }

    private class SettingsAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private SettingsAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(final int position) {
            return null;
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = null;
            if (position == 0) {
                view = mInflater.inflate(R.layout.item_settings_param, parent, false);
                String value;
                if (mCameraFormat.getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    value = getString(R.string.settings_camera_facing_back);
                } else {
                    value = getString(R.string.settings_camera_facing_front);
                }
                setViewText(view, getString(R.string.settings_camera_facing), value);
            } else if (position == 1) {
                view = mInflater.inflate(R.layout.item_settings_param, parent, false);
                String value = mCameraFormat.getWidth() +"x" + mCameraFormat.getHeight();
                setViewText(view, getString(R.string.settings_camera_size), value);
            } else if (position == 2) {
                view = mInflater.inflate(R.layout.item_settings_param, parent, false);
                setViewText(view, getString(R.string.settings_camera_fps), "" + mCameraFormat.getMaxFrameRate());
            }
            return view;
        }

        private void setViewText(final View view, final String title, final String value) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(title);
            }

            TextView valueView = (TextView) view.findViewById(R.id.value);
            if (valueView != null) {
                valueView.setText(value);
            }
        }
    }
}
