/*
 WearServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * ServiceDiscoveryProfile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearServiceDiscoveryProfile extends ServiceDiscoveryProfile implements ConnectionCallbacks,
        OnConnectionFailedListener {

    /**
     * Google Play Service.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Service ID.
     */
    public static final String SERVICE_ID = "Wear";

    /**
     * Device Name: {@value} .
     */
    public static final String DEVICE_NAME = "Android Wear";

    /**
     * Device type for test.
     */
    public static final String DEVICE_TYPE = "BLE";

    /**
     * Online state for test.
     */
    public static final boolean DEVICE_ONLINE = true;

    /**
     * Configure for test.
     */
    public static final String DEVICE_CONFIG = "myConfig";

    /**
     * Static Response Intent.
     */
    private static Intent sResponse;

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addApi(Wearable.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        sResponse = response;

        return false;
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_SERVICE_CHANGE);

            Bundle service = new Bundle();
            setId(service, SERVICE_ID);
            setName(service, DEVICE_NAME);
            setType(service, DEVICE_TYPE);
            setOnline(service, DEVICE_ONLINE);
            setConfig(service, DEVICE_CONFIG);

            setNetworkService(message, service);

            return false;
        }
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    public void onConnected(final Bundle connectionHint) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                List<Bundle> services = new ArrayList<Bundle>();

                Collection<String> mNodes = getNodes();

                // Wear number of devices, reflected in the search results.
                for (String node : mNodes) {
                    // Take the first node in UniqueKey.
                    String[] mNodeArray = node.split("-");

                    Bundle service = new Bundle();
                    setId(service, SERVICE_ID + "(" + mNodeArray[0] + ")");
                    setName(service, DEVICE_NAME + "(" + mNodeArray[0] + ")");
                    setType(service, DEVICE_TYPE);
                    setOnline(service, DEVICE_ONLINE);
                    setConfig(service, DEVICE_CONFIG);
                    services.add(service);
                }

                setResult(sResponse, DConnectMessage.RESULT_OK);
                setServices(sResponse, services);
                getContext().sendBroadcast(sResponse);

                return null;
            }
        }.execute();
    }

    /**
     * Get Wear node.
     * 
     * @return WearNode Wear node.
     */
    private Collection<String> getNodes() {

        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }

    @Override
    public void onConnectionSuspended(final int cause) {
        setResult(sResponse, DConnectMessage.RESULT_ERROR);
        getContext().sendBroadcast(sResponse);
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
        setResult(sResponse, DConnectMessage.RESULT_ERROR);
        getContext().sendBroadcast(sResponse);
    }
}
