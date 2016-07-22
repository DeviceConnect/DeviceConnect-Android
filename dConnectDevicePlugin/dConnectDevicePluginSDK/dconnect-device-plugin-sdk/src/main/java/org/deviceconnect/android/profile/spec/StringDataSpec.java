package org.deviceconnect.android.profile.spec;


public class StringDataSpec extends DConnectDataSpec {

    private final DataFormat mFormat;
    private Integer mMaxLength;
    private Integer mMinLength;
    private String[] mEnumList;

    StringDataSpec(final DataFormat format) {
        super(DataType.STRING);
        mFormat = format;
    }

    public DataFormat getFormat() {
        return mFormat;
    }

    public Integer getMaxLength() {
        return mMaxLength;
    }

    void setMaxLength(final Integer maxLength) {
        mMaxLength = maxLength;
    }

    public Integer getMinLength() {
        return mMinLength;
    }

    void setMinLength(final Integer minLength) {
        mMinLength = minLength;
    }

    public String[] getEnumList() {
        return mEnumList;
    }

    void setEnumList(final String[] enumList) {
        mEnumList = enumList;
    }

    public static class Builder {

        private DataFormat mFormat;
        private Integer mMaxLength;
        private Integer mMinLength;
        private String[] mEnumList;

        public Builder setFormat(final DataFormat format) {
            mFormat = format;
            return this;
        }

        public Builder setMaxLength(final int maxLength) {
            mMaxLength = maxLength;
            return this;
        }

        public Builder setMinLength(final int minLength) {
            mMinLength = minLength;
            return this;
        }

        public Builder setEnumList(final String[] enumList) {
            mEnumList = enumList;
            return this;
        }

        public StringDataSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.TEXT;
            }
            StringDataSpec spec = new StringDataSpec(mFormat);
            if (mEnumList != null) {
                spec.setEnumList(mEnumList);
            } else {
                spec.setMaxLength(mMaxLength);
                spec.setMinLength(mMinLength);
            }
            return spec;
        }
    }

}
