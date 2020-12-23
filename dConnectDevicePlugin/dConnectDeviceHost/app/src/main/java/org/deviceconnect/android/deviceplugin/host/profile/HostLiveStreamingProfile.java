package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
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

    @Override
    public String getProfileName() {
        return "liveStreaming";
    }

    public HostLiveStreamingProfile(final HostMediaRecorderManager hostMediaRecorderManager) {
        mHostMediaRecorderManager = hostMediaRecorderManager;

        // POST /gotapi/liveStreaming/start
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

                if (mHostMediaRecorder != null && mHostMediaRecorder.isBroadcasterRunning()) {
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
                        settings.setPreviewSize(new Size(width, height));
                    }

                    if (bitrate != null) {
                        settings.setPreviewBitRate(bitrate);
                    }

                    if (frameRate != null) {
                        settings.setPreviewMaxFrameRate(frameRate);
                    }

                    // 映像が有効な時の音声設定を行う
                    if (!VIDEO_URI_FALSE.equals(video)) {
                        settings.setAudioEnabled(AUDIO_URI_TRUE.equals(audio));
                    }
                } catch (Exception e) {
                    MessageUtils.setInvalidRequestParameterError(response, "Parameter is invalid.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        Broadcaster broadcaster = recorder.startBroadcaster(broadcastURI);
                        if (broadcaster != null) {
                            broadcaster.setOnEventListener(new Broadcaster.OnEventListener() {
                                @Override
                                public void onStarted() {
                                }

                                @Override
                                public void onStopped() {
                                    postOnStop(broadcaster);
                                }

                                @Override
                                public void onError(Exception e) {
                                    postOnError(broadcaster);
                                }
                            });
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                            postOnStart(broadcaster);
                        } else {
                            MessageUtils.setUnknownError(response, "Failed to start a live streaming.");
                            sendResponse(response);
                        }
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

        // PUT /gotapi/liveStreaming/stop
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "stop";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (mHostMediaRecorder != null) {
                    mHostMediaRecorder.stopBroadcaster();
                    mHostMediaRecorder = null;
                }

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // GET /gotapi/liveStreaming/onStatusChange
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onStatusChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Bundle streaming;
                if (mHostMediaRecorder != null) {
                    BroadcasterProvider provider = mHostMediaRecorder.getBroadcasterProvider();
                    if (provider != null && provider.isRunning()) {
                        streaming = createStreamingBundle(provider.getBroadcaster(), "streaming");
                    } else {
                        streaming = createStreamingBundle(null, "stop");
                    }
                } else {
                    streaming = createStreamingBundle(null, "stop");
                }
                response.putExtra(PARAM_KEY_STREAMING, streaming);

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // PUT /gotapi/liveStreaming/onStatusChange
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

        // DELETE /gotapi/liveStreaming/onStatusChange
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

        // PUT /gotapi/liveStreaming/mute
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

                if (mHostMediaRecorder != null && mHostMediaRecorder.isBroadcasterRunning()) {
                    mHostMediaRecorder.setMute(true);
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                }

                return true;
            }
        });

        // DELETE /gotapi/liveStreaming/mute
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

                if (mHostMediaRecorder != null && mHostMediaRecorder.isBroadcasterRunning()) {
                    mHostMediaRecorder.setMute(false);
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                }

                return true;
            }
        });

        // GET /gotapi/liveStreaming/mute
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "mute";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (mHostMediaRecorder != null && mHostMediaRecorder.isBroadcasterRunning()) {
                    response.putExtra(PARAM_KEY_MUTE, mHostMediaRecorder.isMute());
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                }
                return true;
            }
        });
    }

    /**
     * 指定された video と audio から対応する HostMediaRecorder を取得します.
     *
     * 見つからない場合は null を返却します。
     *
     * @param video 映像のレコーダ識別子
     * @param audio 音声のレコーダ識別子
     * @return HostMediaRecorder のインスタンス
     */
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
        Bundle streaming = new Bundle();
        if (broadcaster != null) {
            streaming.putString(PARAM_KEY_URI, broadcaster.getBroadcastURI());
        }
        streaming.putString(PARAM_KEY_STATUS, status);

        if (broadcaster != null) {
            HostMediaRecorder.Settings settings = mHostMediaRecorder.getSettings();
            Bundle video = new Bundle();
            video.putString(PARAM_KEY_URI, mHostMediaRecorder.getId());
            video.putInt(PARAM_KEY_WIDTH, settings.getPreviewSize().getWidth());
            video.putInt(PARAM_KEY_HEIGHT, settings.getPreviewSize().getHeight());
            video.putInt(PARAM_KEY_BITRATE, settings.getPreviewBitRate());
            video.putInt(PARAM_KEY_FRAME_RATE, settings.getPreviewMaxFrameRate());
            video.putString(PARAM_KEY_MIME_TYPE, broadcaster.getMimeType());
            streaming.putParcelable(PARAM_KEY_VIDEO, video);
        }

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
