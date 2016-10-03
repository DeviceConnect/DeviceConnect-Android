/*
 AWSIotDeviceAuthenticationFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;


import org.deviceconnect.android.deviceplugin.awsiot.cores.core.LocalDevice;
import org.deviceconnect.android.deviceplugin.awsiot.local.DConnectHelper;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * AWS Iot Device Authentication Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceAuthenticationFragment extends Fragment {
    /** Local Device Information Adapter */
    private LocalDeviceAdapter mDeviceAdapter;
    /** Service Discovery call flag. */
    private boolean mServiceDiscoveryCall = false;
    /** Service Information call flag. */
    private boolean mServiceInformationCall = false;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.settings_device_authentication, null);

        /* Device list view. */
        ListView listView = (ListView) rootView.findViewById(R.id.device_list_view);
        List<LocalDevice> devices = new ArrayList<>();
        mDeviceAdapter = new LocalDeviceAdapter(getActivity(), devices);
        listView.setAdapter(mDeviceAdapter);
        listView.setItemsCanFocus(true);

        mServiceDiscoveryCall = true;
        getDeviceList();


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mServiceDiscoveryCall) {
            DeviceListUpdateDialogFragment dialog = (DeviceListUpdateDialogFragment) getFragmentManager().findFragmentByTag("DeviceListDialog");
            if (dialog != null) {
                dialog.dismiss();
            }
            dialog = new DeviceListUpdateDialogFragment();
            dialog.show(getFragmentManager(),"DeviceListDialog");
        }
        if (mServiceInformationCall) {
            DeviceListUpdateDialogFragment dialog = (DeviceListUpdateDialogFragment) getFragmentManager().findFragmentByTag("ServiceInformationDialog");
            if (dialog != null) {
                dialog.dismiss();
            }
            dialog = new DeviceListUpdateDialogFragment();
            dialog.show(getFragmentManager(),"ServiceInformationDialog");
        }
    }

    private class LocalDeviceAdapter extends ArrayAdapter<LocalDevice> {
        private LayoutInflater mInflater;

        public LocalDeviceAdapter(final Context context, final List<LocalDevice> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_device, null);
            }

            final LocalDevice device = getItem(position);
            String name = device.getDeviceName();

            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
            nameView.setText(name);

            Button btn = (Button) convertView.findViewById(R.id.btn_auth_device);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getServiceInformation(device);
                }
            });
            return convertView;
        }
    }

    /**
     * Show a dialog of device list update.
     */
    public static class DeviceListUpdateDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            String msg = getString(R.string.manager_list_update);
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage(msg);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            setCancelable(false);
            return progressDialog;
        }

        @Override
        public void onPause() {
            dismiss();
            super.onPause();
        }
    }

    private void getDeviceList() {
        DConnectHelper.INSTANCE.serviceDiscoverySelfOnly(new DConnectHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    DeviceListUpdateDialogFragment dialog = (DeviceListUpdateDialogFragment) getFragmentManager().findFragmentByTag("DeviceListDialog");
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    mServiceDiscoveryCall = false;
                    return;
                }

                List<LocalDevice> services = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        JSONArray array = jsonObject.getJSONArray("services");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            LocalDevice service = new LocalDevice(o.getString("id"), o.getString("name"));
                            services.add(service);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("AWS", "", e);
                }

                mDeviceAdapter.clear();
                mDeviceAdapter.addAll(services);
                mDeviceAdapter.notifyDataSetInvalidated();

                mServiceDiscoveryCall = false;
                DeviceListUpdateDialogFragment dialog = (DeviceListUpdateDialogFragment) getFragmentManager().findFragmentByTag("DeviceListDialog");
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void getServiceInformation(final LocalDevice device) {
        DeviceListUpdateDialogFragment dialog = new DeviceListUpdateDialogFragment();
        dialog.show(getFragmentManager(),"ServiceInformationDialog");
        mServiceInformationCall = true;

        DConnectHelper.INSTANCE.serviceInformation(device.getServiceId(), new DConnectHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    DeviceListUpdateDialogFragment dialog = (DeviceListUpdateDialogFragment) getFragmentManager().findFragmentByTag("ServiceInformationDialog");
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    mServiceInformationCall = false;
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        final String LF = System.getProperty("line.separator");
                        StringBuilder sb = new StringBuilder();
                        sb.append("Support Profile:").append(LF);

                        JSONArray supports = jsonObject.optJSONArray("supports");
                        for (int i = 0; i < supports.length(); i++) {
                            String support = supports.get(i).toString();
                            sb.append("・").append(support).append(LF);
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(device.getDeviceName())
                                .setMessage(sb)
                                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // No Operation.
                                    }
                                });
                        builder.show();
                    }
                } catch (JSONException e) {
                    Log.e("AWS", "", e);
                }

                DeviceListUpdateDialogFragment dialog = (DeviceListUpdateDialogFragment) getFragmentManager().findFragmentByTag("ServiceInformationDialog");
                if (dialog != null) {
                    dialog.dismiss();
                }
                mServiceInformationCall = false;
            }
        });
    }
}
