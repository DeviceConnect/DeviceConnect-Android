package org.deviceconnect.android.libmedia.streaming.audio.filter;

import android.media.AudioFormat;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class BaseFilter implements Filter {
    private final AudioQuality mAudioQuality;
    private Transformation[] mFilter;
    protected float mCoeff;

    public BaseFilter(AudioQuality audioQuality, float coeff) {
        mAudioQuality = audioQuality;
        mCoeff = coeff;
        prepare();
    }

    private void prepare() {
        switch (mAudioQuality.getChannel()) {
            case AudioFormat.CHANNEL_IN_MONO:
                mFilter = new Transformation[1];
                mFilter[0] = createTransformation();
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                mFilter = new Transformation[2];
                mFilter[0] = createTransformation();
                mFilter[1] = createTransformation();
                break;
            default:
                throw new RuntimeException("Only supports monaural and stereo.");
        }
    }

    private Transformation get(int index) {
        switch (mAudioQuality.getChannel()) {
            case AudioFormat.CHANNEL_IN_MONO:
                return mFilter[0];
            case AudioFormat.CHANNEL_IN_STEREO:
                return mFilter[index % 2];
        }
        throw new RuntimeException("Only supports monaural and stereo.");
    }

    protected abstract Transformation createTransformation();

    @Override
    public void onProcessing(ByteBuffer src, int len) {
        Transformation transformation;
        switch (mAudioQuality.getFormat()) {
            case AudioFormat.ENCODING_PCM_16BIT: {
                ShortBuffer srcA = src.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                for (int i = 0; i < len / 2; i++) {
                    transformation = get(i);
                    transformation.pushIn(srcA.get());
                    float y = transformation.calc();
                    transformation.pushOut(y);
                    srcA.put(i, (short) y);
                }
            }   break;
            case AudioFormat.ENCODING_PCM_8BIT: {
                for (int i = 0; i < len; i++) {
                    transformation = get(i);
                    transformation.pushIn(src.get());
                    float y = transformation.calc();
                    transformation.pushOut(y);
                    src.put(i, (byte) y);
                }
            }   break;
            case AudioFormat.ENCODING_PCM_FLOAT: {
                FloatBuffer srcA = src.order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
                for (int i = 0; i < len / 4; i++) {
                    transformation = get(i);
                    transformation.pushIn(src.get());
                    float y = transformation.calc();
                    transformation.pushOut(y);
                    srcA.put(i, y);
                }
            }   break;
        }
    }

    @Override
    public void onRelease() {
    }

    protected static class Transformation {
        protected float[] mA;
        protected float[] mB;
        protected float[] mIn;
        protected float[] mOut;

        protected void pushIn(float value) {
            pushArray(mIn, value);
        }

        protected void pushOut(float value) {
            pushArray(mOut, value);
        }

        private void pushArray(float[] array, float value) {
            System.arraycopy(array, 0, array, 1, array.length - 1);
            array[0] = value;
        }

        private float calc() {
            float y = 0;
            for (int j = 0; j < mA.length; j++) {
                y += mA[j] * mIn[j];
            }
            for (int j = 0; j < mB.length; j++) {
                y += mB[j] * mOut[j];
            }
            return y;
        }
    }
}
