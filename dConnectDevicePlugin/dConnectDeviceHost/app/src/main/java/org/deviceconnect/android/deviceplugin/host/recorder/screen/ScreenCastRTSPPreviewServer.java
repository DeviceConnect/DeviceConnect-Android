package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import androidx.annotation.RequiresApi;

import android.util.Log;
import android.view.Surface;

import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.GLDrawer2D;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AACStream;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.rtsp.RtspServerImpl;
import net.majorkernelpanic.streaming.video.SurfaceH264Stream;
import net.majorkernelpanic.streaming.video.VideoQuality;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;

import java.io.IOException;
import java.net.Socket;

import static org.deviceconnect.android.deviceplugin.host.BuildConfig.DEBUG;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ScreenCastRTSPPreviewServer extends ScreenCastPreviewServer implements RtspServer.Delegate {

    private static final String TAG = ScreenCastRTSPPreviewServer.class.getSimpleName();

    static final String MIME_TYPE = "video/x-rtp";

    private static final String SERVER_NAME = "Android Host Screen RTSP Server";

    private final ScreenCastManager mScreenCastMgr;

    private final Object mLockObj = new Object();

    private Socket mClientSocket;

    private SurfaceH264Stream mVideoStream;

    private ScreenCast mScreenCast;

    private RtspServer mRtspServer;

    private boolean mIsStartedCast;

    private VideoQuality mQuality;

    private final Handler mHandler;
    private final Object mSync = new Object();
    private volatile boolean mIsRecording;
    private boolean requestDraw;
    private DrawTask mScreenCaptureTask;
    private AACStream mAac;

    ScreenCastRTSPPreviewServer(final Context context,
                                final AbstractPreviewServerProvider serverProvider,
                                final ScreenCastManager screenCastMgr) {
        super(context, serverProvider);
        mScreenCastMgr = screenCastMgr;
        final HandlerThread thread = new HandlerThread("ScreenCastRTSPPreviewServer");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }
    /**
     * Recorderをmute状態にする.
     */
    public void mute() {
        super.mute();
        if (mAac != null) {
            mAac.mute();
        }
    }

    /**
     * Recorderのmute状態を解除する.
     */
    public void unMute() {
        super.unMute();
        if (mAac != null) {
            mAac.unMute();
        }
    }
    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            if (mRtspServer == null) {
                mRtspServer = new RtspServerImpl(SERVER_NAME);
                mRtspServer.setPort(20000);
                mRtspServer.setDelegate(ScreenCastRTSPPreviewServer.this);
                if (!mRtspServer.start()) {
                    callback.onFail();
                    return;
                }
            }
            String uri = "rtsp://localhost:" + mRtspServer.getPort();
            callback.onStart(uri);
        }
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mRtspServer != null) {
                mRtspServer.stop();
                mRtspServer = null;
            }
            stopScreenCast();
            mClientSocket = null;
        }
        stopDrawTask();
    }

    // DrawTaskの後始末
    private void stopDrawTask() {
        synchronized (mSync) {
            mIsRecording = false;
            mSync.notifyAll();
        }
        mHandler.getLooper().quit();
    }

    @Override
    protected void onConfigChange() {
    }

    @Override
    public Session generateSession(final String uri, final Socket clientSocket) {
        try {
            return startScreenCast(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void eraseSession(final Session session) {
        stopScreenCast();
    }

    @Override
    public int getQuality() {
        return 0; // Not support.
    }

    @Override
    public void setQuality(int quality) {
        // Not support.
    }

    private Session startScreenCast(final Socket clientSocket) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        HostDeviceRecorder.PictureSize previewSize = getRotatedPreviewSize();

        VideoQuality videoQuality = new VideoQuality();

        //縦横切り替わった時に、初期化する必要のないように長い方に合わせる
        int resolution = previewSize.getWidth();
        if (resolution < previewSize.getHeight()) {
            resolution = previewSize.getHeight();
        }
        videoQuality.resX = resolution;
        videoQuality.resY = resolution;
        videoQuality.bitrate = mServerProvider.getPreviewBitRate();
        videoQuality.framerate = (int) mServerProvider.getMaxFrameRate();

        synchronized (mLockObj) {
            mClientSocket = clientSocket;
            mVideoStream = new SurfaceH264Stream(prefs, videoQuality);
            mIsStartedCast = true;
            mScreenCaptureTask = new DrawTask(null, 0, videoQuality);
            mIsRecording = true;
            new Thread(mScreenCaptureTask, "ScreenCaptureThread").start();
        }

        SessionBuilder builder = new SessionBuilder();
        builder.setContext(mContext);
        builder.setVideoStream(mVideoStream);
        mAac = new AACStream(mContext);
        if (isMuted()) {
            mAac.mute();
        } else {
            mAac.unMute();
        }
        builder.setAudioStream(mAac);

        builder.setVideoQuality(videoQuality);

        Session session = builder.build();
        session.setOrigin(clientSocket.getLocalAddress().getHostAddress());
        if (session.getDestination() == null) {
            session.setDestination(clientSocket.getInetAddress().getHostAddress());
        }
        return session;
    }

    private void stopScreenCast() {
        synchronized (mLockObj) {
            if (mIsStartedCast) {
                mVideoStream.stop();
                mVideoStream = null;
                mScreenCast.stopCast();
                mScreenCast = null;
                mIsStartedCast = false;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final class DrawTask extends EglTask {
        private long intervals;
        private EGLBase.IEglSurface mEncoderSurface;
        private GLDrawer2D mDrawer;
        private final float[] mTexMatrix = new float[16];
        private int mTexId;
        private SurfaceTexture mSourceTexture;
        private Surface mSourceSurface;


        public DrawTask(final EGLBase.IContext sharedContext, final int flags, final VideoQuality quality) {
            super(sharedContext, flags);
            mQuality = quality;
        }

        @Override
        protected void onStart() {
            mDrawer = new GLDrawer2D(true);
            mTexId = mDrawer.initTex();
            mSourceTexture = new SurfaceTexture(mTexId);
            mSourceTexture.setDefaultBufferSize(mQuality.resX, mQuality.resY);	// これを入れないと映像が取れない
            mSourceSurface = new Surface(mSourceTexture);
            mSourceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener, mHandler);
            mEncoderSurface = getEgl().createFromSurface(mVideoStream.getInputSurface());
            intervals = (long)(1000f / mQuality.framerate);
            HostDeviceRecorder.PictureSize previewSize = getRotatedPreviewSize();
            HostDeviceRecorder.PictureSize resolutionSize = new HostDeviceRecorder.PictureSize(previewSize.getWidth(), previewSize.getWidth());
            if (resolutionSize.getWidth() < previewSize.getHeight()) {
                resolutionSize = new HostDeviceRecorder.PictureSize(previewSize.getHeight(), previewSize.getHeight());
            }
            mScreenCast = mScreenCastMgr.createScreenCast(mSourceSurface, resolutionSize);
            mScreenCast.startCast();
            // 録画タスクを起床
            queueEvent(mDrawTask);
        }

        @Override
        protected void onStop() {
            if (mDrawer != null) {
                mDrawer.release();
                mDrawer = null;
            }
            if (mSourceSurface != null) {
                mSourceSurface.release();
                mSourceSurface = null;
            }
            if (mSourceTexture != null) {
                mSourceTexture.release();
                mSourceTexture = null;
            }
            if (mEncoderSurface != null) {
                mEncoderSurface.release();
                mEncoderSurface = null;
            }
            makeCurrent();
            mScreenCast.stopCast();
            mScreenCastMgr.clean();
        }

        @Override
        protected boolean onError(final Exception e) {
            Log.w(TAG, "mScreenCaptureTask:", e);
            return false;
        }

        @Override
        protected Object processRequest(final int request, final int arg1, final int arg2, final Object obj) {
            return null;
        }

        // TextureSurfaceで映像を受け取った際のコールバックリスナー
        private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener = (surfaceTexture) -> {
            if (mIsRecording) {
                synchronized (mSync) {
                    requestDraw = true;
                    mSync.notifyAll();
                }
            }
        };

        private final Runnable mDrawTask = () -> {
            boolean localRequestDraw;
            synchronized (mSync) {
                localRequestDraw = requestDraw;
                if (!requestDraw) {
                    try {
                        mSync.wait(intervals);
                        localRequestDraw = requestDraw;
                        requestDraw = false;
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            Log.v(TAG, "draw:InterruptedException");
                        }
                        return;
                    }
                }
            }
            if (mIsRecording) {
                if (localRequestDraw) {
                    mSourceTexture.updateTexImage();
                    mSourceTexture.getTransformMatrix(mTexMatrix);
                }
                // SurfaceTextureで受け取った画像をMediaCodecの入力用Surfaceへ描画する
                mEncoderSurface.makeCurrent();
                mDrawer.draw(mTexId, mTexMatrix, 0);
                mEncoderSurface.swap();
                // EGL保持用のオフスクリーンに描画しないとハングアップする機種の為のworkaround
                makeCurrent();
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glFlush();
                queueEvent(this);
            } else {
                releaseSelf();
            }
        };

    }

}
