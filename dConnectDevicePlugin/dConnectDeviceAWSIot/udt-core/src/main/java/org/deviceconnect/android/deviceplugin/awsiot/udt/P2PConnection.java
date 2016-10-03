package org.deviceconnect.android.deviceplugin.awsiot.udt;

import android.util.Log;

import com.barchart.udt.lib.AndroidLoaderUDT;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class P2PConnection {
    static  {
        AndroidLoaderUDT.load();
    }

    private static final boolean DEBUG = false;
    private static final String TAG = "UDT";

    private ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mTimeoutFuture;

    private RelayClient mRelayClient;
    private RelayServer mRelayServer;

    private int mConnectionId;

    private OnP2PConnectionListener mOnP2PConnectionListener;

    public P2PConnection() {
        this(generateConnectionId());
    }

    public P2PConnection(final int connectionId) {
        mConnectionId = connectionId;
    }

    public static int generateConnectionId() {
        return (int) UUID.randomUUID().getLeastSignificantBits();
    }

    public int getConnectionId() {
        return mConnectionId;
    }

    public void setConnectionId(int connectionId) {
        mConnectionId = connectionId;
    }

    public void setOnP2PConnectionListener(final OnP2PConnectionListener listener) {
        mOnP2PConnectionListener = listener;
    }

    public void open() throws IOException {
        if (DEBUG) {
            Log.i(TAG, "P2PConnection#open:");
        }

        if (mRelayServer != null) {
            throw new IOException("RelayServer is already exist.");
        }

        closeClient();

        try {
            startTimeoutTimer();
            mRelayServer = new RelayServer();
            mRelayServer.setOnRelayServerListener(mOnRelayServerListener);
            mRelayServer.open();
        } catch (IOException e) {
            closeServer();
            throw e;
        }
    }

    public void connect(final String address, final int port) throws IOException {
        if (DEBUG) {
            Log.i(TAG, "P2PConnection#connect:" + address + ":" + port);
        }

        if (mRelayClient != null) {
            throw new IOException("RelayClient is already exist.");
        }

        closeServer();

        try {
            mRelayClient = new RelayClient();
            mRelayClient.setOnRelayClientListener(mOnRelayClientListener);
            mRelayClient.connect(address, port);
        } catch (IOException e) {
            closeClient();
            throw e;
        }
    }

    public void sendData(final byte[] data) throws IOException {
        if (mRelayClient != null) {
            mRelayClient.sendData(data);
        }
        if (mRelayServer != null) {
            mRelayServer.sendData(data);
        }
    }

    public void sendData(final byte[] data, final int length) throws IOException {
        if (mRelayClient != null) {
            mRelayClient.sendData(data, length);
        }
        if (mRelayServer != null) {
            mRelayServer.sendData(data, length);
        }
    }

    public void sendData(final byte[] data, final int offset, final int length) throws IOException {
        if (mRelayClient != null) {
            mRelayClient.sendData(data, offset, length);
        }
        if (mRelayServer != null) {
            mRelayServer.sendData(data, offset, length);
        }
    }

    public void close() throws IOException {
        if (DEBUG) {
            Log.i(TAG, "P2PConnection#close");
        }
        closeClient();
        closeServer();
        cancelTimeoutTimer();
        mOnP2PConnectionListener = null;
    }

    private void startTimeoutTimer() {
        if (mTimeoutFuture != null) {
            cancelTimeoutTimer();
        }

        mTimeoutFuture = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    Log.w(TAG, "P2PConnection Timeout. connectionId=" + mConnectionId);
                }

                if (mOnP2PConnectionListener != null) {
                    mOnP2PConnectionListener.onTimeout();
                }

                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 30, TimeUnit.SECONDS);
    }

    private void cancelTimeoutTimer() {
        if (mTimeoutFuture != null) {
            mTimeoutFuture.cancel(true);
        }
    }

    private void closeClient() throws IOException {
        if (mRelayClient != null) {
            mRelayClient.close();
            mRelayClient = null;
        }
    }

    private void closeServer() throws IOException {
        if (mRelayServer != null) {
            mRelayServer.close();
            mRelayServer = null;
        }
    }

    private final RelayClient.OnRelayClientListener mOnRelayClientListener = new RelayClient.OnRelayClientListener() {
        @Override
        public void onConnected(final String address, final int port) {
            if (DEBUG) {
                Log.i(TAG, "P2PConnection#onConnected: " + address + " " + port);
            }
            cancelTimeoutTimer();
            if (mOnP2PConnectionListener != null) {
                mOnP2PConnectionListener.onConnected(address, port);
            }
        }

        @Override
        public void onReceivedData(final byte[] data, final String address, final int port) {
            if (mOnP2PConnectionListener != null) {
                mOnP2PConnectionListener.onReceivedData(data, address, port);
            }
        }

        @Override
        public void onDisconnected(final String address, final int port) {
            if (mOnP2PConnectionListener != null) {
                mOnP2PConnectionListener.onDisconnected(address, port);
            }
        }
    };

    private final RelayServer.OnRelayServerListener mOnRelayServerListener = new RelayServer.OnRelayServerListener() {
        @Override
        public void onRetrievedAddress(final String address, final int port) {
            if (DEBUG) {
                Log.i(TAG, "P2PConnection#onRetrievedAddress: " + address + " " + port);
            }

            if (mOnP2PConnectionListener != null) {
                mOnP2PConnectionListener.onRetrievedAddress(address, port);
            }
        }

        @Override
        public void onConnected(final String address, final int port) {
            if (DEBUG) {
                Log.i(TAG, "P2PConnection#onConnected: " + address + " " + port);
            }
            cancelTimeoutTimer();
            if (mOnP2PConnectionListener != null) {
                mOnP2PConnectionListener.onConnected(address, port);
            }
        }

        @Override
        public void onReceivedData(final byte[] data, final String address, final int port) {
            if (mOnP2PConnectionListener != null) {
                mOnP2PConnectionListener.onReceivedData(data, address, port);
            }
        }

        @Override
        public void onDisconnected(final String address, final int port) {
            if (mOnP2PConnectionListener != null) {
                mOnP2PConnectionListener.onDisconnected(address, port);
            }
        }
    };

    public interface OnP2PConnectionListener {
        void onRetrievedAddress(final String address, final int port);
        void onConnected(String address, int port);
        void onReceivedData(byte[] data, String address, int port);
        void onDisconnected(String address, int port);
        void onTimeout();
    }
}
