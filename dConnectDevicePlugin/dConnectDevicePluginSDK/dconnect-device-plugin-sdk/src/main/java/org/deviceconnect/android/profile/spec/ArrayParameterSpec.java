package org.deviceconnect.android.profile.spec;


public class ArrayParameterSpec extends DConnectParameterSpec {

    private Type mItemType;
    private Integer mMaxLength;
    private Integer mMinLength;

    private ArrayParameterSpec() {
        super(Type.ARRAY);
    }

    public Type getItemType() {
        return mItemType;
    }

    public void setItemType(final Type itemType) {
        mItemType = itemType;
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
        switch (mItemType) {
            case INTEGER:
                try {
                    String arrayParam = (String) obj;
                    String[] items = arrayParam.split(","); // TODO csv以外の形式に対応
                    for (String item : items) {
                        Long.parseLong(item);
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return true; // TODO 他のタイプ・フォーマットに対応
        }
    }

    public static class Builder extends BaseBuilder<Builder> {

        private Type mItemType;
        private Integer mMaxLength;
        private Integer mMinLength;

        public Builder setItemType(final Type itemType) {
            mItemType = itemType;
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

        public ArrayParameterSpec build() {
            ArrayParameterSpec spec = new ArrayParameterSpec();
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setItemType(mItemType);
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
