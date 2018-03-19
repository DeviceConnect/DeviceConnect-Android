package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.activity.VideoChatActivity;
import org.deviceconnect.android.deviceplugin.webrtc.core.AudioTrackExternal;
import org.deviceconnect.android.deviceplugin.webrtc.core.MySurfaceViewRenderer;
import org.deviceconnect.android.deviceplugin.webrtc.core.Peer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;
import org.deviceconnect.android.deviceplugin.webrtc.core.WebRTCController;
import org.deviceconnect.android.deviceplugin.webrtc.fragment.PercentFrameLayout;
import org.deviceconnect.android.profile.VideoChatProfile;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.voiceengine.WebRtcAudioTrack;
import org.webrtc.voiceengine.WebRtcAudioTrackModule;
import org.webrtc.voiceengine.WebRtcAudioTrackModuleFactory;

import java.util.HashMap;
import java.util.Map;

public class WebRTCManager {

    private static final int VIDEO_PORT = 12345;
    private static final int AUDIO_PORT = 11111;

    private WebRTCApplication mApplication;
    private Handler mHandler = new Handler();

    private Map<Peer, WebRTCController> mMap = new HashMap<>();
    private WindowManager mWinMgr;

    private AudioTrackExternal mAudioTrackExternal;

    private MixedReplaceMediaServer mServer = null;

    public WebRTCManager(WebRTCApplication application) {
        mApplication = application;
        mWinMgr = (WindowManager) mApplication.getSystemService(Context.WINDOW_SERVICE);
    }

    public void connectOnUiThread(final Intent intent) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                connect(intent);
            }
        });
    }

    public boolean isConnect() {
        return mServer != null;
    }
    public void connect(Intent intent) {
        if (mServer != null) {
            return; // Error : Already the server is running.
        }
        mServer = new MixedReplaceMediaServer();
        mServer.setPort(VIDEO_PORT);
        mServer.start();

        LayoutInflater inflater = (LayoutInflater) mApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.activity_main, null);

        PercentFrameLayout localLayout = (PercentFrameLayout) root.findViewById(R.id.local_view_layout);
        PercentFrameLayout remoteLayout = (PercentFrameLayout) root.findViewById(R.id.remote_video_layout);

        MySurfaceViewRenderer localRender = (MySurfaceViewRenderer) root.findViewById(R.id.local_video_view);
        localRender.setType(MySurfaceViewRenderer.TYPE_LOCAL);
        localRender.setWebServer(mServer);
        MySurfaceViewRenderer remoteRender = (MySurfaceViewRenderer) root.findViewById(R.id.remote_video_view);
        remoteRender.setType(MySurfaceViewRenderer.TYPE_REMOTE);
        remoteRender.setWebServer(mServer);

        EglBase eglBase = EglBase.create();
        remoteRender.init(eglBase.getEglBaseContext(), null);
        remoteRender.createYuvConverter(eglBase.getEglBaseContext());

        localRender.init(eglBase.getEglBaseContext(), null);
        localRender.createYuvConverter(eglBase.getEglBaseContext());
        localRender.setZOrderMediaOverlay(true);

        PeerConfig config = intent.getParcelableExtra(VideoChatActivity.EXTRA_CONFIG);
        String videoUri = intent.getStringExtra(VideoChatActivity.EXTRA_VIDEO_URI);
        String audioUri = intent.getStringExtra(VideoChatActivity.EXTRA_AUDIO_URI);
        String addressId = intent.getStringExtra(VideoChatActivity.EXTRA_ADDRESS_ID);
        boolean offer = intent.getBooleanExtra(VideoChatActivity.EXTRA_OFFER, false);
        String audioSampleRate = intent.getStringExtra(VideoChatActivity.EXTRA_AUDIOSAMPLERATE);
        int audioSampleRateValue;
        if (audioSampleRate == null) {
            audioSampleRateValue = VideoChatProfile.PARAM_RATE_48000;
        } else {
            audioSampleRateValue = Integer.valueOf(audioSampleRate);
        }
        String audioBitDepth = intent.getStringExtra(VideoChatActivity.EXTRA_AUDIOBITDEPTH);
        String audioChannel = intent.getStringExtra(VideoChatActivity.EXTRA_AUDIOCHANNEL);

        WebRtcAudioTrack.setAudioTrackModuleFactory(new WebRtcAudioTrackModuleFactory() {
            @Override
            public WebRtcAudioTrackModule create(Context context) {
                return mAudioTrackExternal;
            }
        });

        WebRTCController.Builder builder = new WebRTCController.Builder();
        builder.setApplication(mApplication);
        builder.setWebRTCEventListener(mListener);
        builder.setContext(mApplication);
        builder.setEglBase(eglBase);
        builder.setConfig(config);
        builder.setRemoteRender(remoteRender);
        builder.setLocalRender(localRender);
        mAudioTrackExternal = new AudioTrackExternal(mApplication.getApplicationContext(), AUDIO_PORT);
        builder.setAudioTrackExternal(mAudioTrackExternal);
        builder.setVideoUri(videoUri);
        builder.setAudioUri(audioUri);
        builder.setAddressId(addressId);
        builder.setOffer(offer);
        builder.setAudioSampleRate(audioSampleRateValue);
        builder.setAudioBitDepth(audioBitDepth);
        builder.setAudioChannel(audioChannel);
        builder.setLandscape(isLandscape());
        builder.create();

        Point size = getDisplaySize();
        int pt = (int) (5 * getScaledDensity());
        int type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        WindowManager.LayoutParams l = new WindowManager.LayoutParams(pt, pt,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        l.x = -size.x / 2;
        l.y = -size.y / 2;

        mWinMgr.addView(root, l);

        remoteLayout.setPosition(0, 0, 100, 90);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        remoteRender.setMirror(false);

        localLayout.setPosition(72, 72, 25, 25);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        localRender.setMirror("true".equals(videoUri));

        localRender.requestLayout();
        remoteRender.requestLayout();
    }

    private boolean isLandscape() {
        return (mApplication.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * Disconnect a peer connection.
     * @param peer peer for disconnect
     */
    public void disconnect(Peer peer) {
        WebRTCController ctl = mMap.remove(peer);
        if (ctl != null) {
            ctl.onPause();
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            ctl.hangup();
            mWinMgr.removeView((View) ctl.getLocalRender().getParent().getParent());
        }
    }

    public  void destroy() {
        for (Map.Entry<Peer, WebRTCController> e : mMap.entrySet()) {
            e.getValue().hangup();
        }
        mMap.clear();
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
    }

    /**
     * Displayの密度を取得する.
     *
     * @return 密度
     */
    private float getScaledDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        mWinMgr.getDefaultDisplay().getMetrics(metrics);
        return metrics.scaledDensity;
    }

    /**
     * Displayのサイズを取得する.
     *
     * @return サイズ
     */
    private Point getDisplaySize() {
        Display disp = mWinMgr.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        return size;
    }

    /**
     * Open a error dialog of WebRTC.
     */
    private void openWebRTCErrorDialog(WebRTCController controller) {
        openErrorDialog(controller, R.string.error_failed_to_connect_p2p_msg);
    }

    /**
     * Open a error dialog.
     *
     * @param resId resource id
     */
    private void openErrorDialog(final WebRTCController controller, final int resId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mApplication);
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
                        disconnect(controller.getPeer());
                    }
                });
                dialog.show();
            }
        });
    }

    private WebRTCController.WebRTCEventListener mListener = new WebRTCController.WebRTCEventListener() {
        @Override
        public void onFoundPeer(WebRTCController controller) {
            mMap.put(controller.getPeer(), controller);
        }

        @Override
        public void onNotFoundPeer(WebRTCController controller) {
        }

        @Override
        public void onCallFailed(WebRTCController controller) {
        }

        @Override
        public void onAnswerFailed(WebRTCController controller) {
        }

        @Override
        public void onConnected(WebRTCController controller) {
            controller.onResume();
        }

        @Override
        public void onDisconnected(WebRTCController controller) {
            disconnect(controller.getPeer());
        }

        @Override
        public void onError(WebRTCController controller) {
            openWebRTCErrorDialog(controller);
        }
    };
}
