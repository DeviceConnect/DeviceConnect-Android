/*
 HVCC2WAccountRegisterFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.deviceconnect.android.deviceplugin.hvcc2w.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcc2w.R;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCStorage;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.UserDataObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * HVC-C2W Settings Fragment Page 2.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WAccountRegisterFragment extends Fragment {

    /** Signup button. */
    private Button mSignup;
    /** Login button. */
    private Button mLogin;
    /** Logout button.*/
    private Button mLogout;
    /** Email EditText. */
    private EditText mAddress;
    /** Password EditText. */
    private EditText mPassword;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.setting_account, null);
        mAddress = (EditText) root.findViewById(R.id.input_email);
        mPassword = (EditText) root.findViewById(R.id.input_password);
        mSignup = (Button) root.findViewById(R.id.signup);
        mSignup.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                final String email = mAddress.getText().toString();
                if (!email.isEmpty()) {
                    String message = String.format(getString(R.string.c2w_setting_message_2_5), email);
                    HVCC2WDialogFragment.showConfirmAlert(getActivity(), getString(R.string.hw_name),
                            message, getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    exeSignup(email);
                                }
                            });
                } else {
                    HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_setting_error_1), null);
                }

            }
        });

        mLogin = (Button) root.findViewById(R.id.login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exeLogin();
            }
        });
        mLogout = (Button) root.findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exeLogout();
            }
        });
        enableButtons();
        return root;
    }

    /** Execute Signup.*/
    private void exeSignup(final String email) {
        HVCManager.INSTANCE.signup(email, new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {
                try {
                    if (json == null) {
                        HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                getString(R.string.c2w_setting_error_4), null);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject result = jsonObject.getJSONObject("result");
                    String code = result.getString("code");
                    String msg = result.getString("msg");
                    if (BuildConfig.DEBUG) {
                        Log.d("ABC", String.format("response=%s(%s)", code, msg));
                    }
                    if (msg.equals("success")) {
                        HVCC2WDialogFragment.showConfirmAlert(getActivity(), getString(R.string.hw_name),
                                getString(R.string.c2w_setting_message_2_2),
                                getString(R.string.button_gmail), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        intent.setAction("android.intent.category.LAUNCHER");
                                        intent.setClassName("com.google.android.gm",
                                                "com.google.android.gm.ConversationListActivityGmail");
                                        intent.setFlags(0x10200000);
                                        startActivity(intent);

                                    }
                                });
                    } else {
                        HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                getString(R.string.c2w_setting_error_4), null);
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                            getString(R.string.c2w_setting_error_4), null);

                }
            }
        });
    }

    /** Execute Login. */
    private void exeLogin() {
        String email = mAddress.getText().toString();
        String password = mPassword.getText().toString();
        if (!email.isEmpty() && !password.isEmpty()) {
            HVCManager.INSTANCE.login(getContext(), email, password, new HVCManager.ResponseListener() {
                @Override
                public void onReceived(String json) {
                    try {
                        if (json == null) {
                            HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_setting_error_2), null);
                            return;
                        }

                        JSONObject jsonObject = new JSONObject(json);
                        JSONObject result = jsonObject.getJSONObject("result");
                        String code = result.getString("code");
                        String msg = result.getString("msg");
                        if (BuildConfig.DEBUG) {
                            Log.d("ABC", String.format("response=%s(%s)", code, msg));
                        }
                        if (msg.equals("success")) {
                            HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                    getString(R.string.c2w_setting_message_2_3), null);
                            mLogout.setVisibility(View.VISIBLE);
                        } else {
                            HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                    getString(R.string.c2w_setting_error_2), null);
                        }
                    } catch (JSONException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                        HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                getString(R.string.c2w_setting_error_2), null);

                    }
                }
            });
        } else {
            HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_setting_error_2), null);
        }
    }


    /** Execute Logout.*/
    private void exeLogout() {
        HVCManager.INSTANCE.logout(new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {
                try {
                    if (json == null) {
                        HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                getString(R.string.c2w_setting_error_3), null);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject result = jsonObject.getJSONObject("result");
                    String code = result.getString("code");
                    String msg = result.getString("msg");
                    if (BuildConfig.DEBUG) {
                        Log.d("ABC", String.format("response=%s(%s)", code, msg));
                    }
                    if (msg.equals("success")) {
                        mAddress.setText("");
                        mPassword.setText("");
                        HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                getString(R.string.c2w_setting_message_2_4), null);
                        mLogout.setVisibility(View.GONE);
                    } else {
                        HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                                getString(R.string.c2w_setting_error_3), null);
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name),
                            getString(R.string.c2w_setting_error_3), null);

                }
            }
        });
    }

    /* Enable/Disable singup/login/logout buttons.*/
    private void enableButtons() {
        List<UserDataObject> users = HVCStorage.INSTANCE.getUserDatas(null);
        if (users.size() > 0) {
            UserDataObject user = users.get(0);
            mAddress.setText(user.getEmail());
            mPassword.setText(user.getPassword());
            mLogout.setVisibility(View.VISIBLE);
        } else {
            mLogout.setVisibility(View.GONE);
        }
    }
}
