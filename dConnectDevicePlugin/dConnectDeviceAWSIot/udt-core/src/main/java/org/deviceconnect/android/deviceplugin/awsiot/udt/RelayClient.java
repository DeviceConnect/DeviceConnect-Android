package org.deviceconnect.android.deviceplugin.awsiot.udt;

import android.util.Log;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.net.NetSocketUDT;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class RelayClient {

    private static final boolean DEBUG = false;
    private static final String TAG = "UDT";

    private OnRelayClientListener mOnRelayClientListener;
    private NetSocketUDT mSocket;
    private SocketTask mSocketTask;

    private boolean mCloseFlag;

    public void setOnRelayClientListener(final OnRelayClientListener listener) {
        mOnRelayClientListener = listener;
    }

    public void connect(final String address, final int port) throws IOException {
        mSocket = new NetSocketUDT();
        mSocket.connect(new InetSocketAddress(address, port));

        mSocketTask = new SocketTask(mSocket);
        mSocketTask.setOnSocketTaskListener(mOnRelayClientListener);

        Executors.newSingleThreadExecutor().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return mSocketTask.execute();
            }
        });

        Executors.newSingleThreadExecutor().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return monitor(mSocket.socketUDT());
            }
        });
    }

    public void sendData(final byte[] data) throws IOException {
        mSocketTask.sendData(data);
    }

    public void sendData(final byte[] data, final int length) throws IOException {
        mSocketTask.sendData(data, length);
    }

    public void sendData(final byte[] data, final int offset, final int length) throws IOException {
        mSocketTask.sendData(data, offset, length);
    }

    public void close() throws IOException {
        if (DEBUG) {
            Log.i(TAG, "RelayClient#close()");
        }

        mCloseFlag = true;
        mSocket.close();
        mOnRelayClientListener = null;
    }

    private boolean monitor(final SocketUDT socket) {
        try {
            while (!mCloseFlag) {
                Thread.sleep(1000);
                socket.updateMonitor(false);
            }
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "RelayClient#monitor", e);
            }
            return false;
        }
    }

    public interface OnRelayClientListener extends SocketTask.OnSocketTaskListener {
    }
}
