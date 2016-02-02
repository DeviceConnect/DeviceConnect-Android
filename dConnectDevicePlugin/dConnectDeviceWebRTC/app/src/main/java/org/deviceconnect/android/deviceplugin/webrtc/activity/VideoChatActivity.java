/*
 VideoChatActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.core.MediaConnection;
import org.deviceconnect.android.deviceplugin.webrtc.core.MediaStream;
import org.deviceconnect.android.deviceplugin.webrtc.core.Peer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerOption;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerUtil;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingUtil;
import org.deviceconnect.android.deviceplugin.webrtc.util.AudioUtils;
import org.deviceconnect.android.deviceplugin.webrtc.util.CameraUtils;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.VideoChatProfile;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.List;

/**
 * VideoChatActivity.
 * @author NTT DOCOMO, INC.
 */
public class VideoChatActivity extends Activity {
    /**
     * Tag for debugging.
     */
    private static final String TAG = "WEBRTC";

    /**
     * Defined a stun server.
     */
    private static final String STUN_SERVER = "stun:stun.skyway.io:3478";

    /**
     * Defined a extra config.
     * Constant Value: {@value}
     */
    public static final String EXTRA_CONFIG = "config";

    /**
     * Defined a extra address.
     * Constant Value: {@value}
     */
    public static final String EXTRA_ADDRESS_ID = "address_id";

    /**
     * Defined a extra video uri.
     * Constant Value: {@value}
     */
    public static final String EXTRA_VIDEO_URI = "video_uri";

    /**
     * Defined a extra audio uri.
     * Constant Value: {@value}
     */
    public static final String EXTRA_AUDIO_URI = "audio_uri";

    /**
     * Defined a extra offer.
     * Constant Value: {@value}
     */
    public static final String EXTRA_OFFER = "offer";

    private PeerConfig mConfig;
    private String mAddressId;
    private String mVideoUri;
    private String mAudioUri;
    private boolean mOffer;

    private PeerOption.VideoType mVideoType;
    private PeerOption.AudioType mAudioType;

    /**
     * Peer.
     */
    private Peer mPeer;

    /**
     * Connection of WebRTC.
     */
    private MediaConnection mConnection;

    /**
     * View for OpenGLES.
     */
    private GLSurfaceView mVideoView;

    /**
     * Renderer for remote video.
     */
    private VideoRenderer.Callbacks mPrimaryRender;

    /**
     * Renderer for myself video.
     */
    private VideoRenderer.Callbacks mSecondaryRender;

    /**
     * Option of peer's connection.
     */
    private PeerOption mOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVideoView = (GLSurfaceView) findViewById(R.id.glview_call);

        Intent intent = getIntent();
        if (intent != null) {
            mConfig = intent.getParcelableExtra(EXTRA_CONFIG);
            mAddressId = intent.getStringExtra(EXTRA_ADDRESS_ID);
            mVideoUri = intent.getStringExtra(EXTRA_VIDEO_URI);
            mAudioUri = intent.getStringExtra(EXTRA_AUDIO_URI);
            mOffer = intent.getBooleanExtra(EXTRA_OFFER, false);
            if ("true".equals(mVideoUri)) {
                mVideoType = PeerOption.VideoType.CAMERA;
            } else if ("false".equals(mVideoUri)) {
                mVideoType = PeerOption.VideoType.NONE;
            } else {
                mVideoType = PeerOption.VideoType.EXTERNAL_RESOURCE;
            }

            if ("true".equals(mAudioUri)) {
                mAudioType = PeerOption.AudioType.MICROPHONE;
            } else if ("false".equals(mAudioUri)) {
                mAudioType = PeerOption.AudioType.NONE;
            } else {
                mAudioType = PeerOption.AudioType.EXTERNAL_RESOURCE;
            }

            if (mConfig != null && mAddressId != null) {
                VideoRendererGui.setView(mVideoView, new Runnable() {
                    @Override
                    public void run() {
                        WebRTCApplication application = (WebRTCApplication) getApplication();
                        application.getPeer(mConfig, new WebRTCApplication.OnGetPeerCallback() {
                            @Override
                            public void onGetPeer(final Peer peer) {
                                if (peer != null) {
                                    mPeer = peer;
                                    startConnection();
                                } else {
                                    openWebRTCErrorDialog();
                                }
                            }
                        });
                    }
                });
                mPrimaryRender = VideoRendererGui.create(
                        0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);

                // If using a camera front camera , to reverse the image.
                mSecondaryRender = VideoRendererGui.create(
                        70, 70, 25, 25, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, isUseCamera());
                return;
            }
        }
        openWebRTCErrorDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mVideoView.onResume();
        if (mConnection != null) {
            mConnection.startVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mVideoView.onPause();
        if (mConnection != null) {
            mConnection.stopVideo();
        }
    }

    @Override
    protected void onDestroy() {
        hangup();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        } else {
            finish();
            return false;
        }
    }

    /**
     * Creates the PeerOption.
     */
    private boolean createOption() {

        int videoWidth = 480;
        int videoHeight = 640;
        int videoFps = 30;
        int videoFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

        switch (mVideoType) {
            default:
            case CAMERA: {
                String cameraText = SettingUtil.getCameraParam(this);
                CameraUtils.CameraFormat cameraFormat = CameraUtils.textToFormat(cameraText);
                if (cameraFormat == null) {
                    cameraFormat = CameraUtils.getDefaultFormat();
                }

                if (cameraFormat == null) {
                    openCameraErrorDialog();
                    return false;
                }

                videoWidth = cameraFormat.getWidth();
                videoHeight = cameraFormat.getHeight();
                videoFps = cameraFormat.getMaxFrameRate();
                videoFacing = cameraFormat.getFacing();
            }   break;
            case NONE:
            case EXTERNAL_RESOURCE:
                break;
        }

        String audioText = SettingUtil.getAudioParam(this);
        AudioUtils.AudioFormat audioFormat = AudioUtils.textToFormat(audioText);
        if (audioFormat == null) {
            audioFormat = AudioUtils.getDefaultFormat();
        }

        if (audioFormat == null) {
            openAudioErrorDialog();
            return false;
        }

        mOption = new PeerOption();
        mOption.setVideoWidth(videoWidth);
        mOption.setVideoHeight(videoHeight);
        mOption.setVideoFps(videoFps);
        mOption.setVideoFacing(videoFacing);
        mOption.setVideoType(mVideoType);
        mOption.setVideoUri(mVideoUri);
        mOption.setVideoRender(mSecondaryRender);
        mOption.setAudioType(mAudioType);
        mOption.setAudioUri(mAudioUri);
        mOption.setNoAudioProcessing(audioFormat.isNoAudioProcessing());
        mOption.addIceServer(STUN_SERVER);
        mOption.setContext(this);

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "option: " + mOption.toString());
        }

        return true;
    }

    /**
     * Starts the connection of WebRTC.
     */
    private void startConnection() {
        if (!createOption()) {
            return;
        }

        if (mOffer) {
            answer(mAddressId);
        } else {
            call(mAddressId);
        }
    }

    /**
     * Makes a phone call.
     * @param addressId address
     */
    private void call(final String addressId) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@ answer");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnection = mPeer.call(addressId, mOption, mOnMediaEventListener);
                if (mConnection == null) {
                    openWebRTCErrorDialog();
                }
            }
        });
    }

    /**
     * Takes a phone call.
     * @param addressId address
     */
    private void answer(final String addressId) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@ answer");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnection = mPeer.answer(addressId, mOption, mOnMediaEventListener);
                if (mConnection == null) {
                    openWebRTCErrorDialog();
                }
            }
        });
    }

    /**
     * Tests whether you use the device of camera.
     * @return true if you use device of camera, false otherwise
     */
    private boolean isUseCamera() {
        return mVideoType == PeerOption.VideoType.CAMERA;
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
                    mediaStream.setVideoRender(mPrimaryRender);
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
            finish();
        }

        @Override
        public void onError() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ MediaConnection.onError");
            }
        }
    };

    /**
     * Hang up a call.
     */
    private void hangup() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ VideoChatActivity::hangup");
        }

        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        }

        sendHangupEvent();
    }

    /**
     * Notifies call event.
     */
    private void sendCallEvent() {
        List<Event> events = EventManager.INSTANCE.getEventList(
                PeerUtil.getServiceId(mPeer),
                VideoChatProfile.PROFILE_NAME, null, VideoChatProfile.ATTR_ONCALL);
        if (events.size() != 0) {
            Bundle[] args = new Bundle[1];
            args[0] = new Bundle();
            args[0].putString(VideoChatProfile.PARAM_NAME, mAddressId);
            args[0].putString(VideoChatProfile.PARAM_ADDRESSID, mAddressId);
            // TODO: 出力先のURIを指定
//            args[0].putString(VideoChatProfile.PARAM_VIDEO, "XXX");
//            args[0].putString(VideoChatProfile.PARAM_AUDIO, "XXX");
            for (Event e : events) {
                Intent event = EventManager.createEventMessage(e);
                event.putExtra(VideoChatProfile.PARAM_ONCALL, args);
                sendBroadcast(event);
            }
        }
    }

    /**
     * Notifies hang up event.
     */
    private void sendHangupEvent() {
        List<Event> events = EventManager.INSTANCE.getEventList(
                PeerUtil.getServiceId(mPeer),
                VideoChatProfile.PROFILE_NAME, null, VideoChatProfile.ATTR_HANGUP);
        if (events.size() != 0) {
            Bundle arg = new Bundle();
            arg.putString(VideoChatProfile.PARAM_NAME, mAddressId);
            arg.putString(VideoChatProfile.PARAM_ADDRESSID, mAddressId);
            for (Event e : events) {
                Intent event = EventManager.createEventMessage(e);
                event.putExtra(VideoChatProfile.PARAM_HANGUP, arg);
                sendBroadcast(event);
            }
        }
    }

    /**
     * Open a error dialog of camera.
     */
    private void openCameraErrorDialog() {
        openErrorDialog(R.string.error_failed_to_connect_camera);
    }

    /**
     * Open a error dialog of audio.
     */
    private void openAudioErrorDialog() {
        openErrorDialog(R.string.error_failed_to_connect_audio);
    }

    /**
     * Open a error dialog of WebRTC.
     */
    private void openWebRTCErrorDialog() {
        openErrorDialog(R.string.error_failed_to_connect_p2p_msg);
    }

    /**
     * Open a error dialog.
     *
     * @param resId resource id
     */
    private void openErrorDialog(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoChatActivity.this);
                builder.setTitle(R.string.error_failed_to_connect_p2p_title);
                builder.setMessage(resId);
                builder.setPositiveButton(R.string.error_failed_to_connect_p2p_btn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
                dialog.show();
            }
        });
    }
}
