/*
 MixedReplaceMediaServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.util;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Mixed Replace Media Server.
 * @author NTT DOCOMO, INC.
 */
public class MixedReplaceMediaServer {

    /** Logger. */
    private Logger mLogger = Logger.getLogger("host.dplugin");
    
    /**
     * Max value of cache of media.
     */
    private static final int MAX_MEDIA_CACHE = 4;
    
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
     * Default is "image/jpg".
     */
    private String mContentType = "image/jpg";
    
    /**
     * Stop flag.
     */
    private boolean mStopFlag;
    
    /**
     * Name of web server.
     */
    private String mServerName = "DevicePlugin Server";

    /**
     * Type select : local.
     */
    private final String LOCAL = "local";

    /**
     * Type select : remote.
     */
    private final String REMOTE = "remote";

    /**
     * Video select : video.
     */
    private final String VIDEO = "video";

    /**
     * Server Socket.
     */
    private ServerSocket mServerSocket;
    
    /**
     * Manage a thread.
     */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(MAX_CLIENT_SIZE);
    
    /**
     * List a Server Runnable.
     */
    private final List<ServerRunnable> mServerRunnableList = Collections.synchronizedList(
            new ArrayList<ServerRunnable>());

    /**
     * FPS.
     */
    private int mFPS = 30;

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
     * Default is "image/jpg".
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
     * @param type Select local uri or remote uri.
     * @return url
     */
    public String getUrl(final String type) {
        if (mServerSocket == null || mPath == null || type == null || (!type.equals(LOCAL) && !type.equals(REMOTE))) {
            return null;
        }
        switch (type) {
            case LOCAL:
                return "http://localhost:" + mServerSocket.getLocalPort() + "/" + LOCAL + "/" + VIDEO + "/" + mPath;
            case REMOTE:
                return "http://localhost:" + mServerSocket.getLocalPort() + "/" + REMOTE + "/" + VIDEO + "/" + mPath;
            default:
                return null;
        }
    }

    /**
     * Get a MIME type.
     * @return MIME type.
     */
    public String getMimeType() {
        return mContentType;
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
    public synchronized void offerMedia(final String type, final byte[] media) {
        if (type == null || media == null) {
            return;
        }
        if (!mStopFlag) {
            synchronized (mServerRunnableList) {
                for (ServerRunnable run : mServerRunnableList) {
                    switch (type) {
                        case LOCAL:
                            run.offerMedia(LOCAL, media);
                            break;
                        case REMOTE:
                            run.offerMedia(REMOTE, media);
                            break;
                        default:
                            throw new RuntimeException("offerMedia'type is unknown.");
                    }
                }
            }
        }
    }
    
    /**
     * Start a mixed replace media server.
     * <p>
     * If a port is not set, looking for a port that is not used between 9000 to 10000, set to server.
     * </p>
     * @return {@code true} if this server can start or {@code false} if this server cannot start.
     */
    public synchronized Boolean start() {
        try {
            mServerSocket = openServerSocket();
            mLogger.fine("Open a server socket.");
        } catch (IOException e) {
            // Failed to open server socket
            mStopFlag = true;
            return false;
        }

        mPath = UUID.randomUUID().toString();

        mStopFlag = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!mStopFlag) {
                        ServerRunnable run = new ServerRunnable(mServerSocket.accept());
                        synchronized (MixedReplaceMediaServer.this) {
                            mExecutor.execute(run);
                        }
                    }
                } catch (IOException e) {
                    mLogger.warning("Error server socket[" + mServerName + "]");
                } finally {
                    stop();
                }
            }
        }).start();
        return true;
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
                    continue;
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
        mExecutor.shutdown();
        synchronized (mServerRunnableList) {
            for (ServerRunnable run : mServerRunnableList) {
                run.offerMedia(LOCAL, new byte[0]);
            }
        }
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        mPath = null;
        mLogger.fine("MixedReplaceMediaServer is stop.");
    }
    
    /**
     * Class of Server.
     */
    private class ServerRunnable implements Runnable {
        /**
         * Defined buffer size.
         */
        private static final int BUF_SIZE = 1024;
        
        /**
         * Socket.
         */
        private Socket mSocket;
        
        /**
         * Stream for writing.
         */
        private OutputStream mStream;
        
        /**
         * Queue that holds the media (local).
         */
        private final Queue<byte[]> mLocalMediaQueue = new LinkedList<>();

        /**
         * Queue that holds the media (remote).
         */
        private final Queue<byte[]> mRemoteMediaQueue = new LinkedList<>();

        /**
         * Constructor.
         * @param socket socket
         */
        public ServerRunnable(final Socket socket) {
            mSocket = socket;
        }
        
        @Override
        public void run() {
            mLogger.fine("accept client.");
            mServerRunnableList.add(this);
            try {
                mStream = mSocket.getOutputStream();

                byte[] buf = new byte[BUF_SIZE];
                InputStream in = mSocket.getInputStream();
                int len = in.read(buf, 0, BUF_SIZE);
                if (len == -1) {
                    return;
                }
                String type = decodeHeader(buf, len);
                int fps = 1000 / mFPS;

                if (mServerRunnableList.size() > MAX_CLIENT_SIZE) {
                    mStream.write(generateServiceUnavailable().getBytes());
                    mStream.flush();
                } else {
                    mStream.write(generateHttpHeader().getBytes());
                    mStream.flush();

                    while (!mStopFlag) {
                        long startTime = System.currentTimeMillis();
                        byte[] media;
                        if (type.equals(LOCAL)) {
                            media = mLocalMediaQueue.poll();
                        } else {
                            media = mRemoteMediaQueue.poll();
                        }
                        if (media != null) {
                            sendMedia(media);
                        }
                        long diffTime = System.currentTimeMillis() - startTime;
                        if (diffTime < fps) {
                            try {
                                Thread.sleep(fps - diffTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (mStream != null) {
                    try {
                        mStream.write(generateBadRequest().getBytes());
                        mStream.flush();
                    } catch (IOException e1) {
                        mLogger.warning("Error server socket[" + mServerName + "]");
                    }
                }
            } finally {
                mLogger.fine("socket close.");
                if (mStream != null) {
                    try {
                        mStream.close();
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    mSocket.close();
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                mServerRunnableList.remove(this);
            }
        }
        
        /**
         * Inserts the media data into queue.
         * @param media the media to add
         * @return true if the media data was added to this queue, else false
         */
        private boolean offerMedia(final String type, final byte[] media) {
            switch (type) {
                case LOCAL:
                    if (mLocalMediaQueue.size() == MAX_MEDIA_CACHE) {
                        mLocalMediaQueue.remove();
                    }
                    return mLocalMediaQueue.offer(media);
                case REMOTE:
                    if (mRemoteMediaQueue.size() == MAX_MEDIA_CACHE) {
                        mRemoteMediaQueue.remove();
                    }
                    return mRemoteMediaQueue.offer(media);
                default:
                    return false;
            }
        }
        
        /**
         * Send a media data.
         * @param media media data
         * @throws IOException if an error occurs while sending media data.
         */
        private void sendMedia(final byte[] media) throws IOException {
            mStream.write("--".getBytes());
            mStream.write(mBoundary.getBytes());
            mStream.write("\r\n".getBytes());
            mStream.write("Content-Type: ".getBytes());
            mStream.write(mContentType.getBytes());
            mStream.write("\r\n".getBytes());
            mStream.write("Content-Length: ".getBytes());
            mStream.write(String.valueOf(media.length).getBytes());
            mStream.write("\r\n".getBytes());
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
        private String decodeHeader(final byte[] buf, final int len) throws IOException {
            HashMap<String, String> pre = new HashMap<String, String>();
            HashMap<String, String> headers = new HashMap<String, String>();
            HashMap<String, String> params = new HashMap<String, String>();
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

            if (uri != null) {
                String localPath = "/" + LOCAL + "/" + VIDEO + "/" + mPath;
                String remotePath = "/" + REMOTE + "/" + VIDEO + "/" + mPath;
                if (Pattern.compile(uri).matcher(localPath).matches()) {
                    return LOCAL;
                } else if (Pattern.compile(uri).matcher(remotePath).matches()) {
                    return REMOTE;
                }
            }
            throw new IOException("Header is invalid format.");
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
        final String s1 = "HTTP/1.0 200 OK\r\n";
        final String s2 = "Server: " + mServerName + "\r\n";
        final String s3 = "Connection: close\r\n";
        final String s4 = "Max-Age: 0\r\n";
        final String s5 = "Expires: 0\r\n";
        final String s6 = "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n";
        final String s7 = "Pragma: no-cache\r\n";
        final String s8 = "Content-Type: multipart/x-mixed-replace; " + "boundary=" + mBoundary + "\r\n";
        final String s9 = "\r\n";
        return s1 + s2 + s3 + s4 + s5 + s6 + s7 + s8 + s9;
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
        final String s1 = "HTTP/1.0 " + status + " OK\r\n";
        final String s2 = "Server: " + mServerName + "\r\n";
        final String s3 = "Connection: close\r\n" + "\r\n";
        return s1 + s2 + s3;
    }
}
