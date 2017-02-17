package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.service.WebRTCService;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingUtil;
import org.deviceconnect.android.deviceplugin.webrtc.util.AudioUtils;
import org.deviceconnect.android.deviceplugin.webrtc.util.CameraUtils;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.VideoChatProfile;
import org.webrtc.EglBase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebRTCController {
    /**
     * Tag for debugging.
     */
    private static final String TAG = "WEBRTC";

    /**
     * Defined a stun server.
     */
    private static final String STUN_SERVER = "stun:stun.skyway.io:3478";

    private MySurfaceViewRenderer mLocalRender;
    private MySurfaceViewRenderer mRemoteRender;
    private AudioTrackExternal mAudioTrackExternal;

    private WebRTCApplication mApplication;

    private PeerConfig mConfig;
    private PeerOption mOption;
    private Peer mPeer;

    private static final int DEFAULT_WIDTH = 240;
    private static final int DEFAULT_HEIGHT = 320;
    private static final int DEFAULT_FPS = 30;

    /**
     * Connection of WebRTC.
     */
    private MediaConnection mConnection;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private String mAddressId;
    private boolean mOffer;

    private WebRTCController(final WebRTCApplication app, final PeerConfig config, final PeerOption option,
                             final String addressId, final boolean offer, final WebRTCEventListener listener) {
        mApplication = app;
        mConfig = config;
        mOption = option;
        mWebRTCEventListener = listener;
        mAddressId = addressId;
        mOffer = offer;

        mApplication.getPeer(mConfig, new WebRTCApplication.OnGetPeerCallback() {
            @Override
            public void onGetPeer(final Peer peer) {
                if (peer != null) {
                    mPeer = peer;
                    if (mWebRTCEventListener != null) {
                        mWebRTCEventListener.onFoundPeer(WebRTCController.this);
                    }
                    if (mAddressId != null) {
                        if (mOffer) {
                            answer(mAddressId);
                        } else {
                            call(mAddressId);
                        }
                    }
                } else {
                    if (mWebRTCEventListener != null) {
                        mWebRTCEventListener.onNotFoundPeer(WebRTCController.this);
                    }
                }
            }
        });
    }

    private void setLocalRender(final MySurfaceViewRenderer localRender) {
        mLocalRender = localRender;
    }

    private void setRemoteRender(final MySurfaceViewRenderer remoteRender) {
        mRemoteRender = remoteRender;
    }

    private void setAudioTrackExternal(final AudioTrackExternal audioTrackExternal) {
        mAudioTrackExternal = audioTrackExternal;
    }

    private void runOnUiThread(final Runnable run) {
        mExecutorService.execute(run);
    }

    public Peer getPeer() {
        return mPeer;
    }

    public String getAddressId() {
        return mAddressId;
    }

    public MySurfaceViewRenderer getLocalRender() {
        return mLocalRender;
    }

    public MySurfaceViewRenderer getRemoteRender() {
        return mRemoteRender;
    }

    public void onPause() {
        if (mConnection != null) {
            mConnection.stopVideo();
        }
    }

    public void onResume() {
        if (mConnection != null) {
            mConnection.startVideo();
        }
    }

    /**
     * Makes a phone call.
     * @param addressId address
     */
    public void call(final String addressId) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@ answer");
        }

        if (mPeer == null) {
            throw new IllegalStateException("Peer is not set.");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnection = mPeer.call(addressId, mOption, mOnMediaEventListener);
                if (mConnection == null) {
                    if (mWebRTCEventListener != null) {
                        mWebRTCEventListener.onCallFailed(WebRTCController.this);
                    }
                }
            }
        });
    }

    /**
     * Takes a phone call.
     * @param addressId address
     */
    public void answer(final String addressId) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@ answer");
        }

        if (mPeer == null) {
            throw new IllegalStateException("Peer is not set.");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnection = mPeer.answer(addressId, mOption, mOnMediaEventListener);
                if (mConnection == null) {
                    if (mWebRTCEventListener != null) {
                        mWebRTCEventListener.onAnswerFailed(WebRTCController.this);
                    }
                }
            }
        });
    }

    /**
     * Hang up a call.
     */
    public void hangup() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ VideoChatActivity::hangup");
        }

        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        }

        mRemoteRender.release();
        mLocalRender.release();

        EglBase eglBase = mOption.getEglBase();
        if (eglBase != null) {
            eglBase.release();
        }
    }

    private MediaConnection.OnMediaEventListener mOnMediaEventListener
            = new MediaConnection.OnMediaEventListener() {
        @Override
        public void onAddStream(final MediaStream mediaStream) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ MediaConnection.onAddStream");
            }

            sendCallEvent();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mediaStream.setVideoRender(mRemoteRender);
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream mediaStream) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ MediaConnection.onRemoveStream");
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mediaStream.setVideoRender(null);
                }
            });
        }

        @Override
        public void onClose() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ MediaConnection.onClose");
            }

            sendHangupEvent();
        }

        @Override
        public void onError() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ MediaConnection.onError");
            }

            if (mWebRTCEventListener != null) {
                mWebRTCEventListener.onError(WebRTCController.this);
            }
        }
    };

    /**
     * Notifies call event.
     */
    private void sendCallEvent() {
        final String LOCALHOST = "localhost";
        List<Event> events = EventManager.INSTANCE.getEventList(
                WebRTCService.PLUGIN_ID,
                VideoChatProfile.PROFILE_NAME, null, VideoChatProfile.ATTR_ONCALL);
        if (events.size() != 0) {
            Bundle local = new Bundle();
            Bundle remote = new Bundle();
            Bundle videoParam = new Bundle();
            Bundle audioParam = new Bundle();
            String ipAddr = getIPAddress(mApplication.getApplicationContext());

            // Set local parameter
            String uri = mLocalRender.getUrl();
            if (uri == null) {
                uri = "";
            } else if (uri.contains(LOCALHOST)) {
                uri = uri.replaceAll(LOCALHOST, ipAddr);
            }
            videoParam.putString(VideoChatProfile.PARAM_URI, uri);
            String mimeType = mLocalRender.getMimeType();
            if (mimeType == null) {
                mimeType = "";
            }
            videoParam.putString(VideoChatProfile.PARAM_MIMETYPE, mimeType);
            videoParam.putInt(VideoChatProfile.PARAM_FRAMERATE, DEFAULT_FPS);
            videoParam.putInt(VideoChatProfile.PARAM_WIDTH, mLocalRender.getFrameWidth());
            videoParam.putInt(VideoChatProfile.PARAM_HEIGHT, mLocalRender.getFrameHeight());

            local.putBundle(VideoChatProfile.PARAM_VIDEO, (Bundle) videoParam.clone());

            // Set remote parameter
            uri = mRemoteRender.getUrl();
            if (uri == null) {
                uri = "";
            } else if (uri.contains(LOCALHOST)) {
                uri = uri.replaceAll(LOCALHOST, ipAddr);
            }
            videoParam.putString(VideoChatProfile.PARAM_URI, uri);
            mimeType = mLocalRender.getMimeType();
            if (mimeType == null) {
                mimeType = "";
            }
            videoParam.putString(VideoChatProfile.PARAM_MIMETYPE, mimeType);
            videoParam.putInt(VideoChatProfile.PARAM_FRAMERATE, 30);
            videoParam.putInt(VideoChatProfile.PARAM_WIDTH, mRemoteRender.getFrameWidth());
            videoParam.putInt(VideoChatProfile.PARAM_HEIGHT, mRemoteRender.getFrameHeight());
            remote.putBundle(VideoChatProfile.PARAM_VIDEO, (Bundle) videoParam.clone());

            if (mAudioTrackExternal != null) {
                uri = mAudioTrackExternal.getUrl();
                if (uri == null) {
                    uri = "";
                } else if (uri.contains(LOCALHOST)) {
                    uri = uri.replaceAll(LOCALHOST, ipAddr);
                }
                audioParam.putString(VideoChatProfile.PARAM_URI, uri);
                audioParam.putString(VideoChatProfile.PARAM_MIMETYPE, mAudioTrackExternal.getMimeType());
                audioParam.putInt(VideoChatProfile.PARAM_SAMPLERATE, mAudioTrackExternal.getSampleRate());
                audioParam.putInt(VideoChatProfile.PARAM_CHANNELS, mAudioTrackExternal.getChannels());
                audioParam.putInt(VideoChatProfile.PARAM_SAMPLESIZE, mAudioTrackExternal.getSampleSize());
                audioParam.putInt(VideoChatProfile.PARAM_BLOCKSIZE, mAudioTrackExternal.getBlockSize());
                remote.putBundle(VideoChatProfile.PARAM_AUDIO, (Bundle) audioParam.clone());
            }

            Bundle[] args = new Bundle[1];
            args[0] = new Bundle();
            args[0].putString(VideoChatProfile.PARAM_NAME, getAddressId());
            args[0].putString(VideoChatProfile.PARAM_ADDRESSID, getAddressId());
            args[0].putBundle(VideoChatProfile.PARAM_REMOTE, remote);
            args[0].putBundle(VideoChatProfile.PARAM_LOCAL, local);

            for (Event e : events) {
                Intent event = EventManager.createEventMessage(e);
                event.putExtra(VideoChatProfile.PARAM_ONCALL, args);
                mApplication.sendBroadcast(event);
            }
        }

        if (mWebRTCEventListener != null) {
            mWebRTCEventListener.onConnected(WebRTCController.this);
        }
    }

    /**
     * Notifies hang up event.
     */
    private void sendHangupEvent() {
        List<Event> events = EventManager.INSTANCE.getEventList(
                WebRTCService.PLUGIN_ID,
                VideoChatProfile.PROFILE_NAME, null, VideoChatProfile.ATTR_ONHANGUP);
        if (events.size() != 0) {
            Bundle arg = new Bundle();
            arg.putString(VideoChatProfile.PARAM_NAME, getAddressId());
            arg.putString(VideoChatProfile.PARAM_ADDRESSID, getAddressId());
            for (Event e : events) {
                Intent event = EventManager.createEventMessage(e);
                event.putExtra(VideoChatProfile.PARAM_HANGUP, arg);
                mApplication.sendBroadcast(event);
            }
        }

        if (mWebRTCEventListener != null) {
            mWebRTCEventListener.onDisconnected(WebRTCController.this);
        }
    }

    private WebRTCEventListener mWebRTCEventListener;

    public interface WebRTCEventListener {
        void onFoundPeer(WebRTCController controller);
        void onNotFoundPeer(WebRTCController controller);
        void onCallFailed(WebRTCController controller);
        void onAnswerFailed(WebRTCController controller);
        void onConnected(WebRTCController controller);
        void onDisconnected(WebRTCController controller);
        void onError(WebRTCController controller);
    }

    public static String getIPAddress(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    public static class Builder {
        private int mVideoWidth = DEFAULT_WIDTH;
        private int mVideoHeight = DEFAULT_HEIGHT;
        private int mVideoFps = DEFAULT_FPS;
        private int mVideoFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        private boolean mIsLandscape = false;

        private PeerOption.VideoType mVideoType = PeerOption.VideoType.CAMERA;
        private PeerOption.AudioType mAudioType;

        private Context mContext;
        private EglBase mEglBase;
        private String mVideoUri;
        private String mAudioUri;

        private MySurfaceViewRenderer mLocalRender;
        private MySurfaceViewRenderer mRemoteRender;
        private AudioTrackExternal mAudioTrackExternal;

        private PeerConfig mConfig;

        private WebRTCApplication mApplication;
        private WebRTCEventListener mWebRTCEventListener;

        private String mAddressId;
        private boolean mOffer;
        private PeerOption.AudioSampleRate mAudioSampleRate = PeerOption.AudioSampleRate.RATE_48000;
        private PeerOption.AudioBitDepth mAudioBitDepth = PeerOption.AudioBitDepth.PCM_FLOAT;
        private PeerOption.AudioChannel mAudioChannel = PeerOption.AudioChannel.MONAURAL;

        public Builder setVideoWidth(final int width) {
            mVideoWidth = width;
            return this;
        }

        public Builder setVideoHeight(final int height) {
            mVideoHeight = height;
            return this;
        }

        public Builder setVideoFps(final int fps) {
            mVideoFps = fps;
            return this;
        }

        public Builder setVideoFacing(final int facing) {
            mVideoFacing = facing;
            return this;
        }

        public void setAddressId(final String addressId) {
            mAddressId = addressId;
        }

        public void setOffer(final boolean offer) {
            mOffer = offer;
        }

        public void setAudioSampleRate(final int audioSampleRate) {
            mAudioSampleRate = PeerOption.AudioSampleRate.valueOf(audioSampleRate);
        }

        public void setAudioBitDepth(final String audioBitDepth) {
            switch (audioBitDepth) {
                case VideoChatProfile.PARAM_PCM_8BIT:
                    mAudioBitDepth = PeerOption.AudioBitDepth.PCM_8BIT;
                    break;
                case VideoChatProfile.PARAM_PCM_16BIT:
                    mAudioBitDepth = PeerOption.AudioBitDepth.PCM_16BIT;
                    break;
                case VideoChatProfile.PARAM_PCM_FLOAT:
                default:
                    mAudioBitDepth = PeerOption.AudioBitDepth.PCM_FLOAT;
                    break;
            }
        }

        public void setAudioChannel(final String audioChannel) {
            switch (audioChannel) {
                case VideoChatProfile.PARAM_STEREO:
                    mAudioChannel = PeerOption.AudioChannel.STEREO;
                    break;
                case VideoChatProfile.PARAM_MONAURAL:
                default:
                    mAudioChannel = PeerOption.AudioChannel.MONAURAL;
                    break;
            }
        }

        public Builder setVideoUri(final String uri) {
            mVideoUri = uri;
            if ("true".equals(uri)) {
                mVideoType = PeerOption.VideoType.CAMERA;
            } else if ("false".equals(uri)) {
                mVideoType = PeerOption.VideoType.NONE;
            } else {
                mVideoType = PeerOption.VideoType.EXTERNAL_RESOURCE;
            }
            return this;
        }

        public Builder setAudioUri(final String uri) {
            mAudioUri = uri;
            if ("true".equals(uri)) {
                mAudioType = PeerOption.AudioType.MICROPHONE;
            } else if ("false".equals(uri)) {
                mAudioType = PeerOption.AudioType.NONE;
            } else {
                mAudioType = PeerOption.AudioType.EXTERNAL_RESOURCE;
            }
            return this;
        }

        public Builder setLocalRender(final MySurfaceViewRenderer render) {
            mLocalRender = render;
            return this;
        }

        public Builder setRemoteRender(final MySurfaceViewRenderer render) {
            mRemoteRender = render;
            return this;
        }

        public Builder setAudioTrackExternal(final AudioTrackExternal audioTrackExternal) {
            mAudioTrackExternal = audioTrackExternal;
            return this;
        }

        public Builder setConfig(final PeerConfig config) {
            mConfig = config;
            return this;
        }

        public Builder setContext(final Context context) {
            mContext = context;
            return this;
        }

        public Builder setApplication(final WebRTCApplication application) {
            mApplication = application;
            return this;
        }

        public Builder setEglBase(final EglBase eglBase) {
            mEglBase = eglBase;
            return this;
        }

        public Builder setLandscape(final boolean isLandscape) {
            mIsLandscape = isLandscape;
            return this;
        }

        public Builder setWebRTCEventListener(final WebRTCEventListener listener) {
            mWebRTCEventListener = listener;
            return this;
        }

        public WebRTCController create() {
            if (mContext == null) {
                throw new IllegalArgumentException("Context is not set.");
            }

            if (mConfig == null) {
                throw new IllegalArgumentException("PeerConfig is not set.");
            }

            if (mApplication == null) {
                throw new IllegalArgumentException("Application is not set.");
            }

            if (mEglBase == null) {
                throw new IllegalArgumentException("EglBase is not set.");
            }

            if (mLocalRender == null) {
                throw new IllegalArgumentException("localRender is not set.");
            }

            if (mRemoteRender == null) {
                throw new IllegalArgumentException("remoteRender is not set.");
            }

            if (mAddressId == null) {
                throw new IllegalArgumentException("addressId is not set.");
            }

            if (mIsLandscape) {
                int tmp = mVideoWidth;
                mVideoWidth = mVideoHeight;
                mVideoHeight = tmp;
            }

            switch (mVideoType) {
                default:
                case CAMERA: {
                    String cameraText = SettingUtil.getCameraParam(mContext);
                    CameraUtils.CameraFormat cameraFormat = CameraUtils.textToFormat(cameraText);
                    if (cameraFormat == null) {
                        cameraFormat = CameraUtils.getDefaultFormat(mVideoWidth, mVideoHeight);
                    }
                    mVideoWidth = cameraFormat.getWidth();
                    mVideoHeight = cameraFormat.getHeight();
                    mVideoFps = cameraFormat.getMaxFrameRate();
                    mVideoFacing = cameraFormat.getFacing();
                }   break;
                case NONE:
                case EXTERNAL_RESOURCE:
                    break;
            }

            String audioText = SettingUtil.getAudioParam(mContext);
            AudioUtils.AudioFormat audioFormat = AudioUtils.textToFormat(audioText);
            if (audioFormat == null) {
                audioFormat = AudioUtils.getDefaultFormat();
            }

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "mVideoUri: " + mVideoUri);
                Log.i(TAG, "mVideoWidth: " + mVideoWidth);
                Log.i(TAG, "mVideoHeight: " + mVideoHeight);
                Log.i(TAG, "mVideoFps: " + mVideoFps);
                Log.i(TAG, "mVideoFacing: " + mVideoFacing);
                Log.i(TAG, "mVideoType: " + mVideoType);
                Log.i(TAG, "mAudioUri: " + mAudioUri);
                Log.i(TAG, "mAudioSampleRate: " + mAudioSampleRate);
                Log.i(TAG, "mAudioBitDepth: " + mAudioBitDepth);
                Log.i(TAG, "mAudioChannel: " + mAudioChannel);
            }

            PeerOption option = new PeerOption();
            option.setVideoWidth(mVideoWidth);
            option.setVideoHeight(mVideoHeight);
            option.setVideoFps(mVideoFps);
            option.setVideoFacing(mVideoFacing);
            option.setVideoType(mVideoType);
            option.setVideoUri(mVideoUri);
            option.setVideoRender(mLocalRender);
            option.setAudioType(mAudioType);
            option.setAudioUri(mAudioUri);
            option.setNoAudioProcessing(audioFormat.isNoAudioProcessing());
            option.addIceServer(STUN_SERVER);
            option.setContext(mContext);
            option.setEglBase(mEglBase);
            option.setAudioSampleRate(mAudioSampleRate);
            option.setAudioBitDepth(mAudioBitDepth);
            option.setAudioChannel(mAudioChannel);

            WebRTCController ctl = new WebRTCController(mApplication, mConfig, option,
                    mAddressId, mOffer, mWebRTCEventListener);
            ctl.setLocalRender(mLocalRender);
            ctl.setRemoteRender(mRemoteRender);
            ctl.setAudioTrackExternal(mAudioTrackExternal);

            return ctl;
        }
    }
}
