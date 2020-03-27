package org.deviceconnect.opuscodec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class OpusTrack {
    private final static String TAG = "OpusTrack";
    private final static boolean DEBUG = BuildConfig.DEBUG;

    private WorkThread mWorkThread;

    private int mSamplingRate = 48000;
    private int mChannel = 1;
    private OpusDecoder.FrameSize mFrameSize = OpusConstants.FrameSize.E_20_MS;

    public void setSamplingRate(int samplingRate) {
        mSamplingRate = samplingRate;
    }

    public void setChannel(int channel) {
        mChannel = channel;
    }

    public void setFrameSize(OpusDecoder.FrameSize frameSize) {
        mFrameSize = frameSize;
    }

    public void start() {
        if (mWorkThread != null) {
            return;
        }
        mWorkThread = new WorkThread();
        mWorkThread.setName("OpusTrack");
        mWorkThread.start();
    }

    public void stop() {
        if (mWorkThread != null) {
            mWorkThread.terminate();
            mWorkThread = null;
        }
    }

    public void write(byte[] data, int dataLength) {
        if (mWorkThread != null) {
            mWorkThread.add(new Frame(data, dataLength));
        }
    }

    private class WorkThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * Opus デコーダ.
         */
        private OpusDecoder mOpusDecoder;

        /**
         * 音声を再生するための AudioTrack.
         */
        private AudioTrack mAudioTrack;

        /**
         * 一時的に音声データを格納するバッファ.
         */
        private short[] mAudioTempBuf;

        /**
         * Opus で圧縮された音声データのリスト.
         */
        private final Queue<Frame> mQueue = new LinkedList<>();

        /**
         * Opus で圧縮された音声データを追加します.
         *
         * @param data Opus で圧縮された音声データ
         */
        synchronized void add(Frame data) {
            mQueue.offer(data);
            notifyAll();
        }

        /**
         * Opus で圧縮された音声データを取得します.
         * <p>
         * 音声データが存在しない場合にはスレッドをブロックします。
         * </p>
         * @return Opus で圧縮された音声データ
         * @throws InterruptedException スレッドが停止された場合に発生
         */
        private synchronized Frame get() throws InterruptedException {
            while (mQueue.peek() == null) {
                wait();
            }
            return mQueue.remove();
        }

        /**
         * スレッドを停止します.
         */
        void terminate() {
            mStopFlag = true;

            interrupt();

            try {
                join(100);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            createAudioTrack();

            OpusDecoder.SamplingRate samplingRate = convertSamplingRate(mSamplingRate);
            try {
                mOpusDecoder = new OpusDecoder(samplingRate, mChannel, mFrameSize);
            } catch (Exception e) {
                return;
            }

            mAudioTempBuf = mOpusDecoder.bufferAllocate();

            while (!mStopFlag) {
                try {
                    decode(get());
                } catch (InterruptedException e) {
                    break;
                } catch (NativeInterfaceException e) {
                    break;
                } catch (Exception e) {
                    // ignore.
                }
            }

            releaseAudioTrack();

            try {
                mOpusDecoder.release();
            } catch (Exception e) {
                // ignore.
            }
        }

        private void decode(Frame frame) throws NativeInterfaceException {
            int size = mOpusDecoder.decode(frame.getData(), frame.getDataLength(), mAudioTempBuf);
            if (mAudioTrack != null && size > 0) {
                mAudioTrack.write(mAudioTempBuf, 0, size);
            }
        }

        /**
         * 指定されたサンプリングレートとチャンネル数で AudioTrack を作成します.
         */
        private void createAudioTrack() {
            int bufSize = AudioTrack.getMinBufferSize(mSamplingRate,
                    mChannel == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT) * 2;

            if (DEBUG) {
                Log.d(TAG, "OpusTrack::createAudioTrack");
                Log.d(TAG, "  SamplingRate: " + mSamplingRate);
                Log.d(TAG, "  Channels: " + mChannel);
                Log.d(TAG, "  AudioFormat: " + AudioFormat.ENCODING_PCM_16BIT);
                Log.d(TAG, "  BufSize: " + bufSize);
            }

            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    mSamplingRate,
                    mChannel == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, bufSize,
                    AudioTrack.MODE_STREAM);

            mAudioTrack.play();
        }

        /**
         * AudioTrack を破棄します.
         */
        private void releaseAudioTrack() {
            if (mAudioTrack != null) {
                try {
                    mAudioTrack.stop();
                } catch (IllegalStateException e) {
                    // ignore.
                }

                try {
                    mAudioTrack.release();
                } catch (Exception e) {
                    // ignore.
                }

                mAudioTrack = null;
            }
        }
    }

    private static OpusDecoder.SamplingRate convertSamplingRate(Integer samplingRate) {
        switch(samplingRate) {
            case 8000:
                return OpusDecoder.SamplingRate.E_8K;
            case 12000:
                return OpusDecoder.SamplingRate.E_12K;
            case 16000:
                return OpusDecoder.SamplingRate.E_16K;
            case 24000:
                return OpusDecoder.SamplingRate.E_24K;
            case 48000:
                return OpusDecoder.SamplingRate.E_48K;
            default:
                throw new IllegalArgumentException("sampling rate illegal");
        }
    }

    private class Frame {
        private byte[] mData;
        private int mDataLength;

        Frame(byte[] data, int dataLength) {
            mData = data;
            mDataLength = dataLength;
        }

        byte[] getData() {
            return mData;
        }

        int getDataLength() {
            return mDataLength;
        }
    }
}
