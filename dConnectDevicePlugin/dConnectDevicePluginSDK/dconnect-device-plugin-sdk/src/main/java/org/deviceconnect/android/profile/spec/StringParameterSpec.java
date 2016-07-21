package org.deviceconnect.android.profile.spec;


import java.util.regex.Pattern;

public class StringParameterSpec extends DConnectParameterSpec {

    private static final Pattern RGB_PATTERN = Pattern.compile("[0-9a-zA-Z]{6}");

    private final Format mFormat;
    private Integer mMaxLength;
    private Integer mMinLength;
    private String[] mEnumList;

    private StringParameterSpec(final Format format) {
        super(Type.STRING);
        mFormat = format;
    }

    public Format getFormat() {
        return mFormat;
    }

    void setMaxLength(final Integer maxLength) {
        mMaxLength = maxLength;
    }

    public Integer getMaxLength() {
        return mMaxLength;
    }

    void setMinLength(final Integer minLength) {
        mMinLength = minLength;
    }

    public Integer getMinLength() {
        return mMinLength;
    }

    void setEnumList(final String[] enumList) {
        mEnumList = enumList;
    }

    public String[] getEnumList() {
        return mEnumList;
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
        switch (mFormat) {
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
        if (mMaxLength != null && param.length() > mMaxLength) {
            return false;
        }
        if (mMinLength != null && param.length() < mMinLength) {
            return false;
        }
        return true;
    }

    public static class Builder extends BaseBuilder<Builder> {

        private Format mFormat;
        private Integer mMaxLength;
        private Integer mMinLength;
        private String[] mEnumList;

        public Builder setFormat(final Format format) {
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

        public StringParameterSpec build() {
            if (mFormat == null) {
                mFormat = Format.TEXT;
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

    public enum Format {
        TEXT("text"),
        BYTE("byte"),
        BINARY("binary"),
        DATE("date"),
        DATE_TIME("date-time"),
        RGB("rgb");

        private final String mName;

        Format(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static Format parse(final String name) {
            for (Format format : Format.values()) {
                if (format.mName.equalsIgnoreCase(name)) {
                    return format;
                }
            }
            return null;
        }
    }
}
