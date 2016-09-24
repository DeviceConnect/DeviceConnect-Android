/*
 WebServer.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.p2p;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;
import org.deviceconnect.android.deviceplugin.awsiot.udt.P2PConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer extends AWSIotP2PManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AWS";

    private static final int MAX_CLIENT_SIZE = 8;
    private String mPath = "/" + UUID.randomUUID().toString();
    private boolean mStopFlag;
    private ServerSocket mServerSocket;
    private Map<Integer, ServerRunnable> mServerRunnableMap = new ConcurrentHashMap<>();

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(MAX_CLIENT_SIZE);

    private String mDestAddress;
    private Context mContext;

    public WebServer(final Context context, final String address) {
        mContext = context;
        mDestAddress = address;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getUrl() {
        if (mServerSocket == null || mPath == null) {
            return null;
        }
        return "http://localhost:" + mServerSocket.getLocalPort() + mPath;
    }

    public synchronized String start() {
        if (mServerSocket != null) {
            throw new RuntimeException("WebServer is already running.");
        }

        try {
            mServerSocket = openServerSocket();
        } catch (IOException e) {
            // Failed to open server socket
            mStopFlag = true;
            return null;
        }

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mStopFlag = false;
                try {
                    while (!mStopFlag) {
                        mExecutor.execute(new ServerRunnable(mServerSocket.accept()));
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.w(TAG, "WebServer#start: " + e.getMessage());
                    }
                } finally {
                    stop();
                }
            }
        });
        return getUrl();
    }

    public void stop() {
        if (mStopFlag) {
            return;
        }
        mStopFlag = true;

        mExecutor.shutdown();
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
        }
        mPath = null;
    }

    @Override
    public void onReceivedSignaling(final String signaling) {
        if (DEBUG) {
            Log.i(TAG, "WebServer#onReceivedSignaling:" + signaling);
        }

        ServerRunnable run = mServerRunnableMap.get(getConnectionId(signaling));
        if (run != null) {
            run.onReceivedSignaling(signaling);
        }
    }

    public boolean hasConnectionId(final String signaling) {
        return mServerRunnableMap.get(getConnectionId(signaling))  != null;
    }

    protected void onConnected() {
    }

    protected void onDisconnected() {
    }

    private ServerSocket openServerSocket() throws IOException {
        for (int i = 9000; i < 10000; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "already use port=" + i);
                }
            }
        }
        throw new IOException("Cannot open server socket.");
    }

    private byte[] decodeHeader(final byte[] buf, final int len) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, len)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        String line;
        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("host")) {
                writer.write("Host: " + mDestAddress);
            } else {
                writer.write(line);
            }
            writer.write("\r\n");
            writer.flush();
        }
        return out.toByteArray();
    }

    private class ServerRunnable implements Runnable {
        private static final int BUF_SIZE = 1024 * 8;
        private Socket mSocket;
        private boolean mStopFlag;
        private P2PConnection mP2PConnection;
        private int mConnectionId;

        public ServerRunnable(final Socket socket) {
            mConnectionId = P2PConnection.generateConnectionId();
            mSocket = socket;
        }

        private void connectP2P() {
            try {
                mP2PConnection = new P2PConnection(mConnectionId);
                mP2PConnection.setOnP2PConnectionListener(mOnP2PConnectionListener);
                mP2PConnection.open();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "ServerRunnable#connectP2P");
                }
            }
        }

        private void onReceivedSignaling(final String signaling) {
            if (DEBUG) {
                Log.i(TAG, "ServerRunnable#onReceivedSignaling");
            }

            if (mP2PConnection != null) {
                try {
                    mP2PConnection.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.w(TAG, "ServerRunnable#onReceivedSignaling", e);
                    }
                }
            }

            mP2PConnection = createP2PConnection(signaling, mOnP2PConnectionListener);
            if (mP2PConnection == null) {
                sendFailedToConnect();
                mStopFlag = true;
            }
        }

        private void relayHttpRequest() {
            final byte[] buf = new byte[BUF_SIZE];
            int headerSize = 0;
            int readLength = 0;
            int read;

            if (DEBUG) {
                Log.e(TAG, "WebServer#relayHttpRequest");
            }

            try {
                InputStream in = mSocket.getInputStream();
                read = in.read(buf, 0, BUF_SIZE);
                if (read == -1) {
                    // error
                    return;
                }

                while (read > 0) {
                    readLength += read;
                    headerSize = findHeaderEnd(buf, readLength);
                    if (headerSize > 0) {
                        break;
                    }
                    read = in.read(buf, readLength, BUF_SIZE - readLength);
                }

                mP2PConnection.sendData(decodeHeader(buf, headerSize));
                mP2PConnection.sendData(buf, headerSize, readLength - headerSize);

                while (!mStopFlag && (read = in.read(buf)) != -1) {
                    mP2PConnection.sendData(buf, 0, read);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "ServerRunnable#relayHttpRequest", e);
                }
            }
        }

        private void sendFailedToConnect() {
            try {
                mSocket.getOutputStream().write(generateInternalServerError().getBytes());
                mSocket.getOutputStream().flush();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
        }

        @Override
        public void run() {
            if (DEBUG) {
                Log.i(TAG, "ServerRunnable Start. Socket=" + mSocket);
            }
            mServerRunnableMap.put(mConnectionId, this);

            onConnected();

            try {
                connectP2P();

                while (!mStopFlag && !mSocket.isClosed()) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "ServerRunnable#run", e);
                }
                sendFailedToConnect();
            } finally {
                if (DEBUG) {
                    Log.i(TAG, "ServerRunnable End. Socket=" + mSocket);
                }

                mServerRunnableMap.remove(mConnectionId);

                if (mP2PConnection != null) {
                    try {
                        mP2PConnection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (mSocket != null) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                onDisconnected();
            }
        }

        private P2PConnection.OnP2PConnectionListener mOnP2PConnectionListener = new P2PConnection.OnP2PConnectionListener() {
            @Override
            public void onRetrievedAddress(final String address, final int port) {
                if (DEBUG) {
                    Log.d(TAG, "WebServer#onRetrievedAddress=" + address + ":" + port);
                }

                onNotifySignaling(createSignaling(mContext, mP2PConnection.getConnectionId(), address, port));
            }

            @Override
            public void onConnected(final String address, final int port) {
                if (DEBUG) {
                    Log.i(TAG, "WebServer#onConnected: " + address + ":" +  port);
                }

                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        relayHttpRequest();
                    }
                });
            }

            @Override
            public void onReceivedData(final byte[] data, final String address, final int port) {
                if (DEBUG) {
                    Log.i(TAG, "WebServer#onReceivedData: " + address + ":" + port + " " + data.length);
                }

                try {
                    mSocket.getOutputStream().write(data);
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "WebServer#onReceivedData:", e);
                    }
                }
            }

            @Override
            public void onDisconnected(final String address, final int port) {
                if (DEBUG) {
                    Log.i(TAG, "WebServer#onDisconnected: " + address + ":" +  port);
                }

                try {
                    mSocket.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "WebServer#onDisconnected:", e);
                    }
                }
                mStopFlag = true;
            }

            @Override
            public void onTimeout() {
                if (DEBUG) {
                    Log.i(TAG, "WebServer#onTimeout:");
                }

                sendFailedToConnect();

                try {
                    mSocket.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "WebServer#onTimeout:", e);
                    }
                }
                mStopFlag = true;
            }
        };
    }
}
