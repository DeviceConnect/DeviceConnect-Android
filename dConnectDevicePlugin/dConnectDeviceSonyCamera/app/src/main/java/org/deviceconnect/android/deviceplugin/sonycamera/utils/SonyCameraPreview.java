package org.deviceconnect.android.deviceplugin.sonycamera.utils;

import com.example.sony.cameraremote.SimpleRemoteApi;
import com.example.sony.cameraremote.utils.SimpleLiveviewSlicer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SonyCameraPreview extends Thread {

    private boolean mWhileFetching;

    /**
     * SonyCameraのリモートAPI.
     */
    private SimpleRemoteApi mRemoteApi;

    /**
     * Server for MotionJPEG.
     */
    private MixedReplaceMediaServer mServer;

    private OnPreviewListener mOnPreviewListener;

    public SonyCameraPreview(final SimpleRemoteApi remoteApi) {
        mRemoteApi = remoteApi;
    }

    @Override
    public void run() {
        SimpleLiveviewSlicer slicer = null;
        try {
            // Prepare for connecting.
            JSONObject replyJson = mRemoteApi.startLiveview();
            if (!SonyCameraUtil.isErrorReply(replyJson)) {
                JSONArray resultsObj = replyJson.getJSONArray("result");
                String liveviewUrl = null;
                if (1 <= resultsObj.length()) {
                    // Obtain liveview URL from the result.
                    liveviewUrl = resultsObj.getString(0);
                }
                if (liveviewUrl != null) {
                    // Create Slicer to open the stream and parse it.
                    slicer = new SimpleLiveviewSlicer();
                    slicer.open(liveviewUrl);
                }
            }

            if (slicer == null) {
                mOnPreviewListener.onError();
                return;
            }

            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerEventListener(new MixedReplaceMediaServer.ServerEventListener() {
                    @Override
                    public void onStart() {
                    }
                    @Override
                    public void onStop() {
                        mWhileFetching = false;
                    }
                    @Override
                    public void onError() {
                        mWhileFetching = false;
                    }
                });
                mServer.setServerName("SonyCameraDevicePlugin Server");
                mServer.setContentType("image/jpg");

                String ip = mServer.start();
                if (ip == null) {
                    mOnPreviewListener.onError();
                    return;
                }
            }

            mOnPreviewListener.onStartPreview(mServer.getUrl());

            while (mWhileFetching) {
                final SimpleLiveviewSlicer.Payload payload = slicer.nextPayload();
                if (payload == null) { // never occurs
                    continue;
                }
                mServer.offerMedia(payload.getJpegData());
            }
        } catch (IOException e) {
            mOnPreviewListener.onError();
        } catch (JSONException e) {
            mOnPreviewListener.onError();
        } finally {
            if (slicer != null) {
                try {
                    slicer.close();
                } catch (IOException e) {
                }
            }
            try {
                mRemoteApi.stopLiveview();
            } catch (IOException e) {
            }

            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }

            mWhileFetching = false;

            mOnPreviewListener.onComplete();
        }
    }

    public void startPreview() {
        mWhileFetching = true;
        start();
    }

    public void stopPreview() {
        mWhileFetching = false;
    }

    public void setOnPreviewListener(OnPreviewListener onPreviewListener) {
        mOnPreviewListener = onPreviewListener;
    }

    public interface OnPreviewListener {
        void onStartPreview(String url);
        void onError();
        void onComplete();
    }
}
