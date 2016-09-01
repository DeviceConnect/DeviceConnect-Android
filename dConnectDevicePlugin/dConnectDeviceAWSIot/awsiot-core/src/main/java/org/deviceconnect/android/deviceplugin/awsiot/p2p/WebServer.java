package org.deviceconnect.android.deviceplugin.awsiot.p2p;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.BuildConfig;
import org.deviceconnect.android.deviceplugin.awsiot.udt.P2PConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer extends AWSIotP2PManager {

    private static final boolean DEBUG = true;
    private static final String TAG = "ABC";

    private static final int MAX_CLIENT_SIZE = 1;
    private String mPath = UUID.randomUUID().toString();
    private int mPort = -1;
    private boolean mStopFlag;
    private ServerSocket mServerSocket;
    private ServerRunnable mServerRunnable;

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(MAX_CLIENT_SIZE);

    private P2PConnection mP2PConnection;
    private String mDestAddress;
    private Context mContext;

    public WebServer(final Context context, final String address) {
        mContext = context;
        mDestAddress = address;
    }

    public String getUrl() {
        if (mServerSocket == null || mPath == null) {
            return null;
        }
        return "http://localhost:" + mServerSocket.getLocalPort() + "/" + mPath;
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
                        mServerRunnable = new ServerRunnable(mServerSocket.accept());
                        synchronized (WebServer.this) {
                            mExecutor.execute(mServerRunnable);
                        }
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.w(TAG, "", e);
                    }
                } finally {
                    stop();
                }
            }
        });
        return getUrl();
    }

    @Override
    protected P2PConnection createP2PConnection() {
        P2PConnection connection = new P2PConnection();
        connection.setOnP2PConnectionListener(mServerRunnable.mOnP2PConnectionListener);
        return connection;
    }

    @Override
    public void onReceivedSignaling(final String signaling) {
        if (DEBUG) {
            Log.i(TAG, "WebServer#onReceivedSignaling:" + signaling);
        }

        if (mP2PConnection != null) {
            try {
                mP2PConnection.close();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
        }

        mP2PConnection = parseSignal(signaling);
        if (mP2PConnection == null) {
            sendFailedToConnect();
        }
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
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
        }
        mPath = null;
    }

    private class ServerRunnable implements Runnable {
        private static final int BUF_SIZE = 1024 * 8;
        private Socket mSocket;
        private boolean mStopFlag;
        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        public ServerRunnable(final Socket socket) {
            mSocket = socket;
        }

        private void connectP2P() {
            try {
                mP2PConnection = new P2PConnection();
                mP2PConnection.setOnP2PConnectionListener(mOnP2PConnectionListener);
                mP2PConnection.open();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to open P2PConnection.", e);
                }
                if (mP2PConnection != null) {
                    try {
                        mP2PConnection.close();
                    } catch (IOException e1) {
                        if (DEBUG) {
                            Log.w(TAG, "", e);
                        }
                    }
                }
            }
        }

        private void readHttpRequest() throws IOException {
            if (DEBUG) {
                Log.d(TAG, "readHttpRequest: " + mSocket);
            }
            mP2PConnection.sendData(decodeHeader(out.toByteArray(), out.size()));
        }

        @Override
        public void run() {
            if (DEBUG) {
                Log.i(TAG, "WebServer accept. Socket=" + mSocket);
            }

            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    connectP2P();
                }
            });

            try {
                int len;
                byte[] buf = new byte[BUF_SIZE];
                while (!mStopFlag) {
                    // TODO HTTP Requestが大きい時に問題がある。
                    len = mSocket.getInputStream().read(buf);
                    out.write(buf, 0, len);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }

            if (DEBUG) {
                Log.i(TAG, "END");
            }

            try {
                mP2PConnection.close();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
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

                try {
                    readHttpRequest();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "WebServer#onConnected", e);
                    }
                }
            }

            @Override
            public void onReceivedData(final byte[] data, final String address, final int port) {
                if (DEBUG) {
                    Log.i(TAG, "WebServer#onReceivedData: " + address + ":" + port);
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

    private ServerSocket openServerSocket() throws IOException {
        if (mPort != -1) {
            return new ServerSocket(mPort);
        } else {
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
    }

    private byte[] decodeHeader(final byte[] buf, final int len) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, len)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        String line;
        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("host")) {
                writer.write("Host: " + mDestAddress);
                if (DEBUG) {
                    Log.i(TAG, "Convert " + line + " to " + mDestAddress);
                }
            } else {
                writer.write(line);
            }
            writer.write("\r\n");
            writer.flush();
        }
        return out.toByteArray();
    }

    private void sendFailedToConnect() {
        if (mServerRunnable != null) {
            try {
                mServerRunnable.mSocket.getOutputStream().write(generateServiceUnavailable().getBytes());
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
        }
    }

    private String generateBadRequest() {
        return generateErrorHeader("400");
    }

    private String generateInternalServerError() {
        return generateErrorHeader("500");
    }

    private String generateServiceUnavailable() {
        return generateErrorHeader("503");
    }

    private String generateErrorHeader(final String status) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.0 " + status + " OK\r\n");
        sb.append("Server: Server\r\n");
        sb.append("Connection: close\r\n");
        sb.append("\r\n");
        return sb.toString();
    }
}
