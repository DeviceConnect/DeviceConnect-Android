/*
SonyCameraPreview
Copyright (c) 2017 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera.utils;

import com.example.sony.cameraremote.SimpleRemoteApi;
import com.example.sony.cameraremote.utils.SimpleLiveviewSlicer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * SonyカメラからのプレビューデータをWebサーバ経由で配信するクラス.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraPreview extends Thread {

    /**
     * プレビュー再生フラグ.
     * <p>
     * プレビューを再生中はtrue、それ以外はfalse.
     * </p>
     */
    private boolean mWhileFetching;

    /**
     * SonyCameraのリモートAPI.
     */
    private SimpleRemoteApi mRemoteApi;

    /**
     * Server for MotionJPEG.
     */
    private MixedReplaceMediaServer mServer;

    /**
     * プレビューイベント通知リスナー.
     */
    private OnPreviewListener mOnPreviewListener;

    /**
     * タイムスライス.(ms)
     */
    private int mTimeSlice = 100;

    /**
     * コンストラクタ.
     * @param remoteApi Sonyカメラ API にアクセスするクラス
     */
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
                mServer.setContentType("image/jpeg");
                mServer.setTimeSlice(mTimeSlice);

                String ip = mServer.start();
                if (ip == null) {
                    mOnPreviewListener.onError();
                    return;
                }
            }

            mOnPreviewListener.onPreviewServer(mServer.getUrl());

            while (mWhileFetching) {
                final SimpleLiveviewSlicer.Payload payload = slicer.nextPayload();
                if (payload == null) { // never occurs
                    continue;
                }
                mServer.offerMedia(payload.getJpegData());
            }
        } catch (Exception e) {
            mOnPreviewListener.onError();
        } finally {
            if (slicer != null) {
                try {
                    slicer.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
            try {
                mRemoteApi.stopLiveview();
            } catch (IOException e) {
                // do nothing
            }

            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            mWhileFetching = false;

            mOnPreviewListener.onComplete();
        }
    }

    /**
     * プレビューを開始します.
     */
    public void startPreview() {
        mWhileFetching = true;
        start();
    }

    /**
     * プレビューを停止します.
     */
    public void stopPreview() {
        mWhileFetching = false;
    }

    /**
     * プレビュー撮影中か確認を行います.
     * @return 撮影中の場合はtrue、それ以外はfalse
     */
    public boolean isPreview() {
        return mWhileFetching && mServer != null && mServer.isRunning();
    }

    /**
     * プレビューを配信するサーバへのURLを返却します.
     * @return プレビューを配信するサーバへのURL
     */
    public String getPreviewUrl() {
        if (mServer == null) {
            return null;
        }
        return mServer.getUrl();
    }

    /**
     * タイムスライスを設定します.
     * <p>
     * デフォルトでは、100msが設定してあります。
     * </p>
     * @param timeSlice タイムスライス(ms)
     */
    public void setTimeSlice(final int timeSlice) {
        mTimeSlice = timeSlice;
    }

    /**
     * プレビューのイベントを通知するためのリスナーを設定します.
     * @param onPreviewListener リスナー
     */
    public void setOnPreviewListener(OnPreviewListener onPreviewListener) {
        mOnPreviewListener = onPreviewListener;
    }

    /**
     * プレビューの通知を行うリスナークラス.
     */
    public interface OnPreviewListener {
        /**
         * プレビューを出力するサーバを通知します.
         * @param url プレビューサーバへのURL
         */
        void onPreviewServer(String url);

        /**
         * プレビューサーバ起動に失敗したことを通知します.
         */
        void onError();

        /**
         * プレビューサーバが完了したことを通知します.
         */
        void onComplete();
    }
}
