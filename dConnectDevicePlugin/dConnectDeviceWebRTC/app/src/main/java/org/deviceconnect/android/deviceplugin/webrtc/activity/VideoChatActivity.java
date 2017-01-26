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
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.core.MySurfaceViewRenderer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;
import org.deviceconnect.android.deviceplugin.webrtc.core.WebRTCController;
import org.deviceconnect.android.deviceplugin.webrtc.fragment.PercentFrameLayout;
import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCVideoChatProfile;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.voiceengine.WebRtcAudioTrack;

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

    /**
     * Defined a extra audioSampleRate.
     * Constant Value: {@value}
     */
    public static final String EXTRA_AUDIOSAMPLERATE = "audioSampleRate";

    /**
     * Defined a extra audioBitDepth.
     * Constant Value: {@value}
     */
    public static final String EXTRA_AUDIOBITDEPTH = "audioBitDepth";

    /**
     * Defined a extra audioChannel.
     * Constant Value: {@value}
     */
    public static final String EXTRA_AUDIOCHANNEL = "audioChannel";

    /**
     * Defined a extra callTimeStamp.
     * Constant Value: {@value}
     */
    public static final String EXTRA_CALL_TIMESTAMP = "callTimeStamp";

    private PercentFrameLayout mLocalLayout;
    private PercentFrameLayout mRemoteLayout;

    private MySurfaceViewRenderer mLocalRender;
    private MySurfaceViewRenderer mRemoteRender;

    private WebRTCController mWebRTCController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLocalLayout = (PercentFrameLayout) findViewById(R.id.local_view_layout);
        mRemoteLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);

        mLocalRender = (MySurfaceViewRenderer) findViewById(R.id.local_video_view);
        mLocalRender.setType(MySurfaceViewRenderer.TYPE_LOCAL);
        mRemoteRender = (MySurfaceViewRenderer) findViewById(R.id.remote_video_view);
        mRemoteRender.setType(MySurfaceViewRenderer.TYPE_REMOTE);

        EglBase eglBase = EglBase.create();
        mRemoteRender.init(eglBase.getEglBaseContext(), null);
        mLocalRender.init(eglBase.getEglBaseContext(), null);
        mLocalRender.setZOrderMediaOverlay(true);

        WebRtcAudioTrack.setAudioTrackModuleFactory(null);

        Intent intent = getIntent();
        if (intent != null) {
            long prevTimeStamp = ((WebRTCApplication) getApplication()).getCallTimeStamp();
            long callTimeStamp = intent.getLongExtra(EXTRA_CALL_TIMESTAMP, 0);
            if (prevTimeStamp == callTimeStamp) {
                createWebRTCErrorDialog();
                return;
            }
            ((WebRTCApplication) getApplication()).setCallTimeStamp(callTimeStamp);

            PeerConfig config = intent.getParcelableExtra(EXTRA_CONFIG);
            String videoUri = intent.getStringExtra(EXTRA_VIDEO_URI);
            String audioUri = intent.getStringExtra(EXTRA_AUDIO_URI);
            String addressId = intent.getStringExtra(EXTRA_ADDRESS_ID);
            boolean offer = intent.getBooleanExtra(EXTRA_OFFER, false);
            String audioSampleRate = intent.getStringExtra(EXTRA_AUDIOSAMPLERATE);
            int audioSampleRateValue;
            if (audioSampleRate == null) {
                audioSampleRateValue = WebRTCVideoChatProfile.PARAM_RATE_48000;
            } else {
                audioSampleRateValue = Integer.valueOf(audioSampleRate);
            }
            String audioBitDepth = intent.getStringExtra(EXTRA_AUDIOBITDEPTH);
            String audioChannel = intent.getStringExtra(EXTRA_AUDIOCHANNEL);

            WebRTCController.Builder builder = new WebRTCController.Builder();
            builder.setApplication((WebRTCApplication) getApplication());
            builder.setWebRTCEventListener(mListener);
            builder.setContext(this);
            builder.setEglBase(eglBase);
            builder.setConfig(config);
            builder.setRemoteRender(mRemoteRender);
            builder.setLocalRender(mLocalRender);
            builder.setVideoUri(videoUri);
            builder.setAudioUri(audioUri);
            builder.setAddressId(addressId);
            builder.setOffer(offer);
            builder.setAudioSampleRate(audioSampleRateValue);
            builder.setAudioBitDepth(audioBitDepth);
            builder.setAudioChannel(audioChannel);
            builder.setLandscape(isLandscape());
            mWebRTCController = builder.create();
            updateVideoView(videoUri);
        } else {
            openWebRTCErrorDialog();
        }
    }

    private boolean isLandscape() {
        return (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWebRTCController != null) {
            mWebRTCController.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWebRTCController != null) {
            mWebRTCController.onPause();
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
            hangup();
            return false;
        }
    }

    /**
     * Updated layout of the views.
     */
    private void updateVideoView(final String videoUri) {
        mRemoteLayout.setPosition(0, 0, 100, 90);
        mRemoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mRemoteRender.setMirror(false);

        mLocalLayout.setPosition(72, 72, 25, 25);
        mLocalRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mLocalRender.setMirror("true".equals(videoUri));

        mLocalRender.requestLayout();
        mRemoteRender.requestLayout();
    }

    /**
     * Hang up a call.
     */
    private void hangup() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ VideoChatActivity::hangup");
        }

        if (mWebRTCController != null) {
            mWebRTCController.hangup();
            mWebRTCController = null;
        }
    }

    /**
     * Open a error dialog of WebRTC.
     */
    private void openWebRTCErrorDialog() {
        openErrorDialog(R.string.error_failed_to_connect_p2p_msg);
    }

    /**
     * Create a error dialog of WebRTC.
     */
    private void createWebRTCErrorDialog() {
        openErrorDialog(R.string.error_failed_to_connect_already_disconnect_msg);
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

    private WebRTCController.WebRTCEventListener mListener = new WebRTCController.WebRTCEventListener() {
        @Override
        public void onFoundPeer(WebRTCController controller) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "WebRTCEventListener#onFoundPeer");
            }
        }

        @Override
        public void onNotFoundPeer(WebRTCController controller) {
            openWebRTCErrorDialog();
        }

        @Override
        public void onCallFailed(WebRTCController controller) {
            openWebRTCErrorDialog();
        }

        @Override
        public void onAnswerFailed(WebRTCController controller) {
            openWebRTCErrorDialog();
        }

        @Override
        public void onConnected(WebRTCController controller) {
        }

        @Override
        public void onDisconnected(WebRTCController controller) {
            finish();
        }

        @Override
        public void onError(WebRTCController controller) {
            openWebRTCErrorDialog();
        }
    };
}
