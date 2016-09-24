package org.deviceconnect.android.deviceplugin.awsiot.udt;

import android.util.Log;

import com.barchart.udt.net.NetServerSocketUDT;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class RelayServer {

    private static final boolean DEBUG = true;
    private static final String TAG = "UDT";

    private NetServerSocketUDT mAcceptorSocket;
    private List<SocketTask> mSockets = new ArrayList<>();
    private boolean mCloseFlag;

    private OnRelayServerListener mListener;

    public RelayServer() {
    }

    public void setOnRelayServerListener(final OnRelayServerListener listener) {
        mListener = listener;
    }

    public void open() throws IOException {
        if (mAcceptorSocket != null) {
            return;
        }

        StunClient client = new StunClient();
        if (client.bindingRequest()) {
            String address = client.getMappedAddress();
            int port = client.getMappedPort();
            if (mListener != null) {
                mListener.onRetrievedAddress(address, port);
            }

            mAcceptorSocket = new NetServerSocketUDT();
            mAcceptorSocket.bind(new InetSocketAddress("0.0.0.0", port), 256);
            mCloseFlag = false;
            while (!mCloseFlag) {
                final SocketTask task = new SocketTask(mAcceptorSocket.accept());
                task.setOnSocketTaskListener(mListener);
                mSockets.add(task);
                Executors.newSingleThreadExecutor().submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            return task.execute();
                        } finally {
                            mSockets.remove(task);
                        }
                    }
                });
            }
        } else {
            throw new IOException("Failed to retrieved address from STUN server.");
        }
    }

    public void sendData(final byte[] data) throws IOException {
        for (SocketTask task : mSockets) {
            task.sendData(data);
        }
    }

    public void sendData(final byte[] data,final int length) throws IOException {
        for (SocketTask task : mSockets) {
            task.sendData(data, length);
        }
    }

    public void sendData(final byte[] data, final int offset, final int length) throws IOException {
        for (SocketTask task : mSockets) {
            task.sendData(data, offset, length);
        }
    }

    public void close() throws IOException {
        if (DEBUG) {
            Log.i(TAG, "RelayServer#close()");
        }
        mCloseFlag = true;

        for (SocketTask task : mSockets) {
            try {
                task.close();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "RelayServer#close", e);
                }
            }
        }

        if (mAcceptorSocket != null) {
            mAcceptorSocket.close();
        }
        mAcceptorSocket = null;
        mListener = null;
    }


    public interface OnRelayServerListener extends SocketTask.OnSocketTaskListener {
        void onRetrievedAddress(String address, int port);
    }
}
