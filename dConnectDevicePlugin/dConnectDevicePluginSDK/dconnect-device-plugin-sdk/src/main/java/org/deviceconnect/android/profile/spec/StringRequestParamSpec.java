package org.deviceconnect.android.profile.spec;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StringRequestParamSpec extends DConnectRequestParamSpec {

    private static final String FORMAT = "format";
    private static final String MAX_LENGTH = "maxLength";
    private static final String MIN_LENGTH = "minLength";
    private static final String ENUM = "enum";
    private static final String VALUE = "value";

    private final Format mFormat;
    private Integer mMaxLength;
    private Integer mMinLength;
    private Enum<String>[] mEnumList;

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

    void setEnumList(final Enum<String>[] enumList) {
        mEnumList = enumList;
    }

    public Enum<String>[] getEnumList() {
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
        builder.setName(json.getString(NAME));
        builder.setMandatory(json.getBoolean(MANDATORY));
        if (json.has(FORMAT)) {
            Format format = Format.parse(json.getString(FORMAT));
            if (format == null) {
                throw new IllegalArgumentException("format is invalid: " + json.getString(FORMAT));
            }
            builder.setFormat(format);
        }
        if (json.has(MAX_LENGTH)) {
            builder.setMaxLength(json.getInt(MAX_LENGTH));
        }
        if (json.has(MIN_LENGTH)) {
            builder.setMinLength(json.getInt(MIN_LENGTH));
        }
        if (json.has(ENUM)) {
            List<Enum<String>> enumList = new ArrayList<Enum<String>>();
            JSONArray array = json.getJSONArray(ENUM);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Enum<String> enumSpec =  new Enum<String>();
                enumSpec.setName(obj.getString(NAME));
                enumSpec.setValue(obj.getString(VALUE));
                enumList.add(enumSpec);
            }
            builder.setEnumList(enumList);
        }
        return builder.build();
    }

    public static class Builder {
        private String mName;
        private boolean mIsMandatory;
        private Format mFormat;
        private Integer mMaxLength;
        private Integer mMinLength;
        private List<Enum<String>> mEnumList;

        public Builder setName(final String name) {
            mName = name;
            return this;
        }

        public Builder setMandatory(final boolean isMandatory) {
            mIsMandatory = isMandatory;
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

        public Builder setEnumList(final List<Enum<String>> enumList) {
            mEnumList = enumList;
            return this;
        }

        public StringRequestParamSpec build() {
            if (mFormat == null) {
                mFormat = Format.TEXT;
            }
            StringRequestParamSpec spec = new StringRequestParamSpec(mFormat);
            spec.setName(mName);
            spec.setMandatory(mIsMandatory);
            if (mEnumList != null) {
                spec.setEnumList(mEnumList.toArray(new Enum[mEnumList.size()]));
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
        DATE_TIME("date-time");

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
