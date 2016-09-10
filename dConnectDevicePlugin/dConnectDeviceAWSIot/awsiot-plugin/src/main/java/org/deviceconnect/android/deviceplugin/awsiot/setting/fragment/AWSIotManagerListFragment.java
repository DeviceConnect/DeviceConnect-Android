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
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.awsiot.DConnectLocalHelper;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotManager;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;
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

    /** 遠隔にあるDeviceConnectManagerを管理するクラス. */
    private AWSIotManager mAWSIotManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        availability();

        mAWSIotManager = new AWSIotManager(getActivity(), ((AWSIotSettingActivity) getActivity()).getAWSIotController());

        List<RemoteDeviceConnectManager> managers = new ArrayList<>();
        mManagerAdapter = new ManagerAdapter(getActivity(), managers);
        View rootView = inflater.inflate(R.layout.settings_manager_list, null);
        /* Manager list view. */
        ListView listView = (ListView) rootView.findViewById(R.id.manager_list_view);
        listView.setAdapter(mManagerAdapter);

        Button btn = (Button) rootView.findViewById(R.id.button_awsiot_sync);
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

                mAWSIotManager.getAWSIotController().disconnect();

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
        mAWSIotManager.getShadow(new AWSIotManager.GetShadowCallback() {
            @Override
            public void onReceivedShadow(final List<RemoteDeviceConnectManager> list) {
                if (list != null) {
                    mManagerAdapter.clear();
                    mManagerAdapter.addAll(list);
                    mManagerAdapter.notifyDataSetInvalidated();
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

            TextView nameView = (TextView) convertView.findViewById(R.id.manager_name);
            nameView.setText(name);

/*
            Switch sw = (Switch) convertView.findViewById(R.id.manager_switch_onoff);
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
                        mApp.getRemoteDCMManager().updateSubscribeFlag(manager.getId(), false);
                        manager.setSubscribeFlag(false);
                        // ボタン変更
                        Switch sw = (Switch) v.findViewById(R.id.manager_switch_onoff);
                        sw.setChecked(false);
                    } else {
                        // DB変更処理(flag = true)
                        mApp.getRemoteDCMManager().updateSubscribeFlag(manager.getId(), true);
                        manager.setSubscribeFlag(true);
                        // ボタン変更
                        Switch sw = (Switch) v.findViewById(R.id.manager_switch_onoff);
                        sw.setChecked(true);
                    }
                }
            });
*/
            Button btn = (Button) convertView.findViewById(R.id.manager_btn_onoff);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button btn = (Button) v.findViewById(R.id.manager_btn_onoff);
                    if (manager.isSubscribe()) {
                        // DB変更処理(flag = false)
                        mAWSIotManager.updateSubscribeFlag(manager.getServiceId(), false);
                        manager.setSubscribeFlag(false);
                        // ボタン変更
                        btn.setBackgroundResource(R.drawable.button_gray);
                        btn.setText(R.string.setting_btn_off);
                    } else {
                        // DB変更処理(flag = true)
                        mAWSIotManager.updateSubscribeFlag(manager.getServiceId(), true);
                        manager.setSubscribeFlag(true);
                        // ボタン変更
                        btn.setBackgroundResource(R.drawable.button_blue);
                        btn.setText(R.string.setting_btn_on);
                    }
                }
            });
            if (manager.isSubscribe()) {
                btn.setText(R.string.setting_btn_on);
                btn.setBackgroundResource(R.drawable.button_blue);
            } else {
                btn.setBackgroundResource(R.drawable.button_gray);
                btn.setText(R.string.setting_btn_off);
            }
            return convertView;
        }
    }

    private void availability() {
        DConnectLocalHelper.INSTANCE.availability(new DConnectLocalHelper.FinishCallback() {
            @Override
            public void onFinish(String response, Exception error) {
                if (response == null) {
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
                            // TODO 古いManager場合の処理
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
}
