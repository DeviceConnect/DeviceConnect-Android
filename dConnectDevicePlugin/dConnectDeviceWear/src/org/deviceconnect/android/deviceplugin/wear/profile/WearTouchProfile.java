/*
 WearTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.deviceconnect.android.deviceplugin.wear.BuildConfig;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TouchProfile;
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
 * Touch Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearTouchProfile extends TouchProfile implements ConnectionCallbacks, OnConnectionFailedListener {

    /** Google Play Service. */
    private GoogleApiClient mGoogleApiClient;

    /** Tag. */
    private static final String TAG = "WEAR";

    /** Static Service Name. */
    private static String sServiceId;

    /** Status. */
    private static int sStatusEvent;

    /** EVENT_ONTOUCH_REGISTER . */
    private static final int EVENT_ONTOUCH_REGISTER = 1;

    /** EVENT_ONTOUCHSTART_REGISTER . */
    private static final int EVENT_ONTOUCHSTART_REGISTER = 2;

    /** EVENT_ONTOUCHEND_REGISTER . */
    private static final int EVENT_ONTOUCHEND_REGISTER = 3;

    /** EVENT_ONDOUBLETAP_REGISTER . */
    private static final int EVENT_ONDOUBLETAP_REGISTER = 4;

    /** EVENT_ONTOUCHMOVE_REGISTER . */
    private static final int EVENT_ONTOUCHMOVE_REGISTER = 5;

    /** EVENT_ONTOUCHCANCEL_REGISTER . */
    private static final int EVENT_ONTOUCHCANCEL_REGISTER = 6;

    /** EVENT_ONTOUCH_UNREGISTER . */
    private static final int EVENT_ONTOUCH_UNREGISTER = 7;

    /** EVENT_ONTOUCHSTART_UNREGISTER . */
    private static final int EVENT_ONTOUCHSTART_UNREGISTER = 8;

    /** EVENT_ONTOUCHEND_UNREGISTER . */
    private static final int EVENT_ONTOUCHEND_UNREGISTER = 9;

    /** EVENT_ONDOUBLETAP_UNREGISTER . */
    private static final int EVENT_ONDOUBLETAP_UNREGISTER = 10;

    /** EVENT_ONTOUCHMOVE_UNREGISTER . */
    private static final int EVENT_ONTOUCHMOVE_UNREGISTER = 11;

    /** EVENT_ONTOUCHCANCEL_UNREGISTER . */
    private static final int EVENT_ONTOUCHCANCEL_UNREGISTER = 12;

    /** Event flag. */
    private int mRegisterEvent = 0;

    /** Event flag define (touch). */
    private static final int REGIST_FLAG_TOUCH_TOUCH = 0x01;

    /** Event flag define (touchstart). */
    private static final int REGIST_FLAG_TOUCH_TOUCHSTART = 0x02;

    /** Event flag define (touchend). */
    private static final int REGIST_FLAG_TOUCH_TOUCHEND = 0x04;

    /** Event flag define (doubletap). */
    private static final int REGIST_FLAG_TOUCH_DOUBLETAP = 0x08;

    /** Event flag define (touchmove). */
    private static final int REGIST_FLAG_TOUCH_TOUCHMOVE = 0x10;

    /** Event flag define (touchcancel). */
    private static final int REGIST_FLAG_TOUCH_TOUCHCANCEL = 0x20;

    /** Internal management ID. */
    private static String sId = "";

    @Override
    protected boolean onPutOnTouch(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONTOUCH_REGISTER;

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
    protected boolean onPutOnTouchStart(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONTOUCHSTART_REGISTER;

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
    protected boolean onPutOnTouchEnd(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONTOUCHEND_REGISTER;

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
    protected boolean onPutOnDoubleTap(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONDOUBLETAP_REGISTER;

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
    protected boolean onPutOnTouchMove(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONTOUCHMOVE_REGISTER;

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
    protected boolean onPutOnTouchCancel(final Intent request, final Intent response, final String serviceId,
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
            sStatusEvent = EVENT_ONTOUCHCANCEL_REGISTER;

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
    protected boolean onDeleteOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONTOUCH_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                execAsyncTask(EVENT_ONTOUCH_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER);
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
    protected boolean onDeleteOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONTOUCHSTART_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                execAsyncTask(EVENT_ONTOUCHSTART_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER);
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
    protected boolean onDeleteOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONTOUCHEND_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                execAsyncTask(EVENT_ONTOUCHEND_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER);
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
    protected boolean onDeleteOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONDOUBLETAP_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                execAsyncTask(EVENT_ONDOUBLETAP_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER);
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
    protected boolean onDeleteOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONTOUCHMOVE_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                execAsyncTask(EVENT_ONTOUCHMOVE_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER);
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
    protected boolean onDeleteOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            sId = getNodeId(serviceId);
            sStatusEvent = EVENT_ONTOUCHCANCEL_UNREGISTER;
            mGoogleApiClient = new GoogleApiClient.Builder(this.getContext()).addApi(Wearable.API)
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                execAsyncTask(EVENT_ONTOUCHCANCEL_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER);
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

        switch (sStatusEvent) {
        case EVENT_ONTOUCH_REGISTER:
            execAsyncTask(EVENT_ONTOUCH_REGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER);
            break;
        case EVENT_ONTOUCHSTART_REGISTER:
            execAsyncTask(EVENT_ONTOUCHSTART_REGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER);
            break;
        case EVENT_ONTOUCHEND_REGISTER:
            execAsyncTask(EVENT_ONTOUCHEND_REGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER);
            break;
        case EVENT_ONDOUBLETAP_REGISTER:
            execAsyncTask(EVENT_ONDOUBLETAP_REGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER);
            break;
        case EVENT_ONTOUCHMOVE_REGISTER:
            execAsyncTask(EVENT_ONTOUCHMOVE_REGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER);
            break;
        case EVENT_ONTOUCHCANCEL_REGISTER:
            execAsyncTask(EVENT_ONTOUCHCANCEL_REGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER);
            break;
        case EVENT_ONTOUCH_UNREGISTER:
            execAsyncTask(EVENT_ONTOUCH_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER);
            break;
        case EVENT_ONTOUCHSTART_UNREGISTER:
            execAsyncTask(EVENT_ONTOUCHSTART_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER);
            break;
        case EVENT_ONTOUCHEND_UNREGISTER:
            execAsyncTask(EVENT_ONTOUCHEND_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER);
            break;
        case EVENT_ONDOUBLETAP_UNREGISTER:
            execAsyncTask(EVENT_ONDOUBLETAP_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER);
            break;
        case EVENT_ONTOUCHMOVE_UNREGISTER:
            execAsyncTask(EVENT_ONTOUCHMOVE_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER);
            break;
        case EVENT_ONTOUCHCANCEL_UNREGISTER:
            execAsyncTask(EVENT_ONTOUCHCANCEL_UNREGISTER, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER);
            break;
        default:
            break;
        }
    }

    /**
     * Execute asynchronous task.
     * 
     * @param statusEvent Status event.
     * @param action Action Name.
     */
    private void execAsyncTask(final int statusEvent, final String action) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                Collection<String> nodes = getNodes();
                sendMessageToWear(sId, statusEvent, nodes, action, "");
                return null;
            }
        } .execute();
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

        switch (status) {
        case EVENT_ONTOUCH_REGISTER:
            sendRegisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCH, TouchProfile.ATTRIBUTE_ON_TOUCH, message);
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCH;
            break;
        case EVENT_ONTOUCHSTART_REGISTER:
            sendRegisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHSTART,
                    TouchProfile.ATTRIBUTE_ON_TOUCH_START, message);
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHSTART;
            break;
        case EVENT_ONTOUCHEND_REGISTER:
            sendRegisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHEND, TouchProfile.ATTRIBUTE_ON_TOUCH_START,
                    message);
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHEND;
            break;
        case EVENT_ONDOUBLETAP_REGISTER:
            sendRegisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_DOUBLETAP, TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP,
                    message);
            mRegisterEvent |= REGIST_FLAG_TOUCH_DOUBLETAP;
            break;
        case EVENT_ONTOUCHMOVE_REGISTER:
            sendRegisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHMOVE, TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE,
                    message);
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHMOVE;
            break;
        case EVENT_ONTOUCHCANCEL_REGISTER:
            sendRegisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHCANCEL,
                    TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL, message);
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHCANCEL;
            break;

        case EVENT_ONTOUCH_UNREGISTER:
            sendUnregisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCH, REGIST_FLAG_TOUCH_TOUCH, message);
            break;
        case EVENT_ONTOUCHSTART_UNREGISTER:
            sendUnregisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHSTART, REGIST_FLAG_TOUCH_TOUCHSTART,
                    message);
            break;
        case EVENT_ONTOUCHEND_UNREGISTER:
            sendUnregisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHEND, REGIST_FLAG_TOUCH_TOUCHEND, message);
            break;
        case EVENT_ONDOUBLETAP_UNREGISTER:
            sendUnregisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_DOUBLETAP, REGIST_FLAG_TOUCH_DOUBLETAP,
                    message);
            break;
        case EVENT_ONTOUCHMOVE_UNREGISTER:
            sendUnregisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHMOVE, REGIST_FLAG_TOUCH_TOUCHMOVE,
                    message);
            break;
        case EVENT_ONTOUCHCANCEL_UNREGISTER:
            sendUnregisterMessageToWear(id, nodes, WearConst.PARAM_TOUCH_TOUCHCANCEL, REGIST_FLAG_TOUCH_TOUCHCANCEL,
                    message);
            break;
        default:
            break;
        }
    }

    /**
     * Send register message to Wear.
     * 
     * @param id Node ID for send data.
     * @param nodes Node list.
     * @param action Action Name.
     * @param attribute Attribute Name.
     * @param message Send strings.
     */
    private void sendRegisterMessageToWear(final String id, final Collection<String> nodes, final String action,
            final String attribute, final String message) {
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
                                if (mDataArray[0].equals(action)) {
                                    List<Event> events = EventManager.INSTANCE.getEventList(sServiceId,
                                            TouchProfile.PROFILE_NAME, null, attribute);
                                    sendMessageToEvent(data, events);
                                }
                            }
                        });

                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node, action,
                        message.getBytes()).await();

                if (!result.getStatus().isSuccess()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "failed send message(register).");
                    }
                }
            }
        }
    }

    /**
     * Send unregister message to Wear.
     * 
     * @param id Node ID for send data.
     * @param nodes Node list.
     * @param action Action Name.
     * @param flag Event flag.
     * @param message Send strings.
     */
    private void sendUnregisterMessageToWear(final String id, final Collection<String> nodes, final String action,
            final int flag, final String message) {
        for (String node : nodes) {

            // Send to select device node.
            if (node.indexOf(id) != -1) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node, action,
                        message.getBytes()).await();

                if (result.getStatus().isSuccess()) {
                    mRegisterEvent &= ~(flag);
                    if (mRegisterEvent == 0) {
                        mGoogleApiClient.disconnect();
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
            Bundle touchdata = new Bundle();
            List<Bundle> touchlist = new ArrayList<Bundle>();
            Bundle touches = new Bundle();
            int count = Integer.parseInt(mDataArray[0]);
            int index = 1;
            for (int n = 0; n < count; n++) {
                touchdata.putInt(TouchProfile.PARAM_ID, Integer.parseInt(mDataArray[index++]));
                touchdata.putFloat(TouchProfile.PARAM_X, Float.parseFloat(mDataArray[index++]));
                touchdata.putFloat(TouchProfile.PARAM_Y, Float.parseFloat(mDataArray[index++]));
                touchlist.add((Bundle) touchdata.clone());
            }
            touches.putParcelableArray(TouchProfile.PARAM_TOUCHES, touchlist.toArray(new Bundle[touchlist.size()]));
            Event eventdata = events.get(i);
            Intent intent = EventManager.createEventMessage(eventdata);
            intent.putExtra(TouchProfile.PARAM_TOUCH, touches);
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
