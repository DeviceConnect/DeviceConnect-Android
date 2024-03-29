package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractLiveStreamingProvider implements LiveStreamingProvider {
    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * プレビュー配信サーバーのリスト.
     */
    private final List<LiveStreaming> mLiveStreamingList = new ArrayList<>();

    /**
     * プレビュー配信を行うレコーダ.
     */
    private final HostMediaRecorder mRecorder;

    /**
     * Notification 表示フラグ.
     */
    private boolean mIsRunning;

    /**
     * プレビュー配信サーバのイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    public AbstractLiveStreamingProvider(Context context, HostMediaRecorder recorder) {
        mContext = context;
        mRecorder = recorder;
        init();
    }

    @Override
    public void addLiveStreaming(LiveStreaming streaming) {
        if (streaming != null) {
            mLiveStreamingList.add(streaming);
        }
    }

    @Override
    public void removeLiveStreaming(String encoderId) {
        for (LiveStreaming streaming : mLiveStreamingList) {
            if (streaming.getId().equals(encoderId)) {
                streaming.release();
                mLiveStreamingList.remove(streaming);
                return;
            }
        }
    }

    @Override
    public List<LiveStreaming> getLiveStreamingList() {
        return mLiveStreamingList;
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public List<LiveStreaming> start() {
        List<LiveStreaming> results = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(mLiveStreamingList.size());
        for (LiveStreaming streaming : mLiveStreamingList) {
            streaming.setOnEventListener(new Broadcaster.OnEventListener() {
                @Override
                public void onStarted() {
                }

                @Override
                public void onStopped() {
                }

                @Override
                public void onError(Exception e) {
                    postOnError(streaming, e);

                    if (isAllStreamingStopped()) {
                        stop();
                    }
                }
            });
            streaming.start(new LiveStreaming.OnStartCallback() {
                @Override
                public void onSuccess() {
                    results.add(streaming);
                    latch.countDown();
                }

                @Override
                public void onFailed(Exception e) {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(5, TimeUnit.SECONDS);
            if (results.size() > 0) {
                mIsRunning = true;
                sendNotification(mRecorder.getId(), mRecorder.getName());
                postOnStarted(results);
            }
        } catch (InterruptedException e) {
            // ignore.
        }
        return results;
    }

    @Override
    public void stop() {
        hideNotification(mRecorder.getId());

        for (LiveStreaming streaming : getLiveStreamingList()) {
            streaming.stop();
        }

        if (mIsRunning) {
            mIsRunning = false;
            postOnStopped();
        }
    }

    @Override
    public void requestSyncFrame() {
        for (LiveStreaming streaming : getLiveStreamingList()) {
            streaming.requestSyncFrame();
        }
    }

    @Override
    public void requestBitRate() {
        for (LiveStreaming streaming : getLiveStreamingList()) {
            streaming.requestBitRate();
        }
    }

    @Override
    public void requestJpegQuality() {
        for (LiveStreaming streaming : getLiveStreamingList()) {
            streaming.requestJpegQuality();
        }
    }

    @Override
    public void onConfigChange() {
        for (LiveStreaming streaming : getLiveStreamingList()) {
            streaming.onConfigChange();
        }
    }

    @Override
    public void setMute(boolean mute) {
        for (LiveStreaming streaming : getLiveStreamingList()) {
            streaming.setMute(mute);
        }
    }

    @Override
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public void release() {
        stop();

        for (LiveStreaming streaming : getLiveStreamingList()) {
            streaming.release();
        }
    }

    /**
     * Notification を送信します.
     *
     * @param id notification を識別する ID
     * @param name 名前
     */
    protected abstract void sendNotification(String id, String name);

    /**
     * Notification 非表示にします.
     *
     * @param id notification を識別する ID
     */
    protected abstract void hideNotification(String id);

    public Context getContext() {
        return mContext;
    }

    public HostMediaRecorder getRecorder() {
        return mRecorder;
    }

    private void init() {
        HostMediaRecorder.Settings settings = getRecorder().getSettings();
        for (String encoderId : settings.getEncoderIdList()) {
            HostMediaRecorder.EncoderSettings encoderSetting = settings.getEncoderSetting(encoderId);
            if (encoderSetting != null) {
                LiveStreaming streaming = createLiveStreaming(encoderId, encoderSetting);
                if (streaming != null) {
                    addLiveStreaming(streaming);
                }
            }
        }
    }

    private boolean isAllStreamingStopped() {
        for (LiveStreaming streaming : mLiveStreamingList) {
            if (streaming.isRunning()) {
                return true;
            }
        }
        return true;
    }

    private void postOnStarted(List<LiveStreaming> servers) {
        if (mOnEventListener != null) {
            mOnEventListener.onStarted(servers);
        }
    }

    private void postOnStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onStopped();
        }
    }

    private void postOnError(LiveStreaming server, Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(server, e);
        }
    }
}
