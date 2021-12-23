package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.LiveStreaming;
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

import java.util.ArrayList;
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
    private static final String AUDIO_URI_TRUE = "true";
    private static final String AUDIO_URI_FALSE = "false";

    private final HostMediaRecorderManager mHostMediaRecorderManager;
    private HostMediaRecorder mHostMediaRecorder;

    private final HostMediaRecorderManager.OnEventListener mOnEventListener = new HostMediaRecorderManager.OnEventListener() {
        @Override
        public void onMuteChanged(HostMediaRecorder recorder, boolean mute) {
        }

        @Override
        public void onConfigChanged(HostMediaRecorder recorder) {
        }

        @Override
        public void onPreviewStarted(HostMediaRecorder recorder, List<LiveStreaming> servers) {
        }

        @Override
        public void onPreviewStopped(HostMediaRecorder recorder) {
        }

        @Override
        public void onPreviewError(HostMediaRecorder recorder, Exception e) {
        }

        @Override
        public void onBroadcasterStarted(HostMediaRecorder recorder, List<LiveStreaming> broadcasters) {
            if (mHostMediaRecorder == null) {
                mHostMediaRecorder = recorder;
            }
            postOnStart(broadcasters);
        }

        @Override
        public void onBroadcasterStopped(HostMediaRecorder recorder) {
            postOnStop(recorder.getBroadcasterProvider().getLiveStreamingList());
            mHostMediaRecorder = null;
        }

        @Override
        public void onBroadcasterError(HostMediaRecorder recorder, LiveStreaming broadcaster, Exception e) {
            postOnError(broadcaster);
        }

        @Override
        public void onTakePhoto(HostMediaRecorder recorder, String uri, String filePath, String mimeType) {
        }

        @Override
        public void onRecordingStarted(HostMediaRecorder recorder, String fileName) {
        }

        @Override
        public void onRecordingPause(HostMediaRecorder recorder) {
        }

        @Override
        public void onRecordingResume(HostMediaRecorder recorder) {
        }

        @Override
        public void onRecordingStopped(HostMediaRecorder recorder, String fileName) {
        }

        @Override
        public void onFoundRecorder(HostMediaRecorder recorder) {
        }

        @Override
        public void onLostRecorder(HostMediaRecorder recorder) {
        }

        @Override
        public void onError(HostMediaRecorder recorder, Exception e) {
        }
    };

    @Override
    public String getProfileName() {
        return "liveStreaming";
    }

    public HostLiveStreamingProfile(final HostMediaRecorderManager hostMediaRecorderManager) {
        mHostMediaRecorderManager = hostMediaRecorderManager;
        mHostMediaRecorderManager.addOnEventListener(mOnEventListener);

        // POST /gotapi/liveStreaming/start
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "start";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
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
                    if (broadcastURI != null) {
                        for (String encoderId : recorder.getSettings().getEncoderIdList()) {
                            HostMediaRecorder.EncoderSettings encoderSettings = recorder.getSettings().getEncoderSetting(encoderId);
                            if (encoderSettings != null && encoderSettings.getMimeType().equals(HostMediaRecorder.MimeType.RTMP)) {
                                encoderSettings.setBroadcastURI(broadcastURI);
                            }
                        }
                    }
                    
                    if (width != null && height != null) {
                        settings.setPreviewSize(new Size(width, height));

                        for (String encoderId : recorder.getSettings().getEncoderIdList()) {
                            HostMediaRecorder.EncoderSettings encoderSettings = recorder.getSettings().getEncoderSetting(encoderId);
                            if (encoderSettings != null && encoderSettings.getMimeType().equals(HostMediaRecorder.MimeType.RTMP)) {
                                encoderSettings.setPreviewSize(new Size(width, height));
                            }
                        }
                    }

                    if (bitrate != null) {
                        for (String encoderId : recorder.getSettings().getEncoderIdList()) {
                            HostMediaRecorder.EncoderSettings encoderSettings = recorder.getSettings().getEncoderSetting(encoderId);
                            if (encoderSettings != null && encoderSettings.getMimeType().equals(HostMediaRecorder.MimeType.RTMP)) {
                                encoderSettings.setPreviewBitRate(bitrate);
                            }
                        }
                    }

                    if (frameRate != null) {
                        Range<Integer> fps = recorder.getSettings().getPreviewFpsFromFrameRate(frameRate);
                        if (fps != null) {
                            settings.setPreviewFps(fps);
                        }

                        for (String encoderId : recorder.getSettings().getEncoderIdList()) {
                            HostMediaRecorder.EncoderSettings encoderSettings = recorder.getSettings().getEncoderSetting(encoderId);
                            if (encoderSettings != null && encoderSettings.getMimeType().equals(HostMediaRecorder.MimeType.RTMP)) {
                                encoderSettings.setPreviewMaxFrameRate(frameRate);
                            }
                        }
                    }

                    settings.setPreviewAudioSource(getAudioSource(audio));
                } catch (Exception e) {
                    MessageUtils.setInvalidRequestParameterError(response, "Parameter is invalid.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        mHostMediaRecorder = recorder;

                        List<LiveStreaming> broadcasters = recorder.startBroadcaster(broadcastURI);
                        if (!broadcasters.isEmpty() ) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setUnknownError(response, "Failed to start a live streaming.");
                        }
                        sendResponse(response);
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
                } else {
                    // LiveStream プロファイル以外で開始された場合には、
                    // mHostMediaRecorder は null なので、全てのレコーダの配信を停止する。
                    HostMediaRecorder[] recorders = mHostMediaRecorderManager.getRecorders();
                    if (recorders != null) {
                        for (HostMediaRecorder recorder : recorders) {
                            recorder.stopBroadcaster();
                        }
                    }
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
                List<Bundle> streaming = new ArrayList<>();
                if (mHostMediaRecorder != null) {
                    BroadcasterProvider provider = mHostMediaRecorder.getBroadcasterProvider();
                    for (LiveStreaming broadcaster : provider.getLiveStreamingList()) {
                        streaming.add(createStreamingBundle(broadcaster, broadcaster.isRunning() ? "streaming" : "stop"));
                    }
                } else {
                    streaming.add(createStreamingBundle(null, "stop"));
                }
                response.putExtra(PARAM_KEY_STREAMING, streaming.toArray(new Bundle[0]));

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
        return mHostMediaRecorderManager.getRecorder("audio");
    }

    private HostMediaRecorder.AudioSource getAudioSource(String audio) {
        if (AUDIO_URI_FALSE.equals(audio)) {
            return null;
        } else if (AUDIO_URI_TRUE.equals(audio)) {
            return HostMediaRecorder.AudioSource.DEFAULT;
        } else {
            return HostMediaRecorder.AudioSource.typeOf(audio);
        }
    }

    private Bundle createStreamingBundle(LiveStreaming broadcaster, String status) {
        Bundle streaming = new Bundle();
        if (broadcaster != null) {
            streaming.putString(PARAM_KEY_URI, broadcaster.getUri());
        }
        streaming.putString(PARAM_KEY_STATUS, status);

        if (broadcaster != null && mHostMediaRecorder != null) {
            HostMediaRecorder.Settings settings = mHostMediaRecorder.getSettings();
            Bundle video = new Bundle();
            video.putString(PARAM_KEY_URI, mHostMediaRecorder.getId());
            video.putInt(PARAM_KEY_WIDTH, settings.getPreviewSize().getWidth());
            video.putInt(PARAM_KEY_HEIGHT, settings.getPreviewSize().getHeight());
//            video.putInt(PARAM_KEY_BITRATE, settings.getPreviewBitRate());
//            video.putInt(PARAM_KEY_FRAME_RATE, settings.getPreviewMaxFrameRate());
            video.putString(PARAM_KEY_MIME_TYPE, broadcaster.getMimeType());
            streaming.putParcelable(PARAM_KEY_VIDEO, video);
        }
        return streaming;
    }

    private void postOnStart(List<LiveStreaming> broadcasters) {
        List<Event> evtList = EventManager.INSTANCE.getEventList(getService().getId(),
                PROFILE_NAME, null, AT_ON_STATUS_CHANGE);

        List<Bundle> b = new ArrayList<>();
        for (LiveStreaming broadcaster : broadcasters) {
            b.add(createStreamingBundle(broadcaster, "normal"));
        }

        for (Event event : evtList) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra(PARAM_KEY_STREAMING, b.toArray(new Bundle[0]));
            sendEvent(intent, event.getAccessToken());
        }
    }

    private void postOnStop(List<LiveStreaming> broadcasters) {
        List<Event> evtList = EventManager.INSTANCE.getEventList(getService().getId(),
                PROFILE_NAME, null, AT_ON_STATUS_CHANGE);

        List<Bundle> b = new ArrayList<>();
        for (LiveStreaming broadcaster : broadcasters) {
            b.add(createStreamingBundle(broadcaster, "stop"));
        }

        for (Event event : evtList) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra(PARAM_KEY_STREAMING, b.toArray(new Bundle[0]));
            sendEvent(intent, event.getAccessToken());
        }
    }

    private void postOnError(LiveStreaming broadcaster) {
        List<Event> evtList = EventManager.INSTANCE.getEventList(getService().getId(),
                PROFILE_NAME, null, AT_ON_STATUS_CHANGE);

        for (Event event : evtList) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra(PARAM_KEY_STREAMING, createStreamingBundle(broadcaster, "error"));
            sendEvent(intent, event.getAccessToken());
        }
    }
}
