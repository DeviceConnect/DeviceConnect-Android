/*
 WearKeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
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
import org.deviceconnect.android.profile.KeyEventProfile;
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
 * Key Event Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearKeyEventProfile extends KeyEventProfile implements ConnectionCallbacks, OnConnectionFailedListener {

    /** Google Play Service. */
    private GoogleApiClient mGoogleApiClient;

    /** Tag. */
    private static final String TAG = "WEAR";

    /** Static Service Name. */
    private static String sServiceId;

    /** Status. */
    private static int sStatusEvent;

    /** EVENT_ONDOWN_REGISTER . */
    private static final int EVENT_ONDOWN_REGISTER = 1;

    /** EVENT_ONUP_REGISTER . */
    private static final int EVENT_ONUP_REGISTER = 2;

    /** EVENT_ONDOWN_UNREGISTER . */
    private static final int EVENT_ONDOWN_UNREGISTER = 3;

    /** EVENT_ONUP_UNREGISTER . */
    private static final int EVENT_ONUP_UNREGISTER = 4;

    /** Event flag. */
    private int mRegisterEvent = 0;

    /** Event flag define (down). */
    private static final int REGIST_FLAG_KEYEVENT_DOWN = 0x01;

    /** Event flag define (up). */
    private static final int REGIST_FLAG_KEYEVENT_UP = 0x02;

    /** Internal management ID. */
    private static String sId = "";

    @Override
    protected boolean onPutOnDown(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONDOWN_REGISTER;

            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                        .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnUp(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONUP_REGISTER;

            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                        .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDown(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONDOWN_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void... params) {
                        Collection<String> nodes = getNodes();
                        sendMessageToWear(sId, EVENT_ONDOWN_UNREGISTER, nodes,
                                WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER, "");
                        return null;
                    }
                }.execute();
            }

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnUp(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONUP_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void... params) {
                        Collection<String> nodes = getNodes();
                        sendMessageToWear(sId, EVENT_ONUP_UNREGISTER, nodes,
                                WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER, "");
                        return null;
                    }
                }.execute();
            }

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
        }
        return true;
    }

    @Override
    public void onConnected(final Bundle connectionHint) {

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onConnected");
        }

        if (sStatusEvent == EVENT_ONDOWN_REGISTER) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    Collection<String> nodes = getNodes();
                    sendMessageToWear(sId, EVENT_ONDOWN_REGISTER, nodes,
                            WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER, "");
                    return null;
                }
            }.execute();
        } else if (sStatusEvent == EVENT_ONUP_REGISTER) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    Collection<String> nodes = getNodes();
                    sendMessageToWear(sId, EVENT_ONUP_REGISTER, nodes, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER,
                            "");
                    return null;
                }
            }.execute();
        } else if (sStatusEvent == EVENT_ONDOWN_UNREGISTER) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onConnected:EVENT_UNREGISTER");
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    Collection<String> nodes = getNodes();
                    sendMessageToWear(sId, EVENT_ONDOWN_UNREGISTER, nodes,
                            WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER, "");
                    return null;
                }
            }.execute();
        } else if (sStatusEvent == EVENT_ONUP_UNREGISTER) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onConnected:EVENT_UNREGISTER");
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    Collection<String> nodes = getNodes();
                    sendMessageToWear(sId, EVENT_ONUP_UNREGISTER, nodes,
                            WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER, "");
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

        if (status == EVENT_ONDOWN_REGISTER) {
            for (String node : nodes) {

                // Send to select device node.
                if (node.indexOf(id) != -1) {
                    Wearable.MessageApi.addListener(mGoogleApiClient,
                    // Register message receive listener.
                            new MessageApi.MessageListener() {
                                @Override
                                public void onMessageReceived(final MessageEvent messageEvent) {
                                    final String data = new String(messageEvent.getData());
                                    String[] mDataArray = data.split(",", 0);
                                    if (mDataArray[0].equals(WearConst.PARAM_KEYEVENT_DOWN)) {
                                        List<Event> events = EventManager.INSTANCE.getEventList(sServiceId,
                                                KeyEventProfile.PROFILE_NAME, null, KeyEventProfile.ATTRIBUTE_ON_DOWN);
                                        sendMessageToEvent(data, events);
                                    }
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
            mRegisterEvent |= REGIST_FLAG_KEYEVENT_DOWN;
        } else if (status == EVENT_ONUP_REGISTER) {
            for (String node : nodes) {

                // Send to select device node.
                if (node.indexOf(id) != -1) {
                    Wearable.MessageApi.addListener(mGoogleApiClient,
                    // Register message receive listener.
                            new MessageApi.MessageListener() {
                                @Override
                                public void onMessageReceived(final MessageEvent messageEvent) {
                                    final String data = new String(messageEvent.getData());
                                    String[] mDataArray = data.split(",", 0);
                                    if (mDataArray[0].equals(WearConst.PARAM_KEYEVENT_UP)) {
                                        List<Event> events = EventManager.INSTANCE.getEventList(sServiceId,
                                                KeyEventProfile.PROFILE_NAME, null, KeyEventProfile.ATTRIBUTE_ON_UP);
                                        sendMessageToEvent(data, events);
                                    }
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
            mRegisterEvent |= REGIST_FLAG_KEYEVENT_UP;
        } else if (status == EVENT_ONDOWN_UNREGISTER) {
            for (String node : nodes) {

                // Send to select device node.
                if (node.indexOf(id) != -1) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node,
                            WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER, "".getBytes()).await();

                    if (result.getStatus().isSuccess()) {
                        mRegisterEvent &= ~(REGIST_FLAG_KEYEVENT_DOWN);
                        if (mRegisterEvent == 0) {
                            mGoogleApiClient.disconnect();
                        }
                    }
                }
            }
        } else if (status == EVENT_ONUP_UNREGISTER) {
            for (String node : nodes) {

                // Send to select device node.
                if (node.indexOf(id) != -1) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node,
                            WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER, "".getBytes()).await();

                    if (result.getStatus().isSuccess()) {
                        mRegisterEvent &= ~(REGIST_FLAG_KEYEVENT_UP);
                        if (mRegisterEvent == 0) {
                            mGoogleApiClient.disconnect();
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
     * @param events Event request list.
     */
    private void sendMessageToEvent(final String data, final List<Event> events) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@@@@SUCCESS");
        }

        String[] mDataArray = data.split(",", 0);

        for (int i = 0; i < events.size(); i++) {
            Bundle keyevent = new Bundle();

            keyevent.putInt(KeyEventProfile.PARAM_ID, Integer.parseInt(mDataArray[1]));
            keyevent.putString(KeyEventProfile.PARAM_CONFIG, mDataArray[2]);

            Event eventdata = events.get(i);
            Intent intent = EventManager.createEventMessage(eventdata);
            intent.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
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
