/*
 ProximityProfileFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.fragment.profile;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.fragment.SmartDeviceFragment;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.event.EventHandler;
import org.deviceconnect.message.http.event.HttpEventManager;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Proximityプロファイル用フラグメント.
 * @author NTT DOCOMO, INC.
 */
public class ProximityProfileFragment extends SmartDeviceFragment {
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_proximity_service, container, false);
        view.setBackgroundColor(getResources().getColor(android.R.color.background_light));

        final Switch proximity = (Switch) view.findViewById(R.id.fragment_proximity_ondevice);
        proximity.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    registerEvent();
                } else {
                    unregisterEvent();
                }
            }
        });
        
        return view;
    }
    

    /**
     * イベントを登録する.
     */
    private void registerEvent() {
        (new AsyncTask<Void, Void, DConnectMessage>() {
            @Override
            public DConnectMessage doInBackground(final Void ...args) {
                final TextView tv = (TextView) getView().findViewById(R.id.fragment_proximity_response);
                try {
                    URIBuilder builder = new URIBuilder();
                    if (isSSL()) {
                        builder.setScheme("https");
                    } else {
                        builder.setScheme("http");
                    }
                    builder.setHost(getHostName());
                    builder.setPort(getPort());
                    builder.setProfile(ProximityProfileConstants.PROFILE_NAME);
                    builder.setAttribute(ProximityProfileConstants.ATTRIBUTE_ON_DEVICE_PROXIMITY);
                    builder.addParameter(DConnectMessage.EXTRA_SERVICE_ID, getSmartDevice().getId());
                    builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
                    builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, getClientId());
                    HttpResponse response = HttpEventManager.INSTANCE.registerEvent(builder, new EventHandler() {
                        @Override
                        public void onEvent(final JSONObject event) {
                            if (event != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv.setText(event.toString());
                                    }
                                });
                            }
                        }
                    });
                    return (new HttpMessageFactory()).newDConnectMessage(response);
                } catch (IOException e) {
                    Log.e("ABC", "ABC", e);
                }
                return null;
            }
            @Override
            protected void onPostExecute(final DConnectMessage result) {
                if (getActivity().isFinishing()) {
                    return;
                }

                if (result != null) {
                    TextView tv = (TextView) getView().findViewById(R.id.fragment_proximity_event);
                    tv.setText(result.toString());
                }
            }
        }).execute();
    }
    /**
     * イベントを登録する.
     */
    private void unregisterEvent() {
        (new AsyncTask<Void, Void, DConnectMessage>() {
            public DConnectMessage doInBackground(final Void ...args) {
                try {
                    URIBuilder builder = new URIBuilder();
                    if (isSSL()) {
                        builder.setScheme("https");
                    } else {
                        builder.setScheme("http");
                    }
                    builder.setHost(getHostName());
                    builder.setPort(getPort());
                    builder.setProfile(DeviceOrientationProfileConstants.PROFILE_NAME);
                    builder.setAttribute(DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION);
                    builder.addParameter(DConnectMessage.EXTRA_SERVICE_ID, getSmartDevice().getId());
                    builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
                    builder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, getClientId());
                    HttpEventManager.INSTANCE.unregisterEvent(builder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).execute();
    }
}
