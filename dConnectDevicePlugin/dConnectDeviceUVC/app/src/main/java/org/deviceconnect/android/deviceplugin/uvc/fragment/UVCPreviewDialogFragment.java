/*
 UVCPreviewDialogFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import org.deviceconnect.android.deviceplugin.uvc.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCDeviceSettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UVCPreviewDialogFragment extends DialogFragment {

    private static final String TAG = UVCPreviewDialogFragment.class.getSimpleName();

    private static final Logger LOGGER = Logger.getLogger("uvc.dplugin");

    private UVCDeviceManager mDeviceMgr;

    private Spinner mSpinner;

    private BaseAdapter mListAdapter;

    private OnSelectListener mOnSelectListener;

    private DialogInterface.OnClickListener mOnClickListener =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        final Object item = mSpinner.getSelectedItem();
                        if (item instanceof UVCDevice) {
                            if (mOnSelectListener != null) {
                                mOnSelectListener.onSelect((UVCDevice) item);
                            }
                        }
                        break;
                }
            }
        };

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mDeviceMgr = ((UVCDeviceSettingsActivity) activity).getDeviceManager();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View root = getActivity().getLayoutInflater().inflate(com.serenegiant.uvccamera.R.layout.dialog_camera, null);
        mSpinner = (Spinner) root.findViewById(com.serenegiant.uvccamera.R.id.spinner1);
        final View empty = root.findViewById(android.R.id.empty);
        mSpinner.setEmptyView(empty);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root);
        builder.setTitle(com.serenegiant.uvccamera.R.string.select);
        builder.setPositiveButton(android.R.string.ok, mOnClickListener);
        builder.setNegativeButton(android.R.string.cancel , mOnClickListener);
        builder.setNeutralButton(com.serenegiant.uvccamera.R.string.refresh, null);
        final Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadDevices();
    }

    private void reloadDevices() {
        mListAdapter = new DeviceListAdapter(getActivity(), mDeviceMgr.getDeviceList());
        mSpinner.setAdapter(mListAdapter);
    }

    public static DialogFragment show(final Activity activity,
                                      final OnSelectListener listener) {
        LOGGER.info("Shown PreviewDialog.");
        UVCPreviewDialogFragment dialog = new UVCPreviewDialogFragment();
        dialog.mOnSelectListener = listener;
        dialog.show(activity.getFragmentManager(), TAG);
        return dialog;
    }

    private static final class DeviceListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final List<UVCDevice> mList;

        public DeviceListAdapter(final Context context, final List<UVCDevice>list) {
            mInflater = LayoutInflater.from(context);
            mList = list != null ? list : new ArrayList<UVCDevice>();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public UVCDevice getItem(final int position) {
            if ((position >= 0) && (position < mList.size())) {
                return mList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(com.serenegiant.uvccamera.R.layout.listitem_device, parent, false);
            }
            if (view instanceof CheckedTextView) {
                final UVCDevice device = getItem(position);
                ((CheckedTextView) view).setText(
                    String.format("UVC Device:(%x:%x:%s)", device.getVendorId(), device.getProductId(), device.getName()));
            }
            return view;
        }
    }

    public interface OnSelectListener {

        void onSelect(final UVCDevice device);

    }
}
