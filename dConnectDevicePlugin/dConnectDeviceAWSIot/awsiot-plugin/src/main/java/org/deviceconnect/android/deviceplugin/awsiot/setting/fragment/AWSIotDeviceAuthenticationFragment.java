/*
 AWSIotDeviceAuthenticationFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;


import org.deviceconnect.android.deviceplugin.awsiot.DConnectLocalHelper;
import org.deviceconnect.android.deviceplugin.awsiot.core.LocalDevice;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AWS IoT Settings Fragment Page 4.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceAuthenticationFragment extends Fragment {
    /** Local Device Infomation Adapter */
    private LocalDeviceAdapter mDeviceAdapter;

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

        serviceDiscovery(new DConnectLocalHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        mDeviceAdapter.clear();
                        mDeviceAdapter.addAll(createLocalDevices(response));
                        mDeviceAdapter.notifyDataSetInvalidated();
                    } else {
                        // TODO Managerが起動していない場合の処理
                    }
                    DeviceListUpdateDialogFragment dialog = (DeviceListUpdateDialogFragment) getFragmentManager().findFragmentByTag("DeviceListDialog");
                    if (dialog != null) {
                        dialog.dismiss();
                    }

                } catch (JSONException e) {
//                    Log.e(TAG, "", e);
                }
            }
        });

        DeviceListUpdateDialogFragment dialog = new DeviceListUpdateDialogFragment();
        dialog.show(getFragmentManager(),"DeviceListDialog");

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private List<LocalDevice> createLocalDevices(final String data) {
        List<LocalDevice> devices = new ArrayList<>();
        // TODO:JSON解析、Id, Device名取得。
        try {
            JSONObject jsonObject = new JSONObject(data);
            int result = jsonObject.getInt("result");
            if (result == 0) {
                JSONArray services = jsonObject.optJSONArray("services");
                for (int i = 0; i < services.length(); i++) {
                    JSONObject service = services.getJSONObject(i);
                    String id = service.getString("id");
                    String name = service.getString("name");
                    devices.add(new LocalDevice(id, name));
                }
            } else {
                // TODO Managerが起動していない場合の処理
            }
        } catch (JSONException e) {
//                    Log.e(TAG, "", e);
        }

        return devices;
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
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Button btn = (Button) v.findViewById(R.id.btn_auth_device);
                    serviceInformation(device.getServiceId(), new DConnectLocalHelper.FinishCallback() {
                        @Override
                        public void onFinish(String response, Exception error) {
                            if (response == null) {
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
                                } else {
                                    // TODO Managerが起動していない場合の処理
                                }

                            } catch (JSONException e) {

                            }
                        }
                    });
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

    private void serviceDiscovery(final DConnectLocalHelper.FinishCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DConnectLocalHelper.INSTANCE.sendRequest("GET", "http://localhost:4035/gotapi/servicediscovery", callback);
            }
        }).start();
    }

    private void serviceInformation(final String serviceId, final DConnectLocalHelper.FinishCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> param = new HashMap<>();
                param.put("serviceId", serviceId);
                DConnectLocalHelper.INSTANCE.sendRequest("GET", "http://localhost:4035/gotapi/serviceinformation", param, callback);
            }
        }).start();
    }

}
