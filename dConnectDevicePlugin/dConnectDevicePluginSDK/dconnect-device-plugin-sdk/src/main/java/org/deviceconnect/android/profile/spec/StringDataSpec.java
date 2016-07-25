package org.deviceconnect.android.profile.spec;


import java.util.regex.Pattern;

public class StringDataSpec extends DConnectDataSpec {

    private static final Pattern RGB_PATTERN = Pattern.compile("[0-9a-zA-Z]{6}");

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

    @Override
    public boolean validate(final Object obj) {
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
