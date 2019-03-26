package org.deviceconnect.android.deviceplugin.sonycamera.utils;

import android.net.Uri;

import org.deviceconnect.android.deviceplugin.sonycamera.BuildConfig;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Mixed Replace Media Server.
 * @author NTT DOCOMO, INC.
 */
public class MixedReplaceMediaServer {

    /** Logger. */
    private Logger mLogger = Logger.getLogger("sonycamera.dplugin");
    
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
     * Manage a thread.
     */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(MAX_CLIENT_SIZE);

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
     * スリープ時間.
     */
    private int mTimeSlice = 60;

    /**
     * Set a ServerEventListener.
     * @param listener server event listener
     */
    public void setServerEventListener(final ServerEventListener listener) {
        mListener = listener;
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
                for (ServerRunnable run : mRunnables) {
                    run.offerMedia(media);
                }
            }
        }
    }

    /**
     * Set a fps.
     * @param fps fps
     */
    public void setFPS(final int fps) {
        mTimeSlice = 1000 / fps;
    }

    /**
     * Set a time slice.
     * @param timeSlice time slice
     */
    public void setTimeSlice(final int timeSlice) {
        mTimeSlice = timeSlice;
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
            mLogger.fine("Open a server socket.");
        } catch (IOException e) {
            // Failed to open server socket
            mStopFlag = true;
            if (mListener != null) {
                mListener.onError();
            }
            return null;
        }

        mPath = UUID.randomUUID().toString();

        mStopFlag = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null) {
                        mListener.onStart();
                    }
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

                    if (mListener != null) {
                        mListener.onStop();
                    }
                }
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
        synchronized (mRunnables) {
            for (ServerRunnable run : mRunnables) {
                run.offerMedia(new byte[0]);
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
     * Interface of Sever event.
     */
    public interface ServerEventListener {
        /**
         * Event that started a server.
         */
        void onStart();
        /**
         * Event that stopped a server.
         */
        void onStop();
        /**
         * Event that occurred a error on server.
         */
        void onError();
    }

    /**
     * Class of Server.
     */
    private class ServerRunnable implements Runnable {
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
        private final BlockingQueue<byte[]> mMediaQueue = new ArrayBlockingQueue<>(MAX_MEDIA_CACHE);
        
        /**
         * Constructor.
         * @param socket socket
         */
        ServerRunnable(final Socket socket) {
            mSocket = socket;
        }
        
        @Override
        public void run() {
            mLogger.fine("accept client.");
            mRunnables.add(this);
            try {
                mStream = mSocket.getOutputStream();

                byte[] buf = new byte[BUF_SIZE];
                InputStream in = mSocket.getInputStream();
                int len = in.read(buf, 0, BUF_SIZE);
                if (len == -1) {
                    // error
                    return;
                }
                decodeHeader(buf, len);

                if (mRunnables.size() > MAX_CLIENT_SIZE) {
                    mStream.write(generateServiceUnavailable().getBytes());
                    mStream.flush();
                } else {
                    mStream.write(generateHttpHeader().getBytes());
                    mStream.flush();

                    while (!mStopFlag) {
                        long oldTime = System.currentTimeMillis();
                        byte[] media = mMediaQueue.take();
                        if (mSocket.isClosed()) {
                            break;
                        }
                        if (media.length > 0) {
                            sendMedia(media);
                        }
                        long newTime = System.currentTimeMillis();
                        long sleepTime = mTimeSlice - (newTime - oldTime);
                        if (sleepTime < 2) {
                            sleepTime = 2;
                        }
                        Thread.sleep(sleepTime);
                    }
                }
            } catch (InterruptedException e) {
                if (mStream != null) {
                    try {
                        mStream.write(generateInternalServerError().getBytes());
                        mStream.flush();
                    } catch (IOException e1) {
                        mLogger.warning("Error server socket[" + mServerName + "]");
                    }
                }
                Thread.currentThread().interrupt();
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
                mRunnables.remove(this);
                
                if (mRunnables.isEmpty()) {
                    stop();
                }
            }
        }
        
        /**
         * Inserts the media data into queue.
         * @param media the media to add
         * @return true if the media data was added to this queue, else false
         */
        private boolean offerMedia(final byte[] media) {
            if (mMediaQueue.size() == MAX_MEDIA_CACHE) {
                mMediaQueue.remove();
            }
            return mMediaQueue.offer(media);
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
        private void decodeHeader(final byte[] buf, final int len) throws IOException {
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
        return "HTTP/1.0 200 OK\r\n" +
                "Server: " + mServerName + "\r\n" +
                "Connection: close\r\n" +
                "Max-Age: 0\r\n" +
                "Expires: 0\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Content-Type: multipart/x-mixed-replace; " +
                "boundary=" + mBoundary + "\r\n" +
                "\r\n" +
                "--" + mBoundary + "\r\n";
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
        return ("HTTP/1.0 " + status + " OK\r\n") +
                "Server: " + mServerName + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }
}
