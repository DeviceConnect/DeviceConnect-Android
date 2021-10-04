package org.deviceconnect.android.deviceplugin.host.recorder;

import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MovingRectThread;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.filter.HighPassFilter;
import org.deviceconnect.android.libmedia.streaming.audio.filter.LowPassFilter;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import javax.net.ssl.SSLContext;

public abstract class AbstractLiveStreaming implements LiveStreaming, CropInterface {
    /**
     * カメラを操作するレコーダ.
     */
    private final HostMediaRecorder mRecorder;

    /**
     * SSLContext のインスタンス.
     */
    private SSLContext mSSLContext;

    /**
     * ミュート設定.
     */
    private boolean mMute;

    /**
     * プレビュー配信サーバID.
     */
    private final String mId;

    /**
     * 切り抜き範囲移動用スレッド.
     */
    private MovingRectThread mMovingRectThread;

    /**
     * 切り抜き範囲移動用スレッドからのイベントを受け取るリスナー.
     */
    private final MovingRectThread.OnEventListener mMovingRectThreadOnEventListener = this::onUpdateCropRect;

    /**
     * 切り抜き範囲のイベントを通知するリスナー.
     */
    private final WeakReferenceList<CropInterface.OnEventListener> mOnEventListeners = new WeakReferenceList<>();

    protected void onUpdateCropRect(Rect rect) {
        VideoQuality videoQuality = getVideoQuality();
        if (videoQuality != null) {
            videoQuality.setCropRect(new Rect(rect));
        }
        getEncoderSettings().setCropRect(rect);
        postOnMoved(rect);
    }

    public AbstractLiveStreaming(HostMediaRecorder recorder, String id) {
        mRecorder = recorder;
        mId = id;
        mMute = true;
        startMovingRectThread();
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public void setOnEventListener(LiveStreaming.OnEventListener listener) {
    }

    @Override
    public void onConfigChange() {
        VideoEncoder videoEncoder = getVideoEncoder();
        if (videoEncoder != null) {
            setVideoQuality(videoEncoder.getVideoQuality());
            videoEncoder.restart();
        }

        AudioQuality audioQuality = getAudioQuality();
        if (audioQuality != null) {
            setAudioQuality(audioQuality);

            HostMediaRecorder.Settings settings = getRecorder().getSettings();
            setMute(settings.isMute());
        }
    }

    @Override
    public void release() {
        stopMovingRectThread();
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public void setMute(boolean mute) {
        mMute = mute;
    }

    @Override
    public boolean isMuted() {
        return mMute;
    }

    @Override
    public boolean requestSyncFrame() {
        VideoEncoder videoEncoder = getVideoEncoder();
        if (videoEncoder != null) {
            videoEncoder.requestSyncKeyFrame();
            return true;
        }
        return false;
    }

    @Override
    public long getBPS() {
        return 0;
    }

    @Override
    public boolean useSSLContext() {
        return getEncoderSettings().isUseSSL();
    }

    @Override
    public void setSSLContext(final SSLContext sslContext) {
        mSSLContext = sslContext;
    }

    @Override
    public SSLContext getSSLContext() {
        return mSSLContext;
    }

    // CropInterface implements

    @Override
    public String getName() {
        return getEncoderSettings().getName();
    }

    @Override
    public void moveCropRect(Rect start, Rect end, int duration) {
        checkCropRect(end);

        if (end == null || mMovingRectThread == null) {
            setCropRectInternal(end);
        } else {
            mMovingRectThread.move(start, end, duration);
        }
    }

    @Override
    public void setCropRect(Rect rect) {
        checkCropRect(rect);

        if (rect == null || mMovingRectThread == null) {
            setCropRectInternal(rect);
        } else {
            mMovingRectThread.set(rect);
        }
    }

    @Override
    public Rect getCropRect() {
        return getEncoderSettings().getCropRect();
    }

    @Override
    public void addOnEventListener(CropInterface.OnEventListener listener) {
        mOnEventListeners.add(listener);
    }

    @Override
    public void removeOnEventListener(CropInterface.OnEventListener listener) {
        mOnEventListeners.remove(listener);
    }

    private void checkCropRect(Rect rect) {
        if (rect != null) {
            if (getEncoderSettings().getCropRect() == null) {
                postOnAdded(rect);
            }
        } else {
            postOnRemove();
        }
    }

    private void setCropRectInternal(Rect rect) {
        getEncoderSettings().setCropRect(rect);

        VideoQuality videoQuality = getVideoQuality();
        if (videoQuality != null) {
            videoQuality.setCropRect(rect);
        }
    }

    private void postOnAdded(Rect rect) {
        for (CropInterface.OnEventListener l : mOnEventListeners.get()) {
            l.onAdded(this, rect);
        }
    }

    private void postOnRemove() {
        for (CropInterface.OnEventListener l : mOnEventListeners.get()) {
            l.onRemoved(this);
        }
    }

    private void postOnMoved(Rect rect) {
        for (CropInterface.OnEventListener l : mOnEventListeners.get()) {
            l.onMoved(this, rect);
        }
    }

    private void startMovingRectThread() {
        if (mMovingRectThread != null) {
            return;
        }

        mMovingRectThread = new MovingRectThread();
        mMovingRectThread.addOnEventListener(mMovingRectThreadOnEventListener);
        mMovingRectThread.start();
    }

    private void stopMovingRectThread() {
        if (mMovingRectThread != null) {
            mMovingRectThread.stop();
            mMovingRectThread = null;
        }
    }

    /**
     * プレビューを表示するレコーダー.
     *
     * @return レコーダー
     */
    public HostMediaRecorder getRecorder() {
        return mRecorder;
    }

    /**
     * Broadcaster の設定を取得します.
     *
     * @return サーバの設定
     */
    public HostMediaRecorder.EncoderSettings getEncoderSettings() {
        return mRecorder.getSettings().getEncoderSetting(mId);
    }

    /**
     * 映像のエンコーダを取得します.
     *
     * 映像が使用されていない場合は null を返却すること。
     *
     * @return 映像のエンコーダ
     */
    protected VideoEncoder getVideoEncoder() {
        return null;
    }

    /**
     * 音声のエンコーダを取得します.
     *
     * 音声が使用されていない場合は null を返却すること。
     *
     * @return 音声のエンコーダ
     */
    protected AudioEncoder getAudioEncoder() {
        return null;
    }

    /**
     * 映像の設定を取得します.
     *
     * 映像が使用されていない場合は null を返却すること。
     *
     * @return 映像の設定
     */
    protected VideoQuality getVideoQuality() {
        VideoEncoder videoEncoder = getVideoEncoder();
        if (videoEncoder != null) {
            return videoEncoder.getVideoQuality();
        }
        return null;
    }

    /**
     * 音声の設定を取得します.
     *
     * 音声が使用されていない場合は null を返却すること。
     *
     * @return 音声の設定
     */
    protected AudioQuality getAudioQuality() {
        AudioEncoder audioEncoder = getAudioEncoder();
        if (audioEncoder != null) {
            return audioEncoder.getAudioQuality();
        }
        return null;
    }

    /**
     * VideoEncoder の設定に、HostMediaRecorder の設定を反映します.
     *
     * @param videoQuality 設定を行う VideoEncoder の VideoQuality
     */
    public void setVideoQuality(VideoQuality videoQuality) {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.EncoderSettings settings = getEncoderSettings();

        EGLSurfaceDrawingThread d = recorder.getSurfaceDrawingThread();
        Size previewSize = settings.getPreviewSize();
        int w = d.isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
        int h = d.isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
        videoQuality.setVideoWidth(w);
        videoQuality.setVideoHeight(h);
        videoQuality.setCropRect(settings.getCropRect());
        videoQuality.setBitRate(settings.getPreviewBitRate());
        videoQuality.setFrameRate(settings.getPreviewMaxFrameRate());
        videoQuality.setIFrameInterval(settings.getPreviewKeyFrameInterval());
        videoQuality.setUseSoftwareEncoder(settings.isUseSoftwareEncoder());
        videoQuality.setIntraRefresh(settings.getIntraRefresh());
        videoQuality.setProfile(settings.getProfile());
        videoQuality.setLevel(settings.getLevel());
        if (settings.getPreviewBitRateMode() != null) {
            switch (settings.getPreviewBitRateMode()) {
                default:
                case VBR:
                    videoQuality.setBitRateMode(VideoQuality.BitRateMode.VBR);
                    break;
                case CBR:
                    videoQuality.setBitRateMode(VideoQuality.BitRateMode.CBR);
                    break;
                case CQ:
                    videoQuality.setBitRateMode(VideoQuality.BitRateMode.CQ);
                    break;
            }
        } else {
            videoQuality.setBitRateMode(null);
        }
    }

    /**
     * AudioEncoder の設定に、HostMediaRecorder の設定を反映します.
     *
     * @param audioQuality 設定を行う AudioEncoder の AudioQuality
     */
    public void setAudioQuality(AudioQuality audioQuality) {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();

        mMute = settings.isMute();
        audioQuality.setChannel(settings.getPreviewChannel() == 1 ?
                AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
        audioQuality.setSamplingRate(settings.getPreviewSampleRate());
        audioQuality.setBitRate(settings.getPreviewAudioBitRate());
        audioQuality.setUseAEC(settings.isUseAEC());

        if (settings.getAudioFilter() != null) {
            float coeff = settings.getAudioCoefficient();
            switch (settings.getAudioFilter()) {
                case LOW_PASS:
                    audioQuality.setFilter(new LowPassFilter(audioQuality, coeff));
                    break;
                case HIGH_PASS:
                    audioQuality.setFilter(new HighPassFilter(audioQuality, coeff));
                    break;
                default:
                    audioQuality.setFilter(null);
                    break;
            }
        } else {
            audioQuality.setFilter(null);
        }

        MicAudioQuality quality = (MicAudioQuality) audioQuality;
        if (settings.getPreviewAudioSource() == HostMediaRecorder.AudioSource.APP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // アプリの録音機能
                MediaProjectionProvider provider = recorder.getMediaProjectionProvider();
                if (provider != null && provider.getMediaProjection() != null) {
                    MediaProjection mediaProjection = provider.getMediaProjection();
                    AudioPlaybackCaptureConfiguration configuration =
                            new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                                    .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                                    .build();
                    quality.setCaptureConfig(configuration);
                }
            }
            quality.setSource(MicAudioQuality.Source.APP);
        }
    }
}
