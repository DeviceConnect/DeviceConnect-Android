/*
 MediaConnection.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MediaConnection.
 *
 * @author NTT DOCOMO, INC.
 */
public class MediaConnection {

    /**
     * Tag for debugging.
     */
    private static final String TAG = "WEBRTC";

    private static final String PREFIX_MEDIA_PEERJS = "mc_";

    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_H264 = "H264";

    private PeerConnectionFactory mFactory;
    private PeerConnection mPeerConnection;

    private MediaStream mLocalMediaStream;
    private MediaStream mRemoteMediaStream;
    private PeerOption mOption;

    private String mPeerId;
    private String mConnectionId;
    private String mType = "media";
    private String mBrowser;
    private SessionDescription mRemoteSdp;
    private boolean mOpen;

    private OnMediaConnectionCallback mCallback;
    private OnMediaEventListener mListener;

    /**
     * Constructor.
     * @param factory Instance of PeerConnectionFactory
     * @param option option of connection
     */
    MediaConnection(final PeerConnectionFactory factory, final PeerOption option) {
        mFactory = factory;
        mOption = option;
        mConnectionId = PREFIX_MEDIA_PEERJS + PeerUtil.randomToken(16);
        createPeerConnection(createIceServerList(option.getIceServer()));
    }

    /**
     * Retrieves the peer id of the destination.
     * @return peer id
     */
    public String getPeerId() {
        return mPeerId;
    }

    /**
     * Sets the peer id of the destination.
     * @param peerId peer id
     */
    public void setPeerId(final String peerId) {
        mPeerId = peerId;
    }

    /**
     * Retrieves the connection id.
     * @return connection id
     */
    public String getConnectionId() {
        return mConnectionId;
    }

    /**
     * Retrieves the type.
     * @return type
     */
    public String getType() {
        return mType;
    }

    /**
     * Returns true if connected to the p2p.
     * @return true if connected to the p2p.
     */
    public boolean isOpen() {
        return mOpen;
    }

    /**
     * Set a local media stream.
     * @param stream local media stream
     */
    public void setLocalMediaStream(final MediaStream stream) {
        if (mLocalMediaStream != null) {
            // TODO: 既にローカルメディアが設定されている場合
        }
        mLocalMediaStream = stream;
        mPeerConnection.addStream(mLocalMediaStream.getMediaStream());
    }

    /**
     * Gets a local MediaStream.
     * @return MediaStream
     */
    public MediaStream getLocalMediaStream() {
        return mLocalMediaStream;
    }

    /**
     * Gets a remote MediaStream.
     * @return MediaStream
     */
    public MediaStream getRemoteMediaStream() {
        return mRemoteMediaStream;
    }

    /**
     * Sets the callback.
     * @param callback callback
     */
    void setOnConnectionCallback(final OnMediaConnectionCallback callback) {
        mCallback = callback;
    }

    /**
     * Sets the OnMediaEventListener.
     * @param listener listener
     */
    public void setOnMediaEventListener(final OnMediaEventListener listener) {
        mListener = listener;
    }

    /**
     * Starts a video.
     */
    public void startVideo() {
        if (mLocalMediaStream != null) {
            mLocalMediaStream.startVideo();
        }
    }

    /**
     * Stops a video.
     */
    public void stopVideo() {
        if (mLocalMediaStream != null) {
            mLocalMediaStream.stopVideo();
        }
    }

    /**
     * Closes a MediaConnection.
     */
    public synchronized void close() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ MediaConnection::close: " + this);
            Log.d(TAG, "@@@ MediaConnection::close: mOpen=" + mOpen);
        }
        if (!mOpen) {
            return;
        }
        mOpen = false;

        if (mPeerConnection != null) {
            mPeerConnection.dispose();
            mPeerConnection = null;
        }

        if (mLocalMediaStream != null) {
            mLocalMediaStream.close();
            mLocalMediaStream = null;
        }

        if (mRemoteMediaStream != null) {
            mRemoteMediaStream.close();
            mRemoteMediaStream = null;
        }

        if (mCallback != null) {
            mCallback.onClose(this);
        }

        if (mListener != null) {
            mListener.onClose();
        }
    }

    /**
     * Creates an offer message.
     */
    public void createOffer() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ MediaConnection::createOffer");
        }

        mPeerConnection.createOffer(mSdpObserver, createSDPMediaConstraints());
    }

    /**
     * Creates an answer message from json message.
     * @param json offer message
     */
    public void createAnswer(final JSONObject json) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ MediaConnection::createAnswer");
            Log.d(TAG, "@@@@ json: " + json.toString());
        }

        JSONObject payload = json.optJSONObject("payload");
        JSONObject sdpObj = payload.optJSONObject("sdp");
        String type = PeerUtil.getJSONString(sdpObj, "type", "");
        String sdp = PeerUtil.getJSONString(sdpObj, "sdp", "");
        if (getType().equalsIgnoreCase("media")) {
            sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true);
            sdp = preferCodec(sdp, VIDEO_CODEC_H264, false);
        }

        SessionDescription.Type sdpType = SessionDescription.Type.OFFER;
        mConnectionId = PeerUtil.getJSONString(payload, "connectionId", null);
        mRemoteSdp = new SessionDescription(sdpType, sdp);
        if (mPeerConnection != null) {
            mPeerConnection.setRemoteDescription(mSdpObserver, mRemoteSdp);
        }

        mOpen = true;
    }

    /**
     * Handles an answer message.
     * @param json message
     */
    public void handleAnswer(final JSONObject json) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@@ handleAnswer");
            Log.d(TAG, "@@@@ json: " + json.toString());
        }

        JSONObject payload = json.optJSONObject("payload");
        JSONObject sdpObj = payload.optJSONObject("sdp");
        String type = PeerUtil.getJSONString(sdpObj, "type", "");
        String sdp = PeerUtil.getJSONString(sdpObj, "sdp", "");
        if (getType().equalsIgnoreCase("media")) {
            sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true);
            sdp = preferCodec(sdp, VIDEO_CODEC_H264, false);
        }

        SessionDescription.Type sdpType = SessionDescription.Type.ANSWER;
        mBrowser = PeerUtil.getJSONString(payload, "browser", "Supported");
        mRemoteSdp = new SessionDescription(sdpType, sdp);
        if (mPeerConnection != null) {
            mPeerConnection.setRemoteDescription(mSdpObserver, mRemoteSdp);
        }

        mOpen = true;
    }

    /**
     * Handles a candidate message.
     * @param json candidate message
     */
    public void handleCandidate(final JSONObject json) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@@ handleCandidate");
            Log.d(TAG, "@@@@ json: " + json.toString());
        }

        PeerConnection.IceConnectionState state = mPeerConnection.iceConnectionState();
        if (state == PeerConnection.IceConnectionState.COMPLETED) {
            return;
        }

        JSONObject payload = json.optJSONObject("payload");
        JSONObject candidateObj = payload.optJSONObject("candidate");
        String sdpMid = candidateObj.optString("sdpMid");
        Integer sdpMLineIndex = candidateObj.optInt("sdpMLineIndex");
        String candidate = candidateObj.optString("candidate");

        IceCandidate ice = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
        boolean result = mPeerConnection.addIceCandidate(ice);
        if (BuildConfig.DEBUG) {
            if (!result) {
                Log.i(TAG, "@@@ handleCandidate NG");
            } else {
                Log.i(TAG, "@@@ handleCandidate OK");
            }
        }
    }

    /**
     * Creates a PeerConnection.
     * @param config configuration of PeerConnection
     */
    private void createPeerConnection(final List<PeerConnection.IceServer> config) {
        MediaConstraints mc = new MediaConstraints();
        try {
            mPeerConnection = mFactory.createPeerConnection(config, mc, mObserver);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "@@@ Failed to create PeerConnection.", e);
            }
            throw new RuntimeException(e);
        }
    }

    private String preferCodec(final String sdpDescription, final String codec, final boolean isAudio) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        String codecRtpMap = null;
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        String mediaDescription = "m=video ";
        if (isAudio) {
            mediaDescription = "m=audio ";
        }
        for (int i = 0; (i < lines.length)
                && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                continue;
            }
        }
        if (mLineIndex == -1) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
            }
            return sdpDescription;
        }
        if (codecRtpMap == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "No rtpmap for " + codec);
            }
            return sdpDescription;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + ", prefer at "
                    + lines[mLineIndex]);
        }
        String[] origMLineParts = lines[mLineIndex].split(" ");
        if (origMLineParts.length > 3) {
            StringBuilder newMLine = new StringBuilder();
            int origPartIndex = 0;
            // Format is: m=<media> <port> <proto> <fmt> ...
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(codecRtpMap);
            for (; origPartIndex < origMLineParts.length; origPartIndex++) {
                if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex]);
                }
            }
            lines[mLineIndex] = newMLine.toString();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Change media description: " + lines[mLineIndex]);
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Wrong SDP media description format: " + lines[mLineIndex]);
            }
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }

    /**
     * Creates the list of IceServer from uri.
     * If uriList is null, returns the list of default.
     * @param uriList list of IceServer's uri
     * @return list of IceServer
     */
    private List<PeerConnection.IceServer> createIceServerList(final List<String> uriList) {
        if (uriList == null) {
            return PeerUtil.getSkyWayIceServer();
        } else {
            List<PeerConnection.IceServer> list = new ArrayList<>();
            for (String uri : uriList) {
                list.add(new PeerConnection.IceServer(uri));
            }
            return list;
        }
    }

    /**
     * Creates the MediaConstraints of SessionDescription.
     * @return MediaConstraints
     */
    private MediaConstraints createSDPMediaConstraints() {
        MediaConstraints mc = new MediaConstraints();
        mc.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio", "true"));
        mc.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));
        return mc;
    }

    /**
     * Implements the {@link org.webrtc.PeerConnection.Observer}.
     */
    private final PeerConnection.Observer mObserver = new PeerConnection.Observer() {
        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onIceConnectionReceivingChange");
                Log.d(TAG, "@@@ b: " + b);
            }
        }

        @Override
        public void onSignalingChange(final PeerConnection.SignalingState signalingState) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onSignalingChange");
                Log.d(TAG, "@@@ SignalingState: " + signalingState.toString());
            }
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState iceConnectionState) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onIceConnectionChange");
                Log.d(TAG, "@@@ IceConnectionState: " + iceConnectionState.toString());
            }

            switch (iceConnectionState) {
                case FAILED:
                    if (mCallback != null) {
                        mCallback.onError(MediaConnection.this);
                    }
                    // no break;
                case DISCONNECTED:
                    close();
                    break;
                default:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "@@@ iceConnectionState=" + iceConnectionState);
                    }
                    break;
            }
        }

        @Override
        public void onIceGatheringChange(final PeerConnection.IceGatheringState iceGatheringState) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onIceGatheringChange");
                Log.d(TAG, "@@@ IceGatheringState: " + iceGatheringState.toString());
            }
        }

        @Override
        public void onIceCandidate(final IceCandidate iceCandidate) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onIceCandidate");
                Log.d(TAG, "@@@ IceCandidate: " + iceCandidate.toString());
            }

            if (mCallback != null) {
                mCallback.onIceCandidate(MediaConnection.this, iceCandidate);
            }
        }

        @Override
        public void onAddStream(final org.webrtc.MediaStream mediaStream) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "@@@ onAddStream");
                Log.d(TAG, "@@@ MediaStream: " + mediaStream.toString());
            }

            mRemoteMediaStream = new MediaStream(mediaStream);
            if (mListener != null) {
                mListener.onAddStream(mRemoteMediaStream);
            }
        }

        @Override
        public void onRemoveStream(final org.webrtc.MediaStream mediaStream) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onRemoveStream");
                Log.d(TAG, "@@@ MediaStream: " + mediaStream.toString());
            }

            if (mRemoteMediaStream != null) {
                if (mListener != null) {
                    mListener.onRemoveStream(mRemoteMediaStream);
                }
                mRemoteMediaStream.close();
                mRemoteMediaStream = null;
            }
            if (mediaStream.videoTracks.size() == 1) {
                mediaStream.videoTracks.get(0).dispose();
            }
        }

        @Override
        public void onDataChannel(final DataChannel dataChannel) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onDataChannel");
                Log.d(TAG, "@@@ DataChannel: " + dataChannel.toString());
            }
        }

        @Override
        public void onRenegotiationNeeded() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onRenegotiationNeeded");
            }
        }
    };

    /**
     * Implements the {@link SdpObserver}.
     */
    private final SdpObserver mSdpObserver = new SdpObserver() {
        private SessionDescription mSdp;

        @Override
        public void onCreateSuccess(final SessionDescription sessionDescription) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onCreateSuccess");
                Log.d(TAG, "@@@ Description: " + sessionDescription.toString());
            }

            String sdp = sessionDescription.description;
            if (getType().equalsIgnoreCase("media")) {
                sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true);
                sdp = preferCodec(sdp, VIDEO_CODEC_H264, false);
            }

            mSdp = new SessionDescription(sessionDescription.type, sdp);
            mPeerConnection.setLocalDescription(mSdpObserver, mSdp);
        }

        @Override
        public void onSetSuccess() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "@@@ onSetSuccess");
            }

            PeerConnection.SignalingState state = mPeerConnection.signalingState();
            if (state == PeerConnection.SignalingState.HAVE_REMOTE_OFFER) {
                mPeerConnection.createAnswer(this, createSDPMediaConstraints());
            } else /*if (state == PeerConnection.SignalingState.HAVE_LOCAL_OFFER)*/ {
                if (mCallback != null && mSdp != null) {
                    mCallback.onLocalDescription(MediaConnection.this, mSdp);
                }
                mSdp = null;
            }
        }

        @Override
        public void onCreateFailure(final String s) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "@@@ onCreateFailure");
                Log.e(TAG, "@@@ s=" + s);
            }
            if (mCallback != null) {
                mCallback.onError(MediaConnection.this);
            }
        }

        @Override
        public void onSetFailure(String s) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "@@@ onCreateFailure");
                Log.e(TAG, "@@@ s=" + s);
            }
            if (mCallback != null) {
                mCallback.onError(MediaConnection.this);
            }
        }
    };

    /**
     * This interface is used to implement {@link MediaConnection} callbacks.
     */
    public interface OnMediaConnectionCallback {
        /**
         * Calls when the SessionDescription for local is set to the MediaConnection.
         * @param conn MediaConnection
         * @param sdp session description
         */
        void onLocalDescription(MediaConnection conn, SessionDescription sdp);

        /**
         * Calls when the IceCandidate is set to the MediaConnection.
         * @param conn MediaConnection
         * @param iceCandidate iceCandidate
         */
        void onIceCandidate(MediaConnection conn, IceCandidate iceCandidate);

        /**
         * Calls when the MediaConnection has been closed.
         * @param conn Closed the MediaConnection
         */
        void onClose(MediaConnection conn);

        /**
         * Calls when the error has occurred.
         * @param conn Closed the MediaConnection
         */
        void onError(MediaConnection conn);
    }

    /**
     * This interface is used to implement {@link MediaConnection} listeners.
     */
    public interface OnMediaEventListener {
        /**
         * Calls when the MediaStream has been added.
         * @param mediaStream Added the MediaStream
         */
        void onAddStream(MediaStream mediaStream);
        /**
         * Calls when the MediaStream has been removed.
         * @param mediaStream Removed the MediaStream
         */
        void onRemoveStream(MediaStream mediaStream);

        /**
         * Calls when the MediaStream has been closed.
         */
        void onClose();

        /**
         * Calls when the error has occurred.
         */
        void onError();
    }
}
