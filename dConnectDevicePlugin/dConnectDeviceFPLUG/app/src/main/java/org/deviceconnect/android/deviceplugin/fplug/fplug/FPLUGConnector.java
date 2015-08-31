/*
 FPLUGConnector.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * This class provides functions of connecting to F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGConnector extends Thread {

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public interface FPLUGConnectorEventListener {
        void onConnected(BluetoothSocket socket);

        void onError(String message);
    }

    private final BluetoothSocket mTmpSocket;
    private FPLUGConnectorEventListener mListener;

    public FPLUGConnector(BluetoothDevice device, FPLUGConnectorEventListener listener) {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        mListener = listener;
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
            mListener.onError("Socket create() failed");
        }
        mTmpSocket = tmp;
    }

    public void run() {
        if (mTmpSocket == null) {
            return;
        }
        try {
            mTmpSocket.connect();
        } catch (Exception e) {
            try {
                mTmpSocket.close();
            } catch (Exception e2) {
                //do not something
            }
            mListener.onError("Socket connect() failed");
            return;
        }
        mListener.onConnected(mTmpSocket);
    }
}

