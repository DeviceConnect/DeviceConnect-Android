/*
 WearDeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.deviceconnect.android.deviceplugin.wear.BuildConfig;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * DeviceOrientation Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearDeviceOrientationProfile extends DeviceOrientationProfile implements ConnectionCallbacks,
        OnConnectionFailedListener {

    /** Google Play Service. */
    private GoogleApiClient mGoogleApiClient;

    /** Tag. */
    private static final String TAG = "WEAR";

    /** Static Service ID. */
    private static String sServiceId;

    /** Status. */
    private static int sStatusEvent;

    /** EVENT_REGISTER . */
    private static final int EVENT_REGISTER = 1;

    /** EVENT_UNREGISTER . */
    private static final int EVENT_UNREGISTER = 2;

    /** Internal management ID. */
    private static String sId = "";

    @Override
    protected boolean onPutOnDeviceOrientation(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sServiceId = serviceId;
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_REGISTER;

            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {

                mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                        .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }

                setResult(response, DConnectMessage.RESULT_OK);

                return true;
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
                return true;
            }

        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDeviceOrientation(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void... params) {
                        Collection<String> nodes = getNodes();
                        sendMessageToWear(sId, EVENT_UNREGISTER, nodes,
                                WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER, "");
                        return null;

                    }
                }.execute();
            }

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
                return true;
            }

        }
        return true;
    }

    @Override
    public void onConnected(final Bundle connectionHint) {

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onConnected");
        }

        if (sStatusEvent == EVENT_REGISTER) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    Collection<String> nodes = getNodes();
                    sendMessageToWear(sId, EVENT_REGISTER, nodes, WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER,
                            "");
                    return null;
                }
            }.execute();

        } else if (sStatusEvent == EVENT_UNREGISTER) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onConnected:EVENT_UNREGISTER");
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    Collection<String> nodes = getNodes();
                    sendMessageToWear(sId, EVENT_UNREGISTER, nodes,
                            WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER, "");
                    return null;
                }
            }.execute();
        }

    }

    @Override
    public void onConnectionSuspended(final int cause) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onConnectionSuspended");
        }
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onConnectionFailed");
        }
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
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "node.getId():" + node.getId());
            }
            results.add(node.getId());
        }

        return results;
    }

    /**
     * Send message to Wear.
     * 
     * @param id Node ID for send data.
     * @param status status.
     * @param nodes Node list.
     * @param action Action Name.
     * @param message Send strings.
     */
    public void sendMessageToWear(final String id, final int status, final Collection<String> nodes,
            final String action, final String message) {

        if (status == EVENT_REGISTER) {
            for (String node : nodes) {

                // Send to select device node.
                if (node.indexOf(id) != -1) {
                    Wearable.MessageApi.addListener(mGoogleApiClient,
                    // Register message receive listener.
                            new MessageApi.MessageListener() {
                                @Override
                                public void onMessageReceived(final MessageEvent messageEvent) {
                                    final String data = new String(messageEvent.getData());

                                    sendMessageToEvent(data);
                                }
                            });

                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node,
                            action, message.getBytes()).await();

                    if (!result.getStatus().isSuccess()) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "failed send message(register).");
                        }
                    }
                }
            }
        } else if (status == EVENT_UNREGISTER) {
            for (String node : nodes) {

                // Send to select device node.
                if (node.indexOf(id) != -1) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node,
                            WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER, "".getBytes()).await();

                    if (result.getStatus().isSuccess()) {
                        mGoogleApiClient.disconnect();
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "failed send message(unregister).");
                        }
                    }
                }
            }
        }
    }

    /**
     * Send a message to the registration event.
     * 
     * @param data Received Strings.
     */
    private void sendMessageToEvent(final String data) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@@@@SUCCESS");
        }
        String[] mDataArray = data.split(",", 0);

        Bundle orientation = new Bundle();
        Bundle a1 = new Bundle();
        a1.putDouble(DeviceOrientationProfile.PARAM_X, 0.0);
        a1.putDouble(DeviceOrientationProfile.PARAM_Y, 0.0);
        a1.putDouble(DeviceOrientationProfile.PARAM_Z, 0.0);
        Bundle a2 = new Bundle();
        a2.putDouble(DeviceOrientationProfile.PARAM_X, Double.parseDouble(mDataArray[0]));
        a2.putDouble(DeviceOrientationProfile.PARAM_Y, Double.parseDouble(mDataArray[1]));
        a2.putDouble(DeviceOrientationProfile.PARAM_Z, Double.parseDouble(mDataArray[2]));
        Bundle r = new Bundle();
        r.putDouble(DeviceOrientationProfile.PARAM_ALPHA, Double.parseDouble(mDataArray[3]));
        r.putDouble(DeviceOrientationProfile.PARAM_BETA, Double.parseDouble(mDataArray[4]));
        r.putDouble(DeviceOrientationProfile.PARAM_GAMMA, Double.parseDouble(mDataArray[5]));
        orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION, a1);
        orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION_INCLUDING_GRAVITY, a2);
        orientation.putBundle(DeviceOrientationProfile.PARAM_ROTATION_RATE, r);
        orientation.putLong(DeviceOrientationProfile.PARAM_INTERVAL, 0);
        setInterval(orientation, Integer.parseInt(mDataArray[6]));

        List<Event> events = EventManager.INSTANCE.getEventList(sServiceId, DeviceOrientationProfile.PROFILE_NAME, null,
                DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent intent = EventManager.createEventMessage(event);

            intent.putExtra(DeviceOrientationProfile.PARAM_ORIENTATION, orientation);
            getContext().sendBroadcast(intent);
        }
    }

    /**
     * Get node form Service ID.
     * 
     * @param serviceId Service ID.
     * @return nodeId Internal management Node ID.
     */
    private String getNodeId(final String serviceId) {

        String[] mServiceIdArray = serviceId.split("\\(", 0);
        String id = mServiceIdArray[1].replace(")", "");

        return id;
    }
}
