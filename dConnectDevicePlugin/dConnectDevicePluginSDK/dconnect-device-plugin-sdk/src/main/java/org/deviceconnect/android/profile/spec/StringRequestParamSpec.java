package org.deviceconnect.android.profile.spec;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class StringRequestParamSpec extends DConnectRequestParamSpec {

    private static final String KEY_FORMAT = "format";
    private static final String KEY_MAX_LENGTH = "maxLength";
    private static final String KEY_MIN_LENGTH = "minLength";
    private static final String KEY_ENUM = "enum";

    private static final Pattern RGB_PATTERN = Pattern.compile("[0-9a-zA-Z]{6}");

    private final Format mFormat;
    private Integer mMaxLength;
    private Integer mMinLength;
    private String[] mEnumList;

    private StringRequestParamSpec(final Format format) {
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

    public static StringRequestParamSpec fromJson(final JSONObject json) throws JSONException {
        Builder builder = new Builder();
        builder.setRequired(json.getBoolean(KEY_REQUIRED));
        if (json.has(KEY_FORMAT)) {
            Format format = Format.parse(json.getString(KEY_FORMAT));
            if (format == null) {
                throw new IllegalArgumentException("format is invalid: " + json.getString(KEY_FORMAT));
            }
            builder.setFormat(format);
        }
        if (json.has(KEY_MAX_LENGTH)) {
            builder.setMaxLength(json.getInt(KEY_MAX_LENGTH));
        }
        if (json.has(KEY_MIN_LENGTH)) {
            builder.setMinLength(json.getInt(KEY_MIN_LENGTH));
        }
        if (json.has(KEY_ENUM)) {
            JSONArray array = json.getJSONArray(KEY_ENUM);
            String[] enumList = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                enumList[i] = array.getString(i);
            }
            builder.setEnumList(enumList);
        }
        return builder.build();
    }

    public static class Builder {
        private String mName;
        private boolean mIsRequired;
        private Format mFormat;
        private Integer mMaxLength;
        private Integer mMinLength;
        private String[] mEnumList;

        public void setName(final String name) {
            mName = name;
        }

        public Builder setRequired(final boolean isRequired) {
            mIsRequired = isRequired;
            return this;
        }

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

        public StringRequestParamSpec build() {
            if (mFormat == null) {
                mFormat = Format.TEXT;
            }
            StringRequestParamSpec spec = new StringRequestParamSpec(mFormat);
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
