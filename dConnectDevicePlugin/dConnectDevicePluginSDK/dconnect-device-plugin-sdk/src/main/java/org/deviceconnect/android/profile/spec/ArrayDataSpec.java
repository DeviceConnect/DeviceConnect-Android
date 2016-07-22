package org.deviceconnect.android.profile.spec;


public class ArrayDataSpec extends DConnectDataSpec {

    private final DConnectDataSpec mItemsSpec;
    private Integer mMaxLength;
    private Integer mMinLength;

    ArrayDataSpec(final DConnectDataSpec itemsSpec) {
        super(DataType.ARRAY);
        mItemsSpec = itemsSpec;
    }

    public DConnectDataSpec getItemsSpec() {
        return mItemsSpec;
    }

    public Integer getMaxLength() {
        return mMaxLength;
    }

    public void setMaxLength(final Integer maxLength) {
        mMaxLength = maxLength;
    }

    public Integer getMinLength() {
        return mMinLength;
    }

    public void setMinLength(final Integer minLength) {
        mMinLength = minLength;
    }

    @Override
    public boolean validate(final Object obj) {
        if (obj == null) {
            return true;
        }
        String arrayParam = obj.toString();
        if (arrayParam.equals("")) { // TODO allowEmptyValueに対応
            return true;
        }
        String[] items = arrayParam.split(","); // TODO csv以外の形式に対応
        for (String item : items) {
            if (!mItemsSpec.validate(item)) {
                return false;
            }
        }
        return true;
    }

    public static class Builder {

        private DConnectDataSpec mItemsSpec;
        private Integer mMaxLength;
        private Integer mMinLength;

        public Builder setItemsSpec(final DConnectDataSpec itemsSpec) {
            mItemsSpec = itemsSpec;
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

        public ArrayDataSpec build() {
            ArrayDataSpec spec = new ArrayDataSpec(mItemsSpec);
            spec.setMaxLength(mMaxLength);
            spec.setMinLength(mMinLength);
            return spec;
        }
    }
}
