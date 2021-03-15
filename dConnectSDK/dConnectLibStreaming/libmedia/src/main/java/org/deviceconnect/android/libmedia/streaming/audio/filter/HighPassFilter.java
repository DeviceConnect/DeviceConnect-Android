package org.deviceconnect.android.libmedia.streaming.audio.filter;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;

public class HighPassFilter extends BaseFilter {
    public HighPassFilter(AudioQuality audioQuality, float coeff) {
        super(audioQuality, coeff);
    }

    @Override
    protected Transformation createTransformation() {
        float x = (float) Math.exp(-2 * Math.PI * mCoeff);
        Transformation transformation = new Transformation();
        transformation.mA = new float[]{(1 + x) / 2, -(1 + x) / 2};
        transformation.mB = new float[]{x};
        transformation.mIn = new float[transformation.mA.length];
        transformation.mOut = new float[transformation.mB.length];
        return transformation;
    }
}
