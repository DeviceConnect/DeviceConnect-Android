/*
 MixedReplaceMediaServer.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.streaming.mjpeg;

import android.net.Uri;

import net.majorkernelpanic.streaming.BuildConfig;

import java.io.BufferedOutputStream;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * Mixed Replace Media Server.
 *
 * @author NTT DOCOMO, INC.
 */
public class MixedReplaceMediaServer {
    /**
     * Logger.
     */
    private Logger mLogger = Logger.getLogger("mixed-replace-media");

    /**
     * Max value of cache of media.
     */
    private static final int MAX_MEDIA_CACHE = 2;

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
     * The Host name of uri.
     */
    private String mHostName = "localhost";

    /**
     * The path of uri.
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
    private String mServerName = "Mixed-Replace-Media-Server";

    /**
     * Server Socket.
     */
    private ServerSocket mServerSocket;

    /**
     * Interval.
     */
    private int mInterval = 30;

    /**
     * List a Server Runnable.
     */
    private final List<ServerRunnable> mRunnables = Collections.synchronizedList(
            new ArrayList<ServerRunnable>());

    /**
     * Sever event listener.
     */
    private ServerEventListener mListener;

    /**
     * Set a ServerEventListener.
     *
     * @param listener server event listener
     */
    public void setServerEventListener(final ServerEventListener listener) {
        mListener = listener;
    }

    /**
     * Set a interval.
     * @param interval interval
     */
    public void setInterval(final int interval) {
        mInterval = interval;
    }

    /**
     * Set a boundary.
     *
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
     *
     * @return boundary
     */
    public String getBoundary() {
        return mBoundary;
    }

    /**
     * Set a path of uri.
     *
     * @param path path
     */
    public void setPath(final String path) {
        mPath = path;
    }

    /**
     * Get a path of uri.
     *
     * @return path
     */
    public String getPath() {
        return mPath;
    }

    /**
     * Set a content type.
     * <p>
     * Default is "image/jpeg".
     * </p>
     *
     * @param contentType content type
     */
    public void setContentType(final String contentType) {
        mContentType = contentType;
    }

    /**
     * Get a content type.
     *
     * @return content type
     */
    public String getContentType() {
        return mContentType;
    }

    /**
     * Get a host name of uri.
     *
     * @return host name
     */
    public String getHostName() {
        return mHostName;
    }

    /**
     * Set a host name of uri.
     * <p>
     * Default is "locahost".
     * </p>
     *
     * @param hostName host name
     */
    public void setHostName(String hostName) {
        if (hostName == null) {
            throw new IllegalArgumentException("hostName is null.");
        }
        mHostName = hostName;
    }

    /**
     * Set a port of web server.
     *
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
     *
     * @return port
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Set a name of server.
     *
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
     *
     * @return name of server
     */
    public String getServerName() {
        return mServerName;
    }

    /**
     * Get a url of server.
     *
     * @return url
     */
    public String getUrl() {
        if (mServerSocket == null || mPath == null) {
            return null;
        }
        return "http://" + mHostName + ":" + mServerSocket.getLocalPort() + "/" + mPath;
    }

    /**
     * Get a server running status.
     *
     * @return server status
     */
    public synchronized boolean isRunning() {
        return !mStopFlag;
    }

    /**
     * Inserts the media data into queue.
     *
     * @param media media data
     * @return True if to accept, False if to reject
     */
    public synchronized boolean offerMedia(final Buffer media) {
        if (media == null) {
            return false;
        }

        if (!mStopFlag) {
            synchronized (mRunnables) {
                boolean flag = false;
                for (ServerRunnable run : mRunnables) {
                    if (run.offerMedia(media)) {
                        flag = true;
                    }
                }
                return flag;
            }
        }
        return false;
    }

    /**
     * Start a mixed replace media server.
     * <p>
     * If a port is not set, looking for a port that is not used between 9000 to 10000, set to server.
     * </p>
     *
     * @return the local IP address of this server or {@code null} if this server cannot start.
     */
    public synchronized String start() {
        try {
            mServerSocket = openServerSocket();
            mLogger.fine("Open a server socket.");
        } catch (IOException e) {
            // Failed to open server socket
            mStopFlag = true;
            if (mListener != null) {
                mListener.onError(e);
            }
            return null;
        }

        if (mPath == null) {
            mPath = UUID.randomUUID().toString();
        }

        mStopFlag = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null) {
                        mListener.onStarted();
                    }
                    while (!mStopFlag) {
                        new ServerRunnable(mServerSocket.accept()).start();
                    }
                } catch (Exception e) {
                    mLogger.warning("Error server socket[" + mServerName + "]");
                    if (mListener != null) {
                        mListener.onError(e);
                    }
                } finally {
                    stop();
                }
            }
        }).start();
        return getUrl();
    }

    /**
     * Open a server socket that looking for a port that can be used.
     *
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

        mLogger.fine("Stopping MixedReplaceMediaServer...");

        synchronized (mRunnables) {
            for (ServerRunnable run : mRunnables) {
                run.close();
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
        if (mListener != null) {
            mListener.onStopped();
        }

        mLogger.fine("MixedReplaceMediaServer has been stopped.");
    }

    /**
     * Interface of Sever event.
     */
    public interface ServerEventListener {
        /**
         * Event that started a server.
         */
        void onStarted();

        /**
         * Event that stopped a server.
         */
        void onStopped();

        /**
         * Event that occurred a error on server.
         *
         * @param e exception
         */
        void onError(Exception e);

        /**
         * Event that accept a socket.
         *
         * @param socket socket
         * @return True if accept, false to reject
         */
        boolean onAccept(Socket socket);

        /**
         * Event that closed a socket.
         *
         * @param socket socket
         */
        void onClosed(Socket socket);
    }

    /**
     * Class of Server.
     */
    private class ServerRunnable extends Thread {
        /**
         * Defined buffer size.
         */
        private static final int BUF_SIZE = 8192;

        /**
         * Socket.
         */
        private Socket mSocket;

        /**
         * Stream for writing.
         */
        private OutputStream mStream;

        /**
         * Queue that holds the media.
         */
        private final BlockingQueue<Buffer> mMediaQueue = new ArrayBlockingQueue<>(MAX_MEDIA_CACHE);

        /**
         * Flag of the closed.
         */
        private boolean mClosed;

        /**
         * Constructor.
         *
         * @param socket socket
         */
        ServerRunnable(final Socket socket) {
            mSocket = socket;
            setPriority(MAX_PRIORITY);
        }

        @Override
        public void run() {
            mLogger.fine("accept client.");

            boolean isAccept = false;

            mRunnables.add(this);
            try {
                mStream = new BufferedOutputStream(mSocket.getOutputStream());

                byte[] buf = new byte[BUF_SIZE];
                InputStream in = mSocket.getInputStream();
                int len = in.read(buf, 0, BUF_SIZE);
                if (len == -1) {
                    mStream.write(generateBadRequest().getBytes());
                    mStream.flush();
                    return;
                }
                MediaType type = decodeHeader(buf, len);

                if (mListener != null && !mListener.onAccept(mSocket)) {
                    mStream.write(generateInternalServerError().getBytes());
                    mStream.flush();
                } else if (mRunnables.size() > MAX_CLIENT_SIZE) {
                    mStream.write(generateServiceUnavailable().getBytes());
                    mStream.flush();
                } else {
                    isAccept = true;

                    switch (type) {
                        case MJPEG:
                            mStream.write(generateHttpHeaderMJPEG().getBytes());
                            mStream.flush();

                            while (!mStopFlag && !isInterrupted()) {
                                long t = System.currentTimeMillis();
                                Buffer media = mMediaQueue.take();
                                if (mSocket.isClosed()) {
                                    media.release();
                                    break;
                                }
                                if (media.getLength() > 0) {
                                    sendMedia(media.getBuffer(), media.getLength());
                                }
                                media.release();

                                long n = System.currentTimeMillis() - t;
                                if (n < mInterval) {
                                    Thread.sleep(mInterval - n);
                                }
                            }
                            break;
                        case JPEG:
                            Buffer media = mMediaQueue.take();
                            mStream.write(generateHttpHeaderJPEG(media.getLength()).getBytes());
                            if (media.getLength() > 0) {
                                mStream.write(media.getBuffer());
                            }
                            mStream.flush();
                            media.release();
                            break;
                    }
                }
            } catch (InterruptedException e) {
                // ignore.
            } catch (Exception e) {
                if (mStream != null) {
                    try {
                        mStream.write(generateBadRequest().getBytes());
                        mStream.flush();
                    } catch (IOException e1) {
                        mLogger.warning("Error server socket[" + mServerName + "]");
                    }
                }
                if (mListener != null) {
                    mListener.onError(e);
                }
            } finally {
                mLogger.fine("socket close.");

                if (isAccept && mListener != null) {
                    mListener.onClosed(mSocket);
                }

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

                mRunnables.remove(this);
            }
        }

        /**
         * Close the socket.
         */
        private void close() {
            mClosed = true;

            interrupt();

            for (Buffer buffer : mMediaQueue) {
                buffer.release();
            }
            mMediaQueue.clear();
        }

        /**
         * Inserts the media data into queue.
         *
         * @param media the media to add
         * @return true if the media data was added to this queue, else false
         */
        private boolean offerMedia(final Buffer media) {
            if (media == null || mClosed) {
                return false;
            }

            if (mMediaQueue.size() == MAX_MEDIA_CACHE) {
                Buffer removed = mMediaQueue.remove();
                if (removed != null) {
                    removed.release();
                }
            }
            return mMediaQueue.offer(media);
        }

        /**
         * Send a media data.
         *
         * @param media media data
         * @param mediaLength media size
         * @throws IOException if an error occurs while sending media data.
         */
        private void sendMedia(final byte[] media, final int mediaLength) throws IOException {
            mStream.write(("--" + mBoundary + "\r\n").getBytes());
            mStream.write(("Content-Type: " + mContentType + "\r\n").getBytes());
            mStream.write(("Content-Length: " + mediaLength + "\r\n").getBytes());
            mStream.write("\r\n".getBytes());
            mStream.write(media, 0, mediaLength);
            mStream.write("\r\n\r\n".getBytes());
            mStream.flush();
        }

        /**
         * Decode a Http header.
         *
         * @param buf buffer of http header
         * @param len buffer size
         * @return media type
         * @throws IOException if this http header is invalid.
         */
        private MediaType decodeHeader(final byte[] buf, final int len) throws IOException {
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

            if (params.containsKey("snapshot")) {
                return MediaType.JPEG;
            } else {
                return MediaType.MJPEG;
            }
        }

        /**
         * Decode of uri param.
         *
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
         *
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
     * Generate a http header for MJPEG.
     *
     * @return http header
     */
    private String generateHttpHeaderMJPEG() {
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
        sb.append("--").append(mBoundary).append("\r\n");
        return sb.toString();
    }

    /**
     * Generate a http header for plain JPEG.
     *
     * @return http header
     */
    private String generateHttpHeaderJPEG(final int length) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.0 200 OK\r\n");
        sb.append("Server: ").append(mServerName).append("\r\n");
        sb.append("Connection: close\r\n");
        sb.append("Content-Type: image/jpeg\r\n");
        sb.append("Content-Length: ").append(length).append("\r\n");
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * Generate a Bad Request.
     *
     * @return Bad Request
     */
    private String generateBadRequest() {
        return generateErrorHeader("400");
    }

    /**
     * Generate a Internal Serve rError.
     *
     * @return Internal Server Error
     */
    private String generateInternalServerError() {
        return generateErrorHeader("500");
    }

    /**
     * Generate a Service Unavailable.
     *
     * @return Service Unavailable
     */
    private String generateServiceUnavailable() {
        return generateErrorHeader("503");
    }

    /**
     * Generate a error http header.
     *
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

    /**
     * Buffer used by MixedReplaceMediaServer.
     */
    public interface Buffer {
        /**
         * Get a data of buffer.
         *
         * @return data of buffer
         */
        byte[] getBuffer();

        /**
         * Get a length of buffer.
         *
         * @return length of buffer
         */
        int getLength();

        /**
         * Notify the buffer that it has been used.
         */
        void release();
    }

    /**
     * Media type requested by clients.
     */
    private enum MediaType {
        /**
         * Media Type: Motion-JPEG.
         */
        MJPEG,

        /**
         * Media Type: JPEG.
         */
        JPEG
    }
}
