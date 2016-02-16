package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
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
import org.deviceconnect.android.deviceplugin.webrtc.core.MySurfaceViewRenderer;
import org.deviceconnect.android.deviceplugin.webrtc.core.Peer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerUtil;
import org.deviceconnect.android.deviceplugin.webrtc.core.WebRTCController;
import org.deviceconnect.android.deviceplugin.webrtc.fragment.PercentFrameLayout;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.VideoChatProfile;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRTCManager {

    private WebRTCApplication mApplication;
    private Handler mHandler = new Handler();

    private Map<Peer, WebRTCController> mMap = new HashMap<>();
    private WindowManager mWinMgr;

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

    public void connect(Intent intent) {
        LayoutInflater inflater = (LayoutInflater) mApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.activity_main, null);

        PercentFrameLayout localLayout = (PercentFrameLayout) root.findViewById(R.id.local_view_layout);
        PercentFrameLayout remoteLayout = (PercentFrameLayout) root.findViewById(R.id.remote_video_layout);

        MySurfaceViewRenderer localRender = (MySurfaceViewRenderer) root.findViewById(R.id.local_video_view);
        MySurfaceViewRenderer remoteRender = (MySurfaceViewRenderer) root.findViewById(R.id.remote_video_view);

        EglBase eglBase = EglBase.create();
        remoteRender.init(eglBase.getEglBaseContext(), null);
        remoteRender.createYuvConverter(eglBase.getEglBaseContext(), 12345);

        localRender.init(eglBase.getEglBaseContext(), null);
        localRender.createYuvConverter(eglBase.getEglBaseContext(), 12346);
        localRender.setZOrderMediaOverlay(true);

        PeerConfig config = intent.getParcelableExtra(VideoChatActivity.EXTRA_CONFIG);
        String videoUri = intent.getStringExtra(VideoChatActivity.EXTRA_VIDEO_URI);
        String audioUri = intent.getStringExtra(VideoChatActivity.EXTRA_AUDIO_URI);
        String addressId = intent.getStringExtra(VideoChatActivity.EXTRA_ADDRESS_ID);
        boolean offer = intent.getBooleanExtra(VideoChatActivity.EXTRA_OFFER, false);

        WebRTCController.Builder builder = new WebRTCController.Builder();
        builder.setApplication(mApplication);
        builder.setWebRTCEventListener(mListener);
        builder.setContext(mApplication);
        builder.setEglBase(eglBase);
        builder.setConfig(config);
        builder.setRemoteRender(remoteRender);
        builder.setLocalRender(localRender);
        builder.setVideoUri(videoUri);
        builder.setAudioUri(audioUri);
        builder.setAddressId(addressId);
        builder.setOffer(offer);
        builder.create();

        Point size = getDisplaySize();
        int pt = (int) (5 * getScaledDensity());
        WindowManager.LayoutParams l = new WindowManager.LayoutParams(pt, pt,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
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
        localRender.setMirror(true);

        localRender.requestLayout();
        remoteRender.requestLayout();
    }

    /**
     * Disconnect a peer connection.
     * @param peer peer for disconnect
     */
    public void disconnect(Peer peer) {
        WebRTCController ctl = mMap.remove(peer);
        if (ctl != null) {
            ctl.hangup();
            mWinMgr.removeView((View) ctl.getLocalRender().getParent().getParent());
        }
    }

    public  void destroy() {
        for (Map.Entry<Peer, WebRTCController> e : mMap.entrySet()) {
            e.getValue().hangup();
        }
        mMap.clear();
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
     * Notifies call event.
     */
    private void sendCallEvent(WebRTCController controller) {
        List<Event> events = EventManager.INSTANCE.getEventList(
                PeerUtil.getServiceId(controller.getPeer()),
                VideoChatProfile.PROFILE_NAME, null, VideoChatProfile.ATTR_ONCALL);
        if (events.size() != 0) {
            Bundle[] args = new Bundle[1];
            args[0] = new Bundle();
            args[0].putString(VideoChatProfile.PARAM_NAME, controller.getAddressId());
            args[0].putString(VideoChatProfile.PARAM_ADDRESSID, controller.getAddressId());
            // TODO: 出力先のURIを指定
//            args[0].putString(VideoChatProfile.PARAM_VIDEO, "XXX");
//            args[0].putString(VideoChatProfile.PARAM_AUDIO, "XXX");
            for (Event e : events) {
                Intent event = EventManager.createEventMessage(e);
                event.putExtra(VideoChatProfile.PARAM_ONCALL, args);
                mApplication.sendBroadcast(event);
            }
        }
    }

    /**
     * Notifies hang up event.
     */
    private void sendHangupEvent(WebRTCController controller) {
        List<Event> events = EventManager.INSTANCE.getEventList(
                PeerUtil.getServiceId(controller.getPeer()),
                VideoChatProfile.PROFILE_NAME, null, VideoChatProfile.ATTR_HANGUP);
        if (events.size() != 0) {
            Bundle arg = new Bundle();
            arg.putString(VideoChatProfile.PARAM_NAME, controller.getAddressId());
            arg.putString(VideoChatProfile.PARAM_ADDRESSID, controller.getAddressId());
            for (Event e : events) {
                Intent event = EventManager.createEventMessage(e);
                event.putExtra(VideoChatProfile.PARAM_HANGUP, arg);
                mApplication.sendBroadcast(event);
            }
        }
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
            sendCallEvent(controller);
        }

        @Override
        public void onDisconnected(WebRTCController controller) {
            sendHangupEvent(controller);
            disconnect(controller.getPeer());
        }

        @Override
        public void onError(WebRTCController controller) {
        }
    };
}
