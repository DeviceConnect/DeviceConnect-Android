package org.deviceconnect.android.profile.spec;


public class StringParameterSpec extends DConnectParameterSpec<StringDataSpec> {

    StringParameterSpec(final DataFormat format) {
        super(new StringDataSpec(format));
    }

    public DataFormat getFormat() {
        return mDataSpec.getFormat();
    }

    public void setMaxLength(final Integer maxLength) {
        mDataSpec.setMaxLength(maxLength);
    }

    public String[] getEnumList() {
        return mDataSpec.getEnumList();
    }

    public void setMinLength(final Integer minLength) {
        mDataSpec.setMinLength(minLength);
    }

    public void setEnumList(final String[] enumList) {
        mDataSpec.setEnumList(enumList);
    }

    public Integer getMinLength() {
        return mDataSpec.getMinLength();
    }

    public Integer getMaxLength() {
        return mDataSpec.getMaxLength();
    }

    public static class Builder extends BaseBuilder<Builder> {

        private DataFormat mFormat;
        private Integer mMaxLength;
        private Integer mMinLength;
        private String[] mEnumList;

        public Builder setFormat(final DataFormat format) {
            mFormat = format;
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

        public Builder setEnumList(final String[] enumList) {
            mEnumList = enumList;
            return this;
        }

        public StringParameterSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.TEXT;
            }
            StringParameterSpec spec = new StringParameterSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            if (mEnumList != null) {
                spec.setEnumList(mEnumList);
            } else {
                spec.setMaxLength(mMaxLength);
                spec.setMinLength(mMinLength);
            }
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
