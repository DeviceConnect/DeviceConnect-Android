package org.deviceconnect.android.libmedia.streaming.rtsp.player;

import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspClient;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspClientException;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspResponse;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Decoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.DecoderFactory;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.audio.AACLATMDecoderFactory;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.audio.AudioDecoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video.H264DecoderFactory;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video.H265DecoderFactory;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video.VideoDecoder;
import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.SessionDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RtspPlayer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "RTSP-PLAYER";

    /**
     * RTSP サーバと通信を行うクライアント.
     */
    private RtspClient mRtspClient;

    /**
     * RTSP サーバからの映像を描画する Surface.
     */
    private Surface mSurface;

    /**
     * RTSP サーバの URL.
     */
    private String mUrl;

    /**
     * RTSP サーバへの接続リトライ回数.
     */
    private int mRetryCount;

    /**
     * RTSP プレイヤーのイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * ミュート設定.
     */
    private boolean mMute;

    /**
     * デコーダを保持するマップ.
     */
    private final Map<MediaDescription, Decoder> mDecoderMap = new LinkedHashMap<>();

    /**
     * 映像用のデコーダを作成するファクトリークラスを保持するマップ.
     */
    private final Map<String, DecoderFactory> mVideoFactory = new HashMap<>();

    /**
     * 音声用のデコーダを作成するファクトリークラスを保持するマップ.
     */
    private final Map<String, DecoderFactory> mAudioFactory = new HashMap<>();

    /**
     * コンストラクタ.
     *
     * @param url RTSP サーバへのURL
     */
    public RtspPlayer(String url) {
        if (url == null) {
            throw new IllegalArgumentException("url is null.");
        }

        mUrl = url;

        addVideoFactory("H264", new H264DecoderFactory());
        addVideoFactory("H265", new H265DecoderFactory());
        addAudioFactory("mpeg4-generic", new AACLATMDecoderFactory());
    }

    /**
     * ミュート設定を取得します.
     *
     * @return ミュートの場合はtrue、それ以外はfalse
     */
    public boolean isMute() {
        return mMute;
    }

    /**
     * ミュートを設定します.
     *
     * @param mute ミュートの場合はtrue、それ以外はfalse
     */
    public void setMute(boolean mute) {
        mMute = mute;

        for (Decoder decoder : mDecoderMap.values()) {
            if (decoder instanceof AudioDecoder) {
                ((AudioDecoder) decoder).setMute(mute);
            }
        }
    }

    /**
     * イベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * 映像を描画する先の Surface を設定します.
     *
     * @param surface 描画を行う Surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    /**
     * 映像用のデコーダーを登録します.
     *
     * @param name デコーダー名
     * @param factory デコーダーを作成するファクトリークラス
     */
    public void addVideoFactory(String name, DecoderFactory factory) {
        mVideoFactory.put(name.toLowerCase(), factory);
    }

    /**
     * 音声用のデコーダーを登録します.
     *
     * @param name デコーダー名
     * @param factory デコーダーを作成するファクトリークラス
     */
    public void addAudioFactory(String name, DecoderFactory factory) {
        mAudioFactory.put(name.toLowerCase(), factory);
    }

    /**
     * RTSP の再生を開始します.
     */
    public synchronized void start() {
        if (mRtspClient != null) {
            if (DEBUG) {
                Log.e(TAG, "RtspPlayer is already running.");
            }
            return;
        }

        mRetryCount = 0;
        mRtspClient = new RtspClient(mUrl);
        mRtspClient.setOnEventListener(new RtspClient.OnEventListener() {
            @Override
            public void onConnected() {
                if (mOnEventListener != null) {
                    mOnEventListener.onConnected();
                }
            }

            @Override
            public void onDisconnected() {
                if (mOnEventListener != null) {
                    mOnEventListener.onDisconnected();
                }
            }

            @Override
            public void onError(RtspClientException e) {
                if (DEBUG) {
                    Log.e(TAG, "RtspClient::onError: " + e.getStatus());
                }

                // RTSP サーバ側でのエラーの場合にはリトライを行う
                if (e.getStatus() == RtspResponse.Status.STATUS_INTERNAL_SERVER_ERROR) {
                    mRetryCount++;
                    if (mRetryCount < 3) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e1) {
                                // ignore.
                            }
                            stop();
                            start();
                        }).start();
                    } else {
                        if (mOnEventListener != null) {
                            mOnEventListener.onError(e);
                        }
                    }
                } else {
                    if (mOnEventListener != null) {
                        mOnEventListener.onError(e);
                    }
                }
            }

            @Override
            public void onSdpReceived(SessionDescription sdp) {
                if (DEBUG) {
                    Log.d(TAG, sdp.toString());
                }

                for (MediaDescription md : sdp.getMediaDescriptions()) {
                    Decoder decoder = createDecoder(md);
                    if (decoder != null) {
                        mDecoderMap.put(md, decoder);
                    }
                }

                if (mOnEventListener != null) {
                    mOnEventListener.onReady();
                }
            }

            @Override
            public void onRtpReceived(MediaDescription md, byte[] data, int dataLength) {
                Decoder decoder = mDecoderMap.get(md);
                if (decoder != null) {
                    decoder.onRtpReceived(md, data, dataLength);
                }
            }

            @Override
            public void onRtcpReceived(MediaDescription md, byte[] data, int dataLength) {
                Decoder decoder = mDecoderMap.get(md);
                if (decoder != null) {
                    decoder.onRtcpReceived(md, data, dataLength);
                }
            }
        });
        mRtspClient.start();
    }

    /**
     * RTSP の再生を停止します.
     */
    public synchronized void stop() {
        if (mRtspClient != null) {
            mRtspClient.stop();
            mRtspClient = null;
        }

        for (Decoder decoder : mDecoderMap.values()) {
            decoder.onRelease();
        }
        mDecoderMap.clear();
    }

    /**
     * デコーダを作成します.
     *
     * @param md MediaDescription
     * @return デコーダ
     */
    private Decoder createDecoder(MediaDescription md) {
        if ("video".equalsIgnoreCase(md.getMedia())) {
            VideoDecoder decoder = createVideoDecoder(md);
            if (decoder != null) {
                decoder.setSurface(mSurface);
                decoder.setErrorCallback((e) -> {
                    if (mOnEventListener != null) {
                        mOnEventListener.onError(e);
                    }
                });
                decoder.setEventCallback(((width, height) -> {
                    if (mOnEventListener != null) {
                        mOnEventListener.onSizeChanged(width, height);
                    }
                }));
                decoder.onInit(md);
                return decoder;
            } else {
                if (DEBUG) {
                    Log.w(TAG, "Not supported. format=" + md.toString());
                }
            }
        } else if ("audio".equalsIgnoreCase(md.getMedia())) {
            AudioDecoder decoder = createAudioDecoder(md);
            if (decoder != null) {
                decoder.setErrorCallback((e) -> {
                    if (mOnEventListener != null) {
                        mOnEventListener.onError(e);
                    }
                });
                decoder.setMute(mMute);
                decoder.onInit(md);
                return decoder;
            } else {
                if (DEBUG) {
                    Log.w(TAG, "Not supported. format=" + md.toString());
                }
            }
        } else {
            if (DEBUG) {
                Log.w(TAG, "Unknown media type. media=" + md.getMedia());
            }
        }
        return null;
    }

    /**
     * メディア情報から映像用のデコーダーを作成します.
     * <p>
     * デコーダーが作成できなかった場合には null を返却します。
     * </p>
     * @param md メディア情報
     * @return デコーダー
     */
    private VideoDecoder createVideoDecoder(MediaDescription md) {
        String name = getEncodingName(md);
        if (name == null) {
            return null;
        }

        DecoderFactory factory = mVideoFactory.get(name.toLowerCase());
        if (factory != null) {
            return (VideoDecoder) factory.createDecoder();
        }
        return null;
    }

    /**
     * メディア情報から音声用のデコーダを作成します.
     * <p>
     * デコーダーが作成できなかった場合には null を返却します。
     * </p>
     * @param md メディア情報
     * @return デコーダー
     */
    private AudioDecoder createAudioDecoder(MediaDescription md) {
        String name = getEncodingName(md);
        if (name == null) {
            return null;
        }

        DecoderFactory factory = mAudioFactory.get(name.toLowerCase());
        if (factory != null) {
            return (AudioDecoder) factory.createDecoder();
        }
        return null;
    }

    /**
     * メディア情報からエンコーダーの名前を取得します.
     *
     * @param md メディア情報
     * @return エンコーダ名
     */
    private String getEncodingName(MediaDescription md) {
        for (Attribute attribute : md.getAttributes()) {
            if (attribute instanceof RtpMapAttribute) {
                RtpMapAttribute rtpMapAttribute = (RtpMapAttribute) attribute;
                return rtpMapAttribute.getEncodingName();
            }
        }
        return null;
    }

    public interface OnEventListener {
        /**
         * RTSP サーバの接続したことを通知します.
         */
        void onConnected();

        /**
         * RTSP サーバから切断したことを通知します.
         */
        void onDisconnected();

        /**
         * RTSP の受信準備が完了したことを通知します.
         */
        void onReady();

        /**
         * 映像の解像度が変更されたことを通知します.
         *
         * @param width 横幅
         * @param height 縦幅
         */
        void onSizeChanged(int width, int height);

        /**
         * RTSP プレイヤーでエラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Exception e);
    }
}
