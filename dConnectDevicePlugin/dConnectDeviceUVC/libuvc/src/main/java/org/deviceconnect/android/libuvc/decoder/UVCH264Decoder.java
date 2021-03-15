/*
 UVCH264Decoder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc.decoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libuvc.BuildConfig;
import org.deviceconnect.android.libuvc.Frame;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.libuvc.utils.QueueThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * H264をデコードしてSurfaceに描画するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class UVCH264Decoder implements UVCDecoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "UVC";

    /**
     * H.264 のマイムタイプを定義.
     */
    private static final String MIME_TYPE_H264 = "video/avc";

    /**
     * UVCパラメータ.
     */
    private Parameter mParameter;

    /**
     * デコードを行うMediaCodec.
     */
    private MediaCodec mMediaCodec;

    /**
     * SPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd0;

    /**
     * PPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd1;

    /**
     * デコード処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    /**
     * 描画先のSurface.
     */
    private Surface mSurface;

    /**
     * イベント通知用のリスナー.
     */
    private OnEventListener mOnEventListener;

    @Override
    public void setSurface(final Surface surface) {
        mSurface = surface;
    }

    @Override
    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public void onInit(final UVCCamera uvcCamera, final Parameter parameter) {
        if (mWorkThread != null) {
            mWorkThread.close();
            mWorkThread = null;
        }

        mParameter = parameter;
        mCsd0 = null;
        mCsd1 = null;
    }

    @Override
    public void onReceivedFrame(final Frame frame) {
        if (mWorkThread != null) {
            mWorkThread.add(frame);
        } else if (searchSPSandPPS(frame)) {
            try {
                startMediaCodec();
            } catch (Exception e) {
                mCsd0 = null;
                mCsd1 = null;
            }
        } else {
            frame.release();
        }
    }

    @Override
    public void onRelease() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
        }

        if (mWorkThread != null) {
            mWorkThread.close();
            mWorkThread = null;
        }
    }

    /**
     * エラー通知を行う.
     * @param e 例外
     */
    private void postError(final Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    /**
     * SPSとPPSのパケットを探して、csd-0とcsd-1のデータを作成します.
     *
     * @param frame フレームバッファ
     * @return SPSとPPSが作成できた場合はtrue、それ以外はfalse
     */
    private boolean searchSPSandPPS(final Frame frame) {
        byte[] d = frame.getBuffer();
        int length = frame.getLength();

        int startPos = 0;
        int count = 0;
        for (int i = 0; i < frame.getLength() - 4; i++) {
            if (d[i] == 0x00 && d[i + 1] == 0x00 && d[i + 2] == 0x00 && d[i + 3] == 0x01) {
                if (count > 0) {
                    int type = d[startPos + 4] & 0x1F;
                    switch (type) {
                        case 7: // SPS
                            createCSD0(d, startPos, i - startPos);
                            break;
                        case 8: // PPS
                            createCSD1(d, startPos, i - startPos);
                            break;
                    }
                }
                startPos = i;
                count++;
            }
        }

        if (length - startPos > 0) {
            int type = d[startPos + 4] & 0x1F;
            switch (type) {
                case 7: // SPS
                    createCSD0(d, startPos, length - startPos);
                    break;
                case 8: // PPS
                    createCSD1(d, startPos, length - startPos);
                    break;
                default:
                    break;
            }
        }

        frame.release();

        return mCsd0 != null && mCsd1 != null;
    }

    /**
     * csd-0 のデータを作成します.
     *
     * @param data データが格納されたフレームバッファ
     * @param pos データ開始位置
     * @param length データサイズ
     */
    private void createCSD0(final byte[] data, int pos, int length) {
        if (DEBUG) {
            StringBuilder t = new StringBuilder("csd-0: ");
            for (int i = 0; i < length; i++) {
                t.append(String.format("%02X", data[pos + i]));
            }
            Log.d(TAG, t.toString());
            Log.d(TAG, "pos: " + pos + ", length: " + length);
        }
        mCsd0 = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
        mCsd0.put(data, pos, length);
        mCsd0.flip();
    }

    /**
     * csd-1のデータを作成します.
     *
     * @param data データが格納されたフレームバッファ
     * @param pos データ開始位置
     * @param length データサイズ
     */
    private void createCSD1(final byte[] data, int pos, int length) {
        if (DEBUG) {
            StringBuilder t = new StringBuilder("csd-1: ");
            for (int i = 0; i < length; i++) {
                t.append(String.format("%02X", data[pos + i]));
            }
            Log.d(TAG, t.toString());
            Log.d(TAG, "pos: " + pos + ", length: " + length);
        }
        mCsd1 = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
        mCsd1.put(data, pos, length);
        mCsd1.flip();
    }

    /**
     * MediaCodec でエンコードを行うためのスレッドを開始します.
     */
    private void startMediaCodec() {
        mWorkThread = new WorkThread();
        mWorkThread.setName("UVC-H264-Decode-Thread");
        mWorkThread.start();
    }

    /**
     * 指定されたエンコードがハードウェアか確認します.
     *
     * @param info エンコーダ情報
     * @return ハードウェアエンコーダの場合は true、それ以外の場合は false
     */
    private boolean isHardware(MediaCodecInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return info.isHardwareAccelerated();
        } else {
            String name = info.getName();
            return name.startsWith("OMX.qcom.") || name.startsWith("OMX.Exynos.");
        }
    }

    /**
     * エンコーダの名前を取得します.
     *
     * @return エンコーダ名
     */
    private String getMediaCodecName() {
        String name = null;
        int cnt = MediaCodecList.getCodecCount();
        for (int i = 0; i < cnt; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (info.isEncoder()) {
                continue;
            }

            if (!isHardware(info)) {
                for (String type : info.getSupportedTypes()) {
                    if (type.equals(MIME_TYPE_H264)) {
                        name = info.getName();
                    }
                }
            }
        }
        return name;
    }

    /**
     * エンコーダの情報をログに出力します.
     */
    private void printMediaCodecInfo() {
        int cnt = MediaCodecList.getCodecCount();
        for (int i = 0; i < cnt; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (info.isEncoder()) {
                continue;
            }
            Log.i(TAG, "CODEC[" + i + "] " + info.getName());
            for (String type : info.getSupportedTypes()) {
                Log.i(TAG, "     mimeType: " + type);
            }
        }
    }

    /**
     * 映像のサイズ変更を通知します.
     *
     * @param width 横幅
     * @param height 縦幅
     */
    void postSizeChanged(int width, int height) {
    }

    /**
     * MediaCodecを作成します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @param useSoftwareDecoder ソフトウェアデコーダ使用
     * @throws IOException MediaCodecの作成に失敗した場合に発生
     */
    private void createMediaCodec(final int width, final int height, final boolean useSoftwareDecoder) throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE_H264, width, height);
        format.setByteBuffer("csd-0", mCsd0);
        format.setByteBuffer("csd-1", mCsd1);

        if (DEBUG) {
            Log.i(TAG, "createMediaCodec(width: " + width + ", height: " + height + ")");
            printMediaCodecInfo();
        }

        if (useSoftwareDecoder) {
            String name = getMediaCodecName();
            if (name == null) {
                throw new IOException("Not support a software decoder.");
            }
            mMediaCodec = MediaCodec.createByCodecName(name);
        } else {
            mMediaCodec = MediaCodec.createDecoderByType("video/avc");
        }
        mMediaCodec.configure(format, mSurface, null, 0);
        mMediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mMediaCodec.start();
    }

    /**
     * UVCから送られてきたデータをMediaCodecに渡してデコードを行うスレッド.
     */
    private class WorkThread extends QueueThread<Frame> {
        /**
         * ソフトウェア使用フラグ.
         */
        private boolean mUseSoftwareDecoder = false;

        @Override
        public void close() {
            stopMediaCodec();
            super.close();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    createMediaCodec(mParameter.getWidth(), mParameter.getHeight(), mUseSoftwareDecoder);
                    runMediaCodec();
                } catch (InterruptedException e) {
                    if (DEBUG) {
                        Log.w(TAG, "InterruptedException.", e);
                    }
                    return;
                } catch (Exception e) {
                    if (!mUseSoftwareDecoder) {
                        stopMediaCodec();

                        // ハードウェアデコーダで失敗した場合は、
                        // ソフトウェアデコーダを使用して もう一度エンコードを行う.
                        mUseSoftwareDecoder = true;
                    } else {
                        if (DEBUG) {
                            Log.e(TAG, "H264 encode occurred an exception.", e);
                        }
                        postError(e);
                        return;
                    }
                }
            }
        }

        private void stopMediaCodec() {
            if (mMediaCodec != null) {
                try {
                    mMediaCodec.stop();
                } catch (Exception e) {
                    // ignore.
                }

                try {
                    mMediaCodec.release();
                } catch (Exception e) {
                    // ignore.
                }

                mMediaCodec = null;
            }
        }

        private void runMediaCodec() throws InterruptedException {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();

            while (!isInterrupted()) {
                Frame frame = get();

                int inIndex = mMediaCodec.dequeueInputBuffer(10000);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    buffer.clear();
                    buffer.put(frame.getBuffer(), 0, frame.getLength());
                    buffer.flip();

                    mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), 0, 0);
                }

                frame.release();

                int outIndex = mMediaCodec.dequeueOutputBuffer(info, 10000);
                if (outIndex >= 0) {
                    mMediaCodec.releaseOutputBuffer(outIndex, true);
                } else {
                    switch (outIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            if (DEBUG) {
                                Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                                Log.d(TAG, "New format " + mMediaCodec.getOutputFormat());
                            }
                            MediaFormat mf = mMediaCodec.getOutputFormat();
                            int w = mf.getInteger(MediaFormat.KEY_WIDTH);
                            int h = mf.getInteger(MediaFormat.KEY_HEIGHT);
                            postSizeChanged(w, h);
                            break;

                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                        default:
                            break;
                    }
                }
            }
        }
    }
}
