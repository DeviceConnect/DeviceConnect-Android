/*
 WebRTCVideoChatProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.URLUtil;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCDeviceService;
import org.deviceconnect.android.deviceplugin.webrtc.activity.VideoChatActivity;
import org.deviceconnect.android.deviceplugin.webrtc.core.Address;
import org.deviceconnect.android.deviceplugin.webrtc.core.Peer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;
import org.deviceconnect.android.deviceplugin.webrtc.service.WebRTCService;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingUtil;
import org.deviceconnect.android.deviceplugin.webrtc.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.webrtc.util.WebRTCManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VideoChatProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * VideoChat Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCVideoChatProfile extends VideoChatProfile {

    /**
     * Tag for debugging.
     */
    private static final String TAG = "WEBRTC";

    private final DConnectApi mGetProfileApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTR_PROFILE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String configParam = request.getStringExtra(PARAM_CONFIG);

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@ onGetProfile");
                Log.i(TAG, "config: " + configParam);
            }

            PeerConfig config;
            try {
                config = new PeerConfig(configParam);
            } catch (IllegalArgumentException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
                MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
                return true;
            }

            WebRTCApplication application = getWebRTCApplication();
            application.getPeer(config, new WebRTCApplication.OnGetPeerCallback() {
                @Override
                public void onGetPeer(final Peer peer) {
                    if (peer != null) {
                        String deviceName = SettingUtil.getDeviceName(getContext());
                        if (deviceName == null || deviceName.equals("")) {
                            deviceName = "WebRTC Plugin";
                        }
                        response.putExtra(PARAM_NAME, deviceName);
                        response.putExtra(PARAM_ADDRESSID, peer.getMyAddressId());
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response);
                    }
                    getWebRTCService().sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mGetAddressApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTR_ADDRESS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String configParam = request.getStringExtra(PARAM_CONFIG);
            final String addressIdParam = request.getStringExtra(PARAM_ADDRESSID);

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@ onGetAddress");
                Log.i(TAG, "config: " + configParam);
            }

            PeerConfig config;
            try {
                config = new PeerConfig(configParam);
            } catch (IllegalArgumentException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
                MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
                return true;
            }

            WebRTCApplication application = getWebRTCApplication();
            application.getPeer(config, new WebRTCApplication.OnGetPeerCallback() {
                @Override
                public void onGetPeer(final Peer peer) {
                    if (peer != null) {
                        peer.getListPeerList(new Peer.OnGetAddressCallback() {
                            @Override
                            public void onGetAddresses(final List<Address> addressList) {
                                List<Bundle> list = new ArrayList<>();
                                for (int i = 0; i < addressList.size(); i++) {
                                    Address a = addressList.get(i);
                                    if (addressIdParam == null || addressIdParam.equals(a.getAddressId())) {
                                        Bundle address = new Bundle();
                                        address.putString(PARAM_NAME, a.getName());
                                        address.putString(PARAM_ADDRESSID, a.getAddressId());
                                        address.putString(PARAM_STATUS, a.getState().getValue());
                                        list.add(address);
                                    }
                                }
                                Bundle[] addresses = new Bundle[list.size()];
                                list.toArray(addresses);

                                response.putExtra(PARAM_ADDRESSES, addresses);

                                setResult(response, DConnectMessage.RESULT_OK);
                                getWebRTCService().sendResponse(response);
                            }
                        });
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response);
                        getWebRTCService().sendResponse(response);
                    }
                }
            });
            return false;
        }
    };

    private final DConnectApi mPostCallApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTR_CALL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String configParam = request.getStringExtra(PARAM_CONFIG);
            String groupIdParam = request.getStringExtra(PARAM_GROUPID);

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@ onPostCall");
            }

            PeerConfig config;
            try {
                config = new PeerConfig(configParam);
            } catch (IllegalArgumentException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
                MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
                return true;
            }

            if (groupIdParam != null) {
                MessageUtils.setInvalidRequestParameterError(response, "groupId is not supported.");
                return true;
            }

            final WebRTCApplication application = getWebRTCApplication();
            application.getPeer(config, new WebRTCApplication.OnGetPeerCallback() {
                @Override
                public void onGetPeer(final Peer peer) {
                    if (peer != null) {
                        peer.getListPeerList(new Peer.OnGetAddressCallback() {
                            @Override
                            public void onGetAddresses(final List<Address> addressList) {
                                String addressId = request.getStringExtra(PARAM_ADDRESSID);
                                String video = request.getStringExtra(PARAM_VIDEO);
                                String audio = request.getStringExtra(PARAM_AUDIO);
                                String outputs = request.getStringExtra(PARAM_OUTPUTS);
                                String audioSampleRate = request.getStringExtra(PARAM_AUDIOSAMPLERATE);
                                int audioSampleRateValue;
                                if (audioSampleRate == null || audioSampleRate.length() == 0) {
                                    audioSampleRateValue = PARAM_RATE_48000;
                                    audioSampleRate = String.valueOf(PARAM_RATE_48000);
                                } else {
                                    try {
                                        audioSampleRateValue = Integer.valueOf(audioSampleRate);
                                    } catch(NumberFormatException e) {
                                        // Characters that can not be converted to a number has been entered.
                                        audioSampleRateValue = 0;
                                    }
                                }
                                String audioBitDepth = request.getStringExtra(PARAM_AUDIOBITDEPTH);
                                if (audioBitDepth == null || audioBitDepth.length() == 0) {
                                    audioBitDepth = PARAM_PCM_FLOAT;
                                }
                                String audioChannel = request.getStringExtra(PARAM_AUDIOCHANNEL);
                                if (audioChannel == null || audioChannel.length() == 0) {
                                    audioChannel = PARAM_MONAURAL;
                                }

                                // if value is null, sets "true" as default
                                video = (video == null || video.isEmpty()) ? "true" : video;
                                audio = (audio == null || audio.isEmpty()) ? "true" : audio;
                                outputs = (outputs == null || outputs.isEmpty()) ? PARAM_HOST : outputs;

                                if (addressId == null || addressId.length() == 0) {
                                    MessageUtils.setInvalidRequestParameterError(response, "addressId is invalid.");
                                    getWebRTCService().sendResponse(response);
                                } else if (!containAddressId(addressList, addressId)) {
                                    MessageUtils.setInvalidRequestParameterError(response, "addressId is invalid.");
                                    getWebRTCService().sendResponse(response);
                                } else if (!checkUri(video)) {
                                    MessageUtils.setInvalidRequestParameterError(response, "video is invalid.");
                                    getWebRTCService().sendResponse(response);
                                } else if (!checkUri(audio)) {
                                    MessageUtils.setInvalidRequestParameterError(response, "audio is invalid.");
                                    getWebRTCService().sendResponse(response);
                                } else if (!checkAudioSampleRate(audioSampleRateValue)) {
                                    MessageUtils.setInvalidRequestParameterError(response, "audioSampleRate is invalid.");
                                    getWebRTCService().sendResponse(response);
                                } else if (!checkAudioBitDepth(audioBitDepth)) {
                                    MessageUtils.setInvalidRequestParameterError(response, "audioBitDepth is invalid.");
                                    getWebRTCService().sendResponse(response);
                                } else if (!checkAudioChannel(audioChannel)) {
                                    MessageUtils.setInvalidRequestParameterError(response, "audioChannel is invalid.");
                                    getWebRTCService().sendResponse(response);
                                } else {
                                    boolean offer = peer.hasOffer(addressId);
                                    final Intent intent = new Intent();
                                    intent.setClass(getContext(), VideoChatActivity.class);
                                    intent.putExtra(VideoChatActivity.EXTRA_ADDRESS_ID, addressId);
                                    intent.putExtra(VideoChatActivity.EXTRA_VIDEO_URI, video);
                                    intent.putExtra(VideoChatActivity.EXTRA_AUDIO_URI, audio);
                                    intent.putExtra(VideoChatActivity.EXTRA_CONFIG, peer.getConfig());
                                    intent.putExtra(VideoChatActivity.EXTRA_OFFER, offer);
                                    intent.putExtra(VideoChatActivity.EXTRA_AUDIOSAMPLERATE, audioSampleRate);
                                    intent.putExtra(VideoChatActivity.EXTRA_AUDIOBITDEPTH, audioBitDepth);
                                    intent.putExtra(VideoChatActivity.EXTRA_AUDIOCHANNEL, audioChannel);
                                    intent.putExtra(VideoChatActivity.EXTRA_CALL_TIMESTAMP, System.currentTimeMillis());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    final String output = outputs;
                                    init(new PermissionUtility.PermissionRequestCallback() {
                                        @Override
                                        public void onSuccess() {
                                            if (output.equals(PARAM_HOST)) {
                                                getContext().startActivity(intent);
                                                setResult(response, DConnectMessage.RESULT_OK);
                                            } else {
                                                CapabilityUtil.checkCapability(getContext(), new Handler(Looper.getMainLooper()), new CapabilityUtil.Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        WebRTCManager mgr = getWebRTCService().getWebRTCManager();
                                                        if (mgr.isConnect()) {
                                                            MessageUtils.setIllegalServerStateError(response, "Already the http server is running.");
                                                        } else {
                                                            mgr.connectOnUiThread(intent);
                                                            setResult(response, DConnectMessage.RESULT_OK);
                                                        }
                                                        getWebRTCService().sendResponse(response);
                                                    }

                                                    @Override
                                                    public void onFail() {
                                                        MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                                                        getWebRTCService().sendResponse(response);
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onFail(@NonNull String s) {
                                            MessageUtils.setUnknownError(response, s);
                                            getWebRTCService().sendResponse(response);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
                        getWebRTCService().sendResponse(response);
                    }
                }
            });
            return false;
        }
    };

    private final DConnectApi mPutProfileApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTR_PROFILE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String name = request.getStringExtra(PARAM_NAME);
            if (name == null || name.isEmpty()) {
                MessageUtils.setInvalidRequestParameterError(response, "name is invalid.");
                return true;
            }
            SettingUtil.setDeviceName(getContext(), name);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPutOnIncomingApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTR_ONINCOMING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerEvent(request, response);
        }
    };

    private final DConnectApi mPutOnCallApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTR_ONCALL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerEvent(request, response);
        }
    };

    private final DConnectApi mPutOnHangupApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTR_ONHANGUP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerEvent(request, response);
        }
    };

    private final DConnectApi mDeleteCallApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTR_CALL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String configParam = request.getStringExtra(PARAM_CONFIG);

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@ onDeleteCall");
                Log.i(TAG, "config:" + configParam);
            }

            PeerConfig config;
            try {
                config = new PeerConfig(configParam);
            } catch (IllegalArgumentException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "", e);
                }
                MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
                return true;
            }

            WebRTCApplication application = getWebRTCApplication();
            application.getPeer(config, new WebRTCApplication.OnGetPeerCallback() {
                @Override
                public void onGetPeer(final Peer peer) {
                    if (peer != null) {
                        String addressId = request.getStringExtra(PARAM_ADDRESSID);
                        if (peer.hangup(addressId)) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setInvalidRequestParameterError(response,
                                "address has not been call.");
                        }
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response);
                    }
                    getWebRTCService().sendResponse(response);
                }
            });

            return false;
        }
    };

    private final DConnectApi mDeleteOnIncomingApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTR_ONINCOMING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return unregisterEvent(request, response);
        }
    };

    private final DConnectApi mDeleteOnCallApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTR_ONCALL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return unregisterEvent(request, response);
        }
    };

    private final DConnectApi mDeleteOnHangupApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTR_ONHANGUP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return unregisterEvent(request, response);
        }
    };

    public WebRTCVideoChatProfile() {
        addApi(mGetProfileApi);
        addApi(mGetAddressApi);
        addApi(mPostCallApi);
        addApi(mPutProfileApi);
        addApi(mPutOnIncomingApi);
        addApi(mPutOnCallApi);
        addApi(mPutOnHangupApi);
        addApi(mDeleteCallApi);
        addApi(mDeleteOnIncomingApi);
        addApi(mDeleteOnCallApi);
        addApi(mDeleteOnHangupApi);
    }

    /**
     * Checks whether addressId is included in the addressList.
     * @param addressList address list
     * @param addressId address id
     * @return true if addressId is included in the addressList, false otherwise
     */
    private boolean containAddressId(final List<Address> addressList, final String addressId) {
        if (addressList != null) {
            for (Address address : addressList) {
                if (addressId.equals(address.getAddressId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Register the event.
     * @param request request
     * @param response response
     * @return true if returns a response immediately, false otherwise
     */
    private boolean registerEvent(final Intent request, final Intent response) {
        String configParam = request.getStringExtra(PARAM_CONFIG);
        PeerConfig config;
        try {
            config = new PeerConfig(configParam);
        } catch (IllegalArgumentException e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "", e);
            }
            MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
            return true;
        }

        WebRTCApplication application = getWebRTCApplication();
        application.getPeer(config, new WebRTCApplication.OnGetPeerCallback() {
            @Override
            public void onGetPeer(final Peer peer) {
                if (peer == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "peer not found.");
                } else {
                    EventError error = EventManager.INSTANCE.addEvent(request);
                    switch (error) {
                        case NONE:
                            setResult(response, DConnectMessage.RESULT_OK);
                            peer.setPeerEventListener(mListener);
                            break;
                        case INVALID_PARAMETER:
                            MessageUtils.setInvalidRequestParameterError(response);
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                }
                getWebRTCService().sendResponse(response);
            }
        });
        return false;
    }

    /**
     * Unregister the event.
     * @param request request
     * @param response response
     * @return true if returns a response immediately, false otherwise
     */
    private boolean unregisterEvent(final Intent request, final Intent response) {
        String configParam = request.getStringExtra(PARAM_CONFIG);
        PeerConfig config;
        try {
            config = new PeerConfig(configParam);
        } catch (IllegalArgumentException e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "", e);
            }
            MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
            return true;
        }

        WebRTCApplication application = getWebRTCApplication();
        application.getPeer(config, new WebRTCApplication.OnGetPeerCallback() {
            @Override
            public void onGetPeer(final Peer peer) {
                if (peer == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "peer not found.");
                } else {
                    EventError error = EventManager.INSTANCE.removeEvent(request);
                    switch (error) {
                        case NONE:
                            setResult(response, DConnectMessage.RESULT_OK);
                            break;
                        case INVALID_PARAMETER:
                            MessageUtils.setInvalidRequestParameterError(response);
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                }
                getWebRTCService().sendResponse(response);
            }
        });
        return false;
    }

    private WebRTCDeviceService getWebRTCService() {
        return (WebRTCDeviceService) getContext();
    }

    /**
     * Retrieve the instance of WebRTCApplication.
     * @return WebRTCApplication
     */
    private WebRTCApplication getWebRTCApplication() {
        WebRTCDeviceService service = getWebRTCService();
        return (WebRTCApplication) service.getApplication();
    }

    /**
     * Returns whether this uri is valid.
     * @param uri uri
     * @return {@code true} if this uri is valid, {@code false} otherwise.
     */
    private boolean checkUri(final String uri) {
        if (uri == null) {
            return false;
        } else if ("true".equals(uri) || "false".equals(uri)) {
            return true;
        } else if (uri.startsWith("ws://") || uri.startsWith("wss://")) {
            return true;
        } else {
            return URLUtil.isValidUrl(uri);
        }
    }

    /**
     * Check sample rate.
     * @param sampleRate sampleRate.
     * @return {@code true} if this samplerate is valid, {@code false} otherwise.
     */
    private boolean checkAudioSampleRate(final int sampleRate) {
        switch (sampleRate) {
            case PARAM_RATE_22050:
            case PARAM_RATE_32000:
            case PARAM_RATE_44100:
            case PARAM_RATE_48000:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check bit depth.
     * @param bitDepth bitDepth.
     * @return {@code true} if this bitdepth is valid, {@code false} otherwise.
     */
    private boolean checkAudioBitDepth(final String bitDepth) {
        if (bitDepth == null) {
            // Parameters not set.
            return true;
        }
        switch (bitDepth) {
            case PARAM_PCM_8BIT:
            case PARAM_PCM_16BIT:
            case PARAM_PCM_FLOAT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check channel.
     * @param channel channel.
     * @return {@code true} if this bitdepth is valid, {@code false} otherwise.
     */
    private boolean checkAudioChannel(final String channel) {
        if (channel == null) {
            // Parameters not set.
            return true;
        }
        switch (channel) {
            case PARAM_MONAURAL:
            case PARAM_STEREO:
                return true;
            default:
                return false;
        }
    }
    private void init(final PermissionUtility.PermissionRequestCallback callback) {
        CapabilityUtil.requestPermissions(getContext(), new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFail(final String deniedPermission) {
                callback.onFail(deniedPermission);
            }
        });
    }
    /**
     * This listener that receive events from Peer.
     */
    private Peer.PeerEventListener mListener = new Peer.PeerEventListener() {
        @Override
        public void onIncoming(final Peer peer, final Address address) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ run incoming event");
            }

            List<Event> events = EventManager.INSTANCE.getEventList(
                    WebRTCService.PLUGIN_ID,
                    PROFILE_NAME, null, ATTR_ONINCOMING);
            if (events.size() != 0) {
                Bundle arg = new Bundle();
                arg.putString(PARAM_NAME, address.getName());
                arg.putString(PARAM_ADDRESSID, address.getAddressId());
                for (Event e : events) {
                    Intent event = EventManager.createEventMessage(e);
                    event.putExtra(PARAM_INCOMING, arg);
                    DConnectMessageService s = (DConnectMessageService) getContext();
                    s.sendEvent(event, e.getAccessToken());
                }
            }
        }

        @Override
        public void onHangup(final Peer peer, final Address address) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ run onDisconnected event");
            }
        }

        @Override
        public void onCalling(final Peer peer, final Address address) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ run onCalling event");
            }
        }
    };
}
