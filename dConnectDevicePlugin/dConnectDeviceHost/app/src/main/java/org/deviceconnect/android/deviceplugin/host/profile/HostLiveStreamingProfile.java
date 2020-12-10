package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceLiveStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.CameraVideoEncoder;
import org.deviceconnect.android.deviceplugin.host.recorder.screen.ScreenCastRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.screen.ScreenCastVideoEncoder;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.deviceplugin.host.recorder.util.LiveStreamingClient;
import org.deviceconnect.android.libmedia.streaming.video.CanvasVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

public class HostLiveStreamingProfile extends DConnectProfile implements LiveStreamingClient.EventListener {

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
    private HostDeviceLiveStreamRecorder mHostDeviceLiveStreamRecorder;
    private HostMediaRecorderManager mHostMediaRecorderManager;
    private String mVideoURI = null;
    private String mAudioURI = null;
    private Intent mCurrentResponse = null;
    public String getProfileName() {
        return "liveStreaming";
    }

    public HostLiveStreamingProfile(final HostMediaRecorderManager hostMediaRecorderManager) {
        final LiveStreamingClient.EventListener eventListener = this;
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

                //レコーダーがセット済みで配信中の場合エラーを返しておく
                if (mHostDeviceLiveStreamRecorder != null) {
                    if (mHostDeviceLiveStreamRecorder.isStreaming()) {
                        MessageUtils.setIllegalDeviceStateError(response, "status is normal(streaming)");
                        return true;
                    }
                }

                final Bundle extras = request.getExtras();
                if (extras != null) {
                    // リクエストパラメータチェック
                    final String broadcastURI = checkRequestParameter(extras, response);
                    if (broadcastURI == null) {
                        return true;
                    }
                    //映像リソースURIからレコーダーを取得する
                    try {
                        mHostDeviceLiveStreamRecorder = getHostDeviceLiveStreamRecorder();
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                        //例外(パラメータ)はエラー応答
                        MessageUtils.setInvalidRequestParameterError(response, ex.getMessage());
                        return true;
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                        //例外(実行時)はエラー応答(不明なエラーにしておく)
                        MessageUtils.setUnknownError(response, ex.getMessage());
                        return true;
                    }

                    ((HostMediaRecorder) mHostDeviceLiveStreamRecorder).requestPermission(new HostMediaRecorder.PermissionCallback() {
                        @Override
                        public void onAllowed() {
                            //クライアントの生成
                            mHostDeviceLiveStreamRecorder.createLiveStreamingClient(broadcastURI, eventListener);

                            //エンコーダーとパラメーターをセット
                            Integer width = parseInteger(request, PARAM_KEY_WIDTH);
                            Integer height = parseInteger(request, PARAM_KEY_HEIGHT);
                            Integer bitrate = parseInteger(request, PARAM_KEY_BITRATE);
                            Integer frameRate = parseInteger(request, PARAM_KEY_FRAME_RATE);
                            if (DEBUG) {
                                Log.d(TAG, "width : " + width);
                                Log.d(TAG, "height : " + height);
                                Log.d(TAG, "bitrate : " + bitrate);
                                Log.d(TAG, "frameRate : " + frameRate);
                            }
                            if (!mVideoURI.equals("false")) {
                                VideoEncoder encoder;
                                if (mVideoURI.equals(VIDEO_URI_SCREEN)) {
                                    ScreenCastRecorder sRecorder = (ScreenCastRecorder) mHostDeviceLiveStreamRecorder;
                                    HostMediaRecorder.Settings settings = sRecorder.getSettings();
                                    encoder = new ScreenCastVideoEncoder(sRecorder.getScreenCastMgr());
                                    // widthかheightがnullの場合は、PreviewSizeの最小値を設定する
                                    if (width == null || height == null) {
                                        HostMediaRecorder.Size pSize = settings.getSupportedPreviewSizes().get(0);
                                        width = pSize.getWidth();
                                        height = pSize.getHeight();
                                        for (int i = 1; i < settings.getSupportedPreviewSizes().size(); i++) {
                                            if (pSize.getWidth() < settings.getSupportedPreviewSizes().get(i).getWidth()) {
                                                width = settings.getSupportedPreviewSizes().get(i).getWidth();
                                                height = settings.getSupportedPreviewSizes().get(i).getHeight();
                                            }
                                        }
                                    }
                                } else {
                                    encoder = new CameraVideoEncoder((Camera2Recorder) mHostDeviceLiveStreamRecorder);
                                }

                                mHostDeviceLiveStreamRecorder.setVideoEncoder(encoder,
                                                                width, height, bitrate, frameRate);
                            } else {
                                mHostDeviceLiveStreamRecorder.setVideoEncoder(new CanvasVideoEncoder() {
                                    @Override
                                    public void draw(Canvas canvas, int width, int height) {
                                        canvas.drawColor(Color.BLACK);
                                    }
                                }, width, height, bitrate, frameRate);
                            }
                            //音声無し以外の場合はエンコーダーをセット
                            mHostDeviceLiveStreamRecorder.setAudioEncoder();
                            if (!mAudioURI.equals("false")) {
                                mHostDeviceLiveStreamRecorder.setMute(false);
                            }

                            //ストリーミング開始
                            mHostDeviceLiveStreamRecorder.startLiveStreaming();
                            mCurrentResponse = response;
                        }

                        @Override
                        public void onDisallowed() {
                            MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                            sendResponse(response);
                        }
                    });
                    return false;
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, "parameter not available");
                }
                return true;
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
                if (mHostDeviceLiveStreamRecorder != null) {
                    //配信中でない場合はエラーを返しておく
                    if (!mHostDeviceLiveStreamRecorder.isStreaming()) {
                        MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                        return true;
                    }
                    ((HostMediaRecorder) mHostDeviceLiveStreamRecorder)
                            .requestPermission(new HostMediaRecorder.PermissionCallback() {
                              @Override
                              public void onAllowed() {
                                  mHostDeviceLiveStreamRecorder.stopLiveStreaming();
                                  mCurrentResponse = response;
                              }

                              @Override
                              public void onDisallowed() {
                                  MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                                  sendResponse(response);
                              }
                          });
                    return false;
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                    return true;
                }
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
                if (mHostDeviceLiveStreamRecorder != null) {
                    Bundle streaming = new Bundle();
                    streaming.putString(PARAM_KEY_URI, mHostDeviceLiveStreamRecorder.getBroadcastURI());
                    if (mHostDeviceLiveStreamRecorder.isError()) {
                        streaming.putString(PARAM_KEY_STATUS, "error");
                    } else {
                        if (mHostDeviceLiveStreamRecorder.isStreaming()) {
                            streaming.putString(PARAM_KEY_STATUS, "streaming");
                        } else {
                            streaming.putString(PARAM_KEY_STATUS, "stop");
                        }
                    }

                    Bundle video = new Bundle();
                    video.putString(PARAM_KEY_URI, mVideoURI);
                    video.putInt(PARAM_KEY_WIDTH, mHostDeviceLiveStreamRecorder.getVideoWidth());
                    video.putInt(PARAM_KEY_HEIGHT, mHostDeviceLiveStreamRecorder.getVideoHeight());
                    video.putInt(PARAM_KEY_BITRATE, mHostDeviceLiveStreamRecorder.getBitrate());
                    video.putInt(PARAM_KEY_FRAME_RATE, mHostDeviceLiveStreamRecorder.getFrameRate());
                    video.putString(PARAM_KEY_MIME_TYPE, mHostDeviceLiveStreamRecorder.getLiveStreamingMimeType());
                    streaming.putParcelable(PARAM_KEY_VIDEO, video);

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

                if (mHostDeviceLiveStreamRecorder != null) {
                    ((HostMediaRecorder) mHostDeviceLiveStreamRecorder)
                            .requestPermission(new HostMediaRecorder.PermissionCallback() {
                                @Override
                                public void onAllowed() {
                                    mHostDeviceLiveStreamRecorder.setMute(true);
                                    setResult(response, DConnectMessage.RESULT_OK);
                                    sendResponse(response);
                                }

                                @Override
                                public void onDisallowed() {
                                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                                    sendResponse(response);
                                }
                            });
                    return false;
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                    return true;
                }
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
                if (mHostDeviceLiveStreamRecorder != null) {
                    ((HostMediaRecorder) mHostDeviceLiveStreamRecorder)
                            .requestPermission(new HostMediaRecorder.PermissionCallback() {
                                @Override
                                public void onAllowed() {
                                    mHostDeviceLiveStreamRecorder.setMute(false);
                                    setResult(response, DConnectMessage.RESULT_OK);
                                    sendResponse(response);
                                }

                                @Override
                                public void onDisallowed() {
                                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                                    sendResponse(response);
                                }
                            });
                    return false;
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                    return true;
                }
            }
        });

        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "mute";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (mHostDeviceLiveStreamRecorder != null) {
                    response.putExtra(PARAM_KEY_MUTE, mHostDeviceLiveStreamRecorder.isMute());
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "status is not normal(streaming)");
                }
                return true;
            }
        });
    }

    private HostDeviceLiveStreamRecorder getHostDeviceLiveStreamRecorder()  {
        if (DEBUG) {
            Log.d(TAG, "getHostDeviceLiveStreamRecorder()");
            Log.d(TAG, "mVideoURI : " + mVideoURI);
        }
        switch (mVideoURI) {
            case VIDEO_URI_TRUE:
            case VIDEO_URI_FALSE: {
                HostMediaRecorder hostMediaRecorder = mHostMediaRecorderManager.getRecorder(null);
                if (hostMediaRecorder != null) {
                    if (mHostMediaRecorderManager.usingStreamingRecorder()) {
                        throw new RuntimeException("Another target in using.");
                    }

                    if (hostMediaRecorder instanceof HostDeviceLiveStreamRecorder) {
                        return (HostDeviceLiveStreamRecorder) hostMediaRecorder;
                    }
                }
                break;
            }
            case VIDEO_URI_CAMERA_FRONT:
            case VIDEO_URI_CAMERA_1: {
                HostMediaRecorder hostMediaRecorder = mHostMediaRecorderManager.getRecorder(VIDEO_URI_CAMERA_1);
                if (hostMediaRecorder != null) {
                    if (mHostMediaRecorderManager.usingStreamingRecorder()) {
                        throw new RuntimeException("Another target in using.");
                    }
                    if (hostMediaRecorder instanceof HostDeviceLiveStreamRecorder) {
                        return (HostDeviceLiveStreamRecorder) hostMediaRecorder;
                    }
                }
                break;
            }
            case VIDEO_URI_CAMERA_BACK:
            case VIDEO_URI_CAMERA_0: {
                HostMediaRecorder hostMediaRecorder = mHostMediaRecorderManager.getRecorder(VIDEO_URI_CAMERA_0);
                if (hostMediaRecorder != null) {
                    if (mHostMediaRecorderManager.usingPreviewOrStreamingRecorder(hostMediaRecorder.getId())) {
                        throw new RuntimeException("Another target in using.");
                    }

                    if (hostMediaRecorder instanceof HostDeviceLiveStreamRecorder) {
                        return (HostDeviceLiveStreamRecorder) hostMediaRecorder;
                    }
                }
                break;
            }
            case VIDEO_URI_SCREEN: {
                HostMediaRecorder hostMediaRecorder = mHostMediaRecorderManager.getRecorder(VIDEO_URI_SCREEN);
                if (hostMediaRecorder != null) {

                    if (hostMediaRecorder instanceof HostDeviceLiveStreamRecorder) {
                        return (HostDeviceLiveStreamRecorder) hostMediaRecorder;
                    }
                }
                break;
            }
        }
        Log.e(TAG, "getHostDeviceLiveStreamRecorder() recorder not found");
        throw new RuntimeException("recorder not found");
    }

    private String checkRequestParameter(Bundle extras, Intent response) {
        //サーバURIの取得
        String broadcastURI = (String) extras.get(PARAM_KEY_BROADCAST);
        if (broadcastURI == null) {
            MessageUtils.setInvalidRequestParameterError(response, "broadcastURI is null");
            return null;
        }
        if (DEBUG) {
            Log.d(TAG, "broadcastURI : " + broadcastURI);
        }

        //映像リソースURIの取得
        mVideoURI = (String) extras.get(PARAM_KEY_VIDEO);
        if (mVideoURI == null) {  //パラメータが指定されていない場合はfalseとみなす
            mVideoURI = "false";
        } else {
            switch (mVideoURI) {
                case VIDEO_URI_TRUE:
                case VIDEO_URI_FALSE:
                case VIDEO_URI_CAMERA_FRONT:
                case VIDEO_URI_CAMERA_BACK:
                case VIDEO_URI_CAMERA_0:
                case VIDEO_URI_CAMERA_1:
                case VIDEO_URI_SCREEN:
                    break;
                default:
                    MessageUtils.setInvalidRequestParameterError(response, "video parameter illegal");
                    return null;
            }
        }

        if (DEBUG) {
            Log.d(TAG, "mVideoURI : " + mVideoURI);
        }
        //音声リソースURIの取得
        mAudioURI = (String) extras.get(PARAM_KEY_AUDIO);
        if (mAudioURI == null) {
            mAudioURI = "false";  //パラメータが指定されていない場合はfalseとみなす
        } else {
            switch (mAudioURI) {
                case AUDIO_URI_TRUE:
                case AUDIO_URI_FALSE:
                    break;
                default:
                    MessageUtils.setInvalidRequestParameterError(response, "audio parameter illegal");
                    return null;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "audioUri : " + mAudioURI);
        }
        if (mVideoURI.equals("false") && mAudioURI.equals("false")) {
            MessageUtils.setInvalidRequestParameterError(response, "Non-Supported video and audio are false. ");
            return null;
        }
        return broadcastURI;
    }
    @Override
    public void onStart() {
        if (DEBUG) {
            Log.d(TAG, "onStart()");
        }
        if (mCurrentResponse != null) {
            setResult(mCurrentResponse, DConnectMessage.RESULT_OK);
            sendResponse(mCurrentResponse);
            mCurrentResponse = null;
        }
        for(Event event : EventManager.INSTANCE.getEventList(getService().getId(), PROFILE_NAME, null, AT_ON_STATUS_CHANGE)) {
            Bundle root = new Bundle();
            Bundle streaming = new Bundle();
            streaming.putString(PARAM_KEY_URI, mHostDeviceLiveStreamRecorder.getBroadcastURI());
            streaming.putString(PARAM_KEY_STATUS, "normal");

            Bundle video = new Bundle();
            video.putString(PARAM_KEY_URI, mVideoURI);
            video.putInt(PARAM_KEY_WIDTH, mHostDeviceLiveStreamRecorder.getVideoWidth());
            video.putInt(PARAM_KEY_HEIGHT, mHostDeviceLiveStreamRecorder.getVideoHeight());
            video.putInt(PARAM_KEY_BITRATE, mHostDeviceLiveStreamRecorder.getBitrate());
            video.putInt(PARAM_KEY_FRAME_RATE, mHostDeviceLiveStreamRecorder.getFrameRate());
            video.putString(PARAM_KEY_MIME_TYPE, mHostDeviceLiveStreamRecorder.getLiveStreamingMimeType());
            streaming.putParcelable(PARAM_KEY_VIDEO, video);

            root.putParcelable(PARAM_KEY_STREAMING, streaming);

            sendEvent(event, root);
        }
    }

    @Override
    public void onStop() {
        if (DEBUG) {
            Log.d(TAG, "onStop()");
        }
        if (mCurrentResponse != null) {
            setResult(mCurrentResponse, DConnectMessage.RESULT_OK);
            sendResponse(mCurrentResponse);
            mCurrentResponse = null;
        }

        for(Event event : EventManager.INSTANCE.getEventList(getService().getId(), PROFILE_NAME, null, AT_ON_STATUS_CHANGE)) {
            Bundle root = new Bundle();
            Bundle streaming = new Bundle();
            streaming.putString(PARAM_KEY_URI, mHostDeviceLiveStreamRecorder.getBroadcastURI());
            streaming.putString(PARAM_KEY_STATUS, "stop");

            Bundle video = new Bundle();
            video.putString(PARAM_KEY_URI, mVideoURI);
            video.putInt(PARAM_KEY_WIDTH, mHostDeviceLiveStreamRecorder.getVideoWidth());
            video.putInt(PARAM_KEY_HEIGHT, mHostDeviceLiveStreamRecorder.getVideoHeight());
            video.putInt(PARAM_KEY_BITRATE, mHostDeviceLiveStreamRecorder.getBitrate());
            video.putInt(PARAM_KEY_FRAME_RATE, mHostDeviceLiveStreamRecorder.getFrameRate());
            video.putString(PARAM_KEY_MIME_TYPE, mHostDeviceLiveStreamRecorder.getLiveStreamingMimeType());
            streaming.putParcelable(PARAM_KEY_VIDEO, video);

            root.putParcelable(PARAM_KEY_STREAMING, streaming);

            sendEvent(event, root);
        }
    }

    @Override
    public void onError(MediaEncoderException mediaEncoderException) {
        if (DEBUG) {
            Log.d(TAG, "onError()");
        }
        if (mCurrentResponse != null) {
            MessageUtils.setIllegalServerStateError(mCurrentResponse, mediaEncoderException.getMessage());
            sendResponse(mCurrentResponse);
            mCurrentResponse = null;
        }

        for(Event event : EventManager.INSTANCE.getEventList(getService().getId(), PROFILE_NAME, null, AT_ON_STATUS_CHANGE)) {
            Bundle root = new Bundle();
            Bundle streaming = new Bundle();
            streaming.putString(PARAM_KEY_URI, mHostDeviceLiveStreamRecorder.getBroadcastURI());
            streaming.putString(PARAM_KEY_STATUS, "error");

            Bundle video = new Bundle();
            video.putString(PARAM_KEY_URI, mVideoURI);
            video.putInt(PARAM_KEY_WIDTH, mHostDeviceLiveStreamRecorder.getVideoWidth());
            video.putInt(PARAM_KEY_HEIGHT, mHostDeviceLiveStreamRecorder.getVideoHeight());
            video.putInt(PARAM_KEY_BITRATE, mHostDeviceLiveStreamRecorder.getBitrate());
            video.putInt(PARAM_KEY_FRAME_RATE, mHostDeviceLiveStreamRecorder.getFrameRate());
            video.putString(PARAM_KEY_MIME_TYPE, mHostDeviceLiveStreamRecorder.getLiveStreamingMimeType());
            streaming.putParcelable(PARAM_KEY_VIDEO, video);

            root.putParcelable(PARAM_KEY_STREAMING, streaming);

            sendEvent(event, root);
        }
    }
}
