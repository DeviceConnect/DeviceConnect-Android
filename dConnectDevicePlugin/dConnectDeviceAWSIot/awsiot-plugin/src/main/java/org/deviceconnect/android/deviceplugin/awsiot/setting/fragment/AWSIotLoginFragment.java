/*
 AWSIoTLoginFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceService;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.local.AWSIotLocalDeviceService;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;

/**
 * AWS IoT Settings Fragment Page 1.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotLoginFragment extends Fragment {

    /** Access Key EditText. */
    private EditText mAccessKey;
    /** Secret Key EditText. */
    private EditText mSecretKey;
    /** Region select Spinner. */
    private Spinner mRegion;
    /** AWSIotPrefUtil instance. */
    private AWSIotPrefUtil mPrefUtil;
    /** AWSIotController instance. */
    private AWSIotController mAWSIotController;
    /** AWS IoT Regions list. */
    private final Regions[] regionsList = {
            Regions.US_EAST_1,
            Regions.US_WEST_2,
            Regions.AP_SOUTHEAST_1,
            Regions.AP_SOUTHEAST_2,
            Regions.AP_NORTHEAST_1,
            Regions.EU_CENTRAL_1,
            Regions.EU_WEST_1
    };


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setHasOptionsMenu(false);

        mAWSIotController = ((AWSIotSettingActivity) getActivity()).getAWSIotController();

        mPrefUtil = ((AWSIotSettingActivity) getContext()).getPrefUtil();

        View rootView = inflater.inflate(R.layout.settings_login, null);
        mAccessKey = (EditText) rootView.findViewById(R.id.input_awsiot_id);
        mAccessKey.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeSoftwareKeyboard(v);
                }
            }
        });
        String accessKey = mPrefUtil.getAccessKey();
        if (accessKey != null) {
            mAccessKey.setText(accessKey);
        }

        mSecretKey = (EditText) rootView.findViewById(R.id.input_awsiot_password);
        mSecretKey.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeSoftwareKeyboard(v);
                }
            }
        });
        String secretKey = mPrefUtil.getSecretKey();
        if (secretKey != null) {
            mSecretKey.setText(secretKey);
        }

        String[] regionList = getResources().getStringArray(R.array.region_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_region, regionList);
        mRegion = (Spinner) rootView.findViewById(R.id.spn_region);
        mRegion.setAdapter(adapter);
        Regions regions = mPrefUtil.getRegions();
        if (regions != null) {
            for (int i = 0; i < regionsList.length; i++) {
                if (regionsList[i] == regions) {
                    mRegion.setSelection(i);
                }
            }
        }

        /* Login button. */
        Button login = (Button) rootView.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO:AWS IoT ログイン処理
                String accessKey = mAccessKey.getText().toString();
                if (accessKey.length() == 0) {
                    Toast.makeText(getContext(), "Access Keyを入力して下さい。", Toast.LENGTH_LONG).show();
                    return;
                }
                String secretKey = mSecretKey.getText().toString();
                if (secretKey.length() == 0) {
                    Toast.makeText(getContext(), "Secret Keyを入力して下さい。", Toast.LENGTH_LONG).show();
                    return;
                }

                Regions region = regionsList[(int)mRegion.getSelectedItemId()];

                mPrefUtil.setAccessKey(accessKey);
                mPrefUtil.setSecretKey(secretKey);
                mPrefUtil.setRegions(region);

                Log.d("ABC", "accessKey   : " + accessKey);
                Log.d("ABC", "secretKey   : " + secretKey);
                Log.d("ABC", "Index       : " + mRegion.getSelectedItemId());
                Log.d("ABC", "IndexRegion : " + regionsList[(int)mRegion.getSelectedItemId()].toString());
                Log.d("ABC", "region      : " + region);

                DuringLoginDialogFragment dialog = new DuringLoginDialogFragment();
                dialog.show(getFragmentManager(),"DuringDialog");

                mAWSIotController.connect(accessKey, secretKey, region, new AWSIotController.ConnectCallback() {
                    @Override
                    public void onConnected(final Exception err) {
                        DuringLoginDialogFragment dialog = (DuringLoginDialogFragment) getFragmentManager().findFragmentByTag("DuringDialog");
                        if (dialog != null) {
                            dialog.dismiss();
                        }

                        if (err != null) {
                            Toast.makeText(getContext(), "ログインに失敗しました。Access Key, Secret Key, リージョンを確認して下さい。", Toast.LENGTH_LONG).show();
                            return;
                        }

                        mPrefUtil.setAWSLoginFlag(true);
                        // Managerリスト一覧へ遷移
                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        AWSIotManagerListFragment fragment = new AWSIotManagerListFragment();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.container, fragment);
                        transaction.commit();

                        startAWSIot();
                    }
                });
            }
        });

        /* Help button. */
        FloatingActionButton fabHelp = (FloatingActionButton) rootView.findViewById(R.id.fab_help);
        fabHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                AWSIotHelpFragment help = new AWSIotHelpFragment();
                transaction.add(R.id.container, help);
                transaction.addToBackStack("LoginPage");
                transaction.commit();
            }
        });

        return rootView;
    }

    private void startAWSIot() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), AWSIotDeviceService.class);
        intent.setAction(AWSIotDeviceService.ACTION_CONNECT_MQTT);
        getActivity().startService(intent);

        if (mPrefUtil.getManagerRegister()) {
            Intent intent2 = new Intent();
            intent2.setClass(getActivity(), AWSIotLocalDeviceService.class);
            intent2.setAction(AWSIotLocalDeviceService.ACTION_START);
            getActivity().startService(intent2);
        }
    }

    /**
     * Close software Keyboard
     */
    private void closeSoftwareKeyboard(final View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Show a dialog of AWSIoT Login.
     */
    public static class DuringLoginDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            String msg = getString(R.string.duringLogin);
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
