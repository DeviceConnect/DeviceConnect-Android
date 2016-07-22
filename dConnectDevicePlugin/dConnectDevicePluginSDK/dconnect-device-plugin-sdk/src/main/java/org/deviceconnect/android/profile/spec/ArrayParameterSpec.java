package org.deviceconnect.android.profile.spec;


public class ArrayParameterSpec extends DConnectParameterSpec<ArrayDataSpec> {

    ArrayParameterSpec(final DConnectDataSpec itemSpec) {
        super(new ArrayDataSpec(itemSpec));
    }

    public DConnectDataSpec getItemSpec() {
        return mDataSpec.getItemsSpec();
    }

    public Integer getMaxLength() {
        return mDataSpec.getMaxLength();
    }

    public void setMinLength(final Integer minLength) {
        mDataSpec.setMinLength(minLength);
    }

    public Integer getMinLength() {
        return mDataSpec.getMinLength();
    }

    public void setMaxLength(final Integer maxLength) {
        mDataSpec.setMaxLength(maxLength);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private DConnectDataSpec mItemSpec;
        private Integer mMaxLength;
        private Integer mMinLength;

        public Builder setItemsSpec(final DConnectDataSpec itemSpec) {
            mItemSpec = itemSpec;
            return this;
        }

        public Builder setMaxLength(final Integer maxLength) {
            mMaxLength = maxLength;
            return this;
        }

        public Builder setMinLength(final Integer minLength) {
            mMinLength = minLength;
            return this;
        }

        public ArrayParameterSpec build() {
            ArrayParameterSpec spec = new ArrayParameterSpec(mItemSpec);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setMaxLength(mMaxLength);
            spec.setMinLength(mMinLength);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
