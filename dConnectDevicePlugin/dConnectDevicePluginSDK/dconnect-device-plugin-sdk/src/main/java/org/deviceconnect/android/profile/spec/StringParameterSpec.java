package org.deviceconnect.android.profile.spec;


import java.util.regex.Pattern;

public class StringParameterSpec extends DConnectParameterSpec<StringDataSpec> {

    private static final Pattern RGB_PATTERN = Pattern.compile("[0-9a-zA-Z]{6}");

    private StringParameterSpec(final DataFormat format) {
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

    @Override
    public boolean validate(final Object obj) {
        if (!super.validate(obj)) {
            return false;
        }
        if (obj == null) {
            return true;
        }
        if (!(obj instanceof String)) {
            return false;
        }
        String param = (String) obj;
        switch (getFormat()) {
            case TEXT:
                return validateLength(param);
            case BYTE:
            case BINARY:
                return true; // TODO バイナリのサイズ確認(現状、プラグインにはURL形式で通知される)
            case DATE:
                return true; // TODO RFC3339形式であることの確認
            case DATE_TIME:
                return true; // TODO RFC3339形式であることの確認
            case RGB:
                return RGB_PATTERN.matcher(param).matches();
            default:
                throw new IllegalStateException();
        }
    }

    private boolean validateLength(final String param) {
        if (getMaxLength() != null && param.length() > getMaxLength()) {
            return false;
        }
        if (getMinLength() != null && param.length() < getMinLength()) {
            return false;
        }
        return true;
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
