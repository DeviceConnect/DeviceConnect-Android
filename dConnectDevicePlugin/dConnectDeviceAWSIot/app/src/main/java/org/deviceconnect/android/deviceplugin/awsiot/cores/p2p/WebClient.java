/*
 WebClient.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.p2p;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;
import org.deviceconnect.android.deviceplugin.awsiot.udt.P2PConnection;
import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.Executors;

public class WebClient extends AWSIotP2PManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AWS";

    private static final int BUF_SIZE = 1024 * 8;

    public static final String PATH_CONTENT_PROVIDER = "/contentProvider";

    private P2PConnection mP2PConnection;
    private ISocketAdapter mSocket;
    private Context mContext;
    private ByteArrayOutputStream mHttpHeaderData = new ByteArrayOutputStream();

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

    private ISocketAdapter openSocket(final byte[] data) throws IOException {
        if (DEBUG) {
            Log.i(TAG, "WebClient#readHttpHeader: " + data.length);
            Log.i(TAG, "WebClient#readHttpHeader: " + new String(data).replace("\r\n", " "));
        }

        final ISocketAdapter socket = openSocketFromHttpHeader(data, data.length);
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                socket.r();
            }
        });
        return socket;
    }

    private ISocketAdapter openSocketFromHttpHeader(final byte[] buf, final int len) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, len)));

        String address = null;
        int port = 0;

        String inLine = in.readLine();
        if (inLine == null) {
            throw new IOException("Cannot open socket.");
        }

        StringTokenizer st = new StringTokenizer(inLine);
        if (!st.hasMoreTokens()) {
            throw new IOException("Cannot open socket.");
        }

        st.nextToken();

        if (!st.hasMoreTokens()) {
            throw new IOException("Cannot open socket.");
        }

        String uri = st.nextToken();

        // TODO 他のパターンがあれば検討すること。
        if (uri.startsWith(PATH_CONTENT_PROVIDER)) {
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                return new ContentProviderSocketAdapter(mContext, uri.substring(qmi + 1));
            }
        }

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

                return new SocketAdapter(new Socket(address, port));
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
                    mHttpHeaderData.write(data);
                    if (findHeaderEnd(mHttpHeaderData.toByteArray(), mHttpHeaderData.size()) > 0) {
                        mSocket = openSocket(mHttpHeaderData.toByteArray());
                        mSocket.w(mHttpHeaderData.toByteArray());
                        mHttpHeaderData = null;
                    }
                } else {
                    mSocket.w(data);
                }
            } catch (Exception e) {
                sendFailedToConnect();
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
    };

    private interface ISocketAdapter {
        void r();
        void w(byte[] data) throws IOException;
        void close() throws IOException;
    }

    private class SocketAdapter implements ISocketAdapter {
        private Socket mSocket;
        private OutputStream mOutputStream;

        public SocketAdapter(final Socket socket) throws IOException {
            mSocket = socket;
            mOutputStream = socket.getOutputStream();
        }

        @Override
        public void r() {
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
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void w(final byte[] data) throws IOException {
            mOutputStream.write(data);
            mOutputStream.flush();
        }

        @Override
        public void close() throws IOException {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ContentProviderSocketAdapter implements ISocketAdapter {

        private Context mContext;
        private File mFile;
        private String mUri;
        private InputStream mInputStream;

        public ContentProviderSocketAdapter(final Context context, final String uri) throws IOException {
            mContext = context;
            mUri = uri;
        }

        public Context getContext() {
            return mContext;
        }

        private File load(final String uri) throws IOException {
            String fileName = AWSIotUtil.md5(uri);
            if (fileName == null) {
                throw new IOException("");
            }

            File file = File.createTempFile(fileName, null, getContext().getCacheDir());
            FileOutputStream out = null;
            InputStream in = null;
            byte[] buf = new byte[BUF_SIZE];
            int len;
            try {
                out = new FileOutputStream(file);

                ContentResolver r = getContext().getContentResolver();
                in = r.openInputStream(Uri.parse(uri));
                if (in != null) {
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                }
                return file;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void createHeader() throws IOException {
            String a = generateHttpHeader(mFile.length());
            byte[] data = a.getBytes();
            mP2PConnection.sendData(data, 0, data.length);
        }

        private String generateHttpHeader(final long fileSize) {
            SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));

            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 OK\r\n");
            sb.append("Date: ").append(gmtFrmt.format(new Date())).append("\r\n");
            sb.append("Server: AWSIot-Remote-Server(Android)\r\n");
            sb.append("Content-Length: ").append(fileSize).append("\r\n");
            sb.append("Connection: close\r\n");
            sb.append("\r\n");
            return sb.toString();
        }

        @Override
        public void r() {
            try {
                if (DEBUG) {
                    Log.i(TAG, "WebClient#r: uri=" + mUri);
                }

                mFile = load(mUri);

                createHeader();

                mInputStream = new FileInputStream(mFile);
                int len;
                byte[] buf = new byte[BUF_SIZE];
                while ((len = mInputStream.read(buf)) > 0) {
                    mP2PConnection.sendData(buf, 0, len);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            } finally {
                WebClient.this.close();

                if (mFile != null && !mFile.delete()) {
                    if (DEBUG) {
                        Log.w(TAG, "Failed to delete file. mFile=" + mFile.getAbsolutePath());
                    }
                }
            }
        }

        @Override
        public void w(final byte[] data) {
        }

        @Override
        public void close() throws IOException {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
