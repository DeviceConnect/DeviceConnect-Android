/*
 AWSIoTManagerListFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotDBHelper;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.local.DConnectHelper;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;
import org.deviceconnect.android.deviceplugin.awsiot.util.AWSIotUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AWS IoT Settings Fragment Page 2.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotManagerListFragment extends Fragment {

    /** LOG Tag */
    private static final String TAG = AWSIotManagerListFragment.class.getCanonicalName();

    /** Adapter. */
    private ManagerAdapter mManagerAdapter;

    /** Instance of {@link AWSIotDBHelper}. */
    private AWSIotDBHelper mDBHelper;

    /**
     * 遠隔にあるDevice Connect Managerのリスト.
     * TODO AWSIotDeviceApplicationで管理した方が良いかもしれない。
     */
    private List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        mDBHelper = new AWSIotDBHelper(getActivity());

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
                AWSIotInformationFragment infoFragment = new AWSIotInformationFragment();
                transaction.add(R.id.container, infoFragment);
                transaction.addToBackStack("ManagerList");
                transaction.commit();
                break;
            case R.id.menu_device_auth:
                AWSIotDeviceAuthenticationFragment deviceAuthFragment = new AWSIotDeviceAuthenticationFragment();
                transaction.add(R.id.container, deviceAuthFragment);
                transaction.addToBackStack("ManagerList");
                transaction.commit();
                break;
            case R.id.menu_logout:
                // ログイン画面へ遷移

                getAWSIotController().disconnect();

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

        getAWSIotController().getShadow(AWSIotUtil.KEY_DCONNECT_SHADOW_NAME, new AWSIotController.GetShadowCallback() {
            @Override
            public void onReceivedShadow(final String thingName, final String result, final Exception err) {
                mManagerList = AWSIotUtil.parseDeviceShadow(getActivity(), result);
                mManagerAdapter.clear();
                mManagerAdapter.addAll(mManagerList);
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
                        updateSubscribeFlag(manager.getServiceId(), false);
                        manager.setSubscribeFlag(false);
                        // ボタン変更
                        Switch sw = (Switch) v.findViewById(R.id.manager_switch_onoff);
                        sw.setChecked(false);
                    } else {
                        // DB変更処理(flag = true)
                        updateSubscribeFlag(manager.getServiceId(), true);
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

    private AWSIotController getAWSIotController() {
        return ((AWSIotSettingActivity) getActivity()).getAWSIotController();
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
                        // TODO Managerが起動していない場合の処理
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

    /**
     * Update Subscribe flag.
     * @param id Manager Id.
     * @param flag subscribe flag.
     * @return true(Success) / false(Failed).
     */
    public boolean updateSubscribeFlag(final String id, final boolean flag) {
        boolean result = false;
        RemoteDeviceConnectManager manager = findRegisteredManagerById(id);
        if (manager != null) {
            int index = mManagerList.indexOf(manager);
            manager.setSubscribeFlag(flag);
            mDBHelper.updateManager(manager);
            mManagerList.set(index, manager);
            result = true;
        }
        return result;
    }

    /**
     * Find the {@link RemoteDeviceConnectManager} from id.
     *
     * @param id id of Manager
     * @return {@link RemoteDeviceConnectManager}, or null
     */
    private RemoteDeviceConnectManager findRegisteredManagerById(final String id) {
        synchronized (mManagerList) {
            for (RemoteDeviceConnectManager d : mManagerList) {
                if (d.getServiceId().equalsIgnoreCase(id)) {
                    return d;
                }
            }
        }
        return null;
    }
}
