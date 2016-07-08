/*
 FPLUGController.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class provides functions of controlling F-PLUG.
 * <p>
 * This class provides the following functions:
 * <li>Connect to F-PLUG</li>
 * <li>Send requests to F-PLUG</li>
 * <li>Receive responses from F-PLUG</li>
 * This class instance can handle only single F-PLUG.
 * This class is thread safe.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGController {

    public interface FPLUGConnectionListener {
        void onConnected(String address);

        void onDisconnected(String address);

        void onConnectionError(String address, String message);
    }

    private static final int REQUEST_QUEUE_SIZE = 4;
    private static final int TIMEOUT = 5;

    private Set<FPLUGConnectionListener> mConnectionListenerSet = new HashSet<>();

    private BluetoothDevice mTargetDevice;
    private BluetoothSocket mSocket;

    private FPLUGRequest mCurrentRequestData;
    private FPLUGSender mSender;
    private FPLUGReceiver mReceiver;

    private boolean mIsConnected = false;
    private boolean mIsConnecting = false;

    private ScheduledExecutorService mTimeoutTimer = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mFuture;
    private Queue<FPLUGRequest> mRequestQueue = new ArrayBlockingQueue<>(REQUEST_QUEUE_SIZE);

    public FPLUGController(BluetoothDevice device) throws IllegalArgumentException {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }
        this.mTargetDevice = device;
    }

    public String getAddress() {
        return mTargetDevice.getAddress();
    }

    public synchronized boolean isConnected() {
        return mIsConnected;
    }

    public synchronized void connect(FPLUGConnectionListener listener) {
        if (listener != null) {
            mConnectionListenerSet.add(listener);
        }
        if (mIsConnected) {
            if (listener != null) {
                listener.onConnected(getAddress());
            }
            return;
        }
        if (mIsConnecting) {
            return;
        }
        mIsConnecting = true;
        new FPLUGConnector(mTargetDevice, new FPLUGConnector.FPLUGConnectorEventListener() {
            @Override
            public void onConnected(BluetoothSocket socket) {
                mIsConnecting = false;
                connected(socket);
            }

            @Override
            public void onError(String message) {
                mIsConnecting = false;
                callOnError(message);
            }
        }).start();
    }

    public synchronized void addConnectionListener(FPLUGConnectionListener listener) {
        mConnectionListenerSet.add(listener);
    }

    public synchronized void removeConnectionListener(FPLUGConnectionListener listener) {
        mConnectionListenerSet.remove(listener);
    }

    public synchronized void disconnect() {
        if (mIsConnected) {
            cancelTimer();
            closeSocket();
            stopReceiveThread();
            callOnDisconnected();
            mIsConnected = false;
        }
    }

    public void requestInitPlug(final FPLUGRequestCallback callback) {
        checkCallback(callback);
        request(new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.INIT, callback));
    }

    public void requestCancelPairing(final FPLUGRequestCallback callback) {
        checkCallback(callback);
        request(new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.CANCEL_PAIRING, callback));
    }

    public void requestWattHour(final FPLUGRequestCallback callback) {
        checkCallback(callback);
        request(new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.WATT_HOUR, callback));
    }

    public void requestTemperature(final FPLUGRequestCallback callback) {
        checkCallback(callback);
        request(new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.TEMPERATURE, callback));
    }

    public void requestHumidity(final FPLUGRequestCallback callback) {
        checkCallback(callback);
        request(new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.HUMIDITY, callback));
    }

    public void requestIlluminance(final FPLUGRequestCallback callback) {
        checkCallback(callback);
        request(new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.ILLUMINANCE, callback));
    }

    public void requestRealtimeWatt(final FPLUGRequestCallback callback) {
        checkCallback(callback);
        request(new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.REALTIME_WATT, callback));
    }

    public void requestPastWattHour(Calendar date, final FPLUGRequestCallback callback) {
        checkDate(date);
        checkCallback(callback);
        FPLUGRequest data = new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.PAST_WATT, callback);
        data.setValue(date);
        request(data);
    }

    public void requestPastValues(Calendar date, final FPLUGRequestCallback callback) {
        checkDate(date);
        checkCallback(callback);
        FPLUGRequest data = new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.PAST_VALUES, callback);
        data.setValue(date);
        request(data);
    }

    public void requestSetDate(Calendar date, final FPLUGRequestCallback callback) {
        checkDate(date);
        checkCallback(callback);
        FPLUGRequest data = new FPLUGRequest(FPLUGRequest.REQUEST_TYPE.SET_DATE, callback);
        data.setValue(date);
        request(data);
    }

    public void requestLEDControl(boolean on, final FPLUGRequestCallback callback) {
        checkCallback(callback);
        FPLUGRequest.REQUEST_TYPE type = on ? FPLUGRequest.REQUEST_TYPE.LED_ON : FPLUGRequest.REQUEST_TYPE.LED_OFF;
        request(new FPLUGRequest(type, callback));
    }

    private void checkCallback(FPLUGRequestCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }
    }

    private void checkDate(Calendar date) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
    }

    private synchronized void connected(BluetoothSocket socket) {
        closeSocket();
        mSocket = socket;
        mSender = new FPLUGSender(mSocket);
        mIsConnected = true;
        launchReceiveThread();
        callOnConnected();
    }

    private synchronized void callOnConnected() {
        for (FPLUGConnectionListener fplugConnectionListener : mConnectionListenerSet) {
            fplugConnectionListener.onConnected(getAddress());
        }
    }

    private synchronized void callOnDisconnected() {
        for (FPLUGConnectionListener fplugConnectionListener : mConnectionListenerSet) {
            fplugConnectionListener.onDisconnected(getAddress());
        }
    }

    private synchronized void callOnError(String message) {
        for (FPLUGConnectionListener fplugConnectionListener : mConnectionListenerSet) {
            fplugConnectionListener.onConnectionError(getAddress(), message);
        }
    }

    private synchronized void request(FPLUGRequest data) {
        if (!checkRequestQueue(data.getCallback())) {
            return;
        }
        mRequestQueue.add(data);
        if (!isRequesting()) {
            nextRequest();
        }
    }

    private synchronized boolean checkRequestQueue(final FPLUGRequestCallback callback) {
        if (mRequestQueue.size() >= REQUEST_QUEUE_SIZE) {
            callback.onError("to many request. please wait");
            return false;
        }
        return true;
    }

    private synchronized boolean isRequesting() {
        return mFuture != null;
    }

    private synchronized void nextRequest() {
        if (mRequestQueue.size() == 0) {
            mCurrentRequestData = null;
            return;
        }

        final FPLUGRequest data = mRequestQueue.poll();

        if (!mIsConnected) {
            data.getCallback().onError("F-PLUG not connected");
            nextRequest();
            return;
        }

        mSender.executeRequest(data);

        mCurrentRequestData = data;
        mFuture = mTimeoutTimer.schedule(new Runnable() {
            @Override
            public void run() {
                onTimeout();
            }
        }, TIMEOUT, TimeUnit.SECONDS);
    }

    private synchronized void onTimeout() {
        mCurrentRequestData.getCallback().onTimeout();
        mFuture = null;
        mCurrentRequestData = null;
        nextRequest();
    }

    private synchronized void launchReceiveThread() {
        if (mReceiver == null) {
            mReceiver = new FPLUGReceiver(getAddress(), mSocket, new FPLUGReceiver.FPLUGReceiverEventListener() {
                @Override
                public void onDisconnected() {
                    disconnect();
                }

                @Override
                public void onReceiveResponse(FPLUGResponse response) {
                    onReceiveData(response, null);
                }

                @Override
                public void onReceiveError(String message) {
                    onReceiveData(null, message);
                }

            });
            mReceiver.start();
        }
    }

    private synchronized void onReceiveData(FPLUGResponse response, String message) {
        if (mCurrentRequestData != null) {
            if (response != null) {
                mCurrentRequestData.getCallback().onSuccess(response);
            } else {
                mCurrentRequestData.getCallback().onError(message);
            }
        }
        cancelTimer();
        nextRequest();
    }

    private synchronized void stopReceiveThread() {
        if (mReceiver != null) {
            mReceiver.close();
            mReceiver = null;
        }
    }

    private synchronized void cancelTimer() {
        if (mFuture != null) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    private synchronized void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                //do not something
            }
            mSocket = null;
        }
    }

}
