package net.majorkernelpanic.streaming.rtsp;


import android.hardware.Camera;
import android.util.Log;

import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.video.VideoQuality;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.majorkernelpanic.streaming.SessionBuilder.AUDIO_AAC;
import static net.majorkernelpanic.streaming.SessionBuilder.AUDIO_AMRNB;
import static net.majorkernelpanic.streaming.SessionBuilder.AUDIO_NONE;
import static net.majorkernelpanic.streaming.SessionBuilder.VIDEO_H264;
import static net.majorkernelpanic.streaming.SessionBuilder.VIDEO_NONE;

public class RtspServerImpl implements RtspServer {

    private final static String TAG = "RtspServerImpl";

    private int mPort = DEFAULT_PORT;
    private ServerThread mServerThread;
    private final Map<Session, Object> mSessions = new WeakHashMap<>(2);
    private final List<CallbackListener> mCallbackListeners = new LinkedList<>();
    private String mName;

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
    public synchronized void start() {
        if (mServerThread == null) {
            try {
                mServerThread = new ServerThread();
                mServerThread.start();
            } catch (IOException e) {
                mServerThread = null;
                Log.e(TAG,"Port already in use !");
                postError(e, Error.BIND_FAILED);
            }
        }
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

    private void postMessage(int id) {
        synchronized (mCallbackListeners) {
            if (mCallbackListeners.size() > 0) {
                for (CallbackListener cl : mCallbackListeners) {
                    cl.onMessage(this, id);
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

    /**
     * By default the RTSP uses {@link UriParser} to parse the URI requested by the client
     * but you can change that behavior by override this mMethod.
     * @param uri The uri that the client has requested
     * @param client The socket associated to the client
     * @return A proper session
     */
    private Session generateSession(final String uri, final Socket client) throws IllegalStateException, IOException {
        Session session = parse(uri);
        session.setOrigin(client.getLocalAddress().getHostAddress());
        if (session.getDestination() == null) {
            session.setDestination(client.getInetAddress().getHostAddress());
        }
        return session;
    }

    private RtspResponse createResponseForRequest(final RtspRequest request) {
        RtspResponse response = new RtspResponse(request);
        response.serverName = mName;
        return response;
    }

    class ServerThread extends Thread implements Runnable {

        private final ServerSocket mServerSocket;

        ServerThread() throws IOException {
            mServerSocket = new ServerSocket(mPort);
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
                postMessage(MESSAGE_STREAMING_STOPPED);
            }
            mSession.release();

            try {
                mClient.close();
            } catch (IOException ignore) {}

            Log.i(TAG, "Client disconnected");
        }

        RtspResponse processRequest(final RtspRequest request) throws IllegalStateException, IOException {
            RtspResponse response = createResponseForRequest(request);
            String method = request.getMethod();

			/* ********************************************************************************** */
			/* ********************************* Method DESCRIBE ******************************** */
			/* ********************************************************************************** */
            if (method.equalsIgnoreCase("DESCRIBE")) {

                // Parse the requested URI and configure the session
                mSession = generateSession(request.getUri(), mClient);
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
                    postMessage(MESSAGE_STREAMING_STARTED);
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

    /**
     * Configures a Session according to the given URI.
     * Here are some examples of URIs that can be used to configure a Session:
     * <ul><li>rtsp://xxx.xxx.xxx.xxx:8086?h264</li>
     * <li>rtsp://xxx.xxx.xxx.xxx:8086?h263&camera=front</li>
     * <li>rtsp://xxx.xxx.xxx.xxx:8086?h264=200-20-320-240</li>
     * <li>rtsp://xxx.xxx.xxx.xxx:8086?aac</li></ul>
     * @param uri The URI
     * @return A Session configured according to the URI
     */
    private static Session parse(String uri) {
        SessionBuilder builder = SessionBuilder.getInstance().clone();
        byte audioApi = 0;
        byte videoApi = 0;

        List<NameValuePair> params = URLEncodedUtils.parse(URI.create(uri),"UTF-8");
        if (params.size() > 0) {
            builder.setAudioEncoder(AUDIO_NONE).setVideoEncoder(VIDEO_NONE);

            // Those parameters must be parsed first or else they won't necessarily be taken into account
            for (NameValuePair param : params) {

                // CAMERA -> the client can choose between the front facing camera and the back facing camera
                if (param.getName().equalsIgnoreCase("camera")) {
                    if (param.getValue().equalsIgnoreCase("back"))
                        builder.setCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    else if (param.getValue().equalsIgnoreCase("front"))
                        builder.setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }

                // MULTICAST -> the stream will be sent to a multicast group
                // The default mutlicast address is 228.5.6.7, but the client can specify another
                else if (param.getName().equalsIgnoreCase("multicast")) {
                    if (param.getValue()!=null) {
                        try {
                            InetAddress addr = InetAddress.getByName(param.getValue());
                            if (!addr.isMulticastAddress()) {
                                throw new IllegalStateException("Invalid multicast address !");
                            }
                            builder.setDestination(param.getValue());
                        } catch (UnknownHostException e) {
                            throw new IllegalStateException("Invalid multicast address !");
                        }
                    }
                    else {
                        // Default multicast address
                        builder.setDestination("228.5.6.7");
                    }
                }

                // UNICAST -> the client can use this to specify where he wants the stream to be sent
                else if (param.getName().equalsIgnoreCase("unicast")) {
                    if (param.getValue()!=null) {
                        builder.setDestination(param.getValue());
                    }
                }

                // VIDEOAPI -> can be used to specify what api will be used to encode video (the MediaRecorder API or the MediaCodec API)
                else if (param.getName().equalsIgnoreCase("videoapi")) {
                    if (param.getValue()!=null) {
                        if (param.getValue().equalsIgnoreCase("mc2")) {
                            videoApi = MediaStream.MODE_MEDIACODEC_API_2;
                        } else if (param.getValue().equalsIgnoreCase("mc")) {
                            videoApi = MediaStream.MODE_MEDIACODEC_API;
                        }
                    }
                }

                // AUDIOAPI -> can be used to specify what api will be used to encode audio (the MediaRecorder API or the MediaCodec API)
                else if (param.getName().equalsIgnoreCase("audioapi")) {
                    if (param.getValue()!=null) {
                        if (param.getValue().equalsIgnoreCase("mc2")) {
                            audioApi = MediaStream.MODE_MEDIACODEC_API_2;
                        } else if (param.getValue().equalsIgnoreCase("mc")) {
                            audioApi = MediaStream.MODE_MEDIACODEC_API;
                        }
                    }
                }

                // TTL -> the client can modify the time to live of packets
                // By default ttl=64
                else if (param.getName().equalsIgnoreCase("ttl")) {
                    if (param.getValue()!=null) {
                        try {
                            int ttl = Integer.parseInt(param.getValue());
                            if (ttl<0) throw new IllegalStateException();
                            builder.setTimeToLive(ttl);
                        } catch (Exception e) {
                            throw new IllegalStateException("The TTL must be a positive integer !");
                        }
                    }
                }

                // H.264
                else if (param.getName().equalsIgnoreCase("h264")) {
                    VideoQuality quality = VideoQuality.parseQuality(param.getValue());
                    builder.setVideoQuality(quality).setVideoEncoder(VIDEO_H264);
                }

                // AMR
                else if (param.getName().equalsIgnoreCase("amrnb") || param.getName().equalsIgnoreCase("amr")) {
                    AudioQuality quality = AudioQuality.parseQuality(param.getValue());
                    builder.setAudioQuality(quality).setAudioEncoder(AUDIO_AMRNB);
                }

                // AAC
                else if (param.getName().equalsIgnoreCase("aac")) {
                    AudioQuality quality = AudioQuality.parseQuality(param.getValue());
                    builder.setAudioQuality(quality).setAudioEncoder(AUDIO_AAC);
                }

            }

        }

        if (builder.getVideoEncoder() == VIDEO_NONE && builder.getAudioEncoder() == AUDIO_NONE) {
            SessionBuilder b = SessionBuilder.getInstance();
            builder.setVideoEncoder(b.getVideoEncoder());
            builder.setAudioEncoder(b.getAudioEncoder());
        }

        Session session = builder.build();
        if (videoApi > 0 && session.getVideoTrack() != null) {
            session.getVideoTrack().setStreamingMethod(videoApi);
        }
        if (audioApi > 0 && session.getAudioTrack() != null) {
            session.getAudioTrack().setStreamingMethod(audioApi);
        }
        return session;
    }
}
