package org.deviceconnect.android.deviceplugin.awsiot.p2p;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.udt.P2PConnection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Executors;

public class WebClient extends AWSIotP2PManager {

    private static final boolean DEBUG = true;
    private static final String TAG = "ABC";

    private static final int BUF_SIZE = 1024 * 8;

    private P2PConnection mP2PConnection;
    private Socket mSocket;
    private Context mContext;

    public WebClient(final Context context) {
        mContext = context;
    }

    public void onDisconnected(final WebClient webClient) {
    }

    @Override
    public void onReceivedSignaling(final String signaling) {
        mP2PConnection = createP2PConnection(signaling, mOnP2PConnectionListener);
        if (mP2PConnection == null) {
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    mP2PConnection = new P2PConnection();
                    mP2PConnection.setOnP2PConnectionListener(mOnP2PConnectionListener);
                    mP2PConnection.setConnectionId(getConnectionId(signaling));
                    try {
                        mP2PConnection.open();
                    } catch (IOException e) {
                        if (DEBUG) {
                            Log.w(TAG, "WebClient#onReceivedSignaling", e);
                        }
                    }
                }
            });
        }
    }

    public void close() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }

        if (mP2PConnection != null) {
            try {
                mP2PConnection.close();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }

    private void sendFailedToConnect() {
        if (mP2PConnection != null) {
            try {
                mP2PConnection.sendData(generateInternalServerError().getBytes());
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "WebClient#sendFailedToConnect", e);
                }
            }
        }
    }

    private void relayHttpResponse() {
        try {
            InputStream is = mSocket.getInputStream();
            int len;
            byte[] buf = new byte[BUF_SIZE];
            while ((len = is.read(buf)) > 0) {
                mP2PConnection.sendData(buf, 0, len);
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.w(TAG, "WebClient#relayHttpResponse", e);
            }
        } finally {
            close();
        }
    }

    private Socket openSocket(final byte[] data) throws IOException {
        if (DEBUG) {
            Log.i(TAG, "WebClient#readHttpHeader: " + data.length);
            Log.i(TAG, "WebClient#readHttpHeader: " + new String(data).replace("\r\n", " "));
        }

        mSocket = openSocketFromHttpHeader(data, data.length);

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                relayHttpResponse();
            }
        });

        return mSocket;
    }

    private Socket openSocketFromHttpHeader(final byte[] buf, final int len) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, len)));

        String address = null;
        int port = 0;

        String line;
        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("host")) {
                String host = line.substring(line.indexOf(":") + 1).trim();
                if (host.contains(":")) {
                    String[] split = host.split(":");
                    if (split.length == 2) {
                        address = DomainResolution.lookup(split[0]);
                        port = Integer.parseInt(split[1]);
                    }
                } else {
                    address = DomainResolution.lookup(host);
                    port = 80;
                }

                if (address == null || port == 0) {
                    throw new IOException("Cannot open socket. host=" + line);
                }

                return new Socket(address, port);
            }
        }

        throw new IOException("Cannot open socket.");
    }

    private P2PConnection.OnP2PConnectionListener mOnP2PConnectionListener = new P2PConnection.OnP2PConnectionListener() {
        @Override
        public void onRetrievedAddress(final String address, final int port) {
            if (DEBUG) {
                Log.d(TAG, "WebClient#onRetrievedAddress=" + address + ":" + port);
            }

            onNotifySignaling(createSignaling(mContext, mP2PConnection.getConnectionId(), address, port));
        }

        @Override
        public void onConnected(final String address, final int port) {
            if (DEBUG) {
                Log.d(TAG, "WebClient#onConnected=" + address + ":" + port);
            }
        }

        @Override
        public void onReceivedData(final byte[] data, final String address, final int port) {
            if (DEBUG) {
                Log.d(TAG, "WebClient#onReceivedData=" + data.length);
                Log.d(TAG, "" + new String(data).replace("\r\n", " "));
            }

            try {
                if (mSocket == null) {
                    openSocket(data);
                }

                OutputStream os = mSocket.getOutputStream();
                os.write(data);
                os.flush();
            } catch (Exception e) {
                sendFailedToConnect();
            }
        }

        @Override
        public void onDisconnected(final String address, final int port) {
            if (DEBUG) {
                Log.e(TAG, "WebClient#onDisconnect: " + address + ":" + port);
            }

            WebClient.this.onDisconnected(WebClient.this);
        }

        @Override
        public void onTimeout() {
            Log.e(TAG, "WebClient#onTimeout: ");
        }
    };
}
