/*
 SystemProfileFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.fragment.profile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.fragment.SmartDevicePreferenceFragment;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.basic.message.DConnectResponseMessage;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.utils.URIBuilder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * コネクションサービスフラグメント.
 */
public class SystemProfileFragment extends SmartDevicePreferenceFragment {

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("deviceconnect.uiapp");

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        mLogger.entering(getClass().getName(), "onCreate", savedInstanceState);
        super.onCreate(savedInstanceState);

        // load system profile layout
        addPreferencesFromResource(R.xml.system_profile);

        // load current connection statuses
        (new SystemLoader()).execute();

        mLogger.exiting(getClass().getName(), "onCreate");
    }

    @Override
    public View onCreateView(final LayoutInflater paramLayoutInflater,
            final ViewGroup paramViewGroup, final Bundle paramBundle) {
        mLogger.entering(getClass().getName(), "onCreateView",
                new Object[]{paramLayoutInflater, paramViewGroup, paramBundle});

        View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        view.setBackgroundColor(getResources().getColor(android.R.color.background_light));

        mLogger.exiting(getClass().getName(), "onCreateView", view);
        return view;
    }

    /**
     * システムローダー.
     */
    private class SystemLoader extends AsyncTask<Object, Integer, DConnectMessage> {

        @Override
        protected DConnectMessage doInBackground(final Object... args) {
            mLogger.entering(getClass().getName(), "doInBackground", args);

            DConnectMessage message;

            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
            uriBuilder.addParameter(DConnectMessage.EXTRA_SERVICE_ID, getSmartDevice().getId());
            uriBuilder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

            try {
                mLogger.fine("request: " + uriBuilder.build().toString());
                HttpResponse response = sendHttpRequest(new HttpGet(uriBuilder.build()));
                mLogger.fine("response: " + response.toString());

                message = (new HttpMessageFactory()).newDConnectMessage(response);
            } catch (IOException e) {
                message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
            } catch (URISyntaxException e) {
                message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
            }

            mLogger.exiting(getClass().getName(), "doInBackground", message);
            return message;

        }

        @Override
        protected void onPostExecute(final DConnectMessage result) {
            mLogger.entering(getClass().getName(), "onPostExecute", result);
            super.onPostExecute(result);

            if (getActivity().isFinishing()) {
                return;
            }

            if (result.getInt(DConnectMessage.EXTRA_RESULT)
                    != DConnectMessage.RESULT_OK) {
                return;
            }

            // set version
            findPreference(getString(R.string.key_system_profile_version))
                    .setSummary(result.getString(SystemProfileConstants.PARAM_VERSION));

            // set profile list
            PreferenceCategory profileList = (PreferenceCategory) findPreference(
                    getString(R.string.key_system_profile_list));

            List<Object> support = result.getList(SystemProfileConstants.PARAM_SUPPORTS);
            if (support != null) {
                for (Object object: support) {
                    String name = (String) object;
                    if (name != null) {
                        Preference pref = new Preference(getActivity());
                        pref.setTitle(name);
                        profileList.addPreference(pref);
                    }
                }
            }

            mLogger.exiting(getClass().getName(), "onPostExecute");
        }
    }

}
