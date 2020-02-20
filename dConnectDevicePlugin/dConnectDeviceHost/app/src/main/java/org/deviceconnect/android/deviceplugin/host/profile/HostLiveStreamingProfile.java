package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
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
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.livestreaming.LiveStreamingClient;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;

public class HostLiveStreamingProfile extends DConnectProfile implements LiveStreamingClient.EventListener {

    private static final String TAG = "LiveStreamingProfile";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String KEY_BROADCAST_URI = "broadcast";
    private static final String KEY_VIDEO_URI = "video";
    private static final String KEY_AUDIO_URI = "audio";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_BITRATE = "bitrate";
    private static final String KEY_FRAMERATE = "framerate";
    private HostDeviceLiveStreamRecorder mHostDeviceLiveStreamRecorder;
    private HostMediaRecorderManager mHostMediaRecorderManager;

    public String getProfileName() {
        return "libstreaming";
    }

    public HostLiveStreamingProfile(final Context context, final HostMediaRecorderManager hostMediaRecorderManager) {
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
                final Bundle extras = request.getExtras();
                if (extras != null) {
                    //Broadcast URIの取得
                    final String serverUri = (String) extras.get(KEY_BROADCAST_URI);
                    if (serverUri == null) {
                        //無しは許容しない
                        MessageUtils.setInvalidRequestParameterError(response, "requested parameter not available");
                        return true;
                    }

                    if (DEBUG) {
                        Log.d(TAG, "serverUri : " + serverUri);
                    }

                    //既にHostDeviceLiveStreamRecorderを保持していれば停止する
                    if (mHostDeviceLiveStreamRecorder != null) {
                        mHostDeviceLiveStreamRecorder.liveStreamingStop();
                    }

                    //VideoリソースURIの取得
                    String videoUri = (String) extras.get(KEY_VIDEO_URI);
                    if (videoUri == null) {
                        videoUri = "false";
                    }

                    //VideoリソースURIからHostDeviceLiveStreamRecorderを取得する
                    try {
                        mHostDeviceLiveStreamRecorder = getHostDeviceLiveStreamRecorder(videoUri);
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

                    //Live Streamingクライアントの生成
                    mHostDeviceLiveStreamRecorder.createLiveStreamingClient(serverUri);

                    Integer width = (Integer) extras.get(KEY_WIDTH);
                    if (width == null) {
                        width = 0;
                    }
                    Integer height = (Integer) extras.get(KEY_HEIGHT);
                    if (height == null) {
                        height = 0;
                    }
                    Integer bitrate = (Integer) extras.get(KEY_BITRATE);
                    if (bitrate == null) {
                        bitrate = 0;
                    }
                    Integer framerate = (Integer) extras.get(KEY_FRAMERATE);
                    if (framerate == null) {
                        framerate = 0;
                    }
                    if (DEBUG) {
                        Log.d(TAG, "width : " + width);
                        Log.d(TAG, "height : " + height);
                        Log.d(TAG, "bitrate : " + bitrate);
                        Log.d(TAG, "framerate : " + framerate);
                    }

                    //Video無し以外の場合はパラメータをセット
                    if (!videoUri.equals("false")) {
                        mHostDeviceLiveStreamRecorder.setVideoParams(width, height, bitrate, framerate);
                    }

                    String audioUri = (String) extras.get(KEY_AUDIO_URI);
                    if (audioUri == null) {
                        audioUri = "false";
                    }
                    if (DEBUG) {
                        Log.d(TAG, "videoUri : " + videoUri);
                        Log.d(TAG, "audioUri : " + audioUri);
                    }
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
                setResult(response, DConnectMessage.RESULT_OK);
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
                setResult(response, DConnectMessage.RESULT_OK);
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
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    private HostDeviceLiveStreamRecorder getHostDeviceLiveStreamRecorder(final String videoURI) {
        String cameraId;
        switch(videoURI) {
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

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onError(MediaEncoderException mediaEncoderException) {

    }
}
