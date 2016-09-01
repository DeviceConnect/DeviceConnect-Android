package org.deviceconnect.android.deviceplugin.awsiot.p2p;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.udt.P2PConnection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Executors;

public class WebClient extends AWSIotP2PManager {

    private static final boolean DEBUG = true;
    private static final String TAG = "ABC";

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
        boolean open = false;

        P2PConnection connection = parseSignal(signaling);
        if (connection == null) {
            connection = new P2PConnection();
            open = true;
        }

        mP2PConnection = connection;
        mP2PConnection.setOnP2PConnectionListener(new P2PConnection.OnP2PConnectionListener() {
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
                        readHttpHeader(data);
                    }

                    OutputStream os = mSocket.getOutputStream();
                    os.write(data);
                    os.flush();
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.e(TAG, "", e);
                    }
                    close();
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
        });

        if (open) {
            try {
                connection.open();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            }
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

    private void readHttpHeader(final byte[] data) throws IOException {
        if (DEBUG) {
            Log.i(TAG, "WebClient#readHttpHeader: " + data.length);
            Log.i(TAG, "WebClient#readHttpHeader: " + new String(data).replace("\r\n", " "));
        }

        mSocket = openSocketFromHttpHeader(data, data.length);

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO 効率化を考える
                    InputStream is = mSocket.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        out.write(buf, 0, len);
                        mP2PConnection.sendData(out.toByteArray());
                        out.reset();
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.w(TAG, "");
                    }
                } finally {
                    close();
                }
            }
        });
    }

    private Socket openSocketFromHttpHeader(final byte[] buf, final int len) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, len)));

        String address;
        String line;
        int port;

        while ((line = in.readLine()) != null) {
            if (line.toLowerCase().startsWith("host")) {
                String host = line.substring(line.indexOf(":") + 1).trim();
                if (host.contains(":")) {
                    String[] split = host.split(":");
                    address = DomainResolution.lookup(split[0]);
                    port = Integer.parseInt(split[1]);
                } else {
                    address = DomainResolution.lookup(host);
                    port = 80;
                }

                if (address == null || port == 0) {
                    throw new IOException("");
                }

                if (DEBUG) {
                    Log.e(TAG, "**** " + address + ":" + port);
                }

                return new Socket(address, port);
            }
        }

        throw new IOException("");
    }
}
