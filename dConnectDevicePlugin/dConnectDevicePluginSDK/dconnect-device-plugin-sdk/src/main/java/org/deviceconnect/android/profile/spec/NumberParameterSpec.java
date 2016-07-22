package org.deviceconnect.android.profile.spec;


public class NumberParameterSpec extends DConnectParameterSpec<NumberDataSpec> {

    NumberParameterSpec(final DataFormat format) {
        super(new NumberDataSpec(format));
    }

    public DataFormat getFormat() {
        return mDataSpec.getFormat();
    }

     void setMaximum(final Double maximum) {
        mDataSpec.setMaximum(maximum);
    }

    public Double getMinimum() {
        return mDataSpec.getMinimum();
    }

    void setMinimum(final Double minimum) {
        mDataSpec.setMinimum(minimum);
    }

    public boolean isExclusiveMaximum() {
        return mDataSpec.isExclusiveMaximum();
    }

    public boolean isExclusiveMinimum() {
        return mDataSpec.isExclusiveMinimum();
    }

    void setExclusiveMinimum(final boolean exclusiveMinimum) {
        mDataSpec.setExclusiveMinimum(exclusiveMinimum);
    }

    public Double getMaximum() {
        return mDataSpec.getMaximum();
    }

    void setExclusiveMaximum(final boolean exclusiveMaximum) {
        mDataSpec.setExclusiveMaximum(exclusiveMaximum);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private DataFormat mFormat;
        private Double mMaximum;
        private Double mMinimum;
        private Boolean mExclusiveMaximum;
        private Boolean mExclusiveMinimum;

        public Builder setFormat(final DataFormat format) {
            mFormat = format;
            return this;
        }

        public Builder setMaximum(final Double maximum) {
            mMaximum = maximum;
            return this;
        }

        public Builder setMinimum(final Double minimum) {
            mMinimum = minimum;
            return this;
        }

        public Builder setExclusiveMaximum(final Boolean exclusiveMaximum) {
            mExclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public Builder setExclusiveMinimum(final Boolean exclusiveMinimum) {
            mExclusiveMinimum = exclusiveMinimum;
            return this;
        }

        public NumberParameterSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.FLOAT;
            }
            NumberParameterSpec spec = new NumberParameterSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setMaximum(mMaximum);
            spec.setExclusiveMaximum(mExclusiveMaximum);
            spec.setMinimum(mMinimum);
            spec.setExclusiveMinimum(mExclusiveMinimum);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
