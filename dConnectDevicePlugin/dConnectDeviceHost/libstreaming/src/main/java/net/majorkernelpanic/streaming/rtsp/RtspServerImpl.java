package net.majorkernelpanic.streaming.rtsp;


import android.util.Log;

import net.majorkernelpanic.streaming.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtspServerImpl implements RtspServer {

    private final static String TAG = "RtspServerImpl";

    private int mPort = DEFAULT_PORT;
    private ServerThread mServerThread;
    private final Map<Session, Object> mSessions = new WeakHashMap<>(2);
    private final List<CallbackListener> mCallbackListeners = new LinkedList<>();
    private String mName;
    private final Delegate mDefaultDelegate = new Delegate() {
        @Override
        public Session generateSession(final String uri, final Socket client) {
            return null;
        }
    };
    private Delegate mDelegate;

    public RtspServerImpl(final String name) {
        mName = name;
    }

    @Override
    public void addCallbackListener(final CallbackListener listener) {
        synchronized (mCallbackListeners) {
            for (CallbackListener l : mCallbackListeners) {
                if (l == listener) {
                    return;
                }
            }
            mCallbackListeners.add(listener);
        }
    }

    @Override
    public void removeCallbackListener(final CallbackListener listener) {
        synchronized (mCallbackListeners) {
            mCallbackListeners.remove(listener);
        }
    }

    @Override
    public void setDelegate(final Delegate delegate) {
        mDelegate = delegate;
    }

    private Delegate getDelegate() {
        return mDelegate != null ? mDelegate : mDefaultDelegate;
    }

    @Override
    public int getPort() {
        return mPort;
    }

    @Override
    public void setPort(int port) {
        mPort = port;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public synchronized boolean start() {
        if (mServerThread == null) {
            for (int num = 0; ; num++) {
                try {
                    mServerThread = new ServerThread(mPort + num);
                    mServerThread.start();
                    mPort += num;
                    return true;
                } catch (IOException e) {
                    if (num >= 1000) {
                        mServerThread = null;
                        Log.e(TAG,"Port already in use !");
                        postError(e, Error.BIND_FAILED);
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public synchronized void stop() {
        if (mServerThread != null) {
            try {
                mServerThread.kill();
                for (Session session : mSessions.keySet() ) {
                    if (session != null) {
                        if (session.isStreaming()) {
                            session.stop();
                        }
                    }
                }
            } catch (IOException e) {
                postError(e, Error.STOP_FAILED);
            } finally {
                mServerThread = null;
            }
        }
    }

    /** Returns whether or not the RTSP server is streaming to some client(s). */
    public boolean isStreaming() {
        for ( Session session : mSessions.keySet() ) {
            if ( session != null ) {
                if (session.isStreaming()) return true;
            }
        }
        return false;
    }

    /** Returns the bandwidth consumed by the RTSP server in bits per second. */
    public long getBitrate() {
        long bitrate = 0;
        for ( Session session : mSessions.keySet() ) {
            if ( session != null ) {
                if (session.isStreaming()) bitrate += session.getBitrate();
            }
        }
        return bitrate;
    }

    private void postMessage(Message message) {
        synchronized (mCallbackListeners) {
            if (mCallbackListeners.size() > 0) {
                for (CallbackListener cl : mCallbackListeners) {
                    cl.onMessage(this, message);
                }
            }
        }
    }

    private void postError(Exception cause, Error error) {
        synchronized (mCallbackListeners) {
            if (mCallbackListeners.size() > 0) {
                for (CallbackListener cl : mCallbackListeners) {
                    cl.onError(this, cause, error);
                }
            }
        }
    }

    private RtspResponse createResponseForRequest(final RtspRequest request) {
        RtspResponse response = new RtspResponse(request);
        response.serverName = mName;
        return response;
    }

    class ServerThread extends Thread implements Runnable {

        private final ServerSocket mServerSocket;

        ServerThread(final int port) throws IOException {
            mServerSocket = new ServerSocket(port);
        }

        @Override
        public void run() {
            Log.i(TAG,"RTSP server listening on port "+ mServerSocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    new WorkerThread(mServerSocket.accept()).start();
                } catch (SocketException e) {
                    break;
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                }
            }
            Log.i(TAG,"RTSP server stopped !");
        }

        void kill() throws IOException {
            mServerSocket.close();

            try {
                this.join();
            } catch (InterruptedException ignore) {}
        }

    }

    // One thread per client
    class WorkerThread extends Thread implements Runnable {

        private final Socket mClient;
        private final OutputStream mOutput;
        private final BufferedReader mInput;

        private Session mSession;

        WorkerThread(final Socket client) throws IOException {
            mInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
            mOutput = client.getOutputStream();
            mClient = client;
            mSession = new Session();
        }

        public void run() {
            RtspRequest request;
            RtspResponse response;

            Log.i(TAG, "Connection from " + mClient.getInetAddress().getHostAddress());

            while (!Thread.interrupted()) {
                request = null;
                response = null;

                // Parse the request
                try {
                    request = RtspRequest.parseRequest(mInput);
                } catch (SocketException e) {
                    // Client has left
                    break;
                } catch (Exception e) {
                    // We don't understand the request :/
                    response = createResponseForRequest(null);
                    response.status = RtspResponse.STATUS_BAD_REQUEST;
                }

                // Do something accordingly like starting the streams, sending a session description
                if (request != null) {
                    try {
                        response = processRequest(request);
                    } catch (Exception e) {
                        // This alerts the main thread that something has gone wrong in this thread
                        postError(e, Error.START_FAILED);
                        Log.e(TAG,e.getMessage()!=null?e.getMessage():"An error occurred");
                        e.printStackTrace();
                        response = createResponseForRequest(request);
                    }
                }

                // We always send a response
                // The client will receive an "INTERNAL SERVER ERROR" if an exception has been thrown at some point
                try {
                    response.send(mOutput);
                } catch (IOException e) {
                    Log.e(TAG,"Response was not sent properly");
                    break;
                }
            }

            // Streaming stops when client disconnects
            boolean streaming = isStreaming();
            mSession.syncStop();
            if (streaming && !isStreaming()) {
                postMessage(Message.STREAMING_STOPPED);
            }
            mSession.release();

            try {
                mClient.close();
            } catch (IOException ignore) {}

            Log.i(TAG, "Client disconnected");
        }

        RtspResponse processRequest(final RtspRequest request) throws IllegalStateException, IOException {
            RtspResponse response = createResponseForRequest(request);
            // TODO メソッドを enum で定義する
            String method = request.getMethod();
            Delegate delegate = getDelegate();

			/* ********************************************************************************** */
			/* ********************************* Method DESCRIBE ******************************** */
			/* ********************************************************************************** */
            if (method.equalsIgnoreCase("DESCRIBE")) {

                // Parse the requested URI and configure the session
                mSession = delegate.generateSession(request.getUri(), mClient);
                mSessions.put(mSession, null);
                mSession.syncConfigure();

                String requestContent = mSession.getSessionDescription();
                String requestAttributes =
                        "Content-Base: " + mClient.getLocalAddress().getHostAddress() + ":" + mClient.getLocalPort() + "/\r\n" +
                        "Content-Type: application/sdp\r\n";

                response.attributes = requestAttributes;
                response.content = requestContent;

                // If no exception has been thrown, we reply with OK
                response.status = RtspResponse.STATUS_OK;
            }

			/* ********************************************************************************** */
			/* ********************************* Method OPTIONS ********************************* */
			/* ********************************************************************************** */
            else if (method.equalsIgnoreCase("OPTIONS")) {
                response.status = RtspResponse.STATUS_OK;
                response.attributes = "Public: DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE\r\n";
                response.status = RtspResponse.STATUS_OK;
            }

			/* ********************************************************************************** */
			/* ********************************** Method SETUP ********************************** */
			/* ********************************************************************************** */
            else if (method.equalsIgnoreCase("SETUP")) {
                Pattern p;
                Matcher m;
                int p2, p1, ssrc, trackId, src[];
                String destination;

                p = Pattern.compile("trackID=(\\w+)",Pattern.CASE_INSENSITIVE);
                m = p.matcher(request.getUri());

                if (!m.find()) {
                    response.status = RtspResponse.STATUS_BAD_REQUEST;
                    return response;
                }

                trackId = Integer.parseInt(m.group(1));

                if (!mSession.trackExists(trackId)) {
                    response.status = RtspResponse.STATUS_NOT_FOUND;
                    return response;
                }

                p = Pattern.compile("client_port=(\\d+)-(\\d+)",Pattern.CASE_INSENSITIVE);
                m = p.matcher(request.getHeader("transport"));

                if (!m.find()) {
                    int[] ports = mSession.getTrack(trackId).getDestinationPorts();
                    p1 = ports[0];
                    p2 = ports[1];
                } else {
                    p1 = Integer.parseInt(m.group(1));
                    p2 = Integer.parseInt(m.group(2));
                }

                ssrc = mSession.getTrack(trackId).getSSRC();
                src = mSession.getTrack(trackId).getLocalPorts();
                destination = mSession.getDestination();

                mSession.getTrack(trackId).setDestinationPorts(p1, p2);

                boolean streaming = isStreaming();
                mSession.syncStart(trackId);
                if (!streaming && isStreaming()) {
                    postMessage(Message.STREAMING_STARTED);
                }

                response.attributes = "Transport: RTP/AVP/UDP;"+(InetAddress.getByName(destination).isMulticastAddress()?"multicast":"unicast")+
                        ";destination="+mSession.getDestination()+
                        ";client_port="+p1+"-"+p2+
                        ";server_port="+src[0]+"-"+src[1]+
                        ";ssrc="+Integer.toHexString(ssrc)+
                        ";mode=play\r\n" +
                        "Session: "+ "1185d20035702ca" + "\r\n" +
                        "Cache-Control: no-cache\r\n";
                response.status = RtspResponse.STATUS_OK;

                // If no exception has been thrown, we reply with OK
                response.status = RtspResponse.STATUS_OK;

            }

			/* ********************************************************************************** */
			/* ********************************** Method PLAY *********************************** */
			/* ********************************************************************************** */
            else if (method.equalsIgnoreCase("PLAY")) {
                String requestAttributes = "RTP-Info: ";
                if (mSession.trackExists(0)) {
                    requestAttributes += "url=rtsp://"+mClient.getLocalAddress().getHostAddress()+":"+mClient.getLocalPort()+"/trackID="+0+";seq=0,";
                }
                if (mSession.trackExists(1)) {
                    requestAttributes += "url=rtsp://"+mClient.getLocalAddress().getHostAddress()+":"+mClient.getLocalPort()+"/trackID="+1+";seq=0,";
                }
                requestAttributes = requestAttributes.substring(0, requestAttributes.length()-1) + "\r\nSession: 1185d20035702ca\r\n";

                response.attributes = requestAttributes;

                // If no exception has been thrown, we reply with OK
                response.status = RtspResponse.STATUS_OK;
            }

			/* ********************************************************************************** */
			/* ********************************** Method PAUSE ********************************** */
			/* ********************************************************************************** */
            else if (method.equalsIgnoreCase("PAUSE")) {
                response.status = RtspResponse.STATUS_OK;
            }

			/* ********************************************************************************** */
			/* ********************************* Method TEARDOWN ******************************** */
			/* ********************************************************************************** */
            else if (method.equalsIgnoreCase("TEARDOWN")) {
                response.status = RtspResponse.STATUS_OK;
            }

			/* ********************************************************************************** */
			/* ********************************* Unknown mMethod ? ******************************* */
			/* ********************************************************************************** */
            else {
                Log.e(TAG,"Command unknown: "+request);
                response.status = RtspResponse.STATUS_BAD_REQUEST;
            }

            return response;
        }
    }
}
