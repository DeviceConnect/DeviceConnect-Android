package org.deviceconnect.android.libmedia.streaming.audio.filter;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;

public class LowPassFilter extends BaseFilter {
    public LowPassFilter(AudioQuality audioQuality, float coeff) {
        super(audioQuality, coeff);
    }

    @Override
    protected Transformation createTransformation() {
        float x = (float) Math.exp(-2 * Math.PI * mCoeff);
        Transformation transformation = new Transformation();
        transformation.mA = new float[]{1 - x};
        transformation.mB = new float[]{x};
        transformation.mIn = new float[transformation.mA.length];
        transformation.mOut = new float[transformation.mB.length];
        return transformation;
    }
}
