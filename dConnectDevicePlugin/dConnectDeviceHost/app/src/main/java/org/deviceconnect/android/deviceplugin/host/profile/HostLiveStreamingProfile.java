package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.BuildConfig;
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

public class HostLiveStreamingProfile extends DConnectProfile implements LiveStreamingClient.EventListener {

    private static final String TAG = "LiveStreamingProfile";
    private static final boolean DEBUG = BuildConfig.DEBUG;
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
    private HostDeviceLiveStreamRecorder mHostDeviceLiveStreamRecorder;
    private HostMediaRecorderManager mHostMediaRecorderManager;
    private String mVideoURI = null;
    private String mAudioURI = null;

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
                    //サーバURIの取得
                    final String broadcastURI = (String) extras.get(PARAM_KEY_BROADCAST);
                    if (broadcastURI == null) {
                        //無しは許容しない
                        MessageUtils.setInvalidRequestParameterError(response, "requested parameter not available");
                        return true;
                    }

                    if (DEBUG) {
                        Log.d(TAG, "broadcastURI : " + broadcastURI);
                    }

                    //映像リソースURIの取得
                    mVideoURI = (String) extras.get(PARAM_KEY_VIDEO);
                    if (mVideoURI == null) {
                        mVideoURI = "false";
                    }
                    if (DEBUG) {
                        Log.d(TAG, "mVideoURI : " + mVideoURI);
                    }

                    //映像リソースURIからレコーダーを取得する
                    try {
                        mHostDeviceLiveStreamRecorder = getHostDeviceLiveStreamRecorder();
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                        //例外(パラメータ)はエラー応答
                        MessageUtils.setInvalidRequestParameterError(response, ex.getMessage());
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                        //例外(実行時)はエラー応答(不明なエラーにしておく)
                        MessageUtils.setUnknownError(response, ex.getMessage());
                        return true;
                    }

                    //クライアントの生成
                    mHostDeviceLiveStreamRecorder.createLiveStreamingClient(broadcastURI, eventListener);

                    //映像無し以外の場合はエンコーダーとパラメーターをセット
                    if (!mVideoURI.equals("false")) {
                        Integer width = (Integer) extras.get(PARAM_KEY_WIDTH);
                        if (width == null) {
                            width = 0;
                        }
                        Integer height = (Integer) extras.get(PARAM_KEY_HEIGHT);
                        if (height == null) {
                            height = 0;
                        }
                        Integer bitrate = (Integer) extras.get(PARAM_KEY_BITRATE);
                        if (bitrate == null) {
                            bitrate = 0;
                        }
                        Integer framerate = (Integer) extras.get(PARAM_KEY_FRAME_RATE);
                        if (framerate == null) {
                            framerate = 0;
                        }
                        if (DEBUG) {
                            Log.d(TAG, "width : " + width);
                            Log.d(TAG, "height : " + height);
                            Log.d(TAG, "bitrate : " + bitrate);
                            Log.d(TAG, "framerate : " + framerate);
                        }
                        mHostDeviceLiveStreamRecorder.setVideoEncoder(width, height, bitrate, framerate);
                    }

                    //音声リソースURIの取得
                    mAudioURI = (String) extras.get(PARAM_KEY_AUDIO);
                    if (mAudioURI == null) {
                        mAudioURI = "false";
                    }
                    if (DEBUG) {
                        Log.d(TAG, "audioUri : " + mAudioURI);
                    }

                    //音声無し以外の場合はエンコーダーをセット
                    if (!mAudioURI.equals("false")) {
                        mHostDeviceLiveStreamRecorder.setAudioEncoder();
                    }

                    //ストリーミング開始
                    mHostDeviceLiveStreamRecorder.liveStreamingStart();

                    setResult(response, DConnectMessage.RESULT_OK);
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
                    mHostDeviceLiveStreamRecorder.liveStreamingStop();
                }
                setResult(response, DConnectMessage.RESULT_OK);
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
                    mHostDeviceLiveStreamRecorder.setMute(true);
                }
                setResult(response, DConnectMessage.RESULT_OK);
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
                if (mHostDeviceLiveStreamRecorder != null) {
                    mHostDeviceLiveStreamRecorder.setMute(false);
                }
                setResult(response, DConnectMessage.RESULT_OK);
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
                if (mHostDeviceLiveStreamRecorder != null) {
                    response.putExtra(PARAM_KEY_MUTE, mHostDeviceLiveStreamRecorder.isMute());
                }
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    private HostDeviceLiveStreamRecorder getHostDeviceLiveStreamRecorder() {
        String cameraId;
        switch(mVideoURI) {
            //Video無しの場合は取り敢えずdefaultを指定しておく
            case "true":
            case "false":
                cameraId = null;
                break;
            case "front-camera":
                cameraId = getCameraId(CameraCharacteristics.LENS_FACING_FRONT);
                break;
            case "back-camera":
                cameraId = getCameraId(CameraCharacteristics.LENS_FACING_BACK);
                break;
            default:
                Log.e(TAG, "getHostDeviceLiveStreamRecorder() unexpected videoURI");
                throw new IllegalArgumentException("video uri unexpected");
        }
        HostMediaRecorder hostMediaRecorder = mHostMediaRecorderManager.getRecorder(cameraId);
        if (hostMediaRecorder instanceof HostDeviceLiveStreamRecorder) {
            return (HostDeviceLiveStreamRecorder)hostMediaRecorder;
        } else {
            Log.e(TAG, "getHostDeviceLiveStreamRecorder() recorder not found");
            throw new RuntimeException("recorder not found");
        }
    }

    private String getCameraId(int facing) {
        CameraManager cameraManager = (CameraManager)getContext().getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager != null) {
            try {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing != null && lensFacing == facing) {
                        return cameraId;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onStart() {
        for(Event event : EventManager.INSTANCE.getEventList(AT_ON_STATUS_CHANGE)) {
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
        for(Event event : EventManager.INSTANCE.getEventList(AT_ON_STATUS_CHANGE)) {
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
        for(Event event : EventManager.INSTANCE.getEventList(AT_ON_STATUS_CHANGE)) {
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
