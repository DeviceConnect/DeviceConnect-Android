/*
 MixedReplaceMediaServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libmedia.streaming.util;

import android.net.Uri;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Mixed Replace Media Server.
 * @author NTT DOCOMO, INC.
 */
public class MixedReplaceMediaServer {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "IP";

    /**
     * Media Server Callback
     */
    public interface Callback {

        boolean onAccept(Socket socket);

        void onClosed(Socket socket);
    }

    /**
     * Max value of client.
     */
    private static final int MAX_CLIENT_SIZE = 8;
    
    /**
     * Port of the Socket.
     */
    private int mPort = -1;
    
    /**
     * The boundary of a multipart.
     */
    private String mBoundary = UUID.randomUUID().toString();
    
    /**
     * path.
     */
    private String mPath;
    
    /**
     * Content type.
     * Default is "image/jpeg".
     */
    private String mContentType = "image/jpeg";
    
    /**
     * Stop flag.
     */
    private boolean mStopFlag;
    
    /**
     * Name of web server.
     */
    private String mServerName = "DevicePlugin Server";
    
    /**
     * Server Socket.
     */
    private ServerSocket mServerSocket;

    /**
     * List a Server Runnable.
     */
    private final List<ClientThread> mRunnables = Collections.synchronizedList(new ArrayList<>());

    private Callback mCallback;

    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    private boolean notifyOnAccept(final Socket socket) {
        Callback callback = mCallback;
        if (callback != null) {
            return callback.onAccept(socket);
        }
        return true;
    }

    private void notifyOnClose(final Socket socket) {
        Callback callback = mCallback;
        if (callback != null) {
            callback.onClosed(socket);
        }
    }

    /**
     * Set a boundary.
     * @param boundary boundary of a multipart
     */
    public void setBoundary(final String boundary) {
        if (boundary == null) {
            throw new IllegalArgumentException("boundary is null.");
        }
        if (boundary.isEmpty()) {
            throw new IllegalArgumentException("boundary is empty.");
        }
        mBoundary = boundary;
    }
    
    /**
     * Get a boundary.
     * @return boundary
     */
    public String getBoundary() {
        return mBoundary;
    }
    
    /**
     * Set a content type.
     * <p>
     * Default is "image/jpeg".
     * </p>
     * @param contentType content type
     */
    public void setContentType(final String contentType) {
        mContentType = contentType;
    }
    
    /**
     * Get a content type.
     * @return content type
     */
    public String getContentType() {
        return mContentType;
    }

    /**
     * Set a path of web server.
     * @param path path
     */
    public void setPath(String path) {
        mPath = path;
    }

    /**
     * Set a port of web server.
     * @param port port of a web server
     */
    public void setPort(final int port) {
        if (port < 1000) {
            throw new IllegalArgumentException("Port is smaller than 1000.");
        }
        mPort = port;
    }
    
    /**
     * Get a port of web server.
     * @return port
     */
    public int getPort() {
        return mPort;
    }
    
    /**
     * Set a name of server.
     * @param name name of server
     */
    public void setServerName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }
        mServerName = name;
    }
    
    /**
     * Get a name of server.
     * @return name of server
     */
    public String getServerName() {
        return mServerName;
    }
    
    /**
     * Get a url of server.
     * @return url
     */
    public String getUrl() {
        if (mServerSocket == null || mPath == null) {
            return null;
        }
        return "http://localhost:" + mServerSocket.getLocalPort() + "/" + mPath;
    }

    /**
     * Get a server running status.
     * @return server status
     */
    public synchronized boolean isRunning() {
        return !mStopFlag;
    }
    
    /**
     * Inserts the media data into queue.
     * @param media media data
     */
    public synchronized void offerMedia(final byte[] media) {
        if (media == null) {
            return;
        }
        if (!mStopFlag) {
            synchronized (mRunnables) {
                for (ClientThread run : mRunnables) {
                    run.add(media);
                }
            }
        }
    }
    
    /**
     * Start a mixed replace media server.
     * <p>
     * If a port is not set, looking for a port that is not used between 9000 to 10000, set to server.
     * </p>
     * @return the local IP address of this server or {@code null} if this server cannot start.
     */
    public synchronized String start() {
        try {
            mServerSocket = openServerSocket();
        } catch (IOException e) {
            // Failed to open server socket
            mStopFlag = true;
            return null;
        }

        if (mPath == null) {
            mPath = UUID.randomUUID().toString();
        }

        mStopFlag = false;
        new Thread(() -> {
            try {
                while (!mStopFlag) {
                    new ClientThread(mServerSocket.accept()).start();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            } finally {
                stop();
            }
        }).start();
        return getUrl();
    }
    
    /**
     * Open a server socket that looking for a port that can be used.
     * @return ServerSocket
     * @throws IOException if an error occurs while open socket.
     */
    private ServerSocket openServerSocket() throws IOException {
        if (mPort != -1) {
            return new ServerSocket(mPort);
        } else {
            for (int i = 9000; i < 10000; i++) {
                try {
                    return new ServerSocket(i);
                } catch (IOException e) {
                    // ignore.
                }
            }
            throw new IOException("Cannot open server socket.");
        }
    }
    
    /**
     * Stop a mixed replace media server.
     */
    public synchronized void stop() {
        if (mStopFlag) {
            return;
        }
        mStopFlag = true;

        synchronized (mRunnables) {
            for (ClientThread run : mRunnables) {
                run.terminate();
            }
        }

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                // ignore.
            }
        }
        mPath = null;
    }

    public synchronized boolean isEmptyConnection() {
        return mRunnables.isEmpty();
    }

    /**
     * Defined buffer size.
     */
    private static final int BUF_SIZE = 1024;

    /**
     * クライアントの接続を行うスレッド.
     */
    private class ClientThread extends QueueThread<byte[]> {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * Socket.
         */
        private Socket mSocket;

        /**
         * Stream for writing.
         */
        private OutputStream mStream;

        /**
         * コンストラクタ.
         * @param socket 接続しているソケット
         */
        ClientThread(Socket socket) {
            mSocket = socket;
            setName("MRMServer-" + socket.getInetAddress().getHostAddress());
        }

        /**
         * スレッドの停止処理を行います.
         */
        void terminate() {
            mStopFlag = true;

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public synchronized void add(byte[] data) {
            if (getCount() < 3) {
                super.add(data);
            }
        }

        @Override
        public void run() {
            if (DEBUG) {
                Log.d(TAG, "socket accept.");
            }

            mRunnables.add(this);

            boolean isAccept = false;

            try {
                mStream = mSocket.getOutputStream();

                if(!notifyOnAccept(mSocket)) {
                    mStream.write(generateInternalServerError().getBytes());
                    mStream.flush();
                    return;
                }

                byte[] buf = new byte[BUF_SIZE];
                InputStream in = mSocket.getInputStream();
                int len = in.read(buf, 0, BUF_SIZE);
                if (len == -1) {
                    return;
                }
                decodeHeader(buf, len);

                if (mRunnables.size() > MAX_CLIENT_SIZE) {
                    mStream.write(generateServiceUnavailable().getBytes());
                    mStream.flush();
                } else {
                    isAccept = true;

                    mStream.write(generateHttpHeader().getBytes());
                    mStream.flush();

                    while (!mStopFlag) {
                        sendMedia(get());
                    }
                }
            } catch (InterruptedException e) {
                sendInternalServerError();
            } catch (IOException e) {
                sendBadRequest();
            } finally {
                if (DEBUG) {
                    Log.d(TAG, "socket close.");
                }

                mRunnables.remove(this);

                if (isAccept) {
                    notifyOnClose(mSocket);
                }

                if (mStream != null) {
                    try {
                        mStream.close();
                    } catch (IOException e) {
                        if (DEBUG) {
                            Log.e(TAG, "", e);
                        }
                    }
                }

                try {
                    mSocket.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "", e);
                    }
                }
            }
        }

        private void sendInternalServerError() {
            if (mStream == null) {
                return;
            }

            try {
                mStream.write(generateInternalServerError().getBytes());
                mStream.flush();
            } catch (IOException e1) {
                if (DEBUG) {
                    Log.w(TAG, "Error server socket[" + mServerName + "]", e1);
                }
            }
        }

        private void sendBadRequest() {
            if (mStream == null) {
                return;
            }

            try {
                mStream.write(generateBadRequest().getBytes());
                mStream.flush();
            } catch (IOException e1) {
                if (DEBUG) {
                    Log.w(TAG, "Error server socket[" + mServerName + "]", e1);
                }
            }
        }
        
        /**
         * Send a media data.
         * @param media media data
         * @throws IOException if an error occurs while sending media data.
         */
        private void sendMedia(final byte[] media) throws IOException {
            mStream.write(("--" + mBoundary + "\r\n").getBytes());
            mStream.write(("Content-Type: " + mContentType + "\r\n").getBytes());
            mStream.write(("Content-Length: " + media.length + "\r\n").getBytes());
            mStream.write("\r\n".getBytes());
            mStream.write(media);
            mStream.write("\r\n\r\n".getBytes());
            mStream.flush();
        }
        
        /**
         * Decode a Http header.
         * @param buf buffer of http header
         * @param len buffer size
         * @throws IOException if this http header is invalid.
         */
        private void decodeHeader(final byte[] buf, final int len) throws IOException {
            HashMap<String, String> pre = new HashMap<>();
            HashMap<String, String> headers = new HashMap<>();
            HashMap<String, String> params = new HashMap<>();
            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, len)));

            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                throw new IOException("no headers.");
            }

            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                throw new IOException("Header is invalid format.");
            }

            String method = st.nextToken();
            if (!method.toLowerCase(Locale.getDefault()).equals("get")) {
                throw new IOException("Method is invalid.");
            }
            pre.put("method", method);

            if (!st.hasMoreTokens()) {
                throw new IOException("Header is invalid format.");
            }

            String uri = st.nextToken();

            // Decode parameters from the URI
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                decodeParms(uri.substring(qmi + 1), params);
                uri = decodePercent(uri.substring(0, qmi));
            } else {
                decodeParms(null, params);
                uri = decodePercent(uri);
            }
            pre.put("uri", uri);

            // If there's another token, it's protocol version,
            // followed by HTTP headers. Ignore version but parse headers.
            // NOTE: this now forces header names lowercase since they are
            // case insensitive and vary by client.
            if (st.hasMoreTokens()) {
                String line = in.readLine();
                while (line != null && line.trim().length() > 0) {
                    int p = line.indexOf(':');
                    if (p >= 0) {
                        headers.put(line.substring(0, p).trim().toLowerCase(Locale.US),
                                line.substring(p + 1).trim());
                    }
                    line = in.readLine();
                }
            }

            String segment = Uri.parse(uri).getLastPathSegment();
            if (segment == null || !segment.equals(mPath)) {
                throw new IOException("Header is invalid format.");
            }
        }
        
        /**
         * Decode of uri param.
         * @param parms uri
         * @param p 
         */
        private void decodeParms(final String parms, final Map<String, String> p) {
            if (parms == null) {
                return;
            }

            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                if (sep >= 0) {
                    p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
                } else {
                    p.put(decodePercent(e).trim(), "");
                }
            }
        }
        
        /**
         * Decode of uri.
         * @param str uri
         * @return The decoded URI
         */
        private String decodePercent(final String str) {
            try {
                return URLDecoder.decode(str, "UTF8");
            } catch (UnsupportedEncodingException ignored) {
                return null;
            }
        }
    }

    /**
     * Generate a http header.
     * @return http header
     */
    private String generateHttpHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.0 200 OK\r\n");
        sb.append("Server: ").append(mServerName).append("\r\n");
        sb.append("Connection: close\r\n");
        sb.append("Max-Age: 0\r\n");
        sb.append("Expires: 0\r\n");
        sb.append("Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n");
        sb.append("Pragma: no-cache\r\n");
        sb.append("Access-Control-Allow-Origin: *\r\n");
        sb.append("Content-Type: multipart/x-mixed-replace; ");
        sb.append("boundary=").append(mBoundary).append("\r\n");
        sb.append("\r\n");
        return sb.toString();
    }
    
    /**
     * Generate a Bad Request.
     * @return Bad Request
     */
    private String generateBadRequest() {
        return generateErrorHeader("400");
    }
    
    /**
     * Generate a Internal Serve rError.
     * @return Internal Server Error
     */
    private String generateInternalServerError() {
        return generateErrorHeader("500");
    }
    
    /**
     * Generate a Service Unavailable.
     * @return Service Unavailable
     */
    private String generateServiceUnavailable() {
        return generateErrorHeader("503");
    }
    
    /**
     * Generate a error http header.
     * @param status status
     * @return http header
     */
    private String generateErrorHeader(final String status) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.0 ").append(status).append(" OK\r\n");
        sb.append("Server: ").append(mServerName).append("\r\n");
        sb.append("Access-Control-Allow-Origin: *\r\n");
        sb.append("Connection: close\r\n");
        sb.append("\r\n");
        return sb.toString();
    }
}
