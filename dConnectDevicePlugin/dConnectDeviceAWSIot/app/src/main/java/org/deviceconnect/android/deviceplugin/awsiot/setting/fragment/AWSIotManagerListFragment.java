/*
 AWSIoTManagerListFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RDCMListManager;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.local.DConnectHelper;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AWS Iot Manager List Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotManagerListFragment extends Fragment {

    /** LOG Tag */
    private static final String TAG = AWSIotManagerListFragment.class.getCanonicalName();

    /** Adapter. */
    private ManagerAdapter mManagerAdapter;

    /** Instance of {@link RDCMListManager}. */
    private RDCMListManager mRDCMListManager;

    /** Device Connect Manager enable flag. */
    private boolean mMyManagerEnable = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        /* Instance of {@link AWSIotDeviceApplication}. */
        AWSIotDeviceApplication app = (AWSIotDeviceApplication) getContext().getApplicationContext();
        mRDCMListManager = app.getRDCMListManager();

        ManagerListUpdateDialogFragment dialog = new ManagerListUpdateDialogFragment();
        dialog.show(getFragmentManager(),"AvailabilityDialog");

        availability();

        List<RemoteDeviceConnectManager> managers = new ArrayList<>();
        mManagerAdapter = new ManagerAdapter(getActivity(), managers);
        View rootView = inflater.inflate(R.layout.settings_manager_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.manager_list_view);
        listView.setAdapter(mManagerAdapter);

        Button btn = (Button) rootView.findViewById(R.id.button_awsiot_sync);
        btn.setAllCaps(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getManagerList();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getManagerList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        boolean ret = true;
        switch (item.getItemId()) {
            case R.id.menu_awsiot_info:
                if (mMyManagerEnable) {
                    AWSIotInformationFragment infoFragment = new AWSIotInformationFragment();
                    transaction.add(R.id.container, infoFragment);
                    transaction.addToBackStack("ManagerList");
                    transaction.commit();
                } else {
                    showManagerCheckDialog();
                }
                break;
            case R.id.menu_device_auth:
                if (mMyManagerEnable) {
                    AWSIotDeviceAuthenticationFragment deviceAuthFragment = new AWSIotDeviceAuthenticationFragment();
                    transaction.add(R.id.container, deviceAuthFragment);
                    transaction.addToBackStack("ManagerList");
                    transaction.commit();
                } else {
                    showManagerCheckDialog();
                }
                break;
            case R.id.menu_logout:
                AWSIotDeviceApplication.getInstance().logoutAWSIot();

                // ログイン画面へ遷移
                AWSIotLoginFragment loginFragment = new AWSIotLoginFragment();
                transaction.replace(R.id.container, loginFragment);
                transaction.commit();
                break;
            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }
        return ret;
    }

    private void getManagerList() {
        ManagerListUpdateDialogFragment dialog = new ManagerListUpdateDialogFragment();
        dialog.show(getFragmentManager(),"ManagerListDialog");

        mRDCMListManager.updateManagerList(new RDCMListManager.UpdateManagerListCallback() {
            @Override
            public void onUpdateManagerList(final List<RemoteDeviceConnectManager> managerList) {
                mManagerAdapter.clear();
                mManagerAdapter.addAll(mRDCMListManager.getRDCMList());
                mManagerAdapter.notifyDataSetInvalidated();
                ManagerListUpdateDialogFragment dialog = (ManagerListUpdateDialogFragment) getFragmentManager().findFragmentByTag("ManagerListDialog");
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    private class ManagerAdapter extends ArrayAdapter<RemoteDeviceConnectManager> {
        private LayoutInflater mInflater;

        public ManagerAdapter(final Context context, final List<RemoteDeviceConnectManager> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_manager, null);
            }

            final RemoteDeviceConnectManager manager = getItem(position);
            String name = manager.getName();

            Switch sw = (Switch) convertView.findViewById(R.id.manager_switch_onoff);
            sw.setText(name);
            if (manager.isSubscribe()) {
                sw.setChecked(true);
            } else {
                sw.setChecked(false);
            }

            sw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (manager.isSubscribe()) {
                        // DB変更処理(flag = false)
                        mRDCMListManager.updateSubscribe(manager.getServiceId(), false);
                        manager.setSubscribeFlag(false);
                        // ボタン変更
                        Switch sw = (Switch) v.findViewById(R.id.manager_switch_onoff);
                        sw.setChecked(false);
                    } else {
                        // DB変更処理(flag = true)
                        mRDCMListManager.updateSubscribe(manager.getServiceId(), true);
                        manager.setSubscribeFlag(true);
                        // ボタン変更
                        Switch sw = (Switch) v.findViewById(R.id.manager_switch_onoff);
                        sw.setChecked(true);
                    }
                }
            });

            return convertView;
        }
    }

    private void availability() {
        DConnectHelper.INSTANCE.availability(new DConnectHelper.FinishCallback() {
            @Override
            public void onFinish(String response, Exception error) {
                ManagerListUpdateDialogFragment dialog = (ManagerListUpdateDialogFragment) getFragmentManager().findFragmentByTag("AvailabilityDialog");
                if (response == null) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        mMyManagerEnable = true;
                        AWSIotPrefUtil pref = ((AWSIotSettingActivity) getContext()).getPrefUtil();
                        String name = jsonObject.optString("name");
                        String uuid = jsonObject.optString("uuid");
                        if (name == null || uuid == null) {
                            String prefName = pref.getManagerName();
                            name = "TEST";
                            if (prefName.matches(name)) {
                                uuid = pref.getManagerUuid();
                            }
                            if (uuid == null) {
                                uuid = UUID.randomUUID().toString();
                            }
                        }
                        pref.setManagerName(name);
                        pref.setManagerUuid(uuid);
                    } else {
                        mMyManagerEnable = false;
                        showManagerCheckDialog();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * Show a dialog of manager list update.
     */
    public static class ManagerListUpdateDialogFragment extends DialogFragment {
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

    private void showManagerCheckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.setting_confirm_title)
                .setMessage(R.string.setting_manager_off_warning)
                .setPositiveButton(R.string.webview_js_alert_positive_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // No Operation.
                    }
                });
        builder.show();
    }
}
