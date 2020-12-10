package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceLiveStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.deviceplugin.host.recorder.util.LiveStreamingClient;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class HostLiveStreamingProfile extends DConnectProfile {

    private static final String TAG = "LiveStreamingProfile";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String PROFILE_NAME = "liveStreaming";
    private static final String AT_ON_STATUS_CHANGE = "onStatusChange";
    private static final String PARAM_KEY_BROADCAST = "broadcast";
    private static final String PARAM_KEY_VIDEO = "video";
    private static final String PARAM_KEY_AUDIO = "audio";
    private static final String PARAM_KEY_WIDTH = "width";
    private static final String PARAM_KEY_HEIGHT = "height";
    private static final String PARAM_KEY_BITRATE = "bitrate";
    private static final String PARAM_KEY_FRAME_RATE = "frameRate";
    private static final String PARAM_KEY_STREAMING = "streaming";
    private static final String PARAM_KEY_URI = "uri";
    private static final String PARAM_KEY_STATUS = "status";
    private static final String PARAM_KEY_MIME_TYPE = "mimeType";
    private static final String PARAM_KEY_MUTE = "mute";
    private static final String VIDEO_URI_TRUE = "true";
    private static final String VIDEO_URI_FALSE = "false";
    private static final String VIDEO_URI_CAMERA_FRONT = "camera-front";
    private static final String VIDEO_URI_CAMERA_BACK = "camera-back";
    private static final String VIDEO_URI_CAMERA_0 = "camera_0";
    private static final String VIDEO_URI_CAMERA_1 = "camera_1";
    private static final String VIDEO_URI_SCREEN = "screen";
    private static final String AUDIO_URI_TRUE = "true";
    private static final String AUDIO_URI_FALSE = "false";
    private static final int CAMERA_TYPE_FRONT = 0;
    private static final int CAMERA_TYPE_BACK = 1;
    private HostMediaRecorderManager mHostMediaRecorderManager;
    private HostMediaRecorder mHostMediaRecorder;
    public String getProfileName() {
        return "liveStreaming";
    }

    public HostLiveStreamingProfile(final HostMediaRecorderManager hostMediaRecorderManager) {
        mHostMediaRecorderManager = hostMediaRecorderManager;

        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "start";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (DEBUG) {
                    Log.d(TAG, "onRequest() : post /start");
                }

                if (mHostMediaRecorder != null) {
                    MessageUtils.setIllegalDeviceStateError(response, "LiveStreaming is already running.");
                    return true;
                }

                String broadcastURI = request.getStringExtra(PARAM_KEY_BROADCAST);
                if (broadcastURI == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "broadcastURI is null");
                    return true;
                }
                if (DEBUG) {
                    Log.d(TAG, "broadcastURI : " + broadcastURI);
                }

                String video = request.getStringExtra(PARAM_KEY_VIDEO);
                String audio = request.getStringExtra(PARAM_KEY_AUDIO);
                Integer width = parseInteger(request, PARAM_KEY_WIDTH);
                Integer height = parseInteger(request, PARAM_KEY_HEIGHT);
                Integer bitrate = parseInteger(request, PARAM_KEY_BITRATE);
                Integer frameRate = parseInteger(request, PARAM_KEY_FRAME_RATE);

                HostMediaRecorder recorder = getHostMediaRecorder(video, audio);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "recorder is not found.");
                    return true;
                }

                HostMediaRecorder.Settings settings = recorder.getSettings();
                try {
                    if (width != null && height != null) {
                        settings.setPreviewSize(new HostMediaRecorder.Size(width, height));
                    }

                    if (bitrate != null) {
                        settings.setPreviewBitRate(bitrate);
                    }

                    if (frameRate != null) {
                        settings.setPreviewMaxFrameRate(frameRate);
                    }

                    if (!VIDEO_URI_FALSE.equals(video)) {
                        settings.setAudioEnabled(VIDEO_URI_TRUE.equals(audio));
                    }
                } catch (Exception e) {
                    MessageUtils.setInvalidRequestParameterError(response, "Parameter is invalid.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        String testURI = "rtmp://192.168.11.7:1935/live/abc";
                        recorder.startBroadcaster(testURI, new HostMediaRecorder.OnBroadcasterListener() {
                            @Override
                            public void onStarted(Broadcaster broadcaster) {
                                mHostMediaRecorder = recorder;
                                sendResponse(response);
                            }

                            @Override
                            public void onStopped(Broadcaster broadcaster) {
                            }

                            @Override
                            public void onError(Broadcaster broadcaster, Exception e) {
                                MessageUtils.setUnknownError(response, "e" + e.toString());
                                sendResponse(response);
                            }
                        });
                    }

                    @Override
                    public void onDisallowed() {
                        MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                        sendResponse(response);
                    }
                });

                return false;
            }
        });

        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "stop";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (DEBUG) {
                    Log.d(TAG, "onRequest() : put /stop");
                }

                if (mHostMediaRecorder != null) {
                    mHostMediaRecorder.stopBroadcaster();
                    mHostMediaRecorder = null;
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                }
                return true;
            }
        });

        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onStatusChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (DEBUG) {
                    Log.d(TAG, "onRequest() : get /onStatusChange");
                }

                if (mHostMediaRecorder != null) {
                    Bundle streaming = createStreamingBundle(mHostMediaRecorder.getBroadcaster(), "streaming");
                    response.putExtra(PARAM_KEY_STREAMING, streaming);
                }

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onStatusChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.addEvent(request);
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
                return true;
            }
        });

        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onStatusChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
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
                return true;
            }
        });

        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "mute";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (DEBUG) {
                    Log.d(TAG, "onRequest() : put /mute");
                }

                if (mHostMediaRecorder != null) {
                }

//                if (mHostDeviceLiveStreamRecorder != null) {
//                    ((HostMediaRecorder) mHostDeviceLiveStreamRecorder)
//                            .requestPermission(new HostMediaRecorder.PermissionCallback() {
//                                @Override
//                                public void onAllowed() {
//                                    mHostDeviceLiveStreamRecorder.setMute(true);
//                                    setResult(response, DConnectMessage.RESULT_OK);
//                                    sendResponse(response);
//                                }
//
//                                @Override
//                                public void onDisallowed() {
//                                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
//                                    sendResponse(response);
//                                }
//                            });
//                    return false;
//                } else {
//                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
//                    return true;
//                }
                return true;
            }
        });

        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "mute";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (DEBUG) {
                    Log.d(TAG, "onRequest() : delete /mute");
                }

//                if (mHostDeviceLiveStreamRecorder != null) {
//                    ((HostMediaRecorder) mHostDeviceLiveStreamRecorder)
//                            .requestPermission(new HostMediaRecorder.PermissionCallback() {
//                                @Override
//                                public void onAllowed() {
//                                    mHostDeviceLiveStreamRecorder.setMute(false);
//                                    setResult(response, DConnectMessage.RESULT_OK);
//                                    sendResponse(response);
//                                }
//
//                                @Override
//                                public void onDisallowed() {
//                                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
//                                    sendResponse(response);
//                                }
//                            });
//                    return false;
//                } else {
//                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
//                    return true;
//                }
                return true;
            }
        });

        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "mute";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
//                if (mHostDeviceLiveStreamRecorder != null) {
//                    response.putExtra(PARAM_KEY_MUTE, mHostDeviceLiveStreamRecorder.isMute());
//                    setResult(response, DConnectMessage.RESULT_OK);
//                } else {
//                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
//                }
                return true;
            }
        });
    }

    private HostMediaRecorder getHostMediaRecorder(String video, String audio) {
        if (video == null && audio == null) {
            return null;
        }
        if (video != null) {
            switch (video) {
                case VIDEO_URI_FALSE:
                    break;
                case VIDEO_URI_TRUE:
                    return mHostMediaRecorderManager.getRecorder(null);
                default:
                    return mHostMediaRecorderManager.getRecorder(video);
            }
        }
        return mHostMediaRecorderManager.getRecorder(audio);
    }

    private Bundle createStreamingBundle(Broadcaster broadcaster, String status) {
        HostMediaRecorder.Settings settings = mHostMediaRecorder.getSettings();

        Bundle streaming = new Bundle();
        streaming.putString(PARAM_KEY_URI, broadcaster.getBroadcastURI());
        streaming.putString(PARAM_KEY_STATUS, status);

        Bundle video = new Bundle();
        video.putString(PARAM_KEY_URI, mHostMediaRecorder.getId());
        video.putInt(PARAM_KEY_WIDTH, settings.getPreviewSize().getWidth());
        video.putInt(PARAM_KEY_HEIGHT, settings.getPreviewSize().getHeight());
        video.putInt(PARAM_KEY_BITRATE, settings.getPreviewBitRate());
        video.putInt(PARAM_KEY_FRAME_RATE, (int) settings.getPreviewMaxFrameRate());
        video.putString(PARAM_KEY_MIME_TYPE, broadcaster.getMimeType());
        streaming.putParcelable(PARAM_KEY_VIDEO, video);

        return streaming;
    }

    private void postOnStart(Broadcaster broadcaster) {
        if (mHostMediaRecorder == null) {
            return;
        }

        List<Event> evtList = EventManager.INSTANCE.getEventList(getService().getId(),
                PROFILE_NAME, null, AT_ON_STATUS_CHANGE);

        for (Event event : evtList) {
            Bundle root = new Bundle();
            root.putParcelable(PARAM_KEY_STREAMING, createStreamingBundle(broadcaster, "normal"));
            sendEvent(event, root);
        }
    }

    private void postOnStop(Broadcaster broadcaster) {
        if (mHostMediaRecorder == null) {
            return;
        }

        List<Event> evtList = EventManager.INSTANCE.getEventList(getService().getId(),
                PROFILE_NAME, null, AT_ON_STATUS_CHANGE);

        for (Event event : evtList) {
            Bundle root = new Bundle();
            root.putParcelable(PARAM_KEY_STREAMING, createStreamingBundle(broadcaster, "stop"));
            sendEvent(event, root);
        }
    }

    private void postOnError(Broadcaster broadcaster) {
        if (mHostMediaRecorder == null) {
            return;
        }

        List<Event> evtList = EventManager.INSTANCE.getEventList(getService().getId(),
                PROFILE_NAME, null, AT_ON_STATUS_CHANGE);

        for (Event event : evtList) {
            Bundle root = new Bundle();
            root.putParcelable(PARAM_KEY_STREAMING, createStreamingBundle(broadcaster, "error"));
            sendEvent(event, root);
        }
    }
}
