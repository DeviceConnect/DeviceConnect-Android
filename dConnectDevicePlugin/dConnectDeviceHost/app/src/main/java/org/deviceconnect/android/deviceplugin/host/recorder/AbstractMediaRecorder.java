package org.deviceconnect.android.deviceplugin.host.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MP4Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaSharing;
import org.deviceconnect.android.deviceplugin.host.recorder.util.SurfaceMP4Recorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

public abstract class AbstractMediaRecorder implements HostMediaRecorder {
    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * ファイルマネージャ.
     */
    private final FileManager mFileManager;

    /**
     * スクリーンキャスト管理クラス.
     */
    private final MediaProjectionProvider mMediaProjectionProvider;

    /**
     * ファイルを Android 端末内で共有するためのクラス.
     */
    private final MediaSharing mMediaSharing = MediaSharing.getInstance();

    /**
     * リクエストの処理を実行するハンドラ.
     */
    private final Handler mRequestHandler;

    /**
     * イベント通知用リスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * レコーダの状態.
     */
    private State mState = State.INACTIVE;

    /**
     * 録音・録画用クラス.
     */
    private MP4Recorder mMP4Recorder;

    /**
     * コンストラクタ.
     */
    public AbstractMediaRecorder(Context context, FileManager fileManager, MediaProjectionProvider provider) {
        mContext = context;
        mFileManager = fileManager;
        mMediaProjectionProvider = provider;

        HandlerThread requestThread = new HandlerThread("host-media-recorder");
        requestThread.start();
        mRequestHandler = new Handler(requestThread.getLooper());
    }

    // Implements HostMediaRecorder methods.

    @Override
    public void initialize() {
        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            broadcasterProvider.setOnEventListener(new LiveStreamingProvider.OnEventListener() {
                @Override
                public void onStarted(List<LiveStreaming> broadcasters) {
                    postOnBroadcasterStarted(broadcasters);
                }

                @Override
                public void onStopped() {
                    postOnBroadcasterStopped();
                }

                @Override
                public void onError(LiveStreaming broadcaster, Exception e) {
                    postOnBroadcasterError(broadcaster, e);
                }
            });
        }

        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            previewProvider.setOnEventListener(new LiveStreamingProvider.OnEventListener() {
                @Override
                public void onStarted(List<LiveStreaming> servers) {
                    postOnPreviewStarted(servers);
                }

                @Override
                public void onStopped() {
                    postOnPreviewStopped();
                }

                @Override
                public void onError(LiveStreaming server, Exception e) {
                    postOnPreviewError(e);
                }
            });
        }
    }

    @Override
    public void clean() {
        stopRecordingInternal(null);
    }

    @Override
    public void destroy() {
        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            broadcasterProvider.release();
        }

        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            previewProvider.release();
        }

        mRequestHandler.getLooper().quit();
    }

    @Override
    public State getState() {
        return mState;
    }

    @Override
    public boolean isPreviewRunning() {
        PreviewServerProvider provider = getServerProvider();
        return provider != null && provider.isRunning();
    }

    @Override
    public void onDisplayRotation(int rotation) {
        if (getSettings().getOrientation() == -1) {
            onConfigChange();
        }
    }

    @Override
    public void onConfigChange() {
        PreviewServerProvider previewServerProvider = getServerProvider();
        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();

        // 設定が変更された場合には、一度描画用のスレッドを停止しておく
        EGLSurfaceDrawingThread drawingThread = getSurfaceDrawingThread();
        if (drawingThread != null && drawingThread.isRunning()
                && (previewServerProvider.isRunning() || broadcasterProvider.isRunning())) {
            drawingThread.stop(true);
        }

        if (previewServerProvider != null) {
            previewServerProvider.onConfigChange();
        }

        if (broadcasterProvider != null) {
            broadcasterProvider.onConfigChange();
        }

        postOnConfigChanged();
    }

    @Override
    public List<LiveStreaming> startPreview() {
        PreviewServerProvider provider = getServerProvider();
        if (provider == null) {
            return new ArrayList<>();
        }

        List<LiveStreaming> servers = provider.start();
        if (!servers.isEmpty()) {
            provider.setMute(getSettings().isMute());
        }
        return servers;
    }

    @Override
    public void stopPreview() {
        PreviewServerProvider provider = getServerProvider();
        if (provider != null) {
            provider.stop();
        }
    }

    @Override
    public boolean isBroadcasterRunning() {
        BroadcasterProvider provider = getBroadcasterProvider();
        return provider != null && provider.isRunning();
    }

    @Override
    public List<LiveStreaming> startBroadcaster(String uri) {
        BroadcasterProvider provider = getBroadcasterProvider();
        if (provider == null) {
            return new ArrayList<>();
        }

        List<LiveStreaming> broadcasters = provider.start();
        for (LiveStreaming broadcaster : broadcasters) {
            broadcaster.setMute(getSettings().isMute());
        }
        return broadcasters;
    }

    @Override
    public void stopBroadcaster() {
        BroadcasterProvider provider = getBroadcasterProvider();
        if (provider != null) {
            provider.stop();
        }
    }

    @Override
    public void requestKeyFrame() {
        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            previewProvider.requestSyncFrame();
        }

        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            broadcasterProvider.requestSyncFrame();
        }
    }

    @Override
    public void setMute(boolean mute) {
        Settings settings = getSettings();
        settings.setMute(mute);

        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            previewProvider.setMute(mute);
        }

        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            broadcasterProvider.setMute(mute);
        }

        postOnMuteChanged(mute);
    }

    @Override
    public boolean isMute() {
        return getSettings().isMute();
    }

    @Override
    public void setSSLContext(SSLContext sslContext) {
        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            for (LiveStreaming server : previewProvider.getLiveStreamingList()) {
                if (server instanceof PreviewServer) {
                    ((PreviewServer) server).setSSLContext(sslContext);
                }
            }
        }
    }

    @Override
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public long getBPS() {
        long bps = 0;

        PreviewServerProvider previewProvider = getServerProvider();
        if (previewProvider != null) {
            List<LiveStreaming> servers = previewProvider.getLiveStreamingList();
            for (LiveStreaming streaming : servers) {
                bps += streaming.getBPS();
            }
        }

        BroadcasterProvider broadcasterProvider = getBroadcasterProvider();
        if (broadcasterProvider != null) {
            List<LiveStreaming> broadcasters = broadcasterProvider.getLiveStreamingList();
            for (LiveStreaming streaming : broadcasters) {
                bps += streaming.getBPS();
            }
        }

        return bps;
    }

    @Override
    public MediaProjectionProvider getMediaProjectionProvider() {
        return mMediaProjectionProvider;
    }

    // HostDeviceStreamRecorder

    @Override
    public String getStreamMimeType() {
        return "video/mp4";
    }

    @Override
    public void startRecording(final RecordingCallback listener) {
        if (getState() != State.INACTIVE) {
            if (listener != null) {
                listener.onFailed(this, "MediaRecorder is already recording.");
            }
        } else {
            postRequestHandler(() -> startRecordingInternal(listener));
        }
    }

    @Override
    public void stopRecording(final StoppingCallback listener) {
        if (getState() != State.RECORDING) {
            if (listener != null) {
                listener.onFailed(this, "MediaRecorder is already recording.");
            }
        } else {
            postRequestHandler(() -> stopRecordingInternal(listener));
        }
    }

    @Override
    public boolean canPauseRecording() {
        // 一時停止はサポートしない
        return false;
    }

    @Override
    public void pauseRecording() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeRecording() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void muteTrack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unMuteTrack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMutedTrack() {
        throw new UnsupportedOperationException();
    }

    /**
     * Runnable を順番に実行します.
     *
     * @param run 実行する Runnable
     */
    protected void postRequestHandler(Runnable run) {
        mRequestHandler.post(run);
    }

    /**
     * レコーダの状態を設定します.
     *
     * @param state レコーダの状態
     */
    protected void setState(State state) {
        mState = state;
    }

    /**
     * エンコーダを追加します.
     *
     * @param encoderId エンコーダID
     * @param encoderSettings エンコーダの設定
     */
    public void addEncoder(String encoderId, EncoderSettings encoderSettings) {
        getSettings().addEncoder(encoderId);

        switch (encoderSettings.getMimeType()) {
            case MJPEG:
            case RTSP:
            case SRT: {
                LiveStreaming streaming = getServerProvider().createLiveStreaming(encoderId, encoderSettings);
                if (streaming != null) {
                    getServerProvider().addLiveStreaming(streaming);
                }
            }   break;
            case RTMP: {
                LiveStreaming streaming = getBroadcasterProvider().createLiveStreaming(encoderId, encoderSettings);
                if (streaming != null) {
                    getBroadcasterProvider().addLiveStreaming(streaming);
                }
            }   break;
        }
    }

    /**
     * エンコーダを削除します.
     *
     * @param encoderId エンコーダ
     */
    public void removeEncoder(String encoderId) {
        if (getSettings().existEncoderId(encoderId)) {
            EncoderSettings encoderSettings = getSettings().getEncoderSetting(encoderId);
            switch (encoderSettings.getMimeType()) {
                case MJPEG:
                case RTSP:
                case SRT:
                    getServerProvider().removeLiveStreaming(encoderId);
                    break;
                case RTMP:
                    getBroadcasterProvider().removeLiveStreaming(encoderId);
                    break;
            }
            getSettings().removeEncoder(encoderId);
        }
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * ファイル管理クラスを取得します.
     *
     * @return ファイル管理クラス
     */
    public FileManager getFileManager() {
        return mFileManager;
    }

    /**
     * 指定されたパーミッションの要求を行います.
     *
     * @param permissions パーミッションのリスト
     * @param callback パーミッション要求の結果を通知するコールバック
     */
    protected void requestPermission(String[] permissions, PermissionCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        PermissionUtility.requestPermissions(getContext(), handler, permissions, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                callback.onAllowed();
            }

            @Override
            public void onFail(@NonNull String deniedPermission) {
                callback.onDisallowed();
            }
        });
    }

    /**
     * MediaProjection の許可要求を行います.
     *
     * @param callback 要求の結果を通知するコールバック
     */
    protected void requestMediaProjection(PermissionCallback callback) {
        getMediaProjectionProvider().requestPermission(new MediaProjectionProvider.Callback() {
            @Override
            public void onAllowed(MediaProjection mediaProjection) {
                callback.onAllowed();
            }

            @Override
            public void onDisallowed() {
                callback.onDisallowed();
            }
        });
    }

    /**
     * NotificationId を取得します.
     *
     * @return NotificationId
     */
    private int getNotificationId() {
        return 2000 + getId().hashCode();
    }

    /**
     * レコーディング停止用の Notification を削除します.
     *
     * @param id notification を識別する ID
     */
    private void hideNotification(String id) {
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(id, getNotificationId());
        }
    }

    /**
     * 録音・録画停止用の Notification を表示します.
     *
     * @param id notification を識別する ID
     * @param name 名前
     */
    private void showNotificationForStopRecording(String id, String name) {
        PendingIntent contentIntent = createPendingIntentForStopRecording(id);
        Notification notification = createNotificationForStopRecording(contentIntent, null, name);
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = mContext.getResources().getString(R.string.overlay_preview_channel_id);
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        mContext.getResources().getString(R.string.host_notification_recorder_recording),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(mContext.getResources().getString(R.string.host_notification_recorder_recording_content));
                manager.createNotificationChannel(channel);
                notification = createNotificationForStopRecording(contentIntent, channelId, name);
            }
            manager.notify(id, getNotificationId(), notification);
        }
    }

    /**
     * 録音・録画停止用の Notification を作成する.
     *
     * @param pendingIntent Notificationがクリックされたときに起動する Intent
     * @param channelId チャンネルID
     * @param name 名前
     * @return Notification
     */
    protected Notification createNotificationForStopRecording(final PendingIntent pendingIntent, final String channelId, String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(mContext.getString(R.string.host_notification_recorder_recording_ticker));
            builder.setSmallIcon(R.drawable.dconnect_icon);
            builder.setContentTitle(mContext.getString(R.string.host_notification_recorder_recording, name));
            builder.setContentText(mContext.getString(R.string.host_notification_recorder_recording_content));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            return builder.build();
        } else {
            Notification.Builder builder = new Notification.Builder(mContext.getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
            int iconType = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                    R.drawable.dconnect_icon : R.drawable.dconnect_icon_lollipop;
            builder.setSmallIcon(iconType);
            builder.setContentTitle(mContext.getString(R.string.host_notification_recorder_recording, name));
            builder.setContentText(mContext.getString(R.string.host_notification_recorder_recording_content));
            builder.setWhen(System.currentTimeMillis());
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId != null) {
                builder.setChannelId(channelId);
            }
            return builder.build();
        }
    }

    /**
     * PendingIntent を作成する.
     *
     * @param id カメラ ID
     *
     * @return PendingIntent
     */
    private PendingIntent createPendingIntentForStopRecording(String id) {
        Intent intent = new Intent();
        intent.setAction(HostMediaRecorderManager.ACTION_STOP_RECORDING);
        intent.putExtra(HostMediaRecorderManager.KEY_RECORDER_ID, id);
        return PendingIntent.getBroadcast(mContext, getNotificationId(), intent, 0);
    }

    /**
     * 写真を保存して、Android OS のメディアに登録します.
     *
     * @param filename ファイル名
     * @param jpeg 写真データ
     * @param listener 保存結果を通知するリスナー
     */
    protected void storePhoto(String filename, byte[] jpeg, OnPhotoEventListener listener) {
        mFileManager.saveFile(filename, jpeg, true, new FileManager.SaveFileCallback() {
            @Override
            public void onSuccess(@NonNull final String uri) {
                if (DEBUG) {
                    Log.d(TAG, "Saved photo: uri=" + uri);
                }

                String photoFilePath = mFileManager.getBasePath().getAbsolutePath() + "/" + uri;
                registerPhoto(new File(mFileManager.getBasePath(), filename));

                if (listener != null) {
                    listener.onTakePhoto(uri, photoFilePath, MIME_TYPE_JPEG);
                }

                postOnTakePhoto(uri, photoFilePath, MIME_TYPE_JPEG);
            }

            @Override
            public void onFail(@NonNull final Throwable e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to save photo", e);
                }

                if (listener != null) {
                    listener.onFailedTakePhoto(e.getMessage());
                }
            }
        });
    }

    /**
     * 映像ファイルを Android OS に登録します.
     *
     * @param videoFile 映像ファイル
     */
    protected void registerVideo(final File videoFile) {
        Uri uri = mMediaSharing.shareVideo(mContext, videoFile, mFileManager);
        if (DEBUG) {
            String filePath = videoFile.getAbsolutePath();
            if (uri != null) {
                Log.d(TAG, "Registered video: filePath=" + filePath + ", uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register video: file=" + filePath);
            }
        }
    }

    /**
     * 写真ファイルを Android OS に登録します.
     *
     * @param photoFile 写真ファイル
     */
    protected void registerPhoto(final File photoFile) {
        Uri uri = mMediaSharing.sharePhoto(mContext, photoFile);
        if (DEBUG) {
            if (uri != null) {
                Log.d(TAG, "Registered photo: uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register photo: file=" + photoFile.getAbsolutePath());
            }
        }
    }

    /**
     * 音声ファイルを Android OS に登録します.
     *
     * @param audioFile 音声ファイル
     */
    protected void registerAudio(final File audioFile) {
        Uri uri = mMediaSharing.shareAudio(mContext, audioFile);
        if (DEBUG) {
            String filePath = audioFile.getAbsolutePath();
            if (uri != null) {
                Log.d(TAG, "Registered audio: filePath=" + filePath + ", uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register audio: file=" + filePath);
            }
        }
    }

    protected void postOnMuteChanged(boolean mute) {
        if (mOnEventListener != null) {
            mOnEventListener.onMuteChanged(mute);
        }
    }

    protected void postOnConfigChanged() {
        if (mOnEventListener != null) {
            mOnEventListener.onConfigChanged();
        }
    }

    protected void postOnPreviewStarted(List<LiveStreaming> servers) {
        if (mOnEventListener != null) {
            mOnEventListener.onPreviewStarted(servers);
        }
    }

    protected void postOnPreviewStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onPreviewStopped();
        }
    }

    protected void postOnPreviewError(Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onPreviewError(e);
        }
    }

    protected void postOnBroadcasterStarted(List<LiveStreaming> broadcasters) {
        if (mOnEventListener != null) {
            mOnEventListener.onBroadcasterStarted(broadcasters);
        }
    }

    protected void postOnBroadcasterStopped() {
        if (mOnEventListener != null) {
            mOnEventListener.onBroadcasterStopped();
        }
    }

    protected void postOnBroadcasterError(LiveStreaming broadcaster, Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onBroadcasterError(broadcaster, e);
        }
    }

    protected void postOnTakePhoto(String uri, String filePath, String mimeType) {
        if (mOnEventListener != null) {
            mOnEventListener.onTakePhoto(uri, filePath, mimeType);
        }
    }

    protected void postOnRecordingStarted(String path) {
        if (mOnEventListener != null) {
            mOnEventListener.onRecordingStarted(path);
        }
    }

    protected void postOnRecordingStopped(String path) {
        if (mOnEventListener != null) {
            mOnEventListener.onRecordingStopped(path);
        }
    }

    protected void postOnRecordingPause() {
        if (mOnEventListener != null) {
            mOnEventListener.onRecordingPause();
        }
    }

    protected void postOnRecordingResume() {
        if (mOnEventListener != null) {
            mOnEventListener.onRecordingResume();
        }
    }

    protected void postOnError(Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    /**
     * 録音・録画用のクラスを作成します.
     *
     * @return 録音・録画用のクラス
     */
    protected abstract MP4Recorder createMP4Recorder();

    /**
     * 録画を行います.
     *
     * @param callback 録画開始結果を通知するリスナー
     */
    private void startRecordingInternal(final RecordingCallback callback) {
        if (mMP4Recorder != null) {
            if (callback != null) {
                callback.onFailed(this, "Recording has started already.");
            }
            return;
        }

        mMP4Recorder = createMP4Recorder();

        if (mMP4Recorder == null) {
            if (callback != null) {
                callback.onFailed(this, "Failed to start recording.");
            }
            return;
        }

        mMP4Recorder.start(new MP4Recorder.OnStartingCallback() {
            @Override
            public void onSuccess() {
                File outputFile = mMP4Recorder.getOutputFile();

                setState(State.RECORDING);

                showNotificationForStopRecording(getId(), getName());

                if (callback != null) {
                    callback.onRecorded(AbstractMediaRecorder.this, outputFile.getName());
                }

                postOnRecordingStarted(outputFile.getName());
            }

            @Override
            public void onFailure(Throwable e) {
                if (mMP4Recorder != null) {
                    mMP4Recorder.release();
                    mMP4Recorder = null;
                }

                setState(State.INACTIVE);

                if (callback != null) {
                    callback.onFailed(AbstractMediaRecorder.this,
                            "Failed to start recording because of camera problem: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 録画停止を行います.
     *
     * @param callback 録画停止結果を通知するリスナー
     */
    private void stopRecordingInternal(final StoppingCallback callback) {
        if (mMP4Recorder == null) {
            if (callback != null) {
                callback.onFailed(this, "Recording has stopped already.");
            }
            return;
        }

        setState(State.INACTIVE);

        hideNotification(getId());

        mMP4Recorder.stop(new MP4Recorder.OnStoppingCallback() {
            @Override
            public void onSuccess() {
                File outputFile = mMP4Recorder.getOutputFile();

                if (mMP4Recorder instanceof SurfaceMP4Recorder) {
                    registerVideo(outputFile);
                } else {
                    registerAudio(outputFile);
                }

                mMP4Recorder.release();
                mMP4Recorder = null;

                if (callback != null) {
                    callback.onStopped(AbstractMediaRecorder.this, outputFile.getName());
                }

                postOnRecordingStopped(outputFile.getName());
            }

            @Override
            public void onFailure(Throwable e) {
                if (mMP4Recorder != null) {
                    mMP4Recorder.release();
                    mMP4Recorder = null;
                }

                if (callback != null) {
                    callback.onFailed(AbstractMediaRecorder.this,
                            "Failed to stop recording for unexpected error: " + e.getMessage());
                }
            }
        });
    }
}
