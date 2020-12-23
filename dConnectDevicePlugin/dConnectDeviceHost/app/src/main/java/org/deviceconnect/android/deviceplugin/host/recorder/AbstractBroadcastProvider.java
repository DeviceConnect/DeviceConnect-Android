package org.deviceconnect.android.deviceplugin.host.recorder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractBroadcastProvider implements BroadcasterProvider {
    /**
     * 映像を配信するクラス.
     */
    private Broadcaster mBroadcaster;

    /**
     * イベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    @Override
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public Broadcaster getBroadcaster() {
        return mBroadcaster;
    }

    @Override
    public boolean isRunning() {
        return mBroadcaster != null && mBroadcaster.isRunning();
    }

    @Override
    public Broadcaster startBroadcaster(String broadcastURI) {
        if (mBroadcaster != null && mBroadcaster.isRunning()) {
            return mBroadcaster;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);

        mBroadcaster = createBroadcaster(broadcastURI);
        if (mBroadcaster == null) {
            return null;
        }

        mBroadcaster.start(new Broadcaster.OnStartCallback() {
            @Override
            public void onSuccess() {
                result.set(true);
                latch.countDown();
            }

            @Override
            public void onFailed(Exception e) {
                result.set(false);
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }

        if (!result.get()) {
            mBroadcaster.stop();
            mBroadcaster = null;
        } else {
            mBroadcaster.setOnEventListener(new Broadcaster.OnEventListener() {
                @Override
                public void onStarted() {
                    postBroadcastStarted(mBroadcaster);
                }

                @Override
                public void onStopped() {
                    postBroadcastStopped(mBroadcaster);
                }

                @Override
                public void onError(Exception e) {
                    postBroadcastError(mBroadcaster, e);
                }
            });
        }

        return mBroadcaster;
    }

    @Override
    public void stopBroadcaster() {
        if (mBroadcaster != null) {
            mBroadcaster.stop();
            mBroadcaster = null;
        }
    }

    @Override
    public void onConfigChange() {
        if (mBroadcaster != null) {
            mBroadcaster.onConfigChange();
        }
    }

    @Override
    public void setMute(boolean mute) {
        if (mBroadcaster != null) {
            mBroadcaster.setMute(mute);
        }
    }

    /**
     * Broadcaster のインスタンスを作成します.
     *
     * @param broadcastURI 配信先の URI
     * @return Broadcaster のインスタンス
     */
    public abstract Broadcaster createBroadcaster(String broadcastURI);

    private void postBroadcastStarted(Broadcaster broadcaster) {
        if (mOnEventListener != null) {
            mOnEventListener.onStarted(broadcaster);
        }
    }

    private void postBroadcastStopped(Broadcaster broadcaster) {
        if (mOnEventListener != null) {
            mOnEventListener.onStopped(broadcaster);
        }
    }

    private void postBroadcastError(Broadcaster broadcaster, Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(broadcaster, e);
        }
    }
}
