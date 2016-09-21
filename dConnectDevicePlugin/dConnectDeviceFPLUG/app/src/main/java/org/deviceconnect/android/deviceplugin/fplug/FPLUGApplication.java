/*
 FPLUGApplication.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGDiscover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of Application.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGApplication extends Application implements FPLUGController.FPLUGConnectionListener {

    private static final String TAG = "FPLUGApplication";

    private final Map<String, FPLUGController> mControllerMap = new ConcurrentHashMap<>();

    private ControllerListener mListener;

    private ScheduledExecutorService mConnectionTimer = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onCreate");
        }
        //NOTICE:ignore start timer when unit test.
        //Because bluetooth socket will be crush by instantiate two FPLUGController for one F-PLUG.
        //For test, change build flavor to apptest.
        if (!BuildConfig.IS_TEST) {
            startConnectionTimer();
        }
    }

    @Override
    public void onTerminate() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onTerminate");
        }
        super.onTerminate();
        stopConnectionTimer();
        disconnect();
    }

    @Override
    public synchronized void onConnected(final String address) {
        FPLUGController controller = mControllerMap.get(address);
        if (mListener != null && controller != null) {
            mListener.onConnected(controller);
        }
    }

    @Override
    public synchronized void onDisconnected(final String address) {
        FPLUGController controller = mControllerMap.get(address);
        if (mListener != null && controller != null) {
            mListener.onDisconnected(controller);
        }
    }

    @Override
    public void onConnectionError(final String address, final String message) {
        // NOP
    }

    public synchronized void setControllerListener(final ControllerListener listener) {
        mListener = listener;
    }

    public synchronized void connectFPlug(BluetoothDevice fplug, FPLUGController.FPLUGConnectionListener listener) {
        FPLUGController controller = getFPLUGController(fplug.getAddress());
        if (controller == null) {
            controller = new FPLUGController(fplug);
            putController(controller);
        }
        controller.connect(listener);
    }

    public synchronized void removeConnectionListener(FPLUGController.FPLUGConnectionListener listener) {
        for (Map.Entry<String, FPLUGController> entry : mControllerMap.entrySet()) {
            entry.getValue().removeConnectionListener(listener);
        }
    }

    public synchronized FPLUGController getFPLUGController(String address) {
        return mControllerMap.get(address);
    }

    public synchronized FPLUGController getConnectedController(String address) {
        FPLUGController controller = getFPLUGController(address);
        return controller != null && controller.isConnected() ? controller : null;
    }

    public synchronized List<FPLUGController> getConnectedController() {
        List<FPLUGController> list = new ArrayList<>();
        for (Map.Entry<String, FPLUGController> entry : mControllerMap.entrySet()) {
            if (entry.getValue().isConnected()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public synchronized boolean isConnectedFPlug(String address) {
        FPLUGController controller = getFPLUGController(address);
        return controller != null && controller.isConnected();
    }

    private void startConnectionTimer() {
        mConnectionTimer.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                autoConnection();
            }
        }, 10, 10, TimeUnit.MINUTES);
    }

    private void stopConnectionTimer() {
        if (mConnectionTimer != null) {
            mConnectionTimer.shutdownNow();
            mConnectionTimer = null;
        }
    }

    private synchronized void autoConnection() {
        List<BluetoothDevice> fplugs = FPLUGDiscover.getAll();
        if (fplugs == null) {
            return;
        }
        for (BluetoothDevice fplug : fplugs) {
            FPLUGController controller = getFPLUGController(fplug.getAddress());
            if (controller == null) {
                controller = new FPLUGController(fplug);
                putController(controller);
            }
            if (!controller.isConnected()) {
                controller.connect(null);
            }
        }
    }

    private synchronized void disconnect() {
        for (Map.Entry<String, FPLUGController> entry : mControllerMap.entrySet()) {
            entry.getValue().disconnect();
        }
        mControllerMap.clear();
    }

    private synchronized void putController(final FPLUGController controller) {
        controller.addConnectionListener(this);
        mControllerMap.put(controller.getAddress(), controller);
        if (mListener != null) {
            mListener.onAdded(controller);
        }
    }

    public interface ControllerListener {
        void onAdded(FPLUGController controller);

        void onConnected(FPLUGController controller);

        void onDisconnected(FPLUGController controller);
    }
}
