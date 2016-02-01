/*
 WebRTCVideoChatProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCDeviceService;
import org.deviceconnect.android.deviceplugin.webrtc.activity.VideoChatActivity;
import org.deviceconnect.android.deviceplugin.webrtc.core.Address;
import org.deviceconnect.android.deviceplugin.webrtc.core.Peer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerUtil;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VideoChatProfile;
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

    @Override
    protected boolean onGetProfile(final Intent request, final Intent response) {
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
                ((WebRTCDeviceService) getContext()).sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onGetAddress(final Intent request, final Intent response) {
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
                            ((WebRTCDeviceService) getContext()).sendResponse(response);
                        }
                    });
                } else {
                    MessageUtils.setInvalidRequestParameterError(response);
                    ((WebRTCDeviceService) getContext()).sendResponse(response);
                }
            }
        });
        return false;
    }

    @Override
    protected boolean onPostCall(final Intent request, final Intent response) {
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

        WebRTCApplication application = getWebRTCApplication();
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

                            // if value is null, sets "true" as default
                            video = (video == null || video.isEmpty()) ? "true" : video;
                            audio = (audio == null || audio.isEmpty()) ? "true" : audio;

                            if (addressId == null || addressId.length() == 0) {
                                MessageUtils.setInvalidRequestParameterError(response, "addressId is invalid.");
                            } else if (!containAddressId(addressList, addressId)) {
                                MessageUtils.setInvalidRequestParameterError(response, "addressId is invalid.");
                            } else if (!checkUri(video)) {
                                MessageUtils.setInvalidRequestParameterError(response, "video is invalid.");
                            } else if (!checkUri(audio)) {
                                MessageUtils.setInvalidRequestParameterError(response, "audio is invalid.");
                            } else {
                                boolean offer = peer.hasOffer(addressId);
                                final Intent intent = new Intent();
                                intent.setClass(getContext(), VideoChatActivity.class);
                                intent.putExtra(VideoChatActivity.EXTRA_ADDRESS_ID, addressId);
                                intent.putExtra(VideoChatActivity.EXTRA_VIDEO_URI, video);
                                intent.putExtra(VideoChatActivity.EXTRA_AUDIO_URI, audio);
                                intent.putExtra(VideoChatActivity.EXTRA_CONFIG, peer.getConfig());
                                intent.putExtra(VideoChatActivity.EXTRA_OFFER, offer);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(intent);
                                setResult(response, DConnectMessage.RESULT_OK);
                            }
                            ((WebRTCDeviceService) getContext()).sendResponse(response);
                        }
                    });
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, "config is invalid.");
                    ((WebRTCDeviceService) getContext()).sendResponse(response);
                }
            }
        });
        return false;
    }

    @Override
    protected boolean onPutProfile(final Intent request, final Intent response) {
        String name = request.getStringExtra(PARAM_NAME);
        if (name == null || name.isEmpty()) {
            MessageUtils.setInvalidRequestParameterError(response, "name is invalid.");
            return true;
        }
        SettingUtil.setDeviceName(getContext(), name);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutIncoming(final Intent request, final Intent response) {
        return registerEvent(request, response);
    }

    @Override
    protected boolean onPutOnCall(final Intent request, final Intent response) {
        return registerEvent(request, response);
    }

    @Override
    protected boolean onPutHangup(final Intent request, final Intent response) {
        return registerEvent(request, response);

    }

    @Override
    protected boolean onDeleteCall(final Intent request, final Intent response) {
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
                ((WebRTCDeviceService) getContext()).sendResponse(response);
            }
        });

        return false;
    }

    @Override
    protected boolean onDeleteIncoming(final Intent request, final Intent response) {
        return unregisterEvent(request, response);
    }

    @Override
    protected boolean onDeleteOnCall(final Intent request, final Intent response) {
        return unregisterEvent(request, response);
    }

    @Override
    protected boolean onDeleteHangup(final Intent request, final Intent response) {
        return unregisterEvent(request, response);
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
                    request.putExtra(PARAM_SERVICE_ID, PeerUtil.getServiceId(peer));
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
                ((WebRTCDeviceService) getContext()).sendResponse(response);
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
                    request.putExtra(PARAM_SERVICE_ID, PeerUtil.getServiceId(peer));
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
                ((WebRTCDeviceService) getContext()).sendResponse(response);
            }
        });
        return false;
    }

    /**
     * Retrieve the instance of WebRTCApplication.
     * @return WebRTCApplication
     */
    private WebRTCApplication getWebRTCApplication() {
        WebRTCDeviceService service = (WebRTCDeviceService) getContext();
        WebRTCApplication application = (WebRTCApplication) service.getApplication();
        return application;
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
        } else {
            return URLUtil.isValidUrl(uri);
        }
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
                    PeerUtil.getServiceId(peer),
                    PROFILE_NAME, null, ATTR_INCOMING);
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
                Log.i(TAG, "@@@ run onHangup event");
            }

            List<Event> events = EventManager.INSTANCE.getEventList(
                    PeerUtil.getServiceId(peer),
                    PROFILE_NAME, null, ATTR_HANGUP);
            if (events.size() != 0) {
                Bundle arg = new Bundle();
                arg.putString(PARAM_NAME, address.getName());
                arg.putString(PARAM_ADDRESSID, address.getAddressId());
                for (Event e : events) {
                    Intent event = EventManager.createEventMessage(e);
                    event.putExtra(PARAM_HANGUP, arg);
                    DConnectMessageService s = (DConnectMessageService) getContext();
                    s.sendEvent(event, e.getAccessToken());
                }
            }
        }

        @Override
        public void onCalling(final Peer peer, final Address address) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ run onCalling event");
            }

            List<Event> events = EventManager.INSTANCE.getEventList(
                    PeerUtil.getServiceId(peer),
                    PROFILE_NAME, null, ATTR_ONCALL);
            if (events.size() != 0) {
                Bundle[] args = new Bundle[1];
                args[0] = new Bundle();
                args[0].putString(PARAM_NAME, address.getName());
                args[0].putString(PARAM_ADDRESSID, address.getAddressId());
                // TODO video and audio
//                args[0].putString(PARAM_VIDEO, "XXX");
//                args[0].putString(PARAM_AUDIO, "XXX");
                for (Event e : events) {
                    Intent event = EventManager.createEventMessage(e);
                    event.putExtra(PARAM_ONCALL, args);
                    DConnectMessageService s = (DConnectMessageService) getContext();
                    s.sendEvent(event, e.getAccessToken());
                }
            }
        }
    };
}
